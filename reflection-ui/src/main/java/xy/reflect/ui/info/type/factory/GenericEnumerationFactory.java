/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.info.type.factory;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class GenericEnumerationFactory {
	protected ReflectionUI reflectionUI;
	protected Iterable<?> iterable;
	protected String enumerationTypeName;
	protected String typeCaption;
	protected boolean dynamicEnumeration;

	public GenericEnumerationFactory(ReflectionUI reflectionUI, Iterable<?> iterable, String enumerationTypeName,
			String typeCaption, boolean dynamicEnumeration) {
		super();
		this.reflectionUI = reflectionUI;
		this.iterable = iterable;
		this.enumerationTypeName = enumerationTypeName;
		this.typeCaption = typeCaption;
		this.dynamicEnumeration = dynamicEnumeration;
	}

	public GenericEnumerationFactory(ReflectionUI reflectionUI, Object[] array, String enumerationTypeName,
			String typeCaption) {
		this(reflectionUI, Arrays.asList(array), enumerationTypeName, typeCaption, false);
	}

	protected Map<String, Object> getItemSpecificProperties(Object item) {
		return Collections.emptyMap();
	}

	protected String getItemOnlineHelp(Object item) {
		return null;
	}

	protected String getItemName(Object item) {
		return "Item [value=" + item + "]";
	}

	protected String getItemCaption(Object item) {
		return ReflectionUIUtils.toString(reflectionUI, item);
	}

	protected ResourcePath getItemIconImagePath(Object item) {
		return ReflectionUIUtils.getIconImagePath(reflectionUI, item);
	}

	public Object getInstance(Object item) {
		if (item == null) {
			return null;
		}
		Instance result = new Instance(item);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	public Object unwrapInstance(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		if (!instance.getOuterType().equals(this)) {
			throw new ReflectionUIError();
		}
		return instance.getArrayItem();
	}

	public ITypeInfoSource getInstanceTypeInfoSource(SpecificitiesIdentifier specificitiesIdentifier) {
		return new PrecomputedTypeInfoSource(new TypeInfo(), specificitiesIdentifier);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + iterable.hashCode();
		result = prime * result + ((typeCaption == null) ? 0 : typeCaption.hashCode());
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
		GenericEnumerationFactory other = (GenericEnumerationFactory) obj;
		if (!iterable.equals(other.iterable))
			return false;
		if (typeCaption == null) {
			if (other.typeCaption != null)
				return false;
		} else if (!typeCaption.equals(other.typeCaption))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ArrayAsEnumerationFactory [enumerationTypeName=" + enumerationTypeName + ", typeCaption=" + typeCaption
				+ "]";
	}

	public class Instance {
		protected Object item;

		public Instance(Object item) {
			super();
			this.item = item;
		}

		public Object getArrayItem() {
			return item;
		}

		protected GenericEnumerationFactory getOuterType() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((item == null) ? 0 : item.hashCode());
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
			Instance other = (Instance) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GenericEnumerationInstance [item=" + item + "]";
		}

	}

	public class TypeInfo extends AbstractInfo implements IEnumerationTypeInfo {

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(TypeInfo.this, null);
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
		public Dimension getFormPreferredSize() {
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
		public boolean isDynamicEnumeration() {
			return dynamicEnumeration;
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
		public ResourcePath getIconImagePath() {
			return null;
		}

		@Override
		public boolean isPrimitive() {
			return false;
		}

		@Override
		public boolean isImmutable() {
			return true;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getName() {
			return enumerationTypeName;
		}

		@Override
		public String getCaption() {
			return typeCaption;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public boolean isModificationStackAccessible() {
			return false;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.emptyList();
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			if (!iterable.iterator().hasNext()) {
				return Collections.emptyList();
			} else {
				return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

					ITypeInfo returnValueType;

					@Override
					public ITypeInfo getReturnValueType() {
						if (returnValueType == null) {
							returnValueType = reflectionUI.getTypeInfo(TypeInfo.this.getSource());
						}
						return returnValueType;
					}

					@Override
					public Object invoke(Object parentObject, InvocationData invocationData) {
						return getInstance(iterable.iterator().next());
					}

					@Override
					public List<IParameterInfo> getParameters() {
						return Collections.emptyList();
					}
				});
			}
		}

		@Override
		public Object[] getPossibleValues() {
			List<Instance> result = new ArrayList<Instance>();
			for (Object item : iterable) {
				result.add((Instance) getInstance(item));
			}
			return result.toArray();
		}

		@Override
		public IEnumerationItemInfo getValueInfo(final Object object) {
			final Object item = unwrapInstance(object);
			return new ItemInfo(item);
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof Instance;
		}

		@Override
		public void validate(Object object) throws Exception {
			ReflectionUIUtils.checkInstance(this, object);
		}

		@Override
		public boolean canCopy(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return false;
		}

		@Override
		public Object copy(Object object) {
			throw new ReflectionUIError();
		}

		@Override
		public String toString(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return object.toString();
		}

		protected GenericEnumerationFactory getOuterType() {
			return GenericEnumerationFactory.this;
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
			if (!getOuterType().equals(((TypeInfo) obj).getOuterType())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getOuterType() + "]";
		}

	}

	public class ItemInfo implements IEnumerationItemInfo {

		protected Object item;

		public ItemInfo(Object item) {
			this.item = item;
		}

		public Object getItem() {
			return item;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return getItemSpecificProperties(item);
		}

		@Override
		public ResourcePath getIconImagePath() {
			return getItemIconImagePath(item);
		}

		@Override
		public String getOnlineHelp() {
			return getItemOnlineHelp(item);
		}

		@Override
		public String getName() {
			return getItemName(item);
		}

		@Override
		public String getCaption() {
			return getItemCaption(item);
		}

		GenericEnumerationFactory getOuterType() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public String toString() {
			return "ItemInfo [of=" + getOuterType() + ", item=" + item + "]";
		}

	}
}
