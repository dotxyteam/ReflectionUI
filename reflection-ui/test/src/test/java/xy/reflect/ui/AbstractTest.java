package xy.reflect.ui;

import org.junit.BeforeClass;

import xy.reflect.ui.util.SystemProperties;
import xy.ui.testing.util.TestingUtils;

public class AbstractTest {

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

	
}