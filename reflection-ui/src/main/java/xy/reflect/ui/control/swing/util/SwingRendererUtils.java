
package xy.reflect.ui.control.swing.util;

import java.awt.BorderLayout;
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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.plugin.ICustomizableFieldControlPlugin;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.plugin.HtmlPlugin;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ResourcePath.PathKind;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Utilities for dealing with {@link SwingRenderer} instances.
 * 
 * @author olitank
 *
 */
public class SwingRendererUtils {

	public static final BufferedImage NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	public static final ImageIcon ERROR_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/error.png"));
	public static final ImageIcon HELP_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/help.png"));
	public static final ImageIcon DETAILS_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/details.png"));
	public static final ImageIcon ADD_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/add.png"));
	public static final ImageIcon REMOVE_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/remove.png"));
	public static final ImageIcon UP_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/up.png"));
	public static final ImageIcon DOWN_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/down.png"));
	public static final ImageIcon SAVE_ALL_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/save-all.png"));

	public static Map<String, Image> IMAGE_CACHE = new HashMap<String, Image>();

	private static final Map<GraphicsDevice, Rectangle> MAXIMUM_BOUNDS_BY_GRAPHIC_DEVICE_CACHE = new HashMap<GraphicsDevice, Rectangle>();

	public static boolean isNullImage(Image image) {
		return (image.getWidth(null) * image.getHeight(null) == 1);
	}

	public static ImageIcon getSmallIcon(ImageIcon icon) {
		Image image = icon.getImage();
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
		g.drawImage(image.getScaledInstance(imageBoundsInBox.width, imageBoundsInBox.height, Image.SCALE_SMOOTH),
				imageBoundsInBox.x, imageBoundsInBox.y, null);
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
				System.err.println(
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
		JPanel result = new ControlPanel();
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

	public static Form findParentForm(Component component, SwingRenderer swingRenderer) {
		Component candidate = component.getParent();
		while (candidate != null) {
			if (isForm(candidate, swingRenderer)) {
				return (Form) candidate;
			}
			candidate = candidate.getParent();
		}
		return null;
	}

	public static List<Form> findAncestorForms(Component component, SwingRenderer swingRenderer) {
		List<Form> result = new ArrayList<Form>();
		Form ancestor = null;
		while (null != (ancestor = findParentForm(component, swingRenderer))) {
			result.add(ancestor);
			component = ancestor;
		}
		return result;
	}

	public static Form findMostAncestorForm(Component component, SwingRenderer swingRenderer) {
		List<Form> ancestors = findAncestorForms(component, swingRenderer);
		if (ancestors.size() == 0) {
			return null;
		}
		return ancestors.get(ancestors.size() - 1);
	}

	public static List<Form> findDescendantForms(Container container, SwingRenderer swingRenderer) {
		List<Form> result = new ArrayList<Form>();
		for (Component childComponent : container.getComponents()) {
			if (isForm(childComponent, swingRenderer)) {
				result.add((Form) childComponent);
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

	public static void setMultilineToolTipText(JComponent c, String toolTipText) {
		if (toolTipText == null) {
			c.setToolTipText(null);
		} else {
			if (!MiscUtils.isHTMLText(toolTipText)) {
				toolTipText = "<HTML>" + MiscUtils.escapeHTML(toolTipText, true) + "</HTML>";
			}
			c.setToolTipText(toolTipText);
		}
	}

	public static void setSafelyDividerLocation(final JSplitPane splitPane, final double proportionalLocation) {
		if (splitPane.isShowing()) {
			if ((splitPane.getWidth() > 0) && (splitPane.getHeight() > 0)
					&& (splitPane.getMinimumDividerLocation() < splitPane.getMaximumDividerLocation())) {
				splitPane.setDividerLocation(proportionalLocation);
			} else {
				splitPane.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent ce) {
						splitPane.removeComponentListener(this);
						setSafelyDividerLocation(splitPane, proportionalLocation);
					}
				});
			}
		} else {
			splitPane.addHierarchyListener(new HierarchyListener() {
				@Override
				public void hierarchyChanged(HierarchyEvent e) {
					if (((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) && splitPane.isShowing()) {
						splitPane.removeHierarchyListener(this);
						setSafelyDividerLocation(splitPane, proportionalLocation);
					}
				}
			});
		}
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

	public static void setUndecorated(Window window, boolean b) {
		if (window instanceof JDialog) {
			((JDialog) window).setUndecorated(b);
		} else if (window instanceof JFrame) {
			((JFrame) window).setUndecorated(b);
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

	public static List<Object> getAllDisplayedObjects(SwingRenderer swingRenderer) {
		List<Object> result = new ArrayList<Object>();
		for (Form form : swingRenderer.getAllDisplayedForms()) {
			result.add(form.getObject());
		}
		return result;
	}

	public static List<Object> getDisplayedInstances(ITypeInfo type, SwingRenderer swingRenderer) {
		List<Object> result = new ArrayList<Object>();
		for (Object object : getAllDisplayedObjects(swingRenderer)) {
			ITypeInfo objectType = swingRenderer.getReflectionUI()
					.buildTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
			if (objectType.getName().equals(type.getName())) {
				result.add(object);
			}
		}
		return result;
	}

	public static boolean isForm(Component c, SwingRenderer swingRenderer) {
		if (!(c instanceof Form)) {
			return false;
		}
		Form form = (Form) c;
		if (form.getSwingRenderer() != swingRenderer) {
			return false;
		}
		return true;
	}

	public static void handleComponentSizeChange(Component c) {
		c.invalidate();
		Window window = SwingUtilities.getWindowAncestor(c);
		if (window != null) {
			window.validate();
		}
		c.repaint();
	}

	public static Rectangle getScreenBounds(Component c) {
		Window window = getWindowAncestorOrSelf(c);
		if (window != null) {
			GraphicsDevice device = getWindowCurrentGraphicsDevice(window);
			if (device != null) {
				Rectangle maxBounds = SwingRendererUtils.getMaximumWindowBounds(device);
				return maxBounds;
			}
		}
		return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
	}

	public static Dimension getScreenSize(Component c) {
		return getScreenBounds(c).getSize();
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
			widthGrowth = 4 * characterSize;
			heightGrowth = 4 * characterSize;
			bounds.width += widthGrowth;
			bounds.height += heightGrowth;
		}
		int minWidth = characterSize * 60;
		bounds.width = Math.max(bounds.width, minWidth);
		bounds = maxBounds.intersection(bounds);
		bounds.x = maxBounds.x + (maxBounds.width - bounds.width) / 2;
		bounds.y = maxBounds.y + (maxBounds.height - bounds.height) / 2;
		window.setBounds(bounds);
	}

	public static Rectangle getMaximumWindowBounds(GraphicsDevice gd) {
		Rectangle result = MAXIMUM_BOUNDS_BY_GRAPHIC_DEVICE_CACHE.get(gd);
		if (result == null) {
			result = new Rectangle();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			Rectangle screenBounds = gc.getBounds();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
			result.x = screenBounds.x + screenInsets.left;
			result.y = screenBounds.y + screenInsets.top;
			result.height = screenBounds.height - screenInsets.top - screenInsets.bottom;
			result.width = screenBounds.width - screenInsets.left - screenInsets.right;
			MAXIMUM_BOUNDS_BY_GRAPHIC_DEVICE_CACHE.put(gd, result);
		}
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
					Rectangle candidateMaximumBounds = getMaximumWindowBounds(candidateResult);
					if ((window.getWidth() * window.getHeight()) == 0) {
						if (candidateMaximumBounds.contains(window.getLocation())) {
							result = candidateResult;
						}
					} else {
						Rectangle candidateResultIntersection = candidateMaximumBounds.intersection(window.getBounds());
						Rectangle resultMaximumBounds = getMaximumWindowBounds(result);
						Rectangle resultIntersection = resultMaximumBounds.intersection(window.getBounds());
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

	public static ResourcePath putImageInCache(Image image) {
		String imagePathSpecification = ResourcePath.specifyMemoryObjectSpecification(
				image.getClass().getName() + "-" + Integer.toString(image.hashCode()));
		SwingRendererUtils.IMAGE_CACHE.put(imagePathSpecification, image);
		return new ResourcePath(imagePathSpecification);
	}

	public static Image loadImageThroughCache(ResourcePath imagePath, Listener<String> errorMessageListener) {
		if (imagePath == null) {
			return null;
		}
		if (imagePath.getPathKind() == PathKind.MEMORY_OBJECT) {
			return SwingRendererUtils.IMAGE_CACHE.get(imagePath.getSpecification());
		}
		Image result = SwingRendererUtils.IMAGE_CACHE.get(imagePath.getSpecification());
		if (result == null) {
			try {
				URL imageUrl;
				if (imagePath.getPathKind() == PathKind.CLASS_PATH_RESOURCE) {
					String classPathResourceLocation = ResourcePath
							.extractClassPathResourceLocation(imagePath.getSpecification());
					imageUrl = SwingRendererUtils.class.getClassLoader().getResource(classPathResourceLocation);
					if (imageUrl == null) {
						throw new ReflectionUIError(
								"Class path resource not found: '" + classPathResourceLocation + "'");
					}
				} else {
					try {
						imageUrl = new File(imagePath.getSpecification()).toURI().toURL();
					} catch (MalformedURLException e) {
						throw new ReflectionUIError(e);
					}
				}
				result = ImageIO.read(imageUrl);
			} catch (IOException e) {
				if (errorMessageListener != null) {
					errorMessageListener.handle("Failed to load image from '" + imagePath + "': " + e.toString());
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

	public static ImageIcon getIcon(Image iconImage) {
		if (iconImage != null) {
			return new ImageIcon(iconImage);
		} else {
			return null;
		}
	}

	public static ImageIcon getObjectIcon(SwingRenderer swingRenderer, Object object) {
		if (object == null) {
			return null;
		}
		return getIcon(swingRenderer.getObjectIconImage(object));
	}

	public static List<Window> getFrontWindows() {
		List<Window> result = new ArrayList<Window>();
		for (Window window : Window.getWindows()) {
			if (window.isVisible()) {
				Window[] ownedWindows = window.getOwnedWindows();
				if ((ownedWindows == null) || (ownedWindows.length == 0)) {
					result.add(window);
				}
				boolean allOwnedWindowInvisible = true;
				for (Window ownedWindow : ownedWindows) {
					if (ownedWindow.isVisible()) {
						allOwnedWindowInvisible = false;
						break;
					}
				}
				if (allOwnedWindowInvisible) {
					result.add(window);
				}
			}
		}
		return result;
	}

	public static Object showBusyDialogWhileInvokingMethod(Component activatorComponent, SwingRenderer swingRenderer,
			final IMethodControlData data, final InvocationData invocationData) {
		final Object[] result = new Object[1];
		swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
			public void run() {
				result[0] = data.invoke(invocationData);
			}
		}, ReflectionUIUtils.composeMessage(data.getCaption(), "Executing..."));
		return result[0];

	}

	public static Border getErrorBorder() {
		return BorderFactory.createLineBorder(Color.RED, 2);

	}

	public static Component getMessagePane(final String msg, int jOptionPaneMessageType,
			final SwingRenderer swingRenderer) {
		JPanel result = new ControlPanel();
		result.setLayout(new BorderLayout());
		{
			IFieldControlInput textControlInput = new IFieldControlInput() {

				IFieldInfo field = new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
					@Override
					public Object getValue(Object object) {
						return msg;
					}
				};

				@Override
				public ModificationStack getModificationStack() {
					return new ModificationStack(null);
				}

				@Override
				public IFieldControlData getControlData() {
					return new DefaultFieldControlData(swingRenderer.getReflectionUI(), null, field) {

						@Override
						public ColorSpecification getLabelForegroundColor() {
							return swingRenderer.getReflectionUI().getApplicationInfo().getMainForegroundColor();
						}

					};
				}

				@Override
				public IContext getContext() {
					return IContext.NULL_CONTEXT;
				}
			};
			TextControl textControl = MiscUtils.isHTMLText(msg)
					? new HtmlPlugin().new HtmlControl(swingRenderer, textControlInput)
					: new TextControl(swingRenderer, textControlInput);
			result.add(textControl, BorderLayout.CENTER);
		}
		{
			Icon icon;
			if (jOptionPaneMessageType == JOptionPane.ERROR_MESSAGE) {
				icon = UIManager.getIcon("OptionPane.errorIcon");
			} else if (jOptionPaneMessageType == JOptionPane.INFORMATION_MESSAGE) {
				icon = UIManager.getIcon("OptionPane.informationIcon");
			} else if (jOptionPaneMessageType == JOptionPane.QUESTION_MESSAGE) {
				icon = UIManager.getIcon("OptionPane.questionIcon");
			} else if (jOptionPaneMessageType == JOptionPane.WARNING_MESSAGE) {
				icon = UIManager.getIcon("OptionPane.warningIcon");
			} else if (jOptionPaneMessageType == JOptionPane.PLAIN_MESSAGE) {
				icon = null;
			} else {
				throw new ReflectionUIError();
			}
			JLabel iconControl = new JLabel(icon);
			result.add(SwingRendererUtils.flowInLayout(iconControl, GridBagConstraints.NORTH), BorderLayout.WEST);
		}
		return result;
	}

	public static Component getInputPane(final String[] inputValue, final String caption,
			final SwingRenderer swingRenderer) {
		JPanel result = new ControlPanel();
		result.setLayout(new BorderLayout());
		{
			JLabel captionControl = new JLabel(caption);
			if (swingRenderer.getReflectionUI().getApplicationInfo().getMainForegroundColor() != null) {
				captionControl.setForeground(SwingRendererUtils
						.getColor(swingRenderer.getReflectionUI().getApplicationInfo().getMainForegroundColor()));
			}
			result.add(captionControl, BorderLayout.WEST);
		}
		{
			TextControl textControl = new TextControl(swingRenderer, new IFieldControlInput() {

				IFieldInfo field = new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
					@Override
					public Object getValue(Object object) {
						return inputValue[0];
					}

					@Override
					public void setValue(Object object, Object value) {
						inputValue[0] = (String) value;
					}

					@Override
					public ITypeInfo getType() {
						return swingRenderer.getReflectionUI().buildTypeInfo(
								new JavaTypeInfoSource(swingRenderer.getReflectionUI(), String.class, null));
					}

					@Override
					public boolean isGetOnly() {
						return false;
					}
				};

				@Override
				public ModificationStack getModificationStack() {
					return new ModificationStack(null);
				}

				@Override
				public IFieldControlData getControlData() {
					return new DefaultFieldControlData(swingRenderer.getReflectionUI(), null, field) {

						@Override
						public ColorSpecification getBorderColor() {
							return swingRenderer.getReflectionUI().getApplicationInfo().getMainBorderColor();

						}

						@Override
						public ColorSpecification getEditorForegroundColor() {
							return swingRenderer.getReflectionUI().getApplicationInfo().getMainEditorForegroundColor();

						}

						@Override
						public ColorSpecification getEditorBackgroundColor() {
							return swingRenderer.getReflectionUI().getApplicationInfo().getMainEditorBackgroundColor();

						}

					};
				}

				@Override
				public IContext getContext() {
					return IContext.NULL_CONTEXT;
				}
			});
			result.add(textControl, BorderLayout.CENTER);
		}
		return result;
	}

	public static void removeScrollPaneBorder(JScrollPane scrollPane) {
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
	}

	public static boolean requestAnyComponentFocus(Component c, SwingRenderer swingRenderer) {
		if (c instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) c).requestCustomFocus();
		}
		if (isForm(c, swingRenderer)) {
			return ((Form) c).requestFormFocus();
		}
		if (!c.isEnabled()) {
			return false;
		}
		if (c.hasFocus()) {
			return true;
		}
		if (c.requestFocusInWindow()) {
			return true;
		}
		if (c instanceof Container) {
			try {
				for (Component child : ((Container) c).getComponents()) {
					if (requestAnyComponentFocus(child, swingRenderer)) {
						return true;
					}
				}
			} catch (Throwable ignore) {
			}
		}
		return false;
	}

	public static void displayErrorOnBorderAndTooltip(JComponent borderComponent, JComponent toolTipComponent,
			String msg, SwingRenderer swingRenderer) {
		String oldTooltipText = toolTipComponent.getToolTipText();
		String newTooltipText = null;
		{
			if (msg != null) {
				newTooltipText = swingRenderer.prepareMessageToDisplay(msg);
				if (newTooltipText.length() == 0) {
					newTooltipText = null;
				}
			}
		}
		boolean changeDetected = !MiscUtils.equalsOrBothNull(oldTooltipText, newTooltipText);
		if (!changeDetected) {
			return;
		}
		if (newTooltipText == null) {
			borderComponent.setBorder(null);
		} else {
			borderComponent.setBorder(SwingRendererUtils.getErrorBorder());
		}
		SwingRendererUtils.setMultilineToolTipText(toolTipComponent, newTooltipText);
		SwingRendererUtils.handleComponentSizeChange(borderComponent);
	}

	public static List<Form> findObjectDisplayedForms(Object object, SwingRenderer swingRenderer) {
		List<Form> result = new ArrayList<Form>();
		for (Form form : swingRenderer.getAllDisplayedForms()) {
			if (form.getObject() == object) {
				result.add(form);
			}
		}
		return result;
	}

	public static Form findFirstObjectDisplayedForm(Object object, SwingRenderer swingRenderer) {
		for (Form form : findObjectDisplayedForms(object, swingRenderer)) {
			return form;
		}
		return null;
	}

	public static Form findFirstObjectDescendantForm(Object object, Container container, SwingRenderer swingRenderer) {
		for (Form form : findDescendantForms(container, swingRenderer)) {
			if (object == form.getObject()) {
				return form;
			}
		}
		return null;
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

	public static List<Form> excludeSubForms(List<Form> forms, SwingRenderer swingRenderer) {
		List<Form> result = new ArrayList<Form>(forms);
		for (Form form : forms) {
			result.removeAll(findDescendantForms(form, swingRenderer));
		}
		return result;
	}

	public static void updateWindowMenu(Form form, SwingRenderer swingRenderer) {
		Form topForm = SwingRendererUtils.findMostAncestorForm(form, swingRenderer);
		if (topForm == null) {
			topForm = form;
		}
		topForm.updateMenuBar();
	}

	public static Color fixSeveralColorRenderingIssues(Color color) {
		return new Color(color.getRGB());
	}

	public static Color getListSelectionBackgroundColor() {
		return fixSeveralColorRenderingIssues(UIManager.getColor("Tree.selectionBackground"));
	}

	public static Color getListSelectionTextColor() {
		return fixSeveralColorRenderingIssues(UIManager.getColor("Tree.selectionForeground"));
	}

	public static void refreshAllDisplayedFormsAndMenus(SwingRenderer swingRenderer, boolean refreshStructure) {
		for (Form form : getAllRootForms(swingRenderer)) {
			form.refresh(refreshStructure);
			form.updateMenuBar();
		}
	}

	public static List<Form> getAllRootForms(SwingRenderer swingRenderer) {
		return excludeSubForms(swingRenderer.getAllDisplayedForms(), swingRenderer);
	}

	public static ModificationStack findParentFormModificationStack(Component component, SwingRenderer swingRenderer) {
		Form form = findParentForm(component, swingRenderer);
		if (form == null) {
			return null;
		}
		return form.getModificationStack();
	}

	public static Color getColor(ColorSpecification colorSpec) {
		if (colorSpec == null) {
			return null;
		}
		return new Color(colorSpec.getRed(), colorSpec.getGreen(), colorSpec.getBlue());
	}

	public static void setColor(ColorSpecification colorSpec, Color color) {
		colorSpec.setRed(color.getRed());
		colorSpec.setGreen(color.getGreen());
		colorSpec.setBlue(color.getBlue());
	}

	public static ColorSpecification getColorSpecification(Color color) {
		if (color == null) {
			return null;
		}
		ColorSpecification result = new ColorSpecification();
		setColor(result, color);
		return result;
	}

	public static BufferedImage iconToImage(Icon icon) {
		int w = icon.getIconWidth();
		int h = icon.getIconHeight();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h);
		Graphics2D g = image.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		return image;
	}

	public static IFieldControlPlugin findFieldControlPlugin(SwingRenderer swingRenderer, String pluginId) {
		for (IFieldControlPlugin plugin : swingRenderer.getFieldControlPlugins()) {
			if (plugin.getIdentifier().equals(pluginId)) {
				return plugin;
			}
		}
		return null;
	}

	public static IFieldControlPlugin getCurrentFieldControlPlugin(SwingRenderer swingRenderer,
			Map<String, Object> specificProperties, IFieldControlInput input) {
		String chosenPluginId = ReflectionUIUtils.getFieldControlPluginIdentifier(specificProperties);
		if (chosenPluginId != null) {
			IFieldControlPlugin plugin = findFieldControlPlugin(swingRenderer, chosenPluginId);
			if (plugin != null) {
				if (plugin.handles(input)) {
					return plugin;
				}
			}
		}
		return null;
	}

	public static void setCurrentFieldControlPlugin(SwingRenderer swingRenderer, Map<String, Object> specificProperties,
			IFieldControlPlugin plugin) {
		String lastPluginId = ReflectionUIUtils.getFieldControlPluginIdentifier(specificProperties);
		ReflectionUIUtils.setFieldControlPluginIdentifier(specificProperties, null);
		if (lastPluginId != null) {
			IFieldControlPlugin lastPlugin = findFieldControlPlugin(swingRenderer, lastPluginId);
			if (lastPlugin instanceof ICustomizableFieldControlPlugin) {
				ReflectionUIUtils.setFieldControlPluginConfiguration(specificProperties, lastPlugin.getIdentifier(),
						null);
			}
		}
		if (plugin != null) {
			ReflectionUIUtils.setFieldControlPluginIdentifier(specificProperties, plugin.getIdentifier());
			if (plugin instanceof ICustomizableFieldControlPlugin) {
				ReflectionUIUtils.setFieldControlPluginConfiguration(specificProperties, plugin.getIdentifier(),
						((ICustomizableFieldControlPlugin) plugin).getDefaultControlCustomization());
			}
		}
	}

	public static void generateChangeEventsDuringTextFieldEditing(final JSpinner spinner) {
		spinner.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("editor".equals(evt.getPropertyName())) {
					JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
					final JFormattedTextField textField = (JFormattedTextField) editor.getTextField();
					textField.getDocument().addDocumentListener(new DocumentListener() {

						private void anyUpdate() {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									String string = textField.getText();
									Object value;
									DefaultFormatter formatter = ((DefaultFormatter) textField.getFormatter());
									try {
										value = formatter.stringToValue(string);
									} catch (ParseException e) {
										return;
									}
									int caretPosition = textField.getCaretPosition();
									spinner.setValue(value);
									textField.setCaretPosition(Math.min(caretPosition, textField.getText().length()));
								}
							});

						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							anyUpdate();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							anyUpdate();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							anyUpdate();
						}
					});
				}
			}
		});

	}

	public static void withHighQualityScaling(Graphics2D g, Runnable paintAction) {
		Object KEY_ANTIALIASING_ToRestore = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		Object KEY_INTERPOLATION_ToRestore = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		paintAction.run();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, KEY_ANTIALIASING_ToRestore);
		} catch (Throwable ignore) {
		}
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, KEY_INTERPOLATION_ToRestore);
		} catch (Throwable ignore) {
		}
	}

	public static void expectInUIThread() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new ReflectionUIError("The current thread must be the UI thread.");
		}
	}

	/**
	 * @param image The input image.
	 * @return an image that simulates an activation effect when replacing the given
	 *         image.
	 */
	public static BufferedImage addImageActivationEffect(Image image) {
		BufferedImage result = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = result.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		float scalefactor = 0.25f;
		float offset = 128f;
		return new RescaleOp(new float[] { scalefactor, scalefactor, scalefactor, 1f },
				new float[] { offset, offset, offset, 0f }, null).filter(result, null);
	}

	/**
	 * @param color The input color.
	 * @return A color that simulates an activation effect when replacing the given
	 *         color.
	 */
	public static Color addColorActivationEffect(Color color) {
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		if (hsb[2] > 0.5f) {
			hsb[2] -= 0.25f;
		} else {
			hsb[2] += 0.25f;
		}
		int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		return new Color(rgb);
	}

}
