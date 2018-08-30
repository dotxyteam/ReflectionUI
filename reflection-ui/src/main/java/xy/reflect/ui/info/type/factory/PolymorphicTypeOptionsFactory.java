package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PolymorphicTypeOptionsFactory extends GenericEnumerationFactory {

	protected ITypeInfo polymorphicType;

	public PolymorphicTypeOptionsFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		super(reflectionUI, getTypeOptionsCollector(polymorphicType),
				"SubTypesEnumeration [polymorphicType=" + polymorphicType.getName() + "]", "", false);
		this.polymorphicType = polymorphicType;
	}

	protected static Iterable<ITypeInfo> getTypeOptionsCollector(final ITypeInfo polymorphicType) {
		return new Iterable<ITypeInfo>() {

			@Override
			public Iterator<ITypeInfo> iterator() {
				final List<ITypeInfo> result = new ArrayList<ITypeInfo>(
						polymorphicType.getPolymorphicInstanceSubTypes());
				{
					if (polymorphicType.isConcrete()) {
						result.add(0, blockPolymorphism(polymorphicType));
					}
				}
				return result.iterator();
			}
		};

	}

	protected static ITypeInfo blockPolymorphism(final ITypeInfo type) {
		return new InfoProxyFactory() {

			@Override
			protected String getCaption(ITypeInfo type) {
				return ReflectionUIUtils.composeMessage("Basic", super.getCaption(type));
			}

			@Override
			public String getIdentifier() {
				return "PolymorphicRecursionBlocker [polymorphicType=" + type.getName() + "]";
			}

			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
				return Collections.emptyList();
			}

		}.wrapTypeInfo(type);
	}

	public List<ITypeInfo> getTypeOptions() {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		for (Object arrayItem : iterable) {
			result.add((ITypeInfo) arrayItem);
		}
		return result;
	}

	public ITypeInfo guessSubType(Object instance) {
		List<ITypeInfo> options = new ArrayList<ITypeInfo>(getTypeOptions());
		ITypeInfo polymorphicTypeAsValidOption = null;
		ITypeInfo validSubType = null;
		for (ITypeInfo type : options) {
			if (type.supportsInstance(instance)) {
				if (type.getName().equals(polymorphicType.getName())) {
					polymorphicTypeAsValidOption = type;
				} else {
					if (validSubType != null) {
						throw new ReflectionUIError(
								"Failed to guess the polymorphic value type option: Ambiguity detected: More than 1 valid types found:"
										+ "\n- " + validSubType.getName() + "\n- " + type.getName()
										+ "\nInheritance between these types is not supported");
					}
					validSubType = type;
				}
			}
		}
		if (validSubType != null) {
			ITypeInfo actualType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(instance));
			if (actualType.getName().equals(polymorphicType.getName())) {
				throw new ReflectionUIError("Polymorphism inconsistency detected:" + "\n- Base type: "
						+ polymorphicType.getName() + "\n- Current sub-type: " + validSubType.getName()
						+ "\n- Actual type: " + actualType.getName());
			}
			return validSubType;
		}
		if (polymorphicTypeAsValidOption != null) {
			return polymorphicTypeAsValidOption;
		}
		ITypeInfo actualType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(instance));
		if (actualType.getName().equals(polymorphicType.getName())) {
			return blockPolymorphism(actualType);
		}
		return null;
	}

	@Override
	protected ResourcePath getItemIconImagePath(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getIconImagePath();
	}

	@Override
	protected String getItemOnlineHelp(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getOnlineHelp();
	}

	@Override
	protected String getItemName(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getName();
	}

	@Override
	protected String getItemCaption(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getCaption();
	}

	@Override
	public String toString() {
		return "PolymorphicTypeOptionsFactory [polymorphicType=" + polymorphicType + "]";
	}

}
