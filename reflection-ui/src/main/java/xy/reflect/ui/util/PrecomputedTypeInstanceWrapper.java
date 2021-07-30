/*
 * 
 */
package xy.reflect.ui.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;

/**
 * Wrapper allowing to associate an object with a predefined {@link ITypeInfo}.
 * 
 * @author olitank
 *
 */
public class PrecomputedTypeInstanceWrapper implements Comparable<PrecomputedTypeInstanceWrapper> {

	protected Object instance;
	protected ITypeInfo precomputedType;

	public PrecomputedTypeInstanceWrapper(Object instance, ITypeInfo precomputedType) {
		this.instance = instance;
		this.precomputedType = precomputedType;
	}

	/**
	 * @return the wrapped object.
	 */
	public Object unwrap() {
		return instance;
	}

	/**
	 * @return the source of type information that must be used to process this
	 *         wrapper.
	 */
	public ITypeInfoSource getTypeInfoSource() {
		return new TypeInfoSource(precomputedType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareTo(PrecomputedTypeInstanceWrapper that) {
		if (this.instance == null) {
			if (that.instance == null) {
				return 0; // equal
			} else {
				return -1; // null is before other values
			}
		} else {// this.member != null
			if (that.instance == null) {
				return 1; // all other values are after null
			} else {
				return ((Comparable) this.instance).compareTo((Comparable) that.instance);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + ((precomputedType == null) ? 0 : precomputedType.hashCode());
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
		PrecomputedTypeInstanceWrapper other = (PrecomputedTypeInstanceWrapper) obj;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		if (precomputedType == null) {
			if (other.precomputedType != null)
				return false;
		} else if (!precomputedType.equals(other.precomputedType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PrecomputedTypeInstanceWrapper [instance=" + instance + ", precomputedType=" + precomputedType + "]";
	}

	public static class TypeInfoSource implements ITypeInfoSource {

		protected ITypeInfo precomputedType;

		public TypeInfoSource(ITypeInfo precomputedType) {
			this.precomputedType = precomputedType;
		}

		@Override
		public ITypeInfo getTypeInfo() {
			return new InfoFactory(this).wrapTypeInfo(precomputedType);
		}

		@Override
		public SpecificitiesIdentifier getSpecificitiesIdentifier() {
			return precomputedType.getSource().getSpecificitiesIdentifier();
		}

		public ITypeInfo getPrecomputedType() {
			return precomputedType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((precomputedType == null) ? 0 : precomputedType.hashCode());
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
			TypeInfoSource other = (TypeInfoSource) obj;
			if (precomputedType == null) {
				if (other.precomputedType != null)
					return false;
			} else if (!precomputedType.equals(other.precomputedType))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "PrecomputedTypeInstanceWrapper.TypeInfoSource [type=" + precomputedType + "]";
		}

	}

	public static class InfoFactory extends InfoProxyFactory {

		protected TypeInfoSource typeInfoSource;

		public InfoFactory(TypeInfoSource typeInfoSource) {
			this.typeInfoSource = typeInfoSource;
		}

		@Override
		public String getIdentifier() {
			return "PrecomputedTypeInstanceWrapping [type=" + typeInfoSource.getPrecomputedType().getName() + "]";
		}

		@Override
		protected Object getDefaultValue(IParameterInfo param, IMethodInfo method, ITypeInfo containingType,
				Object object) {
			return super.getDefaultValue(param, method, containingType, ReflectionUIUtils.isConstructor(method) ? object
					: ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

		@Override
		protected boolean hasValueOptions(IParameterInfo param, IMethodInfo method, ITypeInfo containingType,
				Object object) {
			return super.hasValueOptions(param, method, containingType, ReflectionUIUtils.isConstructor(method) ? object
					: ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

		@Override
		protected Object[] getValueOptions(IParameterInfo param, IMethodInfo method, ITypeInfo containingType,
				Object object) {
			return super.getValueOptions(param, method, containingType, ReflectionUIUtils.isConstructor(method) ? object
					: ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

		@Override
		protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
			return super.getValue(((PrecomputedTypeInstanceWrapper) object).unwrap(), field, containingType);
		}

		@Override
		protected boolean hasValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
			return super.hasValueOptions(((PrecomputedTypeInstanceWrapper) object).unwrap(), field, containingType);
		}

		@Override
		protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
			return super.getValueOptions(((PrecomputedTypeInstanceWrapper) object).unwrap(), field, containingType);
		}

		@Override
		protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
			super.setValue(((PrecomputedTypeInstanceWrapper) object).unwrap(), value, field, containingType);
		}

		@Override
		protected Runnable getNextUpdateCustomUndoJob(Object object, Object value, IFieldInfo field,
				ITypeInfo containingType) {
			return super.getNextUpdateCustomUndoJob(((PrecomputedTypeInstanceWrapper) object).unwrap(), value, field,
					containingType);
		}

		@Override
		protected String toString(ITypeInfo type, Object object) {
			return super.toString(type, ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

		@Override
		protected boolean canCopy(ITypeInfo type, Object object) {
			return super.canCopy(type, ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

		@Override
		protected Object copy(ITypeInfo type, Object object) {
			Object result = super.copy(type, ((PrecomputedTypeInstanceWrapper) object).unwrap());
			if (result == null) {
				return null;
			}
			result = new PrecomputedTypeInstanceWrapper(result, typeInfoSource.getPrecomputedType());
			return result;
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo ctor : super.getConstructors(type)) {
				result.add(new MethodInfoProxy(ctor) {
					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						Object newInstance = super.invoke(object, invocationData);
						newInstance = new PrecomputedTypeInstanceWrapper(newInstance,
								typeInfoSource.getPrecomputedType());
						return newInstance;
					}
				});
			}
			return result;
		}

		@Override
		protected List<IMethodInfo> getAlternativeConstructors(Object object, IFieldInfo field,
				ITypeInfo containingType) {
			return super.getAlternativeConstructors(((PrecomputedTypeInstanceWrapper) object).unwrap(), field,
					containingType);
		}

		@Override
		protected List<IMethodInfo> getAlternativeListItemConstructors(Object object, IFieldInfo field,
				ITypeInfo containingType) {
			return super.getAlternativeListItemConstructors(((PrecomputedTypeInstanceWrapper) object).unwrap(), field,
					containingType);
		}

		/**
		 * {@link PrecomputedTypeInstanceWrapper} is used to associate object with
		 * custom (usually not polymorphic) type information. Then by default an empty
		 * list is returned by this method. If the custom type is still polymorphic then
		 * this method must be overriden.
		 * 
		 * @return an empty list.
		 */
		@Override
		protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
			return Collections.emptyList();
		}

		@Override
		protected Object invoke(Object object, InvocationData invocationData, IMethodInfo method,
				ITypeInfo containingType) {
			return super.invoke(
					ReflectionUIUtils.isConstructor(method) ? object
							: ((PrecomputedTypeInstanceWrapper) object).unwrap(),
					invocationData, method, containingType);
		}

		@Override
		protected String getConfirmationMessage(Object object, InvocationData invocationData, IMethodInfo method,
				ITypeInfo containingType) {
			return super.getConfirmationMessage(
					ReflectionUIUtils.isConstructor(method) ? object
							: ((PrecomputedTypeInstanceWrapper) object).unwrap(),
					invocationData, method, containingType);
		}

		@Override
		protected void onControlVisibilityChange(Object object, boolean visible, IMethodInfo method,
				ITypeInfo containingType) {
			super.onControlVisibilityChange(ReflectionUIUtils.isConstructor(method) ? object
					: ((PrecomputedTypeInstanceWrapper) object).unwrap(), visible, method, containingType);
		}

		@Override
		protected void onControlVisibilityChange(Object object, boolean visible, IFieldInfo field,
				ITypeInfo containingType) {
			super.onControlVisibilityChange(((PrecomputedTypeInstanceWrapper) object).unwrap(), visible, field,
					containingType);
		}

		@Override
		protected void replaceContent(IListTypeInfo type, Object listValue, Object[] array) {
			super.replaceContent(type, ((PrecomputedTypeInstanceWrapper) listValue).unwrap(), array);
		}

		@Override
		protected Object fromArray(IListTypeInfo type, Object[] array) {
			return new PrecomputedTypeInstanceWrapper(super.fromArray(type, array), type);
		}

		@Override
		protected Object[] toArray(IListTypeInfo type, Object listValue) {
			return super.toArray(type, ((PrecomputedTypeInstanceWrapper) listValue).unwrap());
		}

		@Override
		protected void save(ITypeInfo type, Object object, OutputStream out) {
			super.save(type, ((PrecomputedTypeInstanceWrapper) object).unwrap(), out);
		}

		@Override
		protected void load(ITypeInfo type, Object object, InputStream in) {
			super.load(type, ((PrecomputedTypeInstanceWrapper) object).unwrap(), in);
		}

		@Override
		protected boolean supports(ITypeInfo type, Object object) {
			if (object == null) {
				return super.supports(type, null);
			}
			if (!(object instanceof PrecomputedTypeInstanceWrapper)) {
				return false;
			}
			return super.supports(type, ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

		@Override
		protected boolean onFormVisibilityChange(ITypeInfo type, Object object, boolean visible) {
			return super.onFormVisibilityChange(type, ((PrecomputedTypeInstanceWrapper) object).unwrap(), visible);
		}

		@Override
		protected ITypeInfoSource getSource(ITypeInfo type) {
			return typeInfoSource;
		}

		@Override
		protected void validate(ITypeInfo type, Object object) throws Exception {
			super.validate(type, ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

		@Override
		protected void validateParameters(IMethodInfo method, ITypeInfo containingType, Object object,
				InvocationData invocationData) throws Exception {
			super.validateParameters(method, containingType, ReflectionUIUtils.isConstructor(method) ? object
					: ((PrecomputedTypeInstanceWrapper) object).unwrap(), invocationData);
		}

		@Override
		protected Runnable getNextInvocationUndoJob(IMethodInfo method, ITypeInfo containingType, Object object,
				InvocationData invocationData) {
			return super.getNextInvocationUndoJob(method, containingType,
					ReflectionUIUtils.isConstructor(method) ? object
							: ((PrecomputedTypeInstanceWrapper) object).unwrap(),
					invocationData);
		}

		@Override
		protected Object[] getValues(IEnumerationTypeInfo type) {
			Object[] result = super.getValues(type);
			Object[] newResult = new Object[result.length];
			for (int i = 0; i < result.length; i++) {
				newResult[i] = new PrecomputedTypeInstanceWrapper(result[i], type);
			}
			return newResult;
		}

		@Override
		protected IEnumerationItemInfo getValueInfo(IEnumerationTypeInfo type, Object object) {
			return super.getValueInfo(type, ((PrecomputedTypeInstanceWrapper) object).unwrap());
		}

	}

}
