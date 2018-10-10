package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlInput;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowser;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.SelectionModeConfiguration;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractFileMenuItem extends AbstractBuiltInActionMenuItem {

	protected static Map<Form, File> lastFileByForm = new MapMaker().weakKeys().makeMap();
	protected static Map<Form, Long> lastPersistedVersionByForm = new MapMaker().weakKeys().makeMap();

	protected FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();

	protected abstract void persist(SwingRenderer swingRenderer, Form form, File file);

	public AbstractFileMenuItem() {
		fileBrowserConfiguration.selectionMode = SelectionModeConfiguration.FILES_ONLY;
	}

	public static Map<Form, File> getLastFileByForm() {
		return lastFileByForm;
	}

	public static Map<Form, Long> getLastPersistedVersionByForm() {
		return lastPersistedVersionByForm;
	}

	public FileBrowserConfiguration getFileBrowserConfiguration() {
		return fileBrowserConfiguration;
	}

	public void setFileBrowserConfiguration(FileBrowserConfiguration fileBrowserConfiguration) {
		this.fileBrowserConfiguration = fileBrowserConfiguration;
	}

	@Override
	public boolean isEnabled(Object form, Object renderer) {
		SwingRenderer swingRenderer = (SwingRenderer) renderer;
		Object object = ((Form) form).getObject();
		ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		if (!type.canPersist()) {
			throw new ReflectionUIError("Type '" + type.getName() + "' cannot persist its instances state");
		}
		return true;
	}

	protected File retrieveFile(final SwingRenderer swingRenderer, Form form) {
		final File[] fileHolder = new File[1];
		final FileBrowserPlugin browserPlugin = new FileBrowserPlugin();
		IFieldControlInput fileBrowserInput = new DefaultFieldControlInput(swingRenderer.getReflectionUI()) {

			@Override
			public IFieldControlData getControlData() {
				return new DefaultFieldControlData(swingRenderer.getReflectionUI()) {

					@Override
					public Object getValue() {
						return fileHolder[0];
					}

					@Override
					public void setValue(Object value) {
						fileHolder[0] = (File) value;
					}

					@Override
					public ITypeInfo getType() {
						return swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(File.class));
					}

					@Override
					public Map<String, Object> getSpecificProperties() {
						Map<String, Object> result = super.getSpecificProperties();
						result = new HashMap<String, Object>(result);
						browserPlugin.storeControlCustomization(fileBrowserConfiguration, result);
						return result;
					}

				};
			}

		};
		FileBrowser browser = browserPlugin.createControl(swingRenderer, fileBrowserInput);
		browser.openDialog(form);
		File result = fileHolder[0];
		return result;
	}

	@Override
	public void execute(final Object form, final Object renderer) {
		File file = retrieveFile((SwingRenderer) renderer, (Form) form);
		if (file == null) {
			return;
		}
		lastFileByForm.put((Form) form, file);
		try {
			persist((SwingRenderer) renderer, (Form) form, file);
			ModificationStack modifStack = ((Form) form).getModificationStack();
			lastPersistedVersionByForm.put((Form) form, modifStack.getStateVersion());
		} catch (Throwable t) {
			lastPersistedVersionByForm.put((Form) form, -1l);
			throw new ReflectionUIError(t);
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					SwingRendererUtils.updateWindowMenu((Form) form, (SwingRenderer) renderer);
				}
			});
		}
	}

}