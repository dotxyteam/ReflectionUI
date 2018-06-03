package xy.reflect.ui.control.swing;

import xy.reflect.ui.info.menu.MenuModel;

public interface IAdvancedFieldControl {
	boolean displayError(String msg);

	boolean showsCaption();

	boolean refreshUI(boolean refreshStructure);

	boolean handlesModificationStackUpdate();

	void validateSubForm() throws Exception;

	void addMenuContribution(MenuModel menuModel);

	boolean requestCustomFocus();
}
