/*
 * 
 */
package xy.reflect.ui.info.field;

import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;

/**
 * Field proxy that returns the value extracted from the given storage instead
 * of null.
 * 
 * @author olitank
 *
 */
public class NullReplacementFieldInfo extends FieldInfoProxy {

	protected TextualStorage nullReplacementStorage;
	
	public NullReplacementFieldInfo(IFieldInfo base, TextualStorage nullReplacementStorage) {
		super(base);
		this.nullReplacementStorage = nullReplacementStorage;
	}

	@Override
	public Object getValue(Object object) {
		Object result = super.getValue(object);
		if (result == null) {
			result = nullReplacementStorage.load();
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((nullReplacementStorage == null) ? 0 : nullReplacementStorage.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		NullReplacementFieldInfo other = (NullReplacementFieldInfo) obj;
		if (nullReplacementStorage == null) {
			if (other.nullReplacementStorage != null)
				return false;
		} else if (!nullReplacementStorage.equals(other.nullReplacementStorage))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NullReplacementFieldInfo [nullReplacementStorage=" + nullReplacementStorage + ", base=" + base + "]";
	}

}