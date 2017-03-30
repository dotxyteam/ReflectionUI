package xy.reflect.ui.info.type.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PolymorphicTypeOptionsFactory extends ArrayAsEnumerationFactory {

	protected ITypeInfo polymorphicType;

	public PolymorphicTypeOptionsFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		super(reflectionUI, collectTypeOptions(polymorphicType).toArray(),
				"SubTypesEnumeration [polymorphicType=" + polymorphicType.getName() + "]", "");
		this.polymorphicType = polymorphicType;
	}

	public static List<ITypeInfo> collectTypeOptions(final ITypeInfo polymorphicType) {
		final List<ITypeInfo> result = new ArrayList<ITypeInfo>(polymorphicType.getPolymorphicInstanceSubTypes());
		{
			if (polymorphicType.isConcrete()) {
				if (!result.contains(polymorphicType)) {
					result.add(0, new TypeInfoProxyFactory() {

						@Override
						protected String getCaption(ITypeInfo type) {
							return ReflectionUIUtils.composeMessage("Basic", super.getCaption(type));
						}

						@Override
						public String getIdentifier() {
							return "PolymorphicRecursionBlocker [polymorphicType=" + polymorphicType.getName() + "]";
						}

						@Override
						protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
							return Collections.emptyList();
						}

					}.get(polymorphicType));
				}
			}
		}
		return result;
	}

	public List<ITypeInfo> getTypeOptions() {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		for (Object arrayItem : array) {
			result.add((ITypeInfo) arrayItem);
		}
		return result;
	}

	@Override
	protected Map<String, Object> getItemSpecificProperties(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		Map<String, Object> result = new HashMap<String, Object>();
		DesktopSpecificProperty.setIconImageFilePath(result, DesktopSpecificProperty
				.getIconImageFilePath(DesktopSpecificProperty.accessInfoProperties(polyTypesItem)));
		DesktopSpecificProperty.setIconImage(result,
				DesktopSpecificProperty.getIconImage(DesktopSpecificProperty.accessInfoProperties(polyTypesItem)));
		return result;
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
