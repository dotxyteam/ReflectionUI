
import java.util.HashMap;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

public class SimpleReflectionUITest {

	public static void main(String[] args) {
		SwingCustomizer.getDefault().openObjectFrame(new HashMap<Integer, String>(), "test", null);
	}
}
