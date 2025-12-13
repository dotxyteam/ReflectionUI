
package xy.reflect.ui.info.menu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.KeyboardShortcut;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This class represents a standard menu item (eg: open, save, undo, ...)..
 * 
 * @author olitank
 *
 */
public class StandardActionMenuItemInfo extends AbstractActionMenuItemInfo {

	protected StandardActionType type;
	protected FileBrowserConfiguration fileBrowserConfiguration;
	protected ITypeInfo objectType;

	public StandardActionMenuItemInfo(String name, ResourcePath iconImagePath, StandardActionType type,
			KeyboardShortcut keyboardShortcut, FileBrowserConfiguration fileBrowserConfiguration,
			ITypeInfo objectType) {
		super(name, iconImagePath);
		this.type = type;
		this.keyboardShortcut = keyboardShortcut;
		this.fileBrowserConfiguration = fileBrowserConfiguration;
		this.objectType = objectType;
	}

	public StandardActionMenuItemInfo(String name, ResourcePath iconImagePath, StandardActionType type,
			KeyboardShortcut keyboardShortcut, ITypeInfo objectType) {
		this(name, iconImagePath, type, keyboardShortcut, null, objectType);
	}

	public StandardActionType getType() {
		return type;
	}

	public void setType(StandardActionType type) {
		this.type = type;
	}

	public FileBrowserConfiguration getFileBrowserConfiguration() {
		return fileBrowserConfiguration;
	}

	public void setFileBrowserConfiguration(FileBrowserConfiguration fileBrowserConfiguration) {
		this.fileBrowserConfiguration = fileBrowserConfiguration;
	}

	public ITypeInfo getObjectType() {
		return objectType;
	}

	public void setObjectType(ITypeInfo objectType) {
		this.objectType = objectType;
	}

	public enum StandardActionType {
		NEW, OPEN, SAVE, SAVE_AS, UNDO, REDO, RESET, HELP, EXIT
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
		StandardActionMenuItemInfo other = (StandardActionMenuItemInfo) obj;
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

	public static class FileBrowserConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;

		public List<FileNameFilterConfiguration> fileNameFilters = new ArrayList<FileNameFilterConfiguration>();
		public String actionTitle = "Select";
		public SelectionModeConfiguration selectionMode = SelectionModeConfiguration.FILES_AND_DIRECTORIES;

		@Override
		public String toString() {
			return "FilecontrolConfiguration [fileNameFilters=" + fileNameFilters + ", actionTitle=" + actionTitle
					+ ", selectionMode=" + selectionMode + "]";
		}

	}

	public static class FileNameFilterConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;

		public String description = "";
		public List<String> extensions = new ArrayList<String>();

		public void validate() {
			if (description.length() == 0) {
				throw new ReflectionUIError("Description is mandatory");
			}
			if (extensions.size() == 0) {
				throw new ReflectionUIError("At least 1 extension is mandatory");
			}
		}

		@Override
		public String toString() {
			return "FileNameFilter [description=" + description + ", extensions=" + extensions + "]";
		}
	}

	public static enum SelectionModeConfiguration {
		FILES_AND_DIRECTORIES, FILES_ONLY, DIRECTORIES_ONLY

	}

}
