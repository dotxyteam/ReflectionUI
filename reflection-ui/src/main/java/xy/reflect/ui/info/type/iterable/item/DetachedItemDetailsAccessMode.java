


package xy.reflect.ui.info.type.iterable.item;

import javax.xml.bind.annotation.XmlElement;

/**
 * Implementation of {@link IListItemDetailsAccessMode} specifying that items
 * should be displayed in a separate window, typically a child modal dialog.
 * 
 * @author olitank
 *
 */
public class DetachedItemDetailsAccessMode implements IListItemDetailsAccessMode {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean hasDetachedDetailsDisplayOption() {
		return true;
	}

	@Override
	public boolean hasEmbeddedDetailsDisplayArea() {
		return false;
	}

	@Override
	@XmlElement(name = "detailsAreaPosition")
	public ItemDetailsAreaPosition getEmbeddedDetailsAreaPosition() {
		return null;
	}

	@Override
	@XmlElement(name = "defaultDetailsAreaOccupationRatio")
	public double getDefaultEmbeddedDetailsAreaOccupationRatio() {
		return 0.0;
	}

}
