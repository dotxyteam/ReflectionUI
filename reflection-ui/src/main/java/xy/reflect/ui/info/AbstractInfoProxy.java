package xy.reflect.ui.info;

public abstract class AbstractInfoProxy implements IInfo {

	@Override
	public int hashCode() {
		return ((getName() == null) ? 0 : getName().hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractInfoProxy other = (AbstractInfoProxy) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

}
