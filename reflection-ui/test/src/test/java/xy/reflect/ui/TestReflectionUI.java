package xy.reflect.ui;

import java.io.IOException;

import org.junit.Test;

import xy.ui.testing.Tester;

public class TestReflectionUI {

	@Test
	public void testJavaAwtPointUI() throws IOException {
		Tester.assertSuccessfulReplay(TestReflectionUI.class
				.getResourceAsStream("testJavaAwtPointUI.stt"));
	}

}
