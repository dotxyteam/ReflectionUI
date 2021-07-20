


package xy.reflect.ui.info.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.parameter.DefaultParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ClassUtils;

/**
 * Method information extracted from the given Java method.
 * 
 * Note that a unique suffix may be added to the method name to avoid collisions
 * when there are multiple methods with the same signature accessible from the
 * same class.
 * 
 * @author olitank
 *
 */
public class DefaultMethodInfo extends AbstractInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected Method javaMethod;
	protected List<IParameterInfo> parameters;
	protected ITypeInfo returnValueType;
	protected boolean returnValueVoid = false;
	protected int duplicateNameIndex = -1;
	protected String name;
	protected String caption;

	public DefaultMethodInfo(ReflectionUI reflectionUI, Method javaMethod) {
		this.reflectionUI = reflectionUI;
		this.javaMethod = javaMethod;
		resolveJavaReflectionModelAccessProblems();
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaMethod.setAccessible(true);
	}

	@Override
	public String getName() {
		if (name == null) {
			name = javaMethod.getName();
			int index = getDuplicateSignatureIndex(javaMethod);
			if (index > 0) {
				name += "." + Integer.toString(index);
			}
		}
		return name;
	}

	protected int getDuplicateSignatureIndex(Method javaMethod) {
		if (duplicateNameIndex == -1) {
			duplicateNameIndex = 0;
			for (Method otherMethod : javaMethod.getDeclaringClass().getMethods()) {
				if (ReflectionUIUtils.buildMethodSignature(otherMethod)
						.equals(ReflectionUIUtils.buildMethodSignature(javaMethod))) {
					if (!otherMethod.equals(javaMethod)) {
						// other method with same signature forcibly declared in base class
						duplicateNameIndex += 1;
					}
				}
			}
		}
		return duplicateNameIndex;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public String getCaption() {
		if (caption == null) {
			caption = ReflectionUIUtils.identifierToCaption(javaMethod.getName());
			if (getReturnValueType() != null) {
				caption = caption.replaceAll("^Get ", "Show ");
			}
			int index = getDuplicateSignatureIndex(javaMethod);
			if (index > 0) {
				caption += " (" + (index + 1) + ")";
			}
		}
		return caption;
	}

	@Override
	public boolean isEnabled(Object object) {
		return true;
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return null;
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
	}

	@Override
	public ITypeInfo getReturnValueType() {
		if (returnValueVoid) {
			return null;
		}
		if (returnValueType == null) {
			if (javaMethod.getReturnType() == void.class) {
				returnValueVoid = true;
			} else {
				returnValueType = reflectionUI.buildTypeInfo(
						new JavaTypeInfoSource(reflectionUI, javaMethod.getReturnType(), javaMethod, -1, null));
			}
		}
		return returnValueType;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>();
			Class<?>[] parameterTypes = javaMethod.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				Parameter javaParameter = new Parameter(javaMethod, i);
				if (!DefaultParameterInfo.isCompatibleWith(javaParameter)) {
					continue;
				}
				parameters.add(new DefaultParameterInfo(reflectionUI, javaParameter));
			}
		}
		return parameters;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object[] args = new Object[javaMethod.getParameterTypes().length];
		for (IParameterInfo param : getParameters()) {
			args[param.getPosition()] = invocationData.getParameterValue(param.getPosition());
		}
		try {
			return javaMethod.invoke(object, args);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (InvocationTargetException e) {
			throw new ReflectionUIError(e.getTargetException());
		}
	}

	@Override
	public boolean isHidden() {
		if (ClassUtils.isJavaClassMainMethod(javaMethod)) {
			return true;
		}
		return false;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean b) {
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	public static boolean isCompatibleWith(Method javaMethod, Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		if (GetterFieldInfo.isCompatibleWith(javaMethod, containingJavaClass)) {
			return false;
		}
		for (Method otherJavaMethod : containingJavaClass.getMethods()) {
			if (!otherJavaMethod.equals(javaMethod)) {
				if (GetterFieldInfo.isCompatibleWith(otherJavaMethod, containingJavaClass)) {
					if (javaMethod.equals(GetterFieldInfo.getValidSetterMethod(otherJavaMethod, containingJavaClass))) {
						return false;
					}
				}
			}
		}
		for (Method commonMethod : Object.class.getMethods()) {
			if (ClassUtils.isOverridenBy(commonMethod, javaMethod)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaMethod == null) ? 0 : javaMethod.hashCode());
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
		DefaultMethodInfo other = (DefaultMethodInfo) obj;
		if (javaMethod == null) {
			if (other.javaMethod != null)
				return false;
		} else if (!javaMethod.equals(other.javaMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMethodInfo [javaMethod=" + javaMethod + "]";
	}

}
