package xy.reflect.ui.info.field;

import xy.reflect.ui.info.ICommonInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldInfo extends ICommonInfo{
		ITypeInfo getType();

		Object getValue(Object object);

		void setValue(Object object, Object value);

		boolean isNullable();

		boolean isReadOnly();
		
		InfoCategory getCategory();
	}

