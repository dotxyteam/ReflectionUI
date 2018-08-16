package xy.reflect.ui.control.plugin;

import java.awt.Component;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;

public interface IFieldControlPlugin {

	public String CHOSEN_PROPERTY_KEY = IFieldControlPlugin.class.getName() + ".CHOSEN";

	boolean handles(IFieldControlInput input);

	boolean canDisplayDistinctNullValue();

	Component createControl(Object renderer, IFieldControlInput input);

	String getControlTitle();

	String getIdentifier();

	IFieldControlData filterDistinctNullValueControlData(IFieldControlData controlData);
}
