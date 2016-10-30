package xy.reflect.ui.info.type.iterable.item;

public class EmbeddedItemDetailsAccessMode implements IListItemDetailsAccessMode {

	protected ItemDetailsAreaPosition detailsAreaPosition = ItemDetailsAreaPosition.RIGHT;
	protected double defaultDetailsAreaOccupationRatio = 0.66;

	@Override
	public boolean hasDetailsDisplayOption() {
		return false;
	}

	@Override
	public boolean hasDetailsDisplayArea() {
		return true;
	}

	public ItemDetailsAreaPosition getDetailsAreaPosition() {
		return detailsAreaPosition;
	}

	public void setDetailsAreaPosition(ItemDetailsAreaPosition detailsAreaPosition) {
		this.detailsAreaPosition = detailsAreaPosition;
	}

	public double getDefaultDetailsAreaOccupationRatio() {
		return defaultDetailsAreaOccupationRatio;
	}

	public void setDefaultDetailsAreaOccupationRatio(double defaultDetailsAreaOccupationRatio) {
		this.defaultDetailsAreaOccupationRatio = defaultDetailsAreaOccupationRatio;
	}

	

}
