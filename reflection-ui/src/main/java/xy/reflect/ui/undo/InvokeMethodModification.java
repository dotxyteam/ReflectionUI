package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;

public class InvokeMethodModification implements IModification {

	protected Runnable doJob;
	protected Runnable undoJob;
	protected String title;
	protected IMethodInfo method;

	public InvokeMethodModification(final Object object, final IMethodInfo method,
			final InvocationData invocationData) {
		this(method.getCaption(), method, new Runnable() {
			@Override
			public void run() {
				method.invoke(object, invocationData);
			}
		}, method.getUndoJob(object, invocationData));
	}

	public InvokeMethodModification(String title, IMethodInfo method, Runnable doJob, Runnable undoJob) {
		super();
		this.title = title;
		this.method = method;
		this.doJob = doJob;
		this.undoJob = undoJob;
	}

	@Override
	public InvokeMethodModification applyAndGetOpposite() {
		doJob.run();
		return new InvokeMethodModification(ModificationStack.getUndoTitle(title), method, undoJob, doJob);
	}

	@Override
	public int getNumberOfUnits() {
		return 1;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public IInfo getTarget() {
		return method;
	}

}
