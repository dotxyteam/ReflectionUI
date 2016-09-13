package xy.reflect.ui.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {

	public static Class<?>[] PRIMITIVE_CLASSES = new Class<?>[] { boolean.class, byte.class, short.class, int.class,
			long.class, float.class, double.class, char.class };
	protected static Map<String, Class<?>> PRIMITIVE_CLASS_BY_NAME = new HashMap<String, Class<?>>() {
		private static final long serialVersionUID = 1L;
		{
			for(Class<?> c: PRIMITIVE_CLASSES){
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

	protected static final Map<String, Class<?>> CLASS_BY_NAME_CACHE = new HashMap<String, Class<?>>();
	protected static final Class<?> CLASS_NOT_FOUND = (new Object() {
	}).getClass();

	static {
		for (Class<?> c : new Class[] { void.class, boolean.class, byte.class, char.class, short.class, int.class,
				float.class, double.class, long.class })
			CLASS_BY_NAME_CACHE.put(c.getName(), c);
	}

	public static Class<?> getCachedClassforName(String name) throws ClassNotFoundException {
		synchronized (CLASS_BY_NAME_CACHE) {
			Class<?> c = CLASS_BY_NAME_CACHE.get(name);
			if (c == null) {
				try {
					c = forNameEvenPrimitive(name);
				} catch (ClassNotFoundException e) {
					c = CLASS_NOT_FOUND;
				}
			}
			CLASS_BY_NAME_CACHE.put(name, c);
			if (c == CLASS_NOT_FOUND) {
				throw new ClassNotFoundException(name);
			}
			return c;
		}
	}

	public static Class<?> forNameEvenPrimitive(String name) throws ClassNotFoundException {
		Class<?> result = PRIMITIVE_CLASS_BY_NAME.get(name);
		if(result != null){
			return result;
		}
		result = Class.forName(name);
		return result;
	}

	public static void resetClassCacheForName() {
		synchronized (CLASS_BY_NAME_CACHE) {
			CLASS_BY_NAME_CACHE.clear();
		}
	}

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
		return isPrimitive(class1) || isPrimitiveWrapper(class1);
	}

	public static boolean isPrimitive(Class<?> class1) {
		return primitiveToWrapperType(class1) != null;
	}

	public static boolean isPrimitiveWrapper(Class<?> class1) {
		return wrapperToPrimitiveType(class1) != null;
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
		Class<?> wrapperType = primitiveToWrapperType(primitiveType);
		Object result = Array.newInstance(wrapperType, length);
		for (int i = 0; i < length; i++) {
			final Object wrapper = Array.get(primitiveArray, i);
			Array.set(result, i, wrapper);
		}
		return result;
	}

	public static Object primitiveFromText(String text, Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = primitiveToWrapperType(javaType);
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

}
