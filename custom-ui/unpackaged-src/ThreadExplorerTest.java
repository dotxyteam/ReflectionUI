

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

public class ThreadExplorerTest {

	public static void main(String[] args) {
		System.setProperty(MoreSystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH,
				System.getProperty("custom-ui.project.directory", "./") + "unpackaged-src/thread.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingCustomizer.getDefault().openObjectFrame(new Thread(new Runnable() {

					@Override
					public void run() {
						System.out.println("The thread is starting...");
						System.out.println("The thread is ending...");
					}

				}), "test", null);
			}
		});
	}
}
