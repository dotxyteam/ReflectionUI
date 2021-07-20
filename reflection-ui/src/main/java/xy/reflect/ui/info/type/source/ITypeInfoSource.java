


package xy.reflect.ui.info.type.source;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface is the bridge between objects and abstract UI models. It
 * allows to retrieve UI-oriented type information typically from Java classes.
 * An implementation of this interface must be created in order to plug
 * {@link ReflectionUI} to a new meta-data model (ex: a databse) and then
 * completely change the introspection mechanics.
 * 
 * @author olitank
 *
 */
public interface ITypeInfoSource {

	/**
	 * @return specificities identifier or null. Useful only if {@link CustomizedUI}
	 *         is the current abstract UI model generator.
	 */
	SpecificitiesIdentifier getSpecificitiesIdentifier();

	/**
	 * @return the type information generated from the current source object.
	 */
	ITypeInfo getTypeInfo();

}
