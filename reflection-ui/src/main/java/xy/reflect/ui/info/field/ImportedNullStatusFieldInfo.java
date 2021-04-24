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
package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field proxy allowing to make the base field seem nullable. The null status of
 * the field is actually given by another boolean field.
 * 
 * @author olitank
 *
 */
public class ImportedNullStatusFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo nullStatusField;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public ImportedNullStatusFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, IFieldInfo nullStatusField,
			ITypeInfo containingType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.nullStatusField = nullStatusField;
		this.containingType = containingType;
	}

	@Override
	public boolean isGetOnly() {
		return super.isGetOnly() && nullStatusField.isGetOnly();
	}

	@Override
	public Object getValue(Object object) {
		if (getNullStatus(object)) {
			return super.getValue(object);
		} else {
			return null;
		}
	}

	protected boolean getNullStatus(Object object) {
		Object nullStatus = nullStatusField.getValue(object);
		if (!(nullStatus instanceof Boolean)) {
			throw new ReflectionUIError("Invalid null status field value (boolean expected): '" + nullStatus + "'");
		}
		return (Boolean) nullStatus;
	}

	@Override
	public void setValue(Object object, Object value) {
		if (value == null) {
			nullStatusField.setValue(object, Boolean.FALSE);
		} else {
			nullStatusField.setValue(object, Boolean.TRUE);
			if (!super.isGetOnly()) {
				super.setValue(object, value);
			}
		}
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(final Object object, final Object newValue) {
		if (newValue == null) {
			return nullStatusField.getNextUpdateCustomUndoJob(object, Boolean.FALSE);
		} else {
			Runnable job1 = nullStatusField.getNextUpdateCustomUndoJob(object, Boolean.TRUE);
			Runnable job2 = super.getNextUpdateCustomUndoJob(object, newValue);

			if (job1 == null) {
				job1 = ReflectionUIUtils.createDefaultUndoJob(object, nullStatusField);
			}
			if (job2 == null) {
				job2 = ReflectionUIUtils.createDefaultUndoJob(object, this);
			}

			final Runnable finalJob1 = job1;
			final Runnable finalJob2 = job2;

			return new Runnable() {
				@Override
				public void run() {
					finalJob1.run();
					finalJob2.run();
				}
			};
		}
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = super.getType();
			type = new InfoProxyFactory() {

				@Override
				protected boolean isConcrete(ITypeInfo type) {
					return true;
				}

				@Override
				protected List<IMethodInfo> getConstructors(final ITypeInfo type) {
					return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

						@Override
						public Object invoke(Object parentObject, InvocationData invocationData) {
							return ImportedNullStatusFieldInfo.super.getValue(parentObject);
						}

						@Override
						public ITypeInfo getReturnValueType() {
							return type;
						}

						@Override
						public List<IParameterInfo> getParameters() {
							return Collections.emptyList();
						}
					});
				}

				@Override
				protected ITypeInfoSource getSource(ITypeInfo type) {
					return new TypeInfoSourceProxy(super.getSource(type)) {
						@Override
						public SpecificitiesIdentifier getSpecificitiesIdentifier() {
							return new SpecificitiesIdentifier(containingType.getName(),
									ImportedNullStatusFieldInfo.this.getName());
						}
					};
				}

				@Override
				public String toString() {
					return "setFakeValueContructor [field=" + ImportedNullStatusFieldInfo.this + "]";
				}

			}.wrapTypeInfo(type);
		}
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
		result = prime * result + ((nullStatusField == null) ? 0 : nullStatusField.hashCode());
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
		ImportedNullStatusFieldInfo other = (ImportedNullStatusFieldInfo) obj;
		if (containingType == null) {
			if (other.containingType != null)
				return false;
		} else if (!containingType.equals(other.containingType))
			return false;
		if (nullStatusField == null) {
			if (other.nullStatusField != null)
				return false;
		} else if (!nullStatusField.equals(other.nullStatusField))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImportedNullStatusFieldInfo [base=" + base + ", nullStatusField=" + nullStatusField + "]";
	}

}
