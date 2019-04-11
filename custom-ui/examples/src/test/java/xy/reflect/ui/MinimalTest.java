package xy.reflect.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.example.AudioPlayer;
import xy.reflect.ui.example.LoginScreen;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MinimalTest {

	private List<String> errors = new ArrayList<String>();
	private CustomizedUI customizedUI = new CustomizedUI() {
		@Override
		public void logError(String msg) {
			errors.add(msg);
		}
	};
	private CustomizedSwingRenderer renderer = new CustomizedSwingRenderer(customizedUI);

	@Before
	public void resetLoggedErrors() {
		errors.clear();
	}

	@After
	public void checkLoggedErrors() {
		if (errors.size() > 0) {
			throw new AssertionError(errors.toString());
		}
	}

	@Test
	public void testFileExplorer() throws Exception {
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
	
	
	@Test
	public void testLoginScreen() throws Exception {
		customizedUI.getInfoCustomizations().loadFromFile(new File("loginScreen.icu"),
				ReflectionUIUtils.getDebugLogListener(customizedUI));
		Object object = new LoginScreen();
		StandardEditorBuilder dialogBuilder = renderer.openObjectDialog(null, object, renderer.getObjectTitle(object),
				renderer.getObjectIconImage(object), true, false);
		Assert.assertTrue(dialogBuilder.getCreatedDialog().isVisible());
		dialogBuilder.getCreatedDialog().dispose();
		Assert.assertTrue(!dialogBuilder.getCreatedDialog().isVisible());
	}
	
	
	@Test
	public void testAudioPlayer() throws Exception {
		customizedUI.getInfoCustomizations().loadFromFile(new File("audioPlayer.icu"),
				ReflectionUIUtils.getDebugLogListener(customizedUI));
		Object object = new AudioPlayer();
		StandardEditorBuilder dialogBuilder = renderer.openObjectDialog(null, object, renderer.getObjectTitle(object),
				renderer.getObjectIconImage(object), true, false);
		Assert.assertTrue(dialogBuilder.getCreatedDialog().isVisible());
		dialogBuilder.getCreatedDialog().dispose();
		Assert.assertTrue(!dialogBuilder.getCreatedDialog().isVisible());
	}

}
