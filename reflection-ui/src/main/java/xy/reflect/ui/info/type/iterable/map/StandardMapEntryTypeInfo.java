package xy.reflect.ui.info.type.iterable.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.info.method.InvocationData;

public class StandardMapEntryTypeInfo extends DefaultTypeInfo implements IMapEntryTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapEntryTypeInfo(ReflectionUI reflectionUI, Class<?> keyJavaType, Class<?> valueJavaType) {
		super(reflectionUI, StandardMapEntry.class);
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		return javaType.equals(StandardMapEntry.class);
	}

	@Override
	public String getName() {
		String keyTypeName = (keyJavaType == null) ? Object.class.getName() : keyJavaType.getName();
		String valueTypeName = (valueJavaType == null) ? Object.class.getName() : valueJavaType.getName();
		return StandardMapEntry.class.getName() + "<" + keyTypeName + "," + valueTypeName + ">";
	}

	@Override
	public String getCaption() {
		return "Entry";
	}

	@Override
	public IFieldInfo getKeyField() {
		try {
			return new GetterFieldInfo(reflectionUI, StandardMapEntry.class.getMethod("getKey", new Class<?>[0]),
					StandardMapEntry.class) {
				@Override
				public ITypeInfo getType() {
					if (keyJavaType == null) {
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class));
					}
					return reflectionUI.getTypeInfo(new JavaTypeInfoSource(keyJavaType));
				}
			};
		} catch (SecurityException e) {
			throw new ReflectionUIError(e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public IFieldInfo getValueField() {
		try {
			return new GetterFieldInfo(reflectionUI, StandardMapEntry.class.getMethod("getValue", new Class<?>[0]),
					StandardMapEntry.class) {
				@Override
				public ITypeInfo getType() {
					if (valueJavaType == null) {
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class));
					}
					return reflectionUI.getTypeInfo(new JavaTypeInfoSource(valueJavaType));
				}
			};
		} catch (SecurityException e) {
			throw new ReflectionUIError(e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public List<IFieldInfo> getFields() {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		result.add(getKeyField());
		result.add(getValueField());
		return result;
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		return Collections
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(StandardMapEntryTypeInfo.this) {

					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						StandardMapEntry<?, ?> result;
						try {
							result = (StandardMapEntry<?, ?>) javaType.getConstructors()[0].newInstance(null, null);
						} catch (Exception e) {
							throw new ReflectionUIError();
						}
						Object key = null;
						{
							IFieldInfo keyField = getKeyField();
							try {
								key = ReflectionUIUtils.onTypeInstanciationRequest(reflectionUI, keyField.getType());
							} catch (Throwable ignore) {
							}
							if (key != null) {
								keyField.setValue(result, key);
							}
						}
						Object value = null;
						{
							IFieldInfo valueField = getValueField();
							try {
								value = ReflectionUIUtils.onTypeInstanciationRequest(reflectionUI,
										valueField.getType());
							} catch (Throwable ignore) {
							}
							if (value != null) {
								valueField.setValue(result, value);
							}
						}
						reflectionUI.registerPrecomputedTypeInfoObject(result, StandardMapEntryTypeInfo.this);
						return result;
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
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
		StandardMapEntryTypeInfo other = (StandardMapEntryTypeInfo) obj;
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

}
