package xy.reflect.ui.info.type.iterable.item;

import java.io.Serializable;

public interface IListItemDetailsAccessMode extends Serializable{

	boolean hasDetailsDisplayOption();

	boolean hasDetailsDisplayArea();

	ItemDetailsAreaPosition getDetailsAreaPosition();

	double getDefaultDetailsAreaOccupationRatio();

}
