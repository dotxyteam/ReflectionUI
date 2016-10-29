package xy.reflect.ui.control.swing;

import xy.reflect.ui.util.ReflectionUIError;

public interface IFieldControl {
	boolean displayError(ReflectionUIError error);

	boolean showCaption();

	boolean refreshUI();

	boolean handlesModificationStackUpdate();

	Object getFocusDetails();
	
	void requestDetailedFocus(Object value);
}
