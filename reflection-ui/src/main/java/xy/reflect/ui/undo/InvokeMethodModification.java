package xy.reflect.ui.undo;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;

public class InvokeMethodModification extends AbstractModification {

	protected Object object;
	protected IMethodInfo method;
	protected InvocationData invocationData;

	public static String getTitle(IMethodInfo method) {
		return method.getCaption();
	}

	public InvokeMethodModification(Object object, IMethodInfo method, InvocationData invocationData) {
		super(method);
		this.object = object;
		this.method = method;
		this.invocationData = invocationData;
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				method.invoke(object, invocationData);
			}
		};
	}

	@Override
	protected Runnable createUndoJob() {
		return method.getUndoJob(object, invocationData);
	}

	@Override
	public String getTitle() {
		return "Execute '" + method.getCaption() + "'";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((invocationData == null) ? 0 : invocationData.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
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
		if (invocationData == null) {
			if (other.invocationData != null)
				return false;
		} else if (!invocationData.equals(other.invocationData))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}

}
