


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
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransactionInfo;
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
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual list field that uses multiple fields/methods to simulate a list
 * value.
 * 
 * @author olitank
 *
 */
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
	public String getCaption() {
		return ReflectionUIUtils.identifierToCaption(fieldName);
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
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public IListTypeInfo getType() {
		if (type == null) {
			type = (IListTypeInfo) reflectionUI
					.buildTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(new ValueTypeInfo()));
		}
		return type;
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(final Object object) {
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

			@Override
			public ITypeInfo getReturnValueType() {
				return ImplicitListFieldInfo.this.getType();
			}

			@Override
			public Object invoke(Object ignore, InvocationData invocationData) {
				Object result = getCreateMethod().invoke(object, new InvocationData(object, getCreateMethod(), object));
				return result;
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}
		});
	}

	@Override
	public Object getValue(Object object) {
		return new PrecomputedTypeInstanceWrapper(new ValueInstance(object), new ValueTypeInfo());
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		Object[] array = getType().toArray(value);
		while (true) {
			int size = (Integer) getSizeField().getValue(object);
			if (size == 0) {
				break;
			}
			getRemoveMethod().invoke(object, new InvocationData(object, getRemoveMethod(), 0, 0));
		}
		for (int i = 0; i < array.length; i++) {
			Object item = array[i];
			InvocationData invocationData = new InvocationData(object, getAddMethod());
			invocationData.getProvidedParameterValues().put(0, i);
			invocationData.getProvidedParameterValues().put(1, item);
			getAddMethod().invoke(object, invocationData);
		}
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
	public boolean isTransient() {
		return false;
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
		protected Object[] array;

		public ValueInstance(Object object) {
			this.array = buildArrayFromObject(object);
		}

		public ValueInstance(Object[] precomputedArray) {
			this.array = precomputedArray;
		}

		protected Object[] buildArrayFromObject(Object object) {
			List<Object> result = new ArrayList<Object>();
			int size = (Integer) getSizeField().getValue(object);
			for (int i = 0; i < size; i++) {
				Object item = getGetMethod().invoke(object, new InvocationData(object, getGetMethod(), i));
				result.add(item);
			}
			return result.toArray();
		}

		public ImplicitListFieldInfo getImplicitListField() {
			return ImplicitListFieldInfo.this;
		}

		protected Object[] getArray() {
			return array;
		}

		public ImplicitListFieldInfo getSourceField() {
			return ImplicitListFieldInfo.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getSourceField().hashCode();
			result = prime * result + Arrays.deepHashCode(array);
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
			ValueInstance other = (ValueInstance) obj;
			if (!getSourceField().equals(other.getSourceField()))
				return false;
			if (!Arrays.deepEquals(array, other.array))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ValueInstance [of=" + getImplicitListField() + "]";
		}

	}

	public class ValueTypeInfo extends AbstractInfo implements IListTypeInfo {

		@Override
		public ITypeInfoSource getSource() {
			return new PrecomputedTypeInfoSource(this,
					new SpecificitiesIdentifier(parentType.getName(), ImplicitListFieldInfo.this.getName()));
		}

		@Override
		public ITransactionInfo getTransaction(Object object) {
			return null;
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
		public ColorSpecification getFormEditorsForegroundColor() {
			return null;
		}

		@Override
		public ColorSpecification getFormEditorsBackgroundColor() {
			return null;
		}

		@Override
		public Dimension getFormPreferredSize() {
			return null;
		}

		@Override
		public int getFormSpacing() {
			return ITypeInfo.DEFAULT_FORM_SPACING;
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
		public boolean isItemNullValueSupported() {
			return false;
		}

		@Override
		public InitialItemValueCreationOption getInitialItemValueCreationOption() {
			return InitialItemValueCreationOption.CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES;
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
		public boolean supports(Object object) {
			if (!(object instanceof ValueInstance)) {
				return false;
			}
			if (!getSourceField().equals(((ValueInstance) object).getSourceField())) {
				return false;
			}
			return true;
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
			if (!ImplicitListFieldInfo.this.equals(implicitListFieldValue.getImplicitListField())) {
				throw new ReflectionUIError();
			}
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
		public List<IDynamicListAction> getDynamicActions(List<? extends ItemPosition> selection,
				Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
			return Collections.emptyList();
		}

		@Override
		public List<IDynamicListProperty> getDynamicProperties(List<? extends ItemPosition> selection,
				Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
			return Collections.emptyList();
		}

		@Override
		public ITypeInfo getItemType() {
			return itemType;
		}

		public List<IMethodInfo> getItemConstructors() {
			return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

				@Override
				public ITypeInfo getReturnValueType() {
					return ValueTypeInfo.this.getItemType();
				}

				@Override
				public Object invoke(Object parentObject, InvocationData invocationData) {
					Object result = getCreateMethod().invoke(parentObject,
							new InvocationData(parentObject, getCreateMethod(), parentObject));
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
			List<String> result = new ArrayList<String>();
			for (Object item : toArray(object)) {
				result.add(ReflectionUIUtils.toString(reflectionUI, item));
			}
			return MiscUtils.stringJoin(result, ", ");
		}

		public ImplicitListFieldInfo getSourceField() {
			return ImplicitListFieldInfo.this;
		}

		@Override
		public int hashCode() {
			return getSourceField().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!getSourceField().equals(((ValueTypeInfo) obj).getSourceField())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getSourceField() + "]";
		}

	}
}
