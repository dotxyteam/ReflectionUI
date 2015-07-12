package xy.reflect.ui;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class TestOpenUI {

	@Before
	public void before() {
		TestingUtils.closeAllTestableWindows();
	}

	@Test
	public void testJavaAwtPointUI() throws IOException {
		Tester.assertSuccessfulReplay(TestOpenUI.class
				.getResourceAsStream("testJavaAwtPointUI.stt"));
	}

	@Test
	public void testJavaLangThreadUI() throws IOException {
		Tester.assertSuccessfulReplay(TestOpenUI.class
				.getResourceAsStream("testJavaLangThreadUI.stt"));
	}

	@Test
	public void testTableTreeModelExample() throws IOException {
		Tester.assertSuccessfulReplay(TestOpenUI.class
				.getResourceAsStream("testTableTreeModelExample.stt"));
	}

}
