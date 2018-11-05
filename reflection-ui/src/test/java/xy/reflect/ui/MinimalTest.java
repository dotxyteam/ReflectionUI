package xy.reflect.ui;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class MinimalTest {

	@Test
	public void test() {
		SwingRenderer renderer = SwingRenderer.getDefault();
		Object object = new Thread();
		StandardEditorBuilder dialogBuilder = renderer.openObjectDialog(null, object, renderer.getObjectTitle(object),
				renderer.getObjectIconImage(object), true, false);
		Assert.assertTrue(dialogBuilder.getCreatedDialog().isVisible());
		dialogBuilder.getCreatedDialog().dispose();
		Assert.assertTrue(!dialogBuilder.getCreatedDialog().isVisible());
	}

}
