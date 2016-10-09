package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;

public class ModificationStack {

	protected static final String UNDO_TITLE_PREFIX = "(Revert) ";

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

	protected IModificationListener ALL_LISTENERS = new IModificationListener() {

		@Override
		public void handleDo(IModification modification) {
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleDo(modification);
			}
		}

		@Override
		public void handleUdno(IModification undoModification) {
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleUdno(undoModification);
			}
		}

		@Override
		public void handleRedo(IModification modification) {
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleRedo(modification);
			}
		}

		@Override
		public void handleInvalidate() {
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleInvalidate();
			}
		}

	};

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
			ALL_LISTENERS.handleDo(undoModif);
		}
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
		ALL_LISTENERS.handleUdno(undoModif);
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
		ALL_LISTENERS.handleRedo(modif);
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

	public void endComposite(IInfo target, String title, UndoOrder order) {
		CompositeModification compositeUndoModif = new CompositeModification(target, getUndoTitle(title), order,
				compositeStack.pop().getUndoModifications(order));
		ModificationStack compositeParent;
		if (compositeStack.size() > 0) {
			compositeParent = compositeStack.peek();
		} else {
			compositeParent = this;
		}
		compositeParent.pushUndo(compositeUndoModif);
	}

	public boolean insideComposite(IInfo target, String title, UndoOrder order, Accessor<Boolean> compositeValidated) {
		beginComposite();
		try {
			if (compositeValidated.get()) {
				endComposite(target, title, order);
				return true;
			} else {
				cancelComposite();
				return false;
			}
		} catch (Throwable t) {
			cancelComposite();
			throw new ReflectionUIError(t);
		}

	}

	public void cancelComposite() {
		compositeStack.pop();
	}

	public void invalidate() {
		redoStack.clear();
		undoStack.clear();
		compositeStack.clear();
		invalidated = true;
		ALL_LISTENERS.handleInvalidate();
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

	public boolean isNull() {
		if (getNumberOfUndoUnits() > 0) {
			return false;
		}
		if (isInvalidated()) {
			return false;
		}
		return true;
	}

	public IModification toCompositeModification(IInfo target, String title) {
		return new CompositeModification(target, title, UndoOrder.LIFO, getUndoModifications(UndoOrder.LIFO));
	}

}