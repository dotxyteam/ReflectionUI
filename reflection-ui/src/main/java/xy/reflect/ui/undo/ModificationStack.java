package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This is an undo management class. it allows to undo/redo actions performed
 * using instances of {@link IModification}.
 * 
 * Objects should be exclusively modified through the same modification stack.
 * If a modification occurs but cannot be logged in the modification stack for
 * any reason, then {@link #invalidate()} should be called to inform the
 * modification stack.
 * 
 * @author nikolat
 *
 */
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

	/**
	 * Constructs a modification stack haing the specified name.
	 * 
	 * @param name
	 */
	public ModificationStack(String name) {
		this.name = name;
	}

	/**
	 * @return the name of this modification stack.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return whether this modification stack is currently invalidated.
	 */
	public boolean isInvalidated() {
		return invalidated;
	}

	/**
	 * @return whether this modification stack has been at least once invalidated.
	 */
	public boolean wasInvalidated() {
		return wasInvalidated;
	}

	/**
	 * Adds the specified modification stack listener.
	 * 
	 * @param listener
	 */
	public void addListener(IModificationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the specified modification stack listener.
	 * 
	 * @param listener
	 */
	public void removeListener(IModificationListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return modification stack listeners.
	 */
	public IModificationListener[] getListeners() {
		return listeners.toArray(new IModificationListener[listeners.size()]);
	}

	/**
	 * Executes the specified modification and stores its opposite modification in
	 * the undo stack.
	 * 
	 * @param modification
	 */
	public void apply(IModification modification) {
		try {
			pushUndo(modification.applyAndGetOpposite());
		} catch (IrreversibleModificationException e) {
			invalidate();
		}
	}

	/**
	 * Stores the specified modification undo modification in the undo stack.
	 * 
	 * @param undoModification
	 * @return
	 */
	public boolean pushUndo(IModification undoModification) {
		if (undoModification.isNull()) {
			return false;
		}
		if (compositeStack.size() > 0) {
			compositeStack.peek().pushUndo(undoModification);
			return true;
		}
		validate();
		undoStack.push(undoModification);
		redoStack.clear();
		allListenersProxy.handlePush(undoModification);
		return true;
	}

	/**
	 * @return the number of remaining undo modifications.
	 */
	public int getUndoSize() {
		return undoStack.size();
	}

	/**
	 * @return the number of remaining redo modifications.
	 */
	public int getRedoSize() {
		return redoStack.size();
	}

	/**
	 * Execute the next undo modification.
	 * 
	 * @throws ReflectionUIError
	 *             If there is no remaining undo modification or if a composite
	 *             modification is being created.
	 */
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

	/**
	 * Execute the next redo modification.
	 * 
	 * @throws ReflectionUIError
	 *             If there is no remaining redo modification or if a composite
	 *             modification is being created.
	 */
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

	/**
	 * Execute all the undo modifications.
	 */
	public void undoAll() {
		while (undoStack.size() > 0) {
			undo();
		}
	}

	/**
	 * @return the stack of undo modifications.
	 */
	public IModification[] getUndoModifications() {
		return undoStack.toArray(new IModification[undoStack.size()]);
	}

	/**
	 * @return the stack of redo modifications.
	 */
	public IModification[] getRedoModifications() {
		return redoStack.toArray(new IModification[redoStack.size()]);
	}

	/**
	 * Begins the creation of a composite modification. Following this method call,
	 * all the modifications that will be added to this stack will be packed into a
	 * unique modification until the call of
	 * {@link #endComposite(String, UndoOrder)} to finalize the composite
	 * modification creation or {@link #abortComposite()} to cancel it. Note that
	 * calling this method multiple times before making the related calls to
	 * {@link #endComposite(String, UndoOrder)} or {@link #abortComposite()} will
	 * result in the creation of inner composite modifications.
	 */
	public void beginComposite() {
		if (!isInComposite()) {
			validate();
		}
		compositeStack.push(new ModificationStack("(composite level " + compositeStack.size() + ") " + name));
	}

	/**
	 * @return true if a call to {@link #beginComposite()} have been performed but
	 *         the call to the related {@link #endComposite(String, UndoOrder)} or
	 *         {@link #abortComposite()} has not been performed yet.
	 */
	public boolean isInComposite() {
		return compositeStack.size() > 0;
	}

	/**
	 * @param title
	 *            The composite modification title.
	 * @param order
	 *            The composite modification undo order.
	 * @return true if a potential modification was detected since the call of
	 *         {@link #beginComposite()}. Note that true will also be returned if
	 *         the composite modification creation have been aborted because of an
	 *         invalidation.
	 */
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

	/**
	 * Cancels a composite modification creation initiated by a preceding call to
	 * {@link #beginComposite()}.
	 */
	public void abortComposite() {
		compositeStack.pop();
	}

	/**
	 * Convenient composite modification creation method to that calls
	 * {@link #beginComposite()}, performs the specified action and call
	 * {@link #endComposite(String, UndoOrder)} or {@link #abortComposite()}.
	 * 
	 * @param title
	 *            The composite modification title.
	 * @param order
	 *            The composite modification undo order.
	 * @param action
	 *            The method {@link Accessor#get()} will be called from this object
	 *            before the current method returns. It should push the children
	 *            undo modifications in the current modification stack and return
	 *            true if a potential modification is detected.
	 * @return whether a potential modification was detected.
	 */
	public boolean insideComposite(String title, UndoOrder order, Accessor<Boolean> action) {
		beginComposite();
		boolean modificationDetected;
		try {
			modificationDetected = action.get();
		} catch (Throwable t) {
			invalidate();
			abortComposite();
			throw new ReflectionUIError(t);
		}
		if (modificationDetected) {
			return endComposite(title, order);
		} else {
			abortComposite();
			return false;
		}

	}

	/**
	 * Informs the modification stack of the current undo management inconsistency.
	 * Subsequently {@link #isInvalidated()} will return true and the undo and redo
	 * stacks will be emptied to ensure that the undo management remains consistent.
	 * This invalidation state will be cleared if an undo modification gets added
	 * afterwards.
	 */
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

	/**
	 * Resets the modification stack. Unlike the method {@link #invalidate()}
	 * calling {@link #isInvalidated()} and {@link #wasInvalidated()} will be set to
	 * return false.
	 */
	public void forget() {
		if (compositeStack.size() > 0) {
			throw new ReflectionUIError("Cannot forget while composite modification creation is ongoing");
		}
		undoStack.clear();
		redoStack.clear();
		invalidated = wasInvalidated = false;
		allListenersProxy.handleInvalidate();
	}

	/**
	 * @return whether there are remaining undo modifications.
	 */
	public Boolean canUndo() {
		return (undoStack.size() > 0) && !isInvalidated();
	}

	/**
	 * @return whether there are remaining redo modifications.
	 */
	public Boolean canRedo() {
		return (redoStack.size() > 0) && !isInvalidated();
	}

	/**
	 * @return whether the objects managed by this modification stack can be
	 *         reverted to their initial state (in other terms, there are remaining
	 *         undo modifications and the modification stack was never invalidated).
	 */
	public Boolean canReset() {
		return canUndo() && !wasInvalidated();
	}

	/**
	 * @return whether objects managed by this modification stack are in their
	 *         initial state (in other terms, there are no remaining undo
	 *         modifications and the modification stack was never invalidated).
	 */
	public boolean isNull() {
		if (undoStack.size() > 0) {
			return false;
		}
		if (wasInvalidated()) {
			return false;
		}
		return true;
	}

	/**
	 * @param title
	 *            The title of the new composite modification.
	 * @return a composite modification containing the current undo modification
	 *         stack.
	 */
	public IModification toCompositeUndoModification(String title) {
		return new CompositeModification(title, UndoOrder.getNormal(), getUndoModifications());
	}

	/**
	 * @return a number identifying the current state of all the objects managed by
	 *         this modification stack. If this value does not change between 2
	 *         calls then the managed objects have not changed or their changes have
	 *         successfully been reverted.
	 */
	public long getStateVersion() {
		return stateVersion;
	}

	@Override
	public String toString() {
		return ModificationStack.class.getSimpleName() + "[" + name + "]";
	}

}