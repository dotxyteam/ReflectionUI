package xy.reflect.ui.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import xy.reflect.ui.info.annotation.Name;

public class Parameter extends AccessibleObject {

	private final Member invokable;
	private final int position;
	private Class<?>[] invokableParameterTypes;
	private Annotation[][] invokableParameterAnnotations;
	private String name;

	public Parameter(Member invokable, int position) {
		this.invokable = invokable;
		this.position = position;
		if (invokable instanceof Method) {
			Method method = (Method) invokable;
			this.invokableParameterTypes = method.getParameterTypes();
			this.invokableParameterAnnotations = method
					.getParameterAnnotations();
		} else if (invokable instanceof Constructor) {
			Constructor<?> constructor = (Constructor<?>) invokable;
			this.invokableParameterTypes = constructor.getParameterTypes();
			this.invokableParameterAnnotations = constructor
					.getParameterAnnotations();
		} else {
			throw new ReflectionUIError();
		}
	}

	public Class<?> getType() {
		return invokableParameterTypes[position];
	}

	public Member getDeclaringInvokable() {
		return invokable;
	}

	public int getPosition() {
		return position;
	}

	public Class<?>[] getDeclaringInvokableParameterTypes() {
		return invokableParameterTypes;
	}

	public Annotation[][] getDeclaringInvokableParameterAnnotations() {
		return invokableParameterAnnotations;
	}

	public String getName() {
		if (name == null) {
			String[] parameterNames = ReflectionUIUtils
					.getJavaParameterNames(invokable);
			if (parameterNames == null) {
				for (Annotation annotation : invokableParameterAnnotations[position]) {
					if (annotation instanceof Name) {
						return ((Name) annotation).value();
					}
				}
				name = "parameter" + (position + 1);
			} else {
				name = parameterNames[position];
			}
		}
		return name;
	}

	@Override
	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationType) {
		return getAnnotation(annotationType) != null;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		for (Annotation annotation : invokableParameterAnnotations[position]) {
			if (annotationType.isInstance(annotation)) {
				return annotationType.cast(annotation);
			}
		}
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return getDeclaredAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return invokableParameterAnnotations[position];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((invokable == null) ? 0 : invokable.hashCode());
		result = prime * result + position;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (invokable == null) {
			if (other.invokable != null)
				return false;
		} else if (!invokable.equals(other.invokable))
			return false;
		if (position != other.position)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getType() + " arg" + position;
	}

}
