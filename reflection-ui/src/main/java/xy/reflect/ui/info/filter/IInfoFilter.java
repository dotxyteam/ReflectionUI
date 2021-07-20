


package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * This interface allows to specify objects used to dynamically filter out some
 * fields and methods from the display.
 * 
 * @author olitank
 *
 */
public interface IInfoFilter {

	public IInfoFilter DEFAULT = new IInfoFilter() {

		@Override
		public boolean excludeField(IFieldInfo field) {
			return false;
		}

		@Override
		public boolean excludeMethod(IMethodInfo method) {
			return false;
		}

		@Override
		public String toString() {
			return IInfoFilter.class.getName() + ".DEFAULT";
		}

	};

	/**
	 * @param field
	 *            The field to be filtered out.
	 * @return true if the given field is filtered out from the display.
	 */
	boolean excludeField(IFieldInfo field);

	/**
	 * @param method
	 *            The method to be filtered out.
	 * @return true if the given method is filtered out from the display.
	 */
	boolean excludeMethod(IMethodInfo method);

}
