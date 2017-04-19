package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class FileControlPlugin implements IFieldControlPlugin {

	protected static final File DEFAULT_FILE = new File("");

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	public boolean handles(IFieldControlInput input) {
		final Class<?> javaType;
		try {
			javaType = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
		} catch (ClassNotFoundException e) {
			return false;
		}
		if (!File.class.isAssignableFrom(javaType)) {
			return false;
		}
		return true;
	}

	@Override
	public Component createControl(Object renderer, IFieldControlInput input) {
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
		return new FileControl((SwingRenderer) renderer, input);
	}

	protected class FileControl extends DialogAccessControl implements IAdvancedFieldControl {

		protected static final long serialVersionUID = 1L;

		protected boolean textChangedByUser = true;

		public FileControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
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

}
