package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PolymorphicTypeOptionsFactory extends GenericEnumerationFactory {

	protected ITypeInfo polymorphicType;

	public PolymorphicTypeOptionsFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		super(reflectionUI, collectTypeOptions(polymorphicType).toArray(),
				"SubTypesEnumeration [polymorphicType=" + polymorphicType.getName() + "]", "");
		this.polymorphicType = polymorphicType;
	}

	protected static List<ITypeInfo> collectTypeOptions(final ITypeInfo polymorphicType) {
		final List<ITypeInfo> result = new ArrayList<ITypeInfo>(polymorphicType.getPolymorphicInstanceSubTypes());
		{
			if (polymorphicType.isConcrete()) {
				result.add(0, blockPolymorphism(polymorphicType));
			}
		}
		return result;
	}

	protected static ITypeInfo blockPolymorphism(final ITypeInfo type) {
		return new TypeInfoProxyFactory() {

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

		}.get(type);
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
		Collections.reverse(options);
		ITypeInfo result = null;
		for (ITypeInfo type : options) {
			if (type.supportsInstance(instance)) {
				return type;
			}
		}
		if (result == null) {
			result = reflectionUI
					.getTypeInfo(reflectionUI.getTypeInfoSource(instance));
			if(result.getName().equals(polymorphicType.getName())){
				result = blockPolymorphism(result);
			}
		}
		return result;
	}

	@Override
	protected String getItemIconImagePath(Object arrayItem) {
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
