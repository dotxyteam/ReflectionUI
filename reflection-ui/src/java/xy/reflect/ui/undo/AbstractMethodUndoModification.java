package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;

public abstract class AbstractMethodUndoModification implements IModification {

	protected IMethodInfo method;
	protected Object object;
	protected InvocationData invocationData;

	protected abstract void revertMethod();

	public AbstractMethodUndoModification(Object object, IMethodInfo method,
			InvocationData invocationData) {
		this.object = object;
		this.method = method;
		this.invocationData = invocationData;
	}

	@Override
	public String getTitle() {
		return ModificationStack.getUndoTitle(method.getCaption());
	}

	@Override
	public int getNumberOfUnits() {
		return 1;
	}

	@Override
	public IModification applyAndGetOpposite() {
		revertMethod();
		return new IModification() {
			
			@Override
			public IInfo getTarget() {
				return method;
			}

			@Override
			public String getTitle() {
				return method.getCaption();
			}
			
			@Override
			public int getNumberOfUnits() {
				return 1;
			}
			
			@Override
			public IModification applyAndGetOpposite() {
				method.invoke(object, invocationData);
				return AbstractMethodUndoModification.this;
			}
		};
	}
}
