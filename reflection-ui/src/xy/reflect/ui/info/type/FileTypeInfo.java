package xy.reflect.ui.info.type;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FileControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;

public class FileTypeInfo extends DefaultTypeInfo {

	public FileTypeInfo(ReflectionUI reflectionUI) {
		super(reflectionUI, File.class);
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		IMethodInfo defaultCtor = new AbstractConstructorMethodInfo(
				FileTypeInfo.this) {

			@Override
			public Object invoke(Object object,
					Map<String, Object> valueByParameterName) {
				return getDefaultFile();
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		};
		List<IMethodInfo> result = new ArrayList<IMethodInfo>(
				super.getConstructors());
		result.add(defaultCtor);
		return result;
	}

	public File getDefaultFile() {
		return new File("");
	}

	public void configureFileChooser(JFileChooser fileChooser, File currentFile) {
		fileChooser.setSelectedFile(currentFile.getAbsoluteFile());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}

	public String getDialogTitle() {
		return "Select";
	}

	@Override
	public Component createNonNullFieldValueControl(Object object,
			IFieldInfo field) {
		return new FileControl(reflectionUI, object, field);
	}
	

	@Override
	public boolean isImmutable() {
		return true;
	}

	@Override
	public boolean hasCustomFieldControl() {
		return true;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		return File.class.isAssignableFrom(javaType);
	}


}
