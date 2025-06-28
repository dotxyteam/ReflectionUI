/*
 * 
 */
package xy.reflect.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MembersCapsuleFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsListFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPositionProxy;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;

public class ProxiesStaticTest {

	private static final List<Class<?>> STANDARD_BASE_CLASSES = Arrays.asList(Object.class, Comparable.class);

	@Test
	public void test() {
		testProxyCompleteness(ItemPosition.class, ItemPositionProxy.class);
		testWrapperMemberInfoProxy(IFieldInfo.class, MembersCapsuleFieldInfo.EncapsulatedFieldInfoProxy.class);
		testWrapperMemberInfoProxy(IMethodInfo.class, MembersCapsuleFieldInfo.EncapsulatedMethodInfoProxy.class);
		testWrapperMemberInfoProxy(IFieldInfo.class, MultipleFieldsAsListFieldInfo.ListItemDetailsFieldInfo.class);
		testWrapperInfoProxyFactory(PrecomputedTypeInstanceWrapper.InfoFactory.class);
	}

	private <F extends InfoProxyFactory> void testWrapperInfoProxyFactory(Class<F> proxyFactoryClass) {
		for (Method method : collectPublicAndProtectedMethods(InfoProxyFactory.class)) {
			if (isStandardMethod(method)) {
				continue;
			}
			if (Arrays.asList(method.getParameterTypes()).contains(Object.class) || method.getName()
					.equals("isPrimitive")/* isPrimitive must be overriden to return false for wrappers */) {
				try {
					proxyFactoryClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
				} catch (NoSuchMethodException e) {
					Assert.fail(method + " is not implemented in " + proxyFactoryClass);
				}
			}
		}
	}

	private <B, P extends B> void testWrapperMemberInfoProxy(Class<B> baseClass, Class<P> proxyClass) {
		for (Method method : collectPublicAndProtectedMethods(baseClass)) {
			if (isStandardMethod(method)) {
				continue;
			}
			if (Arrays.asList(method.getParameterTypes()).contains(Object.class)
					|| method.getName().equals("isPrimitive")/* isPrimitive must return false for wrappers */) {
				try {
					proxyClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
				} catch (NoSuchMethodException e) {
					Assert.fail(method + " is not implemented in " + proxyClass);
				}
			}
		}
	}

	private <B, P extends B> void testProxyCompleteness(Class<B> baseClass, Class<P> proxyClass)
			throws SecurityException {
		for (Method method : collectPublicAndProtectedMethods(baseClass)) {
			if (isStandardMethod(method)) {
				continue;
			}
			try {
				proxyClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
			} catch (NoSuchMethodException e) {
				Assert.fail(method + " is not implemented in " + proxyClass);
			}
		}
	}

	private List<Method> collectPublicAndProtectedMethods(Class<?> clazz) {
		List<Method> result = new ArrayList<Method>();
		while (clazz != null) {
			result.addAll(Arrays.asList(clazz.getDeclaredMethods()));
			clazz = clazz.getSuperclass();
		}
		return result;
	}

	private boolean isStandardMethod(Method method) {
		for (Class<?> standardBaseClass : STANDARD_BASE_CLASSES) {
			for (Method standardMethod : collectPublicAndProtectedMethods(standardBaseClass)) {
				if ((standardMethod.getName().equals(method.getName()))) {
					if (Arrays.equals(standardMethod.getParameterTypes(), method.getParameterTypes())) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
