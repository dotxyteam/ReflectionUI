package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;

public interface IModification {
	IModification NULL_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite() {
			return NULL_MODIFICATION;
		}
	
		@Override
		public int getNumberOfUnits() {
			return 0;
		}
	
		@Override
		public String toString() {
			return getTitle();
		}
	
		@Override
		public String getTitle() {
			return "NULL_MODIFICATION";
		}

		@Override
		public IInfo getTarget() {
			return null;
		}
	
	};

	IModification applyAndGetOpposite();

	int getNumberOfUnits();

	String getTitle();

	IInfo getTarget();
}
