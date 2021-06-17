package xy.reflect.ui;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.SystemProperties;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class TestWithAutomation {

	Tester tester = new Tester();

	protected static void checkSystemProperty(String key, String expectedValue) {
		String value = System.getProperty(key);
		if (!MiscUtils.equalsOrBothNull(expectedValue, value)) {
			String errorMsg = "System property invalid value:\n" + "-D" + key + "=" + value + "\nExpected:\n" + "-D"
					+ key + "=" + expectedValue;
			System.err.println(errorMsg);
			throw new AssertionError(errorMsg);

		}
	}

	public static void setupTestEnvironment() {
		checkSystemProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true");
		checkSystemProperty(MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS, "true");
		checkSystemProperty(MoreSystemProperties.DEBUG, "true");
		Locale.setDefault(Locale.US);
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

	@Test
	public void testDuplicates() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testDuplicates.stt"));
	}

	@Test
	public void testMemberNamesCollision() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testMemberNamesCollision.stt"));
	}

	@Test
	public void testPlugins() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester, new File(
				System.getProperty("custom-ui-tests.project.directory", "./") + "test-specifications/testPlugins.stt"));
	}

	@Test
	public void testEcho() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester, new File(
				System.getProperty("custom-ui-tests.project.directory", "./") + "test-specifications/testEcho.stt"));
	}

	@Test
	public void testEmptyObject() throws Exception {
		File virtualImageFile = new File("virtualImage.jpg");
		if (virtualImageFile.exists()) {
			Files.delete(virtualImageFile.toPath());
		}
		Files.copy(getClass().getResourceAsStream("virtualImage.jpg"), virtualImageFile.toPath());
		try {
			TestingUtils.assertSuccessfulReplay(tester,
					new File(System.getProperty("custom-ui-tests.project.directory", "./")
							+ "test-specifications/testEmptyObject.stt"));
		} finally {
			Files.delete(virtualImageFile.toPath());
		}
	}

	@Test
	public void testClassExplorer() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testClassExplorer.stt"));
	}

	@Test
	public void testMenuCreation() throws Exception {
		File testFile = new File("test.tst");
		if (testFile.exists()) {
			Files.delete(testFile.toPath());
		}
		try {
			TestingUtils.assertSuccessfulReplay(tester,
					new File(System.getProperty("custom-ui-tests.project.directory", "./")
							+ "test-specifications/testMenuCreation.stt"));
		} finally {
			Files.delete(testFile.toPath());
		}
	}

	@Test
	public void testPolymorphism() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testPolymorphism.stt"));
	}

	@Test
	public void testListModification() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testListModification.stt"));
	}

	@Test
	public void testTransientFields() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testTransientFields.stt"));
	}

	@Test
	public void testTransactions() throws Exception {
		TestingUtils.assertSuccessfulReplay(tester,
				new File(System.getProperty("custom-ui-tests.project.directory", "./")
						+ "test-specifications/testTransactions.stt"));
	}

}
