package xy.reflect.ui;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class TestWithAutomation {

	Tester tester = new Tester();

	protected static void checkSystemProperty(String key, String expectedValue) {
		String value = System.getProperty(key);
		if (!ReflectionUIUtils.equalsOrBothNull(expectedValue, value)) {
			String errorMsg = "System property invalid value:\n" + "-D" + key + "=" + value + "\nExpected:\n" + "-D"
					+ key + "=" + expectedValue;
			System.err.println(errorMsg);
			throw new AssertionError(errorMsg);

		}
	}

	public static void setupTestEnvironment() {
		checkSystemProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true");
		checkSystemProperty(MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS, "true");
	}

	@BeforeClass
	public static void beforeAllTests() {
		setupTestEnvironment();
		TestingUtils.purgeAllReportsDirectory();
	}

	@Test
	public void testJavaAwtPointUI() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testJavaAwtPointUI.stt"));
	}

	@Test
	public void testJavaLangThreadUI() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testJavaLangThreadUI.stt"));
	}

	@Test
	public void testTableTreeModelExample() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testTableTreeModelExample.stt"));
	}

	@Test
	public void testCustomizations() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testCustomizations.stt"));
	}

}
