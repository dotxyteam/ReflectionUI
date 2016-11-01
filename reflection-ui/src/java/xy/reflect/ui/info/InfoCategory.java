package xy.reflect.ui.info;

public class InfoCategory implements Comparable<InfoCategory> {

	protected String caption;
	protected int position;

	public InfoCategory(String caption, int position) {
		this.caption = caption;
		this.position = position;
	}

	public String getCaption() {
		return caption;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public int compareTo(InfoCategory o) {
		int result = new Integer(position).compareTo(o.position);
		if (result == 0) {
			result = caption.compareTo(o.caption);
		}
		return result;
	}

	@Override
	public int hashCode() {
		return position + caption.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InfoCategory)) {
			return false;
		}
		InfoCategory other = (InfoCategory) obj;
		if (position != other.position) {
			return false;
		}
		if (!caption.equals(other.caption)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return InfoCategory.class.getSimpleName() + " n°" + (position + 1)
				+ " - " + caption;
	}

}
