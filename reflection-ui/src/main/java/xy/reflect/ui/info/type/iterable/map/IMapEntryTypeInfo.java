


package xy.reflect.ui.info.type.iterable.map;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This interface allows to specify UI-oriented properties of map entry types.
 * 
 * @author olitank
 *
 */
public interface IMapEntryTypeInfo extends ITypeInfo {

	/**
	 * @return the field information about the entry key.
	 */
	IFieldInfo getKeyField();

	/**
	 * @return the field information about the entry value.
	 */
	IFieldInfo getValueField();

}
