package xy.reflect.ui.example;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * File explorer GUI generated using only the {@link java.io.File} class
 * features and the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class FileExplorer {

	public static void main(String[] args) throws IOException {
		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "fileExplorer.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new File[] { new File(System.getProperty("java.io.tmpdir")) });
			}
		});
	}

}
