


package xy.reflect.ui.info.menu;

import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.info.ResourcePath;

/**
 * This class represents a standard menu item (eg: open, save, undo, ...)..
 * 
 * @author olitank
 *
 */
public class StandradActionMenuItemInfo extends AbstractActionMenuItemInfo {

	private Type type;
	private FileBrowserConfiguration fileBrowserConfiguration;

	public StandradActionMenuItemInfo(String name, ResourcePath iconImagePath, Type type,
			FileBrowserConfiguration fileBrowserConfiguration) {
		super(name, iconImagePath);
		this.type = type;
		this.fileBrowserConfiguration = fileBrowserConfiguration;
	}

	public StandradActionMenuItemInfo(String name, ResourcePath iconImagePath, Type type) {
		this(name, iconImagePath, type, null);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public FileBrowserConfiguration getFileBrowserConfiguration() {
		return fileBrowserConfiguration;
	}

	public void setFileBrowserConfiguration(FileBrowserConfiguration fileBrowserConfiguration) {
		this.fileBrowserConfiguration = fileBrowserConfiguration;
	}

	public enum Type {
		OPEN, SAVE, SAVE_AS, UNDO, REDO, RESET, HELP, EXIT
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fileBrowserConfiguration == null) ? 0 : fileBrowserConfiguration.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		StandradActionMenuItemInfo other = (StandradActionMenuItemInfo) obj;
		if (fileBrowserConfiguration == null) {
			if (other.fileBrowserConfiguration != null)
				return false;
		} else if (!fileBrowserConfiguration.equals(other.fileBrowserConfiguration))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandradActionMenuItem [type=" + type + ", name=" + caption + "]";
	}

}
