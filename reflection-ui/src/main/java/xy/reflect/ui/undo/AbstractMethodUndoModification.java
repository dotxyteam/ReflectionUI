package xy.reflect.ui.undo;

import java.util.Map;

import xy.reflect.ui.info.method.IMethodInfo;

public abstract class AbstractMethodUndoModification implements IModification {

	protected IMethodInfo method;
	protected Object object;
	protected Map<Integer, Object> valueByParameterPosition;

	protected abstract void revertMethod();

	public AbstractMethodUndoModification(Object object, IMethodInfo method,
			Map<Integer, Object> valueByParameterPosition) {
		this.object = object;
		this.method = method;
		this.valueByParameterPosition = valueByParameterPosition;
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
	public IModification applyAndGetOpposite(boolean refreshView) {
		revertMethod();
		return new IModification() {
			
			@Override
			public String getTitle() {
				return method.getCaption();
			}
			
			@Override
			public int getNumberOfUnits() {
				return 1;
			}
			
			@Override
			public IModification applyAndGetOpposite(boolean refreshView) {
				method.invoke(object, valueByParameterPosition);
				return AbstractMethodUndoModification.this;
			}
		};
	}
}
