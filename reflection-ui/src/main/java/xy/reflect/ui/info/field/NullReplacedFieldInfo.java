package xy.reflect.ui.info.field;

/**
 * Field proxy that returns the given value instead of null.
 * 
 * @author olitank
 *
 */
public class NullReplacedFieldInfo extends FieldInfoProxy {

	private Object nullReplacement;

	public NullReplacedFieldInfo(IFieldInfo base, Object nullReplacement) {
		super(base);
		this.nullReplacement = nullReplacement;
	}

	@Override
	public Object getValue(Object object) {
		Object result = super.getValue(object);
		if (result == null) {
			result = nullReplacement;
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((nullReplacement == null) ? 0 : nullReplacement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		NullReplacedFieldInfo other = (NullReplacedFieldInfo) obj;
		if (nullReplacement == null) {
			if (other.nullReplacement != null)
				return false;
		} else if (!nullReplacement.equals(other.nullReplacement))
			return false;
		return true;
	}

}