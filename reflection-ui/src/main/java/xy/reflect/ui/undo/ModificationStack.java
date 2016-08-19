package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class ModificationStack {

	protected static final String UNDO_TITLE_PREFIX = "(Revert) ";

	public static final IModification EMPTY_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite() {
			return EMPTY_MODIFICATION;
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
	public static final IModification INVALID_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite() {
			return INVALID_MODIFICATION;
		}

		@Override
		public int getNumberOfUnits() {
			return 1;
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "INVALID_MODIFICATION";
		}

	};

	public static final ModificationStack NULL_MODIFICATION_STACK = new ModificationStack(null) {

		@Override
		public void pushUndo(IModification undoModif) {
		}

		@Override
		public void undo() {
		}

		@Override
		public void redo() {
		}

	};

	protected Stack<IModification> undoStack = new Stack<IModification>();
	protected Stack<IModification> redoStack = new Stack<IModification>();
	protected String name;
	protected Stack<ModificationStack> compositeStack = new Stack<ModificationStack>();
	protected List<IModificationListener> listeners = new ArrayList<IModificationListener>();
	protected boolean invalidated = false;

	public ModificationStack(String name) {
		this.name = name;
	}

	public List<IModificationListener> getListeners() {
		return listeners;
	}

	public String getName() {
		return name;
	}

	public boolean isInvalidated() {
		return invalidated;
	}

	public void addListener(IModificationListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IModificationListener listener) {
		listeners.remove(listener);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + name + ")";
	}

	public void apply(IModification modif) {
		pushUndo(modif.applyAndGetOpposite());
	}

	public void pushUndo(IModification undoModif) {
		if (compositeStack.size() > 0) {
			compositeStack.peek().pushUndo(undoModif);
			return;
		}
		if (undoModif.getNumberOfUnits() > 0) {
			undoStack.push(undoModif);
			redoStack.clear();
		}
		notifyListeners(IModificationListener.DO_EVENT);
	}

	public int getUndoSize() {
		return undoStack.size();
	}

	public int getRedoSize() {
		return redoStack.size();
	}

	public int getNumberOfUndoUnits() {
		int result = 0;
		for (IModification undoModif : undoStack) {
			result += undoModif.getNumberOfUnits();
		}
		return result;
	}

	public int getNumberOfRedoUnits() {
		int result = 0;
		for (IModification redoModif : redoStack) {
			result += redoModif.getNumberOfUnits();
		}
		return result;
	}

	public void undo() {
		if (compositeStack.size() > 0) {
			compositeStack.peek().undo();
			return;
		}
		if (undoStack.size() == 0) {
			return;
		}
		IModification undoModif = undoStack.pop();
		redoStack.push(undoModif.applyAndGetOpposite());
		notifyListeners(IModificationListener.UNDO_EVENT);
	}

	public void redo() {
		if (compositeStack.size() > 0) {
			compositeStack.peek().redo();
			return;
		}
		if (redoStack.size() == 0) {
			return;
		}
		IModification modif = redoStack.pop();
		undoStack.push(modif.applyAndGetOpposite());
		notifyListeners(IModificationListener.REDO_EVENT);
	}

	public void undoAll() {
		if (compositeStack.size() > 0) {
			compositeStack.peek().undoAll();
			return;
		}
		while (undoStack.size() > 0) {
			undo();
		}
	}

	public IModification[] getUndoModifications(UndoOrder order) {
		List<IModification> list = new ArrayList<IModification>(undoStack);
		if (order == UndoOrder.LIFO) {
			Collections.reverse(list);
		}
		return list.toArray(new IModification[list.size()]);
	}
	
	public IModification[] getRedoModifications(UndoOrder order) {
		List<IModification> list = new ArrayList<IModification>(redoStack);
		if (order == UndoOrder.LIFO) {
			Collections.reverse(list);
		}
		return list.toArray(new IModification[list.size()]);
	}


	public void beginComposite() {
		compositeStack.push(new ModificationStack("(composite level " + compositeStack.size() + ") " + name));
	}

	public void endComposite(String title, UndoOrder order) {
		CompositeModification compositeUndoModif = new CompositeModification(getUndoTitle(title), order,
				compositeStack.pop().getUndoModifications(order));
		ModificationStack compositeParent;
		if (compositeStack.size() > 0) {
			compositeParent = compositeStack.peek();
		} else {
			compositeParent = this;
		}
		compositeParent.pushUndo(compositeUndoModif);
	}

	public void cancelComposite() {
		compositeStack.pop();
	}

	public void invalidate() {
		redoStack.clear();
		undoStack.clear();
		compositeStack.clear();
		invalidated = true;
		notifyListeners(IModificationListener.INVALIDATE_EVENT);
	}

	protected void notifyListeners(Object event) {
		for (IModificationListener listener : new ArrayList<IModificationListener>(listeners)) {
			listener.handleEvent(event);
		}
	}

	public Boolean canRedo() {
		return redoStack.size() > 0;
	}

	public Boolean canUndo() {
		return undoStack.size() > 0;
	}

	public static String getUndoTitle(String title) {
		String result;
		if (title == null) {
			result = null;
		} else if (title.startsWith(UNDO_TITLE_PREFIX)) {
			result = title.substring(UNDO_TITLE_PREFIX.length());
		} else {
			result = UNDO_TITLE_PREFIX + title;
		}
		return result;
	}
	
	

}