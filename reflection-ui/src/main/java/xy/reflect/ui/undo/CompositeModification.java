


package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.util.ReflectionUIError;

/**
 * Modification composed of sub-modifications that are applied in the specified
 * order.
 * 
 * @author olitank
 *
 */
public class CompositeModification implements IModification {

	protected IModification[] modifications;
	protected String title;
	protected UndoOrder undoOrder;

	public CompositeModification(String title, UndoOrder undoOrder, IModification... modifications) {
		this.title = title;
		this.undoOrder = undoOrder;
		this.modifications = modifications;
	}

	public CompositeModification(String title, UndoOrder undoOrder, List<IModification> modifications) {
		this(title, undoOrder, modifications.toArray(new IModification[modifications.size()]));
	}

	@Override
	public boolean isNull() {
		for (IModification modif : modifications) {
			if (!modif.isNull()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isFake() {
		if (modifications.length == 0) {
			return false;
		}
		for (IModification modif : modifications) {
			if (!modif.isFake()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IModification applyAndGetOpposite() {
		List<IModification> oppositeModifications = new ArrayList<IModification>();
		for (IModification modif : modifications) {
			if (undoOrder == UndoOrder.getNormal()) {
				oppositeModifications.add(0, modif.applyAndGetOpposite());
			} else if (undoOrder == UndoOrder.getAbnormal()) {
				oppositeModifications.add(modif.applyAndGetOpposite());
			} else {
				throw new ReflectionUIError();
			}
		}
		return new CompositeModification(AbstractModification.getUndoTitle(title), undoOrder, oppositeModifications);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getTitle() {
		if (title != null) {
			return title;
		} else {
			List<String> result = new ArrayList<String>();
			for (IModification modif : modifications) {
				String modifTitle = modif.getTitle();
				if (modifTitle == null) {
					return null;
				}
				result.add(modifTitle);
			}
			if (result.size() == 0) {
				return null;
			} else if (result.size() == 1) {
				return result.get(0);
			} else {
				if (result.get(0).endsWith(", ...")) {
					return result.get(0);
				} else {
					return result.get(0) + ", ...";
				}
			}
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

	@Override
	public String toString() {
		return "CompositeModification [title=" + title + ", undoOrder=" + undoOrder + ", modifications="
				+ Arrays.toString(modifications) + "]";
	}

}
