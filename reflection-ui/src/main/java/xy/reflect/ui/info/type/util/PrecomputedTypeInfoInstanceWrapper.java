package xy.reflect.ui.info.type.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.info.method.InvocationData;

public class PrecomputedTypeInfoInstanceWrapper {

	protected Object instance;
	protected ITypeInfo precomputedType;
	protected StackTraceElement[] instanciationTrace = ReflectionUIUtils.createDebugStackTrace(1);

	public PrecomputedTypeInfoInstanceWrapper(Object instance, ITypeInfo precomputedType) {
		this.instance = instance;
		this.precomputedType = precomputedType;
	}

	public ITypeInfo getPrecomputedType() {
		return precomputedType;
	}

	public PrecomputedTypeInfoSource getPrecomputedTypeInfoSource() {
		return new PrecomputedTypeInfoSource(adaptPrecomputedType(precomputedType, getDebugInfoEnclosingMethod()));
	}

	protected Method getDebugInfoEnclosingMethod() {
		return getClass().getEnclosingMethod();
	}

	public static ITypeInfo adaptPrecomputedType(ITypeInfo precomputedType) {
		return adaptPrecomputedType(precomputedType, null);
	}

	public static ITypeInfo adaptPrecomputedType(final ITypeInfo precomputedType, final Object debugInfo) {
		return new InfoProxyGenerator() {

			protected PrecomputedTypeInfoInstanceWrapper wrap(Object object) {
				return new PrecomputedTypeInfoInstanceWrapper(object, precomputedType);
			}

			protected Object unwrap(Object object) {
				PrecomputedTypeInfoInstanceWrapper wrapper = (PrecomputedTypeInfoInstanceWrapper) object;
				if (!wrapper.precomputedType.equals(precomputedType)) {
					throw new ReflectionUIError(PrecomputedTypeInfoInstanceWrapper.class.getSimpleName() + " Error: "
							+ "\nExpected precomputed type: " + precomputedType + " (" + precomputedType.getClass()
							+ ")" + ";" + "\nFound precomputed type: " + wrapper.precomputedType + " ("
							+ wrapper.precomputedType.getClass() + ")" + ";" +

					"\nInstance: " + wrapper.instance);
				}
				return wrapper.getInstance();
			}

			@Override
			protected Object getValue(Object object, IFieldInfo field, ITypeInfo containingType) {
				object = unwrap(object);
				return super.getValue(object, field, containingType);
			}

			@Override
			protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
				object = unwrap(object);
				super.setValue(object, value, field, containingType);
			}

			@Override
			protected Object invoke(Object object, InvocationData invocationData, IMethodInfo method,
					ITypeInfo containingType) {
				object = unwrap(object);
				return super.invoke(object, invocationData, method, containingType);
			}

			@Override
			protected void validate(ITypeInfo type, Object object) throws Exception {
				object = unwrap(object);
				super.validate(type, object);
			}

			@Override
			protected String toString(ITypeInfo type, Object object) {
				object = unwrap(object);
				return super.toString(type, object);
			}

			@Override
			protected Object fromArray(IListTypeInfo type, Object[] listValue) {
				Object result = super.fromArray(type, listValue);
				return wrap(result);
			}

			@Override
			protected Object[] getPossibleValues(IEnumerationTypeInfo type) {
				List<Object> result = new ArrayList<Object>();
				for (Object item : super.getPossibleValues(type)) {
					result.add(wrap(item));
				}
				return result.toArray();
			}

			@Override
			protected IEnumerationItemInfo getValueInfo(Object object, IEnumerationTypeInfo type) {
				object = unwrap(object);
				return super.getValueInfo(object, type);
			}

			@Override
			protected Object[] toArray(IListTypeInfo type, Object object) {
				object = unwrap(object);
				return super.toArray(type, object);
			}

			@Override
			protected boolean supportsInstance(ITypeInfo type, Object object) {
				object = unwrap(object);
				return super.supportsInstance(type, object);
			}

			@Override
			protected void validateParameters(IMethodInfo method, ITypeInfo containingType, Object object,
					InvocationData invocationData) throws Exception {
				object = unwrap(object);
				super.validateParameters(method, containingType, object, invocationData);
			}

			@Override
			protected IModification getUndoModification(IMethodInfo method, ITypeInfo containingType, Object object,
					InvocationData invocationData) {
				object = unwrap(object);
				return super.getUndoModification(method, containingType, object, invocationData);
			}
		}.get(precomputedType);
	}

	public Object getInstance() {
		return instance;
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
		PrecomputedTypeInfoInstanceWrapper other = (PrecomputedTypeInfoInstanceWrapper) obj;
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
		return instance.toString();
	}

}
