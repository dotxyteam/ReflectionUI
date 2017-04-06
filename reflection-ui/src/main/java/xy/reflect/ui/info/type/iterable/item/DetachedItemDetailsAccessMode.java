package xy.reflect.ui.info.type.iterable.item;

public class DetachedItemDetailsAccessMode implements IListItemDetailsAccessMode {

	@Override
	public boolean hasDetailsDisplayOption() {
		return true;
	}

	@Override
	public boolean hasDetailsDisplayArea() {
		return false;
	}

	@Override
	public ItemDetailsAreaPosition getDetailsAreaPosition() {
		return null;
	}

	@Override
	public double getDefaultDetailsAreaOccupationRatio() {
		return 0;
	}

}
