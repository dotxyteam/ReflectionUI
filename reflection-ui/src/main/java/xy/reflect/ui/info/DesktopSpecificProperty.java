package xy.reflect.ui.info;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.util.InfoCustomizations.AbstractInfoCustomization;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ResourcePath;
import xy.reflect.ui.util.SwingRendererUtils;

public class DesktopSpecificProperty {
	public static final String KEY_ICON_IMAGE = DesktopSpecificProperty.class.getSimpleName() + ".KEY_ICON_IMAGE";
	public static final String KEY_ICON_IMAGE_PATH = DesktopSpecificProperty.class.getSimpleName()
			+ ".KEY_ICON_IMAGE_PATH";
	public static final String CREATE_EMBEDDED_FORM = DesktopSpecificProperty.class.getSimpleName()
			+ ".CREATE_EMBEDDED_FORM";
	public static final String INFO_FILTER = DesktopSpecificProperty.class.getSimpleName() + ".INFO_FILTER";
	public static Map<String, Image> iconImageCache = new HashMap<String, Image>();
	public static final Image NULL_ICON_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

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

	public static Map<String, Object> accessControlDataProperties(final IControlData data) {
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
		Image result;
		result = (Image) properties.get(KEY_ICON_IMAGE);
		if (result != null) {
			return result;
		}
		URL imageUrl;
		String imagePath = (String) properties.get(KEY_ICON_IMAGE_PATH);
		if (imagePath == null) {
			return null;
		}
		if (imagePath.startsWith(ResourcePath.CLASSPATH_RESOURCE_PREFIX)) {
			imagePath = imagePath.substring(ResourcePath.CLASSPATH_RESOURCE_PREFIX.length());
			imageUrl = SwingRendererUtils.class.getClassLoader().getResource(imagePath);
		} else {
			try {
				imageUrl = new File(imagePath).toURI().toURL();
			} catch (MalformedURLException e) {
				throw new ReflectionUIError(e);
			}
		}
		result = iconImageCache.get(imagePath);
		if (result == null) {
			try {
				result = ImageIO.read(imageUrl);
			} catch (IOException e) {
				e.printStackTrace();
				result = NULL_ICON_IMAGE;
			}
			iconImageCache.put(imagePath, result);
		}
		if (result == NULL_ICON_IMAGE) {
			return null;
		}
		return result;
	}

	public static void setIconImage(Map<String, Object> properties, Image image) {
		properties.put(KEY_ICON_IMAGE, image);
	}
}