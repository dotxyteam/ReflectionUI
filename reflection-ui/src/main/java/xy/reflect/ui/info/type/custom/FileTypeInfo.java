package xy.reflect.ui.info.type.custom;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.FileControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.method.InvocationData;

public class FileTypeInfo extends DefaultTypeInfo {

	public FileTypeInfo(ReflectionUI reflectionUI) {
		super(reflectionUI, File.class);
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		IMethodInfo defaultCtor = new AbstractConstructorMethodInfo(FileTypeInfo.this) {

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				return getDefaultFile();
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}

		};
		List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors());
		result.add(defaultCtor);
		return result;
	}

	public static File getDefaultFile() {
		return new File("");
	}

	@Override
	public Component createCustomFieldControl(Object object, IFieldInfo field) {
		return new FileControl(reflectionUI, object, field);
	}
	
	@Override
	public boolean hasCustomFieldControl() {
		return true;
	}
	

	public static boolean isCompatibleWith(Class<?> javaType) {
		return File.class.isAssignableFrom(javaType);
	}

}
