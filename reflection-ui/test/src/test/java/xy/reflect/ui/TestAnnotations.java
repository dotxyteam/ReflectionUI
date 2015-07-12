package xy.reflect.ui;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.annotation.Category;
import xy.reflect.ui.info.annotation.Hidden;
import xy.reflect.ui.info.annotation.OnlineHelp;
import xy.reflect.ui.info.annotation.Validating;
import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestAnnotations {

	@Hidden
	public int hiddenField;

	@Hidden
	public void hiddenMethod() {

	}

	@Hidden
	public int hiddenGetterMethod() {
		return 0;
	}

	@Category("category1")
	public int category1Field;

	@OnlineHelp("documentedField documentation")
	public int documentedField;

	@OnlineHelp("documentedField documentation")
	public void documentedMethod() {

	}

	@Validating
	public void validationMethod() throws Exception {
		throw new Exception("Validation KO");
	}

	public int chosenOption;

	@ValueOptionsForField("chosenOption")
	public int[] possibleOptions = new int[] { 3, 4, 5 };

	public int chosenOption2;

	@ValueOptionsForField("chosenOption2")
	public List<Integer> getPossibleOptions2() {
		return Arrays.asList(6, 7, 8);
	}

	@Test
	public void test() {
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo typeInfo = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(this));

		IFieldInfo category1FieldInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "category1Field");
		Assert.assertTrue(category1FieldInfo.getCategory().getCaption()
				.equals("category1"));

		IFieldInfo chosenOptionFieldInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "chosenOption");
		Assert.assertEquals(Arrays.toString((Object[]) chosenOptionFieldInfo
				.getValueOptions(this)), Arrays.toString(possibleOptions));

		IFieldInfo chosenOption2FieldInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "chosenOption2");
		Assert.assertEquals(Arrays.toString((Object[]) chosenOption2FieldInfo
				.getValueOptions(this)), getPossibleOptions2().toString());
		
		IFieldInfo documentedFieldInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "documentedField");
		Assert.assertTrue(documentedFieldInfo.getOnlineHelp().length() > 0);

		IFieldInfo hiddenFieldInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getFields(), "hiddenField");
		Assert.assertTrue(hiddenFieldInfo == null);

		IMethodInfo hiddenMethodInfo = ReflectionUIUtils.findInfoByName(
				typeInfo.getMethods(), "hiddenMethod");
		Assert.assertTrue(hiddenMethodInfo == null);

	}

}
