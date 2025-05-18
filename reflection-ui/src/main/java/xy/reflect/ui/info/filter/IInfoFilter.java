
package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * This interface allows you to specify objects used to dynamically filter
 * certain fields and methods for display.
 * 
 * @author olitank
 *
 */
public interface IInfoFilter {

	public IInfoFilter DEFAULT = new IInfoFilter() {

		@Override
		public IFieldInfo apply(IFieldInfo field) {
			return field;
		}

		@Override
		public IMethodInfo apply(IMethodInfo method) {
			return method;
		}

		@Override
		public String toString() {
			return IInfoFilter.class.getName() + ".DEFAULT";
		}

	};

	/**
	 * @param field The field to be filtered.
	 * @return a field (may be the input field or a proxy) or null if the field
	 *         should be filtered out from the display.
	 */
	IFieldInfo apply(IFieldInfo field);

	/**
	 * @param method The method to be filtered.
	 * @return a method (may be the input method or a proxy) or null if the method
	 *         should be filtered out from the display.
	 */
	IMethodInfo apply(IMethodInfo method);

}
