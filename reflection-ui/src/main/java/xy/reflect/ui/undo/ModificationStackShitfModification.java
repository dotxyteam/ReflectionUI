package xy.reflect.ui.undo;

import xy.reflect.ui.info.IInfo;

public class ModificationStackShitfModification extends AbstractModification {

	protected ModificationStack modificationStack;
	protected int offset;
	protected String title;

	public ModificationStackShitfModification(ModificationStack modificationStack, int offset, String title,
			IInfo target) {
		super(target);
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

}