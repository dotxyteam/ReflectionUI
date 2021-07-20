


package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Type information factory that just chains the given type information
 * factories.
 * 
 * @author olitank
 *
 */
public class InfoProxyFactorChain implements IInfoProxyFactory {

	protected List<IInfoProxyFactory> factories = new ArrayList<IInfoProxyFactory>();

	public InfoProxyFactorChain(IInfoProxyFactory... factories) {
		super();
		this.factories.addAll(Arrays.asList(factories));
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
	public IApplicationInfo wrapApplicationInfo(IApplicationInfo appInfo) {
		IApplicationInfo result = appInfo;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapApplicationInfo(result);
		}
		return result;
	}

}
