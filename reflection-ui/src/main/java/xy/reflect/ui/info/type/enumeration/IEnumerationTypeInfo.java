package xy.reflect.ui.info.type.enumeration;

import xy.reflect.ui.info.type.ITypeInfo;

public interface IEnumerationTypeInfo extends ITypeInfo {
	
	Object[] getPossibleValues();
	
	IEnumerationItemInfo getValueInfo(Object value);
	
	boolean isDynamicEnumeration();
}
