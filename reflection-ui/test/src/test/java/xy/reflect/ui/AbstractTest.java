package xy.reflect.ui;

import org.junit.BeforeClass;

import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.ui.testing.util.TestingUtils;

public class AbstractTest {

	public static void setupTestEnvironment() {
		checkSystemProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true");
		checkSystemProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_EDITABLE, "false");
	}

	protected static void checkSystemProperty(String key, String expectedValue) {
		String value = System.getProperty(key);
		if (!ReflectionUIUtils.equalsOrBothNull(expectedValue, value)) {
			throw new AssertionError("System property invalid value:\n" + "-D" + key + "=" + value + "\nExpected:\n"
					+ "-D" + key + "=" + expectedValue);

		}
	}

	@BeforeClass
	public static void beforeAllTests() {
		setupTestEnvironment();
		TestingUtils.purgeSavedImagesDirectory();
	}

}