package xy.reflect.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
import xy.reflect.ui.undo.ModificationStack;

public class SwingRendererUtils {

	public static final Icon ERROR_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/error.png"));
	public static final Icon HELP_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/help.png"));
	public static final Icon DETAILS_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/details.png"));
	public static final Icon ADD_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/add.png"));
	public static final Icon REMOVE_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/remove.png"));
	public static final Icon UP_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/up.png"));
	public static final Icon DOWN_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/down.png"));

	private static final String DO_NOT_CREATE_EMBEDDED_FORM_PROPRTY_KEY = DefaultTypeInfo.class
			.getName() + "#IS_EMBEDDED_FORM_CONTENT_PROPRTY_KEY";

	public static void showTooltipNow(Component c) {
		try {
			Method showToolTipMehod = ToolTipManager.class.getDeclaredMethod(
					"show", new Class<?>[] { JComponent.class });
			showToolTipMehod.setAccessible(true);
			showToolTipMehod.invoke(ToolTipManager.sharedInstance(),
					new Object[] { c });
		} catch (Throwable e1) {
			try {
				KeyEvent ke = new KeyEvent(c, KeyEvent.KEY_PRESSED,
						System.currentTimeMillis(), InputEvent.CTRL_MASK,
						KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED);
				c.dispatchEvent(ke);
			} catch (Throwable e2) {
				throw new ReflectionUIError(
						"Failed to show tooltip programmatically: \n1st failure: "
								+ e1 + "2nd failure: \n" + e2);
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

	public static ModificationStack findModificationStack(Component component,
			ReflectionUI reflectionUI) {
		JPanel form = findForm(component, reflectionUI);
		if (form == null) {
			return ModificationStack.NULL_MODIFICATION_STACK;
		}
		return reflectionUI.getSwingRenderer().getModificationStackByForm()
				.get(form);
	}

	public static JPanel findForm(Component component, ReflectionUI reflectionUI) {
		while (component != null) {
			if (reflectionUI.getSwingRenderer().getObjectByForm().keySet()
					.contains(component)) {
				return (JPanel) component;
			}
			component = component.getParent();
		}
		return null;
	}

	public static List<JPanel> findDescendantForms(Container container,
			ReflectionUI reflectionUI) {
		List<JPanel> result = new ArrayList<JPanel>();
		for (Component childComponent : container.getComponents()) {
			if (reflectionUI.getSwingRenderer().getObjectByForm().keySet()
					.contains(childComponent)) {
				result.add((JPanel) childComponent);
			} else {
				if (childComponent instanceof Container) {
					result.addAll(findDescendantForms(
							(Container) childComponent, reflectionUI));
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
			c.setToolTipText("<HTML>"
					+ ReflectionUIUtils.escapeHTML(toolTipText, true)
					+ "</HTML>");
		}
	}

	public static Color getTextBackgroundColor() {
		return new JTextField().getBackground();
	}

	public static void disableComponentTree(JComponent c, final boolean revert) {
		String CONTAINER_LISTENER_KEY = ReflectionUIUtils.class.getName()
				+ ".disableComponentTree.CONTAINER_LISTENER_KEY";
		String LAST_STATE_KEY = ReflectionUIUtils.class.getName()
				+ ".disableComponentTree.LAST_STATE_KEY";
		Boolean lastState = (Boolean) c.getClientProperty(LAST_STATE_KEY);
		if (revert) {
			if (lastState == null) {
				return;
			}
			if (lastState) {
				c.setEnabled(true);
			}
			c.putClientProperty(LAST_STATE_KEY, null);
			ContainerListener containerListener = (ContainerListener) c
					.getClientProperty(CONTAINER_LISTENER_KEY);
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
		return new ImageIcon(
				ReflectionUI.class.getResource("resource/help.png"));
	}

	public static boolean isEmbeddedFormCreationForbidden(IFieldInfo field) {
		return Boolean.TRUE.equals(field.getSpecificProperties().get(
				DO_NOT_CREATE_EMBEDDED_FORM_PROPRTY_KEY));
	}

	public static IFieldInfo forbidEmbeddedFormCreation(IFieldInfo field) {
		return new FieldInfoProxy(field) {
			@Override
			public Map<String, Object> getSpecificProperties() {
				Map<String, Object> result = new HashMap<String, Object>(
						super.getSpecificProperties());
				result.put(DO_NOT_CREATE_EMBEDDED_FORM_PROPRTY_KEY,
						Boolean.TRUE);
				return result;
			}
		};
	}

	public static IFieldInfo preventRecursiveEmbeddedForm(
			final ReflectionUI reflectionUI, IFieldInfo field) {
		return new FieldInfoProxy(field) {

			@Override
			public Object getValue(Object object) {
				Object result = super.getValue(object);
				if (result != null) {
					ITypeInfo resultType = reflectionUI
							.getTypeInfo(reflectionUI.getTypeInfoSource(result));
					resultType = new TypeInfoProxyConfiguration() {

						@Override
						protected List<IFieldInfo> getFields(ITypeInfo type) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>();
							for (IFieldInfo field : super.getFields(type)) {
								field = forbidEmbeddedFormCreation(field);
								result.add(field);
							}
							return result;
						}
					}.get(resultType);
					result = new PrecomputedTypeInfoInstanceWrapper(result,
							resultType);
				}
				return result;
			}

			@Override
			public void setValue(Object object, Object value) {
				if (value != null) {
					value = ((PrecomputedTypeInfoInstanceWrapper) value)
							.getInstance();
				}
				super.setValue(object, value);
			}

			@Override
			public ITypeInfo getType() {
				return PrecomputedTypeInfoInstanceWrapper.adaptPrecomputedType(super.getType());
			}
			
			

		};
	}

	public static IFieldInfo prepareEmbeddedFormCreation(
			ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		if (!isEmbeddedFormCreationForbidden(field)) {
			Object fieldValue = field.getValue(object);
			final ITypeInfo fieldValueType = reflectionUI
					.getTypeInfo(reflectionUI.getTypeInfoSource(fieldValue));
			if ((fieldValueType.getFields().size() + fieldValueType
					.getMethods().size() / 3) <= 4) {
				field = preventRecursiveEmbeddedForm(reflectionUI, field);
			}else{
				field = forbidEmbeddedFormCreation(field);
			}
		}
		return field;
	}

}
