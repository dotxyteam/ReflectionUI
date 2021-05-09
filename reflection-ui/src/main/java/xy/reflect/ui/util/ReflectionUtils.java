/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for dealing with classes.
 * 
 * @author olitank
 *
 */
public class ReflectionUtils {

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
		return isPrimitiveClass(class1) || isPrimitiveWrapperClass(class1);
	}

	public static boolean isPrimitiveClass(Class<?> class1) {
		return primitiveToWrapperClass(class1) != null;
	}

	public static boolean isPrimitiveWrapperClass(Class<?> class1) {
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
			throw new IllegalArgumentException("Class '" + clazz + "' is not a valid primitive wrapper class");
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

	public static String primitiveToString(Object object) {
		Class<?> javaType = object.getClass();
		if (!isPrimitiveClassOrWrapper(javaType)) {
			throw new RuntimeException("Invalid primitive type: '" + javaType.getName() + "'");
		}
		return object.toString();
	}

	public static Object primitiveFromString(String text, Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = ReflectionUtils.primitiveToWrapperClass(javaType);
		}
		if (javaType == Character.class) {
			if (text.length() != 1) {
				throw new RuntimeException("Invalid value: '" + text + "'. 1 character is expected");
			}
			return text.charAt(0);
		} else if (javaType == Boolean.class) {
			if (Boolean.TRUE.toString().equals(text)) {
				return true;
			}
			if (Boolean.FALSE.toString().equals(text)) {
				return false;
			}
			throw new RuntimeException("Invalid value: '" + text + "'. Expected '" + Boolean.TRUE.toString() + "' or '"
					+ Boolean.FALSE.toString() + "'");
		} else {
			try {
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
			} catch (Throwable t) {
				throw new ReflectionUIError(javaType.getSimpleName() + " Inupt Error: " + t.toString(), t);
			}
		}
	}

	public static boolean isKnownAsImmutableClass(Class<?> class1) {
		if (isPrimitiveClassOrWrapperOrString(class1)) {
			return true;
		}
		return false;
	}

	public static List<Class<?>> getAncestorClasses(Class<?> type) {
		List<Class<?>> result = new ArrayList<Class<?>>();
		while (type.getSuperclass() != null) {
			result.add(type.getSuperclass());
			type = type.getSuperclass();
		}
		return result;
	}

	public static Set<Class<?>> getAncestorClassesAndInterfaces(Class<?> type) {
		Set<Class<?>> result = new HashSet<Class<?>>();
		List<Class<?>> ancestorClasses = getAncestorClasses(type);
		result.addAll(ancestorClasses);
		result.addAll(getSuperInterfaces(type.getInterfaces()));
		for (Class<?> ancestor : ancestorClasses) {
			result.addAll(getSuperInterfaces(ancestor.getInterfaces()));
		}
		return result;
	}

	public static Set<Class<?>> getSuperInterfaces(Class<?>[] childInterfaces) {
		Set<Class<?>> allInterfaces = new HashSet<Class<?>>();
		for (int i = 0; i < childInterfaces.length; i++) {
			allInterfaces.add(childInterfaces[i]);
			allInterfaces.addAll(getSuperInterfaces(childInterfaces[i].getInterfaces()));
		}
		return allInterfaces;
	}

	public static List<String> gatClassNames(Class<?>[] classes) {
		List<String> result = new ArrayList<String>();
		for (Class<?> clazz : classes) {
			result.add(clazz.getName());
		}
		return result;
	}

	public static Set<Class<?>> getAncestorsAndSelfClassesAndInterfaces(Class<?> type) {
		Set<Class<?>> result = new HashSet<Class<?>>(getAncestorClassesAndInterfaces(type));
		result.add(type);
		return result;
	}

	public static List<Parameter> getJavaParameters(Method javaMethod) {
		List<Parameter> result = new ArrayList<Parameter>();
		for (int i = 0; i < javaMethod.getParameterTypes().length; i++) {
			result.add(new Parameter(javaMethod, i));
		}
		return result;
	}

	public static List<Parameter> getJavaParameters(Constructor<?> ctor) {
		List<Parameter> result = new ArrayList<Parameter>();
		for (int i = 0; i < ctor.getParameterTypes().length; i++) {
			result.add(new Parameter(ctor, i));
		}
		return result;
	}

	public static List<Field> getAllFields(Class<?> type) {
		List<Field> result = new ArrayList<Field>();
		Class<?> currentType = type;
		while (currentType != null && currentType != Object.class) {
			result.addAll(Arrays.asList(currentType.getDeclaredFields()));
			currentType = currentType.getSuperclass();
		}
		return result;
	}

	public static boolean isJavaClassMainMethod(Method javaMethod) {
		if (Modifier.isStatic(javaMethod.getModifiers())) {
			if (javaMethod.getReturnType().equals(void.class)) {
				if (javaMethod.getName().equals("main")) {
					Class<?>[] paramTypes = javaMethod.getParameterTypes();
					if (paramTypes.length == 1) {
						if (paramTypes[0].equals(String[].class)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static String getQualifiedName(Field field) {
		return field.getDeclaringClass().getName() + "#" + field.getName();
	}

	public static String getQualifiedName(Method method) {
		return method.getDeclaringClass().getName() + "#" + method.getName() + "("
				+ MiscUtils.stringJoin(gatClassNames(method.getParameterTypes()), ",") + ")";
	}

	public static String getQualifiedName(Constructor<?> constructor) {
		return constructor.getDeclaringClass().getName() + "#" + constructor.getName() + "("
				+ MiscUtils.stringJoin(gatClassNames(constructor.getParameterTypes()), ",") + ")";
	}

	public static boolean isOverridenBy(Method baseMethod, Method overridingMethod) {
		if (!baseMethod.getDeclaringClass().isAssignableFrom(overridingMethod.getDeclaringClass())) {
			return false;
		}
		if (!baseMethod.getName().equals(overridingMethod.getName())) {
			return false;
		}
		if (!baseMethod.getReturnType().isAssignableFrom(overridingMethod.getReturnType())) {
			return false;
		}
		Class<?>[] baseMethodParamTypes = baseMethod.getParameterTypes();
		Class<?>[] overridingMethodParamTypes = overridingMethod.getParameterTypes();
		if (baseMethodParamTypes.length != overridingMethodParamTypes.length) {
			return false;
		}
		for (int iParam = 0; iParam < baseMethodParamTypes.length; iParam++) {
			Class<?> baseMethodParamType = baseMethodParamTypes[iParam];
			Class<?> overridingMethodParamType = overridingMethodParamTypes[iParam];
			if (!baseMethodParamType.isAssignableFrom(overridingMethodParamType)) {
				return false;
			}
		}
		return true;
	}

}
