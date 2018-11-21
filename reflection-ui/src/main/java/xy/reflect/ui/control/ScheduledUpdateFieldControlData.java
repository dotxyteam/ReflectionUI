package xy.reflect.ui.control;

import java.util.concurrent.Future;

public abstract class ScheduledUpdateFieldControlData extends FieldControlDataProxy {

	protected Object scheduledFieldValue;
	protected boolean scheduling = false;

	protected abstract Future<?> scheduleUpdate(Runnable updateJob);

	public ScheduledUpdateFieldControlData(IFieldControlData base) {
		super(base);
	}

	@Override
	public Object getValue() {
		if (scheduling) {
			return scheduledFieldValue;
		} else {
			return base.getValue();
		}
	}

	@Override
	public void setValue(final Object newValue) {
		scheduledFieldValue = newValue;
		scheduling = true;
		scheduleUpdate(new Runnable() {
			@Override
			public void run() {
				try {
					base.setValue(scheduledFieldValue);
				} finally {
					scheduling = false;
				}
			}
		});
	}

}
