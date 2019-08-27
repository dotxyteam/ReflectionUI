package xy.reflect.ui;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestUtils {

	@Test
	public void testMethodSignature() throws Exception {
		String signature = "void add(java.awt.Point)";
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo rectangleTypeInfo = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(new Rectangle()));
		IMethodInfo addPointMethodInfo = ReflectionUIUtils.findMethodBySignature(rectangleTypeInfo.getMethods(),
				signature);
		Assert.assertNotNull(addPointMethodInfo);
	}

	@Test
	public void testDefaultPersistence() throws Exception {
		Date objectToSave = new Date();
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(objectToSave));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		type.save(objectToSave, out);
		Date objectToLoad = new Date(0);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		type.load(objectToLoad, in);

		Assert.assertEquals(objectToSave, objectToLoad);
	}

}
