package xy.reflect.ui.info.field;

import java.util.Arrays;

public class BufferedValueOptionsFieldInfo extends FieldInfoProxy {

	private Object[] bufferedValueOptions;

	public BufferedValueOptionsFieldInfo(IFieldInfo base, Object[] bufferedValueOptions) {
		super(base);
		this.bufferedValueOptions = bufferedValueOptions;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		if (bufferedValueOptions != null) {
			Object[] result = bufferedValueOptions;
			bufferedValueOptions = null;
			return result;
		}
		return super.getValueOptions(object);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(bufferedValueOptions);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BufferedValueOptionsFieldInfo other = (BufferedValueOptionsFieldInfo) obj;
		if (!Arrays.equals(bufferedValueOptions, other.bufferedValueOptions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BufferedValueOptionsFieldInfo [bufferedValueOptions=" + Arrays.toString(bufferedValueOptions) + "]";
	}
	
	

}
