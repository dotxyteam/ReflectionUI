


package xy.reflect.ui.undo;

/**
 * Modification that calls undo() or redo() on the given modification stack
 * according to the specified offset. If the offset is positive then +offset
 * redo() calls will be performed. Otherwise -offset undo() calls will be
 * performed.
 * 
 * @author olitank
 *
 */
public class ModificationStackShitf extends AbstractModification {

	protected ModificationStack modificationStack;
	protected int offset;
	protected String title;

	public ModificationStackShitf(ModificationStack modificationStack, int offset, String title) {
		this.modificationStack = modificationStack;
		this.offset = offset;
		this.title = title;
	}

	@Override
	protected Runnable createDoJob() {
		return new Runnable() {
			@Override
			public void run() {
				if (offset > 0) {
					for (int i = 0; i < offset; i++) {
						shiftForeward();
					}
				} else {
					for (int i = 0; i < (-offset); i++) {
						shiftBackward();
					}
				}
			}
		};
	}

	@Override
	protected Runnable createUndoJob() {
		return new Runnable() {
			@Override
			public void run() {
				if (offset > 0) {
					for (int i = 0; i < offset; i++) {
						shiftBackward();
					}
				} else {
					for (int i = 0; i < (-offset); i++) {
						shiftForeward();
					}
				}
			}
		};
	}

	protected void shiftBackward() {
		modificationStack.undo();
	}

	protected void shiftForeward() {
		modificationStack.redo();
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "ModificationStackShitf [modificationStack=" + modificationStack + ", offset=" + offset + ", title="
				+ title + "]";
	}
	
	
	

}
