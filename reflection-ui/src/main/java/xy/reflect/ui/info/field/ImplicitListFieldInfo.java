package xy.reflect.ui.info.field;

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
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ImplicitListFieldInfo extends AbstractInfo implements IFieldInfo {
	protected ReflectionUI reflectionUI;
	protected String fieldName;
	protected IListTypeInfo type;
	protected ITypeInfo itemType;
	protected ITypeInfo parentType;
	protected String createMethodName;
	protected String getMethodName;
	protected String addMethodName;
	protected String removeMethodName;
	protected String sizeMethodName;

	public ImplicitListFieldInfo(ReflectionUI reflectionUI, String fieldName, ITypeInfo parentType, ITypeInfo itemType,
			String createMethodName, String getMethodName, String addMethodName, String removeMethodName,
			String sizeMethodName) {
		this.reflectionUI = reflectionUI;
		this.fieldName = fieldName;
		this.parentType = parentType;
		this.createMethodName = createMethodName;
		this.getMethodName = getMethodName;
		this.addMethodName = addMethodName;
		this.removeMethodName = removeMethodName;
		this.sizeMethodName = sizeMethodName;
		this.itemType = itemType;

	}

	protected IMethodInfo getCreateMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(), createMethodName);
	}

	protected IMethodInfo getGetMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(), getMethodName);
	}

	protected IMethodInfo getAddMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(), addMethodName);
	}

	protected IMethodInfo getRemoveMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(), removeMethodName);
	}

	protected IFieldInfo getSizeField() {
		return ReflectionUIUtils.findInfoByName(parentType.getFields(), sizeMethodName);
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
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
	public IListTypeInfo getType() {
		if (type == null) {
			type = new ValueTypeInfo();
		}
		return type;
	}

	@Override
	public IInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		Object result = new ValueInstance(object);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new ValueTypeInfo());
		return result;
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		ValueInstance implicitListFieldValue = (ValueInstance) value;
		if (!this.equals(implicitListFieldValue.getImplicitListField())) {
			throw new ReflectionUIError();
		}
		Object[] array = getType().toArray(implicitListFieldValue);
		while (true) {
			int size = (Integer) getSizeField().getValue(object);
			if (size == 0) {
				break;
			}
			getRemoveMethod().invoke(object, new InvocationData(0, 0));
		}
		for (int i = 0; i < array.length; i++) {
			Object item = array[i];
			InvocationData invocationData = new InvocationData();
			invocationData.setParameterValue(0, i);
			invocationData.setParameterValue(1, item);
			getAddMethod().invoke(object, invocationData);
		}
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
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
	public boolean isGetOnly() {
		return (getAddMethod() == null) || (getRemoveMethod() == null);
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.combine(ValueReturnMode.DIRECT_OR_PROXY, getGetMethod().getValueReturnMode());
	}

	@Override
	public InfoCategory getCategory() {
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

	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addMethodName == null) ? 0 : addMethodName.hashCode());
		result = prime * result + ((createMethodName == null) ? 0 : createMethodName.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((getMethodName == null) ? 0 : getMethodName.hashCode());
		result = prime * result + ((itemType == null) ? 0 : itemType.hashCode());
		result = prime * result + ((removeMethodName == null) ? 0 : removeMethodName.hashCode());
		result = prime * result + ((sizeMethodName == null) ? 0 : sizeMethodName.hashCode());
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
		ImplicitListFieldInfo other = (ImplicitListFieldInfo) obj;
		if (addMethodName == null) {
			if (other.addMethodName != null)
				return false;
		} else if (!addMethodName.equals(other.addMethodName))
			return false;
		if (createMethodName == null) {
			if (other.createMethodName != null)
				return false;
		} else if (!createMethodName.equals(other.createMethodName))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (getMethodName == null) {
			if (other.getMethodName != null)
				return false;
		} else if (!getMethodName.equals(other.getMethodName))
			return false;
		if (itemType == null) {
			if (other.itemType != null)
				return false;
		} else if (!itemType.equals(other.itemType))
			return false;
		if (removeMethodName == null) {
			if (other.removeMethodName != null)
				return false;
		} else if (!removeMethodName.equals(other.removeMethodName))
			return false;
		if (sizeMethodName == null) {
			if (other.sizeMethodName != null)
				return false;
		} else if (!sizeMethodName.equals(other.sizeMethodName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImplicitListField [fieldName=" + fieldName + ", parentType=" + parentType + "]";
	}

	public class ValueInstance {
		protected Object object;
		protected Object[] array;

		public ValueInstance(Object object) {
			this.object = object;
			array = buildArrayFromObject();
		}

		protected Object[] buildArrayFromObject() {
			List<Object> result = new ArrayList<Object>();
			int size = (Integer) getSizeField().getValue(object);
			for (int i = 0; i < size; i++) {
				Object item = getGetMethod().invoke(object, new InvocationData(i));
				result.add(item);
			}
			return result.toArray();
		}

		public ValueInstance(Object[] precomputedArray) {
			this.array = precomputedArray;
		}

		public ImplicitListFieldInfo getImplicitListField() {
			return ImplicitListFieldInfo.this;
		}

		public Object getObject() {
			return object;
		}

		protected Object[] getArray() {
			return array;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(getArray());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ValueInstance other = (ValueInstance) obj;
			if (!Arrays.equals(getArray(), other.getArray())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return ValueInstance.class.getSimpleName() + ": " + Arrays.toString(getArray());
		}

	}

	public class ValueTypeInfo extends AbstractInfo implements IListTypeInfo {

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
		public FieldsLayout getFieldsLayout() {
			return FieldsLayout.VERTICAL_FLOW;
		}

		@Override
		public MenuModel getMenuModel() {
			return new MenuModel();
		}

		@Override
		public boolean isItemConstructorSelectable() {
			return true;
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
		}

		@Override
		public boolean isItemNullValueDistinct() {
			return false;
		}

		@Override
		public ValueReturnMode getItemReturnMode() {
			return getGetMethod().getValueReturnMode();
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
			return "ImplicitListType [fieldName=" + fieldName + ", parentType=" + parentType.getName() + "]";
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.getDefaultListTypeCaption(this);
		}

		@Override
		public void validate(Object object) throws Exception {
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof ValueInstance;
		}

		@Override
		public boolean isConcrete() {
			return true;
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
			return Collections.emptyList();
		}

		@Override
		public Object[] toArray(Object listValue) {
			ValueInstance implicitListFieldValue = (ValueInstance) listValue;
			return implicitListFieldValue.getArray();

		}

		@Override
		public boolean canReplaceContent() {
			return true;
		}

		@Override
		public void replaceContent(Object listValue, Object[] array) {
			ValueInstance implicitListFieldValue = (ValueInstance) listValue;
			implicitListFieldValue.array = array;
		}

		@Override
		public Object fromArray(Object[] array) {
			return new ValueInstance(array);
		}

		@Override
		public boolean canInstanciateFromArray() {
			return true;
		}

		@Override
		public boolean isOrdered() {
			return true;
		}

		@Override
		public boolean isInsertionAllowed() {
			return createMethodName != null;
		}

		@Override
		public boolean isRemovalAllowed() {
			return removeMethodName != null;
		}

		@Override
		public boolean canViewItemDetails() {
			return true;
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return new DefaultListStructuralInfo(reflectionUI);
		}

		@Override
		public IListItemDetailsAccessMode getDetailsAccessMode() {
			return new DetachedItemDetailsAccessMode();
		}

		@Override
		public List<AbstractListAction> getDynamicActions(List<? extends ItemPosition> selection,
				Object rootListValue) {
			return Collections.emptyList();
		}

		@Override
		public List<AbstractListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
				Object rootListValue) {
			return Collections.emptyList();
		}

		@Override
		public ITypeInfo getItemType() {
			return itemType;
		}

		@Override
		public List<IMethodInfo> getAdditionalItemConstructors(final Object listValue) {
			final ValueInstance instance = (ValueInstance) listValue;
			return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

				@Override
				public ITypeInfo getReturnValueType() {
					return ValueTypeInfo.this.getItemType();
				}

				@Override
				public Object invoke(Object nullObject, InvocationData invocationData) {
					Object result = getCreateMethod().invoke(instance.getObject(),
							new InvocationData(instance.getObject()));
					return result;
				}

				@Override
				public List<IParameterInfo> getParameters() {
					return Collections.emptyList();
				}
			});
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

		public ImplicitListFieldInfo getOuterType() {
			return ImplicitListFieldInfo.this;
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
			return "TypeInfo [of=" + getOuterType() + "]";
		}

	}
}
