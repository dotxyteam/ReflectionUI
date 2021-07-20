


package xy.reflect.ui.info.menu;

/**
 * Base class of menu item specifications.
 * 
 * @author olitank
 *
 */
public abstract class AbstractMenuItemInfo extends AbstractMenuElementInfo {
	protected String caption = "";

	public AbstractMenuItemInfo(String caption) {
		super();
		this.caption = caption;
	}

	public AbstractMenuItemInfo() {
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((caption == null) ? 0 : caption.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMenuItemInfo other = (AbstractMenuItemInfo) obj;
		if (caption == null) {
			if (other.caption != null)
				return false;
		} else if (!caption.equals(other.caption))
			return false;
		return true;
	}

}
