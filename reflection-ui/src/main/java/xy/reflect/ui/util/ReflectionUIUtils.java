package xy.reflect.ui.util;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.annotation.Category;
import xy.reflect.ui.info.annotation.Hidden;
import xy.reflect.ui.info.annotation.OnlineHelp;
import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.reflect.ui.info.annotation.Validating;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

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

	public static final String[] NEW_LINE_SEQUENCES = new String[] { "\r\n", "\n", "\r" };

	public static File getStreamAsFile(InputStream in) throws IOException {
		File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		} finally {
			out.close();
		}
		return tempFile;
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

	public static String getMethodInfoSignature(IMethodInfo method) {
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

	public static String getJavaMethodInfoSignature(Method javaMethod) {
		StringBuilder result = new StringBuilder();
		Class<?> returnType = javaMethod.getReturnType();
		result.append(returnType.getName());
		result.append(" " + javaMethod.getName() + "(");
		List<Parameter> params = getJavaParameters(javaMethod);
		for (int i = 0; i < params.size(); i++) {
			Parameter param = params.get(i);
			if (i > 0) {
				result.append(", ");
			}
			result.append(param.getType().getName());
		}
		result.append(")");
		return result.toString();
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

	public static String getJavaMethodSignature(Method javaMethod) {
		return getJavaMethodInfoSignature(javaMethod);
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

	public static boolean hasFileNameExtension(String fileName, String[] extensions) {
		for (String ext : extensions) {
			if (ext.toLowerCase().equals(getFileNameExtension(fileName).toLowerCase())) {
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

	public static <T extends IMethodInfo> T findMethodBySignature(List<T> methods, String signature) {
		for (T method : methods) {
			String candidateMethodSignature = getMethodInfoSignature(method);
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

	public static List<Class<?>> getJavaGenericTypeParameters(final Class<?> type, final Member ofMember,
			int methodArgumentPosition, Class<?> parameterizedBaseClass) {
		TypeResolver typeResolver = new TypeResolver();
		ResolvedType resolvedType = null;
		if (ofMember == null) {
			resolvedType = typeResolver.resolve(type);
		} else {
			MemberResolver memberResolver = new MemberResolver(typeResolver);
			ResolvedType declaringResolvedType = typeResolver.resolve(ofMember.getDeclaringClass());
			ResolvedTypeWithMembers resolvedTypeWithMembers = memberResolver.resolve(declaringResolvedType, null, null);
			if (ofMember instanceof Field) {
				for (ResolvedField resolvedField : resolvedTypeWithMembers.getMemberFields()) {
					if (resolvedField.getRawMember().equals(ofMember)) {
						resolvedType = resolvedField.getType();
						break;
					}
				}
			} else if (ofMember instanceof Method) {
				ResolvedMethod[] resolvedMethods;
				if (Modifier.isStatic(ofMember.getModifiers())) {
					resolvedMethods = resolvedTypeWithMembers.getStaticMethods();
				} else {
					resolvedMethods = resolvedTypeWithMembers.getMemberMethods();
				}
				for (ResolvedMethod resolvedMethod : resolvedMethods) {
					if (resolvedMethod.getRawMember().equals(ofMember)) {
						if (methodArgumentPosition == -1) {
							resolvedType = resolvedMethod.getType();
						} else {
							resolvedType = resolvedMethod.getArgumentType(methodArgumentPosition);
						}
						break;
					}
				}
			} else if (ofMember instanceof Constructor) {
				for (ResolvedConstructor resolvedConstructor : resolvedTypeWithMembers.getConstructors()) {
					if (resolvedConstructor.getRawMember().equals(ofMember)) {
						if (methodArgumentPosition == -1) {
							resolvedType = resolvedConstructor.getType();
						} else {
							resolvedType = resolvedConstructor.getArgumentType(methodArgumentPosition);
						}
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
		List<ResolvedType> resolvedTypeParameters = resolvedType.typeParametersFor(parameterizedBaseClass);
		if (resolvedTypeParameters == null) {
			return null;
		}
		for (ResolvedType classParameter : resolvedTypeParameters) {
			result.add(classParameter.getErasedType());
		}
		return result;
	}

	public static Class<?> getJavaGenericTypeParameter(final JavaTypeInfoSource javaTypeSource,
			Class<?> parameterizedBaseClass, int index) {
		List<Class<?>> parameterClasses = getJavaGenericTypeParameters(javaTypeSource.getJavaType(),
				javaTypeSource.getTypedMember(), javaTypeSource.getParameterPosition(), parameterizedBaseClass);
		if (parameterClasses == null) {
			return null;
		}
		if (parameterClasses.size() <= index) {
			return null;
		}
		return parameterClasses.get(index);
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
		try {
			URL jdkJavadocURL = ReflectionUI.class.getResource("resource/jdk-apidocs");
			Paranamer paranamer;
			paranamer = new AdaptiveParanamer(new DefaultParanamer(), new BytecodeReadingParanamer(),
					new JavadocParanamer(jdkJavadocURL));
			String[] parameterNames = paranamer.lookupParameterNames((AccessibleObject) owner, false);
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
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
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

	public static InfoCategory getAnnotatedInfoCategory(AnnotatedElement annotated) {
		Category annotation = annotated.getAnnotation(Category.class);
		if (annotation == null) {
			return null;
		}
		return new InfoCategory(annotation.value(), annotation.position());
	}

	public static String getAnnotatedInfoOnlineHelp(AnnotatedElement annotated) {
		OnlineHelp annotation = annotated.getAnnotation(OnlineHelp.class);
		if (annotation == null) {
			return null;
		}
		return annotation.value();
	}

	public static boolean isInfoHidden(AccessibleObject javaMetaObject) {
		Hidden annotation = javaMetaObject.getAnnotation(Hidden.class);
		if (annotation != null) {
			return true;
		}
		if (javaMetaObject instanceof Field) {
			Field field = (Field) javaMetaObject;
			if (SystemProperties.hideField(field)) {
				return true;
			}
		}
		if (javaMetaObject instanceof Method) {
			Method method = (Method) javaMetaObject;
			if (SystemProperties.hideMethod(method)) {
				return true;
			}
		}
		if (javaMetaObject instanceof Constructor<?>) {
			Constructor<?> ctor = (Constructor<?>) javaMetaObject;
			if (SystemProperties.hideConstructor(ctor)) {
				return true;
			}
		}
		if (javaMetaObject instanceof Parameter) {
			Parameter param = (Parameter) javaMetaObject;
			if (SystemProperties.hideParameter(param)) {
				return true;
			}
		}
		return false;
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

	public static Color getDisabledTextBackgroundColor() {
		return new JPanel().getBackground();
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

	public static List<Method> getAnnotatedValidatingMethods(Class<?> javaType) {
		List<Method> result = new ArrayList<Method>();
		for (Method method : javaType.getMethods()) {
			Validating annotation = method.getAnnotation(Validating.class);
			if (annotation != null) {
				if (method.getReturnType() != void.class) {
					throw new ReflectionUIError("Invalid validating method, the return type is not 'void': " + method);
				}
				if (method.getParameterTypes().length > 0) {
					throw new ReflectionUIError(
							"Invalid validating method, the number of parameters is not 0: " + method);
				}
				result.add(method);
				continue;
			}
		}
		return result;
	}

	public static Field getAnnotatedValueOptionsField(Class<?> containingJavaType, String targetFieldName) {
		for (Field field : containingJavaType.getFields()) {
			ValueOptionsForField annotation = field.getAnnotation(ValueOptionsForField.class);
			if (annotation != null) {
				if (!targetFieldName.equals(annotation.value())) {
					continue;
				}
				if (!field.getType().isArray()) {
					if (!Collection.class.isAssignableFrom(field.getType())) {
						throw new ReflectionUIError("Invalid value options field: " + field
								+ ". Invalid type: Expected type: <<array> or java.util.Collection");

					}
				}
				return field;
			}
		}
		return null;
	}

	public static Method getAnnotatedValueOptionsMethod(Class<?> containingJavaType, String baseFieldName) {
		for (Method method : containingJavaType.getMethods()) {
			ValueOptionsForField annotation = method.getAnnotation(ValueOptionsForField.class);
			if (annotation != null) {
				if (!baseFieldName.equals(annotation.value())) {
					continue;
				}
				if (!method.getReturnType().isArray()) {
					if (!Collection.class.isAssignableFrom(method.getReturnType())) {
						throw new ReflectionUIError("Invalid value options method: " + method
								+ ". Invalid return type: Expected return type: <<array> or java.util.Collection");
					}
				}
				return method;
			}
		}
		return null;
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

	public static Object[] getFieldValueOptionsFromAnnotatedMember(Object object, Class<?> containingJavaClass,
			String fieldName, ReflectionUI reflectionUI) {
		Object result = null;
		try {
			Field javaValueOptionsField = getAnnotatedValueOptionsField(containingJavaClass, fieldName);
			if (javaValueOptionsField != null) {
				result = javaValueOptionsField.get(object);
			} else {
				Method javaValueOptionsMethod = getAnnotatedValueOptionsMethod(containingJavaClass, fieldName);
				if (javaValueOptionsMethod != null) {
					result = javaValueOptionsMethod.invoke(object);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionUIError(e.getCause());
		}
		if (result == null) {
			return null;
		} else if (result instanceof Collection) {
			return ((Collection<?>) result).toArray();
		} else if (result.getClass().isArray()) {
			Object[] resultArray = new Object[Array.getLength(result)];
			for (int i = 0; i < resultArray.length; i++) {
				resultArray[i] = Array.get(result, i);
			}
			return resultArray;
		} else {
			throw new ReflectionUIError();
		}
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

	public static String getQualifiedName(Parameter parameter) {
		Member invokable = parameter.getDeclaringInvokable();
		return invokable.getDeclaringClass().getName() + "#" + invokable.getName() + "("
				+ stringJoin(gatClassNames(parameter.getDeclaringInvokableParameterTypes()), ",") + "):"
				+ parameter.getPosition();
	}

	public static List<String> gatClassNames(Class<?>[] classes) {
		List<String> paramTypeNames = new ArrayList<String>();
		for (Class<?> clazz : classes) {
			paramTypeNames.add(clazz.getName());
		}
		return paramTypeNames;
	}

	public static Rectangle getMaximumWindowBounds(Window window) {
		Rectangle result = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice gd : ge.getScreenDevices()) {
			for (GraphicsConfiguration gc : gd.getConfigurations()) {
				Rectangle screenBounds = gc.getBounds();
				Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
				Rectangle candidateResult = new Rectangle();
				candidateResult.x = screenBounds.x + screenInsets.left;
				candidateResult.y = screenBounds.y + screenInsets.top;
				candidateResult.height = screenBounds.height - screenInsets.top - screenInsets.bottom;
				candidateResult.width = screenBounds.width - screenInsets.left - screenInsets.right;
				if (result == null) {
					result = candidateResult;
				} else {
					if (window == null) {
						return result;
					} else {
						Rectangle candidateResultIntersection = candidateResult.intersection(window.getBounds());
						Rectangle resultIntersection = result.intersection(window.getBounds());
						if ((candidateResultIntersection.width
								* candidateResultIntersection.height) > (resultIntersection.width
										* resultIntersection.height)) {
							result = candidateResult;
						}
					}
				}
			}
		}
		return result;
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

	public static String composeTitle(String contextTitle, String localTitle) {
		if (contextTitle == null) {
			return localTitle;
		}
		return contextTitle + " - " + localTitle;
	}

	public static String getPrettyMessage(Throwable t) {
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

}
