package xy.reflect.ui;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class TestWithAutomation extends AbstractTest {

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
