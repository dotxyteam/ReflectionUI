


package xy.reflect.ui.control.plugin;

import java.util.Map;

import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin.AbstractConfiguration;

/**
 * Field control plugin that can be configured. The configuration is stored in a
 * map.
 * 
 * @author olitank
 *
 */
public interface ICustomizableFieldControlPlugin extends IFieldControlPlugin {

	/**
	 * @return the default configuration of the plugin control.
	 */
	AbstractConfiguration getDefaultControlCustomization();

	/**
	 * @param specificProperties The plugin control configuration storage.
	 * @return the plugin control configuration loaded from the given map.
	 */
	AbstractConfiguration getControlCustomization(Map<String, Object> specificProperties);

	/**
	 * @param controlConfiguration The new plugin control configuration.
	 * @param specificProperties   The plugin control configuration storage.
	 * @return A new version of the given map with the plugin control configuration
	 *         updated with the specified value.
	 */
	Map<String, Object> storeControlCustomization(AbstractConfiguration controlConfiguration,
			Map<String, Object> specificProperties);

}
