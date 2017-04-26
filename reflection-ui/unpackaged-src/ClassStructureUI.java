import java.util.ArrayList;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;


public class ClassStructureUI {

	public static void main(String[] args){
		new SwingRenderer(new ReflectionUI()).openObjectDialog(null, ArrayList.class);
	}
}
