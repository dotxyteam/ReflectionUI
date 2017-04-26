package xy.reflect.ui.control.plugin;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.customizer.CustomizationTools;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.custom.InfoCustomizations;

public interface ICustomizableFieldControlPlugin extends IFieldControlPlugin {

	JMenuItem makeFieldCustomizerMenuItem(JButton customizerButton, FieldControlPlaceHolder fieldControlPlaceHolder,
			InfoCustomizations infoCustomizations, CustomizationTools customizationTools);

}
