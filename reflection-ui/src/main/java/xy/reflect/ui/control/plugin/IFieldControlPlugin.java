package xy.reflect.ui.control.plugin;

import java.awt.Component;

import xy.reflect.ui.control.IFieldControlInput;

public interface IFieldControlPlugin {

	public String CHOSEN_PROPERTY_KEY = IFieldControlPlugin.class.getName() + ".CHOSEN";
	public String NONE_IDENTIFIER = IFieldControlPlugin.class.getName() + ".NONE";

	boolean handles(IFieldControlInput input);

	Component createControl(Object renderer, IFieldControlInput input);

	String getControlTitle();

	String getIdentifier();
}
