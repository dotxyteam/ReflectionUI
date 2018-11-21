package xy.reflect.ui.control;

import xy.reflect.ui.util.ReflectionUIError;

public abstract class ErrorHandlingFieldControlData extends FieldControlDataProxy {

	protected Object lastFieldValue;
	protected boolean lastFieldValueInitialized = false;
	protected Throwable lastValueUpdateError;

	protected abstract void displayError(Throwable t);

	public ErrorHandlingFieldControlData(IFieldControlData base) {
		super(base);
	}

	@Override
	public Object getValue() {
		try {
			if (lastValueUpdateError != null) {
				throw lastValueUpdateError;
			}
			lastFieldValue = super.getValue();
			lastFieldValueInitialized = true;
			displayError(null);
		} catch (final Throwable t) {
			if (!lastFieldValueInitialized) {
				throw new ReflectionUIError(t);
			} else {
				displayError(t);
			}
		}
		return lastFieldValue;

	}

	@Override
	public void setValue(Object newValue) {
		try {
			lastFieldValue = newValue;
			super.setValue(newValue);
			lastValueUpdateError = null;
		} catch (Throwable t) {
			lastValueUpdateError = t;
		}
	}
}
