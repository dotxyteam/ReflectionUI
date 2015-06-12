package xy.reflect.ui.info.field;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ListControl;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.iterable.util.IListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
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

	public ImplicitListField(ReflectionUI reflectionUI, String fieldName,
			ITypeInfo parentType, String createMethodName,
			String getMethodName, String addMethodName,
			String removeMethodName, String sizeMethodName) {
		this.reflectionUI = reflectionUI;
		this.fieldName = fieldName;
		this.parentType = parentType;
		this.createMethodName = createMethodName;
		this.getMethodName = getMethodName;
		this.addMethodName = addMethodName;
		this.removeMethodName = removeMethodName;
		this.sizeMethodName = sizeMethodName;
		this.itemType = getCreateMethod().getReturnValueType();

	}

	protected IMethodInfo getCreateMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(),
				createMethodName);
	}

	protected IMethodInfo getGetMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(),
				getMethodName);
	}

	protected IMethodInfo getAddMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(),
				addMethodName);
	}

	protected IMethodInfo getRemoveMethod() {
		return ReflectionUIUtils.findInfoByName(parentType.getMethods(),
				removeMethodName);
	}

	protected IFieldInfo getSizeField() {
		return ReflectionUIUtils.findInfoByName(parentType.getFields(),
				sizeMethodName);
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
			type = new ImplicitListFieldType(null);
		}
		return type;
	}

	@Override
	public Object getValue(Object object) {
		Object result = new ImplicitListFieldValue(object);
		result = new PrecomputedTypeInfoInstanceWrapper(result,
				new ImplicitListFieldType(object));
		return result;
	}

	@Override
	public void setValue(Object object, Object value) {
		PrecomputedTypeInfoInstanceWrapper wrapper = (PrecomputedTypeInfoInstanceWrapper) value;
		ImplicitListFieldValue implicitListFieldValue = (ImplicitListFieldValue) wrapper
				.getInstance();
		if (!this.equals(implicitListFieldValue.getImplicitListField())) {
			throw new ReflectionUIError();
		}
		Object[] array = getType().toArray(implicitListFieldValue);
		while (true) {
			int size = (Integer) getSizeField().getValue(object);
			if (size == 0) {
				break;
			}
			getRemoveMethod().invoke(object,
					Collections.<Integer, Object> singletonMap(0, 0));
		}
		for (int i = 0; i < array.length; i++) {
			Object item = array[i];
			Map<Integer, Object> params = new HashMap<Integer, Object>();
			params.put(0, i);
			params.put(1, item);
			getAddMethod().invoke(object, params);
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
	public boolean isReadOnly() {
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
		result = prime * result
				+ ((addMethodName == null) ? 0 : addMethodName.hashCode());
		result = prime
				* result
				+ ((createMethodName == null) ? 0 : createMethodName.hashCode());
		result = prime * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result
				+ ((getMethodName == null) ? 0 : getMethodName.hashCode());
		result = prime * result
				+ ((itemType == null) ? 0 : itemType.hashCode());
		result = prime
				* result
				+ ((removeMethodName == null) ? 0 : removeMethodName.hashCode());
		result = prime * result
				+ ((sizeMethodName == null) ? 0 : sizeMethodName.hashCode());
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

	protected class ImplicitListFieldValue {
		protected Object object;
		protected Object[] precomputedArray;

		public ImplicitListFieldValue(Object object) {
			this.object = object;
		}

		public ImplicitListField getImplicitListField() {
			return ImplicitListField.this;
		}

		public ImplicitListFieldValue(Object[] precomputedArray) {
			this.precomputedArray = precomputedArray;
		}

		public Object getObject() {
			return object;
		}

		public Object[] getPrecomputedArray() {
			return precomputedArray;
		}

		protected Object[] getArray() {
			if (precomputedArray != null) {
				return precomputedArray;
			} else {
				return ImplicitListField.this.getType().toArray((this));
			}
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
			ImplicitListFieldValue other = (ImplicitListFieldValue) obj;
			if (!Arrays.equals(getArray(), other.getArray())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return ImplicitListFieldValue.class.getSimpleName() + ": "
					+ Arrays.toString(getArray());
		}

	}

	protected class ImplicitListFieldType implements IListTypeInfo {

		protected Object object;

		public ImplicitListFieldType(Object object) {
			this.object = object;
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
			return object instanceof ImplicitListFieldValue;
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
		public boolean hasCustomFieldControl() {
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
		public Component createFieldControl(final Object object,
				IFieldInfo field) {
			return new ListControl(reflectionUI, object, new FieldInfoProxy(
					field) {

				@Override
				public ITypeInfo getType() {
					return PrecomputedTypeInfoInstanceWrapper
							.adaptPrecomputedType(new ImplicitListFieldType(
									object));
				}

			});
		}

		@Override
		public Object[] toArray(Object listValue) {
			ImplicitListFieldValue implicitListFieldValue = (ImplicitListFieldValue) listValue;
			if (implicitListFieldValue.getPrecomputedArray() != null) {
				return implicitListFieldValue.getPrecomputedArray();
			}
			List<Object> result = new ArrayList<Object>();
			int size = (Integer) getSizeField().getValue(
					implicitListFieldValue.getObject());
			for (int i = 0; i < size; i++) {
				Object item = getGetMethod().invoke(
						implicitListFieldValue.getObject(),
						Collections.<Integer, Object> singletonMap(0, i));
				result.add(item);
			}
			return result.toArray();
		}

		@Override
		public Object fromArray(Object[] array) {
			return new ImplicitListFieldValue(array);
		}

		@Override
		public boolean isOrdered() {
			return true;
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return new StandardCollectionTypeInfo(reflectionUI, List.class,
					Object.class) {

				@Override
				public ITypeInfo getItemType() {
					return ImplicitListField.this.getType().getItemType();
				}

			}.getStructuralInfo();
		}

		@Override
		public List<IListAction> getSpecificActions(Object object,
				IFieldInfo field, List<? extends ItemPosition> selection) {
			return Collections.emptyList();
		}

		@Override
		public ITypeInfo getItemType() {
			return new TypeInfoProxyConfiguration() {

				@Override
				protected List<IMethodInfo> getConstructors(ITypeInfo type) {
					if (ImplicitListFieldType.this.object == null) {
						return Collections.emptyList();
					} else {
						return Collections
								.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(
										ImplicitListFieldType.this
												.getItemType()) {

									@Override
									public Object invoke(
											Object nullObject,
											Map<Integer, Object> valueByParameterPosition) {
										Object result = getCreateMethod()
												.invoke(ImplicitListFieldType.this.object,
														Collections
																.singletonMap(
																		0,
																		ImplicitListFieldType.this.object));
										return result;
									}

									@Override
									public List<IParameterInfo> getParameters() {
										return Collections.emptyList();
									}
								});
					}
				}

			}.get(itemType);
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
			ImplicitListFieldType other = (ImplicitListFieldType) obj;
			if (!getImplicitListField().equals(other.getImplicitListField()))
				return false;
			return true;
		}

		public ImplicitListField getImplicitListField() {
			return ImplicitListField.this;
		}

	}
}
