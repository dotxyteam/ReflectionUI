package xy.reflect.ui.info;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;


public interface IInfoCollectionSettings {

	IInfoCollectionSettings DEFAULT = new IInfoCollectionSettings() {
		
		@Override
		public boolean allReadOnly() {
			return false;
		}

		@Override
		public boolean excludeField(IFieldInfo field) {
			return false;
		}

		@Override
		public boolean excludeMethod(IMethodInfo method) {
			return false;
		}

	};

	boolean allReadOnly();

	boolean excludeField(IFieldInfo field);

	boolean excludeMethod(IMethodInfo method);

	
}
