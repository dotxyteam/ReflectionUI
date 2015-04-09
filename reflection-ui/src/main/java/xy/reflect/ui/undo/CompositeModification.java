package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CompositeModification implements IModification {

	protected IModification[] modifications;
	private String title;
	private UndoOrder undoOrder;

	public CompositeModification(String title, UndoOrder undoOrder,
			IModification... modifications) {
		this.title = title;
		this.undoOrder = undoOrder;
		this.modifications = modifications;
	}

	@Override
	public int getNumberOfUnits() {
		int result = 0;
		for (IModification modif : modifications) {
			result += modif.getNumberOfUnits();
		}
		return result;
	}

	public CompositeModification(String title, UndoOrder undoOrder,
			List<IModification> modifications) {
		this(title, undoOrder, modifications
				.toArray(new IModification[modifications.size()]));
	}

	@Override
	public IModification applyAndGetOpposite(boolean refreshView) {
		List<IModification> oppositeModifications = new ArrayList<IModification>();
		for (IModification modif : modifications) {
			if (undoOrder == UndoOrder.LIFO) {
				oppositeModifications.add(0,
						modif.applyAndGetOpposite(refreshView));
			} else if (undoOrder == UndoOrder.FIFO) {
				oppositeModifications.add(modif
						.applyAndGetOpposite(refreshView));
			} else {
				throw new ReflectionUIError();
			}
		}
		return new CompositeModification(ModificationStack.getUndoTitle(title),
				undoOrder, oppositeModifications);
	}

	@Override
	public String toString() {
		return getTitle();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		if (title != null) {
			return title;
		} else {
			return ReflectionUIUtils.stringJoin(Arrays.asList(modifications),
					", ");
		}
	}

	public IModification[] getModifications() {
		return modifications;
	}

	public void setModifications(IModification[] modifications) {
		this.modifications = modifications;
	}

	public UndoOrder getUndoOrder() {
		return undoOrder;
	}

	public void setUndoOrder(UndoOrder undoOrder) {
		this.undoOrder = undoOrder;
	}

}
