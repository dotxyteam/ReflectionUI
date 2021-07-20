


package xy.reflect.ui.info;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import xy.reflect.ui.util.IOUtils;

/**
 * This is a renderer-independent resource location class. It allows to specify
 * how to access a resource from the heap, the class-path or the file system.
 * 
 * It will also detect alternative resource location strategies if some exist
 * for the current resource.
 * 
 * Note that objects of this class are just specifications. The actual resource
 * access must be performed in another class.
 * 
 * @author olitank
 *
 */
public class ResourcePath implements Serializable {
	private static final long serialVersionUID = 1L;

	protected static final String CLASSPATH_RESOURCE_PREFIX = "<class-path-resource> ";
	protected static final String MEMORY_OBJECT_PREFIX = "<memory> ";
	protected static final int SELF_ALTERNATIVE_INDEX = 0;

	protected String path;
	protected PathKind pathKind;
	protected int chosenAlternativeIndex;

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		setSpecification(getSpecification());
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		setSpecification(getSpecification());
		out.defaultWriteObject();
	}

	/**
	 * Resource location strategy enumeration.
	 * 
	 * @author olitank
	 *
	 */
	public enum PathKind {
		CLASS_PATH_RESOURCE, ABSOLUTE_FILE, RELATIVE_FILE, MEMORY_OBJECT
	}

	/**
	 * Constructs an empty resource path. {@link #setSpecification(String)} can then
	 * be used to specify the resource location.
	 */
	public ResourcePath() {
		this("");
	}

	/**
	 * Constructs a class-path resource path.
	 * 
	 * @param classInResourcePackage A class in the same package of the resource.
	 * @param resourceName           The name of the resource.
	 */
	public ResourcePath(Class<?> classInResourcePackage, String resourceName) {
		this(ResourcePath.specifyClassPathResourceSpecification(
				classInResourcePackage.getPackage().getName().replace(".", "/") + "/" + resourceName));
	}

	/**
	 * Constructs a resource path from a specification. Specifications can be
	 * created using the static specify*Location(String) methods of this class.
	 * 
	 * @param specification The specification.
	 */
	public ResourcePath(String specification) {
		setSpecification(specification);
	}

	/**
	 * @param path A path that could be used as the argument of
	 *             {@link Class#getResource(String)}
	 * @return a class-path resource location specification string that can be
	 *         passed to the {@link #setSpecification(String)} method.
	 */
	public static String specifyClassPathResourceSpecification(String path) {
		return ResourcePath.CLASSPATH_RESOURCE_PREFIX + path;
	}

	/**
	 * @param path A arbitrary path that will uniquely identify a heap object.
	 * @return a heap resource location specification string that can be passed to
	 *         the {@link #setSpecification(String)} method.
	 */
	public static String specifyMemoryObjectSpecification(String path) {
		return ResourcePath.MEMORY_OBJECT_PREFIX + path;
	}

	/**
	 * @param specification The full resource location specification string.
	 * @return the path that was passed to the
	 *         {@link #specifyClassPathResourceSpecification(String)} method in
	 *         order to create the given resource location specification.
	 */
	public static String extractClassPathResourceLocation(String specification) {
		return specification.substring(ResourcePath.CLASSPATH_RESOURCE_PREFIX.length());
	}

	/**
	 * @param specification The full resource location specification string.
	 * @return the path that was passed to the
	 *         {@link #specifyMemoryObjectSpecification(String)} method in order to
	 *         create the given resource location specification.
	 */
	public static String extractMemoryObjectLocation(String specification) {
		return specification.substring(ResourcePath.MEMORY_OBJECT_PREFIX.length());
	}

	/**
	 * @return the raw path to resource. The format depends on the path kind.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * This method differs from {@link #getDefaultSpecification()} by returning an
	 * alternative specification if one was selected using the
	 * {@link #setChosenAlternative(ResourcePath)} method.
	 * 
	 * @return the chosen or default full resource location specification string.
	 */
	public String getSpecification() {
		return getChosen().getDefaultSpecification();
	}

	/**
	 * @return the full resource location specification string provided through the
	 *         {@link #setSpecification(String)} method.
	 */
	public String getDefaultSpecification() {
		if (pathKind == PathKind.CLASS_PATH_RESOURCE) {
			return ResourcePath.specifyClassPathResourceSpecification(path);
		} else if (pathKind == PathKind.MEMORY_OBJECT) {
			return ResourcePath.specifyMemoryObjectSpecification(path);
		} else {
			return path;
		}
	}

	/**
	 * Sets the full resource location specification string.
	 * 
	 * @param specification The full resource location specification string.
	 */
	public void setSpecification(String specification) {
		if (specification.startsWith(ResourcePath.CLASSPATH_RESOURCE_PREFIX)) {
			path = extractClassPathResourceLocation(specification);
			pathKind = PathKind.CLASS_PATH_RESOURCE;
		} else if (specification.startsWith(ResourcePath.MEMORY_OBJECT_PREFIX)) {
			path = extractMemoryObjectLocation(specification);
			pathKind = PathKind.MEMORY_OBJECT;
		} else {
			File file = new File(specification);
			path = file.getPath();
			pathKind = file.isAbsolute() ? PathKind.ABSOLUTE_FILE : PathKind.RELATIVE_FILE;
		}
		chosenAlternativeIndex = 0;
	}

	/**
	 * @return the current resource location strategy.
	 */
	public PathKind getPathKind() {
		ResourcePath chosen = getChosen();
		return chosen.pathKind;
	}

	/**
	 * Allows to specify the location of a file system resource.
	 * 
	 * @param file The file system resource path.
	 */
	public void setFile(File file) {
		path = file.getPath();
		pathKind = file.isAbsolute() ? PathKind.ABSOLUTE_FILE : PathKind.RELATIVE_FILE;
		chosenAlternativeIndex = 0;
	}

	/**
	 * @return the current or the chosen resource location alternative if one was
	 *         selected using the {@link #setChosenAlternative(ResourcePath)}
	 *         method.
	 */
	protected ResourcePath getChosen() {
		if (chosenAlternativeIndex == SELF_ALTERNATIVE_INDEX) {
			return this;
		} else {
			return getChosenAlternative();
		}
	}

	/**
	 * @return the the chosen resource location alternative if one was selected
	 *         using the {@link #setChosenAlternative(ResourcePath)} method, or null
	 *         if no choice was made.
	 */
	@XmlTransient
	public ResourcePath getChosenAlternative() {
		List<ResourcePath> options = getAlternativeOptions();
		if ((chosenAlternativeIndex >= 0) && (chosenAlternativeIndex < options.size())) {
			return options.get(chosenAlternativeIndex);
		}
		return null;
	}

	/**
	 * Allows to select a resource location alternative.
	 * 
	 * @param chosenAlternative An alternative resource location. Must be included
	 *                          in the list returned by the
	 *                          {@link #getAlternativeOptions()} method.
	 */
	public void setChosenAlternative(ResourcePath chosenAlternative) {
		List<ResourcePath> options = getAlternativeOptions();
		chosenAlternativeIndex = options.indexOf(chosenAlternative);
	}

	/**
	 * @return the list of detected alternative resource locations, each one with a
	 *         different strategy.
	 */
	public List<ResourcePath> getAlternativeOptions() {
		List<ResourcePath> result = new ArrayList<ResourcePath>();
		if ((pathKind == PathKind.ABSOLUTE_FILE) || (pathKind == PathKind.RELATIVE_FILE)) {
			if (path.trim().length() > 0) {
				File file = new File(path);
				result.addAll(findMatchingClassPathResources(file));
				if (pathKind == PathKind.ABSOLUTE_FILE) {
					File currentDir = new File(".");
					if (IOUtils.isAncestor(currentDir, file)) {
						File relativeFile = IOUtils.relativizeFile(currentDir, file);
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
			candidateResourceFile = IOUtils.relativizeFile(mostAncestorFile, candidateResourceFile);
			String candidateResourcePath = candidateResourceFile.getPath().replaceAll("\\\\", "/");
			URL resourceURL = ResourcePath.class.getClassLoader().getResource(candidateResourcePath);
			if (resourceURL != null) {
				result.add(new ResourcePath(specifyClassPathResourceSpecification(candidateResourcePath)));
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
