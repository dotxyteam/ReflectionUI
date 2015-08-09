import xy.reflect.ui.ReflectionUI;

class MyMath {
	public static double sqrt(double d) {
		return Math.sqrt(d);
	}
	
	public static void main(String[] args){
		new ReflectionUI().getSwingRenderer().openObjectFrame(new MyMath(), null, null);
	}
}