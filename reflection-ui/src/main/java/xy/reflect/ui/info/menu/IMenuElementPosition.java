


package xy.reflect.ui.info.menu;

/**
 * This interface is the base of every specification of menu model element that
 * has a parent.
 * 
 * @author olitank
 *
 */
public interface IMenuElementPosition {

	IMenuElementPosition getParent();

	String getElementName();

	MenuElementKind getElementKind();

}
