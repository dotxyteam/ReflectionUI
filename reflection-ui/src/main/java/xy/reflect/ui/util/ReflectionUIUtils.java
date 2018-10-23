package xy.reflect.ui.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.DefaultParanamer;
import com.thoughtworks.paranamer.Paranamer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.plugin.ICustomizableFieldControlPlugin;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractMenuItem;
import xy.reflect.ui.info.menu.IMenuElement;
import xy.reflect.ui.info.menu.IMenuElementPosition;
import xy.reflect.ui.info.menu.Menu;
import xy.reflect.ui.info.menu.MenuElementKind;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;

public class ReflectionUIUtils {

	public static final String[] NEW_LINE_SEQUENCES = new String[] { "\r\n", "\n", "\r" };
	public static final String METHOD_SIGNATURE_REGEX = "(\\s*[^ ]+\\s*)(\\s+[^ ]+\\s*)?\\(([^\\)]*)\\)\\s*";

	public static <BASE, C extends BASE> List<BASE> convertCollection(Collection<C> ts) {
		List<BASE> result = new ArrayList<BASE>();
		for (C t : ts) {
			result.add((BASE) t);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <BASE, C extends BASE> List<C> convertCollectionUnsafely(Collection<BASE> bs) {
		List<C> result = new ArrayList<C>();
		for (BASE b : bs) {
			result.add((C) b);
		}
		return result;
	}

	public static File getCanonicalParent(File file) {
		try {
			return file.getCanonicalFile().getParentFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
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

	public static Set<Class<?>> getAncestorsAndSelfClassesAndInterfaces(Class<?> type) {
		Set<Class<?>> result = new HashSet<Class<?>>(getAncestorClassesAndInterfaces(type));
		result.add(type);
		return result;
	}

	public static Set<Class<?>> getSuperInterfaces(Class<?>[] childInterfaces) {
		Set<Class<?>> allInterfaces = new HashSet<Class<?>>();
		for (int i = 0; i < childInterfaces.length; i++) {
			allInterfaces.add(childInterfaces[i]);
			allInterfaces.addAll(getSuperInterfaces(childInterfaces[i].getInterfaces()));
		}
		return allInterfaces;
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

	public static String buildMethodSignature(IMethodInfo method) {
		StringBuilder result = new StringBuilder();
		ITypeInfo returnType = method.getReturnValueType();
		result.append(((returnType == null) ? "void" : returnType.getName()));
		result.append(" " + method.getName() + "(");
		List<IParameterInfo> params = method.getParameters();
		for (int i = 0; i < params.size(); i++) {
			IParameterInfo param = params.get(i);
			if (i > 0) {
				result.append(", ");
			}
			result.append(param.getType().getName());
		}
		result.append(")");
		return result.toString();
	}

	public static String extractMethodReturnTypeNameFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String result = matcher.group(1);
		if (result != null) {
			result = result.trim();
		}
		return result;
	}

	public static String extractMethodNameFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String result = matcher.group(2);
		if (result != null) {
			result = result.trim();
		}
		return result;
	}

	public static String[] extractMethodParameterTypeNamesFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String paramListString = matcher.group(3);
		paramListString = paramListString.trim();
		List<String> result = new ArrayList<String>(Arrays.asList(paramListString.split("\\s*,\\s*")));
		result.removeAll(Collections.singletonList(""));
		return result.toArray(new String[result.size()]);
	}

	public static List<Parameter> getJavaParameters(Method javaMethod) {
		List<Parameter> result = new ArrayList<Parameter>();
		for (int i = 0; i < javaMethod.getParameterTypes().length; i++) {
			result.add(new Parameter(javaMethod, i));
		}
		return result;
	}

	public static List<Parameter> getJavaParameters(Constructor<?> ctor) {
		List<Parameter> result = new ArrayList<Parameter>();
		for (int i = 0; i < ctor.getParameterTypes().length; i++) {
			result.add(new Parameter(ctor, i));
		}
		return result;
	}

	public static String identifierToCaption(String id) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		char lastC = 0;
		for (char c : id.toCharArray()) {
			if (i == 0) {
				result.append(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c) && !Character.isUpperCase(lastC)) {
				result.append(" " + c);
			} else if (Character.isDigit(c) && !Character.isDigit(lastC)) {
				result.append(" " + c);
			} else if (!Character.isLetterOrDigit(c) && Character.isLetterOrDigit(lastC)) {
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

	public static IMethodInfo getNParametersMethod(List<IMethodInfo> methods, int n) {
		for (IMethodInfo c : methods) {
			if (c.getParameters().size() == n) {
				return c;
			}
		}
		return null;
	}

	public static IMethodInfo getZeroParameterMethod(List<IMethodInfo> methods) {
		return getNParametersMethod(methods, 0);
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

	public static <K, V> K getFirstKeyFromValue(Map<K, V> map, Object value) {
		List<K> list = getKeysFromValue(map, value);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	public static boolean hasFileNameExtension(String fileName, String[] extensions) {
		for (String ext : extensions) {
			if (ext.toLowerCase().equals(FileUtils.getFileNameExtension(fileName).toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static <T extends IMethodInfo> T findMethodBySignature(List<T> methods, String signature) {
		for (T method : methods) {
			String candidateMethodSignature = method.getSignature();
			if (candidateMethodSignature.equals(signature)) {
				return method;
			}
		}
		return null;
	}

	public static <T extends IInfo> T findInfoByName(List<T> infos, String name) {
		for (T info : infos) {
			if (info.getName().equals(name)) {
				return info;
			}
		}
		return null;
	}

	public static <T extends IInfo> T findInfoByCaption(List<T> infos, String caption) {
		for (T info : infos) {
			if (info.getCaption().equals(caption)) {
				return info;
			}
		}
		return null;
	}

	public static String changeCase(String result, boolean upperElseLower, int subStringStart, int subStringEnd) {
		String subString = result.substring(subStringStart, subStringEnd);
		if (upperElseLower) {
			subString = subString.toUpperCase();
		} else {
			subString = subString.toLowerCase();
		}
		return result.substring(0, subStringStart) + subString + result.substring(subStringEnd);
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

	public static <T> String stringJoin(T[] array, String separator) {
		return stringJoin(Arrays.asList(array), separator);
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

	public static String[] getJavaParameterNames(Member owner) {
		Paranamer paranamer = new AdaptiveParanamer(new DefaultParanamer(), new BytecodeReadingParanamer());
		String[] parameterNames;
		try {
			parameterNames = paranamer.lookupParameterNames((AccessibleObject) owner, false);
		} catch (Throwable t) {
			return null;
		}
		if ((parameterNames == null) || (parameterNames.length == 0)) {
			return null;
		}
		if (owner instanceof Constructor) {
			Constructor<?> ctor = (Constructor<?>) owner;
			Class<?> ctorClass = ctor.getDeclaringClass();
			if (ctorClass.isMemberClass()) {
				if (!Modifier.isStatic(ctorClass.getModifiers())) {
					if (parameterNames.length == (ctor.getParameterTypes().length - 1)) {
						List<String> tmpList = new ArrayList<String>(Arrays.asList(parameterNames));
						tmpList.add(0, "parent");
						parameterNames = tmpList.toArray(new String[tmpList.size()]);
					}
				}
			}
		}
		return parameterNames;
	}

	public static void transferStream(InputStream inputStream, OutputStream outputStream) throws IOException {
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
					result.append(" AND ");
				} else {
					result.append(", ");
				}
			}
			result.append(param.getCaption());
			iParam++;
		}
		return result.toString();
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

	public static List<Field> getALlFields(Class<?> type) {
		List<Field> result = new ArrayList<Field>();
		Class<?> currentType = type;
		while (currentType != null && currentType != Object.class) {
			result.addAll(Arrays.asList(currentType.getDeclaredFields()));
			currentType = currentType.getSuperclass();
		}
		return result;
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

	public static StackTraceElement[] createDebugStackTrace(int firstElementsToRemove) {
		StackTraceElement[] result = new Exception().getStackTrace();
		return Arrays.copyOfRange(result, 1 + firstElementsToRemove, result.length);
	}

	public static <M extends Member> M findJavaMemberByName(M[] members, String memberName) {
		for (M member : members) {
			if (member.getName().equals(memberName)) {
				return member;
			}
		}
		return null;
	}

	public static String getQualifiedName(Field field) {
		return field.getDeclaringClass().getName() + "#" + field.getName();
	}

	public static String getQualifiedName(Method method) {
		return method.getDeclaringClass().getName() + "#" + method.getName() + "("
				+ stringJoin(gatClassNames(method.getParameterTypes()), ",") + ")";
	}

	public static String getQualifiedName(Constructor<?> constructor) {
		return constructor.getDeclaringClass().getName() + "#" + constructor.getName() + "("
				+ stringJoin(gatClassNames(constructor.getParameterTypes()), ",") + ")";
	}

	public static List<String> gatClassNames(Class<?>[] classes) {
		List<String> paramTypeNames = new ArrayList<String>();
		for (Class<?> clazz : classes) {
			paramTypeNames.add(clazz.getName());
		}
		return paramTypeNames;
	}

	public static void sortFields(List<IFieldInfo> list) {
		Collections.sort(list, new Comparator<IFieldInfo>() {
			@Override
			public int compare(IFieldInfo f1, IFieldInfo f2) {
				int result;

				result = compareNullables(f1.getCategory(), f2.getCategory());
				if (result != 0) {
					return result;
				}

				result = compareNullables(f1.getType().getName().toUpperCase(), f2.getType().getName().toUpperCase());
				if (result != 0) {
					return result;
				}

				result = compareNullables(f1.getName(), f2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	public static void sortMethods(List<IMethodInfo> list) {
		Collections.sort(list, new Comparator<IMethodInfo>() {
			@Override
			public int compare(IMethodInfo m1, IMethodInfo m2) {
				int result;

				result = compareNullables(m1.getCategory(), m2.getCategory());
				if (result != 0) {
					return result;
				}

				List<String> parameterTypeNames1 = new ArrayList<String>();
				for (IParameterInfo param : m1.getParameters()) {
					parameterTypeNames1.add(param.getType().getName());
				}
				Collections.sort(parameterTypeNames1);
				List<String> parameterTypeNames2 = new ArrayList<String>();
				for (IParameterInfo param : m2.getParameters()) {
					parameterTypeNames2.add(param.getType().getName());
				}
				Collections.sort(parameterTypeNames2);
				result = stringJoin(parameterTypeNames1, "\n").compareTo(stringJoin(parameterTypeNames2, "\n"));
				if (result != 0) {
					return result;
				}

				String returnTypeName1;
				{
					if (m1.getReturnValueType() == null) {
						returnTypeName1 = "";
					} else {
						returnTypeName1 = m1.getReturnValueType().getName();
					}
				}
				String returnTypeName2;
				{
					if (m2.getReturnValueType() == null) {
						returnTypeName2 = "";
					} else {
						returnTypeName2 = m2.getReturnValueType().getName();
					}
				}
				result = compareNullables(returnTypeName1, returnTypeName2);
				if (result != 0) {
					return result;
				}

				result = compareNullables(m1.getName(), m2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	public static String composeMessage(String contextMessage, String localMessage) {
		if ((contextMessage == null) || (contextMessage.length() == 0)) {
			return localMessage;
		}
		return contextMessage + " - " + localMessage;
	}

	public static String getPrettyErrorMessage(Throwable t) {
		return new ReflectionUIError(t).toString();
	}

	public static boolean isOverridenBy(Method baseMethod, Method overridingMethod) {
		if (!baseMethod.getDeclaringClass().isAssignableFrom(overridingMethod.getDeclaringClass())) {
			return false;
		}
		if (!baseMethod.getName().equals(overridingMethod.getName())) {
			return false;
		}
		if (!baseMethod.getReturnType().isAssignableFrom(overridingMethod.getReturnType())) {
			return false;
		}
		Class<?>[] baseMethodParamTypes = baseMethod.getParameterTypes();
		Class<?>[] overridingMethodParamTypes = overridingMethod.getParameterTypes();
		if (baseMethodParamTypes.length != overridingMethodParamTypes.length) {
			return false;
		}
		for (int iParam = 0; iParam < baseMethodParamTypes.length; iParam++) {
			Class<?> baseMethodParamType = baseMethodParamTypes[iParam];
			Class<?> overridingMethodParamType = overridingMethodParamTypes[iParam];
			if (!baseMethodParamType.isAssignableFrom(overridingMethodParamType)) {
				return false;
			}
		}
		return true;
	}

	public static Object createDefaultInstance(ITypeInfo type, Object parentObject) {
		return createDefaultInstance(type, parentObject, true);
	}

	public static Object createDefaultInstance(ITypeInfo type, Object parentObject, boolean subTypeInstanceAllowed) {
		try {
			if (!type.isConcrete()) {
				if (subTypeInstanceAllowed) {
					if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
						for (ITypeInfo subType : type.getPolymorphicInstanceSubTypes()) {
							try {
								return createDefaultInstance(subType, parentObject, true);
							} catch (Throwable ignore) {
							}
						}
					}
					throw new ReflectionUIError("Cannot instanciate abstract or " + Object.class.getName() + " type");
				} else {
					throw new ReflectionUIError("Cannot instanciate abstract or " + Object.class.getName() + " type");
				}
			}

			IMethodInfo zeroParamConstructor = getZeroParameterMethod(type.getConstructors());
			if (zeroParamConstructor != null) {
				return zeroParamConstructor.invoke(parentObject, new InvocationData(zeroParamConstructor));
			}
			for (IMethodInfo constructor : type.getConstructors()) {
				InvocationData invocationData = new InvocationData(constructor);
				if (invocationData.areAllDefaultValuesProvided(constructor.getParameters())) {
					return constructor.invoke(parentObject, invocationData);
				}
			}
			throw new ReflectionUIError("Default constructor not found");
		} catch (Throwable t) {
			throw new ReflectionUIError(
					"Failed to create a default instance of type '" + type.getName() + "': " + t.toString(), t);

		}

	}

	public static <T extends IInfo> Comparator<T> getInfosComparator(final List<String> expectedOrderSpecification,
			List<T> initialOrder) {
		final List<T> initialOrderCopy = new ArrayList<T>(initialOrder);
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (expectedOrderSpecification.contains(o1.getName())
						&& expectedOrderSpecification.contains(o2.getName())) {
					Integer index1 = new Integer(expectedOrderSpecification.indexOf(o1.getName()));
					Integer index2 = new Integer(expectedOrderSpecification.indexOf(o2.getName()));
					return index1.compareTo(index2);
				}
				if (expectedOrderSpecification.contains(o1.getName())) {
					return 1;
				}
				if (expectedOrderSpecification.contains(o2.getName())) {
					return -1;
				}
				Integer index1 = new Integer(initialOrderCopy.indexOf(o1));
				Integer index2 = new Integer(initialOrderCopy.indexOf(o2));
				return index1.compareTo(index2);
			}
		};
	}

	public static boolean hasPolymorphicInstanceSubTypes(ITypeInfo type) {
		List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
		return (polyTypes != null) && (polyTypes.size() > 0);
	}

	public static String toString(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return "";
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type;
			List<String> result = new ArrayList<String>();
			for (Object item : listType.toArray(object)) {
				result.add(toString(reflectionUI, item));
			}
			return ReflectionUIUtils.stringJoin(result, ", ");
		} else {
			return type.toString(object);
		}
	}

	public static ResourcePath getIconImagePath(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return null;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.getIconImagePath();
	}

	public static boolean canCopy(ReflectionUI reflectionUI, Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.canCopy(object);
	}

	public static Object copy(ReflectionUI reflectionUI, Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.copy(object);
	}

	public static void checkInstance(ITypeInfo type, Object object) {
		if (object == null) {
			return;
		}
		if (!type.supportsInstance(object)) {
			throw new ReflectionUIError();
		}
	}

	public static IModification finalizeSeparateObjectValueEditSession(IModification valueUndoModification,
			boolean valueModifAccepted, ValueReturnMode valueReturnMode, boolean valueReplaced,
			IModification commitModif, String editSessionTitle) {
		ModificationStack objectModifStack = new ModificationStack(null);
		ModificationStack valueModifStack = new ModificationStack(null);
		valueModifStack.pushUndo(valueUndoModification);
		finalizeSeparateObjectValueEditSession(objectModifStack, valueModifStack, valueModifAccepted, valueReturnMode,
				valueReplaced, commitModif, editSessionTitle, null);
		return objectModifStack.toCompositeUndoModification(editSessionTitle);
	}

	public static boolean finalizeSeparateObjectValueEditSession(final ModificationStack objectModifStack,
			final ModificationStack valueModifStack, boolean valueModifAccepted, final ValueReturnMode valueReturnMode,
			final boolean valueReplaced, final IModification commitModif, String editSessionTitle,
			final Listener<String> debugLogListener) {

		if (objectModifStack == null) {
			throw new ReflectionUIError();
		}
		if (valueModifStack == null) {
			throw new ReflectionUIError();
		}

		boolean parentObjectImpacted = false;
		if (valueModifAccepted) {
			if (!valueModifStack.isNull()) {
				parentObjectImpacted = objectModifStack.insideComposite(editSessionTitle, UndoOrder.FIFO,
						new Accessor<Boolean>() {
							@Override
							public Boolean get() {
								if (valueReturnMode != ValueReturnMode.CALCULATED) {
									if (valueModifStack.wasInvalidated()) {
										objectModifStack.invalidate();
									} else {
										objectModifStack.pushUndo(valueModifStack.toCompositeUndoModification(null));
									}
								}
								if ((valueReturnMode != ValueReturnMode.DIRECT_OR_PROXY) || valueReplaced) {
									if (commitModif != null) {
										if (debugLogListener != null) {
											debugLogListener.handle("Executing " + commitModif);
										}
										objectModifStack.apply(commitModif);
									}
								}
								return true;
							}
						});
			}
		} else {
			if (!valueModifStack.isNull()) {
				if (valueReturnMode != ValueReturnMode.CALCULATED) {
					if (!valueModifStack.wasInvalidated()) {
						if (debugLogListener != null) {
							debugLogListener.handle("Undoing " + valueModifStack);
						}
						valueModifStack.undoAll();
					} else {
						if (debugLogListener != null) {
							debugLogListener.handle("WARNING: Cannot undo invalidated sub-modification stack: "
									+ valueModifStack + "\n=> Invalidating parent modification stack");
						}
						objectModifStack.invalidate();
						parentObjectImpacted = true;
					}
				}
			}
		}
		return parentObjectImpacted;
	}

	public static boolean canEditSeparateObjectValue(boolean valueKnownAsImmutable, ValueReturnMode valueReturnMode,
			boolean canCommit) {
		if ((valueReturnMode != ValueReturnMode.CALCULATED) && !valueKnownAsImmutable) {
			return true;
		}
		if (canCommit) {
			return true;
		}
		return false;
	}

	public static boolean isValueImmutable(ReflectionUI reflectionUI, Object value) {
		if (value == null) {
			return true;
		}
		ITypeInfo valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
		return valueType.isImmutable();
	}

	public static void setValueThroughModificationStack(IFieldControlData data, Object newValue,
			ModificationStack modifStack) {
		ControlDataValueModification modif = new ControlDataValueModification(data, newValue);
		try {
			modifStack.apply(modif);
		} catch (Throwable t) {
			modifStack.invalidate();
			throw new ReflectionUIError(t);
		}
	}

	public static Object invokeMethodThroughModificationStack(IMethodControlData data, InvocationData invocationData,
			ModificationStack modifStack) {
		if (data.isReadOnly()) {
			return data.invoke(invocationData);
		} else {
			Runnable undoJob = data.getNextUpdateCustomUndoJob(invocationData);
			if (undoJob != null) {
				final Object[] resultHolder = new Object[1];
				data = new MethodControlDataProxy(data) {
					@Override
					public Object invoke(InvocationData invocationData) {
						return resultHolder[0] = super.invoke(invocationData);
					}
				};
				InvokeMethodModification modif = new InvokeMethodModification(data, invocationData);
				try {
					modifStack.apply(modif);
				} catch (Throwable t) {
					modifStack.invalidate();
					throw new ReflectionUIError(t);
				}
				return resultHolder[0];
			} else {
				try {
					Object result = data.invoke(invocationData);
					return result;
				} finally {
					modifStack.invalidate();
				}
			}
		}
	}

	public static String getDefaultMethodCaption(IMethodInfo method) {
		String result = ReflectionUIUtils.identifierToCaption(method.getName());
		if (method.getReturnValueType() != null) {
			result = result.replaceAll("^Get ", "Show ");
		}
		return result;
	}

	public static String getDefaultFieldCaption(IFieldInfo field) {
		String result = ReflectionUIUtils.identifierToCaption(field.getName());
		return result;
	}

	public static String getDefaultListTypeCaption(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		if (itemType == null) {
			return "List";
		} else {
			return "List of " + itemType.getCaption() + " elements";
		}
	}

	public static Listener<String> getDebugLogListener(final ReflectionUI reflectionUI) {
		return new Listener<String>() {
			@Override
			public void handle(String event) {
				reflectionUI.logDebug(event);
			}
		};
	}

	public static Listener<String> getErrorLogListener(final ReflectionUI reflectionUI) {
		return new Listener<String>() {
			@Override
			public void handle(String event) {
				reflectionUI.logError(event);
			}
		};
	}

	public static String getContructorDescription(IMethodInfo ctor) {
		StringBuilder result = new StringBuilder(ctor.getCaption());
		if (ctor.getParameters().size() == 0) {
			result.append(" - by default");
		} else {
			result.append(" - specify ");
			result.append(formatParameterList(ctor.getParameters()));
		}
		return result.toString();
	}

	public static String serializeToHexaText(Object object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.flush();
			byte[] binary = baos.toByteArray();
			return Base64.encodeBase64String(binary);
		} catch (Throwable e) {
			throw new ReflectionUIError(e);
		}
	}

	public static Object deserializeFromHexaText(String text) {
		try {
			byte[] binary = Base64.decodeBase64(text);
			ByteArrayInputStream bais = new ByteArrayInputStream(binary);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Throwable e) {
			throw new ReflectionUIError(e);
		}
	}

	public static MenuElementKind getMenuElementKind(IMenuElement element) {
		if (element instanceof Menu) {
			return MenuElementKind.MENU;
		} else if (element instanceof MenuItemCategory) {
			return MenuElementKind.ITEM_CATEGORY;
		} else if (element instanceof AbstractMenuItem) {
			return MenuElementKind.ITEM;
		} else {
			throw new ReflectionUIError();
		}
	}

	public static Object copyThroughSerialization(Serializable object) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			serialize(object, baos);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			Object copy = deserialize(bais);
			return copy;
		} catch (Throwable t) {
			throw new ReflectionUIError("Could not copy object through serialization: " + t.toString());
		}
	}

	public static void serialize(Object object, OutputStream out) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(object);
		} catch (Throwable t) {
			throw new ReflectionUIError("Failed to serialize object: " + t.toString());
		}
	}

	public static Object deserialize(InputStream in) {
		try {
			ObjectInputStream ois = new ObjectInputStream(in);
			return ois.readObject();
		} catch (Throwable t) {
			throw new ReflectionUIError("Failed to deserialize object: " + t.toString());
		}
	}

	protected static XStream getXStream() {
		XStream result = new XStream();
		result.registerConverter(new JavaBeanConverter(result.getMapper()), -20);
		return result;
	}

	public static void saveXML(Object object, OutputStream out) {
		XStream xstream = getXStream();
		xstream.toXML(object, out);
	}

	public static void loadXML(Object object, InputStream in) {
		XStream xstream = getXStream();
		xstream.fromXML(in, object);
	}

	public static boolean equalsAccordingInfos(Object o1, Object o2, ReflectionUI reflectionUI,
			IInfoFilter infoFilter) {
		if (o1 == o2) {
			return true;
		}
		if ((o1 == null) || (o2 == null)) {
			return false;
		}
		if (ClassUtils.isPrimitiveClassOrWrapperOrString(o1.getClass())) {
			return o1.equals(o2);
		}
		ITypeInfo type1 = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(o1));
		ITypeInfo type2 = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(o2));
		if (!type1.equals(type2)) {
			return false;
		}
		if (type1.isPrimitive()) {
			return o1.equals(o2);
		}
		for (IFieldInfo field : type1.getFields()) {
			if (infoFilter.excludeField(field)) {
				continue;
			}
			Object value1 = field.getValue(o1);
			Object value2 = field.getValue(o2);
			if (!equalsAccordingInfos(value1, value2, reflectionUI, infoFilter)) {
				return false;
			}
		}
		if (type1 instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type1;
			Object[] rawList1 = listType.toArray(o1);
			Object[] rawList2 = listType.toArray(o2);
			if (rawList1.length != rawList2.length) {
				return false;
			}
			for (int i = 0; i < rawList1.length; i++) {
				Object item1 = rawList1[i];
				Object item2 = rawList2[i];
				if (!equalsAccordingInfos(item1, item2, reflectionUI, infoFilter)) {
					return false;
				}
			}
		}
		if (type1 instanceof IEnumerationTypeInfo) {
			IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) type1;
			IEnumerationItemInfo valueInfo1 = enumType.getValueInfo(o1);
			IEnumerationItemInfo valueInfo2 = enumType.getValueInfo(o2);
			if (!valueInfo1.getName().equals(valueInfo2.getName())) {
				return false;
			}
		}
		return true;
	}

	public static String formatMethodControlCaption(IMethodControlData data) {
		String caption = data.getCaption();
		{
			if (caption.length() > 0) {
				if (data.getParameters().size() > 0) {
					caption += "...";
				}
			}
		}
		return caption;
	}

	public static String formatMethodControlTooltipText(IMethodControlData data) {
		if (data.getOnlineHelp() != null) {
			return data.getOnlineHelp();
		} else {
			if (data.getParameters().size() > 0) {
				String toolTipText = formatMethodControlCaption(data);
				if (toolTipText.length() > 0) {
					toolTipText += "\n";
				}
				toolTipText += "Parameter(s): " + ReflectionUIUtils.formatParameterList(data.getParameters());
				return toolTipText;
			} else {
				return null;
			}
		}
	}

	public static List<IMenuElementPosition> getAncestors(IMenuElementPosition elementPosition) {
		List<IMenuElementPosition> result = new ArrayList<IMenuElementPosition>();
		while (elementPosition.getParent() != null) {
			result.add(elementPosition.getParent());
			elementPosition = elementPosition.getParent();
		}
		return result;
	}

	public static Runnable createDefaultUndoJob(final IFieldControlData data) {
		final Object oldValue = data.getValue();
		return new Runnable() {
			@Override
			public void run() {
				data.setValue(oldValue);
			}
		};
	}

	public static Runnable createDefaultUndoJob(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		return createDefaultUndoJob(new DefaultFieldControlData(reflectionUI, object, field));
	}

	public static <T> void replaceItem(List<T> list, T t1, T t2) {
		int index = list.indexOf(t1);
		list.set(index, t2);
	}

	public static String getUniqueID() {
		return new UID().toString();
	}

	public static void setCurrentFieldControlPlugin(CustomizedSwingRenderer swingCustomizer,
			Map<String, Object> specificProperties, IFieldControlPlugin plugin) {
		String lastPluginId = (String) specificProperties.remove(IFieldControlPlugin.CHOSEN_PROPERTY_KEY);
		if (lastPluginId != null) {
			IFieldControlPlugin lastPlugin = SwingRendererUtils.findFieldControlPlugin(swingCustomizer, lastPluginId);
			if (lastPlugin instanceof ICustomizableFieldControlPlugin) {
				((ICustomizableFieldControlPlugin) lastPlugin).cleanUpCustomizations(specificProperties);
			}
		}
		if (plugin != null) {
			specificProperties.put(IFieldControlPlugin.CHOSEN_PROPERTY_KEY, plugin.getIdentifier());
			if (plugin instanceof ICustomizableFieldControlPlugin) {
				((ICustomizableFieldControlPlugin) plugin).setUpCustomizations(specificProperties);
			}
		}
	}
}
