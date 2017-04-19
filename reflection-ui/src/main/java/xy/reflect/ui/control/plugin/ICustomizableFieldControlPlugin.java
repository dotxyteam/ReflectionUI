package xy.reflect.ui.control.plugin;

import javax.swing.JMenuItem;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizations;

public interface ICustomizableFieldControlPlugin extends IFieldControlPlugin {

	JMenuItem makeCustomizerMenuItem(ITypeInfo customizedFieldType, InfoCustomizations infoCustomizations);

}
