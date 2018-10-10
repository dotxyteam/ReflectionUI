package xy.reflect.ui.control.plugin;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractCustomization;

public interface ICustomizationTools {

	void changeCustomizationFieldValue(AbstractCustomization customization, String fieldName, Object fieldValue);

	SwingRenderer getToolsRenderer();

}
