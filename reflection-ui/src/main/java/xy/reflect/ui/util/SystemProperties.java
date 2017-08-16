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

	protected static final String PREFIX = ReflectionUI.class.getPackage().getName();

	@Usage("If the value of this property is  \"true\" then ReflectionUI objects will print debug messages to the console output by default.")
	public static final String DEBUG = PREFIX + ".debug";

	@Usage("The value of this property is used as the maximum size of various caches used to optimize the reflection process")
	public static final String STANDARD_CACHE_SIZE = PREFIX + ".standardCacheSize";

	
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

}
