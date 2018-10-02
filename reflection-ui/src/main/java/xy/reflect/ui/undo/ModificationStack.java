package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;

public class ModificationStack {

	protected Stack<IModification> undoStack = new Stack<IModification>();
	protected Stack<IModification> redoStack = new Stack<IModification>();
	protected String name;
	protected Stack<ModificationStack> compositeStack = new Stack<ModificationStack>();
	protected List<IModificationListener> listeners = new ArrayList<IModificationListener>();
	protected boolean invalidated = false;
	protected boolean wasInvalidated = false;
	protected long stateVersion = 0;

	protected IModificationListener internalListener = new IModificationListener() {

		@Override
		public void handleUdno(IModification undoModification) {
			stateVersion--;
		}

		@Override
		public void handleRedo(IModification modification) {
			stateVersion++;
		}

		@Override
		public void handlePush(IModification undoModification) {
			stateVersion++;
		}

		@Override
		public void handleInvalidationCleared() {
		}

		@Override
		public void handleInvalidate() {
			stateVersion++;
		}

	};
	protected IModificationListener allListenersProxy = new IModificationListener() {

		@Override
		public void handlePush(IModification undoModification) {
			internalListener.handlePush(undoModification);
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handlePush(undoModification);
			}
		}

		@Override
		public void handleUdno(IModification undoModification) {
			internalListener.handleUdno(undoModification);
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleUdno(undoModification);
			}
		}

		@Override
		public void handleRedo(IModification modification) {
			internalListener.handleRedo(modification);
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleRedo(modification);
			}
		}

		@Override
		public void handleInvalidate() {
			internalListener.handleInvalidate();
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleInvalidate();
			}
		}

		@Override
		public void handleInvalidationCleared() {
			internalListener.handleInvalidationCleared();
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.handleInvalidationCleared();
			}
		}

	};

	public ModificationStack(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isInvalidated() {
		return invalidated;
	}

	public boolean wasInvalidated() {
		return wasInvalidated;
	}

	public void addListener(IModificationListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IModificationListener listener) {
		listeners.remove(listener);
	}

	public IModificationListener[] getListeners() {
		return listeners.toArray(new IModificationListener[listeners.size()]);
	}

	public void apply(IModification modif) {
		try {
			pushUndo(modif.applyAndGetOpposite());
		} catch (IrreversibleModificationException e) {
			invalidate();
		}
	}

	public boolean pushUndo(IModification undoModif) {
		if (undoModif.isNull()) {
			return false;
		}
		if (compositeStack.size() > 0) {
			compositeStack.peek().pushUndo(undoModif);
			return true;
		}
		validate();
		undoStack.push(undoModif);
		redoStack.clear();
		allListenersProxy.handlePush(undoModif);
		return true;
	}

	public int getUndoSize() {
		return undoStack.size();
	}

	public int getRedoSize() {
		return redoStack.size();
	}

	public void undo() {
		if (compositeStack.size() > 0) {
			throw new ReflectionUIError("Cannot undo while composite modification creation is ongoing");
		}
		if (undoStack.size() == 0) {
			return;
		}
		IModification undoModif = undoStack.pop();
		try {
			redoStack.push(undoModif.applyAndGetOpposite());
		} catch (IrreversibleModificationException e) {
			invalidate();
			return;
		}
		allListenersProxy.handleUdno(undoModif);
	}

	public void redo() {
		if (compositeStack.size() > 0) {
			throw new ReflectionUIError("Cannot redo while composite modification creation is ongoing");
		}
		if (redoStack.size() == 0) {
			return;
		}
		IModification modif = redoStack.pop();
		try {
			undoStack.push(modif.applyAndGetOpposite());
		} catch (IrreversibleModificationException e) {
			invalidate();
			return;
		}
		allListenersProxy.handleRedo(modif);
	}

	public void undoAll() {
		while (undoStack.size() > 0) {
			undo();
		}
	}

	public IModification[] getUndoModifications() {
		return undoStack.toArray(new IModification[undoStack.size()]);
	}

	public IModification[] getRedoModifications() {
		return redoStack.toArray(new IModification[redoStack.size()]);
	}

	public void beginComposite() {
		if (!isInComposite()) {
			validate();
		}
		compositeStack.push(new ModificationStack("(composite level " + compositeStack.size() + ") " + name));
	}

	public boolean isInComposite() {
		return compositeStack.size() > 0;
	}

	public boolean endComposite(String title, UndoOrder order) {
		if (invalidated) {
			abortComposite();
			return true;
		}
		ModificationStack topComposite = compositeStack.pop();
		ModificationStack compositeParent;
		if (compositeStack.size() > 0) {
			compositeParent = compositeStack.peek();
		} else {
			compositeParent = this;
		}
		IModification[] undoModifs = topComposite.getUndoModifications();
		if (order == UndoOrder.getInverse()) {
			List<IModification> list = new ArrayList<IModification>(Arrays.asList(undoModifs));
			Collections.reverse(list);
			list.toArray(undoModifs);
		}
		CompositeModification compositeUndoModif = new CompositeModification(AbstractModification.getUndoTitle(title),
				order, undoModifs);
		return compositeParent.pushUndo(compositeUndoModif);
	}

	public void abortComposite() {
		compositeStack.pop();
	}

	public boolean insideComposite(String title, UndoOrder order, Accessor<Boolean> compositeValidated) {
		beginComposite();
		boolean ok;
		try {
			ok = compositeValidated.get();
		} catch (Throwable t) {
			invalidate();
			abortComposite();
			throw new ReflectionUIError(t);
		}
		if (ok) {
			return endComposite(title, order);
		} else {
			abortComposite();
			return false;
		}

	}

	public void invalidate() {
		wasInvalidated = invalidated = true;
		allListenersProxy.handleInvalidate();
	}

	protected void validate() {
		if (invalidated) {
			redoStack.clear();
			undoStack.clear();
			compositeStack.clear();
			invalidated = false;
			allListenersProxy.handleInvalidationCleared();
		}
	}

	public void forget() {
		if (compositeStack.size() > 0) {
			throw new ReflectionUIError("Cannot forget while composite modification creation is ongoing");
		}
		undoStack.clear();
		redoStack.clear();
		invalidated = wasInvalidated = false;
		allListenersProxy.handleInvalidate();
	}

	public Boolean canUndo() {
		return (undoStack.size() > 0) && !isInvalidated();
	}

	public Boolean canRedo() {
		return (redoStack.size() > 0) && !isInvalidated();
	}

	public Boolean canReset() {
		return canUndo() && !wasInvalidated();
	}

	public boolean isNull() {
		if (undoStack.size() > 0) {
			return false;
		}
		if (wasInvalidated()) {
			return false;
		}
		return true;
	}

	public IModification toCompositeUndoModification(String title) {
		return new CompositeModification(title, UndoOrder.getNormal(), getUndoModifications());
	}

	public long getStateVersion() {
		return stateVersion;
	}

	@Override
	public String toString() {
		return ModificationStack.class.getSimpleName() + "[" + name + "]";
	}

}