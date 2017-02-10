package xy.reflect.ui;

import java.awt.Rectangle;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestUtils {

	@Test
	public void testMethodSignature() throws Exception {
		String signature = "void java.awt.Rectangle.add(ava.awt.Point pt)";
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo rectangleTypeInfo = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(new Rectangle()));
		IMethodInfo addPointMethodInfo = ReflectionUIUtils.findMethodBySignature(rectangleTypeInfo.getMethods(),
				signature);
		Assert.assertNotNull(addPointMethodInfo);
	}

}
