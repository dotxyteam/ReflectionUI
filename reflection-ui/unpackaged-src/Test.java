
public class Test {

	public static void main(String[] args) {
		StringBuilder s = new StringBuilder("azerty");
		s.delete(0, Integer.MAX_VALUE);
		System.out.println(s.length() + ":" +s.toString());
	}

}
