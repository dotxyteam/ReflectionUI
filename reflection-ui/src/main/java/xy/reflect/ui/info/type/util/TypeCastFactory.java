package xy.reflect.ui.info.type.util;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.IReflectionUI;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldAsOne;
import xy.reflect.ui.info.method.AbstractConstructorMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.util.MethodSetupObjectFactory.TypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class TypeCastFactory {

	protected IReflectionUI reflectionUI;
	protected ITypeInfo targetType;
	protected InstanceTypeInfoFactory instanceTypeInfoFactory;

	public TypeCastFactory(IReflectionUI reflectionUI, ITypeInfo targetType) {
		super();
		this.reflectionUI = reflectionUI;
		this.targetType = targetType;
		this.instanceTypeInfoFactory = createInstanceTypeInfoFactory();
	}

	public Object getInstance(Object object) {
		ReflectionUIUtils.checkInstance(targetType, object);
		Instance result = new Instance(object);
		ITypeInfo instanceTypeInfo = instanceTypeInfoFactory.get(targetType);
		reflectionUI.registerPrecomputedTypeInfoObject(result, instanceTypeInfo);
		return result;
	}

	public Object unwrapInstance(Object obj) {
		if (obj == null) {
			return null;
		}
		Instance instance = (Instance) obj;
		Object result = instance.getUnderlyingObject();
		ReflectionUIUtils.checkInstance(targetType, result);
		return result;
	}

	public ITypeInfoSource getInstanceTypeInfoSource() {
		ITypeInfo instanceTypeInfo = instanceTypeInfoFactory.get(targetType);
		return new PrecomputedTypeInfoSource(instanceTypeInfo);
	}

	protected InstanceTypeInfoFactory createInstanceTypeInfoFactory() {
		return new InstanceTypeInfoFactory();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
		result = prime * result + ((targetType == null) ? 0 : targetType.hashCode());
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
		TypeCastFactory other = (TypeCastFactory) obj;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		if (targetType == null) {
			if (other.targetType != null)
				return false;
		} else if (!targetType.equals(other.targetType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TypeCastFactory [targetType=" + targetType + "]";
	}

	protected class InstanceTypeInfoFactory extends TypeInfoProxyFactory {

		@Override
		public String toString() {
			return "InstanceTypeInfoFactory of " + TypeCastFactory.this;
		}

		@Override
		protected String getName(ITypeInfo type) {
			return "(" + targetType.getName() + ")" + super.getName(type);
		}

		@Override
		protected List<IMethodInfo> getAdditionalItemConstructors(IListTypeInfo type, Object listValue) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo constructor : super.getAdditionalItemConstructors(type, listValue)) {
				result.add(new MethodInfoProxy(constructor) {
					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						Object result = super.invoke(object, invocationData);
						result = getInstance(result);
						return result;
					}

				});
			}
			return result;
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo constructor : super.getConstructors(type)) {
				result.add(new MethodInfoProxy(constructor) {
					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						Object result = super.invoke(object, invocationData);
						result = getInstance(result);
						return result;
					}

				});
			}
			return result;
		}

		@Override
		protected Object[] getPossibleValues(IEnumerationTypeInfo type) {
			List<Object> result = new ArrayList<Object>();
			for (Object value : super.getPossibleValues(type)) {
				result.add(getInstance(value));
			}
			return result.toArray();
		}

		@Override
		protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
			return super.getValue(unwrapInstance(object), field, containingType);
		}

		@Override
		protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
			return super.getValueOptions(unwrapInstance(object), field, containingType);
		}

		@Override
		protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
			super.setValue(unwrapInstance(object), value, field, containingType);
		}

		@Override
		protected Runnable getCustomUndoUpdateJob(Object object, Object value, IFieldInfo field,
				ITypeInfo containingType) {
			return super.getCustomUndoUpdateJob(unwrapInstance(object), value, field, containingType);
		}

		@Override
		protected String toString(ITypeInfo type, Object object) {
			return super.toString(type, unwrapInstance(object));
		}

		@Override
		protected boolean canCopy(ITypeInfo type, Object object) {
			return super.canCopy(type, unwrapInstance(object));
		}

		@Override
		protected Object copy(ITypeInfo type, Object object) {
			return getInstance(super.copy(type, unwrapInstance(object)));
		}

		@Override
		protected boolean equals(ITypeInfo type, Object value1, Object value2) {
			return super.equals(type, unwrapInstance(value1), unwrapInstance(value2));
		}

		@Override
		protected boolean supportsInstance(ITypeInfo type, Object object) {
			return super.supportsInstance(type, unwrapInstance(object));
		}

		@Override
		protected void validate(ITypeInfo type, Object object) throws Exception {
			super.validate(type, unwrapInstance(object));
		}

		@Override
		protected void validateParameters(IMethodInfo method, ITypeInfo containingType, Object object,
				InvocationData invocationData) throws Exception {
			super.validateParameters(method, containingType, unwrapInstance(object), invocationData);
		}

		@Override
		protected Runnable getUndoModification(IMethodInfo method, ITypeInfo containingType, Object object,
				InvocationData invocationData) {
			return super.getUndoModification(method, containingType, unwrapInstance(object), invocationData);
		}

		@Override
		protected IEnumerationItemInfo getValueInfo(Object object, IEnumerationTypeInfo type) {
			return super.getValueInfo(unwrapInstance(object), type);
		}

	}

	protected class Instance {
		protected Object underlyingObject;

		public Instance(Object underlyingObject) {
			super();
			this.underlyingObject = underlyingObject;
		}

		public Object getUnderlyingObject() {
			return underlyingObject;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((underlyingObject == null) ? 0 : underlyingObject.hashCode());
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
			if (underlyingObject == null) {
				if (other.underlyingObject != null)
					return false;
			} else if (!underlyingObject.equals(other.underlyingObject))
				return false;
			return true;
		}

		private TypeCastFactory getOuterType() {
			return TypeCastFactory.this;
		}

		@Override
		public String toString() {
			return underlyingObject.toString();
		}

	}
}
