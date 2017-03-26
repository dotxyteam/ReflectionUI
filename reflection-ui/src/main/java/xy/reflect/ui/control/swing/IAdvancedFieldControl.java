package xy.reflect.ui.control.swing;

public interface IAdvancedFieldControl {
	boolean displayError(String msg);

	boolean showCaption();

	boolean refreshUI();

	boolean handlesModificationStackUpdate();

	Object getFocusDetails();

	boolean requestDetailedFocus(Object value);

	void validateSubForm() throws Exception;
}
