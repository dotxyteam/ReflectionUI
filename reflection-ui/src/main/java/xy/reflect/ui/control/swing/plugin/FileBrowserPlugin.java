package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FileBrowserPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	public String getControlTitle() {
		return "File Browser";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return File.class.isAssignableFrom(javaType);
	}

	@Override
	protected boolean displaysDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlConfiguration() {
		return new FileBrowserConfiguration();
	}

	@Override
	public FileBrowser createControl(Object renderer, IFieldControlInput input,
			AbstractConfiguration controlConfiguration) {
		return new FileBrowser((SwingRenderer) renderer, input, (FileBrowserConfiguration) controlConfiguration);
	}

	public static class FileBrowserConfiguration extends AbstractConfiguration {
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

	public static class FileNameFilterConfiguration extends AbstractConfiguration {
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

	public class FileBrowser extends DialogAccessControl implements IAdvancedFieldControl {

		protected static final long serialVersionUID = 1L;

		protected boolean textChangedByUser = true;
		protected FileBrowserConfiguration controlConfiguration;

		public FileBrowser(SwingRenderer swingRenderer, IFieldControlInput input,
				FileBrowserConfiguration controlConfiguration) {
			super(swingRenderer, input);
			this.controlConfiguration = controlConfiguration;
		}

		@Override
		protected Component createStatusControl(IFieldControlInput input) {
			return new TextControl(swingRenderer, new FieldControlInputProxy(input) {

				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {

						@Override
						public void setValue(Object value) {
							if (value != null) {
								value = new File((String) value);
							}
							base.setValue(value);
						}

						@Override
						public Object getValue() {
							File currentFile = (File) base.getValue();
							if (currentFile == null) {
								return null;
							}
							return currentFile.getPath();
						}

						@Override
						public ITypeInfo getType() {
							return new DefaultTypeInfo(swingRenderer.getReflectionUI(), String.class);
						}
					};
				}

			});
		}

		@Override
		protected Component createChangeControl() {
			Component result = super.createChangeControl();
			if (data.isGetOnly()) {
				result.setEnabled(false);
			}
			return result;
		}

		protected void configureFileChooser(JFileChooser fileChooser, File currentFile) {
			if (currentFile != null) {
				fileChooser.setSelectedFile(currentFile.getAbsoluteFile());
			}
			if (controlConfiguration.selectionMode == SelectionModeConfiguration.FILES_AND_DIRECTORIES) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			} else if (controlConfiguration.selectionMode == SelectionModeConfiguration.FILES_ONLY) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			} else if (controlConfiguration.selectionMode == SelectionModeConfiguration.DIRECTORIES_ONLY) {
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			} else {
				throw new ReflectionUIError();
			}
			int i = 0;
			for (FileNameFilterConfiguration filter : controlConfiguration.fileNameFilters) {
				String swingFilterDescription = filter.description + "(*."
						+ ReflectionUIUtils.stringJoin(filter.extensions, ", *.") + ")";
				String[] swingFilterExtensions = filter.extensions.toArray(new String[filter.extensions.size()]);
				FileNameExtensionFilter newFileFilter = new FileNameExtensionFilter(swingFilterDescription,
						swingFilterExtensions);
				fileChooser.addChoosableFileFilter(newFileFilter);
				if (i == 0) {
					fileChooser.setFileFilter(newFileFilter);
				}
				i++;
			}
		}

		protected String getDialogTitle() {
			return controlConfiguration.actionTitle;
		}

		@Override
		public void openDialog(Component owner) {
			final JFileChooser fileChooser = new JFileChooser();
			File currentFile = (File) data.getValue();
			fileChooser.setCurrentDirectory(lastDirectory);
			configureFileChooser(fileChooser, currentFile);

			final DialogBuilder dialogBuilder = swingRenderer.getDialogBuilder(owner);
			fileChooser.setApproveButtonText(swingRenderer.prepareStringToDisplay(getDialogTitle()));
			fileChooser.rescanCurrentDirectory();
			final boolean[] ok = new boolean[] { false };
			fileChooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (e.getActionCommand().equals(javax.swing.JFileChooser.APPROVE_SELECTION)) {
						ok[0] = true;
						dialogBuilder.getCreatedDialog().dispose();
					} else if (e.getActionCommand().equals(javax.swing.JFileChooser.CANCEL_SELECTION)) {
						dialogBuilder.getCreatedDialog().dispose();
					}
				}
			});
			dialogBuilder.setContentComponent(fileChooser);
			swingRenderer.showDialog(dialogBuilder.createDialog(), true);
			if (!ok[0]) {
				return;
			}

			lastDirectory = fileChooser.getCurrentDirectory();
			data.setValue(fileChooser.getSelectedFile());
			updateControls();
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		@Override
		public boolean refreshUI() {
			updateControls();
			return true;
		}

		@Override
		public boolean handlesModificationStackUpdate() {
			return false;
		}

		@Override
		public String toString() {
			return "FileBrowser [data=" + data + "]";
		}

	}

}
