package xy.reflect.ui.info.type.factory;

import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ControlPluginActivationFactory extends InfoProxyFactory {

	protected String pluginId;
	protected Object pluginConfiguration;

	public ControlPluginActivationFactory(String pluginId, Object pluginConfiguration) {
		this.pluginId = pluginId;
		this.pluginConfiguration = pluginConfiguration;
	}

	@Override
	protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
		result.put(IFieldControlPlugin.CHOSEN_PROPERTY_KEY, pluginId);
		if (pluginConfiguration != null) {
			result.put(pluginId, ReflectionUIUtils.serializeToHexaText(pluginConfiguration));
		} else {
			result.remove(pluginId);
		}
		return result;
	}

}
