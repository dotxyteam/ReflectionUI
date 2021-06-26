package xy.reflect.ui.example;

import java.io.IOException;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * Control screen GUI generated with the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class ControlScreen2 {

	public static void main(String[] args) throws IOException {
		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "controlScreen2.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new ControlScreen2());
			}
		});
	}

	public Configuration getConfiguration() {
		return new Configuration();
	}

	public Execution getExecution() {
		return new Execution();
	}

	public Logs getLogs() {
		return new Logs();
	}

	public Statistics getStatistics() {
		return new Statistics();
	}

	public class Configuration {

	}

	public class Execution {

	}

	public class Logs {

	}

	public class Statistics {

	}
}
