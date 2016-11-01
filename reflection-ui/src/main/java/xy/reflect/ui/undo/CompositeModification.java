package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CompositeModification implements IModification {

	protected IModification[] modifications;
	protected String title;
	protected UndoOrder undoOrder;
	protected IInfo target;

	public CompositeModification(IInfo target, String title, UndoOrder undoOrder, IModification... modifications) {
		this.target = target;
		this.title = title;
		this.undoOrder = undoOrder;
		this.modifications = modifications;
	}
	
	public CompositeModification(IInfo target, String title, UndoOrder undoOrder, List<IModification> modifications) {
		this(target, title, undoOrder, modifications.toArray(new IModification[modifications.size()]));
	}

	

	@Override
	public IInfo getTarget() {
		return target;
	}

	@Override
	public boolean isNull() {
		for (IModification modif : modifications) {
			if(!modif.isNull()){
				return false;
			}
		}
		return true;
	}

	@Override
	public IModification applyAndGetOpposite() {
		List<IModification> oppositeModifications = new ArrayList<IModification>();
		for (IModification modif : modifications) {
			if (undoOrder == UndoOrder.LIFO) {
				oppositeModifications.add(0, modif.applyAndGetOpposite());
			} else if (undoOrder == UndoOrder.FIFO) {
				oppositeModifications.add(modif.applyAndGetOpposite());
			} else {
				throw new ReflectionUIError();
			}
		}
		return new CompositeModification(target, ModificationStack.getUndoTitle(title), undoOrder, oppositeModifications);
	}

	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(modifications);
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((undoOrder == null) ? 0 : undoOrder.hashCode());
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
		CompositeModification other = (CompositeModification) obj;
		if (!Arrays.equals(modifications, other.modifications))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (undoOrder != other.undoOrder)
			return false;
		return true;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		if (title != null) {
			return title;
		} else {
			return ReflectionUIUtils.stringJoin(Arrays.asList(modifications), ", ");
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
