package xy.reflect.ui.control.input;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.undo.ModificationStack;

public class FieldControlInputProxy implements IFieldControlInput {

	protected IFieldControlInput base;

	public FieldControlInputProxy(IFieldControlInput base) {
		super();
		this.base = base;
	}

	public IFieldControlData getControlData() {
		return base.getControlData();
	}

	public IInfo getModificationsTarget() {
		return base.getModificationsTarget();
	}

	public ModificationStack getModificationStack() {
		return base.getModificationStack();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		FieldControlInputProxy other = (FieldControlInputProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlInputProxy [base=" + base + "]";
	}

}
