


package xy.reflect.ui.control.plugin;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Base class of simple field control plugins that can be configured.
 * 
 * @author olitank
 *
 */
public abstract class AbstractSimpleCustomizableFieldControlPlugin extends AbstractSimpleFieldControlPlugin
		implements ICustomizableFieldControlPlugin {

	@Override
	public Map<String, Object> storeControlCustomization(AbstractConfiguration controlConfiguration,
			Map<String, Object> specificProperties) {
		specificProperties = new HashMap<String, Object>(specificProperties);
		ReflectionUIUtils.setFieldControlPluginConfiguration(specificProperties, getIdentifier(), controlConfiguration);
		return specificProperties;
	}

	@Override
	public AbstractConfiguration getControlCustomization(Map<String, Object> specificProperties) {
		AbstractConfiguration result = loadControlCustomization(specificProperties);
		if (result == null) {
			result = getDefaultControlCustomization();
		}
		return result;
	}

	public AbstractConfiguration loadControlCustomization(Map<String, Object> specificProperties) {
		return (AbstractConfiguration) ReflectionUIUtils.getFieldControlPluginConfiguration(specificProperties,
				getIdentifier());
	}

	public AbstractConfiguration loadControlCustomization(IFieldControlInput input) {
		AbstractConfiguration result = loadControlCustomization(
				input.getControlData().getType().getSpecificProperties());
		if (result == null) {
			result = getDefaultControlCustomization();
		}
		return result;
	}

	public static abstract class AbstractConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public int hashCode() {
			return Arrays.hashCode(IOUtils.serializeToBinary(this));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			if (!Arrays.equals(IOUtils.serializeToBinary(obj), IOUtils.serializeToBinary(this)))
				return false;
			return true;
		}

	}

}
