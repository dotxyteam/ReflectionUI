import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

public class TestInner {

	public class Inner {
	}

	public static void main(String[] args) throws ClassNotFoundException {
		Class<?> innerClass = Class.forName("TestInner$Inner");
		Constructor<?>[] constructors = innerClass.getConstructors();
		String str = Arrays.stream(constructors).map(TestInner::stringifyConstructor).collect(Collectors.joining(", "));
		System.out.println(str);
	}

	public static String stringifyConstructor(Constructor<?> c) {
		return "(" + Arrays.stream(c.getParameters()).map(p -> p.getType().getName() + " " + p.getName())
				.collect(Collectors.joining(", ")) + ")";
	}
}