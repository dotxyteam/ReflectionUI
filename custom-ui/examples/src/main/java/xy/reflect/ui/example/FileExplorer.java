package xy.reflect.ui.example;

import java.io.File;
import java.io.IOException;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * File explorer GUI generated using only the {@link java.io.File} class
 * features and the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class FileExplorer {

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "fileExplorer.icu");
		renderer.openObjectFrame(new File[] { new File(System.getProperty("java.io.tmpdir")) });
	}

}
