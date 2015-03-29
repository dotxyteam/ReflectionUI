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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.annotation.Category;
import xy.reflect.ui.info.annotation.Documentation;
import xy.reflect.ui.info.annotation.Validating;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.IListTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.TypeInfoProxyConfiguration;
import xy.reflect.ui.undo.ModificationStack;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedConstructor;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.DefaultParanamer;
import com.thoughtworks.paranamer.JavadocParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class ReflectionUIUtils {

	public static final String[] NEW_LINE_SEQUENCES = new String[] { "\r\n",
			"\n", "\r" };

	public static final Icon ERROR_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/error.png"));
	public static final Icon HELP_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/help.png"));

	public static Class<?> primitiveToWrapperType(Class<?> class1) {
		if (class1 == int.class) {
			return Integer.class;
		} else if (class1 == short.class) {
			return Short.class;
		} else if (class1 == int.class) {
			return Integer.class;
		} else if (class1 == long.class) {
			return Long.class;
		} else if (class1 == float.class) {
			return Float.class;
		} else if (class1 == double.class) {
			return Double.class;
		} else if (class1 == char.class) {
			return Character.class;
		} else if (class1 == boolean.class) {
			return Boolean.class;
		} else {
			return null;
		}
	}

	public static Class<?> wrapperToPrimitiveType(Class<?> class1) {
		if (class1 == Integer.class) {
			return int.class;
		} else if (class1 == Short.class) {
			return short.class;
		} else if (class1 == Integer.class) {
			return int.class;
		} else if (class1 == Long.class) {
			return long.class;
		} else if (class1 == Float.class) {
			return float.class;
		} else if (class1 == Double.class) {
			return double.class;
		} else if (class1 == Character.class) {
			return char.class;
		} else if (class1 == Boolean.class) {
			return boolean.class;
		} else {
			return null;
		}
	}

	public static List<Class<?>> getAncestorClasses(Class<?> type) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		while (type.getSuperclass() != null) {
			result.add(type.getSuperclass());
			type = type.getSuperclass();
		}
		return result;
	}

	public static Set<Class<?>> getAncestorClassesAndInterfaces(Class<?> type) {
		Set<Class<?>> result = new HashSet<Class<?>>();
		List<Class<?>> ancestorClasses = getAncestorClasses(type);
		result.addAll(ancestorClasses);
		result.addAll(getSuperInterfaces(type.getInterfaces()));
		for (Class<?> ancestor : ancestorClasses) {
			result.addAll(getSuperInterfaces(ancestor.getInterfaces()));
		}
		return result;
	}

	public static Set<Class<?>> getAncestorsAndSelfClassesAndInterfaces(
			Class<?> type) {
		Set<Class<?>> result = new HashSet<Class<?>>(
				getAncestorClassesAndInterfaces(type));
		result.add(type);
		return result;
	}

	public static Set<Class<?>> getSuperInterfaces(Class<?>[] childInterfaces) {
		Set<Class<?>> allInterfaces = new HashSet<Class<?>>();
		for (int i = 0; i < childInterfaces.length; i++) {
			allInterfaces.add(childInterfaces[i]);
			allInterfaces.addAll(getSuperInterfaces(childInterfaces[i]
					.getInterfaces()));
		}
		return allInterfaces;
	}

	public static boolean isPrimitiveTypeOrWrapperOrString(Class<?> class1) {
		return (class1 == String.class) || isPrimitiveTypeOrWrapper(class1);
	}

	public static boolean isPrimitiveTypeOrWrapper(Class<?> class1) {
		if (class1.isArray()) {
			return false;
		} else if (primitiveToWrapperType(class1) != null) {
			return true;
		} else if (wrapperToPrimitiveType(class1) != null) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isPrimitiveWrapper(Class<?> class1) {
		return wrapperToPrimitiveType(class1) != null;
	}

	public static class PrimitiveDefaults {
		protected static boolean DEFAULT_BOOLEAN;
		protected static byte DEFAULT_BYTE;
		protected static short DEFAULT_SHORT;
		protected static int DEFAULT_INT;
		protected static long DEFAULT_LONG;
		protected static float DEFAULT_FLOAT;
		protected static double DEFAULT_DOUBLE;
		protected static char DEFAULT_CHAR;

		public static Object get(Class<?> clazz) {
			if (clazz.equals(boolean.class)) {
				return DEFAULT_BOOLEAN;
			} else if (clazz.equals(byte.class)) {
				return DEFAULT_BYTE;
			} else if (clazz.equals(short.class)) {
				return DEFAULT_SHORT;
			} else if (clazz.equals(int.class)) {
				return DEFAULT_INT;
			} else if (clazz.equals(long.class)) {
				return DEFAULT_LONG;
			} else if (clazz.equals(float.class)) {
				return DEFAULT_FLOAT;
			} else if (clazz.equals(double.class)) {
				return DEFAULT_DOUBLE;
			} else if (clazz.equals(char.class)) {
				return DEFAULT_CHAR;
			} else {
				throw new IllegalArgumentException("Class type " + clazz
						+ " not supported");
			}
		}
	}

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

	public static boolean equalsOrBothNull(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	public static String truncateNicely(String string, int maximumLength) {
		if (string.length() <= maximumLength) {
			return string;
		} else {
			return string.substring(0, maximumLength - 3) + "...";
		}
	}

	public static <T> Set<T> getIntersection(Set<T> s1, Set<T> s2) {
		HashSet<T> result = new HashSet<T>(s1);
		result.retainAll(s2);
		return result;
	}

	public static void setRecursivelyEnabled(Component c, boolean b) {
		c.setEnabled(b);
		if (c instanceof Container) {
			for (Component child : ((Container) c).getComponents()) {
				setRecursivelyEnabled(child, b);
			}
		}
	}

	public static String getJavaMethodSignature(Method method) {
		return method.getReturnType() + " " + method.getName() + "("
				+ Arrays.toString(method.getParameterTypes()) + ")";
	}

	public static String getMethodInfoSignature(IMethodInfo method) {
		return method.getReturnValueType() + " " + method.getName() + "("
				+ ReflectionUIUtils.stringJoin(method.getParameters(), ", ")
				+ ")";
	}

	public static String identifierToCaption(String id) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		char lastC = 0;
		for (char c : id.toCharArray()) {
			if (i == 0) {
				result.append(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c)
					&& !Character.isUpperCase(lastC)) {
				result.append(" " + c);
			} else if (Character.isDigit(c) && !Character.isDigit(lastC)) {
				result.append(" " + c);
			} else if (!Character.isLetterOrDigit(c)
					&& Character.isLetterOrDigit(lastC)) {
				result.append(" " + c);
			} else {
				result.append(c);
			}
			lastC = c;
			i++;
		}
		return result.toString();
	}

	public static String getPrintedStackTrace(Throwable e) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(out));
		return out.toString();
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

	public static IMethodInfo getZeroParameterConstrucor(ITypeInfo type) {
		return getNParametersMethod(type.getConstructors(), 0);
	}

	public static IMethodInfo getNParametersMethod(List<IMethodInfo> methods,
			int n) {
		for (IMethodInfo c : methods) {
			if (c.getParameters().size() == n) {
				return c;
			}
		}
		return null;
	}

	public static <K, V> List<K> getKeysFromValue(Map<K, V> map, Object value) {
		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (ReflectionUIUtils.equalsOrBothNull(entry.getValue(), value)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public static boolean hasFileNameExtension(String fileName,
			String[] extensions) {
		for (String ext : extensions) {
			if (ext.toLowerCase().equals(
					getFileNameExtension(fileName).toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static String getFileNameExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf(".");
		if (lastDotIndex == -1) {
			return "";
		} else {
			return fileName.substring(lastDotIndex + 1);
		}
	}

	public static <T extends IInfo> T findInfoByName(List<T> infos, String name) {
		for (T info : infos) {
			if (info.getName().equals(name)) {
				return info;
			}
		}
		return null;
	}

	public static <T extends IInfo> T findInfoByCaption(List<T> infos,
			String caption) {
		for (T info : infos) {
			if (info.getCaption().equals(caption)) {
				return info;
			}
		}
		return null;
	}

	public static ModificationStack findModificationStack(Component component,
			ReflectionUI reflectionUI) {
		JPanel form = findForm(component, reflectionUI);
		if (form == null) {
			return ModificationStack.NULL_MODIFICATION_STACK;
		}
		return reflectionUI.getModificationStackByForm().get(form);
	}

	public static JPanel findForm(Component component, ReflectionUI reflectionUI) {
		while (component != null) {
			if (reflectionUI.getObjectByForm().keySet().contains(component)) {
				return (JPanel) component;
			}
			component = component.getParent();
		}
		return null;
	}

	public static String changeCase(String result, boolean upperElseLower,
			int subStringStart, int subStringEnd) {
		String subString = result.substring(subStringStart, subStringEnd);
		if (upperElseLower) {
			subString = subString.toUpperCase();
		} else {
			subString = subString.toLowerCase();
		}
		return result.substring(0, subStringStart) + subString
				+ result.substring(subStringEnd);
	}

	public static List<Class<?>> getJavaTypeParameters(final Class<?> type,
			final Member ofMember, Class<?> parameterizedBaseClass) {
		TypeResolver typeResolver = new TypeResolver();
		ResolvedType resolvedType = null;
		if (ofMember == null) {
			resolvedType = typeResolver.resolve(type);
		} else {
			MemberResolver memberResolver = new MemberResolver(typeResolver);
			ResolvedType declaringResolvedType = typeResolver.resolve(ofMember
					.getDeclaringClass());
			ResolvedTypeWithMembers resolvedTypeWithMembers = memberResolver
					.resolve(declaringResolvedType, null, null);
			if (ofMember instanceof Field) {
				for (ResolvedField resolvedField : resolvedTypeWithMembers
						.getMemberFields()) {
					if (resolvedField.getRawMember().equals(ofMember)) {
						resolvedType = resolvedField.getType();
						break;
					}
				}
			} else if (ofMember instanceof Method) {
				ResolvedMethod[] resolvedMethods;
				if (Modifier.isStatic(ofMember.getModifiers())) {
					resolvedMethods = resolvedTypeWithMembers
							.getStaticMethods();
				} else {
					resolvedMethods = resolvedTypeWithMembers
							.getMemberMethods();
				}
				for (ResolvedMethod resolvedMethod : resolvedMethods) {
					if (resolvedMethod.getRawMember().equals(ofMember)) {
						resolvedType = resolvedMethod.getType();
						break;
					}
				}
			} else if (ofMember instanceof Constructor) {
				for (ResolvedConstructor resolvedConstructor : resolvedTypeWithMembers
						.getConstructors()) {
					if (resolvedConstructor.getRawMember().equals(ofMember)) {
						resolvedType = resolvedConstructor.getType();
						break;
					}
				}
			} else {
				throw new ReflectionUIError();
			}
			if (resolvedType == null) {
				throw new ReflectionUIError();
			}
		}
		List<Class<?>> result = new ArrayList<Class<?>>();
		List<ResolvedType> resolvedTypeParameters = resolvedType
				.typeParametersFor(parameterizedBaseClass);
		if (resolvedTypeParameters == null) {
			return null;
		}
		for (ResolvedType classParameter : resolvedTypeParameters) {
			result.add(classParameter.getErasedType());
		}
		return result;
	}

	public static Class<?> getJavaTypeParameter(final Class<?> type,
			final Member ofMember, Class<?> parameterizedBaseClass, int index) {
		List<Class<?>> parameterClasses = getJavaTypeParameters(type, ofMember,
				parameterizedBaseClass);
		if (parameterClasses == null) {
			return null;
		}
		if (parameterClasses.size() <= index) {
			return null;
		}
		return parameterClasses.get(index);
	}

	public static boolean canCopyAccordingInfos(ReflectionUI reflectionUI,
			Object object) {
		if (object == null) {
			return true;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object));
		if (type.isImmutable()) {
			return true;
		}
		IMethodInfo ctor = ReflectionUIUtils.getZeroParameterConstrucor(type);
		if (ctor == null) {
			return false;
		}
		for (IFieldInfo field : type.getFields()) {
			Object fieldVaue = field.getValue(object);
			if (field.isReadOnly()) {
				continue;
			}
			if (!canCopyAccordingInfos(reflectionUI, fieldVaue)) {
				return false;
			}
		}
		return true;
	}

	public static Object copyAccordingInfos(ReflectionUI reflectionUI,
			Object object) {
		if (object == null) {
			return null;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object));
		if (type.isImmutable()) {
			return object;
		}
		Object copy;
		if (type instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type;
			Object[] listValue = listType.toListValue(object);
			Object[] listValueCopy = new Object[listValue.length];
			for (int i = 0; i < listValue.length; i++) {
				Object item = listValue[i];
				listValueCopy[i] = copyAccordingInfos(reflectionUI, item);
			}
			copy = listType.fromListValue(listValueCopy);
		} else {
			IMethodInfo ctor = ReflectionUIUtils
					.getZeroParameterConstrucor(type);
			if (ctor == null) {
				throw new ReflectionUIError("Cannot copy object of type '"
						+ type + "': zero parameter constructor not found");
			}
			copy = ctor.invoke(null, Collections.<String, Object> emptyMap());

		}
		for (IFieldInfo field : type.getFields()) {
			if (field.isReadOnly()) {
				continue;
			}
			Object fieldValue = field.getValue(object);
			field.setValue(copy, copyAccordingInfos(reflectionUI, fieldValue));
		}
		return copy;
	}

	public static boolean equalsAccordingInfos(ReflectionUI reflectionUI,
			Object object1, Object object2) {
		if (object1 == null) {
			return object2 == null;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object1));
		if (type.isImmutable()) {
			return object1.equals(object2);
		}
		if (!type.equals(reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object2)))) {
			return false;
		}
		if (type instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type;
			Object[] listValue1 = listType.toListValue(object1);
			Object[] listValue2 = listType.toListValue(object2);
			if (listValue1.length != listValue2.length) {
				return false;
			}
			for (int i = 0; i < listValue1.length; i++) {
				Object item1 = listValue1[i];
				Object item2 = listValue2[i];
				if (!equalsAccordingInfos(reflectionUI, item1, item2)) {
					return false;
				}
			}
		}
		for (IFieldInfo field : type.getFields()) {
			Object fieldVaue1 = field.getValue(object1);
			Object fieldVaue2 = field.getValue(object2);
			if (!equalsAccordingInfos(reflectionUI, fieldVaue1, fieldVaue2)) {
				return false;
			}
		}
		return true;
	}

	public static String toStringAccordingInfos(ReflectionUI reflectionUI,
			Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object));
		if (type.isImmutable()) {
			return type.toString(object);
		}
		StringBuilder result = new StringBuilder();
		result.append(type.getCaption());
		result.append(" (\n");
		for (int i = 0; i < type.getFields().size(); i++) {
			if (i > 0) {
				result.append(",\n");
			}
			IFieldInfo field = type.getFields().get(i);
			result.append(indentLines(
					field.getCaption()
							+ " = "
							+ toStringAccordingInfos(reflectionUI,
									field.getValue(object)), "\t"));
		}
		result.append("\n)");
		return result.toString();
	}

	public static String[] splitLines(String s) {
		if (s.length() == 0) {
			return new String[0];
		}
		return s.split(getNewLineRegex(), -1);
	}

	public static String getNewLineRegex() {
		return stringJoin(Arrays.asList(NEW_LINE_SEQUENCES), "|");
	}

	public static Object indentLines(String s, String tabulation) {
		String[] lines = splitLines(s);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				result.append("\n");
			}
			String line = lines[i];
			result.append(tabulation + line);
		}
		return result.toString();
	}

	public static String stringJoin(List<?> list, String separator) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			Object item = list.get(i);
			if (i > 0) {
				result.append(separator);
			}
			if (item == null) {
				result.append("null");
			} else {
				result.append(item.toString());
			}
		}
		return result.toString();
	}

	public static int getStandardCharacterWidth(Component c) {
		return c.getFontMetrics(c.getFont()).charWidth('a');
	}

	public static Color fixSeveralColorRenderingIssues(Color color) {
		return new Color(color.getRGB());
	}

	public static String[] getJavaParameterNames(Member owner) {
		try {
			URL jdkJavadocURL = ReflectionUI.class
					.getResource("resource/jdk-apidocs");
			Paranamer paranamer;
			paranamer = new AdaptiveParanamer(new DefaultParanamer(),
					new BytecodeReadingParanamer(), new JavadocParanamer(
							jdkJavadocURL));
			String[] parameterNames = paranamer.lookupParameterNames(
					(AccessibleObject) owner, false);
			if ((parameterNames == null) || (parameterNames.length == 0)) {
				return null;
			}
			return parameterNames;
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
	}

	public static void transferStream(InputStream inputStream,
			OutputStream outputStream) throws IOException {
		int read = 0;
		byte[] bytes = new byte[1024];
		while ((read = inputStream.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);
		}
	}

	public static String formatParameterList(List<IParameterInfo> parameters) {
		StringBuilder result = new StringBuilder();
		int iParam = 0;
		for (IParameterInfo param : parameters) {
			if (iParam > 0) {
				if (iParam == parameters.size() - 1) {
					result.append(" and ");
				} else {
					result.append(", ");
				}
			}
			result.append(param.getCaption());
			iParam++;
		}
		return result.toString();
	}

	public static InfoCategory getAnnotatedInfoCategory(
			AnnotatedElement annotated) {
		Category annotation = annotated.getAnnotation(Category.class);
		if (annotation == null) {
			return null;
		}
		return new InfoCategory(annotation.value(), annotation.position());
	}

	public static String getAnnotatedInfoDocumentation(
			AnnotatedElement annotated) {
		Documentation annotation = annotated.getAnnotation(Documentation.class);
		if (annotation == null) {
			return null;
		}
		return annotation.value();
	}

	public static String escapeHTML(String string, boolean preserveNewLines) {
		StringBuffer sb = new StringBuffer(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				//
				// HTML Special Chars
				if (c == '"')
					sb.append("&quot;");
				else if (c == '&')
					sb.append("&amp;");
				else if (c == '<')
					sb.append("&lt;");
				else if (c == '>')
					sb.append("&gt;");
				else if (c == '\n')
					// Handle Newline
					if (preserveNewLines) {
						sb.append("<br/>");
					} else {
						sb.append(c);
					}
				else {
					int ci = 0xffff & c;
					if (ci < 160)
						// nothing special only 7 Bit
						sb.append(c);
					else {
						// Not 7 Bit use the unicode system
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
					}
				}
			}
		}
		return sb.toString();
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

	public static Color getDisabledTextBackgroundColor() {
		return new JPanel().getBackground();
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

	public static List<Field> getALlFields(Class<?> type) {
		List<Field> result = new ArrayList<Field>();
		Class<?> currentType = type;
		while (currentType != null && currentType != Object.class) {
			result.addAll(Arrays.asList(currentType.getDeclaredFields()));
			currentType = currentType.getSuperclass();
		}
		return result;
	}

	public static Icon getHelpIcon() {
		return new ImageIcon(
				ReflectionUI.class.getResource("resource/help.png"));
	}

	public static List<Method> geAnnotatedtValidatingMethods(Class<?> javaType) {
		List<Method> result = new ArrayList<Method>();
		for (Method method : javaType.getMethods()) {
			Validating annotation = method.getAnnotation(Validating.class);
			if (annotation != null) {
				if (method.getReturnType() != void.class) {
					throw new ReflectionUIError(
							"Invalid validating method, the return type is not 'void': "
									+ method);
				}
				if (method.getParameterTypes().length > 0) {
					throw new ReflectionUIError(
							"Invalid validating method, the number of parameters is not 0: "
									+ method);
				}
				result.add(method);
			}
		}
		return result;
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

	public static String multiToSingleLine(String s) {
		return s.replaceAll("\\r\\n|\\n|\\r", " ");
	}

	public static boolean isJavaClassMainMethod(Method javaMethod) {
		if (Modifier.isStatic(javaMethod.getModifiers())) {
			if (javaMethod.getReturnType().equals(void.class)) {
				if (javaMethod.getName().equals("main")) {
					Class<?>[] paramTypes = javaMethod.getParameterTypes();
					if (paramTypes.length == 1) {
						if (paramTypes[0].equals(String[].class)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static Object wrapValueAsField(ReflectionUI reflectionUI,
			final Object[] valueArray, final String fieldCaption,
			final String containingTypeCaption, final boolean readOnly) {
		final ITypeInfo valueTypeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(valueArray[0]));
		return new PrecomputedTypeInfoInstanceWrapper(valueArray[0],
				new TypeInfoProxyConfiguration() {

					@Override
					protected List<IFieldInfo> getFields(ITypeInfo type) {
						return Collections
								.<IFieldInfo> singletonList(new IFieldInfo() {

									@Override
									public void setValue(Object object,
											Object value) {
										valueArray[0] = value;
									}

									@Override
									public boolean isReadOnly() {
										return readOnly;
									}

									@Override
									public boolean isNullable() {
										return false;
									}

									@Override
									public Object getValue(Object object) {
										return valueArray[0];
									}

									@Override
									public ITypeInfo getType() {
										return valueTypeInfo;
									}

									@Override
									public String getName() {
										return "";
									}

									@Override
									public String getCaption() {
										return fieldCaption;
									}

									@Override
									public InfoCategory getCategory() {
										return null;
									}

									@Override
									public String getDocumentation() {
										return null;
									}
								});
					}

					@Override
					protected List<IMethodInfo> getMethods(ITypeInfo type) {
						return Collections.emptyList();
					}

					@Override
					protected String getCaption(ITypeInfo type) {
						return containingTypeCaption;
					}

				}.get(valueTypeInfo));
	}

	public static <T extends Comparable<T>> int compareNullables(T c1, T c2) {
		if (c1 == null) {
			if (c2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (c2 == null) {
				return 1;
			} else {
				return c1.compareTo(c2);
			}
		}
	}

}
