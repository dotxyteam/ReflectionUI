


package xy.reflect.ui.info.method;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Virtual method allowing to execute the
 * {@link ITypeInfo#save(Object, OutputStream)} method.
 * 
 * @author olitank
 *
 */
public class SaveToFileMethod extends AbstractPersistenceMethod {

	public SaveToFileMethod(ReflectionUI reflectionUI, ITypeInfo containingType) {
		super(reflectionUI, containingType);
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
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			containingType.save(object, out);
		} catch (FileNotFoundException e) {
			throw new ReflectionUIError(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ignore) {
				}
			}
		}
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String toString() {
		return "SaveToFileMethod [containingType=" + containingType + "]";
	}

}
