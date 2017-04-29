package xy.reflect.ui.info.menu;

public class ActionMenuItem extends AbstractMenuItem {

	private static final long serialVersionUID = 1L;

	protected Runnable action;

	public ActionMenuItem(String name, String iconImagePath, Runnable action) {
		super(name, iconImagePath);
		this.action = action;
	}

	public Runnable getAction() {
		return action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionMenuItem other = (ActionMenuItem) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActionMenuItem [name=" + name + ", action=" + action + "]";
	}

}
