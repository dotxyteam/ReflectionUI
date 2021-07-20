


package xy.reflect.ui.info.type.iterable.item;

import java.io.Serializable;

/**
 * Allows to specify the preferences about item display of list types.
 * 
 * @author olitank
 *
 */
public interface IListItemDetailsAccessMode extends Serializable {

	/**
	 * @return whether there is typically an option to open a dialog displaying the
	 *         selected item details.
	 */
	boolean hasDetachedDetailsDisplayOption();

	/**
	 * @return whether there is typically an area in the current window that
	 *         displays the selected item details.
	 */
	boolean hasEmbeddedDetailsDisplayArea();

	/**
	 * @return the position of the details area according to the list component.
	 */
	ItemDetailsAreaPosition getEmbeddedDetailsAreaPosition();

	/**
	 * @return a ratio between 0.0 and 1.0 specifying the size of the embedded
	 *         details area according to the list component size. This ratio could
	 *         typically be changed by dragging manually a divider.
	 */
	double getDefaultEmbeddedDetailsAreaOccupationRatio();

}
