
package xy.reflect.ui.control.swing.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodContext;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.builder.DialogBuilder;
import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.control.swing.plugin.ColorPickerPlugin;
import xy.reflect.ui.control.swing.plugin.CustomCheckBoxPlugin;
import xy.reflect.ui.control.swing.plugin.DatePickerPlugin;
import xy.reflect.ui.control.swing.plugin.DateTimePickerPlugin;
import xy.reflect.ui.control.swing.plugin.DetailedListControlPlugin;
import xy.reflect.ui.control.swing.plugin.EditorPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.FormattedNumberPlugin;
import xy.reflect.ui.control.swing.plugin.HtmlPlugin;
import xy.reflect.ui.control.swing.plugin.ImageViewPlugin;
import xy.reflect.ui.control.swing.plugin.MultipleLinesTextPlugin;
import xy.reflect.ui.control.swing.plugin.OptionButtonsPlugin;
import xy.reflect.ui.control.swing.plugin.PasswordFieldPlugin;
import xy.reflect.ui.control.swing.plugin.SingleLineTextPlugin;
import xy.reflect.ui.control.swing.plugin.SliderPlugin;
import xy.reflect.ui.control.swing.plugin.SpinnerPlugin;
import xy.reflect.ui.control.swing.plugin.SplitFormPlugin;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin;
import xy.reflect.ui.control.swing.plugin.ToggleButtonPlugin;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.control.swing.util.WindowManager;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractActionMenuItemInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.BetterFutureTask;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ReschedulableTask;
import xy.reflect.ui.util.SystemProperties;

/**
 * This is the {@link ReflectionUI} renderer class for Swing-based UIs.
 * 
 * @author olitank
 *
 */
public class SwingRenderer {

	public static void main(String[] args) throws Exception {
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instantiate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + SystemProperties.describe();
		final Class<?> clazz;
		if (args.length == 0) {
			clazz = Object.class;
		} else if (args.length == 1) {
			if (args[0].equals("--help")) {
				System.out.println(usageText);
				return;
			} else {
				clazz = Class.forName(args[0]);
			}
		} else {
			throw new IllegalArgumentException(usageText);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ReflectionUI reflectionUI = SwingRenderer.getDefault().getReflectionUI();
				Object object = SwingRenderer.getDefault().onTypeInstantiationRequest(null,
						reflectionUI.getTypeInfo(new JavaTypeInfoSource(clazz, null)));
				if (object == null) {
					return;
				}
				SwingRenderer.getDefault().openObjectFrame(object);
			}
		});
	}

	protected static SwingRenderer defaultInstance;

	/**
	 * @return the default instance of this class.
	 */
	public static SwingRenderer getDefault() {
		if (defaultInstance == null) {
			Class<?> customClass = SystemProperties.getAlternateDefaultSwingRendererClass();
			if (customClass != null) {
				try {
					defaultInstance = (SwingRenderer) customClass.getMethod("getDefault").invoke(null);
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			} else {
				defaultInstance = new SwingRenderer(ReflectionUI.getDefault());
			}
		}
		return defaultInstance;
	}

	protected ReflectionUI reflectionUI;
	protected Map<String, InvocationData> lastInvocationDataByMethodSignature = new HashMap<String, InvocationData>();

	protected List<Form> allDisplayedForms = new ArrayList<Form>();

	protected static final String BUSY_DIALOG_JOB_EXECUTOR_NAME = SwingRenderer.class.getName()
			+ ".busyDialogJobExecutor";
	protected ExecutorService busyDialogJobExecutor = MiscUtils.newExecutor(BUSY_DIALOG_JOB_EXECUTOR_NAME, 1);

	protected static final String BUSY_DIALOG_CLOSER_NAME = SwingRenderer.class.getName() + ".busyDialogCloser";
	protected ExecutorService busyDialogCloser = MiscUtils.newExecutor(BUSY_DIALOG_CLOSER_NAME, 1);

	protected static final String FORM_VALIDATOR_NAME = SwingRenderer.class.getName() + ".formValidator";
	protected ExecutorService formValidator = MiscUtils.newExecutor(FORM_VALIDATOR_NAME, 1);

	protected static final String DELAYED_UPDATE_EXECUTOR_NAME = SwingRenderer.class.getName()
			+ ".delayedUpdateExecutor";
	protected ExecutorService delayedUpdateExecutor = MiscUtils.newExecutor(DELAYED_UPDATE_EXECUTOR_NAME, 1);

	/**
	 * Constructs an instance that will render abstract UI models generated by the
	 * given {@link ReflectionUI} object.
	 * 
	 * @param reflectionUI The abstract UI model generator.
	 */
	public SwingRenderer(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	/**
	 * @return the abstract UI model generator use by this renderer.
	 */
	public ReflectionUI getReflectionUI() {
		return reflectionUI;
	}

	/**
	 * @return an executor service intended to execute form validation jobs.
	 */
	public ExecutorService getFormValidator() {
		return formValidator;
	}

	/**
	 * @return all displayed forms that were generated using this renderer.
	 */
	public List<Form> getAllDisplayedForms() {
		return allDisplayedForms;
	}

	/**
	 * @return the last parameters that were used to invoke methods identified by
	 *         their signature.
	 */
	public Map<String, InvocationData> getLastInvocationDataByMethodSignature() {
		return lastInvocationDataByMethodSignature;
	}

	/**
	 * @param activatorComponent The owner component of the exception update process
	 *                           or null.
	 * @param updateJob          The specific update action.
	 * @param delayMilliseconds  The number of milliseconds to wait before executing
	 *                           the update.
	 * @return a re-schedulable delayed update task.
	 */
	public ReschedulableTask createDelayedUpdateProcess(Component activatorComponent, Runnable updateJob,
			int delayMilliseconds) {
		return new ReschedulableTask() {
			@Override
			protected void execute() {
				SwingUtilities.invokeLater(() -> {
					try {
						updateJob.run();
					} catch (Throwable t) {
						handleException(activatorComponent, t);
					}
				});
			}

			@Override
			protected ExecutorService getTaskExecutor() {
				return delayedUpdateExecutor;
			}

			@Override
			protected long getExecutionDelayMilliseconds() {
				return delayMilliseconds;
			}
		};
	}

	/**
	 * @param string The string to display. Note that only label and messages are
	 *               processed by this method. Field/parameter values and method
	 *               return values of type 'String' are not processed.
	 * @return an eventual adjustment/translation of the provided string before it
	 *         gets displayed on the UI.
	 */
	public String prepareMessageToDisplay(String string) {
		return string;
	}

	/**
	 * @param input An object providing what is needed to manage the method call
	 *              from the UI.
	 * @return an action that can collect parameters, invoke the given method and
	 *         eventually display the result.
	 */
	public MethodAction createMethodAction(IMethodControlInput input) {
		return new MethodAction(this, input);
	}

	/**
	 * @param object Any object.
	 * @return a form allowing to view and edit the given object.
	 */
	public final Form createForm(Object object) {
		return createForm(object, IInfoFilter.DEFAULT);
	}

	/**
	 * @param object     Any object.
	 * @param infoFilter An object allowing to filter out some fields or methods.
	 * @return a form allowing to view and edit the given object.
	 */
	public Form createForm(final Object object, IInfoFilter infoFilter) {
		return new Form(this, object, infoFilter);
	}

	/**
	 * @return the plugins providing additional field controls.
	 */
	public List<IFieldControlPlugin> getFieldControlPlugins() {
		List<IFieldControlPlugin> result = new ArrayList<IFieldControlPlugin>();
		result.add(new SplitFormPlugin());
		result.add(new FormattedNumberPlugin());
		result.add(new OptionButtonsPlugin());
		result.add(new SliderPlugin());
		result.add(new SpinnerPlugin());
		result.add(new FileBrowserPlugin());
		result.add(new ColorPickerPlugin());
		result.add(new ImageViewPlugin());
		result.add(new ToggleButtonPlugin());
		result.add(new CustomCheckBoxPlugin());
		result.add(new DetailedListControlPlugin());
		result.add(new StyledTextPlugin());
		result.add(new PasswordFieldPlugin());
		result.add(new HtmlPlugin());
		result.add(new EditorPlugin());
		result.add(new DatePickerPlugin());
		result.add(new DateTimePickerPlugin());
		result.add(new SingleLineTextPlugin());
		result.add(new MultipleLinesTextPlugin());
		return result;
	}

	/**
	 * @return the virtual category in which non-categorized members will be put
	 *         when there are at least 1 defined category.
	 */
	public InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1, null);
	}

	/**
	 * @param object The object to describe.
	 * @return words describing the given object type in an elegant way.
	 */
	public String getObjectTitle(Object object) {
		if (object == null) {
			return "<Missing Value>";
		}
		return reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object)).getCaption();
	}

	/**
	 * @param object The object to describe.
	 * @return an icon image describing the given object type.
	 */
	public Image getObjectIconImage(Object object) {
		if (object == null) {
			return null;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		ResourcePath imagePath = type.getIconImagePath(object);
		if (imagePath == null) {
			return null;
		}
		return getIconImage(imagePath);
	}

	/**
	 * @param appInfo The application to describe.
	 * @return an icon image describing the given application.
	 */
	public Image getApplicationIconImage(IApplicationInfo appInfo) {
		if (appInfo.getIconImagePath() == null) {
			return null;
		}
		return getIconImage(appInfo.getIconImagePath());
	}

	/**
	 * @param iconImagePath The path indicating the icon image location.
	 * @return an icon image loaded from the resource indicated by given path.
	 */
	public Image getIconImage(ResourcePath iconImagePath) {
		return SwingRendererUtils.loadImageThroughCache(iconImagePath,
				ReflectionUIUtils.getErrorLogListener(reflectionUI));
	}

	/**
	 * @param category The category to describe.
	 * @return an icon image describing the given category.
	 */
	public Image getCategoryIconImage(InfoCategory category) {
		if (category.getIconImagePath() == null) {
			return null;
		}
		return getIconImage(category.getIconImagePath());
	}

	/**
	 * @param data The object describing the method.
	 * @return an icon image representing the method described by the given object.
	 */
	public Image getMethodIconImage(IMethodControlData data) {
		ResourcePath imagePath = data.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return getIconImage(imagePath);
	}

	/**
	 * @param itemInfo The enumeration item to describe.
	 * @return an icon image describing the given enumeration item.
	 */
	public Image getEnumerationItemIconImage(IEnumerationItemInfo itemInfo) {
		ResourcePath imagePath = itemInfo.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return getIconImage(imagePath);
	}

	/**
	 * @param menuItem The menu item to be described.
	 * @return an icon image that must be associated with the given menu item.
	 */
	public Image getMenuItemIconImage(AbstractActionMenuItemInfo menuItem) {
		ResourcePath imagePath = menuItem.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return getIconImage(imagePath);
	}

	/**
	 * This method manages the display of exceptions typically thrown by underlying
	 * objects when interacting with their generated UIs.
	 * 
	 * @param activatorComponent The owner component of the exception dialog or
	 *                           null.
	 * @param t                  The exception to be displayed.
	 */
	public void handleException(Component activatorComponent, final Throwable t) {
		reflectionUI.logDebug(t);
		openErrorDialog(activatorComponent, "An Error Occurred", t);
	}

	/**
	 * Allows to retrieve the parameter values and execute the specified method.
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           eventual dialogs or null.
	 * @param objectType         The method owner type.
	 * @param object             The method owner or null for static methods or
	 *                           constructors.
	 * @param method             The method to be executed.
	 * @return the method call return value or null if the method class was
	 *         cancelled or the return type is "void".
	 */
	public Object onMethodInvocationRequest(final Component activatorComponent, final ITypeInfo objectType,
			final IMethodInfo method, final Object object) {
		MethodAction methodAction = createMethodAction(new IMethodControlInput() {

			ModificationStack dummyModificationStack = new ModificationStack(null);

			@Override
			public ModificationStack getModificationStack() {
				return dummyModificationStack;
			}

			@Override
			public IContext getContext() {
				return new MethodContext(objectType, method);
			}

			@Override
			public IMethodControlData getControlData() {
				return new DefaultMethodControlData(reflectionUI, object, method);
			}
		});
		methodAction.setShouldDisplayReturnValueIfAny(false);
		methodAction.onInvocationRequest(activatorComponent);
		return methodAction.getReturnValue();
	}

	/**
	 * Allows to create an instance of the specified type. Dialogs may be displayed
	 * to allow to select the constructor or provide parameter values. If sub-types
	 * of the given type are know, then a type selection dialog will be displayed.
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           eventual dialogs or null.
	 * @param type               An abstract UI type information object.
	 * @return an object created using the given type information.
	 */
	public Object onTypeInstantiationRequest(final Component activatorComponent, ITypeInfo type) {
		try {
			if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {

				PolymorphicTypeOptionsFactory enumFactory = new PolymorphicTypeOptionsFactory(reflectionUI, type);
				List<ITypeInfo> polyTypes = enumFactory.getTypeOptions();

				if (polyTypes.size() == 1) {
					return onTypeInstantiationRequest(activatorComponent, polyTypes.get(0));
				} else {
					IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
							.getTypeInfo(enumFactory.getInstanceTypeInfoSource(null));
					Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null, "Choose a type",
							"New '" + type.getCaption() + "'");
					if (resultEnumItem == null) {
						return null;
					}
					return onTypeInstantiationRequest(activatorComponent,
							(ITypeInfo) enumFactory.getInstanceItem(resultEnumItem));
				}
			} else {
				List<IMethodInfo> constructors = new ArrayList<IMethodInfo>();
				{
					for (IMethodInfo ctor : type.getConstructors()) {
						if (ctor.isHidden()) {
							continue;
						}
						constructors.add(ctor);
					}
				}
				if (type.isConcrete() && (constructors.size() > 0)) {
					final IMethodInfo chosenConstructor;
					if (constructors.size() == 1) {
						chosenConstructor = constructors.get(0);
					} else {
						constructors = new ArrayList<IMethodInfo>(constructors);
						Collections.sort(constructors, new Comparator<IMethodInfo>() {

							@Override
							public int compare(IMethodInfo o1, IMethodInfo o2) {
								return new Integer(o1.getParameters().size())
										.compareTo(new Integer(o2.getParameters().size()));
							}
						});
						final GenericEnumerationFactory enumFactory = new GenericEnumerationFactory(reflectionUI,
								constructors.toArray(), "ConstructorSelection [type=" + type.getName() + "]", "") {
							protected String getItemCaption(Object choice) {
								return ReflectionUIUtils.getContructorDescription((IMethodInfo) choice);
							}
						};
						IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
								.getTypeInfo(enumFactory.getInstanceTypeInfoSource(null));
						Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null,
								"Choose an option", "Create " + type.getCaption());
						if (resultEnumItem == null) {
							return null;
						}
						chosenConstructor = (IMethodInfo) enumFactory.getInstanceItem(resultEnumItem);
						if (chosenConstructor == null) {
							return null;
						}
					}
					return onMethodInvocationRequest(activatorComponent, type, chosenConstructor, null);
				} else {
					String typeCaption = type.getCaption();
					String msg;
					if (typeCaption.length() == 0) {
						msg = "Create";
					} else {
						msg = "Create " + type.getCaption() + " of type";
					}
					String className = openInputDialog(activatorComponent, "", msg, null);
					if (className == null) {
						return null;
					}
					try {
						type = reflectionUI
								.getTypeInfo(new JavaTypeInfoSource(ClassUtils.getCachedClassForName(className), null));
					} catch (ClassNotFoundException e) {
						throw new ReflectionUIError(e);
					}
					if (type == null) {
						return null;
					} else {
						return onTypeInstantiationRequest(activatorComponent, type);
					}
				}
			}
		} catch (Throwable t) {
			throw new ReflectionUIError("Could not create '" + type.getCaption() + "': " + t.toString(), t);
		}
	}

	/**
	 * @param type The type to instantiate.
	 * @return whether dialogs would be displayed when calling
	 *         {@link #onTypeInstantiationRequest(Component, ITypeInfo)} with the
	 *         given type.
	 */
	public boolean isDecisionRequiredOnTypeInstantiationRequest(ITypeInfo type) {
		if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
			final PolymorphicTypeOptionsFactory enumFactory = new PolymorphicTypeOptionsFactory(reflectionUI, type);
			List<ITypeInfo> polyTypes = enumFactory.getTypeOptions();
			if (polyTypes.size() == 1) {
				return isDecisionRequiredOnTypeInstantiationRequest(polyTypes.get(0));
			} else {
				return true;
			}
		} else {
			List<IMethodInfo> constructors = new ArrayList<IMethodInfo>();
			{
				for (IMethodInfo ctor : type.getConstructors()) {
					if (ctor.isHidden()) {
						continue;
					}
					constructors.add(ctor);
				}
			}
			if (type.isConcrete() && (constructors.size() > 0)) {
				final IMethodInfo chosenConstructor;
				if (constructors.size() == 1) {
					chosenConstructor = constructors.get(0);
				} else {
					return true;
				}
				return ReflectionUIUtils.requiresParameterValue(chosenConstructor);
			} else {
				return true;
			}
		}
	}

	/**
	 * Displays a dialog allowing to select a value from a list of enumeration
	 * items.
	 * 
	 * @param parentComponent A component belonging to the parent window or null.
	 * @param enumType        The enumeration type holding the list of displayed
	 *                        items.
	 * @param initialEnumItem The initially selected item.
	 * @param message         A displayed description.
	 * @param title           The title of the displayed window or null to have the
	 *                        default title.
	 * @return the selected item or null if the selection was cancelled.
	 */
	public Object openSelectionDialog(Component parentComponent, IEnumerationTypeInfo enumType, Object initialEnumItem,
			String message, String title) {
		if (initialEnumItem == null) {
			initialEnumItem = enumType.getValues()[0];
		}
		final Object[] chosenItemHolder = new Object[] { initialEnumItem };

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(
				reflectionUI, enumType, "StandardSelection [title="
						+ ((title == null) ? null : ReflectionUIUtils.secureNameContent(title)) + "]",
				"Selection", message);
		encapsulation.setFieldGetOnly(false);
		encapsulation.setFieldNullValueDistinct(false);
		Object encapsulatedChosenItem = encapsulation.getInstance(chosenItemHolder);

		if (!openObjectDialog(parentComponent, encapsulatedChosenItem, title,
				getObjectIconImage(encapsulatedChosenItem), true, true).isCancelled()) {
			return chosenItemHolder[0];
		} else {
			return null;
		}
	}

	/**
	 * Displays a dialog allowing to select a value from a list of items.
	 * 
	 * @param parentComponent  A component belonging to the parent window or null.
	 * @param options          The array holding the list of selectable items.
	 * @param initialSelection The initially selected item.
	 * @param message          A displayed description.
	 * @param title            The title of the displayed window or null to have the
	 *                         default title.
	 * @return the selected item or null if the selection was cancelled.
	 */
	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent, final List<T> options, T initialSelection,
			String message, String title) {
		if (options.size() == 0) {
			throw new ReflectionUIError();
		}
		final GenericEnumerationFactory enumFactory = new GenericEnumerationFactory(reflectionUI, options.toArray(),
				"SelectionDialogArrayAsEnumeration [title="
						+ ((title == null) ? null : ReflectionUIUtils.secureNameContent(title)) + "]",
				"") {

			Map<Object, String> captions = new HashMap<Object, String>();
			Map<Object, Image> iconImages = new HashMap<Object, Image>();

			{
				for (Object choice : options) {
					captions.put(choice, ReflectionUIUtils.toString(SwingRenderer.this.reflectionUI, choice));
					iconImages.put(choice, getObjectIconImage(choice));
				}
			}

			@Override
			protected ResourcePath getItemIconImagePath(Object choice) {
				Image image = iconImages.get(choice);
				if (image == null) {
					return null;
				}
				return SwingRendererUtils.putImageInCache(image);
			}

			@Override
			protected String getItemName(Object choice) {
				return "Option [caption=" + captions.get(choice) + "]";
			}

			@Override
			protected String getItemCaption(Object choice) {
				return captions.get(choice);
			}

		};
		IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
				.getTypeInfo(enumFactory.getInstanceTypeInfoSource(null));
		Object resultEnumItem = openSelectionDialog(parentComponent, enumType,
				enumFactory.getItemInstance(initialSelection), message, title);
		if (resultEnumItem == null) {
			return null;
		}
		T result = (T) enumFactory.getInstanceItem(resultEnumItem);
		return result;

	}

	/**
	 * Displays a dialog allowing to view, edit and then provide a value of any
	 * type. Note that the dialog is modal.
	 * 
	 * @param activatorComponent A component belonging to the parent window or null.
	 * @param initialValue       The initially selected value.
	 * @param valueCaption       A displayed description.
	 * @param title              The title of the displayed window or null to have
	 *                           the default title.
	 * @return the input value or null if the dialog was cancelled.
	 */
	@SuppressWarnings("unchecked")
	public <T> T openInputDialog(Component activatorComponent, T initialValue, String valueCaption, String title) {
		if (initialValue == null) {
			throw new ReflectionUIError();
		}
		final Object[] valueHolder = new Object[] { initialValue };
		ITypeInfo initialValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(initialValue));

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, initialValueType,
				"StandardInput [title=" + ((title == null) ? null : ReflectionUIUtils.secureNameContent(title)) + "]",
				"Input", valueCaption);
		encapsulation.setFieldGetOnly(false);
		encapsulation.setFieldNullValueDistinct(false);
		Object encapsulatedValue = encapsulation.getInstance(valueHolder);

		StandardEditorBuilder editorBuilder = createEditorBuilder(activatorComponent, encapsulatedValue, title,
				getObjectIconImage(encapsulatedValue), true);
		editorBuilder.createAndShowDialog();
		if (!editorBuilder.isCancelled()) {
			return (T) valueHolder[0];
		} else {
			return null;
		}
	}

	/**
	 * Displays a dialog allowing to answer a question by "Yes" or "No".
	 * 
	 * @param activatorComponent A component belonging to the parent window or null.
	 * @param question           The question to be displayed.
	 * @param title              The title of the displayed window or null to have
	 *                           the default title.
	 * @return whether "yes" was chosen or not.
	 */
	public boolean openQuestionDialog(Component activatorComponent, String question, String title) {
		return openQuestionDialog(activatorComponent, question, title, "Yes", "No");
	}

	/**
	 * Displays a dialog allowing to answer a question by "Yes" or "No".
	 * 
	 * @param activatorComponent A component belonging to the parent window or null.
	 * @param question           The question to be displayed.
	 * @param title              The title of the displayed window or null to have
	 *                           the default title.
	 * @param yesCaption         The actual text displayed on the 'yes' button.
	 * @param noCaption          The actual text displayed on the 'no' button.
	 * @return whether 'yes' was chosen or not.
	 */
	public boolean openQuestionDialog(Component activatorComponent, String question, String title,
			final String yesCaption, final String noCaption) {
		final DialogBuilder dialogBuilder = createDialogBuilder(activatorComponent);
		dialogBuilder.setButtonBarControls(
				new ArrayList<Component>(dialogBuilder.createStandardOKCancelDialogButtons(yesCaption, noCaption)));
		dialogBuilder.setContentComponent(SwingRendererUtils.getMessagePane(prepareMessageToDisplay(question),
				JOptionPane.QUESTION_MESSAGE, this));
		dialogBuilder.setTitle(title);
		showDialog(dialogBuilder.createDialog(), true);
		return dialogBuilder.getCreatedDialog().wasOkPressed();
	}

	/**
	 * Displays a simple text information dialog. Note that the dialog is modal.
	 * 
	 * @param activatorComponent A component belonging to the parent window or null.
	 * @param msg                The message to be displayed.
	 * @param title              The title of the displayed window or null to have
	 *                           the default title.
	 * @param iconImage          The icon image of the displayed window.
	 */
	public void openInformationDialog(Component activatorComponent, String msg, String title) {
		DialogBuilder dialogBuilder = createDialogBuilder(activatorComponent);

		List<Component> buttons = new ArrayList<Component>();
		buttons.add(dialogBuilder.createDialogClosingButton("Close", null));

		dialogBuilder.setTitle(title);
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getMessagePane(prepareMessageToDisplay(msg), JOptionPane.INFORMATION_MESSAGE, this));
		dialogBuilder.setButtonBarControls(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	/**
	 * Displays an error dialog. Note that the dialog is modal.
	 * 
	 * @param activatorComponent A component belonging to the parent window or null.
	 * @param title              The title of the displayed window or null to have
	 *                           the default title.
	 * @param iconImage          The icon image of the displayed window.
	 * @param error              The exception corresponding to the error to
	 *                           display.
	 */
	public void openErrorDialog(Component activatorComponent, String title, final Throwable error) {
		DialogBuilder dialogBuilder = createDialogBuilder(activatorComponent);

		List<Component> buttons = new ArrayList<Component>();
		final JButton deatilsButton = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public String retrieveText() {
				return SwingRenderer.this.prepareMessageToDisplay("Details");
			}

			@Override
			public Image retrieveBackgroundImage() {
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath() != null) {
					return SwingRendererUtils.loadImageThroughCache(
							reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI));
				}
				return null;
			}

			@Override
			public Font retrieveCustomFont() {
				if (reflectionUI.getApplicationInfo().getButtonCustomFontResourcePath() != null) {
					return SwingRendererUtils.loadFontThroughCache(
							reflectionUI.getApplicationInfo().getButtonCustomFontResourcePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI));
				}
				return null;
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonBackgroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveForegroundColor() {
				if (reflectionUI.getApplicationInfo().getMainButtonForegroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonForegroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveBorderColor() {
				if (reflectionUI.getApplicationInfo().getMainButtonBorderColor() != null) {
					return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainButtonBorderColor());
				}
				return null;
			}
		};
		deatilsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openErrorDetailsDialog(deatilsButton, error);
			}
		});
		buttons.add(deatilsButton);
		buttons.add(dialogBuilder.createDialogClosingButton("Close", null));

		dialogBuilder.setTitle(title);
		dialogBuilder.setContentComponent(SwingRendererUtils
				.getMessagePane(prepareMessageToDisplay(formatErrorMessage(error)), JOptionPane.ERROR_MESSAGE, this));
		dialogBuilder.setButtonBarControls(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	/**
	 * @param error The exception resulting from the error to display.
	 * @return An elegant error message from the given exception.
	 */
	public String formatErrorMessage(Throwable error) {
		return MiscUtils.getPrettyErrorMessage(error);
	}

	/**
	 * Displays a detailed error dialog (with stack trace, ...). Note that the
	 * dialog is modal.
	 * 
	 * @param activatorComponent A component belonging to the parent window or null.
	 * @param error              The exception resulting from the error to display.
	 */
	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		String stackTraceString = MiscUtils.getPrintedStackTrace(error);
		final DialogBuilder dialogBuilder = createDialogBuilder(activatorComponent);
		dialogBuilder.setButtonBarControls(
				Collections.<Component>singletonList(dialogBuilder.createDialogClosingButton("Close", null)));
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getMessagePane(stackTraceString, JOptionPane.INFORMATION_MESSAGE, this));
		dialogBuilder.setTitle("Error Details");
		showDialog(dialogBuilder.createDialog(), true);
	}

	/**
	 * Opens a dialog allowing to edit the given object. Note that the dialog is
	 * modal.
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @param object             Any object.
	 * @return the editor builder allowing to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object) {
		return openObjectDialog(activatorComponent, object, null);
	}

	/**
	 * Opens a dialog allowing to edit the given object. Note that the dialog is
	 * modal.
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @param object             Any object.
	 * @param title              The title of the dialog or null to have the default
	 *                           title.
	 * @return the editor builder allowing to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title) {
		return openObjectDialog(activatorComponent, object, title, null);
	}

	/**
	 * Opens a dialog allowing to edit the given object. Note that the dialog is
	 * modal.
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @param object             Any object.
	 * @param title              The title of the dialog or null to have the default
	 *                           title.
	 * @param iconImage          The dialog icon image or null to have the default
	 *                           icon.
	 * @return the editor builder allowing to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			Image iconImage) {
		return openObjectDialog(activatorComponent, object, title, iconImage, true);
	}

	/**
	 * Opens a dialog allowing to edit the given object.
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @param object             Any object.
	 * @param title              The title of the dialog or null to have the default
	 *                           title.
	 * @param iconImage          The dialog icon image or null to have the default
	 *                           icon.
	 * @param cancellable        Whether the object state changes may be cancelled
	 *                           or not.
	 * @return the editor builder allowing to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			Image iconImage, boolean cancellable) {
		return openObjectDialog(activatorComponent, object, title, iconImage, cancellable, true);
	}

	/**
	 * Opens a dialog allowing to edit the given object.
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @param object             Any object.
	 * @param title              The title of the dialog or null to have the default
	 *                           title.
	 * @param iconImage          The dialog icon image or null to have the default
	 *                           icon.
	 * @param cancellable        Whether the object state changes may be cancelled
	 *                           or not.
	 * @param modal              Whether the dialog is modal or not.
	 * @return the editor builder allowing to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			final Image iconImage, final boolean cancellable, boolean modal) {
		StandardEditorBuilder editorBuilder = createEditorBuilder(activatorComponent, object, title, iconImage,
				cancellable);
		showDialog(editorBuilder.createDialog(), modal);
		return editorBuilder;
	}

	/**
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @param object             Any object.
	 * @param title              The title of the dialog or null to have the default
	 *                           title.
	 * @param iconImage          The dialog icon image or null to have the default
	 *                           icon.
	 * @param dialogCancellable  Whether the object state changes may be cancelled
	 *                           or not.
	 * @return an object allowing build a complete editor for the given object.
	 */
	public StandardEditorBuilder createEditorBuilder(Component activatorComponent, final Object object,
			final String title, final Image iconImage, final boolean dialogCancellable) {
		return new StandardEditorBuilder(this, activatorComponent, object) {

			@Override
			protected boolean isDialogCancellable() {
				return dialogCancellable;
			}

			@Override
			protected String getEditorWindowTitle() {
				if (title == null) {
					return super.getEditorWindowTitle();
				}
				return title;
			}

			@Override
			protected Image getCustomEditorWindowIconImage() {
				if (iconImage != null) {
					return iconImage;
				} else {
					return super.getCustomEditorWindowIconImage();
				}
			}

		};
	}

	/**
	 * Opens a frame allowing to edit the given object.
	 * 
	 * @param object    Any object.
	 * @param title     The title of the frame or null to have the default title.
	 * @param iconImage The frame icon image or null to have the default icon.
	 * @return the editor builder allowing to check the status of the frame.
	 */
	public StandardEditorBuilder openObjectFrame(Object object, String title, Image iconImage) {
		StandardEditorBuilder editorBuilder = createEditorBuilder(null, object, title, iconImage, false);
		showFrame(editorBuilder.createFrame());
		return editorBuilder;
	}

	/**
	 * Opens a frame allowing to edit the given object.
	 * 
	 * @param object Any object.
	 * @return the editor builder allowing to check the status of the frame.
	 */
	public StandardEditorBuilder openObjectFrame(Object object) {
		return openObjectFrame(object, null, null);
	}

	/**
	 * Displays a frame.
	 * 
	 * @param frame The frame to display.
	 */
	public void showFrame(JFrame frame) {
		frame.setVisible(true);
	}

	/**
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @return an object allowing build a dialog.
	 */
	public DialogBuilder createDialogBuilder(Component activatorComponent) {
		return new ApplicationDialogBuilder(activatorComponent);
	}

	/**
	 * Runs a task while displaying a busy indicator in a modal dialog. Note that
	 * the busy indicator is shown only if the task lasts long enough. <br>
	 * The following rules should be considered before using this method:
	 * <ul>
	 * <li>Invoke busy indication: we are in the UI thread and we are not updating
	 * the UI (not mandatory if the task duration is known and short).</li>
	 * <li>Invoke the UI thread: when we are not in the UI thread and we are
	 * updating the UI (maybe to allow user input).</li>
	 * <li>Run the task directly: otherwise.</li>
	 * <ul>
	 * 
	 * @param activatorComponent A component belonging to the parent window of the
	 *                           dialog or null.
	 * @param bakgroundTask      The task to execute.
	 * @param title              The title of the dialog or null to have the default
	 *                           title.
	 */
	public void showBusyDialogWhile(final Component activatorComponent, final Runnable bakgroundTask,
			final String title) {
		if (SystemProperties.isDebugModeActive()) {
			SwingRendererUtils.expectInUIThread();
		}
		if (Thread.currentThread().getName().equals(BUSY_DIALOG_CLOSER_NAME)) {
			throw new ReflectionUIError(
					"Illegal: must not call showBusyDialogWhile() from the busyDialogJobExecutorThread");
		}
		final Throwable[] exceptionThrown = new Throwable[1];
		final boolean[] reallyDone = new boolean[] { false };
		final BetterFutureTask<?> busyDialogJob = new BetterFutureTask<Boolean>(new Runnable() {
			@Override
			public void run() {
				try {
					bakgroundTask.run();
				} catch (Throwable t) {
					exceptionThrown[0] = t;
				} finally {
					reallyDone[0] = true;
				}
			}
		}, true);
		busyDialogJobExecutor.submit(busyDialogJob);
		try {
			busyDialogJob.get(1000, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e1) {
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
		if (!busyDialogJob.isDone()) {
			final DialogBuilder dialogBuilder = createDialogBuilder(activatorComponent);
			busyDialogCloser.submit(new Runnable() {
				@Override
				public void run() {
					while (true) {
						if (dialogBuilder.getCreatedDialog() != null) {
							if (dialogBuilder.getCreatedDialog().isVisible()) {
								if (reallyDone[0]) {
									break;
								}
							}
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new ReflectionUIError(e);
						}
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							dialogBuilder.getCreatedDialog().dispose();
						}
					});
				}
			});
			final JXBusyLabel busyLabel = new JXBusyLabel();
			{
				IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
				if (appInfo.getMainForegroundColor() != null) {
					busyLabel.setForeground(SwingRendererUtils.getColor(appInfo.getMainForegroundColor()));
				}
				if (appInfo.getLabelCustomFontResourcePath() != null) {
					busyLabel.setFont(SwingRendererUtils
							.loadFontThroughCache(appInfo.getLabelCustomFontResourcePath(),
									ReflectionUIUtils.getErrorLogListener(reflectionUI))
							.deriveFont(busyLabel.getFont().getStyle(), busyLabel.getFont().getSize()));
				}
				busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
				busyLabel.setText(prepareMessageToDisplay("Please wait..."));
				busyLabel.setVerticalTextPosition(SwingConstants.TOP);
				busyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				busyLabel.setBusy(true);
				dialogBuilder.setContentComponent(busyLabel);
			}
			dialogBuilder.setTitle(title);
			final JDialog dialog = dialogBuilder.createDialog();
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent event) {
					try {
						busyDialogJob.cancelRepeatedlyAndWait(100);
					} catch (InterruptedException e) {
						throw new ReflectionUIError(e);
					}
					dialog.removeWindowListener(this);
				}
			});
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			showDialog(dialog, true);
		}
		if (exceptionThrown[0] != null) {
			throw new ReflectionUIError(exceptionThrown[0]);
		}
	}

	/**
	 * Displays a dialog.
	 * 
	 * @param dialog The dialog to display.
	 * @param modal  Whether the dialog should be modal or not.
	 */
	public void showDialog(JDialog dialog, boolean modal) {
		if (modal) {
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		} else {
			dialog.setModalityType(ModalityType.MODELESS);
		}
		dialog.setVisible(true);
	}

	/**
	 * @param window
	 * @return an object that manages the appearance and behavior of a window
	 *         (dialog or frame).
	 */
	public WindowManager createWindowManager(Window window) {
		return new WindowManager(this, window);
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "SwingRenderer.DEFAULT";
		} else {
			return super.toString();
		}
	}

	protected class ApplicationDialogBuilder extends DialogBuilder {

		public ApplicationDialogBuilder(Component ownerComponent) {
			super(SwingRenderer.this, ownerComponent);
			this.iconImage = swingRenderer.getApplicationIconImage(reflectionUI.getApplicationInfo());
			if (reflectionUI.getApplicationInfo().getMainButtonBackgroundColor() != null) {
				setClosingButtonBackgroundColor(
						SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainButtonBackgroundColor()));
			}

			if (reflectionUI.getApplicationInfo().getMainButtonForegroundColor() != null) {
				setClosingButtonForegroundColor(
						SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainButtonForegroundColor()));
			}

			if (reflectionUI.getApplicationInfo().getMainButtonBorderColor() != null) {
				setClosingButtonBorderColor(
						SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainButtonBorderColor()));
			}

			if (reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath() != null) {
				setClosingButtonBackgroundImage(SwingRendererUtils.loadImageThroughCache(
						reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath(),
						ReflectionUIUtils.getErrorLogListener(reflectionUI)));
			}

			if (reflectionUI.getApplicationInfo().getButtonCustomFontResourcePath() != null) {
				setClosingButtonCustomFont(SwingRendererUtils.loadFontThroughCache(
						reflectionUI.getApplicationInfo().getButtonCustomFontResourcePath(),
						ReflectionUIUtils.getErrorLogListener(reflectionUI)));
			}
		}

	}

}
