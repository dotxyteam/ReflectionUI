package xy.reflect.ui.util;

import xy.reflect.ui.ReflectionUI;

public class SystemProperties {

	
	private static final String PREFIX = ReflectionUI.class.getPackage().getName();
	public static final String HIDE_NULLABLE_FACETS = PREFIX + ".hideNullableFacets";

	public static boolean hideNullablefacets() {
		return Boolean.valueOf(System.getProperty(HIDE_NULLABLE_FACETS, "false"));
	}

}
