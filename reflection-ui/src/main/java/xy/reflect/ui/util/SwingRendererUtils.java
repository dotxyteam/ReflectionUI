package xy.reflect.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.awt.event.ActionEvent;
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

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractMenuItem;
import xy.reflect.ui.info.menu.ActionMenuItem;
import xy.reflect.ui.info.menu.Menu;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationProxy;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.ResourcePath.PathKind;

public class SwingRendererUtils {

	public static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
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

	public static void showTooltipNow(JComponent c) {
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
						"Failed to show tooltip programmatically: \n1st failure: " + e1 + "\n2nd failure: \n" + e2);
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

	public static JPanel flowInLayout(Component c, int gridBagConstraintsAnchor) {
		JPanel result = new JPanel();
		result.setLayout(new GridBagLayout());
		GridBagConstraints contraints = new GridBagConstraints();
		contraints.gridx = 0;
		contraints.gridy = 0;
		contraints.weightx = 1;
		contraints.weighty = 1;
		contraints.anchor = gridBagConstraintsAnchor;
		result.add(c, contraints);
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
			if (isForm(childComponent, swingRenderer)) {
				result.add((JPanel) childComponent);
			}
			if (childComponent instanceof Container) {
				result.addAll(findDescendantForms((Container) childComponent, swingRenderer));
			}
		}
		return result;
	}

	public static int getStandardCharacterWidth(Component c) {
		Font font = c.getFont();
		if (font == null) {
			font = UIManager.getFont("Panel.font");
		}
		return c.getFontMetrics(font).charWidth('a');
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

	public static Color getEditableTextBackgroundColor() {
		return SwingRendererUtils.fixSeveralColorRenderingIssues(new JTextArea().getBackground());
	}

	public static Color getEditableTextForegroundColor() {
		return SwingRendererUtils.fixSeveralColorRenderingIssues(new JTextArea().getForeground());
	}

	public static Color getNonEditableTextForegroundColor() {
		return SwingRendererUtils.fixSeveralColorRenderingIssues(new JTextArea().getDisabledTextColor());
	}

	public static Color getNonEditableTextBackgroundColor() {
		return SwingRendererUtils.fixSeveralColorRenderingIssues(new JPanel().getBackground());
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
		if (c.isFocusOwner()) {
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

	public static boolean isForm(Component c, SwingRenderer swingRenderer) {
		return swingRenderer.getObjectByForm().keySet().contains(c);
	}

	public static void forwardFormModifications(final SwingRenderer swingRenderer, final JPanel form,
			final Accessor<Boolean> valueModifAcceptedGetter, final Accessor<ValueReturnMode> valueReturnModeGetter,
			final Accessor<Boolean> valueReplacedGetter, final Accessor<IModification> commitModifGetter,
			final Accessor<IInfo> editSessionTargetGetter, final Accessor<String> editSessionTitleGetter,
			final Accessor<ModificationStack> parentObjectModifStackGetter) {
		swingRenderer.getModificationStackByForm().put(form, new ModificationStack(null) {

			ModificationStack thisModificationStack = this;
			IModification eventTrigger = new ModificationProxy(IModification.NULL_MODIFICATION) {

				@Override
				public IModification applyAndGetOpposite() {
					return this;
				}

				@Override
				public boolean isNull() {
					return false;
				}

				@Override
				public String toString() {
					return "EventTrigger[of=" + thisModificationStack + "]";
				}

			};

			@Override
			public String toString() {
				return "Forward Modifications Of " + form.toString() + " To " + parentObjectModifStackGetter.get();
			}

			@Override
			public boolean pushUndo(IModification undoModif) {
				super.pushUndo(eventTrigger);
				ModificationStack valueModifStack = new ModificationStack(null);
				valueModifStack.pushUndo(undoModif);
				Boolean valueModifAccepted = valueModifAcceptedGetter.get();
				ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
				boolean valueReplaced = valueReplacedGetter.get();
				IModification commitModif = commitModifGetter.get();
				String editSessionTitle = AbstractModification.getUndoTitle(undoModif.getTitle());
				String editSessionTitlePrefix = editSessionTitleGetter.get();
				if (editSessionTitlePrefix != null) {
					editSessionTitle = ReflectionUIUtils.composeMessage(editSessionTitlePrefix, editSessionTitle);
				}
				ModificationStack parentObjectModifStack = parentObjectModifStackGetter.get();
				IInfo editSessionTarget = editSessionTargetGetter.get();
				return ReflectionUIUtils.finalizeParentObjectValueEditSession(parentObjectModifStack, valueModifStack,
						valueModifAccepted, valueReturnMode, valueReplaced, commitModif, editSessionTarget,
						editSessionTitle, ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
			}

			@Override
			public void invalidate() {
				super.invalidate();
				ModificationStack valueModifStack = new ModificationStack(null);
				valueModifStack.invalidate();
				Boolean valueModifAccepted = valueModifAcceptedGetter.get();
				ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
				boolean valueReplaced = valueReplacedGetter.get();
				IModification commitModif = commitModifGetter.get();
				String editSessionTitle = null;
				IInfo editSessionTarget = editSessionTargetGetter.get();
				ModificationStack parentObjectModifStack = parentObjectModifStackGetter.get();
				ReflectionUIUtils.finalizeParentObjectValueEditSession(parentObjectModifStack, valueModifStack,
						valueModifAccepted, valueReturnMode, valueReplaced, commitModif, editSessionTarget,
						editSessionTitle, ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
			}

			@Override
			public void beginComposite() {
				ModificationStack parentModifStack = parentObjectModifStackGetter.get();
				parentModifStack.beginComposite();
			}

			@Override
			public boolean endComposite(IInfo childModifTarget, String title, UndoOrder order) {
				ModificationStack parentModifStack = parentObjectModifStackGetter.get();
				return parentModifStack.endComposite(childModifTarget, title, order);
			}

			@Override
			public void abortComposite() {
				ModificationStack parentModifStack = parentObjectModifStackGetter.get();
				parentModifStack.abortComposite();
			}

			@Override
			public void undo() {
				throw new ReflectionUIError();
			}

			@Override
			public void redo() {
				throw new ReflectionUIError();
			}

		});
	}

	public static void handleComponentSizeChange(Component c) {
		c.invalidate();
		Window window = SwingUtilities.getWindowAncestor(c);
		if (window != null) {
			window.validate();
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
		int characterSize = getStandardCharacterWidth(window);
		window.setBounds(maxBounds);
		window.pack();
		Rectangle bounds = window.getBounds();
		int widthGrowth, heightGrowth;
		{
			if (bounds.width < (characterSize * 80)) {
				widthGrowth = (characterSize * 80) - bounds.width;
			} else {
				widthGrowth = 0;
			}
			heightGrowth = 4 * characterSize;
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

	public static ResourcePath putImageInCached(Image image) {
		String imagePathSpecification = ResourcePath
				.formatMemoryObjectSpecification(Integer.toString(image.hashCode()));
		SwingRendererUtils.IMAGE_CACHE.put(imagePathSpecification, image);
		return new ResourcePath(imagePathSpecification);
	}

	public static Image loadImageThroughcache(ResourcePath imagePath, Listener<String> errorMessageListener) {
		if (imagePath == null) {
			return null;
		}
		if (imagePath.getPathKind() == PathKind.MEMORY_OBJECT) {
			return SwingRendererUtils.IMAGE_CACHE.get(imagePath.getSpecification());
		}
		Image result = SwingRendererUtils.IMAGE_CACHE.get(imagePath);
		if (result == null) {
			URL imageUrl;
			if (imagePath.getPathKind() == PathKind.CLASS_PATH_RESOURCE) {
				imageUrl = SwingRendererUtils.class.getClassLoader()
						.getResource(ResourcePath.extractClassPathResourceValue(imagePath.getSpecification()));
			} else {
				try {
					imageUrl = new File(imagePath.getSpecification()).toURI().toURL();
				} catch (MalformedURLException e) {
					throw new ReflectionUIError(e);
				}
			}
			try {
				result = ImageIO.read(imageUrl);
			} catch (IOException e) {
				if (errorMessageListener != null) {
					errorMessageListener.handle("Failed to load image from '" + imageUrl + "': " + e.toString());
				}
				result = NULL_IMAGE;
			}
			SwingRendererUtils.IMAGE_CACHE.put(imagePath.getSpecification(), result);
		}
		if (result == NULL_IMAGE) {
			return null;
		}
		return result;
	}

	public static Icon geObjectIcon(SwingRenderer swingRenderer, Object object) {
		if (object == null) {
			return null;
		}
		Image iconImage = swingRenderer.getObjectIconImage(object);
		if (iconImage != null) {
			return SwingRendererUtils.getSmallIcon(iconImage);
		} else {
			return null;
		}
	}

	public static Icon getMethodIcon(SwingRenderer swingRenderer, IMethodControlData data) {
		Image iconImage = swingRenderer.getMethodIconImage(data);
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
			final IMethodControlData data, final InvocationData invocationData) {
		final Object[] result = new Object[1];
		swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
			public void run() {
				result[0] = data.invoke(invocationData);
			}
		}, ReflectionUIUtils.composeMessage(data.getCaption(), "Execution"));
		return result[0];

	}

	public static Object showBusyDialogWhileGettingFieldValue(Component activatorComponent, SwingRenderer swingRenderer,
			final IFieldControlData data) {
		final Object[] result = new Object[1];
		final String title;
		if ((data.getCaption() == null) || (data.getCaption().length() == 0)) {
			title = "Getting Value";
		} else {
			title = "Getting " + data.getCaption();
		}
		swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
			public void run() {
				result[0] = data.getValue();
			}
		}, title);
		return result[0];

	}

	public static void showBusyDialogWhileSettingFieldValue(Component activatorComponent, SwingRenderer swingRenderer,
			final IFieldControlData data, final Object value) {
		swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
			public void run() {
				data.setValue(value);
			}
		}, "Setting " + data.getCaption());

	}

	public static void setErrorBorder(JComponent c) {
		c.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

	}

	public static Component getMessageJOptionPane(String msg, int messageType) {
		JTextArea msgComponent = new JTextArea();
		msgComponent.setText(msg);
		msgComponent.setEditable(false);
		msgComponent.setBackground(getNonEditableTextBackgroundColor());
		final JScrollPane scrollPane = new JScrollPane(msgComponent);
		scrollPane.setBorder(null);
		JOptionPane result = new JOptionPane(scrollPane, messageType, JOptionPane.DEFAULT_OPTION, null,
				new Object[] {});
		return result;
	}

	public static boolean requestAnyComponentFocus(Component c, Object focusDetails, SwingRenderer swingRenderer) {
		if (c instanceof IAdvancedFieldControl) {
			if (((IAdvancedFieldControl) c).requestDetailedFocus(focusDetails)) {
				return true;
			}
			if (focusDetails != null) {
				if (((IAdvancedFieldControl) c).requestDetailedFocus(null)) {
					return true;
				}
			}
		}
		if (isForm(c, swingRenderer)) {
			if (swingRenderer.requestFormDetailedFocus((JPanel) c, focusDetails)) {
				return true;
			}
			if (focusDetails != null) {
				if (swingRenderer.requestFormDetailedFocus((JPanel) c, null)) {
					return true;
				}
			}
		}
		if (c.hasFocus()) {
			return true;
		}
		return c.requestFocusInWindow();
	}

	public static void displayErrorOnBorderAndTooltip(JComponent borderComponent, JComponent tooltipComponent,
			String msg, SwingRenderer swingRenderer) {
		String oldTooltipText;
		if (msg == null) {
			borderComponent.setBorder(null);
			oldTooltipText = tooltipComponent.getToolTipText();
			tooltipComponent.setToolTipText(null);
		} else {
			SwingRendererUtils.setErrorBorder(borderComponent);
			oldTooltipText = tooltipComponent.getToolTipText();
			String newTooltipText = swingRenderer.prepareStringToDisplay(msg);
			if (newTooltipText.length() == 0) {
				newTooltipText = null;
			}
			SwingRendererUtils.setMultilineToolTipText(tooltipComponent, newTooltipText);
		}
		SwingRendererUtils.handleComponentSizeChange(borderComponent);
		if (!ReflectionUIUtils.equalsOrBothNull(oldTooltipText, tooltipComponent.getToolTipText())) {
			SwingRendererUtils.showTooltipNow(tooltipComponent);
		}
	}

	public static List<JPanel> findObjectForms(Object object, SwingRenderer swingRenderer) {
		return ReflectionUIUtils.getKeysFromValue(swingRenderer.getObjectByForm(), object);
	}

	public static JPanel findFirstObjectActiveForm(Object object, SwingRenderer swingRenderer) {
		for (JPanel form : findObjectForms(object, swingRenderer)) {
			if (form.isDisplayable()) {
				return form;
			}
		}
		return null;
	}

	public static JPanel findFirstObjectDescendantForm(Object object, Container container,
			SwingRenderer swingRenderer) {
		for (JPanel form : findDescendantForms(container, swingRenderer)) {
			if (object == swingRenderer.getObjectByForm().get(form)) {
				return form;
			}
		}
		return null;
	}

	public static JPanel getForm(Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory) {
		if (fieldControlPlaceHoldersByCategory.values().size() > 0) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory.values()
					.iterator().next();
			if (fieldControlPlaceHolders.size() > 0) {
				return fieldControlPlaceHolders.get(0).getForm();
			}
		}
		if (methodControlPlaceHoldersByCategory.values().size() > 0) {
			List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory.values()
					.iterator().next();
			if (methodControlPlaceHolders.size() > 0) {
				return methodControlPlaceHolders.get(0).getForm();
			}
		}
		return null;
	}

	public static void updateMenubar(JMenuBar menuBar, MenuModel menuModel) {
		menuBar.removeAll();
		for (Menu menu : menuModel.getMenus()) {
			menuBar.add(createJMenu(menu));
		}
	}

	public static JMenu createJMenu(Menu menu) {
		JMenu result = new JMenu(menu.getName());
		for (AbstractMenuItem item : menu.getItems()) {
			result.add(createJMenuItem(item));
		}
		for (MenuItemCategory category : menu.getItemCategories()) {
			result.addSeparator();
			for (AbstractMenuItem item : category.getItems()) {
				result.add(createJMenuItem(item));
			}
		}
		return result;
	}

	public static JMenuItem createJMenuItem(AbstractMenuItem item) {
		if (item instanceof ActionMenuItem) {
			return createJMenuActionItem((ActionMenuItem) item);
		} else if (item instanceof Menu) {
			return createJMenu((Menu) item);
		} else {
			throw new ReflectionUIError();
		}
	}

	public static JMenuItem createJMenuActionItem(final ActionMenuItem actionItem) {
		return new JMenuItem(new AbstractAction(actionItem.getName()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				actionItem.getAction().run();
			}
		});
	}

	public static void setMenuBar(Window window, JMenuBar menuBar) {
		if (window instanceof JFrame) {
			((JFrame) window).setJMenuBar(menuBar);
		} else if (window instanceof JDialog) {
			((JDialog) window).setJMenuBar(menuBar);
		} else {
			throw new ReflectionUIError();
		}
	}

	public static JMenuBar getMenuBar(Window window) {
		if (window instanceof JFrame) {
			return ((JFrame) window).getJMenuBar();
		} else if (window instanceof JDialog) {
			return ((JDialog) window).getJMenuBar();
		} else {
			throw new ReflectionUIError();
		}
	}

	public static void setTitle(Window window, String title) {
		if (window instanceof JFrame) {
			((JFrame) window).setTitle(title);
		} else if (window instanceof JDialog) {
			((JDialog) window).setTitle(title);
		} else {
			throw new ReflectionUIError();
		}
	}

	public static String getTitle(Window window) {
		if (window instanceof JFrame) {
			return ((JFrame) window).getTitle();
		} else if (window instanceof JDialog) {
			return ((JDialog) window).getTitle();
		} else {
			throw new ReflectionUIError();
		}
	}

}
