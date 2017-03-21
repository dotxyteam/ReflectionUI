package xy.reflect.ui.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {

	protected static final Class<?>[] PRIMITIVE_CLASSES = new Class<?>[] { boolean.class, byte.class, short.class,
			int.class, long.class, float.class, double.class, char.class };
	protected static final Class<?>[] PRIMITIVE_WRAPPER_CLASSES = new Class<?>[] { Boolean.class, Byte.class,
			Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class };

	protected static final Map<String, Class<?>> PRIMITIVE_CLASS_BY_NAME = new HashMap<String, Class<?>>() {
		private static final long serialVersionUID = 1L;
		{
			for (Class<?> c : PRIMITIVE_CLASSES) {
				put(c.getName(), c);
			}
		}
	};

	protected static boolean DEFAULT_BOOLEAN;
	protected static byte DEFAULT_BYTE;
	protected static short DEFAULT_SHORT;
	protected static int DEFAULT_INT;
	protected static long DEFAULT_LONG;
	protected static float DEFAULT_FLOAT;
	protected static double DEFAULT_DOUBLE;
	protected static char DEFAULT_CHAR;

	protected static final Map<String, Class<?>> CLASS_BY_NAME = new HashMap<String, Class<?>>();
	protected static final Class<?> CLASS_NOT_FOUND = (new Object() {
		@Override
		public String toString() {
			return "CLASS_NOT_FOUND";
		}
	}).getClass();

	static {
		for (Class<?> c : new Class[] { void.class, boolean.class, byte.class, char.class, short.class, int.class,
				float.class, double.class, long.class })
			CLASS_BY_NAME.put(c.getName(), c);
	}

	public static Class<?> getCachedClassforName(String name) throws ClassNotFoundException {
		synchronized (CLASS_BY_NAME) {
			Class<?> c = CLASS_BY_NAME.get(name);
			if (c == null) {
				try {
					c = forNameEvenIfPrimitive(name);
				} catch (ClassNotFoundException e) {
					c = CLASS_NOT_FOUND;
				}
			}
			CLASS_BY_NAME.put(name, c);
			if (c == CLASS_NOT_FOUND) {
				throw new ClassNotFoundException(name);
			}
			return c;
		}
	}

	public static Class<?> forNameEvenIfPrimitive(String name) throws ClassNotFoundException {
		Class<?> result = PRIMITIVE_CLASS_BY_NAME.get(name);
		if (result != null) {
			return result;
		}
		result = Class.forName(name);
		return result;
	}

	public static void clearClassCache() {
		synchronized (CLASS_BY_NAME) {
			CLASS_BY_NAME.clear();
		}
	}

	public static Class<?> primitiveToWrapperClass(Class<?> class1) {
		int index = Arrays.asList(PRIMITIVE_CLASSES).indexOf(class1);
		if (index == -1) {
			return null;
		}
		return PRIMITIVE_WRAPPER_CLASSES[index];
	}

	public static Class<?> wrapperToPrimitiveClass(Class<?> class1) {
		int index = Arrays.asList(PRIMITIVE_WRAPPER_CLASSES).indexOf(class1);
		if (index == -1) {
			return null;
		}
		return PRIMITIVE_CLASSES[index];
	}

	public static boolean isPrimitiveClassOrWrapperOrString(Class<?> class1) {
		return (class1 == String.class) || isPrimitiveClassOrWrapper(class1);
	}

	public static boolean isPrimitiveClassOrWrapper(Class<?> class1) {
		return isPrimitiveClass(class1) || isPrimitiveWrapper(class1);
	}

	public static boolean isPrimitiveClass(Class<?> class1) {
		return primitiveToWrapperClass(class1) != null;
	}

	public static boolean isPrimitiveWrapper(Class<?> class1) {
		return wrapperToPrimitiveClass(class1) != null;
	}

	public static Object getDefaultPrimitiveValue(Class<?> clazz) {
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
		Class<?> wrapperType = primitiveToWrapperClass(primitiveType);
		Object result = Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			final Object wrapper = Array.get(primitiveArray, i);
			Array.set(result, i, wrapper);
		}
		return result;
	}

	public static Object primitiveFromText(String text, Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = primitiveToWrapperClass(javaType);
		}
		if (javaType == Character.class) {
			text = text.trim();
			if (text.length() != 1) {
				throw new RuntimeException("Invalid value: '" + text + "'. 1 character is expected");
			}
			return text.charAt(0);
		} else {
			try {
				return javaType.getConstructor(new Class[] { String.class }).newInstance(text);
			} catch (IllegalArgumentException e) {
				throw new ReflectionUIError(e);
			} catch (SecurityException e) {
				throw new ReflectionUIError(e);
			} catch (InstantiationException e) {
				throw new ReflectionUIError(e);
			} catch (IllegalAccessException e) {
				throw new ReflectionUIError(e);
			} catch (InvocationTargetException e) {
				throw new ReflectionUIError(e.getTargetException());
			} catch (NoSuchMethodException e) {
				throw new ReflectionUIError(e);
			}
		}
	}

	public static boolean isKnownAsImmutableClass(Class<?> class1) {
		if (isPrimitiveClassOrWrapperOrString(class1)) {
			return true;
		}
		if (Arrays.<Class<?>>asList(
				java.lang.StackTraceElement.class, java.math.BigInteger.class, java.math.BigDecimal.class,
				java.io.File.class, java.awt.Font.class, java.awt.BasicStroke.class, java.awt.Color.class,
				java.awt.GradientPaint.class, java.awt.LinearGradientPaint.class, java.awt.RadialGradientPaint.class,
				java.awt.Cursor.class, java.util.Locale.class, java.util.UUID.class, java.util.Collections.class,
				java.net.URL.class, java.net.URI.class, java.net.Inet4Address.class, java.net.Inet6Address.class,
				java.net.InetSocketAddress.class
		).contains(class1)) {
			return true;
		}
		return false;
	}

}
