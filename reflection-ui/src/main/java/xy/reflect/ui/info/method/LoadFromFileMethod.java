package xy.reflect.ui.info.method;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class LoadFromFileMethod extends AbstractPersistenceMethod implements IMethodInfo {

	public LoadFromFileMethod(ReflectionUI reflectionUI, ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.containingType = containingType;
	}

	@Override
	public String getName() {
		return "load";
	}

	@Override
	public String getCaption() {
		return "Load";
	}

	@Override
	public String getOnlineHelp() {
		return "Loads a file";
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		File file = (File) invocationData.getParameterValue(0);
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			containingType.load(object, in);
		} catch (FileNotFoundException e) {
			throw new ReflectionUIError(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignore) {
				}
			}
		}
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	
}
