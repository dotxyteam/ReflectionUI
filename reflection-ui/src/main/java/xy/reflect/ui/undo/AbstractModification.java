


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
