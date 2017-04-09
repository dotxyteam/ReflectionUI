import java.io.File;

import xy.reflect.ui.control.swing.SwingRenderer;

public class Test  {

	public File[] disks = File.listRoots();

	public static void main(String[] args) {
		SwingRenderer.getDefault().openObjectDialog(null, new Test());
	}

}
