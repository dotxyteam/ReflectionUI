
package xy.reflect.ui.control;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;

/**
 * Control data proxy that queues and returns the provided values before
 * returning the underlying control data values.
 * 
 * @author olitank
 *
 */
public class BufferedFieldControlData extends FieldControlDataProxy {

	protected List<Object> buffer = new ArrayList<Object>();

	public BufferedFieldControlData(IFieldControlData base) {
		super(base);
	}

	@Override
	public Object getValue() {
		if (buffer.size() > 0) {
			Object value = buffer.remove(0);
			return ErrorOccurrence.rethrow(value);
		}
		return super.getValue();
	}

	@Override
	public void setValue(Object value) {
		if (buffer.size() > 0) {
			throw new ReflectionUIError("Cannot update buffered field control data value (" + this + ")");
		}
		super.setValue(value);
	}

	public void withInBuffer(Object value, Runnable runnable) {
		int initialBufferSize = buffer.size();
		buffer.add(value);
		try {
			runnable.run();
		} finally {
			if (buffer.size() < initialBufferSize) {
				throw new ReflectionUIError("Field control data value accessed at least once too often (" + this + ")");
			}
			while (buffer.size() > initialBufferSize) {
				buffer.remove(0);
			}
		}
	}

}
