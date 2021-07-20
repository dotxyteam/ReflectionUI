


package xy.reflect.ui.info.type.factory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Factory that generates type proxies that should activate the specified field
 * control plugin.
 * 
 * @author olitank
 *
 */
public class ControlPluginActivationFactory extends InfoProxyFactory {

	protected String pluginId;
	protected Serializable pluginConfiguration;

	public ControlPluginActivationFactory(String pluginId, Serializable pluginConfiguration) {
		this.pluginId = pluginId;
		this.pluginConfiguration = pluginConfiguration;
	}

	@Override
	protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
		ReflectionUIUtils.setFieldControlPluginIdentifier(result, pluginId);
		ReflectionUIUtils.setFieldControlPluginConfiguration(result, pluginId, pluginConfiguration);
		return result;
	}

}
