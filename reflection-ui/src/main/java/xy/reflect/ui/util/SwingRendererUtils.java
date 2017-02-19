package xy.reflect.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.data.FieldControlData;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.MethodControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;

@SuppressWarnings("unused")
public class SwingRendererUtils {

	public static final Image NULL_ICON_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	public static final ImageIcon ERROR_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/error.png"));
	public static final ImageIcon HELP_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/help.png"));
	public static final ImageIcon DETAILS_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/details.png"));
	public static final ImageIcon ADD_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/add.png"));
	public static final ImageIcon REMOVE_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/remove.png"));
	public static final ImageIcon UP_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/up.png"));
	public static final ImageIcon DOWN_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/down.png"));
	public static final ImageIcon CUSTOMIZATION_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/custom.png"));
	public static final ImageIcon SAVE_ALL_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/save-all.png"));
	public static Map<String, Image> IMAGE_CACHE = new HashMap<String, Image>();

	public static Icon getSmallIcon(Image image) {
		image = SwingRendererUtils.scalePreservingRatio(image, 16, 16, Image.SCALE_SMOOTH);
		return new ImageIcon(image);
	}

	public static Image scalePreservingRatio(Image image, int newWidth, int newHeight, int scaleQuality) {
		Dimension imageSize = new Dimension(image.getWidth(null), image.getHeight(null));
		Dimension boxSize = new Dimension(newWidth, newHeight);
		Rectangle imageBoundsInBox = new Rectangle();
		{
			float resizeRatioBasedOnWidth = (float) boxSize.width / (float) imageSize.width;
			float resizeRatioBasedOnHeight = (float) boxSize.height / (float) imageSize.height;
			float resizeRatio = Math.min(resizeRatioBasedOnWidth, resizeRatioBasedOnHeight);
			imageBoundsInBox.width = Math.round(imageSize.width * resizeRatio);
			imageBoundsInBox.height = Math.round(imageSize.height * resizeRatio);
			imageBoundsInBox.x = Math.round((boxSize.width - imageBoundsInBox.width) / 2f);
			imageBoundsInBox.y = Math.round((boxSize.height - imageBoundsInBox.height) / 2f);
		}
		BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, imageBoundsInBox.x, imageBoundsInBox.y, imageBoundsInBox.x + imageBoundsInBox.width,
				imageBoundsInBox.y + imageBoundsInBox.height, 0, 0, image.getWidth(null), image.getHeight(null), null);
		g.dispose();
		return result;
	}

	public static void showTooltipNow(Component c) {
		try {
			Method showToolTipMehod = ToolTipManager.class.getDeclaredMethod("show",
					new Class<?>[] { JComponent.class });
			showToolTipMehod.setAccessible(true);
			showToolTipMehod.invoke(ToolTipManager.sharedInstance(), new Object[] { c });
		} catch (Throwable e1) {
			try {
				KeyEvent ke = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), InputEvent.CTRL_MASK,
						KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED);
				c.dispatchEvent(ke);
			} catch (Throwable e2) {
				throw new ReflectionUIError(
						"Failed to show tooltip programmatically: \n1st failure: " + e1 + "2nd failure: \n" + e2);
			}
		}
	}

	public static void setRecursivelyEnabled(Component c, boolean b) {
		c.setEnabled(b);
		if (c instanceof Container) {
			for (Component child : ((Container) c).getComponents()) {
				setRecursivelyEnabled(child, b);
			}
		}
	}

	public static Window getWindowAncestorOrSelf(Component c) {
		if (c instanceof Window) {
			return (Window) c;
		}
		if (c == null) {
			return null;
		}
		return SwingUtilities.getWindowAncestor(c);
	}

	public static Component flowInLayout(Component c, int flowLayoutAlignment) {
		JPanel result = new JPanel();
		result.setLayout(new FlowLayout(flowLayoutAlignment));
		result.add(c);
		return result;
	}

	public static JPanel findParentForm(Component component, SwingRenderer swingRenderer) {
		Component candidateForm = component.getParent();
		while (candidateForm != null) {
			if (swingRenderer.getObjectByForm().keySet().contains(candidateForm)) {
				return (JPanel) candidateForm;
			}
			candidateForm = candidateForm.getParent();
		}
		return null;
	}

	public static List<JPanel> findDescendantForms(Container container, SwingRenderer swingRenderer) {
		List<JPanel> result = new ArrayList<JPanel>();
		for (Component childComponent : container.getComponents()) {
			if (swingRenderer.getObjectByForm().keySet().contains(childComponent)) {
				result.add((JPanel) childComponent);
			} else {
				if (childComponent instanceof Container) {
					result.addAll(findDescendantForms((Container) childComponent, swingRenderer));
				}
			}
		}
		return result;
	}

	public static int getStandardCharacterWidth(Component c) {
		return c.getFontMetrics(c.getFont()).charWidth('a');
	}

	public static Color fixSeveralColorRenderingIssues(Color color) {
		return new Color(color.getRGB());
	}

	public static void setMultilineToolTipText(JComponent c, String toolTipText) {
		if (toolTipText == null) {
			c.setToolTipText(null);
		} else {
			c.setToolTipText("<HTML>" + ReflectionUIUtils.escapeHTML(toolTipText, true) + "</HTML>");
		}
	}

	public static Color getTextBackgroundColor() {
		return SwingRendererUtils.fixSeveralColorRenderingIssues(new JTextField().getBackground());
	}

	public static void disableComponentTree(JComponent c, final boolean revert) {
		String CONTAINER_LISTENER_KEY = ReflectionUIUtils.class.getName()
				+ ".disableComponentTree.CONTAINER_LISTENER_KEY";
		String LAST_STATE_KEY = ReflectionUIUtils.class.getName() + ".disableComponentTree.LAST_STATE_KEY";
		Boolean lastState = (Boolean) c.getClientProperty(LAST_STATE_KEY);
		if (revert) {
			if (lastState == null) {
				return;
			}
			if (lastState) {
				c.setEnabled(true);
			}
			c.putClientProperty(LAST_STATE_KEY, null);
			ContainerListener containerListener = (ContainerListener) c.getClientProperty(CONTAINER_LISTENER_KEY);
			c.removeContainerListener(containerListener);
		} else {
			if (lastState != null) {
				return;
			}
			c.putClientProperty(LAST_STATE_KEY, c.isEnabled());
			c.setEnabled(false);
			ContainerListener containerListener = new ContainerListener() {

				@Override
				public void componentRemoved(ContainerEvent e) {
				}

				@Override
				public void componentAdded(ContainerEvent e) {
					Component child = e.getChild();
					if (!(child instanceof JComponent)) {
						return;
					}
					disableComponentTree((JComponent) child, revert);
				}
			};
			c.addContainerListener(containerListener);
			c.putClientProperty(CONTAINER_LISTENER_KEY, containerListener);
		}
		for (Component child : c.getComponents()) {
			if (!(child instanceof JComponent)) {
				continue;
			}
			disableComponentTree((JComponent) child, revert);
		}
	}

	public static String getWindowTitle(Window window) {
		if (window instanceof JFrame) {
			return ((JFrame) window).getTitle();
		} else if (window instanceof JDialog) {
			return ((JDialog) window).getTitle();
		} else {
			return null;
		}
	}

	public static Container getContentPane(Window window) {
		if (window instanceof JFrame) {
			return ((JFrame) window).getContentPane();
		} else if (window instanceof JDialog) {
			return ((JDialog) window).getContentPane();
		} else {
			return null;
		}
	}

	public static void setContentPane(Window window, Container contentPane) {
		if (window instanceof JFrame) {
			((JFrame) window).setContentPane(contentPane);
		} else if (window instanceof JDialog) {
			((JDialog) window).setContentPane(contentPane);
		} else {
			throw new ReflectionUIError();
		}
	}

	public static boolean hasOrContainsFocus(Component c) {
		if (c.hasFocus()) {
			return true;
		}
		if (c instanceof Container) {
			Container container = (Container) c;
			for (Component childComponent : container.getComponents()) {
				if (hasOrContainsFocus(childComponent)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Icon getHelpIcon() {
		return new ImageIcon(ReflectionUI.class.getResource("resource/help.png"));
	}

	public static List<Object> getActiveInstances(ITypeInfo type, SwingRenderer swingRenderer) {
		List<Object> result = new ArrayList<Object>();
		for (Map.Entry<JPanel, Object> entry : swingRenderer.getObjectByForm().entrySet()) {
			Object object = entry.getValue();
			ITypeInfo objectType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
			if (objectType.equals(type)) {
				result.add(object);
			}
		}
		return result;
	}

	public static boolean isFormEmpty(ITypeInfo type, IInfoFilter infoFilter, SwingRenderer swingRenderer) {
		List<IFieldInfo> fields = type.getFields();
		List<IMethodInfo> methods = type.getMethods();

		fields = new ArrayList<IFieldInfo>(fields);
		for (Iterator<IFieldInfo> it = fields.iterator(); it.hasNext();) {
			IFieldInfo field = it.next();
			if (infoFilter.excludeField(field)) {
				it.remove();
			}
		}

		methods = new ArrayList<IMethodInfo>(methods);
		for (Iterator<IMethodInfo> it = methods.iterator(); it.hasNext();) {
			IMethodInfo method = it.next();
			if (infoFilter.excludeMethod(method)) {
				it.remove();
			}
		}

		return (fields.size() + methods.size()) == 0;
	}

	public static boolean isObjectDisplayEmpty(Object value, IInfoFilter infoFilter, SwingRenderer swingRenderer) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
		if (SwingRendererUtils.hasCustomControl(value, valueType, swingRenderer)) {
			return false;
		}
		if (!isFormEmpty(valueType, infoFilter, swingRenderer)) {
			return false;
		}
		return true;
	}

	public static final boolean hasCustomControl(Object fieldValue, ITypeInfo fieldType, SwingRenderer swingRenderer) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, fieldType);
		Object encapsulatedValue = encapsulation.getInstance(new Object[] { fieldValue });
		ITypeInfo valueAsFieldType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(encapsulatedValue));
		IFieldInfo field = valueAsFieldType.getFields().get(0);
		return swingRenderer.hasCustomFieldControl(encapsulatedValue, field);
	}

	public static boolean isForm(Component c, SwingRenderer swingRenderer) {
		return swingRenderer.getObjectByForm().keySet().contains(c);
	}

	public static void forwardSubModifications(final ReflectionUI reflectionUI, final JPanel subForm,
			final Accessor<Boolean> childModifAcceptedGetter,
			final Accessor<ValueReturnMode> childValueReturnModeGetter, final Accessor<Boolean> childValueNewGetter,
			final Accessor<IModification> commitModifGetter, final Accessor<IInfo> childModifTargetGetter,
			final Accessor<String> parentModifTitleGetter, final Accessor<ModificationStack> parentModifStackGetter,
			final SwingRenderer swingRenderer) {
		final ModificationStack subFormModifStack = swingRenderer.getModificationStackByForm().get(subForm);
		swingRenderer.getModificationStackByForm().put(subForm,
				new ModificationStack("Forward Sub-Modifications To " + subForm.toString()) {

					@Override
					public boolean pushUndo(IModification undoModif) {
						subFormModifStack.pushUndo(undoModif);
						ModificationStack childModifStack = new ModificationStack(null);
						childModifStack.pushUndo(undoModif);
						Boolean childModifAccepted = childModifAcceptedGetter.get();
						ValueReturnMode childValueReturnMode = childValueReturnModeGetter.get();
						Boolean childValueNew = childValueNewGetter.get();
						IModification commitModif = commitModifGetter.get();
						String subModifTitle = AbstractModification.getUndoTitle(undoModif.getTitle());
						String parentModifTitle = parentModifTitleGetter.get();
						if (parentModifTitle != null) {
							subModifTitle = ReflectionUIUtils.composeMessage(parentModifTitle, subModifTitle);
						}
						ModificationStack parentModifStack = parentModifStackGetter.get();
						IInfo childModifTarget = childModifTargetGetter.get();
						return ReflectionUIUtils.integrateSubModifications(reflectionUI, parentModifStack,
								childModifStack, childModifAccepted, childValueReturnMode, childValueNew, commitModif,
								childModifTarget, subModifTitle);
					}

					@Override
					public void beginComposite() {
						subFormModifStack.beginComposite();
						ModificationStack parentModifStack = parentModifStackGetter.get();
						parentModifStack.beginComposite();
					}

					@Override
					public boolean endComposite(IInfo childModifTarget, String title, UndoOrder order) {
						subFormModifStack.endComposite(childModifTarget, title, order);
						ModificationStack parentModifStack = parentModifStackGetter.get();
						return parentModifStack.endComposite(childModifTarget, title, order);
					}

					@Override
					public void abortComposite() {
						subFormModifStack.abortComposite();
						ModificationStack parentModifStack = parentModifStackGetter.get();
						parentModifStack.abortComposite();
					}

					@Override
					public void invalidate() {
						subFormModifStack.invalidate();
						ModificationStack childModifStack = new ModificationStack(null);
						childModifStack.invalidate();
						Boolean childModifAccepted = childModifAcceptedGetter.get();
						ValueReturnMode childValueReturnMode = childValueReturnModeGetter.get();
						Boolean childValueNew = childValueNewGetter.get();
						IModification commitModif = commitModifGetter.get();
						String childModifTitle = null;
						IInfo childModifTarget = childModifTargetGetter.get();
						ModificationStack parentModifStack = parentModifStackGetter.get();
						ReflectionUIUtils.integrateSubModifications(reflectionUI, parentModifStack, childModifStack,
								childModifAccepted, childValueReturnMode, childValueNew, commitModif, childModifTarget,
								childModifTitle);
					}

				});
	}

	public static void handleComponentSizeChange(Component c) {
		Window window = SwingUtilities.getWindowAncestor(c);
		if (window != null) {
			window.validate();
		}
	}

	public static void setValueThroughModificationStack(Object object, IFieldInfo field, Object newValue,
			ModificationStack modifStack) {
		ControlDataValueModification modif = new ControlDataValueModification(new FieldControlData(object, field),
				newValue, field);
		try {
			modifStack.apply(modif);
		} catch (Throwable t) {
			modifStack.invalidate();
			throw new ReflectionUIError(t);
		}
	}

	public static Object invokeMethodThroughModificationStack(Object object, IMethodInfo method,
			InvocationData invocationData, ModificationStack modifStack) {
		if (method.isReadOnly()) {
			return method.invoke(object, invocationData);
		} else {
			Runnable undoJob = method.getUndoJob(object, invocationData);
			if (undoJob != null) {
				final Object[] resultHolder = new Object[1];
				method = new MethodInfoProxy(method) {
					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						return resultHolder[0] = super.invoke(object, invocationData);
					}
				};
				InvokeMethodModification modif = new InvokeMethodModification(object, method, invocationData);
				try {
					modifStack.apply(modif);
				} catch (Throwable t) {
					modifStack.invalidate();
					throw new ReflectionUIError(t);
				}
				return resultHolder[0];
			} else {
				Object result = method.invoke(object, invocationData);
				modifStack.invalidate();
				return result;
			}
		}
	}

	public static void adjustWindowInitialBounds(Window window) {
		window.setLocationRelativeTo(null);
		if (window.getParent() != null) {
			Window parentWindow = SwingRendererUtils.getWindowAncestorOrSelf(window.getParent());
			if (parentWindow != null) {
				GraphicsDevice parentScreen = SwingRendererUtils.getWindowCurrentGraphicsDevice(parentWindow);
				window.setLocation(parentScreen.getDefaultConfiguration().getBounds().getLocation());
			}
		}
		Rectangle maxBounds = SwingRendererUtils.getMaximumWindowBounds(getWindowCurrentGraphicsDevice(window));
		window.setBounds(maxBounds);
		window.pack();
		Rectangle bounds = window.getBounds();
		int widthGrowth, heightGrowth;
		{
			if (bounds.width < maxBounds.width / 2) {
				widthGrowth = maxBounds.width / 2 - bounds.width;
			} else {
				widthGrowth = 0;
			}
			heightGrowth = 4 * SwingRendererUtils.getStandardCharacterWidth(window);
			bounds.width += widthGrowth;
			bounds.height += heightGrowth;
		}
		bounds = maxBounds.intersection(bounds);
		bounds.x = maxBounds.x + (maxBounds.width - bounds.width) / 2;
		bounds.y = maxBounds.y + (maxBounds.height - bounds.height) / 2;
		window.setBounds(bounds);
	}

	public static Rectangle getMaximumWindowBounds(GraphicsDevice gd) {
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		Rectangle screenBounds = gc.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
		Rectangle result = new Rectangle();
		result.x = screenBounds.x + screenInsets.left;
		result.y = screenBounds.y + screenInsets.top;
		result.height = screenBounds.height - screenInsets.top - screenInsets.bottom;
		result.width = screenBounds.width - screenInsets.left - screenInsets.right;
		return result;
	}

	public static GraphicsDevice getWindowCurrentGraphicsDevice(Window window) {
		GraphicsDevice result = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice gd : ge.getScreenDevices()) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			Rectangle screenBounds = gc.getBounds();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
			GraphicsDevice candidateResult = gd;
			if (result == null) {
				result = candidateResult;
			} else {
				if (window == null) {
					return result;
				} else {
					if ((window.getWidth() * window.getHeight()) == 0) {
						if (getMaximumWindowBounds(candidateResult).contains(window.getLocation())) {
							result = candidateResult;
						}
					} else {
						Rectangle candidateResultIntersection = getMaximumWindowBounds(candidateResult)
								.intersection(window.getBounds());
						Rectangle resultIntersection = getMaximumWindowBounds(result).intersection(window.getBounds());
						int candidateResultIntersectionArea = candidateResultIntersection.width
								* candidateResultIntersection.height;
						int resultIntersectionArea = resultIntersection.width * resultIntersection.height;
						if (candidateResultIntersectionArea > resultIntersectionArea) {
							result = candidateResult;
						}
					}
				}
			}
		}
		return result;
	}

	public static int removeAWTEventListener(AWTEventListener listener) {
		final List<AWTEventListener> listenersToRemove = new ArrayList<AWTEventListener>();
		for (AWTEventListener l : Toolkit.getDefaultToolkit().getAWTEventListeners()) {
			if (l == listener) {
				listenersToRemove.add(l);
			} else if (l instanceof AWTEventListenerProxy) {
				final AWTEventListenerProxy proxyListener = (AWTEventListenerProxy) l;
				if (proxyListener.getListener() == listener) {
					listenersToRemove.add(proxyListener);
				}
			}
		}
		for (AWTEventListener l : listenersToRemove) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(l);
		}
		return listenersToRemove.size();
	}

	public Icon getControlIcon(IControlData data, SwingRenderer swingRenderer) {
		Image iconImage = swingRenderer.getControlDataIconImage(data);
		if (iconImage != null) {
			return SwingRendererUtils.getSmallIcon(iconImage);
		} else {
			return null;
		}
	}

	public static Color getNullColor() {
		return fixSeveralColorRenderingIssues(new JTextArea().getDisabledTextColor());
	}

	public static Image getCachedIconImage(SwingRenderer swingRenderer, Map<String, Object> properties) {
		if (properties == null) {
			return null;
		}
		Image result = DesktopSpecificProperty.getIconImage(properties);
		if (result != null) {
			return result;
		}
		String imagePath = DesktopSpecificProperty.getIconImageFilePath(properties);
		if (imagePath == null) {
			return null;
		}
		result = SwingRendererUtils.IMAGE_CACHE.get(imagePath);
		if (result == null) {
			URL imageUrl;
			if (imagePath.startsWith(ResourcePath.CLASSPATH_RESOURCE_PREFIX)) {
				imagePath = imagePath.substring(ResourcePath.CLASSPATH_RESOURCE_PREFIX.length());
				imageUrl = SwingRendererUtils.class.getClassLoader().getResource(imagePath);
			} else {
				try {
					imageUrl = new File(imagePath).toURI().toURL();
				} catch (MalformedURLException e) {
					throw new ReflectionUIError(e);
				}
			}
			try {
				result = ImageIO.read(imageUrl);
			} catch (IOException e) {
				swingRenderer.getReflectionUI()
						.logError("Failed to load image from '" + imageUrl + "': " + e.toString());
				result = NULL_ICON_IMAGE;
			}
			SwingRendererUtils.IMAGE_CACHE.put(imagePath, result);
		}
		if (result == NULL_ICON_IMAGE) {
			return null;
		}
		return result;
	}

	public static Icon getControlDataIcon(SwingRenderer swingRenderer, IControlData data) {
		Image iconImage = swingRenderer.getControlDataIconImage(data);
		if (iconImage != null) {
			return SwingRendererUtils.getSmallIcon(iconImage);
		} else {
			return null;
		}
	}

	public static Icon getMethodIcon(SwingRenderer swingRenderer, Object object, IMethodInfo method) {
		Image iconImage = swingRenderer.getMethodIconImage(object, method);
		if (iconImage != null) {
			return SwingRendererUtils.getSmallIcon(iconImage);
		} else {
			return null;
		}
	}

	public static Window getActiveWindow() {
		return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
	}

	public static Object showBusyDialogWhileInvokingMethod(Component activatorComponent, SwingRenderer swingRenderer,
			final Object object, final IMethodInfo method, final InvocationData invocationData) {
		final Object[] result = new Object[1];
		swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
			public void run() {
				result[0] = method.invoke(object, invocationData);
			}
		}, ReflectionUIUtils.composeMessage(method.getCaption(), "Execution"));
		return result[0];

	}

	public static Object showBusyDialogWhileGettingFieldValue(Component activatorComponent, SwingRenderer swingRenderer,
			final Object object, final IFieldInfo field) {
		final Object[] result = new Object[1];
		swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
			public void run() {
				result[0] = field.getValue(object);
			}
		}, "Getting " + field.getCaption());
		return result[0];

	}

	public static void showBusyDialogWhileSettingFieldValue(Component activatorComponent, SwingRenderer swingRenderer,
			final Object object, final IFieldInfo field, final Object value) {
		swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
			public void run() {
				field.setValue(object, value);
			}
		}, "Setting " + field.getCaption());

	}

}
