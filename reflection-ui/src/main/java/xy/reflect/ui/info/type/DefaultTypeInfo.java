package xy.reflect.ui.info.type;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.PublicFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultTypeInfo extends AbstractInfo implements ITypeInfo {

	protected JavaTypeInfoSource source;
	protected ReflectionUI reflectionUI;
	protected List<IFieldInfo> fields;
	protected List<IMethodInfo> methods;
	protected List<IMethodInfo> constructors;

	public DefaultTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source) {
		if (source == null) {
			throw new ReflectionUIError();
		}
		this.reflectionUI = reflectionUI;
		this.source = source;
	}

	@Override
	public ITypeInfoSource getSource() {
		return source;
	}

	@Override
	public ResourcePath getFormBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getFormBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getFormForegroundColor() {
		return null;
	}

	@Override
	public Dimension getFormPreferredSize() {
		return null;
	}

	@Override
	public boolean onFormVisibilityChange(Object object, boolean visible) {
		return false;
	}

	public Class<?> getJavaType() {
		return source.getJavaType();
	}

	@Override
	public boolean canPersist() {
		return true;
	}

	@Override
	public void save(Object object, OutputStream out) {
		ReflectionUIUtils.saveXML(object, out);
	}

	@Override
	public void load(Object object, InputStream in) {
		ReflectionUIUtils.loadXML(object, in);
	}

	@Override
	public FieldsLayout getFieldsLayout() {
		return FieldsLayout.VERTICAL_FLOW;
	}

	@Override
	public MethodsLayout getMethodsLayout() {
		return MethodsLayout.HORIZONTAL_FLOW;
	}

	@Override
	public MenuModel getMenuModel() {
		return new MenuModel();
	}

	@Override
	public boolean isPrimitive() {
		return getJavaType().isPrimitive();
	}

	@Override
	public boolean isImmutable() {
		return ClassUtils.isKnownAsImmutableClass(getJavaType());
	}

	@Override
	public boolean isConcrete() {
		if ((!getJavaType().isPrimitive()) && Modifier.isAbstract(getJavaType().getModifiers())) {
			return false;
		}
		if (getJavaType().isInterface()) {
			return false;
		}
		if (getJavaType() == Object.class) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isModificationStackAccessible() {
		return true;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		if (constructors == null) {
			constructors = new ArrayList<IMethodInfo>();
			if (ClassUtils.isPrimitiveClassOrWrapperOrString(getJavaType())) {
				constructors.add(new AbstractConstructorInfo() {

					ITypeInfo returnValueType;

					@Override
					public Object invoke(Object parentObject, InvocationData invocationData) {
						if (String.class.equals(getJavaType())) {
							return "";
						} else {
							Class<?> primitiveType = getJavaType();
							if (ClassUtils.isPrimitiveWrapperClass(primitiveType)) {
								primitiveType = ClassUtils.wrapperToPrimitiveClass(getJavaType());
							}
							return ClassUtils.getDefaultPrimitiveValue(primitiveType);
						}
					}

					@Override
					public ITypeInfo getReturnValueType() {
						if (returnValueType == null) {
							returnValueType = reflectionUI
									.getTypeInfo(new PrecomputedTypeInfoSource(DefaultTypeInfo.this, null));
						}
						return returnValueType;
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
			} else {
				for (Constructor<?> javaConstructor : getJavaType().getConstructors()) {
					if (!DefaultConstructorInfo.isCompatibleWith(javaConstructor)) {
						continue;
					}
					constructors.add(new DefaultConstructorInfo(reflectionUI, javaConstructor));
				}
			}
		}
		return constructors;
	}

	@Override
	public String getName() {
		return getJavaType().getName();
	}

	@Override
	public String getCaption() {
		if (String.class.equals(getJavaType())) {
			return "Text";
		} else if (getJavaType().isPrimitive()) {
			return ClassUtils.primitiveToWrapperClass(getJavaType()).getSimpleName();
		} else {
			return ReflectionUIUtils.identifierToCaption(getJavaType().getSimpleName());
		}
	}

	@Override
	public List<IFieldInfo> getFields() {
		if (fields == null) {
			fields = new ArrayList<IFieldInfo>();
			for (Field javaField : getJavaType().getFields()) {
				if (!PublicFieldInfo.isCompatibleWith(javaField)) {
					continue;
				}
				fields.add(new PublicFieldInfo(reflectionUI, javaField, getJavaType()));
			}
			for (Method javaMethod : getJavaType().getMethods()) {
				if (!GetterFieldInfo.isCompatibleWith(javaMethod, getJavaType())) {
					continue;
				}
				GetterFieldInfo getterFieldInfo = new GetterFieldInfo(reflectionUI, javaMethod, getJavaType());
				fields.add(getterFieldInfo);
			}
			ReflectionUIUtils.sortFields(fields);
		}
		return fields;
	}

	@Override
	public List<IMethodInfo> getMethods() {
		if (methods == null) {
			methods = new ArrayList<IMethodInfo>();
			for (Method javaMethod : getJavaType().getMethods()) {
				if (!DefaultMethodInfo.isCompatibleWith(javaMethod, getJavaType())) {
					continue;
				}
				methods.add(new DefaultMethodInfo(reflectionUI, javaMethod));
			}
			ReflectionUIUtils.sortMethods(methods);
		}
		return methods;
	}

	@Override
	public boolean supportsInstance(Object object) {
		if (getJavaType().isPrimitive()) {
			return ClassUtils.primitiveToWrapperClass(getJavaType()).isInstance(object);
		} else {
			return getJavaType().isInstance(object);
		}
	}

	@Override
	public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
		return Collections.emptyList();
	}

	@Override
	public String toString(Object object) {
		ReflectionUIUtils.checkInstance(this, object);
		if (object == null) {
			return null;
		} else if (object instanceof String) {
			return (String) object;
		} else {
			String result = object.toString();
			if (result == null) {
				result = "";
			}
			result = result
					.replaceAll(getJavaType().getName().replace(".", "\\.").replace("$", "\\$").replace("[", "\\[")
							+ "@([0-9a-z]+)", getCaption() + " [id=$1]");
			result = result.replace(getJavaType().getName(), getCaption());
			return result;
		}
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public void validate(Object object) throws Exception {
		ReflectionUIUtils.checkInstance(this, object);
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public boolean canCopy(Object object) {
		ReflectionUIUtils.checkInstance(this, object);
		if (object == null) {
			return true;
		}
		if (object instanceof Serializable) {
			return true;
		}
		return false;
	}

	@Override
	public Object copy(Object object) {
		ReflectionUIUtils.checkInstance(this, object);
		if (object == null) {
			return null;
		}
		return ReflectionUIUtils.copyThroughSerialization((Serializable) object);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		DefaultTypeInfo other = (DefaultTypeInfo) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultTypeInfo [source=" + source + "]";
	}

}
