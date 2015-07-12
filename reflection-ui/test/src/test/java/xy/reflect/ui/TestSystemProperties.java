package xy.reflect.ui;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

public class TestSystemProperties {

	public String nullableField;
	
	public Comparable<?> necessarilyNullableField;

	public int hiddenField;

	public void hiddenMethod() {
	}

	public void hiddenParametersMethod(String parameter1, String parameter2) {
	}

	@Test
	public void testHiddenNullableFacets() {
		ReflectionUI reflectionUI;
		ITypeInfo typeInfo;

		reflectionUI = new ReflectionUI();
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "nullableField").isNullable());
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "necessarilyNullableField").isNullable());

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_NULLABLE_FACETS, "true");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(!ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "nullableField").isNullable());
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "necessarilyNullableField").isNullable());
	}

	@Test
	public void testHiddenMethods() {
		ReflectionUI reflectionUI;
		ITypeInfo typeInfo;

		reflectionUI = new ReflectionUI();
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getMethods(), "hiddenMethod") != null);

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_METHODS,
				TestSystemProperties.class.getName() + "*#hiddenMethod()");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getMethods(), "hiddenMethod") == null);

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_METHODS, "*#hiddenMethod*");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getMethods(), "hiddenMethod") == null);

	}

	@Test
	public void testHiddenFields() {
		ReflectionUI reflectionUI;
		ITypeInfo typeInfo;

		reflectionUI = new ReflectionUI();
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "hiddenField") != null);

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_FIELDS,
				TestSystemProperties.class.getName() + "*#hiddenField");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "hiddenField") == null);

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_FIELDS, "*#hiddenField*");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "hiddenField") == null);

	}

	@Test
	public void testHiddenConstructors() {
		ReflectionUI reflectionUI;
		ITypeInfo typeInfo;

		reflectionUI = new ReflectionUI();
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getConstructors(), "TestSystemProperties") != null);

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_FIELDS,
				TestSystemProperties.class.getName()
						+ "*#TestSystemProperties()");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "TestSystemProperties") == null);

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_FIELDS,
				"*#TestSystemProperties*");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "TestSystemProperties") == null);

	}

	@Test
	public void testHiddenParameters() {
		ReflectionUI reflectionUI;
		ITypeInfo typeInfo;
		IMethodInfo hiddenMethodInfo;

		reflectionUI = new ReflectionUI();
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		hiddenMethodInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getMethods(), "hiddenParametersMethod");
		hiddenMethodInfo.invoke(this, Collections.<Integer, Object> emptyMap());
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				hiddenMethodInfo.getParameters(), "parameter1") != null);

		reflectionUI = new ReflectionUI();
		System.setProperty(
				SystemProperties.HIDE_PARAMETERS,
				TestSystemProperties.class.getName()
						+ "#hiddenParametersMethod(java.lang.String,java.lang.String):0");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		hiddenMethodInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getMethods(), "hiddenParametersMethod");
		hiddenMethodInfo.invoke(this, Collections.<Integer, Object> emptyMap());
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				hiddenMethodInfo.getParameters(), "parameter1") == null);
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				hiddenMethodInfo.getParameters(), "parameter2") != null);

		reflectionUI = new ReflectionUI();
		System.setProperty(SystemProperties.HIDE_PARAMETERS,
				"*#hiddenParametersMethod*");
		typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));
		hiddenMethodInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getMethods(), "hiddenParametersMethod");
		hiddenMethodInfo.invoke(this, Collections.<Integer, Object> emptyMap());
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				hiddenMethodInfo.getParameters(), "parameter1") == null);
		Assert.assertTrue(ReflectionUIUtils.findInfoByName(
				hiddenMethodInfo.getParameters(), "parameter2") == null);

	}

}
