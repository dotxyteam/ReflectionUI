import java.io.File;
import java.io.IOException;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * File explorer GUI generated using only the {@link java.io.File} class
 * features and the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class FileExplorer {

	public static void main(String[] args) throws IOException {
		CustomizedUI reflectionUI = CustomizedSwingRenderer.getDefault().getCustomizedUI();
		reflectionUI.getInfoCustomizations().loadFromStream(FileExplorer.class.getResourceAsStream("fileExplorer.icu"),
				ReflectionUIUtils.getDebugLogListener(reflectionUI));

		CustomizedSwingRenderer.getDefault()
				.openObjectFrame(new File[] { new File(System.getProperty("java.io.tmpdir")) });
	}

}
