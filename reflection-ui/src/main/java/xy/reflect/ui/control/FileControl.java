package xy.reflect.ui.control;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Map;

import javax.swing.JFileChooser;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.FileTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class FileControl extends DialogAccessControl {

	protected static final long serialVersionUID = 1L;

	protected FileTypeInfo fileType;
	protected boolean textChangedByUser = true;

	private static File lastDirectory;

	public FileControl(ReflectionUI reflectionUI, Object object,
			IFieldInfo field) {
		super(reflectionUI, object, field);
		this.fileType = (FileTypeInfo) field.getType();
	}

	@Override
	protected TextControl createTextControl() {
		return new TextControl(reflectionUI, object, new IFieldInfo() {

			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getCaption() {
				return null;
			}

			@Override
			public void setValue(Object object, Object value) {
				field.setValue(object, new File((String) value));
			}

			@Override
			public boolean isReadOnly() {
				return field.isReadOnly();
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
				return new DefaultTextualTypeInfo(reflectionUI, String.class);
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}

			@Override
			public String getDocumentation() {
				return null;
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
		if (field.isReadOnly()) {
			result.setEnabled(false);
		}
		return result;
	}

	@Override
	protected void openDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		File currentFile = (File) field.getValue(object);
		if(lastDirectory != null){
			fileChooser.setCurrentDirectory(lastDirectory);
		}
		fileType.configureFileChooser(fileChooser, currentFile);
		int returnVal = fileChooser.showDialog(this,
				reflectionUI.translateUIString(fileType.getDialogTitle()));
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		lastDirectory = fileChooser.getCurrentDirectory();
		field.setValue(object, fileChooser.getSelectedFile());
		updateControls();
	}
}
