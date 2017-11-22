package xy.reflect.ui.info.type.factory;

import xy.reflect.ui.info.type.ITypeInfo;

public interface ITypeInfoProxyFactory {

	ITypeInfo wrapType(ITypeInfo type);

}