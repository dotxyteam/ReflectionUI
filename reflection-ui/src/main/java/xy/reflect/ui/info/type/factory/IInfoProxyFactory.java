


package xy.reflect.ui.info.type.factory;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * The base interface of all abstract UI model transformers.
 * 
 * @author olitank
 *
 */
public interface IInfoProxyFactory {

	/**
	 * @param type The type information to be transformed.
	 * @return typically a proxy of the given type information.
	 */
	ITypeInfo wrapTypeInfo(ITypeInfo type);

	/**
	 * @param appInfo The application information to be transformed.
	 * @return typically a proxy of the given application information.
	 */
	IApplicationInfo wrapApplicationInfo(IApplicationInfo appInfo);

}
