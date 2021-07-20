


package xy.reflect.ui.info.menu;

import java.util.List;

/**
 * This interface is the base of every specification of menu model element that
 * has children.
 * 
 * @author olitank
 *
 */
public interface IMenuItemContainerInfo extends IMenuElementInfo {

	List<AbstractMenuItemInfo> getItems();

}
