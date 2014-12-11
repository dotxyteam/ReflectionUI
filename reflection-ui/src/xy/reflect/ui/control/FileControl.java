package xy.reflect.ui.control;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.FileTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class FileControl extends DialogAcessControl {

	protected static final long serialVersionUID = 1L;

	protected FileTypeInfo fileType;
	protected boolean textChangedByUser = true;

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
				return false;
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
			public String getCategoryCaption() {
				return null;
			}
		});
	}

	@Override
	protected JButton createButton() {
		JButton result = super.createButton();
		if (field.isReadOnly()) {
			result.setEnabled(false);
		}
		return result;
	}

	@Override
	protected void openDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		File currentFile = (File) field.getValue(object);
		fileType.configureFileChooser(fileChooser, currentFile);
		int returnVal = fileChooser.showDialog(this,
				reflectionUI.translateUIString(fileType.getDialogTitle()));
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		field.setValue(object, fileChooser.getSelectedFile());
		updateControls();
	}
}
