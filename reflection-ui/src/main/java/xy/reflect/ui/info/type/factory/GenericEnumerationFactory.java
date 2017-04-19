package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
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

	protected String getItemIconImagePath(Object item) {
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

	public ITypeInfoSource getInstanceTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfo());
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

	protected class Instance {
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

	protected class TypeInfo implements IEnumerationTypeInfo {

		@Override
		public boolean isDynamicEnumeration() {
			return dynamicEnumeration;
		}

		@Override
		public String getIconImagePath() {
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

					@Override
					public ITypeInfo getReturnValueType() {
						return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(TypeInfo.this));
					}

					@Override
					public Object invoke(Object object, InvocationData invocationData) {
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
			return new IEnumerationItemInfo() {

				@Override
				public Map<String, Object> getSpecificProperties() {
					return getItemSpecificProperties(item);
				}

				@Override
				public String getIconImagePath() {
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

				@Override
				public String toString() {
					return item.toString();
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

		GenericEnumerationFactory getOuterType() {
			return GenericEnumerationFactory.this;
		}

		@Override
		public String toString() {
			return "TypeInfo [of=" + getOuterType() + "]";
		}

	}
}
