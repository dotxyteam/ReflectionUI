package xy.reflect.ui.control;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Allows access, from abstract UI model methods
 * ({@link IFieldInfo#getValue(Object)},
 * {@link IMethodInfo#invoke(Object, InvocationData)}, ...), to information
 * about the display context of the current component. The implementations are
 * normally provided by the renderers.
 * 
 * @author olitank
 *
 */
public interface RenderingContext {

	/**
	 * @param type Any type information.
	 * @return The object that corresponds to the first compatible (of the given
	 *         type) object in the current display context. This object corresponds
	 *         either to the object of the current form, to the object of one of the
	 *         current ancestor forms, or to null if no compatible object is found.
	 *         If multiple compatible objects are found, the one from the form
	 *         highest in the hierarchy is returned.
	 */
	Object getCurrent(ITypeInfo type);

}
