package xy.reflect.ui.info.type.iterable.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;

public class StandardMapEntryTypeInfo extends DefaultTypeInfo implements
		IMapEntryTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapEntryTypeInfo(ReflectionUI reflectionUI, Class<?> keyJavaType, Class<?> valueJavaType) {
		super(reflectionUI, StandardMapEntry.class);
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
	}

	@Override
	public String getCaption() {
		return "Entry";
	}

	@Override
	public IFieldInfo getKeyField() {
		try {
			return new GetterFieldInfo(
					reflectionUI,
					StandardMapEntry.class.getMethod("getKey", new Class<?>[0]),
					StandardMapEntry.class) {
				@Override
				public ITypeInfo getType() {
					if (keyJavaType == null) {
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
								Object.class));
					}
					return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
							keyJavaType));
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
			return new GetterFieldInfo(reflectionUI,
					StandardMapEntry.class.getMethod("getValue",
							new Class<?>[0]), StandardMapEntry.class) {
				@Override
				public ITypeInfo getType() {
					if (valueJavaType == null) {
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
								Object.class));
					}
					return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
							valueJavaType));
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
				.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
						StandardMapEntryTypeInfo.this) {

					@Override
					public Object invoke(Object object,
							Map<Integer, Object> valueByParameterPosition) {
						StandardMapEntry<?, ?> result;
						try {
							result = (StandardMapEntry<?, ?>) javaType
									.getConstructors()[0].newInstance(null,
									null);
						} catch (Exception e) {
							throw new ReflectionUIError();
						}
						Object key = null;
						{
							IFieldInfo keyField = getKeyField();
							try {
								key = reflectionUI.onTypeInstanciationRequest(
										null, keyField.getType(), true);
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
								value = reflectionUI
										.onTypeInstanciationRequest(null,
												valueField.getType(), true);
							} catch (Throwable ignore) {
							}
							if (value != null) {
								valueField.setValue(result, value);
							}
						}
						return new PrecomputedTypeInfoInstanceWrapper(result,
								StandardMapEntryTypeInfo.this);
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
	}

}
