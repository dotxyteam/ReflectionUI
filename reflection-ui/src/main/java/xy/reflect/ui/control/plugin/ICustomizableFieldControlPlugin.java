package xy.reflect.ui.control.plugin;

import java.awt.Component;

import javax.swing.JMenuItem;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizations;
import xy.reflect.ui.info.type.factory.InfoCustomizations.FieldCustomization;

public interface ICustomizableFieldControlPlugin extends IFieldControlPlugin {

	JMenuItem makeCustomizerMenuItem(Component activatorComponent, FieldCustomization fieldCustomization,
			SwingRenderer customizationToolsRenderer);

}
