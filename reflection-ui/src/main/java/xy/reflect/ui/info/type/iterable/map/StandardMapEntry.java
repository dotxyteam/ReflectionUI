package xy.reflect.ui.info.type.iterable.map;

import java.util.Map;
import java.util.Map.Entry;

import xy.reflect.ui.util.ReflectionUIUtils;

public class StandardMapEntry<K, V> {
	protected Entry<K, V> javaMapEntry;
	protected K alternateKey;
	protected V alternateValue;

	public StandardMapEntry(Map.Entry<K, V> javaMapEntry) {
		this.javaMapEntry = javaMapEntry;
	}

	public K getKey() {
		if (alternateKey != null) {
			return alternateKey;
		} else {
			return javaMapEntry.getKey();
		}
	}

	public void setKey(K key) {
		if (ReflectionUIUtils.equalsOrBothNull(key, javaMapEntry.getKey())) {
			alternateKey = null;
		} else {
			alternateKey = key;
		}
	}

	public V getValue() {
		if (alternateKey != null) {
			return alternateValue;
		} else {
			return javaMapEntry.getValue();
		}
	}

	public void setValue(V value) {
		if (alternateKey != null) {
			alternateValue = value;
		} else {
			javaMapEntry.setValue(value);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alternateKey == null) ? 0 : alternateKey.hashCode());
		result = prime * result + ((alternateValue == null) ? 0 : alternateValue.hashCode());
		result = prime * result + ((javaMapEntry == null) ? 0 : javaMapEntry.hashCode());
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
		StandardMapEntry<?,?> other = (StandardMapEntry<?,?>) obj;
		if (alternateKey == null) {
			if (other.alternateKey != null)
				return false;
		} else if (!alternateKey.equals(other.alternateKey))
			return false;
		if (alternateValue == null) {
			if (other.alternateValue != null)
				return false;
		} else if (!alternateValue.equals(other.alternateValue))
			return false;
		if (javaMapEntry == null) {
			if (other.javaMapEntry != null)
				return false;
		} else if (!javaMapEntry.equals(other.javaMapEntry))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getKey() + ": " + getValue();
	}

}