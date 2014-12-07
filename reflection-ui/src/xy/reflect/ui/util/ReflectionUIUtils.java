package xy.reflect.ui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedConstructor;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.classmate.members.ResolvedMethod;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ModificationStack;
import xy.reflect.ui.info.ICommonInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.IListTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class ReflectionUIUtils {

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
			KeyEvent ke = new KeyEvent(c, KeyEvent.KEY_PRESSED,
					System.currentTimeMillis(), InputEvent.CTRL_MASK,
					KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED);
			c.dispatchEvent(ke);
		} catch (Throwable e1) {
			throw new AssertionError(e1);
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

	public static String writeMethodSignature(Method method) {
		return method.getReturnType() + " " + method.getName() + "("
				+ Arrays.toString(method.getParameterTypes()) + ")";
	}

	public static String identifierToCaption(String id) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		for (char c : id.toCharArray()) {
			if (i == 0) {
				result.append(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c)) {
				result.append(" " + c);
			} else {
				result.append(c);
			}
			i++;
		}
		return result.toString();
	}

	public static void adjustWindowBounds(Window window) {
		Rectangle bounds = window.getBounds();
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();
		if (bounds.width < maxBounds.width / 2) {
			bounds.grow((maxBounds.width / 2 - bounds.width) / 2, 0);
		}
		bounds = maxBounds.intersection(bounds);
		window.setBounds(bounds);
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
	
	public static IMethodInfo getNParametersMethod(List<IMethodInfo> methods, int n) {
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

	public static <T extends ICommonInfo> T findInfoByName(List<T> infos,
			String name) {
		for (T info : infos) {
			if (info.getName().equals(name)) {
				return info;
			}
		}
		return null;
	}

	public static <T extends ICommonInfo> T findInfoByCaption(List<T> infos,
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
		JPanel form = findAncestorForm(component, reflectionUI);
		if (form == null) {
			return ModificationStack.NULL_MODIFICATION_STACK;
		}
		return reflectionUI.getModificationStackByForm().get(form);
	}

	public static JPanel findAncestorForm(Component component,
			ReflectionUI reflectionUI) {
		Component parent;
		while ((parent = component.getParent()) != null) {
			if (reflectionUI.getObjectByForm().keySet().contains(parent)) {
				return (JPanel) parent;
			}
			component = parent;
		}
		return null;
	}

	public static void updateLayout(Component c) {
		Component root = SwingUtilities.getRoot(c);
		if (root != null) {
			root.validate();
		}

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

	public static List<Class<?>> getJavaTypeParameters(final Class<?> clazz,
			final Member ofMember, Class<?> parameterizedClass) {
		TypeResolver typeResolver = new TypeResolver();
		ResolvedType resolvedType = null;
		if (ofMember == null) {
			resolvedType = typeResolver.resolve(clazz);
		} else {
			MemberResolver memberResolver = new MemberResolver(typeResolver);
			ResolvedType declaringResolvedType = typeResolver.resolve(ofMember
					.getDeclaringClass());
			ResolvedTypeWithMembers arrayListTypeWithMembers = memberResolver
					.resolve(declaringResolvedType, null, null);
			if (ofMember instanceof Field) {
				for (ResolvedField resolvedField : arrayListTypeWithMembers
						.getMemberFields()) {
					if (resolvedField.getRawMember().equals(ofMember)) {
						resolvedType = resolvedField.getType();
						break;
					}
				}
			} else if (ofMember instanceof Method) {
				for (ResolvedMethod resolvedMethod : arrayListTypeWithMembers
						.getMemberMethods()) {
					if (resolvedMethod.getRawMember().equals(ofMember)) {
						resolvedType = resolvedMethod.getType();
						break;
					}
				}
			} else if (ofMember instanceof Constructor) {
				for (ResolvedConstructor resolvedConstructor : arrayListTypeWithMembers
						.getConstructors()) {
					if (resolvedConstructor.getRawMember().equals(ofMember)) {
						resolvedType = resolvedConstructor.getType();
						break;
					}
				}
			} else {
				throw new AssertionError();
			}
			if (resolvedType == null) {
				throw new AssertionError();
			}
		}
		List<Class<?>> result = new ArrayList<Class<?>>();
		List<ResolvedType> resolvedTypeParameters = resolvedType
				.typeParametersFor(parameterizedClass);
		if (resolvedTypeParameters == null) {
			return null;
		}
		for (ResolvedType classParameter : resolvedTypeParameters) {
			result.add(classParameter.getErasedType());
		}
		return result;
	}

	public static Class<?> getJavaTypeParameter(final Class<?> clazz,
			final Member ofMember, Class<?> parameterizedClass, int index) {
		List<Class<?>> parameterClasses = getJavaTypeParameters(clazz,
				ofMember, parameterizedClass);
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
		if (reflectionUI.isAtomicValue(object)) {
			return true;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object));
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
		if (reflectionUI.isAtomicValue(object)) {
			return object;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object));
		Object copy;
		if (type instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type;
			List<?> standardList = listType.toStandardList(object);
			List<Object> standardListCopy = new ArrayList<Object>();
			for (Object item : standardList) {
				standardListCopy.add(copyAccordingInfos(reflectionUI, item));
			}
			copy = listType.fromStandardList(standardListCopy);
		} else {
			IMethodInfo ctor = ReflectionUIUtils
					.getZeroParameterConstrucor(type);
			if (ctor == null) {
				throw new AssertionError("Cannot copy object of type '" + type
						+ "': zero parameter constructor not found");
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
		if (reflectionUI.isAtomicValue(object1)) {
			return object1.equals(object2);
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object1));
		if (!type.equals(reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(object2)))) {
			return false;
		}
		if (type instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type;
			List<?> standardList1 = listType.toStandardList(object1);
			List<?> standardList2 = listType.toStandardList(object2);
			if (standardList1.size() != standardList2.size()) {
				return false;
			}
			for (int i = 0; i < standardList1.size(); i++) {
				Object item1 = standardList1.get(i);
				Object item2 = standardList2.get(i);
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


}
