package xy.reflect.ui.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;

public class SystemProperties {

	private static final String PREFIX = ReflectionUI.class.getPackage()
			.getName();

	public static final String HIDE_NULLABLE_FACETS = PREFIX
			+ ".hideNullableFacets";
	public static final String HIDE_METHODS = PREFIX + ".hideMethods";
	public static final String HIDE_FIELDS = PREFIX + ".hideFields";
	
	public static boolean hideNullablefacets() {
		return Boolean.valueOf(System
				.getProperty(HIDE_NULLABLE_FACETS, "false"));
	}

	public static boolean hideMethod(Method method) {
		String hiddenPattern = System.getProperty(HIDE_METHODS);
		if (hiddenPattern == null) {
			return false;
		}
		String qualifiedName = ReflectionUIUtils.getQualifiedName(method);
		return Pattern.compile(hiddenPattern).matcher(qualifiedName)
				.find();
	}

	public static boolean hideField(Field field) {
		String hiddenPattern = System.getProperty(HIDE_FIELDS);
		if (hiddenPattern == null) {
			return false;
		}
		String qualifiedName = ReflectionUIUtils.getQualifiedName(field);
		return Pattern.compile(hiddenPattern).matcher(qualifiedName)
				.find();
	}

}
