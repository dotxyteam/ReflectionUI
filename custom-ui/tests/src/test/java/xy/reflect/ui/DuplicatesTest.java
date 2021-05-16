package xy.reflect.ui;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

public class DuplicatesTest {

	public static void main(String[] args) {
		System.setProperty(MoreSystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH,
				System.getProperty("custom-ui-tests.project.directory", "./") + "duplicates.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingCustomizer.getDefault().openObjectFrame("123", "test", null);
			}
		});
	}
}
