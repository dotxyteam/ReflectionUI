package xy.reflect.ui.control.plugin;

import java.io.Serializable;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.custom.InfoCustomizations;

public interface ICustomizableFieldControlPlugin extends IFieldControlPlugin {

	JMenuItem makeFieldCustomizerMenuItem(JButton customizerButton, FieldControlPlaceHolder fieldControlPlaceHolder,
			InfoCustomizations infoCustomizations, ICustomizationTools customizationTools);

	Serializable getDefaultControlCustomization();

}
