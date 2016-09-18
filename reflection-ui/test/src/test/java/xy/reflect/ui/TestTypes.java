package xy.reflect.ui;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.UtilitiesTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TestTypes extends AbstractTest {

	@Test
	public void testUtilitiesTypeInfo() throws Exception {
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(new ReflectionUIUtils()));
		Assert.assertTrue(type instanceof UtilitiesTypeInfo);
		Assert.assertTrue(type.getMethods().size() > 0);
		Assert.assertTrue(type.getFields().size() == 0);
		ITypeInfo type2 = reflectionUI.getTypeInfo(new JavaTypeInfoSource(ReflectionUIUtils.class));
		Assert.assertEquals(type, type2);

	}

}
