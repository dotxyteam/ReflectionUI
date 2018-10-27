import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.plugin.OptionButtonsPlugin;
import xy.reflect.ui.control.swing.plugin.OptionButtonsPlugin.OptionButtonsConfiguration;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
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
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.Menu;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.menu.builtin.swing.CloseWindowMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.OpenMenuItem;
import xy.reflect.ui.info.menu.builtin.swing.SaveAsMenuItem;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.CategoriesStyle;
import xy.reflect.ui.info.type.factory.ControlPluginActivationFactory;
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
		changeFieldsAndMethodOrder();
		allowToSetNull();
		hideSomeFieldsAndMethods();
		addVirtualFieldsAndMethods();
		categorizeFieldsAndMethods();
		customizeCopyCutPasteFeature();
		customizeColors();
		addMenus();
		useFieldControlPlugins();
		createCustomControls();
		useInfoCustomizationsClass();
		useXmlCustomizations();
		openObjectFrame();
	}

	private static void openObjectDialog() {
		/*
		 * Most basic use case: opening an object dialog:
		 */
		Object myObject = new HelloWorld();
		SwingRenderer.getDefault().openObjectDialog(null, myObject, myObject + " Dialog");
	}

	private static void createObjectForm() {
		/*
		 * create JPanel-based form in order to include it in a GUI as a sub-component.
		 */
		Object myObject = new HelloWorld();
		JOptionPane.showMessageDialog(null, SwingRenderer.getDefault().createForm(myObject), "As a form",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private static void changeFieldsAndMethodOrder() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
				return new InfoProxyFactory() {

					/*
					 * You can change the order of the fields of your class by overloading the
					 * following method:
					 */
					@Override
					protected List<IFieldInfo> getFields(ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(containingType));
							IFieldInfo nameField = ReflectionUIUtils.findInfoByName(result, "name");
							result.remove(nameField);
							result.add(0, nameField);
							return result;
						} else {
							return super.getFields(containingType);
						}
					}

					/*
					 * You can change the order of the methods of your class by overloading the
					 * following method:
					 */
					@Override
					protected List<IMethodInfo> getMethods(ITypeInfo type) {
						return super.getMethods(type);
					}

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Name field => first field");
	}

	private static void allowToSetNull() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
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

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Non-primitive fields => nullable");
	}

	private static void hideSomeFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
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

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "List fields => hidden");
	}

	private static void addVirtualFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
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

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Added virtual members (numberOfFields + resetFields())");
	}

	private static void categorizeFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
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
						return CategoriesStyle.MODERN;
					}

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "'Advanced properties' category created");
	}

	private static void customizeCopyCutPasteFeature() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
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

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Copy/Cut/Paste from lists => disabled");
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
		swingRenderer.openObjectDialog(null, myObject, "Custom colors");
	}

	private static void addMenus() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
				return new InfoProxyFactory() {

					/*
					 * In order to add a menu bar to windows created with ReflectionUI, you must
					 * override the following method. Note that each object can independently
					 * contribute via its ITypeInfo to the menu bar. The contributions are then
					 * merged by rendrerer.
					 */
					@Override
					protected MenuModel getMenuModel(ITypeInfo type) {
						if (type.getName().equals(HelloWorld.class.getName())) {
							MenuModel menuModel = new MenuModel();
							{
								Menu fileMenu = new Menu("File");
								{
									/*
									 * Some standards menu items are provided by the framework (open file, save
									 * file, quit, undo, redo, ...). They use the related features defined in the
									 * I*Info meta objects. The menu item categories allow to distinctly separate
									 * groups of menu items.
									 * 
									 */
									MenuItemCategory persistenceCategory = new MenuItemCategory("Persistence");
									{
										/*
										 * In our case we only need the displayed object to implement the Serializable
										 * interface for the following file menu items to works.
										 */
										OpenMenuItem openFileMenuItem = new OpenMenuItem();
										{
											openFileMenuItem.setName("Open Hello World File");
											persistenceCategory.addItem(openFileMenuItem);
										}
										SaveAsMenuItem saveAsFileMenuItem = new SaveAsMenuItem();
										{
											saveAsFileMenuItem.setName("Save Hello World File As...");
											persistenceCategory.addItem(saveAsFileMenuItem);
										}
										fileMenu.addItemCategory(persistenceCategory);
									}
									MenuItemCategory lifeCycleCategory = new MenuItemCategory("Life Cycle");
									{
										CloseWindowMenuItem exitMenuItem = new CloseWindowMenuItem();
										{
											exitMenuItem.setName("Quit");
											lifeCycleCategory.addItem(exitMenuItem);
										}
										fileMenu.addItemCategory(lifeCycleCategory);
									}
								}
								menuModel.setMenus(Arrays.asList(fileMenu));
							}
							return menuModel;
						} else {
							return super.getMenuModel(type);
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "With File Menu");
	}

	private static void createCustomControls() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI();
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI) {

			@Override
			public Form createForm(Object object, IInfoFilter infoFilter) {
				return new Form(this, object, infoFilter) {

					private static final long serialVersionUID = 1L;

					/*
					 * In order to create a custom field control:
					 */
					@Override
					public FieldControlPlaceHolder createFieldControlPlaceHolder(final IFieldInfo field) {
						return new FieldControlPlaceHolder(this.swingRenderer, this, field) {

							private static final long serialVersionUID = 1L;

							@Override
							public Component createFieldControl() {
								if (field.getName().equals("upperCase")) {
									final IFieldControlData data = getControlData();
									final JToggleButton result = new JToggleButton(data.getCaption());
									result.setSelected((Boolean) data.getValue());
									result.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											data.setValue(result.isSelected());
										}
									});
									return result;
								} else {
									return super.createFieldControl();
								}
							}

						};
					}

					/*
					 * In order to create a custom method control:
					 */
					@Override
					public MethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
						return new MethodControlPlaceHolder(this.swingRenderer, this, method) {

							private static final long serialVersionUID = 1L;

							@Override
							public Component createMethodControl() {
								// Create your custom method control here
								return super.createMethodControl();
							}

						};
					}

				};
			}

		};
		swingRenderer.openObjectDialog(null, myObject, "'Upper Case' field control => toggle button");
	}

	private static void useFieldControlPlugins() {
		Object myObject = new HelloWorld();

		/*
		 * Control plugins make it easy to customize the generated UIs by providing
		 * easy-to-use alternative controls. Each plugin potentially offers
		 * configuration options.
		 */
		final OptionButtonsPlugin radioButtonsPlugin = new OptionButtonsPlugin();
		final OptionButtonsConfiguration radioButtonsConfiguration = new OptionButtonsConfiguration();

		ReflectionUI reflectionUI = new ReflectionUI() {

			/*
			 * A plugin can be shared by several fields. It must therefore be associated
			 * with the desired fields as follows:
			 */
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
				return new InfoProxyFactory() {

					@Override
					protected ITypeInfo getType(IFieldInfo field, ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())
								&& field.getName().equals("language")) {
							ITypeInfo result = super.getType(field, containingType);
							result = new ControlPluginActivationFactory(radioButtonsPlugin.getIdentifier(),
									radioButtonsConfiguration).wrapTypeInfo(result);
							return result;
						} else {
							return super.getType(field, containingType);
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeInfoSource));
			}

		};

		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI) {

			/*
			 * The plugin must then be registered with the renderer for its activation to be
			 * effective:
			 */
			@Override
			public List<IFieldControlPlugin> getFieldControlPlugins() {
				List<IFieldControlPlugin> result = new ArrayList<IFieldControlPlugin>(super.getFieldControlPlugins());
				result.add(0, radioButtonsPlugin);
				return result;
			}

		};
		swingRenderer.openObjectDialog(null, myObject, "Language field control => radio buttons");
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
				"'Name' field caption modified using " + InfoCustomizations.class.getSimpleName() + " class");
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
			String message = "<!-- Check " + FileExplorer.class.getName() + ".java and " + Calculator.class.getName()
					+ ".java examples for more information -->";
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
		SwingRenderer.getDefault().openObjectDialog(null, myObject, myObject + " Frame");
	}

}