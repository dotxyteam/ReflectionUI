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
 * Specifies how to order the execution of undo modifications according to the
 * source do/redo modifications order.
 * 
 * @author olitank
 *
 */
public enum UndoOrder {
	/**
	 * See {@link #getNormal()}.
	 */
	FIFO,

	/**
	 * See {@link #getInverse()}.
	 */
	LIFO;

	/**
	 * @return the {@link #LIFO} enum constant specifying that undo modifications
	 *         order should be the same as the source do/redo modifications order.
	 */
	public static UndoOrder getNormal() {
		return LIFO;
	}

	/**
	 * @return the {@link #FIFO} enum constant specifying that undo modifications
	 *         order should be reverted according to the source do/redo
	 *         modifications order.
	 */
	public static UndoOrder getInverse() {
		return FIFO;
	}
};
