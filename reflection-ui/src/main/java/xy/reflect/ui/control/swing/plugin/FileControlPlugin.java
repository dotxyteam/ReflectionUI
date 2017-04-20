package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
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
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class FileControlPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	protected static final File DEFAULT_FILE = new File("");

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	protected String getControlTitle() {
		return "File Control";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return File.class.isAssignableFrom(javaType);
	}

	@Override
	protected boolean handlesNull() {
		return true;
	}

	@Override
	protected AbstractCustomization getDefaultControlCustomization() {
		return new FileControlCustomization();
	}

	@Override
	protected Component createControl(Object renderer, IFieldControlInput input,
			AbstractCustomization controlCustomization) {
		if (input.getControlData().isValueNullable()) {
			input = new FieldControlInputProxy(input) {

				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {
						@Override
						public Object getValue() {
							Object result = super.getValue();
							if (result == null) {
								result = DEFAULT_FILE;
							}
							return result;
						}

						@Override
						public void setValue(Object value) {
							if (value.equals(DEFAULT_FILE)) {
								value = null;
							}
							super.setValue(value);
						}
					};
				}

			};
		}
		return new FileControl((SwingRenderer) renderer, input, (FileControlCustomization) controlCustomization);
	}

	public static class FileControlCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		public List<FileNameFilter> fileNameFilters = new ArrayList<FileNameFilter>();
		public String actionTitle = "Select";
		public SelectionMode selectionMode = SelectionMode.FILES_AND_DIRECTORIES;

		@Override
		public String toString() {
			return "FileControlCustomization [fileNameFilters=" + fileNameFilters + ", actionTitle=" + actionTitle
					+ ", selectionMode=" + selectionMode + "]";
		}

	}

	public static class FileNameFilter extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		public String description = "";
		public List<String> extensions = new ArrayList<String>();

		public void validate(){
			if(description.length() == 0){
				throw new ReflectionUIError("Description is mandatory");
			}
			if(extensions.size() == 0){
				throw new ReflectionUIError("At least 1 extension is mandatory");
			}
		}
		
		@Override
		public String toString() {
			return "FileNameFilter [description=" + description + ", extensions=" + extensions + "]";
		}
	}

	public static enum SelectionMode {
		FILES_AND_DIRECTORIES, FILES_ONLY, DIRECTORIES_ONLY

	}

	protected class FileControl extends DialogAccessControl implements IAdvancedFieldControl {

		protected static final long serialVersionUID = 1L;

		protected boolean textChangedByUser = true;
		protected FileControlCustomization controlCustomization;

		public FileControl(SwingRenderer swingRenderer, IFieldControlInput input,
				FileControlCustomization controlCustomization) {
			super(swingRenderer, input);
			this.controlCustomization = controlCustomization;
		}

		@Override
		protected TextControl createStatusControl(IFieldControlInput input) {
			return new TextControl(swingRenderer, new FieldControlInputProxy(input) {

				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {

						@Override
						public void setValue(Object value) {
							value = new File((String) value);
							base.setValue(value);
						}

						@Override
						public Object getValue() {
							File currentFile = (File) base.getValue();
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
		protected Component createButton() {
			Component result = super.createButton();
			if (data.isGetOnly()) {
				result.setEnabled(false);
			}
			return result;
		}

		protected void configureFileChooser(JFileChooser fileChooser, File currentFile) {
			if ((currentFile != null) && !currentFile.equals(DEFAULT_FILE)) {
				fileChooser.setSelectedFile(currentFile.getAbsoluteFile());
			}
			if (controlCustomization.selectionMode == SelectionMode.FILES_AND_DIRECTORIES) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			} else if (controlCustomization.selectionMode == SelectionMode.FILES_ONLY) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			} else if (controlCustomization.selectionMode == SelectionMode.DIRECTORIES_ONLY) {
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			} else {
				throw new ReflectionUIError();
			}
			for (FileNameFilter filter : controlCustomization.fileNameFilters) {
				String swingFilterDescription = filter.description + "(*."
						+ ReflectionUIUtils.stringJoin(filter.extensions, ", *.") + ")";
				String[] swingFilterExtensions = filter.extensions.toArray(new String[filter.extensions.size()]);
				fileChooser.addChoosableFileFilter(
						new FileNameExtensionFilter(swingFilterDescription, swingFilterExtensions));
			}
		}

		protected String getDialogTitle() {
			return controlCustomization.actionTitle;
		}

		@Override
		protected void openDialog() {
			final JFileChooser fileChooser = new JFileChooser();
			File currentFile = (File) data.getValue();
			fileChooser.setCurrentDirectory(lastDirectory);
			configureFileChooser(fileChooser, currentFile);
			int returnVal = fileChooser.showDialog(this, swingRenderer.prepareStringToDisplay(getDialogTitle()));
			if (returnVal != JFileChooser.APPROVE_OPTION) {
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
		public boolean requestDetailedFocus(Object focusDetails) {
			return SwingRendererUtils.requestAnyComponentFocus(statusControl, null, swingRenderer);
		}

		@Override
		public String toString() {
			return "FileControl [data=" + data + "]";
		}

	}

}
