package xy.reflect.ui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import xy.reflect.ui.ReflectionUI;

public class SystemProperties {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public @interface Usage {
		public String value();

	}

	private static final String PREFIX = ReflectionUI.class.getPackage().getName();

	@Usage("If the value of this property is \"true\" then the UI customization tools will be hidden.")
	public static final String HIDE_INFO_CUSTOMIZATIONS_TOOLS = PREFIX + ".infoCustomizationsToolsHidden";

	@Usage("If the value of this property is set then the customizations that were specified for the UI customization tools will be editable and saved to the specified output file.")
	public static final String INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH = PREFIX
			+ ".customizationToolsCustomizationsFilePath";

	@Usage("If the value of this property is \"true\" then the default customizations are active.")
	public static final String DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE = PREFIX + ".defaultCustomizationsActive";

	@Usage("If the value of this property is set then the default customizations are loaded from and saved to the specified file instead of the default one.")
	public static final String DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH = PREFIX + ".defaultCustomizationsFilePath";

	@Usage("If the value of this property is  \"true\" then ReflectionUI objects will print debug messages to the console output by default.")
	public static final String DEBUG = PREFIX + ".debug";

	@Usage("If the value of this property is  \"true\" then customizations will be initialized if not found.")
	public static final String CREATE_INFO_CUSTOMIZATIONS_IF_NOT_FOUND = PREFIX + ".createCustomizationsIfNotFound";

	@Usage("The value of this property is used as the maximum size of various caches used to optimize the reflection process")
	public static final String STANDARD_CACHE_SIZE = PREFIX + ".standardCacheSize";

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

	public static boolean areDefaultInfoCustomizationsActive() {
		return System.getProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true").equals("true");
	}

	public static String getDefaultInfoCustomizationsFilePath() {
		return System.getProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH, "default.icu");
	}

	public static boolean areCustomizationToolsDisabled() {
		return System.getProperty(SystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS, "false").equals("true");
	}

	public static boolean isInfoCustomizationToolsCustomizationAllowed() {
		return System.getProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH) != null;
	}

	public static boolean isDebugModeActive() {
		return System.getProperty(SystemProperties.DEBUG, "false").equals("true");
	}

	public static boolean areInfoCustomizationsCreatedIfNotFound() {
		return System.getProperty(SystemProperties.CREATE_INFO_CUSTOMIZATIONS_IF_NOT_FOUND, "true").equals("true");
	}

	public static long getStandardCacheSize() {
		return Long.valueOf(System.getProperty(SystemProperties.STANDARD_CACHE_SIZE, "1000"));
	}

}
