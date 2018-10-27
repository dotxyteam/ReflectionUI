package xy.reflect.ui.info.type.iterable.item;

import java.io.Serializable;

/**
 * Allows to describe preferences about items display of list types.
 * 
 * @author olitank
 *
 */
public interface IListItemDetailsAccessMode extends Serializable {

	boolean hasDetailsDisplayOption();

	boolean hasDetailsDisplayArea();

	ItemDetailsAreaPosition getDetailsAreaPosition();

	double getDefaultDetailsAreaOccupationRatio();

}
