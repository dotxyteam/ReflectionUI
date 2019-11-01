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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIError;

public class MultipleFieldsAsListFieldInfo extends AbstractInfo implements IFieldInfo {

	protected List<IFieldInfo> fields;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public MultipleFieldsAsListFieldInfo(ReflectionUI reflectionUI, List<IFieldInfo> fields, ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.fields = fields;
		this.containingType = containingType;
	}

	public String getItemTitle(IFieldInfo field) {
		return field.getCaption();
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new ValueListTypeInfo().getSource());
		}
		return type;
	}

	@Override
	public String getCaption() {
		StringBuilder result = new StringBuilder(MultipleFieldsAsListFieldInfo.class.getSimpleName());
		result.append("List Containing ");
		int i = 0;
		for (IFieldInfo field : fields) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(field.getCaption());
			i++;
		}
		return result.toString();
	}

	@Override
	public Object getValue(Object object) {
		List<ValueListItem> result = new ArrayList<ValueListItem>();
		for (IFieldInfo field : fields) {
			ValueListItem listItem = getListItem(object, field);
			reflectionUI.registerPrecomputedTypeInfoObject(listItem, getListItemTypeInfo(field));
			result.add(listItem);
		}
		return result;
	}

	protected ValueListItem getListItem(Object object, IFieldInfo listFieldInfo) {
		return new ValueListItem(object, listFieldInfo);
	}

	protected ITypeInfo getListItemTypeInfo(IFieldInfo field) {
		return new ValueListItemTypeInfo(field);
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
	public void setValue(Object object, Object value) {
		throw new ReflectionUIError();
	}

	@Override
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public boolean isTransient() {
		return true;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
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
	public String getName() {
		StringBuilder result = new StringBuilder("listFrom");
		int i = 0;
		for (IFieldInfo field : fields) {
			if (i > 0) {
				result.append("And");
			}
			String fieldName = field.getName();
			if (fieldName.length() > 0) {
				fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			}
			result.append(fieldName);
			i++;
		}
		return result.toString();
	}

	@Override
	public boolean isHidden() {
		return false;
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
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
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
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		return fields.hashCode();
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
		return fields.equals(((MultipleFieldsAsListFieldInfo) obj).fields);
	}

	@Override
	public String toString() {
		return "MultipleFieldAsListField [fields=" + fields + "]";
	}

	public class ValueListItem {

		protected Object object;
		protected IFieldInfo field;

		public ValueListItem(Object object, IFieldInfo field) {
			this.object = object;
			this.field = field;
		}

		public Object getObject() {
			return object;
		}

		public IFieldInfo getField() {
			return field;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((object == null) ? 0 : object.hashCode());
			result = prime * result + ((field == null) ? 0 : field.hashCode());
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
			ValueListItem other = (ValueListItem) obj;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getItemTitle(field);
		}

	}

	public class ValueListTypeInfo extends StandardCollectionTypeInfo {

		public ValueListTypeInfo() {
			super(MultipleFieldsAsListFieldInfo.this.reflectionUI, new JavaTypeInfoSource(ArrayList.class, null),
					MultipleFieldsAsListFieldInfo.this.reflectionUI
							.getTypeInfo(new JavaTypeInfoSource(ValueListItem.class, null)));
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this, new SpecificitiesIdentifier(containingType.getName(),
					MultipleFieldsAsListFieldInfo.this.getName()));
		}

		@Override
		public boolean isInsertionAllowed() {
			return false;
		}

		@Override
		public boolean isRemovalAllowed() {
			return false;
		}

		@Override
		public boolean isOrdered() {
			return false;
		}

		@Override
		public boolean canViewItemDetails() {
			return false;
		}
	}

	public class ValueListItemTypeInfo extends AbstractInfo implements ITypeInfo {

		protected IFieldInfo field;

		public ValueListItemTypeInfo(IFieldInfo field) {
			this.field = field;
		}

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this, null);
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
		public ColorSpecification getCategoriesBackgroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getCategoriesForegroundColor() {
			return null;
		}

		@Override
		public Dimension getFormPreferredSize() {
			return null;
		}

		@Override
		public boolean canPersist() {
			return false;
		}

		@Override
		public boolean onFormVisibilityChange(Object object, boolean visible) {
			return false;
		}

		@Override
		public void save(Object object, OutputStream out) {
		}

		@Override
		public void load(Object object, InputStream in) {
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

		@Override
		public String getCaption() {
			return getItemTitle(field);
		}

		public IFieldInfo getDetailsField() {
			return new ValueListItemDetailsFieldInfo(field);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.<IFieldInfo>singletonList(getDetailsField());
		}

		@Override
		public String getName() {
			return "FieldAsListItemTypeInfo [index=" + MultipleFieldsAsListFieldInfo.this.fields.indexOf(field)
					+ ", containingList=" + MultipleFieldsAsListFieldInfo.this.getName() + "]";
		}

		@Override
		public String getOnlineHelp() {
			return field.getOnlineHelp();
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return field.getSpecificProperties();
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof ValueListItem;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public String toString(Object object) {
			return getItemTitle(field);
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
			throw new ReflectionUIError();
		}

		@Override
		public boolean isModificationStackAccessible() {
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((field == null) ? 0 : field.hashCode());
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
			ValueListItemTypeInfo other = (ValueListItemTypeInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			return true;
		}

		private MultipleFieldsAsListFieldInfo getOuterType() {
			return MultipleFieldsAsListFieldInfo.this;
		}

		@Override
		public String toString() {
			return "FieldAsListItemTypeInfo [index=" + MultipleFieldsAsListFieldInfo.this.fields.indexOf(field)
					+ ", containingList=" + getOuterType() + "]";
		}

	}

	public class ValueListItemDetailsFieldInfo extends FieldInfoProxy {

		public ValueListItemDetailsFieldInfo(IFieldInfo field) {
			super(field);
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.getTypeInfo(new TypeInfoSourceProxy(super.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(
							new ValueListItemTypeInfo(ValueListItemDetailsFieldInfo.this.base).getName(),
							ValueListItemDetailsFieldInfo.this.getName());
				}
			});
		}

		@Override
		public String getCaption() {
			return getItemTitle(base);
		}

		@Override
		public Object getValue(Object object) {
			object = ((ValueListItem) object).getObject();
			return super.getValue(object);
		}

		@Override
		public void setValue(Object object, Object value) {
			object = ((ValueListItem) object).getObject();
			super.setValue(object, value);
		}

		@Override
		public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
			object = ((ValueListItem) object).getObject();
			return super.getNextUpdateCustomUndoJob(object, value);
		}

		@Override
		public Object[] getValueOptions(Object object) {
			object = ((ValueListItem) object).getObject();
			return super.getValueOptions(object);
		}

		@Override
		public boolean isHidden() {
			return false;
		}

	}

}
