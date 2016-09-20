package xy.reflect.ui.control.swing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.util.InfoCustomizations.AbstractInfoCustomization;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public class SwingSpecificProperty {
	private static final String KEY_ICON_IMAGE = SwingSpecificProperty.class.getSimpleName() + ".KEY_ICON_IMAGE";
	private static final String KEY_ICON_IMAGE_PATH = SwingSpecificProperty.class.getSimpleName()
			+ ".KEY_ICON_IMAGE_PATH";
	private static final String KEY_ICON_IMAGE_PATH_KIND = SwingSpecificProperty.class.getSimpleName()
			+ ".KEY_ICON_IMAGE_PATH_KIND";
	private static final String CREATE_EMBEDDED_FORM = SwingSpecificProperty.class.getSimpleName()
			+ ".CREATE_EMBEDDED_FORM";
	private static final String HIDE_UNDO_MANAGEMENT_CONTROLS = SwingSpecificProperty.class.getSimpleName()
			+ ".HIDE_UNDO_MANAGEMENT_CONTROLS";

	private static final String VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE = SwingSpecificProperty.class.getSimpleName()
			+ ".VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE";
	private static final String VALUE_PATH_TYPE_KIND_RELATIVE_FILE = SwingSpecificProperty.class.getSimpleName()
			+ ".VALUE_PATH_TYPE_KIND_RELATIVE_FILE";
	private static final String VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE = SwingSpecificProperty.class.getSimpleName()
			+ ".VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE";
	private static final String CLASSPATH_RESOURCE_PREFIX = "<class-path-resource> ";

	private static Map<String, Image> iconImageCache = new HashMap<String, Image>();
	private static final Image NULL_ICON_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

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
		return Boolean.TRUE.equals(properties.get(SwingSpecificProperty.CREATE_EMBEDDED_FORM));
	}

	public static void setSubFormExpanded(Map<String, Object> properties, boolean b) {
		properties.put(SwingSpecificProperty.CREATE_EMBEDDED_FORM, b);
	}
	
	public static boolean isUndoManagementHidden(Map<String, Object> properties) {
		return Boolean.TRUE.equals(properties.get(SwingSpecificProperty.HIDE_UNDO_MANAGEMENT_CONTROLS));
	}

	public static void setUndoManagementHidden(Map<String, Object> properties, boolean b) {
		properties.put(SwingSpecificProperty.HIDE_UNDO_MANAGEMENT_CONTROLS, b);
	}

	public static File getIconImageFile(Map<String, Object> properties) {
		String filePath = (String) properties.get(SwingSpecificProperty.KEY_ICON_IMAGE_PATH);
		if (filePath == null) {
			return new File("");
		}
		String filePathKind = (String) properties.get(SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
		if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(filePathKind)) {
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
				filePathKind = SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE;
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
		properties.put(SwingSpecificProperty.KEY_ICON_IMAGE_PATH, filePath);
		properties.put(SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND, filePathKind);
	}

	public static Image getIconImage(Map<String, Object> properties) {
		Image result;
		result = (Image) properties.get(SwingSpecificProperty.KEY_ICON_IMAGE);
		if (result != null) {
			return result;
		}
		URL imageUrl;
		String imagePath = (String) properties.get(SwingSpecificProperty.KEY_ICON_IMAGE_PATH);
		String pathKind = (String) properties.get(SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
		if (imagePath == null) {
			return null;
		}
		if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
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
		properties.put(SwingSpecificProperty.KEY_ICON_IMAGE, image);		
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
				URL resourceURL = SwingSpecificProperty.class.getClassLoader().getResource(candidateResourcePath);
				if (resourceURL != null) {
					result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE,
							candidateResourcePath));
					break;
				}
			}
		}
		{
			File currentDir = new File(".");
			if (FileUtils.isAncestor(currentDir, file)) {
				File relativeFile = FileUtils.relativizeFile(currentDir, file);
				result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE,
						relativeFile.getPath()));
			}
		}
		{
			result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE,
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
			if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE.equals(pathKind)) {
				return "<absolute-file> " + path;
			} else if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE.equals(pathKind)) {
				return "<relative-file> " + path;
			} else if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
				return CLASSPATH_RESOURCE_PREFIX + path;
			} else {
				return "<illegal-path-kind: " + pathKind + "> " + path;
			}
		}
	}

	

}