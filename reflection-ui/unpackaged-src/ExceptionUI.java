import xy.reflect.ui.ReflectionUI;


public class ExceptionUI {

	public static void main(String[] args){
		new ReflectionUI().getSwingRenderer().openObjectDialog(null, new ExceptionUI(), true);
	}
	
	public void throwException() throws Exception{
		Object.class.getField("bad field name");
	}
}
