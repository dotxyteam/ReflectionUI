package xy.reflect.ui.info.method;

import java.util.Map;

import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.IModification;

public abstract class AbstractMethodUndoModification implements IModification {

	private IMethodInfo method;
	private Object object;
	private Map<Integer, Object> valueByParameterPosition;

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
