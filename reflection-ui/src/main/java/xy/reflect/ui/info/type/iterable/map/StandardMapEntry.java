


package xy.reflect.ui.info.type.iterable.map;

import java.util.Map;

/**
 * This class allows to hold temporarily key-value pairs of standard maps
 * (assignable to {@link Map}).
 * 
 * @author olitank
 *
 */
public class StandardMapEntry implements Comparable<StandardMapEntry> {
	protected Object key;
	protected Object value;

	public StandardMapEntry(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return key + ": " + value;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public Object setValue(Object value) {
		Object oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareTo(StandardMapEntry that) {
		if (this.key == null) {
			if (that.key == null) {
				return 0; // equal
			} else {
				return -1; // null is before other values
			}
		} else {// this.member != null
			if (that.key == null) {
				return 1; // all other values are after null
			} else {
				return ((Comparable) this.key).compareTo((Comparable) that.key);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		StandardMapEntry other = (StandardMapEntry) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
