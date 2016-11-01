package xy.reflect.ui.info;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.type.util.InfoCustomizations.AbstractInfoCustomization;
import xy.reflect.ui.util.FileUtils;

public class DesktopSpecificProperty {
	public static final String KEY_ICON_IMAGE = DesktopSpecificProperty.class.getSimpleName() + ".KEY_ICON_IMAGE";
	public static final String KEY_ICON_IMAGE_PATH = DesktopSpecificProperty.class.getSimpleName()
			+ ".KEY_ICON_IMAGE_PATH";
	public static final String KEY_ICON_IMAGE_PATH_KIND = DesktopSpecificProperty.class.getSimpleName()
			+ ".KEY_ICON_IMAGE_PATH_KIND";
	public static final String CREATE_EMBEDDED_FORM = DesktopSpecificProperty.class.getSimpleName()
			+ ".CREATE_EMBEDDED_FORM";
	public static final String VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE = DesktopSpecificProperty.class.getSimpleName()
			+ ".VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE";
	public static final String VALUE_PATH_TYPE_KIND_RELATIVE_FILE = DesktopSpecificProperty.class.getSimpleName()
			+ ".VALUE_PATH_TYPE_KIND_RELATIVE_FILE";
	public static final String VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE = DesktopSpecificProperty.class.getSimpleName()
			+ ".VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE";
	public static final String CLASSPATH_RESOURCE_PREFIX = "<class-path-resource> ";

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

	public static boolean isSubFormExpanded(Map<String, Object> properties) {
		return Boolean.TRUE.equals(properties.get(DesktopSpecificProperty.CREATE_EMBEDDED_FORM));
	}

	public static void setSubFormExpanded(Map<String, Object> properties, boolean b) {
		properties.put(DesktopSpecificProperty.CREATE_EMBEDDED_FORM, b);
	}

	public static File getIconImageFile(Map<String, Object> properties) {
		String filePath = (String) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH);
		if (filePath == null) {
			return new File("");
		}
		String filePathKind = (String) properties.get(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
		if (DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(filePathKind)) {
			filePath = CLASSPATH_RESOURCE_PREFIX + filePath;
		}
		return new File(filePath);
	}

	public static void setIconImageFile(Map<String, Object> properties, File file) {
		String filePath;
		if (file == null) {
			filePath = null;
		} else {
			filePath = file.getPath();
			if (filePath.length() == 0) {
				filePath = null;
			}
		}
		String filePathKind = null;
		if (filePath != null) {
			if (filePath.startsWith(CLASSPATH_RESOURCE_PREFIX)) {
				filePath = filePath.substring(CLASSPATH_RESOURCE_PREFIX.length());
				filePathKind = DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE;
			} else {
				List<PathKindOption> pathKindOptions = getPathKindOptions(filePath);
				PathKindOption chosenPathKindOption;
				if (pathKindOptions.size() == 1) {
					chosenPathKindOption = pathKindOptions.get(0);
				} else {
					chosenPathKindOption = SwingRenderer.DEFAULT.openSelectionDialog(null, pathKindOptions, null,
							"Choose an option", "Icon Image");
					if (chosenPathKindOption == null) {
						return;
					}
				}
				filePath = chosenPathKindOption.path;
				filePathKind = chosenPathKindOption.pathKind;
			}
		}
		properties.put(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH, filePath);
		properties.put(DesktopSpecificProperty.KEY_ICON_IMAGE_PATH_KIND, filePathKind);
	}

	private static List<PathKindOption> getPathKindOptions(String filePath) {
		List<PathKindOption> result = new ArrayList<PathKindOption>();
		File file = new File(filePath);
		{
			File candidateResourceFile = new File(file.getAbsoluteFile().getPath());
			while (true) {
				File mostAncestorFile = candidateResourceFile.getParentFile();
				if (mostAncestorFile == null) {
					break;
				}
				while (mostAncestorFile.getParentFile() != null) {
					mostAncestorFile = mostAncestorFile.getParentFile();
				}
				candidateResourceFile = FileUtils.relativizeFile(mostAncestorFile, candidateResourceFile);
				String candidateResourcePath = candidateResourceFile.getPath().replaceAll("\\\\", "/");
				URL resourceURL = DesktopSpecificProperty.class.getClassLoader().getResource(candidateResourcePath);
				if (resourceURL != null) {
					result.add(new PathKindOption(DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE,
							candidateResourcePath));
					break;
				}
			}
		}
		{
			File currentDir = new File(".");
			if (FileUtils.isAncestor(currentDir, file)) {
				File relativeFile = FileUtils.relativizeFile(currentDir, file);
				result.add(new PathKindOption(DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE,
						relativeFile.getPath()));
			}
		}
		{
			result.add(new PathKindOption(DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE,
					file.getAbsolutePath()));
		}
		return result;
	}

	private static class PathKindOption {
		String path;
		String pathKind;

		public PathKindOption(String pathKind, String path) {
			super();
			this.path = path;
			this.pathKind = pathKind;
		}

		@Override
		public String toString() {
			if (DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE.equals(pathKind)) {
				return "<absolute-file> " + path;
			} else if (DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE.equals(pathKind)) {
				return "<relative-file> " + path;
			} else if (DesktopSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
				return CLASSPATH_RESOURCE_PREFIX + path;
			} else {
				return "<illegal-path-kind: " + pathKind + "> " + path;
			}
		}
	}

}