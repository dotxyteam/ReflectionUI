
package xy.reflect.ui.info.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
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
	protected Class<?> objectJavaClass;
	protected List<IParameterInfo> parameters;
	protected int duplicateSignatureIndex = -1;
	protected String name;
	protected String caption;
	protected final Object mutex = new Object();

	public DefaultMethodInfo(ReflectionUI reflectionUI, Method javaMethod, Class<?> objectJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaMethod = javaMethod;
		this.objectJavaClass = objectJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	public static boolean isCompatibleWith(Method javaMethod, Class<?> objectJavaClass) {
		if (GetterFieldInfo.isCompatibleWith(javaMethod, objectJavaClass)) {
			return false;
		}
		for (Method otherJavaMethod : objectJavaClass.getMethods()) {
			if (!otherJavaMethod.equals(javaMethod)) {
				if (GetterFieldInfo.isCompatibleWith(otherJavaMethod, objectJavaClass)) {
					if (javaMethod.equals(GetterFieldInfo.getValidSetterMethod(otherJavaMethod, objectJavaClass))) {
						return false;
					}
				}
			}
		}
		for (Method commonMethod : Object.class.getMethods()) {
			if (ClassUtils.isOverriddenBy(commonMethod, javaMethod)) {
				return false;
			}
		}
		return true;
	}

	public Method getJavaMethod() {
		return javaMethod;
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		try {
			javaMethod.setAccessible(true);
		} catch (Throwable t) {
			reflectionUI.logDebug(t);
		}
	}

	@Override
	public String getName() {
		if (name == null) {
			name = javaMethod.getName();
			int index = obtainDuplicateSignatureIndex();
			if (index > 0) {
				name += "." + Integer.toString(index);
			}
		}
		return name;
	}

	protected int obtainDuplicateSignatureIndex() {
		if (duplicateSignatureIndex == -1) {
			duplicateSignatureIndex = 0;
			Method[] allJavaMethods = objectJavaClass.getMethods();
			ReflectionUIUtils.sortMethods(allJavaMethods);
			for (Method eachJavaMethod : allJavaMethods) {
				if (DefaultMethodInfo.isCompatibleWith(eachJavaMethod, objectJavaClass)) {
					if (ReflectionUIUtils.buildMethodSignature(eachJavaMethod)
							.equals(ReflectionUIUtils.buildMethodSignature(javaMethod))) {
						if (eachJavaMethod.equals(javaMethod)) {
							break;
						}
						duplicateSignatureIndex++;
					}
				}
			}
		}
		return duplicateSignatureIndex;
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
			int index = obtainDuplicateSignatureIndex();
			if (index > 0) {
				caption += " (" + (index + 1) + ")";
			}
		}
		return caption;
	}

	@Override
	public IValidationJob getReturnValueAbstractFormValidationJob(Object object, Object returnValue) {
		return null;
	}

	@Override
	public boolean isControlReturnValueValiditionEnabled() {
		return false;
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
	public String getExecutionSuccessMessage() {
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
		if (javaMethod.getReturnType() == void.class) {
			return null;
		} else {
			return reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaMethod.getReturnType(), javaMethod, -1, null));
		}
	}

	@Override
	public List<IParameterInfo> getParameters() {
		synchronized (mutex) {
			if (parameters == null) {
				parameters = new ArrayList<IParameterInfo>();
				Parameter[] javaParameters = javaMethod.getParameters();
				for (int i = 0; i < javaParameters.length; i++) {
					Parameter javaParameter = javaParameters[i];
					if (!DefaultParameterInfo.isCompatibleWith(javaParameter)) {
						continue;
					}
					parameters
							.add(new DefaultParameterInfo(reflectionUI, javaParameter, i, javaMethod, objectJavaClass));
				}
			}
			return parameters;
		}
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object[] args = new Object[javaMethod.getParameterTypes().length];
		for (int i = 0; i < args.length; i++) {
			args[i] = invocationData.getParameterValue(i);
		}
		try {
			return javaMethod.invoke(object, args);
		} catch (InvocationTargetException e) {
			throw new ReflectionUIError(e.getTargetException());
		} catch (Exception e) {
			throw new ReflectionUIError(e);
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
	public boolean isRelevant(Object object) {
		return true;
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
	public Runnable getPreviousInvocationCustomRedoJob(Object object, InvocationData invocationData) {
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
		result = prime * result + ((objectJavaClass == null) ? 0 : objectJavaClass.hashCode());
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		if (objectJavaClass == null) {
			if (other.objectJavaClass != null)
				return false;
		} else if (!objectJavaClass.equals(other.objectJavaClass))
			return false;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMethodInfo [javaMethod=" + javaMethod + ", objectJavaClass=" + objectJavaClass + "]";
	}

}
