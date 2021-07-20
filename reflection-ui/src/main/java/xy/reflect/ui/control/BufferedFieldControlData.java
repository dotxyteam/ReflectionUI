


package xy.reflect.ui.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Control data proxy that stacks and returns the provided values before
 * returning the underlying control data values.
 * 
 * @author olitank
 *
 */
public class BufferedFieldControlData extends FieldControlDataProxy {

	protected List<Object> buffer = new ArrayList<Object>();

	public BufferedFieldControlData(IFieldControlData base, Object... values) {
		super(base);
		buffer.addAll(Arrays.asList(values));
	}

	@Override
	public Object getValue() {
		if (buffer.size() > 0) {
			Object nextValue = buffer.remove(0);
			return ErrorOccurrence.rethrow(nextValue);
		}
		return super.getValue();
	}

	@Override
	public void setValue(Object value) {
		buffer.clear();
		super.setValue(value);
	}

	public void addInBuffer(Object value) {
		buffer.add(value);
	}

}
