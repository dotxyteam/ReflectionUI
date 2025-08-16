
package xy.reflect.ui.info.type.iterable.map;

import java.util.Arrays;
import java.util.Map;

import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;

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
	protected Class<?>[] genericTypeParameters;

	public StandardMapEntry() {
	}

	public StandardMapEntry(Object key, Object value, Class<?>[] genericTypeParameters) {
		this.key = key;
		this.value = value;
		if (genericTypeParameters != null) {
			if (genericTypeParameters.length != 2) {
				throw new ReflectionUIError("Invalid generic type parameter array (expected 2 items) for "
						+ StandardMapEntry.class.getName() + " constructor: " + genericTypeParameters);
			}
			{
				Class<?> keyType = genericTypeParameters[0];
				if (ClassUtils.isPrimitiveClass(keyType) && (key == null)) {
					throw new ReflectionUIError(
							"Invalid entry key provided: null (is not a valid for the primitive type "
									+ keyType.getName() + ")");
				}
				if ((key != null) && !((ClassUtils.isPrimitiveClass(keyType)
						&& ClassUtils.primitiveToWrapperClass(keyType).isInstance(key))
						|| (!ClassUtils.isPrimitiveClass(keyType) && keyType.isInstance(key)))) {
					throw new ReflectionUIError(
							"Invalid entry key provided (is not an instance of " + keyType + "): '" + key + "'");
				}
			}
			{
				Class<?> valueType = genericTypeParameters[1];
				if (ClassUtils.isPrimitiveClass(valueType) && (value == null)) {
					throw new ReflectionUIError(
							"Invalid entry value provided: null (is not a valid for the primitive type "
									+ valueType.getName() + ")");
				}
				if ((value != null) && !((ClassUtils.isPrimitiveClass(valueType)
						&& ClassUtils.primitiveToWrapperClass(valueType).isInstance(value))
						|| (!ClassUtils.isPrimitiveClass(valueType) && valueType.isInstance(value)))) {
					throw new ReflectionUIError(
							"Invalid entry value provided (is not an instance of " + valueType + "): '" + value + "'");
				}
			}
		}
		this.genericTypeParameters = genericTypeParameters;
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

	public Class<?>[] getGenericTypeParameters() {
		return genericTypeParameters;
	}

	public void setGenericTypeParameters(Class<?>[] genericTypeParameters) {
		this.genericTypeParameters = genericTypeParameters;
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
		result = prime * result + Arrays.hashCode(genericTypeParameters);
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
		if (!Arrays.equals(genericTypeParameters, other.genericTypeParameters))
			return false;
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
