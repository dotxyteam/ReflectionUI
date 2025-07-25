
package xy.reflect.ui.info.menu;

import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.util.KeyboardShortcut;

/**
 * Base class of action menu item specifications.
 * 
 * @author olitank
 *
 */
public abstract class AbstractActionMenuItemInfo extends AbstractMenuItemInfo {

	protected ResourcePath iconImagePath;
	protected KeyboardShortcut keyboardShortcut;

	public AbstractActionMenuItemInfo(String name, ResourcePath iconImagePath) {
		super(name);
		this.iconImagePath = iconImagePath;
	}

	public AbstractActionMenuItemInfo() {
		super();
	}

	public ResourcePath getIconImagePath() {
		return iconImagePath;
	}

	public void setIconImagePath(ResourcePath iconImagePath) {
		this.iconImagePath = iconImagePath;
	}

	public KeyboardShortcut getKeyboardShortcut() {
		return keyboardShortcut;
	}

	public void setKeyboardShortcut(KeyboardShortcut keyboardShortcut) {
		this.keyboardShortcut = keyboardShortcut;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((iconImagePath == null) ? 0 : iconImagePath.hashCode());
		result = prime * result + ((keyboardShortcut == null) ? 0 : keyboardShortcut.hashCode());
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
		AbstractActionMenuItemInfo other = (AbstractActionMenuItemInfo) obj;
		if (iconImagePath == null) {
			if (other.iconImagePath != null)
				return false;
		} else if (!iconImagePath.equals(other.iconImagePath))
			return false;
		if (keyboardShortcut == null) {
			if (other.keyboardShortcut != null)
				return false;
		} else if (!keyboardShortcut.equals(other.keyboardShortcut))
			return false;
		return true;
	}

}
