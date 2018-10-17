import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.app.ApplicationInfoProxy;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.CategoriesStyle;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class Tutorial {

	public static void main(String[] args) {
		/*
		 * Each of the following methods demonstrates a feature of the library.
		 */

		openObjectDialog();
		createObjectForm();
		allowToSetNull();
		hideSomeFieldsAndMethods();
		addVirtualFieldsAndMethods();
		categorizeFieldsAndMethods();
		customizeCopyCutPasteFeature();
		customizeColors();
		useInfoCustomizationsClass();
		useXmlCustomizations();
		openObjectFrame();
	}

	private static void openObjectDialog() {
		/*
		 * Most basic use case: opening an object dialog:
		 */
		Object myObject = new HelloWorld();
		SwingRenderer.getDefault().openObjectDialog(null, myObject);
	}

	private static void createObjectForm() {
		/*
		 * create JPanel-based form in order to include it in a GUI as a sub-component.
		 */
		Object myObject = new HelloWorld();
		JOptionPane.showMessageDialog(null, SwingRenderer.getDefault().createForm(myObject), "As a form",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private static void allowToSetNull() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * By default, the generated user interface does not allow to assign <null>
					 * values. Indeed, the developers do not generally allow the assignment of
					 * <null> values from their graphical interface despite the fact that it is
					 * authorized by the language. The following methods allow to selectively enable
					 * or disable the nullable facet of any value displayed in the generated GUI.
					 */

					@Override
					protected boolean isNullValueDistinct(IParameterInfo param, IMethodInfo method,
							ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							return !param.getType().isPrimitive();
						} else {
							return super.isNullValueDistinct(param, method, containingType);
						}
					}

					@Override
					protected boolean isNullValueDistinct(IFieldInfo field, ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							return !field.getType().isPrimitive();
						} else {
							return super.isNullValueDistinct(field, containingType);
						}
					}

					@Override
					protected boolean isNullReturnValueDistinct(IMethodInfo method, ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							return !method.getReturnValueType().isPrimitive();
						} else {
							return super.isNullReturnValueDistinct(method, containingType);
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Non-primitive fields => nullable", null, false, true);
	}

	private static void hideSomeFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * This is how you can hide fields and methods:
					 */

					@Override
					protected boolean isHidden(IFieldInfo field, ITypeInfo containingType) {
						if (field.getType() instanceof IListTypeInfo) {
							return true;
						} else {
							return super.isHidden(field, containingType);
						}
					}

					@Override
					protected boolean isHidden(IMethodInfo method, ITypeInfo containingType) {
						return super.isHidden(method, containingType);
					}

					@Override
					protected boolean isHidden(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
						return super.isHidden(param, method, containingType);
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "List fields => hidden", null, false, true);
	}

	private static void addVirtualFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * This is how you can add virtual fields and methods that would generally be
					 * calculated from the existing ones:
					 */

					@Override
					protected List<IFieldInfo> getFields(ITypeInfo type) {
						if (type.getName().equals(HelloWorld.class.getName())) {
							final List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
							result.add(new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

								@Override
								public String getName() {
									return "numberOfFields";
								}

								@Override
								public String getCaption() {
									return "(Virtual) Number Of Fields";
								}

								@Override
								public Object getValue(Object object) {
									return result.size();
								}

							});
							return result;
						} else {
							return super.getFields(type);
						}
					}

					@Override
					protected List<IMethodInfo> getMethods(ITypeInfo type) {
						if (type.getName().equals(HelloWorld.class.getName())) {
							List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
							result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

								@Override
								public String getName() {
									return "resetFields";
								}

								@Override
								public String getCaption() {
									return "(Virtual) Reset Fields";
								}

								@Override
								public Object invoke(Object object, InvocationData invocationData) {
									HelloWorld newObject = new HelloWorld();
									for (IFieldInfo field : ReflectionUI.getDefault()
											.getTypeInfo(new JavaTypeInfoSource(HelloWorld.class, null)).getFields()) {
										if (field.isGetOnly()) {
											continue;
										}
										field.setValue(object, field.getValue(newObject));
									}
									return null;
								}

							});
							return result;
						} else {
							return super.getMethods(type);
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Added virtual members (numberOfFields + resetFields())", null,
				false, true);
	}

	private static void categorizeFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * you have the ability to categorize the fields and methods of a class like
					 * this:
					 */

					@Override
					protected InfoCategory getCategory(IFieldInfo field, ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							if (!field.getName().equals("name")) {
								return new InfoCategory("Advanced", 1);
							}
						}
						return super.getCategory(field, containingType);
					}

					@Override
					protected InfoCategory getCategory(IMethodInfo method, ITypeInfo containingType) {
						return super.getCategory(method, containingType);
					}

					/*
					 * This method allows you to modify the display of categories (classic, modern,
					 * ...):
					 */
					@Override
					protected CategoriesStyle getCategoriesStyle(ITypeInfo type) {
						// TODO Auto-generated method stub
						return super.getCategoriesStyle(type);
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "'Advanced properties' category created", null, false, true);
	}

	private static void customizeCopyCutPasteFeature() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * copy/cut/paste: By default this feature is enabled on collections/arrays but
					 * only for Serializable items. If your item class does not implement the
					 * Serializable interface yet you want to allow to copy its instances (or if you
					 * want to customize the copy process) then override the following methods. Here
					 * actually we will simply disable the feature on all lists:
					 */

					@Override
					public boolean canCopy(ITypeInfo type, Object object) {
						return false;
					}

					@Override
					public Object copy(ITypeInfo type, Object object) {
						throw new AssertionError();
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Copy/Cut/Paste from lists => disabled", null, false, true);
	}

	private static void customizeColors() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public IApplicationInfo getApplicationInfo() {
				return new ApplicationInfoProxy(super.getApplicationInfo()) {

					@Override
					public boolean isSystemIntegrationCrossPlatform() {
						return true;
					}

					@Override
					public ColorSpecification getTitleBackgroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.RED.darker());
					}

					@Override
					public ColorSpecification getTitleForegroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.WHITE);
					}

					@Override
					public ColorSpecification getMainBackgroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.BLACK);
					}

					@Override
					public ColorSpecification getMainForegroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.WHITE);
					}

					@Override
					public ColorSpecification getMainBorderColor() {
						return SwingRendererUtils.getColorSpecification(Color.GRAY);
					}

					@Override
					public ColorSpecification getButtonBackgroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.CYAN);
					}

					@Override
					public ColorSpecification getButtonForegroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.BLACK);
					}

					@Override
					public ColorSpecification getButtonBorderColor() {
						return SwingRendererUtils.getColorSpecification(Color.RED);
					}

				};
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Custom colors", null, false, true);
	}

	private static void useInfoCustomizationsClass() {
		Object myObject = new HelloWorld();

		/*
		 * Create a CustomizedUI: This is a subclass of ReflectionUI allowing the use of
		 * declarative customization of type informations.
		 */
		InfoCustomizations customizations = new InfoCustomizations();
		CustomizedUI customizedUI = new CustomizedUI(customizations);

		/*
		 * Initilize the customization of the chosen type.
		 */
		TypeCustomization helloWorldTypeCustomization = InfoCustomizations.getTypeCustomization(customizations,
				HelloWorld.class.getName(), true);

		/*
		 * Customize a field of this type.
		 */
		FieldCustomization nameFieldCustomization = InfoCustomizations
				.getFieldCustomization(helloWorldTypeCustomization, "name", true);
		nameFieldCustomization.setCustomFieldCaption(
				"(Caption modified through " + InfoCustomizations.class.getSimpleName() + " class) Name");

		/*
		 * Open the customized UI.
		 */
		SwingRenderer swingRenderer = new SwingRenderer(customizedUI);
		swingRenderer.openObjectDialog(null, myObject,
				"'Name' field caption modified using " + InfoCustomizations.class.getSimpleName() + " class", null,
				false, true);
	}

	private static void useXmlCustomizations() {
		try {
			/*
			 * InfoCustomizations can be serialized/deserialized to XML.
			 */
			InfoCustomizations customizations = new InfoCustomizations();
			CustomizedUI customizedUI = new CustomizedUI(customizations);

			/*
			 * Customize a field.
			 */
			TypeCustomization helloWorldTypeCustomization = InfoCustomizations.getTypeCustomization(customizations,
					HelloWorld.class.getName(), true);
			FieldCustomization nameFieldCustomization = InfoCustomizations
					.getFieldCustomization(helloWorldTypeCustomization, "name", true);
			nameFieldCustomization.setCustomFieldCaption(
					"(Caption modified through " + InfoCustomizations.class.getSimpleName() + " class) Name");

			/*
			 * Saving customizations to an XML stream.
			 */
			ByteArrayOutputStream memoryOutput = new ByteArrayOutputStream();
			customizations.saveToStream(memoryOutput, ReflectionUIUtils.getDebugLogListener(customizedUI));

			/*
			 * Loading customizations from an XML stream.
			 */
			ByteArrayInputStream memoryInput = new ByteArrayInputStream(memoryOutput.toByteArray());
			customizations.loadFromStream(memoryInput, ReflectionUIUtils.getDebugLogListener(customizedUI));

			/*
			 * Displaying XML serialization text.
			 */
			String message = "<!-- Check the source code of " + FileExplorer.class.getName() + " and "
					+ Calculator.class.getName() + " classes for more information -->";
			message += "\n\n" + memoryOutput.toString();
			CustomizedSwingRenderer.getDefault().openInformationDialog(null, message, "XML customizations example",
					null);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private static void openObjectFrame() {
		/*
		 * Opening an object frame:
		 */
		Object myObject = new HelloWorld();
		SwingRenderer.getDefault().openObjectDialog(null, myObject);
	}

}