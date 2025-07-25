
package xy.reflect.ui.info.type.source;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface is the bridge between objects and abstract UI models. It
 * allows to retrieve UI-oriented type information typically from Java classes.
 * An implementation of this interface must be created in order to plug
 * {@link ReflectionUI} to a new meta-data model (ex: a database) and then
 * completely change the introspection mechanics.
 * 
 * @author olitank
 *
 */
public interface ITypeInfoSource {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 */
	public static ITypeInfoSource NULL_TYPE_INFO_SOURCE = new ITypeInfoSource() {
		
		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return null;
		}
		
		@Override
		public ITypeInfo buildTypeInfo(ReflectionUI reflectionUI) {
			return ITypeInfo.NULL_BASIC_TYPE_INFO;
		}
	};

	/**
	 * @return specificities identifier or null. Useful only if {@link CustomizedUI}
	 *         is the current abstract UI model generator.
	 */
	SpecificitiesIdentifier getSpecificitiesIdentifier();

	/**
	 * @param reflectionUI The reflection-based user interface processor.
	 * @return the type information generated from the current source object.
	 */
	ITypeInfo buildTypeInfo(ReflectionUI reflectionUI);

}
