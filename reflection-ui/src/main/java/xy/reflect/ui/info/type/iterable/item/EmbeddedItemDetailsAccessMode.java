package xy.reflect.ui.info.type.iterable.item;

public class EmbeddedItemDetailsAccessMode implements IListItemDetailsAccessMode {

	protected ItemDetailsAreaPosition detailsAreaPosition = ItemDetailsAreaPosition.RIGHT;
	protected double detailsAreaOccupationRatio = 0.66;

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

	@Override
	public double getDetailsAreaOccupationRatio() {
		return detailsAreaOccupationRatio;
	}

	public void setDetailsAreaOccupationRatio(double detailsAreaOccupationRatio) {
		this.detailsAreaOccupationRatio = detailsAreaOccupationRatio;
	}

}
