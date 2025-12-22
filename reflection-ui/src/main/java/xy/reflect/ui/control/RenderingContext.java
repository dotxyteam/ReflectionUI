package xy.reflect.ui.control;

import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Allows access, from abstract UI model methods
 * ({@link IFieldInfo#getValue(Object)},
 * {@link IMethodInfo#invoke(Object, InvocationData)}, ...), to information
 * about the display context of the current component.
 * 
 * Note that the the contextual information is cached. If it cannot be found
 * locally, then its search is delegated to the parent
 * ({@link RenderingContext#getParent()}) (if any).
 * 
 * The implementations are normally provided by the renderers.
 * 
 * @author olitank
 *
 */
public abstract class RenderingContext {

	protected abstract Object findCurrentObjectLocally(ITypeInfo type);

	protected RenderingContext parent;
	protected Map<ITypeInfo, Object> currentObjectCache = createCurrentObjectCache();

	protected RenderingContext(RenderingContext parent) {
		this.parent = parent;
	}

	protected Map<ITypeInfo, Object> createCurrentObjectCache() {
		return new HashMap<ITypeInfo, Object>();
	}

	/**
	 * @return The parent {@link RenderingContext}.
	 */
	public RenderingContext getParent() {
		return parent;
	}

	/**
	 * @param type Any type information.
	 * @return The object that corresponds to the first compatible (of the given
	 *         type) object in the current display context. This object corresponds
	 *         either to the object of the current form, to the object of one of the
	 *         current ancestor forms, or to null if no compatible object is found.
	 *         If multiple compatible objects are found, the one from the form
	 *         highest in the hierarchy is returned.
	 */
	public Object getCurrentObject(ITypeInfo type) {
		synchronized (currentObjectCache) {
			if (currentObjectCache.containsKey(type)) {
				return currentObjectCache.get(type);
			} else {
				Object result = findCurrentObjectLocally(type);
				if (result == null) {
					if (parent != null) {
						result = parent.getCurrentObject(type);
					}
				}
				currentObjectCache.put(type, result);
				return result;
			}
		}
	}
}
