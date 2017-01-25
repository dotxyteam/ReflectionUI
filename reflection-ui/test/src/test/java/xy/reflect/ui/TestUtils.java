package xy.reflect.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestUtils {

	@Test
	public void testMethodSignature() throws Exception {
		Method addPointMethod = Rectangle.class.getMethod("add", Point.class);
		String signature = ReflectionUIUtils.getJavaMethodInfoSignature(addPointMethod);
		
		IReflectionUI reflectionUI = new StandardReflectionUI();
		ITypeInfo rectangleTypeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(new Rectangle()));
		IMethodInfo addPointMethodInfo = ReflectionUIUtils.findMethodBySignature(rectangleTypeInfo.getMethods(), signature);
		Assert.assertNotNull(addPointMethodInfo);
	}

}
