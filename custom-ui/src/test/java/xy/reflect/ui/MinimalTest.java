package xy.reflect.ui;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.SystemProperties;

public class MinimalTest {

	@Test
	public void test() throws Exception {
		SwingRenderer renderer = SwingCustomizer.getDefault();
		Object object = new Thread();
		StandardEditorBuilder dialogBuilder = renderer.openObjectDialog(null, object, renderer.getObjectTitle(object),
				renderer.getObjectIconImage(object), true, false);
		Assert.assertTrue(dialogBuilder.getCreatedDialog().isVisible());
		dialogBuilder.getCreatedDialog().dispose();
		Assert.assertTrue(!dialogBuilder.getCreatedDialog().isVisible());
		FileUtils.delete(new File(SystemProperties.getDefaultInfoCustomizationsFilePath()));
	}

}
