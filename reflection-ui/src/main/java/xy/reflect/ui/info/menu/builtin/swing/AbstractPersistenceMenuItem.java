package xy.reflect.ui.info.menu.builtin.swing;

import java.io.File;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowser;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.SelectionModeConfiguration;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractPersistenceMenuItem extends AbstractBuiltInActionMenuItem {

	private static final long serialVersionUID = 1L;

	protected static Map<JPanel, File> lastFileByForm = new MapMaker().weakKeys().makeMap();
	protected static Map<JPanel, Long> lastPersistedVersionByForm = new MapMaker().weakKeys().makeMap();

	protected FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();

	protected abstract void persist(SwingRenderer swingRenderer, JPanel form, File file);

	public AbstractPersistenceMenuItem() {
		fileBrowserConfiguration.selectionMode = SelectionModeConfiguration.FILES_ONLY;
	}

	public static Map<JPanel, File> getLastFileByForm() {
		return lastFileByForm;
	}

	public static Map<JPanel, Long> getLastPersistedVersionByForm() {
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
		Object object = swingRenderer.getObjectByForm().get(form);
		ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		if (!type.canPersist()) {
			throw new ReflectionUIError("Type '" + type.getName() + "' cannot persist its instances state");
		}
		return true;
	}

	protected File retrieveFile(final SwingRenderer swingRenderer, JPanel form) {
		final File[] fileHolder = new File[1];
		IFieldControlInput fileBrowserInput = new FieldControlInputProxy(IFieldControlInput.NULL_CONTROL_INPUT) {

			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {

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

				};
			}

		};
		FileBrowser browser = new FileBrowserPlugin().createControl(swingRenderer, fileBrowserInput,
				fileBrowserConfiguration);
		browser.openDialog(form);
		return fileHolder[0];
	}

	@Override
	public void execute(final Object form, final Object renderer) {
		File file = retrieveFile((SwingRenderer) renderer, (JPanel) form);
		if (file == null) {
			return;
		}
		lastFileByForm.put((JPanel) form, file);
		try {
			persist((SwingRenderer) renderer, (JPanel) form, file);
			ModificationStack modifStack = ((SwingRenderer) renderer).getModificationStackByForm().get(form);
			lastPersistedVersionByForm.put((JPanel) form, modifStack.getStateVersion());
		} catch (Throwable t) {
			lastPersistedVersionByForm.put((JPanel) form, -1l);
			throw new ReflectionUIError(t);
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					SwingRendererUtils.updateWindowMenu((JPanel) form, (SwingRenderer) renderer);
				}
			});
		}
	}

}