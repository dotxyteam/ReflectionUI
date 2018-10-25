package xy.reflect.ui.info.parameter;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public interface IParameterInfo extends IInfo {

	IParameterInfo NULL_PARAMETER_INFO = new IParameterInfo() {

		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = new DefaultTypeInfo(reflectionUI, new JavaTypeInfoSource(Object.class, null));

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getName() {
			return "NULL_PARAMETER_INFO";
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public boolean isNullValueDistinct() {
			return false;
		}

		@Override
		public ITypeInfo getType() {
			return type;
		}

		@Override
		public int getPosition() {
			return 0;
		}

		@Override
		public Object getDefaultValue(Object object) {
			return null;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	ITypeInfo getType();

	boolean isNullValueDistinct();

	Object getDefaultValue(Object object);

	int getPosition();

	boolean isHidden();

	Object[] getValueOptions(Object object);

}
