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

public abstract class AbstractModification implements IModification {

	protected String title;
	protected Runnable doJob;
	protected Runnable undoJob;
	protected static final String UNDO_TITLE_PREFIX = "(Revert) ";

	protected abstract Runnable createDoJob();

	protected abstract Runnable createUndoJob();

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public IModification applyAndGetOpposite() {
		if (doJob == null) {
			doJob = createDoJob();
		}
		if (undoJob == null) {
			undoJob = createUndoJob();
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
