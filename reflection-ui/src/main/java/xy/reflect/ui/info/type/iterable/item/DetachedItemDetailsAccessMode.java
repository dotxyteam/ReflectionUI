package xy.reflect.ui.info.type.iterable.item;

public class DetachedItemDetailsAccessMode implements IListItemDetailsAccessMode {
	private static final long serialVersionUID = 1L;

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
