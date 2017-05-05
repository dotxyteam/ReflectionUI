import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.SystemProperties;

public class Calculator {

	public static void main(String[] args) {
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH, "calculator.icu");
		
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		SwingRenderer.getDefault().openObjectFrame(engine);
	}
	

}
