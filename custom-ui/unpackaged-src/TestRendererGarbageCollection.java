import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

public class TestRendererGarbageCollection {

	public static void main(String[] args) {
		test();
		System.out.println("test finished");
	}

	private static void test() {
		SwingCustomizer renderer = new SwingCustomizer(new CustomizedUI());
		renderer.showBusyDialogWhile(null, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

			}
		}, "nothing");
		System.out.println(renderer);
	}

}
