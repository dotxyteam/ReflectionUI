package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.StandardReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IFieldInfo extends IInfo {
	public IFieldInfo NULL_FIELD_INFO = new IFieldInfo() {

		@Override
		public String getName() {
			return "NULL_FIELD_INFO";
		}

		@Override
		public String getOnlineHelp() {
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
		public boolean isGetOnly() {
			return true;
		}

		@Override
		public boolean isNullable() {
			return true;
		}

		
		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.PROXY;
		}

		@Override
		public Object getValue(Object object) {
			return null;
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object object, Object value) {
			return null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public ITypeInfo getType() {
			return new DefaultTypeInfo(new StandardReflectionUI(), Object.class);
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String toString() {
			return "NULL_FIELD_INFO";
		}

	};

	ITypeInfo getType();

	Object getValue(Object object);

	Object[] getValueOptions(Object object);

	void setValue(Object object, Object value);

	Runnable getCustomUndoUpdateJob(Object object, Object value);

	boolean isNullable();

	boolean isGetOnly();

	ValueReturnMode getValueReturnMode();

	InfoCategory getCategory();
}
