package xy.reflect.ui.undo;

public interface IModification {
	IModification NULL_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite() {
			return NULL_MODIFICATION;
		}

		@Override
		public boolean isNull() {
			return true;
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
	IModification FAKE_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite() {
			return FAKE_MODIFICATION;
		}

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "FAKE_MODIFICATION";
		}

	};;

	IModification applyAndGetOpposite();

	boolean isNull();

	String getTitle();
}
