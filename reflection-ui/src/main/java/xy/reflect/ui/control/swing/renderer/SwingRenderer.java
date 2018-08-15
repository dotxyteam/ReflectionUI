package xy.reflect.ui.control.swing.renderer;

import java.awt.Color;
import java.awt.Component;
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import xy.reflect.ui.control.swing.Form;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.WindowManager;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
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

public class SwingRenderer {

	public static final String CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY = SwingRenderer.class.getName()
			+ ".CUSTOMIZATIONS_FORBIDDEN";

	protected static SwingRenderer defaultInstance;

	protected ReflectionUI reflectionUI;
	protected Map<String, InvocationData> lastInvocationDataByMethodSignature = new HashMap<String, InvocationData>();
	protected Map<AbstractActionMenuItem, Form> formByMethodActionMenuItem = new MapMaker().weakKeys().makeMap();
	protected List<Form> allDisplayedForms = new ArrayList<Form>();

	protected ExecutorService busyDialogRunner = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r);
			result.setName("busyDialogRunner");
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

	public SwingRenderer(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "SwingRenderer.DEFAULT";
		} else {
			return super.toString();
		}
	}

	public static SwingRenderer getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new SwingRenderer(ReflectionUI.getDefault());
		}
		return defaultInstance;

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

	public Form createForm(Object object) {
		return createForm(object, IInfoFilter.DEFAULT);
	}

	public Form createForm(final Object object, IInfoFilter infoFilter) {
		return new Form(this, object, infoFilter);
	}

	public List<IFieldControlPlugin> getFieldControlPlugins() {
		List<IFieldControlPlugin> result = new ArrayList<IFieldControlPlugin>();
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

	public Image getMethodIconImage(IMethodControlData data) {
		ResourcePath imagePath = data.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return SwingRendererUtils.loadImageThroughcache(imagePath, ReflectionUIUtils.getErrorLogListener(reflectionUI));
	}

	public Image getEnumerationItemIconImage(IEnumerationItemInfo itemInfo) {
		ResourcePath imagePath = itemInfo.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return SwingRendererUtils.loadImageThroughcache(imagePath, ReflectionUIUtils.getErrorLogListener(reflectionUI));
	}

	public Image getMenuIconImage(AbstractActionMenuItem menuItem) {
		ResourcePath imagePath = menuItem.getIconImagePath();
		if (imagePath == null) {
			return null;
		}
		return SwingRendererUtils.loadImageThroughcache(imagePath, ReflectionUIUtils.getErrorLogListener(reflectionUI));
	}

	public void handleExceptionsFromDisplayedUI(Component activatorComponent, final Throwable t) {
		reflectionUI.logError(t);
		openErrorDialog(activatorComponent, "An Error Occured", null, t);
	}

	public Object onTypeInstanciationRequest(final Component activatorComponent, ITypeInfo type) {
		try {
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
							.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
					Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null, "Choose an option",
							"Create " + type.getCaption());
					if (resultEnumItem == null) {
						return null;
					}
					chosenConstructor = (IMethodInfo) enumFactory.unwrapInstance(resultEnumItem);
					if (chosenConstructor == null) {
						return null;
					}
				}
				final ITypeInfo finalType = type;
				MethodAction methodAction = createMethodAction(new IMethodControlInput() {

					ModificationStack dummyModificationStack = new ModificationStack(null);

					@Override
					public IInfo getModificationsTarget() {
						return chosenConstructor;
					}

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
						return new DefaultMethodControlData(null, chosenConstructor);
					}
				});
				methodAction.setShouldDisplayReturnValueIfAny(false);
				methodAction.execute(activatorComponent);
				return methodAction.getReturnValue();
			} else {
				if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {

					List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
					if (polyTypes.size() == 1) {
						return

						onTypeInstanciationRequest(activatorComponent, polyTypes.get(0));
					} else

					{
						final PolymorphicTypeOptionsFactory enumFactory = new PolymorphicTypeOptionsFactory(
								reflectionUI, type);
						IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
								.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
						Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null,
								"Choose a type:", "New '" + type.getCaption() + "'");
						if (resultEnumItem == null) {
							return null;
						}
						return onTypeInstanciationRequest(activatorComponent,
								(ITypeInfo) enumFactory.unwrapInstance(resultEnumItem));
					}
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
								.getTypeInfo(new JavaTypeInfoSource(ClassUtils.getCachedClassforName(className)));
					} catch (ClassNotFoundException e) {
						throw new ReflectionUIError(e);
					}
					if (type == null) {
						return null;
					} else {
						return onTypeInstanciationRequest(activatorComponent, type);
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
	public <T> T openInputDialog(Component parentComponent, T initialValue, String valueCaption, String title) {
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

		if (!openObjectDialog(parentComponent, encapsulatedValue, title, getObjectIconImage(encapsulatedValue), true,
				true).isCancelled()) {
			return (T) valueHolder[0];
		} else {
			return null;
		}
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title) {
		return openQuestionDialog(activatorComponent, question, title, "Yes", "No");
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title, String yesCaption,
			String noCaption) {
		DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
		dialogBuilder.setToolbarComponents(dialogBuilder.createStandardOKCancelDialogButtons(yesCaption, noCaption));
		dialogBuilder.setContentComponent(SwingRendererUtils.getMessageJOptionPane(prepareStringToDisplay(question),
				JOptionPane.QUESTION_MESSAGE));
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
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getMessageJOptionPane(prepareStringToDisplay(msg), JOptionPane.INFORMATION_MESSAGE));
		dialogBuilder.setToolbarComponents(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	public void openErrorDialog(Component activatorComponent, String title, Image iconImage, final Throwable error) {
		DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);

		List<Component> buttons = new ArrayList<Component>();
		final JButton deatilsButton = new JButton(prepareStringToDisplay("Details"));
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
		dialogBuilder.setContentComponent(SwingRendererUtils.getMessageJOptionPane(
				prepareStringToDisplay(ReflectionUIUtils.getPrettyErrorMessage(error)), JOptionPane.ERROR_MESSAGE));
		dialogBuilder.setToolbarComponents(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		String statckTraceString = ReflectionUIUtils.getPrintedStackTrace(error);
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI,
				reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(statckTraceString)), "Error Details", "");
		encapsulation.setFieldGetOnly(true);
		encapsulation.setFieldNullValueDistinct(false);
		Object encapsulatedValue = encapsulation.getInstance(Accessor.<Object>returning(statckTraceString));
		openObjectDialog(activatorComponent, encapsulatedValue);
	}

	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object) {
		return openObjectDialog(activatorComponent, object, null, null, false, true);
	}

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
			public Image getObjectIconImage() {
				if (iconImage == null) {
					return super.getObjectIconImage();
				}
				return iconImage;
			}

		};
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		StandardEditorBuilder editorBuilder = getEditorBuilder(null, object, title, iconImage, false);
		showFrame(editorBuilder.createFrame());

	}

	public void showFrame(JFrame frame) {
		frame.setVisible(true);
	}

	public void openObjectFrame(Object object) {
		openObjectFrame(object, null, null);
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
				.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
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
		final Future<?> busyDialogRunnerJob = busyDialogRunner.submit(new Runnable() {
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
			busyDialogRunnerJob.get(1000, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e1) {
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
		if (!busyDialogRunnerJob.isDone()) {
			final DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
			busyDialogCloser.submit(new Runnable() {
				@Override
				public void run() {
					while ((dialogBuilder.getCreatedDialog() == null) || (!dialogBuilder.getCreatedDialog().isVisible())
							|| (!busyDialogRunnerJob.isDone())) {
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
			busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
			busyLabel.setText("Please wait...");
			busyLabel.setVerticalTextPosition(SwingConstants.TOP);
			busyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			busyLabel.setBusy(true);
			dialogBuilder.setContentComponent(busyLabel);
			dialogBuilder.setTitle(title);
			final JDialog dialog = dialogBuilder.createDialog();
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					busyDialogRunnerJob.cancel(true);
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

	public Color getNullColor() {
		return SwingRendererUtils.getNonEditableTextBackgroundColor();
	}

	public long getDataUpdateDelayMilliseconds() {
		return 500;
	}

	public WindowManager createWindowManager(Window window) {
		return new WindowManager(this, window);
	}

}