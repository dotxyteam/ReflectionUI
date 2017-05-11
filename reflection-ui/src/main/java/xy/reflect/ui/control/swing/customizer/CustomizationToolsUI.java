package xy.reflect.ui.control.swing.customizer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractMemberCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ConversionMethodFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomTypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ITypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.JavaClassBasedTypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.Mapping;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ParameterCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeConversion;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.AbstractMenuElement;
import xy.reflect.ui.info.menu.IMenuElement;
import xy.reflect.ui.info.menu.IMenuItemContainer;
import xy.reflect.ui.info.menu.Menu;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.factory.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

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
			protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
				super.setValue(object, value, field, containingType);
				if (containingType.getName().equals(CustomizationController.class.getName())
						&& (field.getName().equals("inEditMode"))) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							rebuildAllDisplayedForms();
						}

						void rebuildAllDisplayedForms() {
							for (JPanel form : SwingRendererUtils.excludeSubForms(
									SwingRendererUtils.getAllDisplayedForms(swingCustomizer), swingCustomizer)) {
								swingCustomizer.getCustomizationTools().rebuildCustomizerForm(form);
							}
						}
					});
				}
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
					List<IFieldInfo> result = new ArrayList<IFieldInfo>();
					for (IFieldInfo field : super.getFields(type)) {
						if (field.getName().equals(InfoCustomizations.UID_FIELD_NAME)) {
							continue;
						}
						if (field.getName().equals(InfoCustomizations.INITIAL_STATE_FIELD_NAME)) {
							continue;
						}
						result.add(field);
					}
					return result;
				} else {
					return super.getFields(type);
				}
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if (type.getName().equals(ListCustomization.class.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					result.add(getListItemTypeCustomizationDisplayMethod());
					return result;
				} else if (type.getName().equals(MethodCustomization.class.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					result.add(getLastInvocationDataStorageMethod());
					return result;
				} else {
					return super.getMethods(type);
				}
			}

			protected IMethodInfo getLastInvocationDataStorageMethod() {
				return new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

					@Override
					public String getSignature() {
						return ReflectionUIUtils.buildMethodSignature(this);
					}

					@Override
					public String getName() {
						return "storeLastInvocationData";
					}

					@Override
					public String getCaption() {
						return "Store Last Invocation Data";
					}

					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						MethodCustomization mc = (MethodCustomization) object;
						InvocationData lastInvocationData = swingCustomizer.getLastInvocationDataByMethodSignature()
								.get(mc.getMethodSignature());
						if (lastInvocationData == null) {
							throw new ReflectionUIError(
									"Last invocation data not found for the method '" + mc.getMethodSignature() + "'");
						}
						List<TextualStorage> storages = new ArrayList<InfoCustomizations.TextualStorage>(
								mc.getSerializedInvocationDatas());
						TextualStorage newStorage = new TextualStorage();
						newStorage.save(lastInvocationData);
						storages.add(newStorage);
						mc.setSerializedInvocationDatas(storages);
						return null;
					}

					@Override
					public boolean isReadOnly() {
						return false;
					}

					@Override
					public Runnable getNextInvocationUndoJob(final Object object, InvocationData invocationData) {
						return new Runnable() {

							@Override
							public void run() {
								MethodCustomization mc = (MethodCustomization) object;
								List<TextualStorage> storages = new ArrayList<InfoCustomizations.TextualStorage>(
										mc.getSerializedInvocationDatas());
								storages.remove(storages.size() - 1);
								mc.setSerializedInvocationDatas(storages);
							}
						};
					}

				};
			}

			protected IMethodInfo getListItemTypeCustomizationDisplayMethod() {
				return new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

					@Override
					public String getSignature() {
						return ReflectionUIUtils.buildMethodSignature(this);
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
									TypeCustomization t = InfoCustomizations.getTypeCustomization(
											swingCustomizer.getInfoCustomizations(), lc.getItemTypeName());
									swingCustomizer.getCustomizationTools().openCustomizationEditor(null, t);
								}
							}
						});
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
				} else if (object instanceof TypeConversion) {
					String result = "Convert To ";
					ITypeInfoFinder newTypeFinder = ((TypeConversion) object).getNewTypeFinder();
					if (ReflectionUIUtils.equalsOrBothNull(newTypeFinder, new TypeConversion().getNewTypeFinder())) {
						result += "...";
					} else {
						result += "<" + ReflectionUIUtils.toString(CustomizationToolsUI.this, newTypeFinder) + ">";
					}
					return result;
				} else if (object instanceof Mapping) {
					String result = "Map";
					ConversionMethodFinder reverseConversion = ((Mapping) object).getReverseConversionMethodFinder();
					if (reverseConversion != null) {
						if (reverseConversion.getConversionMethodSignature() != null) {
							String typeName = ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(
									reverseConversion.getConversionMethodSignature());
							result += " From " + typeName;
						}
					}
					ConversionMethodFinder conversion = ((Mapping) object).getConversionMethodFinder();
					if (conversion != null) {
						if (conversion.getConversionMethodSignature() != null) {
							String typeName = ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(
									conversion.getConversionMethodSignature());
							result += " To " + typeName;
						}
					}
					result += "...";
					return result;
				} else if (object instanceof JavaClassBasedTypeInfoFinder) {
					return ((JavaClassBasedTypeInfoFinder) object).getClassName();
				} else if (object instanceof CustomTypeInfoFinder) {
					return "Custom Type Implemented By " + ((CustomTypeInfoFinder) object).getImplementationClassName();
				} else if (object instanceof TextualStorage) {
					Object stored = ((TextualStorage) object).load();
					String result = ReflectionUIUtils.toString(CustomizationToolsUI.this, stored);
					if (stored != null) {
						ITypeInfo storedType = CustomizationToolsUI.this
								.getTypeInfo(CustomizationToolsUI.this.getTypeInfoSource(stored));
						result = "(" + storedType.getCaption() + ") " + result;
					}
					return result;
				} else {
					return super.toString(type, object);
				}
			}

		}.get(result);
		result = swingCustomizer.getCustomizationTools().getToolsCustomizationsFactory().get(result);
		return result;
	}

}