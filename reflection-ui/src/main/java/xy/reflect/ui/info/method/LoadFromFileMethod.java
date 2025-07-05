
package xy.reflect.ui.info.method;

import java.io.File;
import java.io.InputStream;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Virtual method allowing to execute the
 * {@link ITypeInfo#load(Object, InputStream)} method.
 * 
 * @author olitank
 *
 */
public class LoadFromFileMethod extends AbstractPersistenceMethod {

	public LoadFromFileMethod(ReflectionUI reflectionUI, ITypeInfo objectType) {
		super(reflectionUI, objectType);
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
		objectType.load(object, file);
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String toString() {
		return "LoadFromFileMethod [objectType=" + objectType + "]";
	}

}
