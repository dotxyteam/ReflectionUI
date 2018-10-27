package xy.reflect.ui.info.type.source;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface is the bridge between object and abstract UI models. An
 * implementation of this interface must be created in order to plug
 * {@link ReflectionUI} to a new meta-data model (ex: a databse) and then
 * completely change the introspection mechanics.
 * 
 * @author olitank
 *
 */
public interface ITypeInfoSource {

	/**
	 * @return specificities identifier or null. useful only if {@link CustomizedUI}
	 *         is used the current abstract UI model generator.
	 */
	SpecificitiesIdentifier getSpecificitiesIdentifier();

	/**
	 * @param reflectionUI
	 *            The current abstract UI model generator object.
	 * @return a UI-oriented type information object.
	 */
	ITypeInfo getTypeInfo(ReflectionUI reflectionUI);

}
