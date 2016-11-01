package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;

public class InvokeMethodModification implements IModification {

	protected Runnable doJob;
	protected Runnable undoJob;
	protected String title;
	protected IMethodInfo method;

	public static InvokeMethodModification create(final Object object, final IMethodInfo method,
			final InvocationData invocationData) {
		String title = getTitle(method);
		Runnable doJob = new Runnable() {
			@Override
			public void run() {
				method.invoke(object, invocationData);
			}
		};
		Runnable undoJob = method.getUndoJob(object, invocationData);
		return new InvokeMethodModification(title, method, doJob, undoJob);
	}
	


	public static String getTitle(IMethodInfo method) {
		return  method.getCaption();
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
	public boolean isNull() {
		return false;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public IInfo getTarget() {
		return method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((doJob == null) ? 0 : doJob.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((undoJob == null) ? 0 : undoJob.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InvokeMethodModification other = (InvokeMethodModification) obj;
		if (doJob == null) {
			if (other.doJob != null)
				return false;
		} else if (!doJob.equals(other.doJob))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (undoJob == null) {
			if (other.undoJob != null)
				return false;
		} else if (!undoJob.equals(other.undoJob))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getTitle();
	}

}
