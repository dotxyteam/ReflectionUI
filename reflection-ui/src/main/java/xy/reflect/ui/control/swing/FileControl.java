package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Map;

import javax.swing.JFileChooser;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class FileControl extends DialogAccessControl implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected boolean textChangedByUser = true;

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	public FileControl(SwingRenderer swingRenderer, Object object, IFieldInfo field) {
		super(swingRenderer, object, field);
	}

	@Override
	protected TextControl createStatusControl() {
		return new TextControl(swingRenderer, object, new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

			@Override
			public void setValue(Object object, Object value) {
				field.setValue(object, new File((String) value));
			}

			@Override
			public boolean isGetOnly() {
				return field.isGetOnly();
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public Object getValue(Object object) {
				File currentFile = (File) field.getValue(object);
				return currentFile.getPath();
			}

			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return Collections.emptyMap();
			}
		});
	}

	@Override
	protected Component createButton() {
		Component result = super.createButton();
		if (field.isGetOnly()) {
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
		File currentFile = (File) field.getValue(object);
		fileChooser.setCurrentDirectory(lastDirectory);
		configureFileChooser(fileChooser, currentFile);
		int returnVal = fileChooser.showDialog(this,
				swingRenderer.prepareStringToDisplay(getDialogTitle()));
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		lastDirectory = fileChooser.getCurrentDirectory();
		field.setValue(object, fileChooser.getSelectedFile());
		updateControls();
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	@Override
	public boolean showCaption() {
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
