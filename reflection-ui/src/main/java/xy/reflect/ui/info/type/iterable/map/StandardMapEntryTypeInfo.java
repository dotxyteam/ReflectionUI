/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.info.type.iterable.map;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
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

	public StandardMapEntryTypeInfo(JavaTypeInfoSource source, Class<?> keyJavaType, Class<?> valueJavaType) {
		super(source);
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
		return new StandardMapEntry(keyCopy, valueCopy);
	}

	@Override
	public IFieldInfo getKeyField() {
		if (keyField == null) {
			try {
				keyField = new GetterFieldInfo(reflectionUI,
						StandardMapEntry.class.getMethod("getKey", new Class<?>[0]), StandardMapEntry.class) {
					ITypeInfo type;

					@Override
					public ITypeInfo getType() {
						if (type == null) {
							SpecificitiesIdentifier specificitiesIdentifier = new SpecificitiesIdentifier(
									StandardMapEntryTypeInfo.this.getName(), ((IFieldInfo) this).getName());
							if (keyJavaType == null) {
								type = reflectionUI.getTypeInfo(
										new JavaTypeInfoSource(reflectionUI, Object.class, specificitiesIdentifier));
							} else {
								type = reflectionUI.getTypeInfo(
										new JavaTypeInfoSource(reflectionUI, keyJavaType, specificitiesIdentifier));
							}
						}
						return type;
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
	public IFieldInfo getValueField() {
		if (valueField == null) {
			try {
				valueField = new GetterFieldInfo(reflectionUI,
						StandardMapEntry.class.getMethod("getValue", new Class<?>[0]), StandardMapEntry.class) {

					ITypeInfo type;

					@Override
					public ITypeInfo getType() {
						if (type == null) {
							SpecificitiesIdentifier specificitiesIdentifier = new SpecificitiesIdentifier(
									StandardMapEntryTypeInfo.this.getName(), ((IFieldInfo) this).getName());
							if (valueJavaType == null) {
								type = reflectionUI.getTypeInfo(
										new JavaTypeInfoSource(reflectionUI, Object.class, specificitiesIdentifier));
							} else {
								type = reflectionUI.getTypeInfo(
										new JavaTypeInfoSource(reflectionUI, valueJavaType, specificitiesIdentifier));
							}
						}
						return type;
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
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		try {
			result.add(new StandardMapEntryConstructorInfo());
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
		return result;
	}

	protected Constructor<?> getStandardMapEntryJavaConstructor() {
		try {
			return StandardMapEntry.class.getConstructor(Object.class, Object.class);
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
			super(StandardMapEntryTypeInfo.this.reflectionUI, getStandardMapEntryJavaConstructor());
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

					ITypeInfo type;

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
						if (type == null) {
							type = reflectionUI
									.getTypeInfo(new TypeInfoSourceProxy(relatedField.getType().getSource()) {
										@Override
										public SpecificitiesIdentifier getSpecificitiesIdentifier() {
											return null;
										}
									});
						}
						return type;
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
		public ITypeInfo getReturnValueType() {
			return reflectionUI
					.getTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(StandardMapEntryTypeInfo.this));
		}

		@Override
		public Object invoke(Object ignore, InvocationData invocationData) {
			StandardMapEntry standardMapEntry = (StandardMapEntry) super.invoke(ignore, invocationData);
			return new PrecomputedTypeInstanceWrapper(standardMapEntry, StandardMapEntryTypeInfo.this);
		}

	}

}
