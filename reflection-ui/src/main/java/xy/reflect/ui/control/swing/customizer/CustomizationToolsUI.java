package xy.reflect.ui.control.swing.customizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractMemberCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ParameterCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.AbstractMenuElement;
import xy.reflect.ui.info.menu.IMenuElement;
import xy.reflect.ui.info.menu.IMenuItemContainer;
import xy.reflect.ui.info.menu.Menu;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.factory.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
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

			protected boolean isDerivedTypeInfo(ITypeInfo type, Class<?> baseClass) {
				Class<?> clazz;
				try {
					clazz = ClassUtils.getCachedClassforName(type.getName());
				} catch (ClassNotFoundException e) {
					return false;
				}
				if (baseClass.isAssignableFrom(clazz)) {
					return true;
				}
				return false;
			}

			@Override
			protected String getCaption(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
				if (info instanceof GenericEnumerationFactory.ItemInfo) {
					Object item = ((GenericEnumerationFactory.ItemInfo) info).getItem();
					if (item instanceof IMenuElement) {
						List<IMenuElement> path = InfoCustomizations
								.getMenuElementPath(swingCustomizer.getInfoCustomizations(), (IMenuElement) item);
						if (path == null) {
							return ((IMenuElement) item).getName();
						}
						List<String> result = new ArrayList<String>();
						for (IMenuElement pathItem : path) {
							if (pathItem instanceof Menu) {
								result.add(pathItem.getName());
							} else if (pathItem instanceof MenuItemCategory) {
								result.add("(" + pathItem.getName() + ")");
							} else {
								throw new ReflectionUIError();
							}
						}
						return ReflectionUIUtils.stringJoin(result, " / ");
					}
				}
				return super.getCaption(info, parentEnumType);
			}

			@Override
			protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
				if ((object instanceof AbstractMemberCustomization) && field.getName().equals("category")) {
					List<CustomizationCategory> result = InfoCustomizations.getMemberCategoryOptions(
							swingCustomizer.getInfoCustomizations(), (AbstractMemberCustomization) object);
					return result.toArray();
				} else if ((object instanceof MethodCustomization) && field.getName().equals("menuLocation")) {
					TypeCustomization tc = InfoCustomizations.findParentTypeCustomization(
							swingCustomizer.getInfoCustomizations(), (MethodCustomization) object);
					List<IMenuItemContainer> result = InfoCustomizations.getAllMenuItemContainers(tc);
					return result.toArray();
				} else {
					return super.getValueOptions(object, field, containingType);
				}
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if (isDerivedTypeInfo(type, AbstractCustomization.class)
						|| isDerivedTypeInfo(type, AbstractMenuElement.class)) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
					IFieldInfo uidField = ReflectionUIUtils.findInfoByName(result, "uniqueIdentifier");
					result.remove(uidField);
					return result;
				} else {
					return super.getFields(type);
				}
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
									SwingRenderer renderer = swingCustomizer.getCustomizationTools().getToolsRenderer();
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
				if (object instanceof TypeCustomization) {
					return ((TypeCustomization) object).getTypeName();
				} else if (object instanceof FieldCustomization) {
					return ((FieldCustomization) object).getFieldName();
				} else if (object instanceof MethodCustomization) {
					return ((MethodCustomization) object).getMethodName();
				} else if (object instanceof ParameterCustomization) {
					return ((ParameterCustomization) object).getParameterName();
				} else if (object instanceof ColumnCustomization) {
					return ((ColumnCustomization) object).getColumnName();
				} else if (object instanceof CustomizationCategory) {
					return ((CustomizationCategory) object).getCaption();
				} else if (object instanceof ResourcePath) {
					return ((ResourcePath) object).getSpecification();
				} else if (object instanceof IMenuElement) {
					return ((IMenuElement) object).getName();
				} else {
					return super.toString(type, object);
				}
			}

		}.get(result);
		result = swingCustomizer.getCustomizationTools().getToolsCustomizationsFactory().get(result);
		return result;
	}

}