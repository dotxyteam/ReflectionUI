package xy.reflect.ui.undo;
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
	
	};

	IModification applyAndGetOpposite();

	int getNumberOfUnits();

	String getTitle();
}
