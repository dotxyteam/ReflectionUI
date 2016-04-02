package xy.reflect.ui.info;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;


public interface IInfoCollectionSettings {

	public IInfoCollectionSettings DEFAULT = new IInfoCollectionSettings() {
		
		@Override
		public boolean excludeField(IFieldInfo field) {
			return false;
		}

		@Override
		public boolean excludeMethod(IMethodInfo method) {
			return false;
		}

	};
	

	boolean excludeField(IFieldInfo field);

	boolean excludeMethod(IMethodInfo method);

	
}
