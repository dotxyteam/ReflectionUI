import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;

public class Test {

	public List<String> list = new ArrayList<String>();
	
	public static void main(String[] args) {
		new ReflectionUI().getSwingRenderer().openObjectFrame(new Test());
	}

}
