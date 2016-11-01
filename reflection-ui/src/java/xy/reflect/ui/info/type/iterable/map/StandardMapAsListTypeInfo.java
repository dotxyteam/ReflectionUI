package xy.reflect.ui.info.type.iterable.map;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.DefaultConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class StandardMapAsListTypeInfo extends StandardCollectionTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapAsListTypeInfo(ReflectionUI reflectionUI, Class<?> javaType, Class<?> keyJavaType,
			Class<?> valueJavaType) {
		super(reflectionUI, javaType, StandardMapEntry.class);
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
	}

	public Class<?> getKeyJavaType() {
		return keyJavaType;
	}

	public Class<?> getValueJavaType() {
		return valueJavaType;
	}

	@Override
	public ITypeInfo getItemType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(StandardMapEntry.class, keyJavaType, valueJavaType));
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		List<IMethodInfo> defaultConstructors = new ArrayList<IMethodInfo>();
		if (isConcrete()) {
			for (Constructor<?> javaConstructor : javaType.getConstructors()) {
				if (!DefaultConstructorMethodInfo.isCompatibleWith(javaConstructor)) {
					continue;
				}
				defaultConstructors.add(new DefaultConstructorMethodInfo(reflectionUI, this, javaConstructor));
			}
		}
		if (ReflectionUIUtils.getNParametersMethod(defaultConstructors, 0) != null) {
			return defaultConstructors;
		} else {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>(defaultConstructors);
			result.add(new AbstractConstructorMethodInfo(this) {

				@Override
				public Object invoke(Object object, InvocationData invocationData) {
					return new HashMap<Object, Object>();
				}

				@Override
				public List<IParameterInfo> getParameters() {
					return Collections.emptyList();
				}
			});
			return result;
		}
	}

	@Override
	public boolean canReplaceContent() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void replaceContent(Object listValue, Object[] array) {
		Map map = (Map) listValue;
		map.clear();
		for (Object item : array) {
			StandardMapEntry standardMapEntry = (StandardMapEntry) item;
			map.put(standardMapEntry.getKey(), standardMapEntry.getValue());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromArray(Object[] array) {
		IMethodInfo constructor = ReflectionUIUtils.getZeroParameterConstrucor(this);
		Map result = (Map) constructor.invoke(null, new InvocationData());
		for (Object item : array) {
			StandardMapEntry entry = (StandardMapEntry) item;
			if (result.containsKey(entry.getKey())) {
				throw new ReflectionUIError(
						"Duplicate key: '" + ReflectionUIUtils.toString(reflectionUI, entry.getKey()) + "'");
			}
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object[] toArray(Object listValue) {
		List<StandardMapEntry> result = new ArrayList<StandardMapEntry>();
		for (Object obj : ((Map) listValue).entrySet()) {
			Map.Entry entry = (Entry) obj;
			StandardMapEntry standardMapEntry = new StandardMapEntry(entry.getKey(), entry.getValue());
			reflectionUI.registerPrecomputedTypeInfoObject(standardMapEntry, getItemType());
			result.add(standardMapEntry);
		}
		return result.toArray();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((keyJavaType == null) ? 0 : keyJavaType.hashCode());
		result = prime * result + ((valueJavaType == null) ? 0 : valueJavaType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardMapAsListTypeInfo other = (StandardMapAsListTypeInfo) obj;
		if (keyJavaType == null) {
			if (other.keyJavaType != null)
				return false;
		} else if (!keyJavaType.equals(other.keyJavaType))
			return false;
		if (valueJavaType == null) {
			if (other.valueJavaType != null)
				return false;
		} else if (!valueJavaType.equals(other.valueJavaType))
			return false;
		return true;
	}

	@Override
	public boolean isOrdered() {
		return false;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Map.class.isAssignableFrom(javaType)) {
			if (ReflectionUIUtils
					.getZeroParameterConstrucor(new DefaultTypeInfo(new ReflectionUI(), javaType)) != null) {
				return true;
			}
			if (javaType.isAssignableFrom(HashMap.class)) {
				return true;
			}
		}
		return false;
	}

}
