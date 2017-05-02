import java.io.File;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.SystemProperties;

public class FileExplorer {

	public static void main(String[] args) {
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true");
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH, "fileExplorer.icu");
		SwingRenderer.getDefault().openObjectFrame(File.listRoots());
	}

}
