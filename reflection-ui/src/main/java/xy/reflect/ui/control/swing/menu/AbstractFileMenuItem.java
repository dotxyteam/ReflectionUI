
package xy.reflect.ui.control.swing.menu;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlInput;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowser;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.StandardActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Base class for persistence (open, save, save as) menu items.
 * 
 * @author olitank
 *
 */
public abstract class AbstractFileMenuItem extends AbstractStandardActionMenuItem {

	private static final long serialVersionUID = 1L;

	protected static Map<Form, File> lastFileByForm = MiscUtils.newWeakKeysIdentityBasedMap();
	protected static Map<Form, Long> lastPersistedVersionByForm = MiscUtils.newWeakKeysIdentityBasedMap();

	protected FileBrowserConfiguration fileBrowserConfiguration;

	protected abstract void persist(SwingRenderer swingRenderer, Form form, File file);

	public AbstractFileMenuItem(SwingRenderer swingRenderer, Form menuBarOwner,
			StandardActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, menuBarOwner, menuItemInfo);
		fileBrowserConfiguration = (menuItemInfo.getFileBrowserConfiguration() != null)
				? new FileBrowserConfiguration(menuItemInfo.getFileBrowserConfiguration())
				: null;
		check();
	}

	protected void check() {
		if (fileBrowserConfiguration == null) {
			throw new ReflectionUIError();
		}
		Object object = getContextForm().getObject();
		ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		if (!type.canPersist()) {
			throw new ReflectionUIError("Type '" + type.getName() + "' cannot persist its instances state");
		}
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
	protected boolean isActive() {
		return true;
	}

	protected File retrieveFile() {
		final File[] fileHolder = new File[1];
		final FileBrowserPlugin fileBrowserPlugin = new FileBrowserPlugin();
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
						return new InfoProxyFactory() {

							@Override
							protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
								Map<String, Object> result = super.getSpecificProperties(type);
								result = new HashMap<String, Object>(result);
								result = fileBrowserPlugin.storeControlCustomization(fileBrowserConfiguration, result);
								return result;
							}

						}.wrapTypeInfo(
								swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(File.class, null)));
					}

				};
			}

		};
		FileBrowser fileBrowser = fileBrowserPlugin.createControl(swingRenderer, fileBrowserInput);
		fileBrowser.openDialog(menuBarOwner);
		File result = fileHolder[0];
		return result;
	}

	@Override
	public void execute() {
		File file = retrieveFile();
		if (file == null) {
			return;
		}
		processFile(file);
	}

	protected void processFile(File file) {
		Form form = getContextForm();
		try {
			persist(swingRenderer, form, file);
			ModificationStack modifStack = form.getModificationStack();
			lastPersistedVersionByForm.put(form, modifStack.getStateVersion());
			lastFileByForm.put(form, file);
		} catch (Throwable t) {
			lastPersistedVersionByForm.put(form, -1l);
			throw new ReflectionUIError(t);
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					SwingRendererUtils.updateWindowMenu(menuBarOwner, swingRenderer);
				}
			});
		}
	}

	public boolean isFileSynchronized() {
		ModificationStack modifStack = getContextForm().getModificationStack();
		Long lastSavedVersion = lastPersistedVersionByForm.get(getContextForm());
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

}
