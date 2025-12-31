package xy.reflect.ui.info.type.factory;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;

/**
 * Implementation of {@link IInfoProxyFactory} that delegates to another
 * instance that can be replaced dynamically.
 * 
 * @author olitank
 *
 */
public abstract class DelegatingInfoProxyFactory implements IInfoProxyFactory {

	/**
	 * @return Dynamically the delegate.
	 */
	protected abstract IInfoProxyFactory getDelegate();

	/**
	 * @return An object identifying the delegate. It allows to compare instances of
	 *         the current class even if the delegate cannot be retrieved. By
	 *         default the return value is the delegate itself.
	 */
	protected Object getDelegateId() {
		return getDelegate();
	}

	public IApplicationInfo wrapApplicationInfo(IApplicationInfo appInfo) {
		return getDelegate().wrapApplicationInfo(appInfo);
	}

	public IApplicationInfo unwrapApplicationInfo(IApplicationInfo appInfo) {
		return getDelegate().unwrapApplicationInfo(appInfo);
	}

	public ITypeInfo wrapTypeInfo(ITypeInfo type) {
		return getDelegate().wrapTypeInfo(type);
	}

	public ITypeInfo unwrapTypeInfo(ITypeInfo type) {
		return getDelegate().unwrapTypeInfo(type);
	}

	public IFieldInfo wrapFieldInfo(IFieldInfo field, ITypeInfo objectType) {
		return getDelegate().wrapFieldInfo(field, objectType);
	}

	public IFieldInfo unwrapFieldInfo(IFieldInfo field, ITypeInfo objectType) {
		return getDelegate().unwrapFieldInfo(field, objectType);
	}

	public IMethodInfo wrapMethodInfo(IMethodInfo method, ITypeInfo objectType) {
		return getDelegate().wrapMethodInfo(method, objectType);
	}

	public IMethodInfo unwrapMethodInfo(IMethodInfo method, ITypeInfo objectType) {
		return getDelegate().unwrapMethodInfo(method, objectType);
	}

	public IEnumerationItemInfo wrapEnumerationItemInfo(IEnumerationItemInfo itemInfo,
			IEnumerationTypeInfo parentEnumType) {
		return getDelegate().wrapEnumerationItemInfo(itemInfo, parentEnumType);
	}

	public IEnumerationItemInfo unwrapEnumerationItemInfo(IEnumerationItemInfo itemInfo,
			IEnumerationTypeInfo parentEnumType) {
		return getDelegate().unwrapEnumerationItemInfo(itemInfo, parentEnumType);
	}

	public IMethodInfo wrapConstructorInfo(IMethodInfo constructor, ITypeInfo objectType) {
		return getDelegate().wrapConstructorInfo(constructor, objectType);
	}

	public IMethodInfo unwrapConstructorInfo(IMethodInfo constructor, ITypeInfo objectType) {
		return getDelegate().unwrapConstructorInfo(constructor, objectType);
	}

	public IParameterInfo wrapParameterInfo(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return getDelegate().wrapParameterInfo(param, method, objectType);
	}

	public IParameterInfo unwrapParameterInfo(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		return getDelegate().unwrapParameterInfo(param, method, objectType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDelegateId() == null) ? 0 : getDelegateId().hashCode());
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
		DelegatingInfoProxyFactory other = (DelegatingInfoProxyFactory) obj;
		if (getDelegateId() == null) {
			if (other.getDelegateId() != null)
				return false;
		} else if (!getDelegateId().equals(other.getDelegateId()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DelegatingInfoProxyFactory [delegate=" + getDelegateId() + "]";
	}

}
