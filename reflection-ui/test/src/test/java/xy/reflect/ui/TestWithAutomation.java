package xy.reflect.ui;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class TestWithAutomation extends AbstractUIAutomationTest {

	Tester tester = new Tester();

	@BeforeClass
	public static void beforeAllTests() {
		TestingUtils.purgeSavedImagesDirectory(new Tester());
	}

	@Test
	public void testJavaAwtPointUI() throws IOException {
		TestingUtils.assertSuccessfulReplay(tester,
				TestWithAutomation.class.getResourceAsStream("testJavaAwtPointUI.stt"));
	}

	@Test
	public void testJavaLangThreadUI() throws IOException {
		TestingUtils.assertSuccessfulReplay(tester,
				TestWithAutomation.class.getResourceAsStream("testJavaLangThreadUI.stt"));
	}

	@Test
	public void testTableTreeModelExample() throws IOException {
		TestingUtils.assertSuccessfulReplay(tester,
				TestWithAutomation.class.getResourceAsStream("testTableTreeModelExample.stt"));
	}
	
	@Test
	public void testCustomizations() throws IOException {
		TestingUtils.assertSuccessfulReplay(tester,
				TestWithAutomation.class.getResourceAsStream("testCustomizations.stt"));
	}

}
