package xy.reflect.ui.control.plugin;

import java.awt.Component;

import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.customization.CustomizationTools;
import xy.reflect.ui.info.type.factory.InfoCustomizations.FieldCustomization;

@SuppressWarnings("unused")
public interface ICustomizableFieldControlPlugin extends IFieldControlPlugin {

	JMenuItem makeFieldCustomizerMenuItem(Component customizedFormComponent, FieldCustomization fieldCustomization,
			CustomizationTools customizationTools);

}
