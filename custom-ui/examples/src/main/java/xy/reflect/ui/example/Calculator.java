package xy.reflect.ui.example;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * Calculator GUI generated using only the JavaScript engine and the XML
 * declarative customizations.
 * 
 * @author olitank
 *
 */
public class Calculator {

	public static void main(String[] args) throws IOException {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");

		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "calculator.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(engine);
			}
		});
	}

}
