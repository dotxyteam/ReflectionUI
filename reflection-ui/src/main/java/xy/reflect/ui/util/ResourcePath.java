package xy.reflect.ui.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResourcePath {

	protected static final String CLASSPATH_RESOURCE_PREFIX = "<class-path-resource> ";
	protected static final String MEMORY_OBJECT_PREFIX = "<memory> ";
	protected static final int SELF_ALTERNATIVE_INDEX = 0;

	protected String path;
	protected PathKind pathKind;
	protected int chosenAlternativeIndex;

	public enum PathKind {
		CLASS_PATH_RESOURCE, ABSOLUTE_FILE, RELATIVE_FILE, MEMORY_OBJECT
	}

	public ResourcePath(String specification) {
		super();
		if (specification.startsWith(ResourcePath.CLASSPATH_RESOURCE_PREFIX)) {
			path = extractClassPathResourceValue(specification);
			pathKind = PathKind.CLASS_PATH_RESOURCE;
		} else if (specification.startsWith(ResourcePath.MEMORY_OBJECT_PREFIX)) {
			path = extractMemoryObjectValue(specification);
			pathKind = PathKind.MEMORY_OBJECT;
		} else {
			File file = new File(specification);
			path = file.getPath();
			pathKind = file.isAbsolute() ? PathKind.ABSOLUTE_FILE : PathKind.RELATIVE_FILE;
		}
		chosenAlternativeIndex = 0;
	}

	public static String formatClassPathResourceSpecification(String value) {
		return ResourcePath.CLASSPATH_RESOURCE_PREFIX + value;
	}

	public static String extractClassPathResourceValue(String specification) {
		return specification.substring(ResourcePath.CLASSPATH_RESOURCE_PREFIX.length());
	}

	public static String formatMemoryObjectSpecification(String value) {
		return ResourcePath.MEMORY_OBJECT_PREFIX + value;
	}

	public static String extractMemoryObjectValue(String specification) {
		return specification.substring(ResourcePath.MEMORY_OBJECT_PREFIX.length());
	}

	protected ResourcePath getChosen() {
		if (chosenAlternativeIndex == SELF_ALTERNATIVE_INDEX) {
			return this;
		} else {
			return getChosenAlternative();
		}
	}

	public String getSpecification() {
		ResourcePath chosen = getChosen();
		if (chosen.pathKind == PathKind.CLASS_PATH_RESOURCE) {
			return ResourcePath.formatClassPathResourceSpecification(chosen.path);
		} else if (chosen.pathKind == PathKind.MEMORY_OBJECT) {
			return ResourcePath.formatMemoryObjectSpecification(chosen.path);
		} else {
			return chosen.path;
		}
	}

	public PathKind getPathKind() {
		ResourcePath chosen = getChosen();
		return chosen.pathKind;
	}

	public void setFile(File file) {
		path = file.getPath();
		pathKind = file.isAbsolute() ? PathKind.ABSOLUTE_FILE : PathKind.RELATIVE_FILE;
		chosenAlternativeIndex = 0;
	}

	public ResourcePath getChosenAlternative() {
		List<ResourcePath> options = getAlternativeOptions();
		if ((chosenAlternativeIndex >= 0) && (chosenAlternativeIndex < options.size())) {
			return options.get(chosenAlternativeIndex);
		}
		return null;
	}

	public void setChosenAlternative(ResourcePath chosenAlternative) {
		List<ResourcePath> options = getAlternativeOptions();
		chosenAlternativeIndex = options.indexOf(chosenAlternative);
	}

	public List<ResourcePath> getAlternativeOptions() {
		List<ResourcePath> result = new ArrayList<ResourcePath>();
		if ((pathKind == PathKind.ABSOLUTE_FILE) || (pathKind == PathKind.RELATIVE_FILE)) {
			if (path.trim().length() > 0) {
				File file = new File(path);
				result.addAll(findMatchingClassPathResources(file));
				if (pathKind == PathKind.ABSOLUTE_FILE) {
					File currentDir = new File(".");
					if (FileUtils.isAncestor(currentDir, file)) {
						File relativeFile = FileUtils.relativizeFile(currentDir, file);
						result.add(new ResourcePath(relativeFile.getPath()));
					}
				}
				if (pathKind == PathKind.RELATIVE_FILE) {
					result.add(new ResourcePath(file.getAbsolutePath()));
				}
			}
		}
		result.add(SELF_ALTERNATIVE_INDEX, this);
		return result;
	}

	protected Collection<? extends ResourcePath> findMatchingClassPathResources(File file) {
		List<ResourcePath> result = new ArrayList<ResourcePath>();
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
			URL resourceURL = ResourcePath.class.getClassLoader().getResource(candidateResourcePath);
			if (resourceURL != null) {
				result.add(new ResourcePath(ResourcePath.CLASSPATH_RESOURCE_PREFIX + candidateResourcePath));
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chosenAlternativeIndex;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((pathKind == null) ? 0 : pathKind.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourcePath other = (ResourcePath) obj;
		if (chosenAlternativeIndex != other.chosenAlternativeIndex)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (pathKind != other.pathKind)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResourcePath [path=" + path + ", pathKind=" + pathKind + ", chosenAlternativeIndex="
				+ chosenAlternativeIndex + ", getSpecification()=" + getSpecification() + "]";
	}

}