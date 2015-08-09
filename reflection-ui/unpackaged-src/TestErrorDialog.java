import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.util.ReflectionUIError;

public class TestErrorDialog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReflectionUI reflectionUI = new ReflectionUI();
		StringBuilder msg = new StringBuilder();
		for (int row = 0; row < 10; row++) {
			if (row > 0) {
				msg.append("\n");
			}
			for (int col = 0; col < 100; col++) {
				msg.append("bla ");
			}
		}
		reflectionUI.getSwingRenderer().openErrorDialog(null, "test",
				new ReflectionUIError(msg.toString()));
	}
}
