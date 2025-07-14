
package xy.reflect.ui.info.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This class represents field information generated from a
 * '(get|is|has)Something()' Java method. If the corresponding
 * 'setSomething(...)' method is not found in the same containing class then
 * this field will be "read-only" (may not be editable).
 * 
 * Note that the field name of underlying synthetic methods will have a special
 * prefix. A unique suffix may also be added to avoid collisions when there are
 * multiple fields with the same name accessible from the same containing type
 * information.
 * 
 * @author olitank
 *
 */
public class GetterFieldInfo extends AbstractInfo implements IFieldInfo {

	public static final Pattern GETTER_PATTERN = Pattern.compile("^(?:get|is|has)(.*)");

	protected ReflectionUI reflectionUI;
	protected Method javaGetterMethod;
	protected Class<?> objectJavaClass;
	protected IMethodInfo setterMethodInfo;
	protected int duplicateNameIndex = -1;
	protected String name;
	protected String caption;

	public GetterFieldInfo(ReflectionUI reflectionUI, Method javaGetterMethod, Class<?> objectJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaGetterMethod = javaGetterMethod;
		this.objectJavaClass = objectJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	public Method getJavaGetterMethod() {
		return javaGetterMethod;
	}

	public static String getterToFieldName(String getterMethodName) {
		Matcher m = GETTER_PATTERN.matcher(getterMethodName);
		if (!m.matches()) {
			return null;
		}
		String result = m.group(1);
		if (result.length() > 0) {
			result = MiscUtils.changeCase(result, false, 0, 1);
		}
		return result;
	}

	protected static String namePrefix(Method javaGetterMethod) {
		return javaGetterMethod.isSynthetic() ? "_" : "";
	}

	public static Method getValidSetterMethod(Method javaGetterMethod, Class<?> objectJavaClass) {
		String fieldName = getterToFieldName(javaGetterMethod.getName());
		String setterMethodName = "set" + ((fieldName.length() > 0) ? MiscUtils.changeCase(fieldName, true, 0, 1) : "");
		try {
			for (Method otherMethod : objectJavaClass.getMethods()) {
				if (otherMethod.getName().equals(setterMethodName)) {
					if (otherMethod.getParameterTypes().length == 1) {
						if (otherMethod.getParameterTypes()[0].equals(javaGetterMethod.getReturnType())) {
							if (Modifier.isStatic(otherMethod.getModifiers()) == Modifier
									.isStatic(javaGetterMethod.getModifiers())) {
								return otherMethod;
							}
						}
					}
				}
			}
			return null;
		} catch (SecurityException e) {
			throw new ReflectionUIError(e);
		}
	}

	public static boolean isCompatibleWith(Method javaMethod, Class<?> objectJavaClass) {
		String fieldName = GetterFieldInfo.getterToFieldName(javaMethod.getName());
		if (fieldName == null) {
			return false;
		}
		for (Field siblingField : objectJavaClass.getFields()) {
			if (PublicFieldInfo.isCompatibleWith(siblingField)) {
				if (siblingField.getName().equals(fieldName)) {
					return false;
				}
			}
		}
		if (javaMethod.getParameterTypes().length > 0) {
			return false;
		}
		for (Method commonMethod : Object.class.getMethods()) {
			if (ClassUtils.isOverriddenBy(commonMethod, javaMethod)) {
				return false;
			}
		}
		return true;
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		try {
			javaGetterMethod.setAccessible(true);
		} catch (Throwable t) {
			reflectionUI.logDebug(t);
		}
	}

	public DefaultMethodInfo getGetterMethodInfo() {
		return new DefaultMethodInfo(reflectionUI, javaGetterMethod, objectJavaClass);
	}

	protected IMethodInfo getSetterMethodInfo() {
		if (setterMethodInfo == null) {
			Method javaSetterMethod = GetterFieldInfo.getValidSetterMethod(javaGetterMethod, objectJavaClass);
			if (javaSetterMethod == null) {
				setterMethodInfo = IMethodInfo.NULL_METHOD_INFO;
			} else {
				setterMethodInfo = new DefaultMethodInfo(reflectionUI, javaSetterMethod, objectJavaClass);
			}
		}
		if (setterMethodInfo == IMethodInfo.NULL_METHOD_INFO) {
			return null;
		}
		return setterMethodInfo;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = namePrefix(javaGetterMethod) + GetterFieldInfo.getterToFieldName(javaGetterMethod.getName());
			int index = obtainDuplicateNameIndex();
			if (index > 0) {
				name += "." + Integer.toString(index);
			}
		}
		return name;
	}

	protected int obtainDuplicateNameIndex() {
		if (duplicateNameIndex == -1) {
			duplicateNameIndex = 0;
			Method[] allJavaMethods = objectJavaClass.getMethods();
			ReflectionUIUtils.sortMethods(allJavaMethods);
			for (Method eachJavaMethod : allJavaMethods) {
				if (GetterFieldInfo.isCompatibleWith(eachJavaMethod, objectJavaClass)) {
					if ((namePrefix(eachJavaMethod) + GetterFieldInfo.getterToFieldName(eachJavaMethod.getName()))
							.equals(namePrefix(javaGetterMethod)
									+ GetterFieldInfo.getterToFieldName(javaGetterMethod.getName()))) {
						if (eachJavaMethod.equals(javaGetterMethod)) {
							break;
						}
						duplicateNameIndex++;
					}
				}
			}
		}
		return duplicateNameIndex;
	}

	@Override
	public String getCaption() {
		if (caption == null) {
			caption = ReflectionUIUtils
					.identifierToCaption(GetterFieldInfo.getterToFieldName(javaGetterMethod.getName()));
			int index = obtainDuplicateNameIndex();
			if (index > 0) {
				caption += " (" + (index + 1) + ")";
			}
		}
		return caption;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isRelevant(Object object) {
		return true;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 0.0;
	}

	@Override
	public boolean isDisplayAreaHorizontallyFilled() {
		return true;
	}

	@Override
	public boolean isDisplayAreaVerticallyFilled() {
		return false;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaGetterMethod.getReturnType(), javaGetterMethod, -1,
				new SpecificitiesIdentifier(
						reflectionUI.getTypeInfo(new JavaTypeInfoSource(objectJavaClass, null)).getName(),
						GetterFieldInfo.this.getName())));
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		IMethodInfo getter = getGetterMethodInfo();
		return getter.invoke(object, new InvocationData(object, getter));
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(Object object, Object newValue) {
		return null;
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		IMethodInfo setter = getSetterMethodInfo();
		setter.invoke(object, new InvocationData(object, setter, value));
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return getSetterMethodInfo() == null;
	}

	@Override
	public boolean isTransient() {
		return (getSetterMethodInfo() != null) && (getSetterMethodInfo().isReadOnly());
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
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public boolean isControlValueValiditionEnabled() {
		return false;
	}

	@Override
	public IValidationJob getValueAbstractFormValidationJob(Object object) {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaGetterMethod == null) ? 0 : javaGetterMethod.hashCode());
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
		GetterFieldInfo other = (GetterFieldInfo) obj;
		if (javaGetterMethod == null) {
			if (other.javaGetterMethod != null)
				return false;
		} else if (!javaGetterMethod.equals(other.javaGetterMethod))
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
		return "GetterFieldInfo [javaGetterMethod=" + javaGetterMethod + ", objectJavaClass=" + objectJavaClass + "]";
	}

}
