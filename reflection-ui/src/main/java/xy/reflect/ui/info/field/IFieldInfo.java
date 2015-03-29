package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldInfo extends IInfo {
	IFieldInfo NULL_FIELD_INFO = new IFieldInfo() {

		@Override
		public String getName() {
			return "";
		}

		@Override
		public String getDocumentation() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public void setValue(Object object, Object value) {
		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public boolean isNullable() {
			return false;
		}

		@Override
		public Object getValue(Object object) {
			return "";
		}

		@Override
		public ITypeInfo getType() {
			return new DefaultTextualTypeInfo(new ReflectionUI(), String.class);
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}
	};

	ITypeInfo getType();

	Object getValue(Object object);

	void setValue(Object object, Object value);

	boolean isNullable();

	boolean isReadOnly();

	InfoCategory getCategory();
}
