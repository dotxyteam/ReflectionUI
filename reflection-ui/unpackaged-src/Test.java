import java.io.File;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class Test {

	public static void main(String[] args) {
		SwingRenderer.getDefault().openObjectFrame(new File("tmp"));
	}

}
