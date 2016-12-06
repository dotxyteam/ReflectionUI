package xy.reflect.ui.info.type.util;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.util.MethodSetupObjectFactory.TypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class ArrayAsEnumerationFactory {
	protected ReflectionUI reflectionUI;
	protected Object[] array;
	protected String typeName;
	protected String typeCaption;

	public ArrayAsEnumerationFactory(ReflectionUI reflectionUI, Object[] array, String typeName, String typeCaption) {
		super();
		this.reflectionUI = reflectionUI;
		this.array = array;
		this.typeName = typeName;
		this.typeCaption = typeCaption;
	}
	


	protected Map<String, Object> getItemSpecificProperties(Object arrayItem) {
		return Collections.emptyMap();
	}

	protected String getItemOnlineHelp(Object arrayItem) {
		return null;
	}

	protected String getItemName(Object arrayItem) {
		return "item(" + ReflectionUIUtils.toString(reflectionUI, arrayItem) + ")";
	}

	protected String getItemCaption(Object arrayItem) {
		return ReflectionUIUtils.toString(reflectionUI, arrayItem);
	}

	public Object getInstance(Object arrayItem) {
		if (arrayItem == null) {
			return null;
		}
		if (!Arrays.asList(array).contains(arrayItem)) {
			throw new ReflectionUIError();
		}
		Instance result = new Instance(arrayItem);
		reflectionUI.registerPrecomputedTypeInfoObject(result, new TypeInfo());
		return result;
	}

	public Object unwrapInstance(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		if (instance.getOuterType() != this) {
			throw new ReflectionUIError();
		}
		return instance.getArrayItem();
	}

	public ITypeInfoSource getTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfo());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(array);
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
		ArrayAsEnumerationFactory other = (ArrayAsEnumerationFactory) obj;
		if (!Arrays.equals(array, other.array))
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
		return "ArrayAsEnumerationFactory [typeCaption=" + typeCaption + "]";
	}

	protected class Instance {
		protected Object arrayItem;

		public Instance(Object arrayItem) {
			super();
			this.arrayItem = arrayItem;
		}

		public Object getArrayItem() {
			return arrayItem;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((arrayItem == null) ? 0 : arrayItem.hashCode());
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
			if (arrayItem == null) {
				if (other.arrayItem != null)
					return false;
			} else if (!arrayItem.equals(other.arrayItem))
				return false;
			return true;
		}

		private ArrayAsEnumerationFactory getOuterType() {
			return ArrayAsEnumerationFactory.this;
		}

		@Override
		public String toString() {
			return arrayItem.toString();
		}

	}

	protected class TypeInfo implements IEnumerationTypeInfo {
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
			return typeName;
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
			if (array.length == 0) {
				return Collections.emptyList();
			} else {
				return Collections.<IMethodInfo> singletonList(new AbstractConstructorMethodInfo(TypeInfo.this) {

					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						return getInstance(array[0]);
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
			for (Object arrayItem : array) {
				result.add((Instance) getInstance(arrayItem));
			}
			return result.toArray();
		}

		@Override
		public IEnumerationItemInfo getValueInfo(final Object object) {
			final Object arrayItem = unwrapInstance(object);
			return new IEnumerationItemInfo() {

				@Override
				public Map<String, Object> getSpecificProperties() {
					return getItemSpecificProperties(arrayItem);
				}

				@Override
				public String getOnlineHelp() {
					return getItemOnlineHelp(arrayItem);
				}

				@Override
				public String getName() {
					return getItemName(arrayItem);
				}

				@Override
				public String getCaption() {
					return getItemCaption(arrayItem);
				}
			};
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
		public String toString(Object object) {
			ReflectionUIUtils.checkInstance(this, object);
			return object.toString();
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
		public boolean equals(Object value1, Object value2) {
			ReflectionUIUtils.checkInstance(this, value1);
			return ReflectionUIUtils.equalsOrBothNull(value1, value2);
		}

	}
}
