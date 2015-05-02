import java.io.File;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;


public class TestPrecomputeTypeInfo {

	private static ReflectionUI reflectionUI;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		reflectionUI = new ReflectionUI();
		reflectionUI.openObjectFrame(getPrecomputedTypeFile(new File(".").getAbsoluteFile()), null, null);
	}

	private static Object getPrecomputedTypeFile(File absoluteFile) {
		return new PrecomputedTypeInfoInstanceWrapper(absoluteFile, new FileTypeInfo(reflectionUI));
	}

}
