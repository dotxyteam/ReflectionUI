package xy.reflect.ui.info.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.PublicFieldInfo;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultTypeInfo implements ITypeInfo {

	protected Class<?> javaType;
	protected ReflectionUI reflectionUI;
	protected List<IFieldInfo> fields;
	protected List<IMethodInfo> methods;
	protected List<IMethodInfo> constructors;

	public DefaultTypeInfo(ReflectionUI reflectionUI, Class<?> javaType) {
		if (javaType == null) {
			throw new ReflectionUIError();
		}
		this.reflectionUI = reflectionUI;
		this.javaType = javaType;
	}

	public Class<?> getJavaType() {
		return javaType;
	}

	@Override
	public boolean isPassedByReference() {
		return !javaType.isPrimitive();
	}

	@Override
	public boolean isConcrete() {
		if ((!javaType.isPrimitive()) && Modifier.isAbstract(javaType.getModifiers())) {
			return false;
		}
		if (javaType == Object.class) {
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
			if (!isConcrete()) {
				return Collections.emptyList();
			}
			for (Constructor<?> javaConstructor : javaType.getConstructors()) {
				if (!DefaultConstructorInfo.isCompatibleWith(javaConstructor)) {
					continue;
				}
				constructors.add(new DefaultConstructorInfo(reflectionUI, this, javaConstructor));
			}
		}
		return constructors;
	}

	@Override
	public String getName() {
		return javaType.getName();
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.identifierToCaption(javaType.getSimpleName());
	}

	@Override
	public List<IFieldInfo> getFields() {
		if (fields == null) {
			fields = new ArrayList<IFieldInfo>();
			for (Field javaField : javaType.getFields()) {
				if (!PublicFieldInfo.isCompatibleWith(javaField)) {
					continue;
				}
				fields.add(new PublicFieldInfo(reflectionUI, javaField, javaType));
			}
			for (Method javaMethod : javaType.getMethods()) {
				if (!GetterFieldInfo.isCompatibleWith(javaMethod, javaType)) {
					continue;
				}
				GetterFieldInfo getterFieldInfo = new GetterFieldInfo(reflectionUI, javaMethod, javaType);
				if (ReflectionUIUtils.findInfoByName(fields, getterFieldInfo.getName()) != null) {
					continue;
				}
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
			for (Method javaMethod : javaType.getMethods()) {
				if (!DefaultMethodInfo.isCompatibleWith(javaMethod, javaType)) {
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
		if (javaType.isPrimitive()) {
			return ClassUtils.primitiveToWrapperType(javaType).isInstance(object);
		} else {
			return javaType.isInstance(object);
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
		} else {
			String result = object.toString();
			if (result == null) {
				result = "";
			}
			result = result.replaceAll(
					javaType.getName().replace(".", "\\.").replace("$", "\\$").replace("[", "\\[") + "@([0-9a-z]+)",
					getCaption() + " ($1)");
			result = result.replace(javaType.getName(), getCaption());
			return result;
		}
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
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object copy = ois.readObject();
			return copy;
		} catch (Throwable t) {
			throw new ReflectionUIError("Could not copy object: " + t.toString());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((constructors == null) ? 0 : constructors.hashCode());
		result = prime * result + ((javaType == null) ? 0 : javaType.hashCode());
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
		if (javaType == null) {
			if (other.javaType != null)
				return false;
		} else if (!javaType.equals(other.javaType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultTypeInfo [javaType=" + javaType + "]";
	}

}
