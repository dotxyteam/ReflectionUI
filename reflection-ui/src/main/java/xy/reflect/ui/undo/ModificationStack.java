/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.undo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This is an undo management class. it allows to undo/redo actions performed
 * using instances of {@link IModification}.
 * 
 * An object should be exclusively modified through the same modification stack
 * to preserve its consistency. If a modification occurs but cannot be logged in
 * the modification stack for any reason, then {@link #invalidate()} should be
 * called to inform the modification stack and its clients.
 * 
 * @author olitank
 *
 */
public class ModificationStack {

	protected Stack<IModification> undoStack = new Stack<IModification>();
	protected Stack<IModification> redoStack = new Stack<IModification>();
	protected String name;
	protected int maximumSize = 25;
	protected Stack<ModificationStack> compositeStack = new Stack<ModificationStack>();
	protected List<IModificationListener> listeners = new ArrayList<IModificationListener>();
	protected boolean invalidated = false;
	protected boolean wasInvalidated = false;
	protected boolean exhaustive = true;
	protected long stateVersion = 0;
	protected boolean eventFiringEnabled = true;
	protected Filter<IModification> pushFilter;

	protected IModificationListener internalListener = new IModificationListener() {

		@Override
		public void afterUndo(IModification undoModification) {
			stateVersion--;
		}

		@Override
		public void afterRedo(IModification modification) {
			stateVersion++;
		}

		@Override
		public void afterPush(IModification undoModification) {
			stateVersion++;
		}

		@Override
		public void afterClearInvalidation() {
		}

		@Override
		public void afterInvalidate() {
			stateVersion++;
		}

	};
	protected IModificationListener allListenersProxy = new IModificationListener() {

		@Override
		public void afterPush(IModification undoModification) {
			internalListener.afterPush(undoModification);
			if (!eventFiringEnabled) {
				return;
			}
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.afterPush(undoModification);
			}
		}

		@Override
		public void afterUndo(IModification undoModification) {
			internalListener.afterUndo(undoModification);
			if (!eventFiringEnabled) {
				return;
			}
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.afterUndo(undoModification);
			}
		}

		@Override
		public void afterRedo(IModification modification) {
			internalListener.afterRedo(modification);
			if (!eventFiringEnabled) {
				return;
			}
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.afterRedo(modification);
			}
		}

		@Override
		public void afterInvalidate() {
			internalListener.afterInvalidate();
			if (!eventFiringEnabled) {
				return;
			}
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.afterInvalidate();
			}
		}

		@Override
		public void afterClearInvalidation() {
			internalListener.afterClearInvalidation();
			if (!eventFiringEnabled) {
				return;
			}
			for (IModificationListener listener : new ArrayList<IModificationListener>(
					ModificationStack.this.listeners)) {
				listener.afterClearInvalidation();
			}
		}

	};

	/**
	 * Constructs a modification stack having the specified name.
	 * 
	 * @param name The name.
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
	 * @return the maximum size of the stack. Note that once this size is exceeded,
	 *         {@link #wasInvalidated()} will return true.
	 */
	public int getMaximumSize() {
		return maximumSize;
	}

	/**
	 * Updates the maximum size of the stack. Note that once this size is exceeded,
	 * {@link #wasInvalidated()} will return true.
	 * 
	 * @param maximumSize The new maximum size value.
	 */
	public void setMaximumSize(int maximumSize) {
		if (maximumSize <= 0) {
			throw new ReflectionUIError();
		}
		this.maximumSize = maximumSize;
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
	 * @return false if a fake modification ({@link IModification#isFake()} == true)
	 *         has been submitted at least once to this modification stack (means
	 *         that some listeners may need to be informed of the occurrence of
	 *         non-memorized events about object state change), true otherwise.
	 */
	public boolean isExhaustive() {
		return exhaustive;
	}

	/**
	 * Adds the specified listener to the modification stack.
	 * 
	 * @param listener The listener.
	 */
	public void addListener(IModificationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the specified listener from the modification stack.
	 * 
	 * @param listener The listener.
	 */
	public void removeListener(IModificationListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return all the modification stack listeners.
	 */
	public IModificationListener[] getListeners() {
		return listeners.toArray(new IModificationListener[listeners.size()]);
	}

	/**
	 * @return whether listeners are enabled or not.
	 */
	public boolean isEventFiringEnabled() {
		return eventFiringEnabled;
	}

	/**
	 * Enables/disables listeners.
	 * 
	 * @param eventFiringEnabled Is true to enable listeners, false to disable
	 *                           listeners.
	 */
	public void setEventFiringEnabled(boolean eventFiringEnabled) {
		this.eventFiringEnabled = eventFiringEnabled;
	}

	/**
	 * @return a filter used to alter modifications before adding them on the undo
	 *         stack or null.
	 */
	public Filter<IModification> getPushFilter() {
		return pushFilter;
	}

	/**
	 * Changes the filter used to alter modifications before adding them on the undo
	 * stack.
	 * 
	 * @param pushFilter The new filter or null.
	 */
	public void setPushFilter(Filter<IModification> pushFilter) {
		this.pushFilter = pushFilter;
	}

	/**
	 * Executes the specified modification and pushes its opposite modification on
	 * the undo stack (calls {@link #push(IModification)}).
	 * 
	 * @param modification The modification that must be executed.
	 */
	public void apply(IModification modification) {
		try {
			push(modification.applyAndGetOpposite());
		} catch (IrreversibleModificationException e) {
			invalidate();
		}
	}

	/**
	 * Normally stores the specified undo modification on the undo stack and clears
	 * redo stack.
	 * 
	 * If there is a 'push' filter (see {@link #getPushFilter()}) then the specified
	 * undo modification will be filtered before being stored.
	 * 
	 * If the specified undo modification is null (see
	 * {@link IModification#isNull()}) then it will not be stored and false will be
	 * returned.
	 * 
	 * If the specified undo modification is fake (see
	 * {@link IModification#isFake()}) then it will not be stored and this
	 * modification stack will be marked as non-exhaustive (see
	 * {@link #isExhaustive()}).
	 * 
	 * If there is a current composite stack (see {@link #isInComposite()}) then the
	 * specified undo modification will rather be stored on it.
	 * 
	 * @param undoModification The undo modification that should be pushed on the
	 *                         undo stack.
	 * @return true only and only if the specified undo modification is not null.
	 */
	public boolean push(IModification undoModification) {
		if (undoModification.isNull()) {
			return false;
		}
		if (pushFilter != null) {
			undoModification = pushFilter.get(undoModification);
		}
		if (compositeStack.size() > 0) {
			compositeStack.peek().push(undoModification);
			return true;
		}
		validate();
		if (undoModification.isFake()) {
			exhaustive = false;
		} else {
			undoStack.push(undoModification);
		}
		if (undoStack.size() > maximumSize) {
			undoStack.remove(0);
			wasInvalidated = true;
		}
		if (!undoModification.isFake()) {
			redoStack.clear();
		}
		allListenersProxy.afterPush(undoModification);
		return true;
	}

	/**
	 * @return the next modification that will be executed when calling
	 *         {@link ModificationStack#undo()}.
	 */
	public IModification getNextUndoModification() {
		if (undoStack.size() == 0) {
			return null;
		}
		return undoStack.peek();
	}

	/**
	 * @return the next modification that will be executed when calling
	 *         {@link ModificationStack#redo()}.
	 */
	public IModification getNextRedoModification() {
		if (redoStack.size() == 0) {
			return null;
		}
		return redoStack.peek();
	}

	/**
	 * Executes the next undo modification (modification on the top of the undo
	 * stack) if found.
	 * 
	 * @throws ReflectionUIError If a composite modification is being created.
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
		allListenersProxy.afterUndo(undoModif);
	}

	/**
	 * Executes the next redo modification (modification on the top of the redo
	 * stack) if found.
	 * 
	 * @throws ReflectionUIError If a composite modification is being created.
	 */
	public void redo() {
		if (compositeStack.size() > 0) {
			throw new ReflectionUIError("Cannot redo while composite modification creation is ongoing");
		}
		if (redoStack.size() == 0) {
			return;
		}
		IModification redoModif = redoStack.pop();
		try {
			undoStack.push(redoModif.applyAndGetOpposite());
		} catch (IrreversibleModificationException e) {
			invalidate();
			return;
		}
		allListenersProxy.afterRedo(redoModif);
	}

	/**
	 * Executes all the undo modifications (calls {@link #undo()} until
	 * {@link #getNextUndoModification()} returns null).
	 */
	public void undoAll() {
		while (getNextUndoModification() != null) {
			undo();
		}
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
	 * @param title The composite modification title.
	 * @param order The composite modification undo order.
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
		CompositeModification topCompositeUndoModif;
		{
			List<IModification> list = new ArrayList<IModification>(topComposite.undoStack);
			if (order == UndoOrder.getNormal()) {
				Collections.reverse(list);
			}
			topCompositeUndoModif = new CompositeModification(AbstractModification.getUndoTitle(title), order,
					list.toArray(new IModification[list.size()]));
		}
		if (!topComposite.isExhaustive()) {
			compositeParent.exhaustive = false;
		}
		return compositeParent.push(topCompositeUndoModif);
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
	 * @param title  The composite modification title.
	 * @param order  The composite modification undo order.
	 * @param action The method {@link Accessor#get()} will be called from this
	 *               parameter object before the current method returns. It should
	 *               push the children undo modifications in the current
	 *               modification stack and return true if a potential modification
	 *               is detected.
	 * @return whether a potential modification was detected.
	 */
	public boolean insideComposite(String title, UndoOrder order, Accessor<Boolean> action) {
		beginComposite();
		boolean modificationDetected;
		try {
			modificationDetected = action.get();
		} catch (Throwable t) {
			try {
				invalidate();
			} catch (Throwable ignore) {
			}
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
		allListenersProxy.afterInvalidate();
	}

	protected void validate() {
		if (invalidated) {
			redoStack.clear();
			undoStack.clear();
			compositeStack.clear();
			invalidated = false;
			allListenersProxy.afterClearInvalidation();
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
		invalidate();
		validate();
		wasInvalidated = false;
		exhaustive = true;
	}

	/**
	 * @return whether there are remaining undo modifications.
	 */
	public Boolean canUndo() {
		return !isInvalidated() && (getNextUndoModification() != null);
	}

	/**
	 * @return whether there are remaining redo modifications.
	 */
	public Boolean canRedo() {
		return !isInvalidated() && (getNextRedoModification() != null);
	}

	/**
	 * @return whether the objects managed by this modification stack can be
	 *         reverted to their initial state (in other terms, there are remaining
	 *         undo modifications and the modification stack was never invalidated).
	 */
	public Boolean canReset() {
		return !wasInvalidated() && canUndo();
	}

	/**
	 * @return whether objects managed by this modification stack are in their
	 *         initial state (in other terms, there are no remaining undo
	 *         modifications and the modification stack was never invalidated).
	 */
	public boolean isInitial() {
		if (getNextUndoModification() != null) {
			return false;
		}
		if (wasInvalidated()) {
			return false;
		}
		return true;
	}

	/**
	 * @param title The title of the new composite modification.
	 * @return a composite modification containing the current undo modification
	 *         stack elements.
	 */
	public CompositeModification toCompositeUndoModification(String title) {
		return new CompositeModification(title, UndoOrder.getNormal(),
				undoStack.toArray(new IModification[undoStack.size()]));
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
