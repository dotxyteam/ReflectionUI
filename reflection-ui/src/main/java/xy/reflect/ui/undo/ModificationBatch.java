package xy.reflect.ui.undo;

import java.util.ArrayList;

/**
 * Represents 1 or more modifications which could be canceled in 1 time.
 * 
 * @author olitank
 *
 */
public class ModificationBatch extends ArrayList<IModification> {

	private static final long serialVersionUID = 1L;

	protected static ModificationBatch current;

	public static ModificationBatch getCurrent() {
		return current;
	}

	public static void start() {
		current = new ModificationBatch();
	}

	protected ModificationBatch() {
	}

}
