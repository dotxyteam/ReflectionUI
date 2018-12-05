/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.undo;

/**
 * {@link ModificationStack} listener interface.
 * 
 * @author nikolat
 *
 */
public interface IModificationListener {

	/**
	 * Called after the execution of
	 * {@link ModificationStack#pushUndo(IModification)}.
	 * 
	 * @param undoModification
	 *            The parameter passed to
	 *            {@link ModificationStack#pushUndo(IModification)}.
	 */
	void handlePush(IModification undoModification);

	/**
	 * Called after the execution of {@link ModificationStack#undo()}.
	 * 
	 * @param undoModification
	 *            The undo modification that was executed.
	 */
	void handleUdno(IModification undoModification);

	/**
	 * Called after the execution of {@link ModificationStack#redo()}.
	 * 
	 * @param modification
	 *            The modification that was executed.
	 */
	void handleRedo(IModification modification);

	/**
	 * Called after the execution of {@link ModificationStack#invalidate()} or
	 * {@link ModificationStack#forget()}.
	 */
	void handleInvalidate();

	/**
	 * Called after {@link ModificationStack#isInvalidated()} return value changes
	 * from true to false. {@link ModificationStack#forget()}.
	 */
	void handleInvalidationCleared();
}
