package xy.reflect.ui;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class ReflectionUIMinimalTest {

	private List<String> errors = new ArrayList<String>();
	private ReflectionUI customizedUI = new ReflectionUI() {
		@Override
		public void logError(String msg) {
			errors.add(msg);
		}
	};
	private SwingRenderer renderer = new SwingRenderer(customizedUI);

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
	public void test() {
		Object object = new Thread();
		StandardEditorBuilder dialogBuilder = renderer.openObjectDialog(null, object, renderer.getObjectTitle(object),
				renderer.getObjectIconImage(object), true, false);
		Assert.assertTrue(dialogBuilder.getCreatedDialog().isVisible());
		dialogBuilder.getCreatedDialog().dispose();
		Assert.assertTrue(!dialogBuilder.getCreatedDialog().isVisible());
	}

}
