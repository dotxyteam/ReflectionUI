package xy.reflect.ui.info.type.factory;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IInfoProxyFactory {

	ITypeInfo wrapTypeInfo(ITypeInfo type);

	IApplicationInfo wrapApplicationInfo(IApplicationInfo appInfo);

}