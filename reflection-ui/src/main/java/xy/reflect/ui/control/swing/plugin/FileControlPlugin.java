package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.ICustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class FileControlPlugin implements ICustomizableFieldControlPlugin {

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
	public JMenuItem makeCustomizerMenuItem(final Component activatorComponent,
			final FieldCustomization fieldCustomization, final SwingRenderer customizationToolsRenderer) {
		return new JMenuItem(new AbstractAction(customizationToolsRenderer.prepareStringToDisplay("File...")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				FileControlCustomization controlCustomization = (FileControlCustomization) loadCustomization(
						FileControlCustomization.class, fieldCustomization);
				if (controlCustomization == null) {
					controlCustomization = new FileControlCustomization();
				}
				StandardEditorBuilder status = customizationToolsRenderer.openObjectDialog(activatorComponent,
						controlCustomization, null, null, true, true);
				if (status.isCancelled()) {
					return;
				}
				storeCustomization(controlCustomization, fieldCustomization);
			}
		});
	}

	protected void storeCustomization(Object controlCustomization, FieldCustomization fieldCustomization) {
		Map<String, Object> specificProperties = fieldCustomization.getSpecificProperties();
		specificProperties = new HashMap<String, Object>(specificProperties);
		specificProperties.put(controlCustomization.getClass().getName(),
				ReflectionUIUtils.serializeToHexaText(controlCustomization));
		fieldCustomization.setSpecificProperties(specificProperties);
	}

	@SuppressWarnings("unchecked")
	protected <T> T loadCustomization(Class<T> customizationClass, Map<String, Object> specificProperties) {
		String text = (String) specificProperties.get(customizationClass.getName());
		if (text == null) {
			return null;
		}
		return (T) ReflectionUIUtils.deserializeFromHexaText(text);
	}

	protected <T> T loadCustomization(Class<T> customizationClass, FieldCustomization fieldCustomization) {
		return loadCustomization(customizationClass, fieldCustomization.getSpecificProperties());
	}

	protected <T> T loadCustomization(Class<T> customizationClass, IFieldControlInput input) {
		return (T) loadCustomization(customizationClass, input.getControlData().getSpecificProperties());
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

	protected static class FileControlCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		public List<FileNameFilter> fileNameFilters = new ArrayList<FileNameFilter>();

	}

	public static class FileNameFilter extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		public String description = "";
		public List<String> extensions = new ArrayList<String>();
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
			FileControlCustomization controlCustomization = loadCustomization(FileControlCustomization.class, input);
			for (FileNameFilter filter : controlCustomization.fileNameFilters) {
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filter.description,
						filter.extensions.toArray(new String[filter.extensions.size()])));
			}
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
