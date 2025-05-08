
package xy.reflect.ui.control;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control data that handles value access errors by notifying them and
 * returning the last valid value.
 * 
 * When an update of a field value fails, the error is caught and thrown when
 * the the current control (normally) tries to refresh its state because of this
 * update that it initiated itself. With most controls it allows the user to
 * continue modifying the erroneous value until it becomes correct, while
 * viewing the error message. Otherwise, control would likely revert to the
 * state corresponding to the last valid value, preventing the user from
 * completing its correction. Note that the update value error is only thrown
 * once and discarded so that the current control updates its state and drops
 * the error message if another control performs an action that triggers current
 * form refreshment.
 * 
 * @author olitank
 *
 */
public class ErrorHandlingFieldControlData extends FieldControlDataProxy {

	protected SwingRenderer swingRenderer;
	protected JComponent errorDialogOwner;

	protected Object lastFieldValue;
	protected boolean lastFieldValueInitialized = false;
	protected Throwable lastValueUpdateError;
	protected String currentlyDisplayedErrorId;

	public ErrorHandlingFieldControlData(IFieldControlData data, SwingRenderer swingRenderer,
			JComponent errorDialogOwner) {
		super(data);
		this.swingRenderer = swingRenderer;
		this.errorDialogOwner = errorDialogOwner;
	}

	@Override
	public Object getValue() {
		try {
			if (lastValueUpdateError != null) {
				Throwable toThrow = lastValueUpdateError;
				lastValueUpdateError = null;
				throw toThrow;
			}
			try {
				lastFieldValue = super.getValue();
				lastFieldValueInitialized = true;
				handleError(null);
			} catch (ErrorWithDefaultValue e) {
				lastFieldValue = e.getDefaultValue();
				lastFieldValueInitialized = true;
				throw e.getError();
			}
		} catch (final Throwable t) {
			if (!lastFieldValueInitialized) {
				ITypeInfo type = getType();
				if (!type.supports(null)) {
					try {
						lastFieldValue = ReflectionUIUtils.createDefaultInstance(type, true);
					} catch (Throwable ignore) {
						throw new ReflectionUIError(t);
					}
				}
				lastFieldValueInitialized = true;
			}
			handleError(t);
		}
		return lastFieldValue;

	}

	@Override
	public void setValue(Object newValue) {
		try {
			lastFieldValue = newValue;
			super.setValue(newValue);
			lastValueUpdateError = null;
		} catch (Throwable t) {
			lastValueUpdateError = t;
		}
	}

	/**
	 * Called to notify an error.
	 * 
	 * @param t The exception that was thrown or null if the error is gone.
	 */
	protected void handleError(final Throwable t) {
		final String newErrorId = (t == null) ? null : t.toString();
		if (MiscUtils.equalsOrBothNull(newErrorId, currentlyDisplayedErrorId)) {
			return;
		}
		currentlyDisplayedErrorId = newErrorId;
		if (t != null) {
			errorDialogOwner.setBorder(BorderFactory.createCompoundBorder(errorDialogOwner.getBorder(),
					SwingRendererUtils.getErrorBorder()));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showErrorDialog(t);
				}
			});
		} else {
			errorDialogOwner.setBorder(((CompoundBorder) errorDialogOwner.getBorder()).getOutsideBorder());
		}
	}

	protected void showErrorDialog(Throwable t) {
		swingRenderer.handleException(errorDialogOwner, new ReflectionUIError(
				((getCaption().length() > 0) ? ("'" + getCaption() + "' error: ") : "") + t.toString(), t));
	}

}
