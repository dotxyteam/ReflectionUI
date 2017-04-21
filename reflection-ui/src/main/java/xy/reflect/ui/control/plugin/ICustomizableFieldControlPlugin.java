package xy.reflect.ui.control.plugin;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.customization.CustomizationTools;
import xy.reflect.ui.info.type.factory.InfoCustomizations;

public interface ICustomizableFieldControlPlugin extends IFieldControlPlugin {

	JMenuItem makeFieldCustomizerMenuItem(JButton customizer, FieldControlPlaceHolder fieldControlPlaceHolder,
			InfoCustomizations infoCustomizations, CustomizationTools customizationTools);

}
