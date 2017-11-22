package xy.reflect.ui.info.parameter;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IParameterInfo extends IInfo {

	IParameterInfo NULL_PARAMETER_INFO = new IParameterInfo() {

		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = new DefaultTypeInfo(reflectionUI, Object.class);

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
		public String getCaption() {
			return "";
		}

		@Override
		public boolean isNullValueDistinct() {
			return true;
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
		public Object getDefaultValue() {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	ITypeInfo getType();

	boolean isNullValueDistinct();

	Object getDefaultValue();

	int getPosition();

}
