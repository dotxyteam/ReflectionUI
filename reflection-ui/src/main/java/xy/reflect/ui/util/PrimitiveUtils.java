package xy.reflect.ui.util;

import java.lang.reflect.Array;

public class PrimitiveUtils {

	protected static boolean DEFAULT_BOOLEAN;
	protected static byte DEFAULT_BYTE;
	protected static short DEFAULT_SHORT;
	protected static int DEFAULT_INT;
	protected static long DEFAULT_LONG;
	protected static float DEFAULT_FLOAT;
	protected static double DEFAULT_DOUBLE;
	protected static char DEFAULT_CHAR;

	public static Class<?> primitiveToWrapperType(Class<?> class1) {
		if (class1 == byte.class) {
			return Byte.class;
		} else if (class1 == short.class) {
			return Short.class;
		} else if (class1 == int.class) {
			return Integer.class;
		} else if (class1 == long.class) {
			return Long.class;
		} else if (class1 == float.class) {
			return Float.class;
		} else if (class1 == double.class) {
			return Double.class;
		} else if (class1 == char.class) {
			return Character.class;
		} else if (class1 == boolean.class) {
			return Boolean.class;
		} else {
			return null;
		}
	}

	public static Class<?> wrapperToPrimitiveType(Class<?> class1) {
		if (class1 == Byte.class) {
			return byte.class;
		} else if (class1 == Short.class) {
			return short.class;
		} else if (class1 == Integer.class) {
			return int.class;
		} else if (class1 == Long.class) {
			return long.class;
		} else if (class1 == Float.class) {
			return float.class;
		} else if (class1 == Double.class) {
			return double.class;
		} else if (class1 == Character.class) {
			return char.class;
		} else if (class1 == Boolean.class) {
			return boolean.class;
		} else {
			return null;
		}
	}

	public static boolean isPrimitiveTypeOrWrapperOrString(Class<?> class1) {
		return (class1 == String.class) || isPrimitiveTypeOrWrapper(class1);
	}

	public static boolean isPrimitiveTypeOrWrapper(Class<?> class1) {
		if (class1.isArray()) {
			return false;
		} else if (primitiveToWrapperType(class1) != null) {
			return true;
		} else if (wrapperToPrimitiveType(class1) != null) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isPrimitiveWrapper(Class<?> class1) {
		return wrapperToPrimitiveType(class1) != null;
	}

	public static Object getDefaultValue(Class<?> clazz) {
		if (clazz.equals(boolean.class)) {
			return DEFAULT_BOOLEAN;
		} else if (clazz.equals(byte.class)) {
			return DEFAULT_BYTE;
		} else if (clazz.equals(short.class)) {
			return DEFAULT_SHORT;
		} else if (clazz.equals(int.class)) {
			return DEFAULT_INT;
		} else if (clazz.equals(long.class)) {
			return DEFAULT_LONG;
		} else if (clazz.equals(float.class)) {
			return DEFAULT_FLOAT;
		} else if (clazz.equals(double.class)) {
			return DEFAULT_DOUBLE;
		} else if (clazz.equals(char.class)) {
			return DEFAULT_CHAR;
		} else {
			throw new IllegalArgumentException("Class type " + clazz + " not supported");
		}
	}

	public static Object primitiveToWrapperArray(final Object primitiveArray) {
		final Class<?> arrayType = primitiveArray.getClass();
		final int length = Array.getLength(primitiveArray);
		Class<?> primitiveType = arrayType.getComponentType();
		Class<?> wrapperType = primitiveToWrapperType(primitiveType);
		Object	result = Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			final Object wrapper = Array.get(primitiveArray, i);
			Array.set(result, i, wrapper);
		}
		return result;
	}

}
