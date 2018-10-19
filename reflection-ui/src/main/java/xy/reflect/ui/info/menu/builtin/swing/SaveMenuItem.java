package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;

import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.undo.ModificationStack;

public class SaveMenuItem extends AbstractSaveMenuItem {

	protected static final long serialVersionUID = 1L;

	protected FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();

	public SaveMenuItem() {
		name = "Save";
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		if (isFileSynchronized((Form) form, (SwingRenderer) renderer)) {
			return false;
		}
		return super.isEnabled(form, renderer);
	}

	public boolean isFileSynchronized(Form form, SwingRenderer swingRenderer) {
		ModificationStack modifStack = form.getModificationStack();
		Long lastSavedVersion = lastPersistedVersionByForm.get(form);
		if (lastSavedVersion == null) {
			if (modifStack.getStateVersion() == 0) {
				return true;
			}
		} else {
			if (lastSavedVersion.equals(modifStack.getStateVersion())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName(final Object form, final Object renderer) {
		String result = super.getName(form, renderer);
		File file = lastFileByForm.get((Form) form);
		if (file != null) {
			result += " " + file.getPath();
		}
		return result;
	}

	@Override
	protected File retrieveFile(final SwingRenderer swingRenderer, Form form) {
		File file = lastFileByForm.get(form);
		if (file != null) {
			return file;
		}
		return super.retrieveFile(swingRenderer, form);
	}
}
