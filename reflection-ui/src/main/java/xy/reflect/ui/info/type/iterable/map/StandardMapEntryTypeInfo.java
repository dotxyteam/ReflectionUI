package xy.reflect.ui.info.type.iterable.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;

public class StandardMapEntryTypeInfo extends DefaultTypeInfo implements IMapEntryTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapEntryTypeInfo(ReflectionUI reflectionUI, Class<?> keyJavaType, Class<?> valueJavaType) {
		super(reflectionUI, new JavaTypeInfoSource(StandardMapEntry.class, null));
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
	public boolean supportsInstance(Object object) {
		if (!super.supportsInstance(object)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		StandardMapEntry entry = (StandardMapEntry) object;
		if (entry != null) {
			Object key = entry.getKey();
			if (key != null) {
				if (keyJavaType != null) {
					if (!keyJavaType.isInstance(key)) {
						return false;
					}
				}
			}
			Object value = entry.getValue();
			if (value != null) {
				if (valueJavaType != null) {
					if (!valueJavaType.isInstance(value)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public IFieldInfo getKeyField() {
		try {
			return new GetterFieldInfo(reflectionUI, StandardMapEntry.class.getMethod("getKey", new Class<?>[0]),
					StandardMapEntry.class) {
				@Override
				public ITypeInfo getType() {
					if (keyJavaType == null) {
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class, null));
					}
					return reflectionUI.getTypeInfo(new JavaTypeInfoSource(keyJavaType, null));
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
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class, null));
					}
					return reflectionUI.getTypeInfo(new JavaTypeInfoSource(valueJavaType, null));
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
	public List<IMethodInfo> getMethods() {
		return Collections.emptyList();
	}

	@Override
	public List<IMethodInfo> getConstructors() {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		try {
			result.add(new DefaultConstructorInfo(reflectionUI,
					StandardMapEntry.class.getConstructor(Object.class, Object.class)) {

				@Override
				public ITypeInfo getReturnValueType() {
					return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(StandardMapEntryTypeInfo.this, null));
				}

				@Override
				public List<IParameterInfo> getParameters() {
					List<IParameterInfo> result = new ArrayList<IParameterInfo>();
					for (IParameterInfo param : super.getParameters()) {
						result.add(new ParameterInfoProxy(param) {

							IFieldInfo relatedField;
							{
								if (getPosition() == 0) {
									relatedField = getKeyField();
								} else if (getPosition() == 1) {
									relatedField = getValueField();
								} else {
									throw new ReflectionUIError();
								}
							}

							@Override
							public String getName() {
								return relatedField.getName();
							}

							@Override
							public String getCaption() {
								return relatedField.getCaption();
							}

							@Override
							public ITypeInfo getType() {
								return relatedField.getType();
							}

							@Override
							public boolean isNullValueDistinct() {
								return relatedField.isNullValueDistinct();
							}

						});
					}
					return result;
				}

			});
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
		return result;
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

	@Override
	public String toString() {
		return "StandardMapEntryTypeInfo [keyJavaType=" + keyJavaType + ", valueJavaType=" + valueJavaType + "]";
	}

}
