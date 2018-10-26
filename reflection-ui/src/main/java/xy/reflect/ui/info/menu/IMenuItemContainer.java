package xy.reflect.ui.info.menu;

import java.util.List;

/**
 * This interface is the base of every menu model element that has children.
 * 
 * @author olitank
 *
 */
public interface IMenuItemContainer extends IMenuElement {

	List<AbstractMenuItem> getItems();

}
