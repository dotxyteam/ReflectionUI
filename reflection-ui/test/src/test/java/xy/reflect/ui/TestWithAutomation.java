package xy.reflect.ui;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import xy.reflect.ui.util.SystemProperties;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class TestWithAutomation {

	public static void setupConceptionEnvironment() {
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true");
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_EDITABLE, "true");
		System.setProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH,
				"D:/prog/git/ReflectionUI/reflection-ui/src/main/resources/xy/reflect/ui/resource/customizations-tools.icu");
	}
	
	public static void setupTestEnvironment() {
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true");
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_EDITABLE, "false");
	}

	@BeforeClass
	public static void beforeAllTests() {
		setupTestEnvironment();
		TestingUtils.purgeSavedImagesDirectory();
	}

	@Before
	public void beforeEachTest() {
		TestingUtils.closeAllTestableWindows();
	}

	@Test
	public void testJavaAwtPointUI() throws IOException {
		Tester.assertSuccessfulReplay(TestWithAutomation.class.getResourceAsStream("testJavaAwtPointUI.stt"));
	}

	@Test
	public void testJavaLangThreadUI() throws IOException {
		Tester.assertSuccessfulReplay(TestWithAutomation.class.getResourceAsStream("testJavaLangThreadUI.stt"));
	}

	@Test
	public void testTableTreeModelExample() throws IOException {
		Tester.assertSuccessfulReplay(TestWithAutomation.class.getResourceAsStream("testTableTreeModelExample.stt"));
	}

	

}
