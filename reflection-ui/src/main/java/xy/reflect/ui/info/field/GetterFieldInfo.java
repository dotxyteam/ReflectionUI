
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
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ClassUtils;

/**
 * Field generated from a '(get|is|has)Something()' Java method. If the
 * corresponding 'setSomething(...)' method is not found in the same class then
 * this field will be "read-only" (may not be editable).
 * 
 * Note that a unique suffix may be added to the field name to avoid collisions
 * when there are multiple fields with the same name accessible from the same
 * class.
 * 
 * @author olitank
 *
 */
public class GetterFieldInfo extends AbstractInfo implements IFieldInfo {

	public static final Pattern GETTER_PATTERN = Pattern.compile("^(?:get|is|has)([A-Z].*)");

	protected ReflectionUI reflectionUI;
	protected Method javaGetterMethod;
	protected Class<?> containingJavaClass;
	protected ITypeInfo type;
	protected IMethodInfo setterMethodInfo;
	protected int duplicateNameIndex = -1;
	protected String name;
	protected String caption;

	public GetterFieldInfo(ReflectionUI reflectionUI, Method javaGetterMethod, Class<?> containingJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaGetterMethod = javaGetterMethod;
		this.containingJavaClass = containingJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	protected static String getterToFieldName(String getterMethodName) {
		Matcher m = GETTER_PATTERN.matcher(getterMethodName);
		if (!m.matches()) {
			return null;
		}
		String result = m.group(1);
		if (result != null) {
			result = MiscUtils.changeCase(result, false, 0, 1);
		}
		return result;
	}

	public static Method getValidSetterMethod(Method javaGetterMethod, Class<?> containingJavaClass) {
		String fieldName = getterToFieldName(javaGetterMethod.getName());
		String setterMethodName = "set" + MiscUtils.changeCase(fieldName, true, 0, 1);
		try {
			for (Method otherMethod : containingJavaClass.getMethods()) {
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

	public static boolean isCompatibleWith(Method javaMethod, Class<?> containingJavaClass) {
		if (javaMethod.isSynthetic()) {
			return false;
		}
		if (javaMethod.isBridge()) {
			return false;
		}
		String fieldName = GetterFieldInfo.getterToFieldName(javaMethod.getName());
		if (fieldName == null) {
			return false;
		}
		for (Field siblingField : containingJavaClass.getFields()) {
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
			if (ClassUtils.isOverridenBy(commonMethod, javaMethod)) {
				return false;
			}
		}
		return true;
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaGetterMethod.setAccessible(true);
	}

	public IMethodInfo getGetterMethodInfo() {
		return new DefaultMethodInfo(reflectionUI, javaGetterMethod);
	}

	protected IMethodInfo getSetterMethodInfo() {
		if (setterMethodInfo == null) {
			Method javaSetterMethod = GetterFieldInfo.getValidSetterMethod(javaGetterMethod, containingJavaClass);
			if (javaSetterMethod == null) {
				setterMethodInfo = IMethodInfo.NULL_METHOD_INFO;
			} else {
				setterMethodInfo = new DefaultMethodInfo(reflectionUI, javaSetterMethod);
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
			name = GetterFieldInfo.getterToFieldName(javaGetterMethod.getName());
			int index = getDuplicateSignatureIndex(javaGetterMethod);
			if (index > 0) {
				name += "." + Integer.toString(index);
			}
		}
		return name;
	}

	protected int getDuplicateSignatureIndex(Method javaMethod) {
		if (duplicateNameIndex == -1) {
			for (Method otherMethod : javaMethod.getDeclaringClass().getMethods()) {
				if (ReflectionUIUtils.buildMethodSignature(otherMethod)
						.equals(ReflectionUIUtils.buildMethodSignature(javaMethod))) {
					if (!otherMethod.equals(javaMethod)) {
						// other method with same signature forcibly declared in base class
						duplicateNameIndex = getDuplicateSignatureIndex(otherMethod) + 1;
					}
				}
			}
			if (duplicateNameIndex == -1) {
				duplicateNameIndex = 0;
			}
		}
		return duplicateNameIndex;
	}

	@Override
	public String getCaption() {
		if (caption == null) {
			caption = ReflectionUIUtils
					.identifierToCaption(GetterFieldInfo.getterToFieldName(javaGetterMethod.getName()));
			int index = getDuplicateSignatureIndex(javaGetterMethod);
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
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 1.0;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI
					.buildTypeInfo(new TypeInfoSourceProxy(getGetterMethodInfo().getReturnValueType().getSource()) {
						@Override
						public SpecificitiesIdentifier getSpecificitiesIdentifier() {
							return new SpecificitiesIdentifier(reflectionUI
									.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, containingJavaClass, null))
									.getName(), GetterFieldInfo.this.getName());
						}

						@Override
						protected String getTypeInfoProxyFactoryIdentifier() {
							return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", javaGetterMethod="
									+ javaGetterMethod + ", containingJavaClass=" + containingJavaClass + "]";
						}
					});
		}
		return type;
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
		return javaGetterMethod.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return javaGetterMethod.equals(((GetterFieldInfo) obj).javaGetterMethod);
	}

	@Override
	public String toString() {
		return "GetterFieldInfo [javaGetterMethod=" + javaGetterMethod + "]";
	}

}
