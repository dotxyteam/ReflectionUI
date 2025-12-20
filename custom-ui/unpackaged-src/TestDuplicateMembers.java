import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

public class TestDuplicateMembers {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingCustomizer.getDefault().openObjectFrame(new TestDuplicateMembers());
			}
		});
	}

	public String field = "public field";

	public String getField() {
		return "getter field";
	}

	public String getField(int i) {
		return "getter field with i";
	}

	public String getField(int i, int j) {
		return "getter field with i and j";
	}

}
