package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.info.type.ITypeInfo;

public class TypeInfoProxyFactorChain implements ITypeInfoProxyFactory {

	protected List<ITypeInfoProxyFactory> factories = new ArrayList<ITypeInfoProxyFactory>();

	public TypeInfoProxyFactorChain(ITypeInfoProxyFactory... factories) {
		super();
		this.factories.addAll(Arrays.asList(factories));
	}

	@Override
	public ITypeInfo wrapType(ITypeInfo type) {
		ITypeInfo result = type;
		for (ITypeInfoProxyFactory factory : factories) {
			result = factory.wrapType(result);
		}
		return result;
	}

}
