package xy.reflect.ui;

import java.io.IOException;

import org.junit.Test;

import xy.ui.testing.Tester;

public class TestWithAutomation extends AbstractUIAutomationTest {

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
