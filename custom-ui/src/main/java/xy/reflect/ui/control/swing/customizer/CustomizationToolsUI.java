/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.control.swing.customizer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractMemberCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ConversionMethodFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomTypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldTypeSpecificities;
import xy.reflect.ui.info.custom.InfoCustomizations.IMenuElementCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.IMenuItemContainerCustomization;
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
import xy.reflect.ui.info.menu.IMenuElementPosition;
import xy.reflect.ui.info.menu.MenuElementKind;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class CustomizationToolsUI extends CustomizedUI {

	protected static final String IS_FIELD_TYPE_SPECIFICITIES_TYPE = CustomizationToolsUI.class.getName() + ".is"
			+ FieldTypeSpecificities.class.getSimpleName();

	protected final SwingCustomizer swingCustomizer;

	public CustomizationToolsUI(InfoCustomizations infoCustomizations, SwingCustomizer swingCustomizer) {
		super(infoCustomizations);
		this.swingCustomizer = swingCustomizer;
	}

	@Override
	protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
		type = new InfoProxyFactory() {

			@Override
			public String toString() {
				return "Before" + CustomizationTools.class.getName() + InfoProxyFactory.class.getSimpleName();
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
				Object item = info.getItem();
				if (item instanceof IMenuItemContainerCustomization) {
					IMenuElementPosition position = InfoCustomizations.getMenuElementPosition(
							swingCustomizer.getInfoCustomizations(), (IMenuItemContainerCustomization) item);
					if (position == null) {
						return ((IMenuItemContainerCustomization) item).getName();
					}
					List<String> result = new ArrayList<String>();
					result.add(position.getElementName());
					while (position.getParent() != null) {
						position = position.getParent();
						if (position.getElementKind() == MenuElementKind.MENU) {
							result.add(0, position.getElementName());
						} else if (position.getElementKind() == MenuElementKind.ITEM_CATEGORY) {
							result.add(0, "(" + position.getElementName() + ")");
						} else {
							throw new ReflectionUIError();
						}
					}
					return ReflectionUIUtils.stringJoin(result, " / ");
				}
				if (info.getItem() instanceof ResourcePath) {
					ResourcePath resourcePath = (ResourcePath) item;
					return resourcePath.getDefaultSpecification();
				}
				return super.getCaption(info, parentEnumType);
			}

			@Override
			protected boolean hasValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
				if ((object instanceof AbstractMemberCustomization) && field.getName().equals("categoryCaption")) {
					return true;
				} else if ((object instanceof MethodCustomization) && field.getName().equals("menuLocation")) {
					return true;
				} else {
					return super.hasValueOptions(object, field, containingType);
				}
			}

			@Override
			protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
				if ((object instanceof AbstractMemberCustomization) && field.getName().equals("categoryCaption")) {
					List<String> result = InfoCustomizations.getMemberCategoryCaptionOptions(
							swingCustomizer.getInfoCustomizations(), (AbstractMemberCustomization) object);
					return result.toArray();
				} else if ((object instanceof MethodCustomization) && field.getName().equals("menuLocation")) {
					TypeCustomization tc = InfoCustomizations.findParentTypeCustomization(
							swingCustomizer.getInfoCustomizations(), (MethodCustomization) object);
					List<IMenuItemContainerCustomization> result = InfoCustomizations
							.getAllMenuItemContainerCustomizations(tc);
					return result.toArray();
				} else {
					return super.getValueOptions(object, field, containingType);
				}
			}

			@Override
			protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
				if (type.getName().equals(FieldTypeSpecificities.class.getName())) {
					Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
					result.put(IS_FIELD_TYPE_SPECIFICITIES_TYPE, Boolean.TRUE);
					return result;
				}
				return super.getSpecificProperties(type);
			}

			@Override
			protected String getName(ITypeInfo type) {
				if (type.getName().equals(FieldTypeSpecificities.class.getName())) {
					return InfoCustomizations.class.getName();
				}
				return super.getName(type);
			}

			@Override
			protected String getCaption(ITypeInfo type) {
				if (type.getName().equals(FieldTypeSpecificities.class.getName())) {
					return "";
				}
				return super.getCaption(type);
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
						InvocationData lastInvocationData = swingCustomizer.getLastInvocationDataByIdentifier()
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
						if (swingCustomizer.isCustomizationsEditorEnabled()) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									ListCustomization lc = (ListCustomization) object;
									if (lc.getItemTypeName() == null) {
										SwingRenderer renderer = swingCustomizer.getCustomizationTools()
												.getToolsRenderer();
										renderer.openInformationDialog(null, "The item type is not defined",
												renderer.getObjectTitle(lc), renderer.getObjectIconImage(lc));
									} else {
										TypeCustomization t = InfoCustomizations.getTypeCustomization(
												swingCustomizer.getInfoCustomizations(), lc.getItemTypeName());
										swingCustomizer.getCustomizationTools().openCustomizationEditor(null, t);
									}
								}
							});
						}
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
					String result = ((ResourcePath) object).getSpecification();
					if (result.length() == 0) {
						result = "<Path not specified>";
					}
					return result;
				} else if (object instanceof IMenuElementCustomization) {
					return ((IMenuElementCustomization) object).getName();
				} else if (object instanceof TypeConversion) {
					String result = "To ";
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
				} else if (object instanceof FieldTypeSpecificities) {
					return "";
				} else {
					return super.toString(type, object);
				}
			}

			@Override
			protected ITypeInfo getType(IFieldInfo field, ITypeInfo containingType) {
				if (field.getType().getName().equals(ColorSpecification.class.getName())) {
					return getTypeInfo(new JavaTypeInfoSource(Color.class,
							new SpecificitiesIdentifier(containingType.getName(), field.getName())));
				}
				return super.getType(field, containingType);
			}

			@Override
			protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
				if (field.getType().getName().equals(ColorSpecification.class.getName())) {
					ColorSpecification colorSpec = (ColorSpecification) super.getValue(object, field, containingType);
					if (colorSpec == null) {
						return null;
					}
					return SwingRendererUtils.getColor(colorSpec);
				} else {
					return super.getValue(object, field, containingType);
				}
			}

			@Override
			protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
				if (field.getType().getName().equals(ColorSpecification.class.getName())) {
					Color color = (Color) value;
					ColorSpecification colorSpec = null;
					if (color != null) {
						colorSpec = SwingRendererUtils.getColorSpecification(color);
					}
					super.setValue(object, colorSpec, field, containingType);
				} else {
					super.setValue(object, value, field, containingType);
				}
			}

			@Override
			protected Object invoke(Object object, InvocationData invocationData, IMethodInfo method,
					ITypeInfo containingType) {
				return super.invoke(object, invocationData, method, containingType);
			}

		}.wrapTypeInfo(type);
		return type;
	}

	@Override
	protected ITypeInfo getTypeInfoAfterCustomizations(ITypeInfo type) {
		type = new InfoProxyFactory() {

			@Override
			public String toString() {
				return "After" + CustomizationTools.class.getName() + InfoProxyFactory.class.getSimpleName();
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if (Boolean.TRUE.equals(type.getSpecificProperties().get(IS_FIELD_TYPE_SPECIFICITIES_TYPE))) {
					List<IFieldInfo> result = new ArrayList<>(super.getFields(type));
					for (Iterator<IFieldInfo> it = result.iterator(); it.hasNext();) {
						IFieldInfo field = it.next();
						InfoCategory category = field.getCategory();
						if (category != null) {
							if (!Arrays.asList("Types", "Lists", "Enumerations").contains(category.getCaption())) {
								it.remove();
							}
						}
					}
					return result;
				}
				return super.getFields(type);
			}

		}.wrapTypeInfo(type);
		return type;
	}

	@Override
	public void logError(String msg) {
		super.logError(msg);
	}

}
