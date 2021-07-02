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

import xy.reflect.ui.util.ReflectionUIError;

/**
 * This class exists as convenience for creating {@link IModification} objects.
 * 
 * @author olitank
 *
 */
public abstract class AbstractModification implements IModification {

	protected Runnable doJob;
	protected Runnable undoJob;
	protected static final String UNDO_TITLE_PREFIX = "(Revert) ";

	/**
	 * @return The task that performs the modification. Must not be null.
	 */
	protected abstract Runnable createDoJob();

	/**
	 * @return The task that reverts the modification. Must not be null.
	 */
	protected abstract Runnable createUndoJob();

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isFake() {
		return false;
	}

	@Override
	public IModification applyAndGetOpposite() {
		if (doJob == null) {
			doJob = createDoJob();
			if (doJob == null) {
				throw new ReflectionUIError();
			}
		}
		if (undoJob == null) {
			try {
				undoJob = createUndoJob();
			} catch (Throwable t) {
				doJob.run();
				throw new IrreversibleModificationException();
			}
			if (undoJob == null) {
				throw new ReflectionUIError();
			}
		}

		doJob.run();

		return new OppositeModification();
	}

	@Override
	public String toString() {
		return "Modification [title=" + getTitle() + "]";
	}

	public static String getUndoTitle(String title) {
		String result;
		if (title == null) {
			result = null;
		} else if (title.startsWith(AbstractModification.UNDO_TITLE_PREFIX)) {
			result = title.substring(AbstractModification.UNDO_TITLE_PREFIX.length());
		} else {
			result = AbstractModification.UNDO_TITLE_PREFIX + title;
		}
		return result;
	}

	public class OppositeModification implements IModification {

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public boolean isFake() {
			return false;
		}

		@Override
		public String getTitle() {
			return AbstractModification.getUndoTitle(AbstractModification.this.getTitle());
		}

		public AbstractModification getSourceModification() {
			return AbstractModification.this;
		}

		@Override
		public IModification applyAndGetOpposite() {
			undoJob.run();
			return getSourceModification();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getSourceModification().hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OppositeModification other = (OppositeModification) obj;
			if (!getSourceModification().equals(other.getSourceModification()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "OppositeModification [soure=" + getSourceModification() + "]";
		}

	}
}
