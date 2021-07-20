


package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Factory that generates type information proxies that conform to the specified
 * {@link IInfoFilter}.
 * 
 * @author olitank
 *
 */
public class FilteredTypeFactory extends InfoProxyFactory {

	protected IInfoFilter infoFilter;

	public FilteredTypeFactory(IInfoFilter infoFilter) {
		this.infoFilter = infoFilter;
	}

	@Override
	protected List<IFieldInfo> getFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : super.getFields(type)) {
			if (infoFilter.excludeField(field)) {
				continue;
			}
			result.add(field);
		}
		return result;
	}

	@Override
	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo method : super.getMethods(type)) {
			if (infoFilter.excludeMethod(method)) {
				continue;
			}
			result.add(method);
		}
		return result;
	}

	@Override
	public String toString() {
		return "FilteredTypeFactory [infoFilter=" + infoFilter + "]";
	}

}
