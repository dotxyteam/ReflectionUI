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

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual field grouping the given members in a virtual sub-object.
 * 
 * @author olitank
 *
 */
public class CapsuleFieldInfo extends AbstractInfo implements IFieldInfo {

	public static final String CONTAINING_CAPSULE_FIELD_PROPERTY_KEY = CapsuleFieldInfo.class.getName();

	protected List<IFieldInfo> encapsulatedFields;
	protected List<IMethodInfo> encapsulatedMethods;
	protected ReflectionUI reflectionUI;
	protected String fieldName;
	protected ITypeInfo containingType;

	public CapsuleFieldInfo(ReflectionUI reflectionUI, String fieldName, List<IFieldInfo> encapsulatedFields,
			List<IMethodInfo> encapsulatedMethods, ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.fieldName = fieldName;
		this.encapsulatedFields = encapsulatedFields;
		this.encapsulatedMethods = encapsulatedMethods;
		this.containingType = containingType;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	public static String formatTypeName(String fieldName, String containingTypeName) {
		return MessageFormat.format(
				"CapsuleFieldType [context=EncapsulationContext [containingType={0}], fieldName={1}]",
				containingTypeName, fieldName);
	}

	public static String extractContainingTypeName(String typeName) {
		Pattern p = Pattern.compile(
				"CapsuleFieldType \\[context=EncapsulationContext \\[containingType=(.+)\\], fieldName=(.+)\\]");
		Matcher m = p.matcher(typeName);
		if (!m.matches()) {
			return null;
		}
		return m.group(1);
	}

	public static String extractFieldName(String typeName) {
		Pattern p = Pattern.compile(
				"CapsuleFieldType \\[context=EncapsulationContext \\[containingType=(.+)\\], fieldName=(.+)\\]");
		Matcher m = p.matcher(typeName);
		if (!m.matches()) {
			return null;
		}
		return m.group(2);
	}

	public IFieldInfo createEncapsulatedFieldInfoProxy(IFieldInfo field) {
		return new EncapsulatedFieldInfoProxy(field);
	}

	public IMethodInfo createEncapsulatedMethodInfoProxy(IMethodInfo method) {
		return new EncapsulatedMethodInfoProxy(method);
	}

	public ITypeInfo getContainingType() {
		return containingType;
	}

	public List<IFieldInfo> getEncapsulatedFields() {
		return encapsulatedFields;
	}

	public List<IMethodInfo> getEncapsulatedMethods() {
		return encapsulatedMethods;
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 1.0;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new ValueTypeInfo().getSource());
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
	}

	@Override
	public Object getValue(Object object) {
		Value result = new Value(object);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new ValueTypeInfo());
		return result;
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public String getNullValueLabel() {
		return null;
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
	public boolean isFormControlMandatory() {
		return true;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((encapsulatedFields == null) ? 0 : encapsulatedFields.hashCode());
		result = prime * result + ((encapsulatedMethods == null) ? 0 : encapsulatedMethods.hashCode());
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
		CapsuleFieldInfo other = (CapsuleFieldInfo) obj;
		if (containingType == null) {
			if (other.containingType != null)
				return false;
		} else if (!containingType.equals(other.containingType))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (encapsulatedFields == null) {
			if (other.encapsulatedFields != null)
				return false;
		} else if (!encapsulatedFields.equals(other.encapsulatedFields))
			return false;
		if (encapsulatedMethods == null) {
			if (other.encapsulatedMethods != null)
				return false;
		} else if (!encapsulatedMethods.equals(other.encapsulatedMethods))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CapsuleField [fieldName=" + fieldName + ", containingType=" + containingType + ", fields="
				+ encapsulatedFields + ", methods=" + encapsulatedMethods + "]";
	}

	public class Value {

		protected Object object;

		public Value(Object object) {
			super();
			this.object = object;
		}

		public Object getObject() {
			return object;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((object == null) ? 0 : object.hashCode());
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
			Value other = (Value) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		private CapsuleFieldInfo getOuterType() {
			return CapsuleFieldInfo.this;
		}

		@Override
		public String toString() {
			return "CapsuleFieldFieldInstance [object=" + object + "]";
		}

	}

	public class ValueTypeInfo extends AbstractInfo implements ITypeInfo {

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this,
					new SpecificitiesIdentifier(containingType.getName(), fieldName));
		}

		@Override
		public CategoriesStyle getCategoriesStyle() {
			return CategoriesStyle.getDefault();
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
		public ColorSpecification getFormBorderColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorsForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorsBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonForegroundColor() {
			return null;
		}

		@Override
		public ResourcePath getFormButtonBackgroundImagePath() {
			return null;
		}

		@Override
		public ColorSpecification getFormButtonBorderColor() {
			return null;
		}

		@Override
		public Dimension getFormPreferredSize() {
			return null;
		}

		@Override
		public ColorSpecification getCategoriesBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getCategoriesForegroundColor() {
			return null;
		}

		@Override
		public boolean onFormVisibilityChange(Object object, boolean visible) {
			return false;
		}

		@Override
		public boolean canPersist() {
			return false;
		}

		@Override
		public void save(Object object, OutputStream out) {
		}

		@Override
		public void load(Object object, InputStream in) {
		}

		@Override
		public String getName() {
			return formatTypeName(fieldName, containingType.getName());
		}

		@Override
		public String getCaption() {
			return CapsuleFieldInfo.this.getCaption();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isConcrete() {
			return false;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IFieldInfo field : CapsuleFieldInfo.this.encapsulatedFields) {
				result.add(createEncapsulatedFieldInfoProxy(field));
			}
			return result;
		}

		@Override
		public List<IMethodInfo> getMethods() {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo method : CapsuleFieldInfo.this.encapsulatedMethods) {
				result.add(createEncapsulatedMethodInfoProxy(method));
			}
			return result;
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof Value;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public String toString(Object object) {
			Value value = (Value) object;
			ITypeInfo valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
			StringBuilder result = new StringBuilder();
			for (IFieldInfo field : valueType.getFields()) {
				try {
					Object fieldValue = field.getValue(value);
					if (fieldValue == null) {
						continue;
					}
					String fieldValueString = ReflectionUIUtils.toString(reflectionUI, fieldValue);
					if (fieldValueString.length() == 0) {
						continue;
					}
					for (String newLine : ReflectionUIUtils.NEW_LINE_SEQUENCES) {
						fieldValueString = fieldValueString.replace(newLine, " ");
					}
					fieldValueString = ReflectionUIUtils.truncateNicely(fieldValueString, 20);
					String fieldName = field.getName();
					if ((fieldName != null) && (fieldName.length() > 0)) {
						fieldValueString = fieldName + "=" + fieldValueString;
					}
					if (result.length() > 0) {
						result.append(", ");
					}
					result.append(fieldValueString);
				} catch (Throwable t) {
					continue;
				}
			}
			return result.toString();
		}

		@Override
		public void validate(Object object) throws Exception {
		}

		@Override
		public boolean canCopy(Object object) {
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isModificationStackAccessible() {
			return true;
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
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

		protected CapsuleFieldInfo getOuterType() {
			return CapsuleFieldInfo.this;
		}

		@Override
		public int hashCode() {
			return getOuterType().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!getOuterType().equals(((ValueTypeInfo) obj).getOuterType())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "ValueTypeInfo [of=" + getOuterType() + "]";
		}

	}

	public class EncapsulatedFieldInfoProxy extends FieldInfoProxy {

		protected ITypeInfo type;

		public EncapsulatedFieldInfoProxy(IFieldInfo base) {
			super(base);
		}

		public CapsuleFieldInfo getOuterType() {
			return CapsuleFieldInfo.this;
		}

		@Override
		public ITypeInfo getType() {
			if (type == null) {
				type = reflectionUI.getTypeInfo(new TypeInfoSourceProxy(super.getType().getSource()) {

					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return new SpecificitiesIdentifier(CapsuleFieldInfo.this.getType().getName(),
								EncapsulatedFieldInfoProxy.this.getName());
					}

				});
			}
			return type;
		}

		@Override
		public Object getValue(Object object) {
			object = ((Value) object).getObject();
			return super.getValue(object);
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			object = ((Value) object).getObject();
			return super.getNextUpdateCustomUndoJob(object, value);
		}

		@Override
		public boolean hasValueOptions(Object object) {
			object = ((Value) object).getObject();
			return super.hasValueOptions(object);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			object = ((Value) object).getObject();
			return super.getValueOptions(object);
		}

		@Override
		public void setValue(Object object, Object value) {
			object = ((Value) object).getObject();
			super.setValue(object, value);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
			result.put(CONTAINING_CAPSULE_FIELD_PROPERTY_KEY, CapsuleFieldInfo.this);
			return result;
		}

	}

	public class EncapsulatedMethodInfoProxy extends MethodInfoProxy {

		protected boolean returnValueVoid = false;
		protected ITypeInfo returnValueType;

		public EncapsulatedMethodInfoProxy(IMethodInfo base) {
			super(base);
		}

		public CapsuleFieldInfo getOuterType() {
			return CapsuleFieldInfo.this;
		}

		@Override
		public ITypeInfo getReturnValueType() {
			if (returnValueVoid) {
				return null;
			}
			if (returnValueType == null) {
				if (super.getReturnValueType() == null) {
					returnValueVoid = true;
				} else {
					returnValueType = reflectionUI
							.getTypeInfo(new TypeInfoSourceProxy(super.getReturnValueType().getSource()) {
								@Override
								public SpecificitiesIdentifier getSpecificitiesIdentifier() {
									return null;
								}
							});
				}
			}
			return returnValueType;
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			object = ((Value) object).getObject();
			return super.invoke(object, invocationData);
		}

		@Override
		public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
			object = ((Value) object).getObject();
			return super.getNextInvocationUndoJob(object, invocationData);
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
			object = ((Value) object).getObject();
			super.validateParameters(object, invocationData);
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
			result.put(CONTAINING_CAPSULE_FIELD_PROPERTY_KEY, CapsuleFieldInfo.this);
			return result;
		}

	}

}
