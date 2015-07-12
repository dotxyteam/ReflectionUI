package xy.reflect.ui.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import xy.reflect.ui.ReflectionUI;

public class SystemProperties {

	private static final String PREFIX = ReflectionUI.class.getPackage()
			.getName();

	public static final String HIDE_NULLABLE_FACETS = PREFIX
			+ ".hideNullableFacets";
	public static final String HIDE_FIELDS = PREFIX + ".hideFields";
	public static final String HIDE_METHODS = PREFIX + ".hideMethods";
	public static final String HIDE_PARAMETERS = PREFIX + ".hideParameters";
	public static final String HIDE_CONSTRUCTORS = PREFIX + ".hideConstructors";

	public static boolean hideNullablefacets() {
		return Boolean.valueOf(System
				.getProperty(HIDE_NULLABLE_FACETS, "false"));
	}

	private static boolean matchesHiddenPattern(String qualifiedName,
			String hiddenPattern) {
		String[] patterns = hiddenPattern.split("\\|");
		return Wildcard.matchOne(qualifiedName, patterns) != -1;
	}

	public static boolean hideField(Field field) {
		String hiddenPattern = System.getProperty(HIDE_FIELDS);
		if (hiddenPattern == null) {
			return false;
		}
		String qualifiedName = ReflectionUIUtils.getQualifiedName(field);
		return matchesHiddenPattern(qualifiedName, hiddenPattern);
	}

	public static boolean hideMethod(Method method) {
		String hiddenPattern = System.getProperty(HIDE_METHODS);
		if (hiddenPattern == null) {
			return false;
		}
		String qualifiedName = ReflectionUIUtils.getQualifiedName(method);
		return matchesHiddenPattern(qualifiedName, hiddenPattern);
	}

	public static boolean hideConstructor(Constructor<?> ctor) {
		String hiddenPattern = System.getProperty(HIDE_CONSTRUCTORS);
		if (hiddenPattern == null) {
			return false;
		}
		String qualifiedName = ReflectionUIUtils.getQualifiedName(ctor);
		return matchesHiddenPattern(qualifiedName, hiddenPattern);
	}

	public static boolean hideParameter(Parameter param) {
		String hiddenPattern = System.getProperty(HIDE_PARAMETERS);
		if (hiddenPattern == null) {
			return false;
		}
		String qualifiedName = ReflectionUIUtils.getQualifiedName(param);
		return matchesHiddenPattern(qualifiedName, hiddenPattern);
	}

}
