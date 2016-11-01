package xy.reflect.ui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import xy.reflect.ui.ReflectionUI;

public class SystemProperties {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public @interface Usage {
		public String value();

	}

	private static final String PREFIX = ReflectionUI.class.getPackage().getName();

	@Usage("If the value of this property is \"true\" then the values in the UI will be dsiplayed as much as possible as not-nullable.")
	public static final String HIDE_NULLABLE_FACETS = PREFIX + ".hideNullableFacets";

	@Usage("Fields that needs to be hidden in the UI can be specified in this property using this format: 'package.subpackage.TheClass#theField|package2.subpackage2.TheClass2#theField2|...'. Wildcard characters can be used.")
	public static final String HIDE_FIELDS = PREFIX + ".hideFields";

	@Usage("Methods (including constructors) that needs to be hidden in the UI can be specified in this property using this format: 'package.subpackage.TheClass#theMethod(parameterType1,parameterType2,...)|package2.subpackage2.TheClass2#theMethod2(parameterType1,parameterType2,...)|...'. Wildcard characters can be used.")
	public static final String HIDE_METHODS = PREFIX + ".hideMethods";

	@Usage("Parameters that needs to be hidden in the UI can be specified in this property using this format: 'package.subpackage.TheClass#theMethod(parameterType1,parameterType2,...):<PARAMETER_INDEX>|package2.subpackage2.TheClass2#theMethod2(parameterType1,parameterType2,...):1|...'. Wildcard characters can be used.")
	public static final String HIDE_PARAMETERS = PREFIX + ".hideParameters";

	@Usage("If the value of this property is \"true\" then the UI customization tools will be hidden.")
	public static final String HIDE_INFO_CUSTOMIZATIONS_TOOLS = PREFIX
			+ ".infoCustomizationsToolsHidden";

	@Usage("If the value of this property is set then the customizations that were specified for the UI customization tools will be editable and saved to the specified output file.")
	public static final String INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH = PREFIX
			+ ".customizationToolsCustomizationsFilePath";

	@Usage("If the value of this property is \"true\" then the default customizations are active.")
	public static final String DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE = PREFIX + ".defaultCustomizationsActive";

	@Usage("If the value of this property is \"true\" then the default customizations are editable.")
	public static final String DEFAULT_INFO_CUSTOMIZATIONS_EDITABLE = PREFIX + ".defaultCustomizationsEditable";

	@Usage("If the value of this property is set then the default customizations are loaded from and saved to the specified file instead of the default one.")
	public static final String DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH = PREFIX + ".defaultCustomizationsFilePath";

	public static String describe() {
		StringBuilder result = new StringBuilder();
		for (Field field : SystemProperties.class.getFields()) {
			Usage usage = field.getAnnotation(Usage.class);
			if (usage == null) {
				continue;
			}
			if (result.length() > 0) {
				result.append("\n");
			}
			try {
				result.append("-D" + (String) field.get(null) + "=...\n  => " + usage.value());
			} catch (IllegalArgumentException e) {
				throw new ReflectionUIError(e);
			} catch (IllegalAccessException e) {
				throw new ReflectionUIError(e);
			}
		}
		return result.toString();
	}

	public static boolean hideNullablefacets() {
		return Boolean.valueOf(System.getProperty(HIDE_NULLABLE_FACETS, "false"));
	}

	private static boolean matchesHiddenPattern(String qualifiedName, String hiddenPattern) {
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
		String hiddenPattern = System.getProperty(HIDE_METHODS);
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

	public static boolean areInfoCustomizationToolsAuthorized() {
		return !"true".equals(System.getProperty(SystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS, "false"));
	}

	public static boolean isInfoCustomizationToolsCustomizationAllowed() {
		return System.getProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH) != null;
	}

	public static boolean areDefaultInfoCustomizationsActive() {
		return System.getProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "false").equals("true");
	}

	public static String getDefaultInfoCustomizationsFilePath() {
		return System.getProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH, "default.icu");
	}

	public static boolean areDefaultInfoCustomizationsEditable() {
		return System.getProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_EDITABLE, "true").equals("true");
	}

}
