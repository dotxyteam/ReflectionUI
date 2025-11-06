package xy.reflect.ui.util;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * By default, each {@link ITypeInfo} is either basic (based on a class) or
 * derived (based on another {@link ITypeInfo}). A derived {@link ITypeInfo} is
 * often associated with derived instances that are "virtual objects" linked to
 * the instances of the source basic {@link ITypeInfo} class. This interface
 * allows identify such a derived instance, its base instance and the associated
 * derived {@link ITypeInfo}. Note that such derived {@link ITypeInfo} are not
 * naturally associated with their derived instances. A
 * {@link PrecomputedTypeInstanceWrapper} is then used to create and maintain
 * this link.
 * 
 * @author olitank
 *
 */
public interface IDerivedInstance {

	/**
	 * @return the base instance (see {@link IDerivedInstance} documentation for
	 *         more information).
	 */
	Object getBaseInstance();

	/**
	 * @param object Any object that may be a derived instance wrapper.
	 * @return the derived instance wrapped in the given object if any, or null.
	 */
	static IDerivedInstance get(Object object) {
		if (object instanceof PrecomputedTypeInstanceWrapper) {
			PrecomputedTypeInstanceWrapper wrapper = (PrecomputedTypeInstanceWrapper) object;
			if (wrapper.getInstance() instanceof IDerivedInstance) {
				return (IDerivedInstance) wrapper.getInstance();
			}
		}
		return null;
	}

	/**
	 * @param object Any object that may be a derived instance wrapper.
	 * @return the derived type associated wit the derived instance wrapped in the
	 *         given object if any, or null.
	 */
	static ITypeInfo type(Object object) {
		if (object instanceof PrecomputedTypeInstanceWrapper) {
			PrecomputedTypeInstanceWrapper wrapper = (PrecomputedTypeInstanceWrapper) object;
			if (wrapper.getInstance() instanceof IDerivedInstance) {
				return wrapper.getPrecomputedType();
			}
		}
		return null;
	}

	/**
	 * @param object Any object that may be a derived instance wrapper.
	 * @return the base instance of the given stack of derived instances wrapped in
	 *         the given object if any, or null.
	 */
	static Object root(Object object) {
		IDerivedInstance derivedInstance = get(object);
		if (derivedInstance != null) {
			return root(derivedInstance.getBaseInstance());
		}
		return object;
	}
}
