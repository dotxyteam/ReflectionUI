import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.util.ReflectionUIUtils;

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

		CustomizedUI reflectionUI = CustomizedSwingRenderer.getDefault().getCustomizedUI();
		reflectionUI.getInfoCustomizations().loadFromStream(Calculator.class.getResourceAsStream("calculator.icu"),
				ReflectionUIUtils.getDebugLogListener(reflectionUI));

		CustomizedSwingRenderer.getDefault().openObjectFrame(engine);
	}

}
