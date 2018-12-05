/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.control.swing.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Dialog.ModalityType;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXBusyLabel;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodContext;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.plugin.ColorPickerPlugin;
import xy.reflect.ui.control.swing.plugin.CustomCheckBoxPlugin;
import xy.reflect.ui.control.swing.plugin.DetailedListControlPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.HtmlPlugin;
import xy.reflect.ui.control.swing.plugin.ImageViewPlugin;
import xy.reflect.ui.control.swing.plugin.OptionButtonsPlugin;
import xy.reflect.ui.control.swing.plugin.PasswordFieldPlugin;
import xy.reflect.ui.control.swing.plugin.SliderPlugin;
import xy.reflect.ui.control.swing.plugin.SpinnerPlugin;
import xy.reflect.ui.control.swing.plugin.StyledTextPlugin;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractActionMenuItem;
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
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.component.AbstractControlButton;

/**
 * This is the {@link ReflectionUI} renderer class for Swing-based UIs.
 * 
 * @author olitank
 *
 */
public class SwingRenderer {

	public static void main(String[] args) throws Exception {
		Class<?> clazz = Object.class;
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instanciate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + SystemProperties.describe();
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
		Object object = SwingRenderer.getDefault().onTypeInstanciationRequest(null,
				SwingRenderer.getDefault().getReflectionUI().getTypeInfo(new JavaTypeInfoSource(clazz, null)), null);
		if (object == null) {
			return;
		}
		SwingRenderer.getDefault().openObjectFrame(object);
	}

	public static final String CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY = SwingRenderer.class.getName()
			+ ".CUSTOMIZATIONS_FORBIDDEN";

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
	protected Map<AbstractActionMenuItem, Form> formByMethodActionMenuItem = new MapMaker().weakKeys().makeMap();
	protected List<Form> allDisplayedForms = new ArrayList<Form>();

	protected ExecutorService busyDialogJobExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r);
			result.setName("busyDialogJobExecutor");
			result.setDaemon(true);
			return result;
		}
	});
	protected ExecutorService busyDialogCloser = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r);
			result.setName("busyDialogCloser");
			result.setDaemon(true);
			return result;
		}
	});
	
	/**
	 * Constructs an instance that will render abstract UI models generated by the
	 * given {@link ReflectionUI} object.
	 * 
	 * @param reflectionUI
	 *            The abstract UI model generator.
	 */
	public SwingRenderer(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	public ReflectionUI getReflectionUI() {
		return reflectionUI;
	}

	public List<Form> getAllDisplayedForms() {
		return allDisplayedForms;
	}

	public Map<AbstractActionMenuItem, Form> getFormByActionMenuItem() {
		return formByMethodActionMenuItem;
	}

	public Map<String, InvocationData> getLastInvocationDataByMethodSignature() {
		return lastInvocationDataByMethodSignature;
	}

	public String prepareStringToDisplay(String string) {
		return string;
	}

	public String getObjectTitle(Object object) {
		if (object == null) {
			return "<Missing Value>";
		}
		return reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object)).getCaption();
	}

	public MethodAction createMethodAction(IMethodControlInput input) {
		return new MethodAction(this, input);
	}

	/**
	 * @param object
	 *            Any object.
	 * @return a form allowing to edit the given object.
	 */
	public final Form createForm(Object object) {
		return createForm(object, IInfoFilter.DEFAULT);
	}

	/**
	 * @param object
	 *            Any object.
	 * @param infoFilter
	 *            An object allowing to filter out some fields and methods.
	 * @return a form allowing to edit the given object.
	 */
	public Form createForm(final Object object, IInfoFilter infoFilter) {
		return new Form(this, object, infoFilter);
	}

	public List<IFieldControlPlugin> getFieldControlPlugins() {
		List<IFieldControlPlugin> result = new ArrayList<IFieldControlPlugin>();
		result.add(new OptionButtonsPlugin());
		result.add(new SliderPlugin());
		result.add(new SpinnerPlugin());
		result.add(new FileBrowserPlugin());
		result.add(new ColorPickerPlugin());
		result.add(new ImageViewPlugin());
		result.add(new CustomCheckBoxPlugin());
		result.add(new DetailedListControlPlugin());
		result.add(new StyledTextPlugin());
		result.add(new PasswordFieldPlugin());
		result.add(new HtmlPlugin());
		return result;
	}

	public InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1);
	}

	public Image getObjectIconImage(Object object) {
		if (object != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
			ResourcePath imagePath = type.getIconImagePath();
			if (imagePath == null) {
				return null;
			}
			Image result = SwingRendererUtils.loadImageThroughcache(imagePath,
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public ImageIcon getObjectIcon(Object object) {
		return SwingRendererUtils.getIcon(getObjectIconImage(object));
	}

	public ImageIcon getMethodIcon(IMethodControlData data) {
		ResourcePath imagePath = data.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return SwingRendererUtils.getIcon(SwingRendererUtils.loadImageThroughcache(imagePath,
				ReflectionUIUtils.getErrorLogListener(reflectionUI)));
	}

	public ImageIcon getEnumerationItemIcon(IEnumerationItemInfo itemInfo) {
		ResourcePath imagePath = itemInfo.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return SwingRendererUtils.getIcon(SwingRendererUtils.loadImageThroughcache(imagePath,
				ReflectionUIUtils.getErrorLogListener(reflectionUI)));
	}

	public ImageIcon getMenuItemIcon(AbstractActionMenuItem menuItem) {
		ResourcePath imagePath = menuItem.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return SwingRendererUtils.getIcon(SwingRendererUtils.loadImageThroughcache(imagePath,
				ReflectionUIUtils.getErrorLogListener(reflectionUI)));
	}

	public void handleExceptionsFromDisplayedUI(Component activatorComponent, final Throwable t) {
		reflectionUI.logError(t);
		openErrorDialog(activatorComponent, "An Error Occured", null, t);
	}

	/**
	 * Allows to create an instance of the specified type. Dialogs may be displayed
	 * to allow to select the constructor or provide parameter values. If sub-types
	 * of the given type are know, then a type selection dialog will be displayed.
	 * 
	 * @param activatorComponent
	 *            A component belonging to the parent window of the eventual dialogs
	 *            or null.
	 * @param type
	 *            An abstract UI type information object.
	 * @param parentObject
	 *            The parent object of the new instance or null if none exists.
	 * @return an object created using the given type information.
	 */
	public Object onTypeInstanciationRequest(final Component activatorComponent, ITypeInfo type,
			final Object parentObject) {
		try {
			if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {

				final PolymorphicTypeOptionsFactory enumFactory = new PolymorphicTypeOptionsFactory(reflectionUI, type);

				List<ITypeInfo> polyTypes = enumFactory.getTypeOptions();
				if (polyTypes.size() == 1) {
					return onTypeInstanciationRequest(activatorComponent, polyTypes.get(0), parentObject);
				} else {
					IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
							.getTypeInfo(enumFactory.getInstanceTypeInfoSource(null));
					Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null, "Choose a type:",
							"New '" + type.getCaption() + "'");
					if (resultEnumItem == null) {
						return null;
					}
					return onTypeInstanciationRequest(activatorComponent,
							(ITypeInfo) enumFactory.unwrapInstance(resultEnumItem), parentObject);
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
						chosenConstructor = (IMethodInfo) enumFactory.unwrapInstance(resultEnumItem);
						if (chosenConstructor == null) {
							return null;
						}
					}
					final ITypeInfo finalType = type;
					MethodAction ctorAction = createMethodAction(new IMethodControlInput() {

						ModificationStack dummyModificationStack = new ModificationStack(null);

						@Override
						public ModificationStack getModificationStack() {
							return dummyModificationStack;
						}

						@Override
						public IContext getContext() {
							return new MethodContext(finalType, chosenConstructor);
						}

						@Override
						public IMethodControlData getControlData() {
							return new DefaultMethodControlData(reflectionUI, parentObject, chosenConstructor);
						}
					});
					ctorAction.setShouldDisplayReturnValueIfAny(false);
					ctorAction.onInvocationRequest(activatorComponent);
					return ctorAction.getReturnValue();
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
								.getTypeInfo(new JavaTypeInfoSource(ClassUtils.getCachedClassforName(className), null));
					} catch (ClassNotFoundException e) {
						throw new ReflectionUIError(e);
					}
					if (type == null) {
						return null;
					} else {
						return onTypeInstanciationRequest(activatorComponent, type, parentObject);
					}
				}
			}
		} catch (Throwable t) {
			throw new ReflectionUIError(
					"Could not create an instance of type '" + type.getName() + "': " + t.toString(), t);
		}
	}

	public Object openSelectionDialog(Component parentComponent, IEnumerationTypeInfo enumType, Object initialEnumItem,
			String message, String title) {
		if (initialEnumItem == null) {
			initialEnumItem = enumType.getPossibleValues()[0];
		}
		final Object[] chosenItemHolder = new Object[] { initialEnumItem };

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, enumType, "Selection",
				message);
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

	@SuppressWarnings("unchecked")
	public <T> T openInputDialog(Component activatorComponent, T initialValue, String valueCaption, String title) {
		if (initialValue == null) {
			throw new ReflectionUIError();
		}
		final Object[] valueHolder = new Object[] { initialValue };
		ITypeInfo initialValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(initialValue));

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, initialValueType, "Input",
				valueCaption);
		encapsulation.setFieldGetOnly(false);
		encapsulation.setFieldNullValueDistinct(false);
		Object encapsulatedValue = encapsulation.getInstance(valueHolder);

		StandardEditorBuilder editorBuilder = getEditorBuilder(activatorComponent, encapsulatedValue, title,
				getObjectIconImage(encapsulatedValue), true);
		final DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
		dialogBuilder.setToolbarComponentsAccessor(new Accessor<List<Component>>() {
			@Override
			public List<Component> get() {
				return new ArrayList<Component>(dialogBuilder.createStandardOKCancelDialogButtons(null, null));
			}
		});
		dialogBuilder.setContentComponent(editorBuilder.createForm(false, false));
		dialogBuilder.setTitle(title);
		showDialog(dialogBuilder.createDialog(), true);
		if (dialogBuilder.wasOkPressed()) {
			return (T) valueHolder[0];
		} else {
			return null;
		}
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title) {
		return openQuestionDialog(activatorComponent, question, title, "Yes", "No");
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title,
			final String yesCaption, final String noCaption) {
		final DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
		dialogBuilder.setToolbarComponentsAccessor(new Accessor<List<Component>>() {
			@Override
			public List<Component> get() {
				return new ArrayList<Component>(
						dialogBuilder.createStandardOKCancelDialogButtons(yesCaption, noCaption));
			}
		});
		dialogBuilder
				.setContentComponent(SwingRendererUtils.getMessagePane(question, JOptionPane.QUESTION_MESSAGE, this));
		dialogBuilder.setTitle(title);
		showDialog(dialogBuilder.createDialog(), true);
		return dialogBuilder.wasOkPressed();
	}

	public void openInformationDialog(Component activatorComponent, String msg, String title, Image iconImage) {
		DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);

		List<Component> buttons = new ArrayList<Component>();
		buttons.add(dialogBuilder.createDialogClosingButton("Close", null));

		dialogBuilder.setTitle(title);
		dialogBuilder.setIconImage(iconImage);
		dialogBuilder
				.setContentComponent(SwingRendererUtils.getMessagePane(msg, JOptionPane.INFORMATION_MESSAGE, this));
		dialogBuilder.setToolbarComponentsAccessor(Accessor.returning(buttons));

		showDialog(dialogBuilder.createDialog(), true);
	}

	public void openErrorDialog(Component activatorComponent, String title, Image iconImage, final Throwable error) {
		DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);

		List<Component> buttons = new ArrayList<Component>();
		@SuppressWarnings("serial")
		final JButton deatilsButton = new AbstractControlButton() {

			@Override
			public String retrieveCaption() {
				return "Details";
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return SwingRenderer.this;
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
		dialogBuilder.setIconImage(iconImage);
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getMessagePane(formatErrorMessage(error) + "\n", JOptionPane.ERROR_MESSAGE, this));
		dialogBuilder.setToolbarComponentsAccessor(Accessor.returning(buttons));

		showDialog(dialogBuilder.createDialog(), true);
	}

	public String formatErrorMessage(Throwable error) {
		return ReflectionUIUtils.getPrettyErrorMessage(error);
	}

	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		String statckTraceString = ReflectionUIUtils.getPrintedStackTrace(error);
		final DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
		dialogBuilder.setToolbarComponentsAccessor(new Accessor<List<Component>>() {
			@Override
			public List<Component> get() {
				return Collections.<Component>singletonList(dialogBuilder.createDialogClosingButton("Close", null));
			}
		});
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getMessagePane(statckTraceString, JOptionPane.INFORMATION_MESSAGE, this));
		dialogBuilder.setTitle("Error Details");
		showDialog(dialogBuilder.createDialog(), true);
	}

	/**
	 * Opens a dialog allowing to edit the given object.
	 * 
	 * @param activatorComponent
	 *            A component belonging to the parent window of the dialog or null.
	 * @param object
	 *            Any object.
	 * @return the dialog builder allowing thus to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object) {
		return openObjectDialog(activatorComponent, object, null);
	}

	/**
	 * Opens a dialog allowing to edit the given object.
	 * 
	 * @param activatorComponent
	 *            A component belonging to the parent window of the dialog or null.
	 * @param object
	 *            Any object.
	 * @param title
	 *            The title of the dialog or null to have the default title.
	 * @return the dialog builder allowing thus to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title) {
		return openObjectDialog(activatorComponent, object, title, null);
	}

	/**
	 * Opens a dialog allowing to edit the given object.
	 * 
	 * @param activatorComponent
	 *            A component belonging to the parent window of the dialog or null.
	 * @param object
	 *            Any object.
	 * @param title
	 *            The title of the dialog or null to have the default title.
	 * @param iconImage
	 *            The dialog icon image or null to have the default icon.
	 * @return the dialog builder allowing thus to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			Image iconImage) {
		return openObjectDialog(activatorComponent, object, title, iconImage, true);
	}

	/**
	 * Opens a dialog allowing to edit the given object.
	 * 
	 * @param activatorComponent
	 *            A component belonging to the parent window of the dialog or null.
	 * @param object
	 *            Any object.
	 * @param title
	 *            The title of the dialog or null to have the default title.
	 * @param iconImage
	 *            The dialog icon image or null to have the default icon.
	 * @param cancellable
	 *            Whether the object state changes can be cancelled or not.
	 * @return the dialog builder allowing thus to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			Image iconImage, boolean cancellable) {
		return openObjectDialog(activatorComponent, object, title, iconImage, cancellable, true);
	}

	/**
	 * Opens a dialog allowing to edit the given object.
	 * 
	 * @param activatorComponent
	 *            A component belonging to the parent window of the dialog or null.
	 * @param object
	 *            Any object.
	 * @param title
	 *            The title of the dialog or null to have the default title.
	 * @param iconImage
	 *            The dialog icon image or null to have the default icon.
	 * @param cancellable
	 *            Whether the object state changes can be cancelled or not.
	 * @param modal
	 *            Whether the dialog is modal or not.
	 * @return the dialog builder allowing thus to check the status of the dialog.
	 */
	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			final Image iconImage, final boolean cancellable, boolean modal) {
		StandardEditorBuilder editorBuilder = getEditorBuilder(activatorComponent, object, title, iconImage,
				cancellable);
		showDialog(editorBuilder.createDialog(), modal);
		return editorBuilder;
	}

	public StandardEditorBuilder getEditorBuilder(Component activatorComponent, final Object object, final String title,
			final Image iconImage, final boolean cancellable) {
		return new StandardEditorBuilder(this, activatorComponent, object) {

			@Override
			protected DialogBuilder createDelegateDialogBuilder() {
				return getDialogBuilder(ownerComponent);
			}

			@Override
			public boolean isCancellable() {
				return cancellable;
			}

			@Override
			public String getEditorWindowTitle() {
				if (title == null) {
					return super.getEditorWindowTitle();
				}
				return title;
			}

			@Override
			public Image getEditorWindowIconImage() {
				if (iconImage != null) {
					return iconImage;
				} else {
					return super.getEditorWindowIconImage();
				}
			}

		};
	}

	/**
	 * Opens a frame allowing to edit the given object.
	 * 
	 * @param object
	 *            Any object.
	 * @param title
	 *            The title of the frame or null to have the default title.
	 * @param iconImage
	 *            The frame icon image or null to have the default icon.
	 */
	public void openObjectFrame(Object object, String title, Image iconImage) {
		StandardEditorBuilder editorBuilder = getEditorBuilder(null, object, title, iconImage, false);
		showFrame(editorBuilder.createFrame());

	}

	/**
	 * Opens a frame allowing to edit the given object.
	 * 
	 * @param object
	 *            Any object.
	 */
	public void openObjectFrame(Object object) {
		openObjectFrame(object, null, null);
	}

	public void showFrame(JFrame frame) {
		frame.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent, final List<T> choices, T initialSelection,
			String message, String title) {
		if (choices.size() == 0) {
			throw new ReflectionUIError();
		}
		final GenericEnumerationFactory enumFactory = new GenericEnumerationFactory(reflectionUI, choices.toArray(),
				"SelectionDialogArrayAsEnumeration [title=" + title + "]", "") {

			Map<Object, String> captions = new HashMap<Object, String>();
			Map<Object, Image> iconImages = new HashMap<Object, Image>();

			{
				for (Object choice : choices) {
					captions.put(choice, ReflectionUIUtils.toString(SwingRenderer.this.reflectionUI, choice));
					iconImages.put(choice, getObjectIconImage(choice));
				}
			}

			@Override
			protected ResourcePath getItemIconImagePath(Object choice) {
				Image image = iconImages.get(choice);
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
				enumFactory.getInstance(initialSelection), message, title);
		if (resultEnumItem == null) {
			return null;
		}
		T result = (T) enumFactory.unwrapInstance(resultEnumItem);
		return result;

	}

	public DialogBuilder getDialogBuilder(Component activatorComponent) {
		return new DialogBuilder(this, activatorComponent);
	}

	public void showBusyDialogWhile(final Component activatorComponent, final Runnable runnable, final String title) {
		final Throwable[] exceptionThrown = new Throwable[1];
		final Future<?> busyDialogJob = busyDialogJobExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable t) {
					exceptionThrown[0] = t;
				}
			}
		});
		try {
			busyDialogJob.get(1000, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e1) {
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
		if (!busyDialogJob.isDone()) {
			final DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
			busyDialogCloser.submit(new Runnable() {
				@Override
				public void run() {
					while ((dialogBuilder.getCreatedDialog() == null) || (!dialogBuilder.getCreatedDialog().isVisible())
							|| (!busyDialogJob.isDone())) {
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
				busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
				busyLabel.setText("Please wait...");
				busyLabel.setVerticalTextPosition(SwingConstants.TOP);
				busyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				busyLabel.setBusy(true);
				dialogBuilder.setContentComponent(busyLabel);
			}
			dialogBuilder.setTitle(title);
			final JDialog dialog = dialogBuilder.createDialog();
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					busyDialogJob.cancel(true);
				}
			});
			showDialog(dialog, true, false);
		}
		if (exceptionThrown[0] != null) {
			throw new ReflectionUIError(exceptionThrown[0]);
		}
	}

	public void showDialog(JDialog dialog, boolean modal) {
		showDialog(dialog, modal, true);
	}

	public void showDialog(JDialog dialog, boolean modal, boolean closeable) {
		if (modal) {
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			if (closeable) {
				dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			} else {
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			}
			dialog.setVisible(true);
			dialog.dispose();
		} else {
			dialog.setModalityType(ModalityType.MODELESS);
			if (closeable) {
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			} else {
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			}
			dialog.setVisible(true);
		}

	}

	public WindowManager createWindowManager(Window window) {
		return new WindowManager(this, window);
	}

	public BufferedImage addImageActivationEffect(Image image) {
		BufferedImage result = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		float scalefactor = 0.5f;
		float offset = 64f;
		return new RescaleOp(new float[] { scalefactor, scalefactor, scalefactor, 1f },
				new float[] { offset, offset, offset, 0f }, null).filter(result, null);
	}

	public Color addColorActivationEffect(Color color) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		if (hsb[2] > 0.5f) {
			hsb[2] -= 0.2f;
		} else {
			hsb[2] += 0.2f;
		}
		int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		return new Color(rgb);
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "SwingRenderer.DEFAULT";
		} else {
			return super.toString();
		}
	}

}
