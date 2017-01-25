package xy.reflect.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.IReflectionUI;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
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
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;

@SuppressWarnings("unused")
public class SwingRendererUtils {

	public static final ImageIcon ERROR_ICON = new ImageIcon(IReflectionUI.class.getResource("resource/error.png"));
	public static final ImageIcon HELP_ICON = new ImageIcon(IReflectionUI.class.getResource("resource/help.png"));
	public static final ImageIcon DETAILS_ICON = new ImageIcon(IReflectionUI.class.getResource("resource/details.png"));
	public static final ImageIcon ADD_ICON = new ImageIcon(IReflectionUI.class.getResource("resource/add.png"));
	public static final ImageIcon REMOVE_ICON = new ImageIcon(IReflectionUI.class.getResource("resource/remove.png"));
	public static final ImageIcon UP_ICON = new ImageIcon(IReflectionUI.class.getResource("resource/up.png"));
	public static final ImageIcon DOWN_ICON = new ImageIcon(IReflectionUI.class.getResource("resource/down.png"));
	public static final ImageIcon CUSTOMIZATION_ICON = new ImageIcon(
			IReflectionUI.class.getResource("resource/custom.png"));
	public static final ImageIcon SAVE_ALL_ICON = new ImageIcon(
			IReflectionUI.class.getResource("resource/save-all.png"));

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
		image = image.getScaledInstance(imageBoundsInBox.width, imageBoundsInBox.height, scaleQuality);
		g.drawImage(image, imageBoundsInBox.x, imageBoundsInBox.y, null);
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

	public static IFieldInfo getControlFormAwareField(FieldControlPlaceHolder fieldControlPlaceHolder, SwingRenderer swingRenderer) {
		if (fieldControlPlaceHolder == null) {
			return null;
		}
		return fieldControlPlaceHolder.getFormAwareField();
	}

	public static ModificationStack findParentFormModificationStack(Component component, SwingRenderer swingRenderer) {
		JPanel form = findParentForm(component, swingRenderer);
		if (form == null) {
			return null;
		}
		return swingRenderer.getModificationStackByForm().get(form);
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
		return new ImageIcon(IReflectionUI.class.getResource("resource/help.png"));
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
		return SwingRendererUtils.getIconImageFromProperties(DesktopSpecificProperty.accessInfoProperties(info));
	}

	public static Object invokeMethodAndAllowToUndo(Object object, IMethodInfo method, InvocationData invocationData,
			ModificationStack stack) {
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
					stack.apply(modif);
				} catch (Throwable t) {
					stack.invalidate();
					throw new ReflectionUIError(t);
				}
				return resultHolder[0];
			} else {
				Object result = method.invoke(object, invocationData);
				stack.invalidate();
				return result;
			}
		}
	}

	public static boolean isFormEmpty(ITypeInfo type, IInfoFilter infoFilter,
			SwingRenderer swingRenderer) {
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

	public static boolean isObjectDisplayEmpty(Object value, IInfoFilter infoFilter,
			SwingRenderer swingRenderer) {
		IReflectionUI reflectionUI = swingRenderer.getReflectionUI();
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
		IReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, fieldType);
		Object encapsulatedValue = encapsulation.getInstance(new Object[] { fieldValue });
		ITypeInfo valueAsFieldType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(encapsulatedValue));
		IFieldInfo field = valueAsFieldType.getFields().get(0);
		return swingRenderer.hasCustomFieldControl(encapsulatedValue, field);
	}

	public static boolean isForm(Component c, SwingRenderer swingRenderer) {
		return swingRenderer.getObjectByForm().keySet().contains(c);
	}

	public static Image getIconImageFromProperties(Map<String, Object> properties) {
		Image result;
		URL imageUrl;
		String imagePath = (String) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH);
		String pathKind = (String) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
		if (imagePath == null) {
			return null;
		}
		if (DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
			imageUrl = SwingRendererUtils.class.getClassLoader().getResource(imagePath);
		} else {
			try {
				imageUrl = new File(imagePath).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new ReflectionUIError(e);
			}
		}
		result = DesktopSpecificProperty.iconImageCache.get(imagePath);
		if (result == null) {
			try {
				result = ImageIO.read(imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
				result = DesktopSpecificProperty.NULL_ICON_IMAGE;
			}
			DesktopSpecificProperty.iconImageCache.put(imagePath, result);
		}
		if (result == DesktopSpecificProperty.NULL_ICON_IMAGE) {
			return null;
		}
		return result;
	}

	public static Image getIconImage(Map<String, Object> properties) {
		Image result;
		result = (Image) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE);
		if (result != null) {
			return result;
		}
		URL imageUrl;
		String imagePath = (String) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH);
		String pathKind = (String) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
		if (imagePath == null) {
			return null;
		}
		if (DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
			imageUrl = SwingRendererUtils.class.getClassLoader().getResource(imagePath);
		} else {
			try {
				imageUrl = new File(imagePath).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new ReflectionUIError(e);
			}
		}
		result = DesktopSpecificProperty.iconImageCache.get(imagePath);
		if (result == null) {
			try {
				result = ImageIO.read(imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
				result = DesktopSpecificProperty.NULL_ICON_IMAGE;
			}
			DesktopSpecificProperty.iconImageCache.put(imagePath, result);
		}
		if (result == DesktopSpecificProperty.NULL_ICON_IMAGE) {
			return null;
		}
		return result;
	}

	public static void setIconImage(Map<String, Object> properties, Image image) {
		properties.put(DesktopSpecificProperty.KEY_ICON_IMAGE, image);
	}

	public static void forwardSubModifications(final IReflectionUI reflectionUI, final JPanel subForm,
			final Accessor<Boolean> childModifAcceptedGetter,
			final Accessor<ValueReturnMode> childValueReturnModeGetter, final Accessor<Boolean> childValueNewGetter,
			final Accessor<IModification> commitModifGetter, final Accessor<IInfo> childModifTargetGetter,
			final Accessor<String> parentModifTitleGetter, final Accessor<ModificationStack> parentModifStackGetter,
			final SwingRenderer swingRenderer) {
		final ModificationStack subFormModifStack = swingRenderer.getModificationStackByForm().get(subForm);
		swingRenderer.getModificationStackByForm().put(subForm, new ModificationStack("Forward Sub-Modifications To " + subForm.toString()) {

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
					subModifTitle = ReflectionUIUtils.composeTitle(parentModifTitle, subModifTitle);
				}
				ModificationStack parentModifStack = parentModifStackGetter.get();
				IInfo childModifTarget = childModifTargetGetter.get();
				return ReflectionUIUtils.integrateSubModifications(reflectionUI, parentModifStack, childModifStack,
						childModifAccepted, childValueReturnMode, childValueNew, commitModif, childModifTarget,
						subModifTitle);
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
}
