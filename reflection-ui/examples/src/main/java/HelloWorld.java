import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class HelloWorld {

	public static void main(String[] args) {
		SwingRenderer.getDefault().openObjectFrame(new HelloWorld());
	}

	private String name = "<write your name>";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String sayHello() {
		return "Hello " + name;
	}

}
