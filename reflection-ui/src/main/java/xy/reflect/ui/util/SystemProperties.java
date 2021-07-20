


package xy.reflect.ui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import xy.reflect.ui.ReflectionUI;

/**
 * Contains the system properties exposed by the library.
 * 
 * @author olitank
 *
 */
public class SystemProperties {

	public  static void main(String[] args) {
		System.out.println(describe());
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public @interface Usage {
		public String value();

	}

	protected static final String PREFIX = ReflectionUI.class.getPackage().getName();

	@Usage("If the value of this property is \"true\" then ReflectionUI objects will print debug messages to the console output by default.")
	public static final String DEBUG = PREFIX + ".debug";

	@Usage("The value of this property is used as the maximum size of various caches used to optimize the reflection process")
	public static final String STANDARD_CACHE_SIZE = PREFIX + ".standardCacheSize";

	@Usage("If the value of this property is \"true\" then the default customizations are active.")
	public static final String DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE = PREFIX + ".defaultCustomizationsActive";

	@Usage("If the value of this property is set then the default customizations are loaded from and saved to the specified file instead of the default one.")
	public static final String DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH = PREFIX + ".defaultCustomizationsFilePath";

	@Usage("If the value of this property is \"true\" then customizations will be initialized if not found.")
	public static final String CREATE_INFO_CUSTOMIZATIONS_IF_NOT_FOUND = PREFIX + ".createCustomizationsIfNotFound";

	@Usage("If this property is set then SwingRenderer.getDefault() will return an object of the specified class.")
	public static final String ALTERNATE_DEFAULT_SWING_RENDERER_CLASS_NAME = PREFIX
			+ ".alternateDefaultSwingRendererClass";

	@Usage("If this property is set then CustomizedSwingRenderer.getDefault() will return an object of the specified class.")
	public static final String ALTERNATE_DEFAULT_CUSTOMIZED_SWING_RENDERER_CLASS_NAME = PREFIX
			+ ".alternateDefaultCustomizedSwingRendererClass";

	public static String describe() {
		return describe(SystemProperties.class);
	}

	public static String describe(Class<? extends SystemProperties> clazz) {
		StringBuilder result = new StringBuilder();
		for (Field field : clazz.getFields()) {
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

	public static boolean isDebugModeActive() {
		return System.getProperty(SystemProperties.DEBUG, "false").equals("true");
	}

	public static long getStandardCacheSize() {
		return Long.valueOf(System.getProperty(SystemProperties.STANDARD_CACHE_SIZE, "1000"));
	}

	public static boolean areDefaultInfoCustomizationsActive() {
		return System.getProperty(DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true").equals("true");
	}

	public static String getDefaultInfoCustomizationsFilePath() {
		if (!areDefaultInfoCustomizationsActive()) {
			return null;
		}
		return System.getProperty(DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH, "default.icu");
	}

	public static boolean areInfoCustomizationsCreatedIfNotFound() {
		return System.getProperty(CREATE_INFO_CUSTOMIZATIONS_IF_NOT_FOUND, "true").equals("true");
	}

	public static Class<?> getAlternateDefaultSwingRendererClass() {
		String className = System.getProperty(ALTERNATE_DEFAULT_SWING_RENDERER_CLASS_NAME);
		if (className == null) {
			return null;
		} else {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new ReflectionUIError(e);
			}
		}
	}

	public static Class<?> getAlternateDefaultCustomizedSwingRendererClass() {
		String className = System.getProperty(ALTERNATE_DEFAULT_CUSTOMIZED_SWING_RENDERER_CLASS_NAME);
		if (className == null) {
			return null;
		} else {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new ReflectionUIError(e);
			}
		}
	}

}
