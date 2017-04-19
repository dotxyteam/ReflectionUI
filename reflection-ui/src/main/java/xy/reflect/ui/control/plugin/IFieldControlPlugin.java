package xy.reflect.ui.control.plugin;

import java.awt.Component;

import xy.reflect.ui.control.IFieldControlInput;

public interface IFieldControlPlugin {

	boolean handles(IFieldControlInput input);

	Component createControl(Object renderer, IFieldControlInput input);
}
