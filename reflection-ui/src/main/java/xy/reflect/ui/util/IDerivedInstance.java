package xy.reflect.ui.util;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows the identification of derived instances, which are
 * utility objects normally used to present values associated with other objects
 * in a different way. These derived instances therefore should not store any
 * data and so that they can be recreated from the base objects at any time.
 * 
 * This interface also allows to identify the associated derived
 * {@link ITypeInfo} and the base object/instance.
 * 
 * Note that such derived instances are not naturally associated with their
 * derived {@link ITypeInfo}. A {@link PrecomputedTypeInstanceWrapper} is then
 * used to create and maintain this link.
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
