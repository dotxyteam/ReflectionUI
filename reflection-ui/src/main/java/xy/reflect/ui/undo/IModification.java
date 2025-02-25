
package xy.reflect.ui.undo;

/**
 * This is the base interface of every reversible object state modification made
 * through an abstract UI model element. Note that it simulates a control action
 * and then should operate at the same level as UI controls (eventually manage
 * busy indication, ...).
 * 
 * @author olitank
 *
 */
public interface IModification {

	/**
	 * Dummy instance of this class made for utilitarian purposes. Represents a null
	 * (no impact on the object state) modification. It just returns true when
	 * {@link #isNull()} is called.
	 */
	IModification NULL_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite(ModificationStack modificationStack) {
			return NULL_MODIFICATION;
		}

		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public boolean isVolatile() {
			return false;
		}

		@Override
		public boolean isComposite() {
			return false;
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
	 * Dummy instance of this class made for utilitarian purposes. Represents a null
	 * (no impact on the object state) modification. It just returns true when
	 * {@link #isVolatile()} is called.
	 */
	IModification FAKE_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite(ModificationStack modificationStack) {
			return FAKE_MODIFICATION;
		}

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public boolean isVolatile() {
			return true;
		}

		@Override
		public boolean isComposite() {
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
	 * @param modificationStack The contextual modification stack.
	 * @return the opposite modification.
	 * @throws IrreversibleModificationException When it appears that the
	 *                                           modification cannot be reverted.
	 * @throws CancelledModificationException    When it appears that the
	 *                                           modification has been aborted.
	 */
	IModification applyAndGetOpposite(ModificationStack modificationStack)
			throws IrreversibleModificationException, CancelledModificationException;

	/**
	 * @return true if and only if this modification should be considered as empty,
	 *         with no impact on the target object state or its associated
	 *         {@link ModificationStack} and listeners.
	 */
	boolean isNull();

	/**
	 * @return true if and only if this modification is not intended to be reverted
	 *         or replayed (its undo modification should not be pushed on a
	 *         {@link ModificationStack}) but have impact on
	 *         {@link IModificationListener}s that should receive notifications and
	 *         typically refresh themselves.
	 */
	boolean isVolatile();

	/**
	 * @return true if and only if this modification does not perform any tangible
	 *         action by itself but is rather used to orchestrate other
	 *         modifications.
	 */
	boolean isComposite();

	/**
	 * @return the title of this modification.
	 */
	String getTitle();
}
