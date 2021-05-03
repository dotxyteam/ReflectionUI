package xy.reflect.ui.example;

import java.io.IOException;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Control screen GUI generated with the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class ControlScreen {

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "controlScreen.icu");
		renderer.openObjectFrame(new ControlScreen());
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
