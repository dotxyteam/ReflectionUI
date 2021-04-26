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

/**
 * This is the base interface of every reversible object state modification made
 * through an abstract UI model element. Note that it simulates a control action
 * and then should operate at the same level as UI controls (eventually manage
 * busy indication, ...).
 * 
 * @author olitank
 *
 */
public interface IModification {

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 * Represents a null (no impact on the object state) modification. It just
	 * returns true when {@link #isNull()} is called.
	 */
	IModification NULL_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite() {
			return NULL_MODIFICATION;
		}

		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public boolean isFake() {
			return false;
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

	/**
	 * Dummy instance of this class made for utilitarian purposes.
	 * Represents a null (no impact on the object state) modification. It just
	 * returns true when {@link #isFake()} is called.
	 */
	IModification FAKE_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite() {
			return FAKE_MODIFICATION;
		}

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public boolean isFake() {
			return true;
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "FAKE_MODIFICATION";
		}

	};;

	/**
	 * Applies the current modification.
	 * 
	 * @return the opposite modification.
	 * @throws IrreversibleModificationException When it appears that the
	 *                                           modification cannot be reverted.
	 */
	IModification applyAndGetOpposite() throws IrreversibleModificationException;

	/**
	 * @return true if and only if this modification should be considered as empty,
	 *         with no impact on the target object state or its associated
	 *         {@link ModificationStack} and listeners.
	 */
	boolean isNull();

	/**
	 * @return true if and only if this modification should be considered as fake,
	 *         with no impact on the target object state but with impact on its
	 *         associated {@link ModificationStack} listeners that should receive
	 *         notifications.
	 */
	boolean isFake();

	/**
	 * @return the title of this modification.
	 */
	String getTitle();
}
