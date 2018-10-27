package xy.reflect.ui.undo;

/**
 * This is the base interface of every object state modification made through an
 * abstract UI model element.
 * 
 * @author olitank
 *
 */
public interface IModification {

	/**
	 * Dummy instance of this class made available for utilitarian purposes.
	 * Represents a null (no impact on the object state) modification.
	 */
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

	/**
	 * Dummy instance of this class made available for utilitarian purposes.
	 * Represents a fake (simulated impact on the object state) modification.
	 */
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

	/**
	 * Applies the current modification.
	 * 
	 * @return the opposite modification.
	 */
	IModification applyAndGetOpposite();

	/**
	 * @return true if and only if this modification should be considered as empty,
	 *         with no impact on the target object state.
	 */
	boolean isNull();

	/**
	 * @return the title of this modification.
	 */
	String getTitle();
}
