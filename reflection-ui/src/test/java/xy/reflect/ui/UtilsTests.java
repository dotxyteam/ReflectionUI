/*
 * 
 */
package xy.reflect.ui;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory.TypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class UtilsTests {

	@Test
	public void testMethodSignatureUtils() {
		TypeInfo complexType = new EncapsulatedObjectFactory(ReflectionUI.getDefault(),
				ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(int[].class, null)), "testContext",
				"Complex Test Type", "Test Field").new TypeInfo();
		String methodName = "m";
		List<String> methodParameters = Arrays.asList(complexType.getName(), complexType.getName());
		String methodSignature = ReflectionUIUtils.buildMethodSignature(complexType.getName(), methodName,
				methodParameters);

		Assert.assertEquals(complexType.getName(),
				ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(methodSignature));
		Assert.assertEquals(methodName, ReflectionUIUtils.extractMethodNameFromSignature(methodSignature));
		Assert.assertEquals(methodParameters,
				Arrays.asList(ReflectionUIUtils.extractMethodParameterTypeNamesFromSignature(methodSignature)));
	}

}
