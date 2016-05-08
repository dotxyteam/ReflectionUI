import java.io.File;

import xy.reflect.ui.ReflectionUI;

public class Test {

	public static void main(String[] args) {
		new ReflectionUI().getSwingRenderer().openObjectFrame(new File("."));
	}

}
