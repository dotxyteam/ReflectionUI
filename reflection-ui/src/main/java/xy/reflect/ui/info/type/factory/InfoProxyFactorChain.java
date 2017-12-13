package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.type.ITypeInfo;

public class InfoProxyFactorChain implements IInfoProxyFactory {

	protected List<IInfoProxyFactory> factories = new ArrayList<IInfoProxyFactory>();

	public InfoProxyFactorChain(IInfoProxyFactory... factories) {
		super();
		this.factories.addAll(Arrays.asList(factories));
	}

	@Override
	public ITypeInfo wrapType(ITypeInfo type) {
		ITypeInfo result = type;
		for (IInfoProxyFactory factory : factories) {
			result = factory.wrapType(result);
		}
		return result;
	}

}