package xy.reflect.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.SwingSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.InfoProxyGenerator;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;

@SuppressWarnings("unused")
public class SwingRendererUtils {

	public static final ImageIcon ERROR_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/error.png"));
	public static final ImageIcon HELP_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/help.png"));
	public static final ImageIcon DETAILS_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/details.png"));
	public static final ImageIcon ADD_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/add.png"));
	public static final ImageIcon REMOVE_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/remove.png"));
	public static final ImageIcon UP_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/up.png"));
	public static final ImageIcon DOWN_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/down.png"));
	public static final ImageIcon CUSTOMIZATION_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/custom.png"));
	public static final ImageIcon SAVE_ICON = new ImageIcon(ReflectionUI.class.getResource("resource/save.png"));
	public static final Image NULL_ICON_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private static Map<String, Image> iconImageCache = new HashMap<String, Image>();

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

	public static ModificationStack findModificationStack(Component component, SwingRenderer swingRenderer) {
		JPanel form = findForm(component, swingRenderer);
		if (form == null) {
			return ModificationStack.NULL_MODIFICATION_STACK;
		}
		return swingRenderer.getModificationStackByForm().get(form);
	}

	public static JPanel findForm(Component component, SwingRenderer swingRenderer) {
		while (component != null) {
			if (swingRenderer.getObjectByForm().keySet().contains(component)) {
				return (JPanel) component;
			}
			component = component.getParent();
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
		return new JTextField().getBackground();
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

	public static Image getIconImageFromInfo(IInfo info) {
		Image result;
		result = (Image) info.getSpecificProperties()
				.get(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE);
		if(result != null){
			return result;
		}
		URL imageUrl;
		String imagePath = (String) info.getSpecificProperties()
				.get(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH);
		String pathKind = (String) info.getSpecificProperties()
				.get(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
		if (imagePath == null) {
			return null;
		}
		if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
			imageUrl = SwingRendererUtils.class.getClassLoader().getResource(imagePath);
		} else {
			try {
				imageUrl = new File(imagePath).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new ReflectionUIError(e);
			}
		}
		result = iconImageCache.get(imagePath);
		if (result == null) {
			try {
				result = ImageIO.read(imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
				result = NULL_ICON_IMAGE;
			}
			iconImageCache.put(imagePath, result);
		}
		if (result == NULL_ICON_IMAGE) {
			return null;
		}
		return result;
	}

	public static Object invokeMethodAndAllowToUndo(Object object, IMethodInfo method, InvocationData invocationData, JPanel form, SwingRenderer swingRenderer) {
		ModificationStack stack = swingRenderer.getModificationStackByForm().get(form);
		Object result;
		try {
			result = method.invoke(object, invocationData);
		} catch (Throwable t) {
			stack.invalidate();
			throw new ReflectionUIError(t);
		}
		IModification undoModif = method.getUndoModification(object, invocationData);
		if (undoModif == null) {
			stack.invalidate();
		} else {
			stack.pushUndo(undoModif);
		}
		return result;	
	}
}
