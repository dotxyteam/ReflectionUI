package xy.reflect.ui.util;

import java.rmi.server.UID;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.plugin.ICustomizableFieldControlPlugin;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;

public class SwingCustomizerUtils {
	public static final ImageIcon CUSTOMIZATION_ICON = new ImageIcon(
			ReflectionUI.class.getResource("resource/custom.png"));

	public static <T> void replaceItem(List<T> list, T t1, T t2) {
		int index = list.indexOf(t1);
		list.set(index, t2);
	}

	public static String getUniqueID() {
		return new UID().toString();
	}

	public static void setCurrentFieldControlPlugin(CustomizedSwingRenderer swingCustomizer,
			Map<String, Object> specificProperties, IFieldControlPlugin plugin) {
		String lastPluginId = (String) specificProperties.remove(IFieldControlPlugin.CHOSEN_PROPERTY_KEY);
		if (lastPluginId != null) {
			IFieldControlPlugin lastPlugin = SwingRendererUtils.findFieldControlPlugin(swingCustomizer, lastPluginId);
			if (lastPlugin instanceof ICustomizableFieldControlPlugin) {
				((ICustomizableFieldControlPlugin) lastPlugin).cleanUpCustomizations(specificProperties);
			}
		}
		if (plugin != null) {
			specificProperties.put(IFieldControlPlugin.CHOSEN_PROPERTY_KEY, plugin.getIdentifier());
			if (plugin instanceof ICustomizableFieldControlPlugin) {
				((ICustomizableFieldControlPlugin) plugin).setUpCustomizations(specificProperties);
			}
		}
	}
}
