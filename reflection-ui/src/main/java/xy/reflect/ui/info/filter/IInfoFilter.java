package xy.reflect.ui.info.filter;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

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

	boolean excludeField(IFieldInfo field);

	boolean excludeMethod(IMethodInfo method);

}
