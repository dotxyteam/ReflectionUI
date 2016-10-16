package xy.reflect.ui.undo;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;

public class SetFieldValueModification implements IModification {

	protected ReflectionUI reflectionUI;
	private String title;
	protected Object object;
	protected IFieldInfo field;
	private Runnable doJob;
	private Runnable undoJob;

	public static SetFieldValueModification create(ReflectionUI reflectionUI, final Object object,
			final IFieldInfo field, final Object value) {
		String title = "Edit '" + field.getCaption() + "'";
		Runnable doJob = new Runnable() {
			@Override
			public void run() {
				field.setValue(object, value);
			}
		};
		Runnable undoJob = null;
		return new SetFieldValueModification(reflectionUI, title, object, field, doJob, undoJob);
	}

	public SetFieldValueModification(ReflectionUI reflectionUI, String title, Object object, IFieldInfo field, Runnable doJob,
			Runnable undoJob) {
		this.reflectionUI = reflectionUI;
		this.title = title;
		this.object = object;
		this.field = field;
		this.doJob = doJob;
		this.undoJob = undoJob;
	}

	@Override
	public IInfo getTarget() {
		return field;
	}

	@Override
	public int getNumberOfUnits() {
		return 1;
	}

	@Override
	public IModification applyAndGetOpposite() {
		if (undoJob == null) {
			final Object oldValue = field.getValue(object);
			undoJob = new Runnable() {
				@Override
				public void run() {
					field.setValue(object, oldValue);
				}
			};
		}
		doJob.run();
		return new SetFieldValueModification(reflectionUI, ModificationStack.getUndoTitle(getTitle()), object, field, undoJob,
				doJob);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return getTitle();
	}
}
