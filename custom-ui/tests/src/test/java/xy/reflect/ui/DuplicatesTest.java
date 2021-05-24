package xy.reflect.ui;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

public class DuplicatesTest {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SwingCustomizer(new CustomizedUI(),
						System.getProperty("custom-ui-tests.project.directory", "./") + "duplicates.icu")
								.openObjectFrame("123", "test", null);
			}
		});
	}
}
