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

import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MembersCapsuleFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsListFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPositionProxy;
import xy.reflect.ui.info.type.iterable.map.StandardMapAsListTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;

public class StaticTests {

	private static final List<Class<?>> STANDARD_BASE_CLASSES = Arrays.asList(Object.class, Comparable.class);

	@Test
	public void checkInfoCustomizationsClass() {
		checkCustomizationClass(InfoCustomizations.class);
		for (Class<?> customizationClass : InfoCustomizations.class.getDeclaredClasses()) {
			checkCustomizationClass(customizationClass);
		}
	}

	private void checkCustomizationClass(Class<?> customizationClass) {
		ITypeInfo customizationTypeInfo = ReflectionUI.getDefault()
				.getTypeInfo(new JavaTypeInfoSource(customizationClass, null));
		for (IFieldInfo field : customizationTypeInfo.getFields()) {
			if (field.isGetOnly()) {
				continue;
			}
			if (!(field.getType() instanceof IListTypeInfo)) {
				continue;
			}
			if (field.getType() instanceof StandardMapAsListTypeInfo) {
				continue;
			}
			if (!(field instanceof GetterFieldInfo)) {
				continue;
			}
			Method getter = ((GetterFieldInfo) field).getJavaGetterMethod();
			if (getter.getAnnotation(javax.xml.bind.annotation.XmlTransient.class) != null) {
				continue;
			}
			if (getter.getAnnotation(javax.xml.bind.annotation.XmlElements.class) != null) {
				continue;
			}
			if (getter.getAnnotation(javax.xml.bind.annotation.XmlElement.class) != null) {
				new InfoCustomizations() {

					private static final long serialVersionUID = 1L;
					{
						String migratedLocalName = migrator.migrateXMLLocalName(field.getName());
						if (migratedLocalName != null) {
							javax.xml.bind.annotation.XmlElement annotation = getter
									.getAnnotation(javax.xml.bind.annotation.XmlElement.class);
							if (!migratedLocalName.equals(annotation.name())) {
								Assert.fail("The singular form of list item XML element name specified by annotation ("
										+ annotation.name() + ") does not match the migrated name (" + migratedLocalName
										+ ") for " + getter);
							}
						}
					}

				};
				continue;
			}
			Assert.fail("The plural form of list item XML element name was not changed by annotation for " + getter);
		}
	}

	@Test
	public void checkProxyClasses() {
		checkProxyCompleteness(ItemPosition.class, ItemPositionProxy.class);
		checkWrapperMemberInfoProxy(IFieldInfo.class, MembersCapsuleFieldInfo.EncapsulatedFieldInfoProxy.class);
		checkWrapperMemberInfoProxy(IMethodInfo.class, MembersCapsuleFieldInfo.EncapsulatedMethodInfoProxy.class);
		checkWrapperMemberInfoProxy(IFieldInfo.class, MultipleFieldsAsListFieldInfo.ListItemDetailsFieldInfo.class);
		checkWrapperInfoProxyFactory(PrecomputedTypeInstanceWrapper.InfoFactory.class);
	}

	private <F extends InfoProxyFactory> void checkWrapperInfoProxyFactory(Class<F> proxyFactoryClass) {
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

	private <B, P extends B> void checkWrapperMemberInfoProxy(Class<B> baseClass, Class<P> proxyClass) {
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

	private <B, P extends B> void checkProxyCompleteness(Class<B> baseClass, Class<P> proxyClass)
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
