package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public interface IFieldInfo extends IInfo {

	public IFieldInfo NULL_FIELD_INFO = new IFieldInfo() {

		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(Object.class, null));

		@Override
		public String getName() {
			return "NULL_FIELD_INFO";
		}

		@Override
		public double getDisplayAreaHorizontalWeight() {
			return 1.0;
		}

		@Override
		public double getDisplayAreaVerticalWeight() {
			return 1.0;
		}

		@Override
		public boolean isHidden() {
			return false;
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
		public boolean isNullValueDistinct() {
			return false;
		}

		@Override
		public String getNullValueLabel() {
			return null;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.INDETERMINATE;
		}

		@Override
		public Object getValue(Object object) {
			return null;
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			return null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public ITypeInfo getType() {
			return type;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public boolean isFormControlMandatory() {
			return false;
		}

		@Override
		public boolean isFormControlEmbedded() {
			return false;
		}

		@Override
		public IInfoFilter getFormControlFilter() {
			return IInfoFilter.DEFAULT;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String toString() {
			return "NULL_FIELD_INFO";
		}

		@Override
		public long getAutoUpdatePeriodMilliseconds() {
			return -1;
		}

	};

	ITypeInfo getType();

	Object getValue(Object object);

	Object[] getValueOptions(Object object);

	void setValue(Object object, Object value);

	Runnable getNextUpdateCustomUndoJob(Object object, Object newValue);

	boolean isNullValueDistinct();

	boolean isGetOnly();

	String getNullValueLabel();

	ValueReturnMode getValueReturnMode();

	InfoCategory getCategory();

	boolean isFormControlMandatory();

	boolean isFormControlEmbedded();

	IInfoFilter getFormControlFilter();

	long getAutoUpdatePeriodMilliseconds();

	boolean isHidden();

	double getDisplayAreaHorizontalWeight();

	double getDisplayAreaVerticalWeight();

}
