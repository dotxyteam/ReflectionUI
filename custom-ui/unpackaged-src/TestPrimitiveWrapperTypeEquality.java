
import java.awt.Point;
import java.lang.reflect.Method;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public class TestPrimitiveWrapperTypeEquality {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(int.class.equals(Integer.class));
		Method pointMoveMethod = Point.class.getMethod("move", int.class, int.class);
		ReflectionUI reflectionUI = new ReflectionUI();
		ITypeInfo type = reflectionUI
				.getTypeInfo(new JavaTypeInfoSource(Integer.class, pointMoveMethod, 1, null));
		System.out.println(type);
		type = reflectionUI.getTypeInfo(
				new JavaTypeInfoSource(pointMoveMethod.getParameterTypes()[1], pointMoveMethod, 1, null));
		System.out.println(type);
	}

}
