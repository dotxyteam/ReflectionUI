
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

public class SimpleReflectionUITest {

	public static class Test {
		public int i = 0;
		public int i2 = 0;
		public int i3 = 0;

		public void add(int increment) {
			i += increment;
		}
	}

	public static void main(String[] args) {
		SwingCustomizer.getDefault().openObjectFrame(new Test(), "test", null);
	}
}
