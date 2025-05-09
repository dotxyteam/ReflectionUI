
package xy.reflect.ui.info.type.iterable.map;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Type information that should be associated with {@link StandardMapEntry}
 * instances.
 * 
 * @author olitank
 *
 */
public class StandardMapEntryTypeInfo extends DefaultTypeInfo implements IMapEntryTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	protected GetterFieldInfo keyField;
	protected GetterFieldInfo valueField;

	public StandardMapEntryTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source, Class<?> keyJavaType,
			Class<?> valueJavaType) {
		super(reflectionUI, source);
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
	}

	public Class<?> getKeyJavaType() {
		return keyJavaType;
	}

	public Class<?> getValueJavaType() {
		return valueJavaType;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		return javaType.equals(StandardMapEntry.class);
	}

	@Override
	public String getCaption() {
		return "Entry";
	}

	@Override
	public boolean supports(Object object) {
		if (object == null) {
			return false;
		}
		if (!(object instanceof StandardMapEntry)) {
			return false;
		}
		StandardMapEntry entry = (StandardMapEntry) object;
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
		return true;
	}

	@Override
	public boolean canCopy(Object object) {
		return getKeyField().getType().canCopy(getKeyField().getValue(object))
				&& getValueField().getType().canCopy(getValueField().getValue(object));
	}

	@Override
	public Object copy(Object object) {
		Object keyCopy = getKeyField().getType().copy(getKeyField().getValue(object));
		Object valueCopy = getValueField().getType().copy(getValueField().getValue(object));
		return new StandardMapEntry(keyCopy, valueCopy, new Class[] { keyJavaType, valueJavaType });
	}

	@Override
	public GetterFieldInfo getKeyField() {
		if (keyField == null) {
			try {
				keyField = new GetterFieldInfo(reflectionUI,
						StandardMapEntry.class.getMethod("getKey", new Class<?>[0]), StandardMapEntry.class) {
					@Override
					public ITypeInfo getType() {
						SpecificitiesIdentifier specificitiesIdentifier = new SpecificitiesIdentifier(
								StandardMapEntryTypeInfo.this.getName(), ((IFieldInfo) this).getName());
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
								(keyJavaType == null) ? Object.class : keyJavaType, specificitiesIdentifier));
					}

					@Override
					public String getName() {
						return ((keyJavaType == null) ? Object.class : keyJavaType).getName() + "Key";
					}

					@Override
					public String getCaption() {
						return "Key";
					}

				};
			} catch (SecurityException e) {
				throw new ReflectionUIError(e);
			} catch (NoSuchMethodException e) {
				throw new ReflectionUIError(e);
			}
		}
		return keyField;
	}

	@Override
	public GetterFieldInfo getValueField() {
		if (valueField == null) {
			try {
				valueField = new GetterFieldInfo(reflectionUI,
						StandardMapEntry.class.getMethod("getValue", new Class<?>[0]), StandardMapEntry.class) {

					@Override
					public ITypeInfo getType() {
						SpecificitiesIdentifier specificitiesIdentifier = new SpecificitiesIdentifier(
								StandardMapEntryTypeInfo.this.getName(), ((IFieldInfo) this).getName());
						return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
								(valueJavaType == null) ? Object.class : valueJavaType, specificitiesIdentifier));
					}

					@Override
					public String getName() {
						return ((valueJavaType == null) ? Object.class : valueJavaType).getName() + "Value";
					}

					@Override
					public String getCaption() {
						return "Value";
					}
				};
			} catch (SecurityException e) {
				throw new ReflectionUIError(e);
			} catch (NoSuchMethodException e) {
				throw new ReflectionUIError(e);
			}
		}
		return valueField;
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
		return Collections.singletonList(new StandardMapEntryConstructorInfo());
	}

	protected Constructor<?> getStandardMapEntryJavaConstructor() {
		try {
			return StandardMapEntry.class.getConstructor(Object.class, Object.class, Class[].class);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public String toString(Object object) {
		Object key = getKeyField().getValue(object);
		Object value = getValueField().getValue(object);
		return ReflectionUIUtils.toString(reflectionUI, key) + ": " + ReflectionUIUtils.toString(reflectionUI, value);
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

	protected class StandardMapEntryConstructorInfo extends DefaultConstructorInfo {

		public StandardMapEntryConstructorInfo() {
			super(StandardMapEntryTypeInfo.this.reflectionUI, getStandardMapEntryJavaConstructor(),
					StandardMapEntry.class);
		}

		@Override
		public String getSignature() {
			return ReflectionUIUtils.buildMethodSignature(this);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return StandardMapEntryTypeInfo.this;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			List<IParameterInfo> result = new ArrayList<IParameterInfo>();
			for (IParameterInfo param : super.getParameters()) {
				if (param.getPosition() == 2) {
					continue;
				}
				result.add(new ParameterInfoProxy(param) {

					GetterFieldInfo relatedField;
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
						if (getPosition() == 0) {
							return "key";
						} else if (getPosition() == 1) {
							return "value";
						} else {
							throw new ReflectionUIError();
						}
					}

					@Override
					public String getCaption() {
						return relatedField.getCaption();
					}

					@Override
					public ITypeInfo getType() {
						if (getPosition() == 0) {
							return reflectionUI.getTypeInfo(
									new JavaTypeInfoSource((keyJavaType != null) ? keyJavaType : Object.class, null));
						} else if (getPosition() == 1) {
							return reflectionUI.getTypeInfo(new JavaTypeInfoSource(
									(valueJavaType != null) ? valueJavaType : Object.class, null));
						} else {
							throw new ReflectionUIError();
						}
					}

					@Override
					public boolean isNullValueDistinct() {
						return relatedField.isNullValueDistinct();
					}

					@Override
					public Object getDefaultValue(Object ignore) {
						if (ReflectionUIUtils.canCreateDefaultInstance(getType(), true)) {
							return ReflectionUIUtils.createDefaultInstance(getType(), true);
						}
						return super.getDefaultValue(ignore);
					}

				});
			}
			return result;
		}

		@Override
		public Object invoke(Object ignore, InvocationData invocationData) {
			invocationData.getProvidedParameterValues().put(2, new Class[] { keyJavaType, valueJavaType });
			return super.invoke(ignore, invocationData);
		}

		@Override
		public String toString() {
			return "StandardMapEntryConstructorInfo [keyJavaType=" + keyJavaType + ", valueJavaType=" + valueJavaType
					+ "]";
		}

	}

}
