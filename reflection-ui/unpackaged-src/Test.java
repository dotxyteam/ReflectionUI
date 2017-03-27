import xy.reflect.ui.control.swing.SwingRenderer;

public class Test {

	public String s = "azerty";

	public static void main(String[] args) {
		SwingRenderer.getDefault().openObjectDialog(null, new Test());
	}

}
