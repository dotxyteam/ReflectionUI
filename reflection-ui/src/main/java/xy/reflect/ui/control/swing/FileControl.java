package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import xy.reflect.ui.control.input.FieldControlDataProxy;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class FileControl extends DialogAccessControl implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected boolean textChangedByUser = true;

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	public FileControl(SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, input);
	}

	@Override
	protected TextControl createStatusControl(IFieldControlInput input) {
		return new TextControl(swingRenderer, input) {

			private static final long serialVersionUID = 1L;

			@Override
			protected IFieldControlData retrieveData() {
				return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {

					@Override
					public void setValue(Object value) {
						FileControl.this.data.setValue(new File((String) value));
					}

					@Override
					public boolean isGetOnly() {
						return FileControl.this.data.isGetOnly();
					}

					@Override
					public Object getValue() {
						File currentFile = (File) FileControl.this.data.getValue();
						return currentFile.getPath();
					}

					@Override
					public ITypeInfo getType() {
						return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
					}
				};
			}

		};
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
		if ((currentFile != null) && !currentFile.equals(FileTypeInfo.getDefaultFile())) {
			fileChooser.setSelectedFile(currentFile.getAbsoluteFile());
		}
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}

	protected String getDialogTitle() {
		return "Select";
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
