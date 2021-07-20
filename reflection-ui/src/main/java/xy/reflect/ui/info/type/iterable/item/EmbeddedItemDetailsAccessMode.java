


package xy.reflect.ui.info.type.iterable.item;

import javax.xml.bind.annotation.XmlElement;

/**
 * Implementation of {@link IListItemDetailsAccessMode} specifying that items
 * should be displayed in the same window as the list component, typically in an
 * embedded details area that contains the currently selected item.
 * 
 * @author olitank
 *
 */
public class EmbeddedItemDetailsAccessMode implements IListItemDetailsAccessMode {
	private static final long serialVersionUID = 1L;

	protected ItemDetailsAreaPosition embeddedDetailsAreaPosition = ItemDetailsAreaPosition.RIGHT;
	protected double defaultEmbeddedDetailsAreaOccupationRatio = 0.66;

	@Override
	public boolean hasDetachedDetailsDisplayOption() {
		return false;
	}

	@Override
	public boolean hasEmbeddedDetailsDisplayArea() {
		return true;
	}

	@Override
	@XmlElement(name = "detailsAreaPosition")
	public ItemDetailsAreaPosition getEmbeddedDetailsAreaPosition() {
		return embeddedDetailsAreaPosition;
	}

	public void setEmbeddedDetailsAreaPosition(ItemDetailsAreaPosition embeddedDetailsAreaPosition) {
		this.embeddedDetailsAreaPosition = embeddedDetailsAreaPosition;
	}

	@Override
	@XmlElement(name = "defaultDetailsAreaOccupationRatio")
	public double getDefaultEmbeddedDetailsAreaOccupationRatio() {
		return defaultEmbeddedDetailsAreaOccupationRatio;
	}

	public void setDefaultEmbeddedDetailsAreaOccupationRatio(double defaultEmbeddedDetailsAreaOccupationRatio) {
		this.defaultEmbeddedDetailsAreaOccupationRatio = defaultEmbeddedDetailsAreaOccupationRatio;
	}

}
