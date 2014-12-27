package xy.reflect.ui.info.field;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldInfo extends IInfo{
		ITypeInfo getType();

		Object getValue(Object object);

		void setValue(Object object, Object value);

		boolean isNullable();

		boolean isReadOnly();
		
		InfoCategory getCategory();
	}

