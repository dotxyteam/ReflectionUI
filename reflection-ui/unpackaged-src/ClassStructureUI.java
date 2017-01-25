import java.util.ArrayList;
import xy.reflect.ui.StandardReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;


public class ClassStructureUI {

	public static void main(String[] args){
		new SwingRenderer(new StandardReflectionUI()).openObjectDialog(null, ArrayList.class);
	}
}
