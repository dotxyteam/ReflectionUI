package xy.reflect.ui;

import java.io.File;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MinimalTest {

	@Test
	public void testFileExplorer() throws Exception {
		CustomizedUI customizedUI = CustomizedSwingRenderer.getDefault().getCustomizedUI();
		CustomizedSwingRenderer renderer = CustomizedSwingRenderer.getDefault();

		customizedUI.getInfoCustomizations().loadFromFile(new File("fileExplorer.icu"),
				ReflectionUIUtils.getDebugLogListener(customizedUI));
		Object object = new File[] { new File(System.getProperty("java.io.tmpdir")) };
		StandardEditorBuilder dialogBuilder = renderer.openObjectDialog(null, object, renderer.getObjectTitle(object),
				renderer.getObjectIconImage(object), true, false);
		Assert.assertTrue(dialogBuilder.getCreatedDialog().isVisible());
		dialogBuilder.getCreatedDialog().dispose();
		Assert.assertTrue(!dialogBuilder.getCreatedDialog().isVisible());
	}

	@Test
	public void testCalculator() throws Exception {
		CustomizedUI customizedUI = CustomizedSwingRenderer.getDefault().getCustomizedUI();
		CustomizedSwingRenderer renderer = CustomizedSwingRenderer.getDefault();

		customizedUI.getInfoCustomizations().loadFromFile(new File("calculator.icu"),
				ReflectionUIUtils.getDebugLogListener(customizedUI));
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		Object object = engine;
		StandardEditorBuilder dialogBuilder = renderer.openObjectDialog(null, object, renderer.getObjectTitle(object),
				renderer.getObjectIconImage(object), true, false);
		Assert.assertTrue(dialogBuilder.getCreatedDialog().isVisible());
		dialogBuilder.getCreatedDialog().dispose();
		Assert.assertTrue(!dialogBuilder.getCreatedDialog().isVisible());
	}

}
