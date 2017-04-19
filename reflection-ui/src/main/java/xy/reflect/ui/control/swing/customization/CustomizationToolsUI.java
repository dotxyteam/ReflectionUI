package xy.reflect.ui.control.swing.customization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.factory.InfoCustomizations;
import xy.reflect.ui.info.type.factory.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.factory.InfoCustomizations.AbstractMemberCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.type.factory.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.util.ResourcePath;

class CustomizationToolsUI extends ReflectionUI {

	protected final SwingCustomizer swingCustomizer;

	CustomizationToolsUI(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
	}

	@Override
	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		ITypeInfo result = super.getTypeInfo(typeSource);
		result = new TypeInfoProxyFactory() {
			@Override
			public String toString() {
				return CustomizationTools.class.getName() + TypeInfoProxyFactory.class.getSimpleName();
			}

			@Override
			protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
				if ((object instanceof AbstractMemberCustomization) && field.getName().equals("category")) {
					TypeCustomization tc = findParentTypeCustomization((AbstractMemberCustomization) object,
							swingCustomizer.getInfoCustomizations());
					List<CustomizationCategory> categories = tc.getMemberCategories();
					return categories.toArray(new CustomizationCategory[categories.size()]);
				} else {
					return super.getValueOptions(object, field, containingType);
				}
			}

			protected TypeCustomization findParentTypeCustomization(AbstractMemberCustomization custumizationMember,
					InfoCustomizations infoCustomizations) {
				for (TypeCustomization tc : infoCustomizations.getTypeCustomizations()) {
					for (FieldCustomization fc : tc.getFieldsCustomizations()) {
						if (fc == custumizationMember) {
							return tc;
						}
						TypeCustomization fieldTc = findParentTypeCustomization(custumizationMember,
								fc.getSpecificTypeCustomizations());
						if (fieldTc != null) {
							return fieldTc;
						}
					}
					for (MethodCustomization mc : tc.getMethodsCustomizations()) {
						if (mc == custumizationMember) {
							return tc;
						}
					}
				}
				return null;
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if (type.getName().equals(ListCustomization.class.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					result.add(getListItemTypeCustomizationDisplayMethod(swingCustomizer.getInfoCustomizations()));
					return result;
				} else {
					return super.getMethods(type);
				}
			}

			protected IMethodInfo getListItemTypeCustomizationDisplayMethod(
					final InfoCustomizations infoCustomizations) {
				return new IMethodInfo() {

					@Override
					public boolean isReturnValueNullable() {
						return false;
					}

					@Override
					public boolean isReturnValueDetached() {
						return false;
					}

					@Override
					public Map<String, Object> getSpecificProperties() {
						return Collections.emptyMap();
					}

					@Override
					public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
						return null;
					}

					@Override
					public String getOnlineHelp() {
						return null;
					}

					@Override
					public String getName() {
						return "displayItemTypeCustomization";
					}

					@Override
					public String getCaption() {
						return "Display Item Type Customization";
					}

					@Override
					public String getIconImagePath() {
						return null;
					}

					@Override
					public void validateParameters(Object object, InvocationData invocationData) throws Exception {
					}

					@Override
					public boolean isReadOnly() {
						return true;
					}

					@Override
					public Object invoke(final Object object, InvocationData invocationData) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								ListCustomization lc = (ListCustomization) object;
								if (lc.getItemTypeName() == null) {
									SwingRenderer renderer = swingCustomizer.getCustomizationTools()
											.getCustomizationToolsRenderer();
									renderer.openInformationDialog(null, "The item type is not defined",
											renderer.getObjectTitle(lc), renderer.getObjectIconImage(lc));
								} else {
									TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations,
											lc.getItemTypeName());
									swingCustomizer.getCustomizationTools().openCustomizationEditor(null, t);
								}
							}
						});
						return null;
					}

					@Override
					public ValueReturnMode getValueReturnMode() {
						return ValueReturnMode.DIRECT_OR_PROXY;
					}

					@Override
					public Runnable getUndoJob(Object object, InvocationData invocationData) {
						return null;
					}

					@Override
					public ITypeInfo getReturnValueType() {
						return null;
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}

					@Override
					public String getNullReturnValueLabel() {
						return null;
					}

					@Override
					public InfoCategory getCategory() {
						return null;
					}
				};
			}

			@Override
			protected String toString(ITypeInfo type, Object object) {
				if (object instanceof CustomizationCategory) {
					return ((CustomizationCategory) object).getCaption();
				} else if (object instanceof ResourcePath) {
					return ((ResourcePath) object).getSpecification();
				} else {
					return super.toString(type, object);
				}
			}

		}.get(result);
		result = swingCustomizer.getCustomizationTools().getCustomizationToolsCustomizations().get(this, result);
		return result;
	}

}