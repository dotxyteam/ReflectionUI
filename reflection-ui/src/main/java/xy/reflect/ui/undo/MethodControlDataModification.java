
package xy.reflect.ui.undo;

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Modification that invokes a method.
 * 
 * @author olitank
 *
 */
public class MethodControlDataModification extends AbstractModification {

	protected IMethodControlData data;
	protected InvocationData invocationData;

	public MethodControlDataModification(IMethodControlData data, InvocationData invocationData) {
		this.data = data;
		this.invocationData = invocationData;
	}

	@Override
	protected Runnable createDoJob() {
		return ReflectionUIUtils.createInvocationJob(data, invocationData);
	}

	@Override
	protected Runnable createUndoJob() {
		Runnable result = data.getLastFormRefreshStateRestorationJob();
		if(result != null) {
			return result;
		}
		return data.getNextInvocationUndoJob(invocationData);
	}

	@Override
	protected Runnable createRedoJob() {
		Runnable result = data.getLastFormRefreshStateRestorationJob();
		if(result != null) {
			return result;
		}
		return ReflectionUIUtils.getPreviousInvocationCustomOrDefaultRedoJob(data, invocationData);
	}

	@Override
	public String getTitle() {
		return getTitle(data.getCaption());
	}

	public static String getTitle(String methodCaption) {
		if ((methodCaption == null) || (methodCaption.length() == 0)) {
			return null;
		}
		return "Execute '" + methodCaption + "'";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((invocationData == null) ? 0 : invocationData.hashCode());
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
		MethodControlDataModification other = (MethodControlDataModification) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (invocationData == null) {
			if (other.invocationData != null)
				return false;
		} else if (!invocationData.equals(other.invocationData))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodControlDataModification [data=" + data + ", invocationData=" + invocationData + "]";
	}

}
