package xy.reflect.ui.info.parameter;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

/**
 * This interface allows to specify UI-oriented parameter properties.
 * 
 * @author olitank
 *
 */
public interface IParameterInfo extends IInfo {

	/**
	 * Dummy instance of this class made available for utilitarian purposes.
	 */
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

	/**
	 * @return UI-oriented type properties of the current parameter.
	 */
	ITypeInfo getType();

	/**
	 * @return true if and only if this parameter control must distinctly display
	 *         and allow to set the null value. This is usually needed if a null
	 *         value has a special meaning different from "empty/default value" for
	 *         the developer.
	 */
	boolean isNullValueDistinct();

	/**
	 * @param object
	 *            The object offering the method hosting this parameter.
	 * @return the default value of this parameter.
	 */
	Object getDefaultValue(Object object);

	/**
	 * @return the 0 based position of this parameter.
	 */
	int getPosition();

	/**
	 * @return true if and only if this parameter control is filtered out from the
	 *         display.
	 */
	boolean isHidden();

	/**
	 * @param object
	 *            The object offering the method hosting this parameter.
	 * @return options for value of this parameter or null if there is not any know
	 *         option.
	 */
	Object[] getValueOptions(Object object);

}
