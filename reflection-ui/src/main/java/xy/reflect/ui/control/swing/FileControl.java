package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class FileControl extends DialogAccessControl implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected boolean textChangedByUser = true;

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	public FileControl(SwingRenderer swingRenderer, IControlData data) {
		super(swingRenderer, data);
	}

	@Override
	protected TextControl createStatusControl() {
		return new TextControl(swingRenderer, new ControlDataProxy(IControlData.NULL_CONTROL_DATA) {

			@Override
			public void setValue(Object value) {
				data.setValue(new File((String) value));
			}

			@Override
			public boolean isGetOnly() {
				return data.isGetOnly();
			}

			@Override
			public Object getValue() {
				File currentFile = (File) data.getValue();
				return currentFile.getPath();
			}

			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
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
		int returnVal = fileChooser.showDialog(this,
				swingRenderer.prepareStringToDisplay(getDialogTitle()));
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		lastDirectory = fileChooser.getCurrentDirectory();
		data.setValue(fileChooser.getSelectedFile());
		updateControls();
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	@Override
	public boolean showCaption(String caption) {
		return false;
	}

	@Override
	public boolean refreshUI() {
		updateStatusControl();
		return true;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public void requestFocus() {
		statusControl.requestFocus();
	}

}
