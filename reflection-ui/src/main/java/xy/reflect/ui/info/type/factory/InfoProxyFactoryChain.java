
package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;

/**
 * Type information factory that just chains the given type information
 * factories.
 * 
 * @author olitank
 *
 */
public class InfoProxyFactoryChain implements IInfoProxyFactory {

	protected List<IInfoProxyFactory> factories = new ArrayList<IInfoProxyFactory>();

	public InfoProxyFactoryChain(IInfoProxyFactory... factories) {
		this.factories.addAll(Arrays.asList(factories));
	}

	public List<IInfoProxyFactory> accessFactories() {
		return factories;
	}

	@Override
	public ITypeInfo wrapTypeInfo(ITypeInfo type) {
		ITypeInfo result = type;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapTypeInfo(result);
		}
		return result;
	}

	@Override
	public ITypeInfo unwrapTypeInfo(ITypeInfo type) {
		ITypeInfo result = type;
		for (IInfoProxyFactory factory : getReverseFactoryList()) {
			result = factory.unwrapTypeInfo(result);
		}
		return result;
	}

	@Override
	public IApplicationInfo wrapApplicationInfo(IApplicationInfo appInfo) {
		IApplicationInfo result = appInfo;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapApplicationInfo(result);
		}
		return result;
	}

	@Override
	public IApplicationInfo unwrapApplicationInfo(IApplicationInfo appInfo) {
		IApplicationInfo result = appInfo;
		for (IInfoProxyFactory factory : getReverseFactoryList()) {
			result = factory.unwrapApplicationInfo(result);
		}
		return result;
	}

	@Override
	public IFieldInfo wrapFieldInfo(IFieldInfo field, ITypeInfo objectType) {
		IFieldInfo result = field;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapFieldInfo(result, objectType);
		}
		return result;
	}

	@Override
	public IFieldInfo unwrapFieldInfo(IFieldInfo field, ITypeInfo objectType) {
		IFieldInfo result = field;
		for (IInfoProxyFactory factory : getReverseFactoryList()) {
			result = factory.unwrapFieldInfo(result, objectType);
		}
		return result;
	}

	@Override
	public IMethodInfo wrapMethodInfo(IMethodInfo method, ITypeInfo objectType) {
		IMethodInfo result = method;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapMethodInfo(result, objectType);
		}
		return result;
	}

	@Override
	public IMethodInfo unwrapMethodInfo(IMethodInfo method, ITypeInfo objectType) {
		IMethodInfo result = method;
		for (IInfoProxyFactory factory : getReverseFactoryList()) {
			result = factory.unwrapMethodInfo(result, objectType);
		}
		return result;
	}

	@Override
	public IEnumerationItemInfo wrapEnumerationItemInfo(IEnumerationItemInfo itemInfo,
			IEnumerationTypeInfo parentEnumType) {
		IEnumerationItemInfo result = itemInfo;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapEnumerationItemInfo(result, parentEnumType);
		}
		return result;
	}

	@Override
	public IEnumerationItemInfo unwrapEnumerationItemInfo(IEnumerationItemInfo itemInfo,
			IEnumerationTypeInfo parentEnumType) {
		IEnumerationItemInfo result = itemInfo;
		for (IInfoProxyFactory factory : getReverseFactoryList()) {
			result = factory.unwrapEnumerationItemInfo(result, parentEnumType);
		}
		return result;
	}

	@Override
	public IMethodInfo wrapConstructorInfo(IMethodInfo constructor, ITypeInfo objectType) {
		IMethodInfo result = constructor;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapConstructorInfo(result, objectType);
		}
		return result;
	}

	@Override
	public IMethodInfo unwrapConstructorInfo(IMethodInfo constructor, ITypeInfo objectType) {
		IMethodInfo result = constructor;
		for (IInfoProxyFactory factory : getReverseFactoryList()) {
			result = factory.unwrapConstructorInfo(result, objectType);
		}
		return result;
	}

	@Override
	public IParameterInfo wrapParameterInfo(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		IParameterInfo result = param;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapParameterInfo(result, method, objectType);
		}
		return result;
	}

	@Override
	public IParameterInfo unwrapParameterInfo(IParameterInfo param, IMethodInfo method, ITypeInfo objectType) {
		IParameterInfo result = param;
		for (IInfoProxyFactory factory : getReverseFactoryList()) {
			result = factory.unwrapParameterInfo(result, method, objectType);
		}
		return result;
	}

	protected List<IInfoProxyFactory> getReverseFactoryList() {
		List<IInfoProxyFactory> result = new ArrayList<IInfoProxyFactory>(factories);
		Collections.reverse(result);
		return result;
	}

}
