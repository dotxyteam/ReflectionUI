/*
 * 
 */
package xy.reflect.ui;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class UtilsTests {

	@Test
	public void testMethodSignatureUtils() {
		ITypeInfo type;
		String methodReturnTypeName;
		String methodName;
		List<String> methodParameters;
		String methodSignature;

		type = ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(int.class, null));
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);

		type = ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(Integer.class, null));
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);

		type = ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(int[][].class, null));
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);

		type = ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(Integer[][].class, null));
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);

		type = new EncapsulatedObjectFactory(ReflectionUI.getDefault(),
				ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(int.class, null)), "testContext",
				"Complex Test Type", "Test Field").new TypeInfo();
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);

		type = new EncapsulatedObjectFactory(ReflectionUI.getDefault(),
				ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(Integer.class, null)), "testContext",
				"Complex Test Type", "Test Field").new TypeInfo();
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);

		type = new EncapsulatedObjectFactory(ReflectionUI.getDefault(),
				ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(int[][].class, null)), "testContext",
				"Complex Test Type", "Test Field").new TypeInfo();
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);

		type = new EncapsulatedObjectFactory(ReflectionUI.getDefault(),
				ReflectionUI.getDefault().getTypeInfo(new JavaTypeInfoSource(Integer[][].class, null)), "testContext",
				"Complex Test Type", "Test Field").new TypeInfo();
		methodReturnTypeName = type.getName();
		methodName = "m";
		methodParameters = Arrays.asList(type.getName(), type.getName());
		methodSignature = ReflectionUIUtils.buildMethodSignature(type.getName(), methodName, methodParameters);
		testMethodSignatureUtils(methodSignature, methodReturnTypeName, methodName, methodParameters);
	}

	private void testMethodSignatureUtils(String methodSignature, String methodReturnTypeName, String methodName,
			List<String> methodParameters) {
		Assert.assertEquals(methodReturnTypeName,
				ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(methodSignature));
		Assert.assertEquals(methodName, ReflectionUIUtils.extractMethodNameFromSignature(methodSignature));
		Assert.assertEquals(methodParameters,
				Arrays.asList(ReflectionUIUtils.extractMethodParameterTypeNamesFromSignature(methodSignature)));
	}

}
