package xy.reflect.ui.info.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PublicFieldInfo implements IFieldInfo {

	protected Field javaField;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo type;
	protected Class<?> containingJavaClass;

	public PublicFieldInfo(ReflectionUI reflectionUI, Field field, Class<?> containingJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaField = field;
		this.containingJavaClass = containingJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		javaField.setAccessible(true);
	}

	public Field getJavaField() {
		return javaField;
	}

	@Override
	public void setValue(Object object, Object value) {
		try {
			javaField.set(object, value);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		try {
			return javaField.get(object);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaField.getType(), javaField));
		}
		return type;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.identifierToCaption(javaField.getName());
	}

	@Override
	public boolean isNullable() {
		return getType().isPassedByReference();
	}

	@Override
	public boolean isGetOnly() {
		return Modifier.isFinal(javaField.getModifiers());
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		if (!getType().isPassedByReference()) {
			return ValueReturnMode.COPY;
		} else {
			return ValueReturnMode.SELF_OR_PROXY;
		}
	}

	@Override
	public String toString() {
		return "PublicFieldInfo [javaField=" + javaField + "]";
	}

	@Override
	public String getName() {
		return javaField.getName();
	}

	@Override
	public int hashCode() {
		return javaField.hashCode();
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
		return javaField.equals(((PublicFieldInfo) obj).javaField);
	}

	public static boolean isCompatibleWith(Field field) {
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
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

};