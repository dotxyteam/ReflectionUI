


package xy.reflect.ui.info.method;

import java.io.File;
import java.io.OutputStream;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Virtual method allowing to execute the
 * {@link ITypeInfo#save(Object, OutputStream)} method.
 * 
 * @author olitank
 *
 */
public class SaveToFileMethod extends AbstractPersistenceMethod {

	public SaveToFileMethod(ReflectionUI reflectionUI, ITypeInfo objectType) {
		super(reflectionUI, objectType);
	}

	@Override
	public String getName() {
		return "save";
	}

	@Override
	public String getCaption() {
		return "Save";
	}

	@Override
	public String getOnlineHelp() {
		return "Saves to a file";
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		File file = (File) invocationData.getParameterValue(0);
		objectType.save(object, file);
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String toString() {
		return "SaveToFileMethod [objectType=" + objectType + "]";
	}

}
