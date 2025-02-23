package xy.reflect.ui.undo;

/**
 * Represents 1 or more modifications which could be canceled in 1 time.
 * 
 * @author olitank
 *
 */
public class ModificationScheme {

	protected static ModificationScheme current;

	protected boolean tangible = false;

	protected ModificationScheme() {
	}

	/**
	 * @return the current modification scheme.
	 */
	public static ModificationScheme get() {
		return current;
	}

	/**
	 * Sets/resets the current modification scheme.
	 */
	public static void initiate() {
		current = new ModificationScheme();
	}

	/**
	 * @return whether at least 1 of the atomic modifications of this scheme has
	 *         been executed.
	 */
	public boolean isTangible() {
		return tangible;
	}

	/**
	 * Sets that at least 1 of the atomic modifications of this scheme has been
	 * executed.
	 */
	public void makeTangible() {
		this.tangible = true;
	}
}
