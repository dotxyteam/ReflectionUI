package xy.reflect.ui.info;

import java.awt.Image;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.util.InfoCustomizations.AbstractInfoCustomization;

public class DesktopSpecificProperty {
	public static final String KEY_ICON_IMAGE = DesktopSpecificProperty.class.getSimpleName() + ".KEY_ICON_IMAGE";
	public static final String KEY_ICON_IMAGE_PATH = DesktopSpecificProperty.class.getSimpleName()
			+ ".KEY_ICON_IMAGE_PATH";
	public static final String CREATE_EMBEDDED_FORM = DesktopSpecificProperty.class.getSimpleName()
			+ ".CREATE_EMBEDDED_FORM";
	public static final String FORBID_CUSTUM_CONTROL = DesktopSpecificProperty.class.getSimpleName()
			+ ".FORBID_POLYMORPHIC_CONTROL";
	public static final String INFO_FILTER = DesktopSpecificProperty.class.getSimpleName() + ".INFO_FILTER";

	public static Map<String, Object> accessCustomizationsProperties(final AbstractInfoCustomization c) {
		return new AbstractMap<String, Object>() {
			@Override
			public Set<java.util.Map.Entry<String, Object>> entrySet() {
				Map<String, Object> properties = c.getSpecificProperties();
				if (properties == null) {
					return Collections.emptySet();
				}
				return properties.entrySet();
			}

			@Override
			public Object put(String key, Object value) {
				Map<String, Object> properties = c.getSpecificProperties();
				if (properties == null) {
					properties = new HashMap<String, Object>();
					c.setSpecificProperties(properties);
				}
				return properties.put(key, value);
			}
		};
	}

	public static Map<String, Object> accessInfoProperties(final IInfo i) {
		return new AbstractMap<String, Object>() {
			@Override
			public Set<java.util.Map.Entry<String, Object>> entrySet() {
				Map<String, Object> properties = i.getSpecificProperties();
				if (properties == null) {
					return Collections.emptySet();
				}
				return properties.entrySet();
			}
		};
	}

	public static Map<String, Object> accessControlDataProperties(final IFieldControlData data) {
		return new AbstractMap<String, Object>() {
			@Override
			public Set<java.util.Map.Entry<String, Object>> entrySet() {
				Map<String, Object> properties = data.getSpecificProperties();
				if (properties == null) {
					return Collections.emptySet();
				}
				return properties.entrySet();
			}
		};
	}

	public static boolean isSubFormExpanded(Map<String, Object> properties) {
		return Boolean.TRUE.equals(properties.get(DesktopSpecificProperty.CREATE_EMBEDDED_FORM));
	}

	public static void setSubFormExpanded(Map<String, Object> properties, boolean b) {
		properties.put(DesktopSpecificProperty.CREATE_EMBEDDED_FORM, b);
	}

	public static boolean isCustumControlForbidden(Map<String, Object> properties) {
		return Boolean.TRUE.equals(properties.get(DesktopSpecificProperty.FORBID_CUSTUM_CONTROL));
	}

	public static void setCustomControlForbidden(Map<String, Object> properties, boolean b) {
		properties.put(DesktopSpecificProperty.FORBID_CUSTUM_CONTROL, b);
	}

	public static IInfoFilter getFilter(Map<String, Object> properties) {
		return (IInfoFilter) properties.get(DesktopSpecificProperty.INFO_FILTER);
	}

	public static void setFilter(Map<String, Object> properties, IInfoFilter filter) {
		properties.put(DesktopSpecificProperty.INFO_FILTER, filter);
	}

	public static String getIconImageFilePath(Map<String, Object> properties) {
		return (String) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH);
	}

	public static void setIconImageFilePath(Map<String, Object> properties, String filePath) {
		properties.put(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH, filePath);
	}

	public static Image getIconImage(Map<String, Object> properties) {
		return (Image) properties.get(KEY_ICON_IMAGE);
	}

	public static void setIconImage(Map<String, Object> properties, Image image) {
		properties.put(KEY_ICON_IMAGE, image);
	}
}