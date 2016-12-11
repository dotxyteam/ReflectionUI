package xy.reflect.ui.control.swing;

import xy.reflect.ui.util.ReflectionUIError;

public interface IAdvancedFieldControl {
	boolean displayError(ReflectionUIError error);

	boolean showCaption(String caption);

	boolean refreshUI();

	boolean handlesModificationStackUpdate();

	Object getFocusDetails();
	
	void requestDetailedFocus(Object value);
	
	void validateSubForm() throws Exception;
}
