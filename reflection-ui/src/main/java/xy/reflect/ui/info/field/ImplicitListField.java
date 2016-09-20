package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ImplicitListField implements IFieldInfo {
	protected String fieldName;
	protected ReflectionUI reflectionUI;
	protected ITypeInfo itemType;
	protected String createMethodName;
	protected String getMethodName;
	protected String addMethodName;
	protected String removeMethodName;
	protected String sizeMethodName;
	protected IListTypeInfo type;
	private ITypeInfo parentType;

	public ImplicitListField(ReflectionUI reflectionUI, String fieldName, ITypeInfo parentType, ITypeInfo itemType,
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
		return ReflectionUIUtils.identifierToCaption(getName());
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
			type = new TypeInfo();
		}
		return type;
	}

	@Override
	public Object getValue(Object object) {
		Object result = new Instance(object);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	@Override
	public void setValue(Object object, Object value) {
		Instance implicitListFieldValue = (Instance) value;
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
			invocationData.setparameterValue(0, i);
			invocationData.setparameterValue(1, item);
			getAddMethod().invoke(object, invocationData);
		}
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public boolean isGetOnly() {
		return false;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
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
		ImplicitListField other = (ImplicitListField) obj;
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
		return getCaption();
	}

	protected class Instance {
		protected Object object;
		protected Object[] array;

		public Instance(Object object) {
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

		public Instance(Object[] precomputedArray) {
			this.array = precomputedArray;
		}

		public ImplicitListField getImplicitListField() {
			return ImplicitListField.this;
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
			Instance other = (Instance) obj;
			if (!Arrays.equals(getArray(), other.getArray())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return Instance.class.getSimpleName() + ": " + Arrays.toString(getArray());
		}

	}

	protected class TypeInfo implements IListTypeInfo {

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
			return ImplicitListField.this.getName() + "Type";
		}

		@Override
		public String getCaption() {
			return ReflectionUIUtils.identifierToCaption(getName());
		}

		@Override
		public void validate(Object object) throws Exception {
		}

		@Override
		public String toString(Object object) {
			return object.toString();
		}

		@Override
		public boolean supportsInstance(Object object) {
			return object instanceof Instance;
		}

		@Override
		public boolean isConcrete() {
			return true;
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return null;
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
			Instance implicitListFieldValue = (Instance) listValue;
			return implicitListFieldValue.getArray();

		}

		@Override
		public boolean canReplaceContent() {
			return true;
		}

		@Override
		public void replaceContent(Object listValue, Object[] array) {
			Instance implicitListFieldValue = (Instance) listValue;
			implicitListFieldValue.array = array;
		}

		@Override
		public Object fromArray(Object[] array) {
			return new Instance(array);
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
		public boolean canAdd() {
			return createMethodName != null;
		}

		@Override
		public boolean canRemove() {
			return removeMethodName != null;
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return new DefaultListStructuralInfo(reflectionUI);
		}

		@Override
		public List<AbstractListAction> getSpecificActions(Object object, IFieldInfo field,
				List<? extends ItemPosition> selection) {
			return Collections.emptyList();
		}

		@Override
		public ITypeInfo getItemType() {
			return itemType;
		}

		@Override
		public List<IMethodInfo> getObjectSpecificItemConstructors(final Object object, IFieldInfo field) {
			return Collections.<IMethodInfo> singletonList(
					new AbstractConstructorMethodInfo(TypeInfo.this.getItemType()) {

						@Override
						public Object invoke(Object nullObject, InvocationData invocationData) {
							Object result = getCreateMethod().invoke(object, new InvocationData(object));
							return result;
						}

						@Override
						public List<IParameterInfo> getParameters() {
							return Collections.emptyList();
						}
					});
		}

		@Override
		public int hashCode() {
			return getImplicitListField().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TypeInfo other = (TypeInfo) obj;
			if (!getImplicitListField().equals(other.getImplicitListField()))
				return false;
			return true;
		}

		public ImplicitListField getImplicitListField() {
			return ImplicitListField.this;
		}

	}
}
