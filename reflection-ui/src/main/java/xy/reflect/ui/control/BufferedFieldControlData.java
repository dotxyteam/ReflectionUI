
package xy.reflect.ui.control;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;

/**
 * Control data proxy that provides the specified value during the execution of
 * {@link #returningValue(Object, Runnable)}. When the buffer is empty, the
 * underlying control data values are returned. Note that
 * {@link ErrorOccurrence} instances can be put in the buffer so that their
 * underlying exceptions will be thrown when accessing the data value.
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
			Object value = buffer.get(buffer.size() - 1);
			return ErrorOccurrence.rethrow(value);
		}
		return super.getValue();
	}

	@Override
	public void setValue(Object value) {
		if (buffer.size() > 0) {
			throw new ReflectionUIError("Cannot update buffered field control data value while the buffer is not empty."
					+ "\n" + "Buffered values: " + buffer + "\n" + "Control data: " + this);
		}
		super.setValue(value);
	}

	public void returningValue(Object value, Runnable runnable) {
		buffer.add(value);
		try {
			runnable.run();
		} finally {
			buffer.remove(buffer.size() - 1);
		}
	}

}
