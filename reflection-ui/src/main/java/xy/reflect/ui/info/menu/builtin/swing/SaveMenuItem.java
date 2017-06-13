package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;
import javax.swing.JPanel;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
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
		ModificationStack modifStack = ((SwingRenderer) renderer).getModificationStackByForm().get(form);
		Long savedVersion = lastPersistedVersionByForm.get((JPanel) form);
		if (savedVersion == null) {
			lastPersistedVersionByForm.put((JPanel) form, modifStack.getStateVersion());
			return false;
		}
		if (savedVersion.equals(modifStack.getStateVersion())) {
			return false;
		}
		return super.isEnabled(form, renderer);
	}

	@Override
	public String getName(final Object form, final Object renderer) {
		String result = super.getName(form, renderer);
		File file = lastFileByForm.get((JPanel) form);
		if (file != null) {
			result += " " + file.getPath();
		}
		return result;
	}

	@Override
	protected File retrieveFile(final SwingRenderer swingRenderer, JPanel form) {
		File file = lastFileByForm.get(form);
		if (file != null) {
			return file;
		}
		return super.retrieveFile(swingRenderer, form);
	}
}
