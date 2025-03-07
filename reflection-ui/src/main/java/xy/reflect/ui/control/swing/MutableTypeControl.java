
package xy.reflect.ui.control.swing;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.ErrorOccurrence;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.builder.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;

/**
 * Field control that detects and rejects ({@link #refreshUI(boolean)} will
 * return false) null and unsupported values (type not compatible). A sub-form
 * is used to display the non-null supported value.
 * 
 * Note that this control is used when
 * {@link IFieldControlData#isNullValueDistinct()} returns false. Which means
 * that it prevents its sub-control from encountering null value. The null value
 * allows to destroy the current control and pick a more suitable one.
 * 
 * 
 * @author olitank
 *
 */
public class MutableTypeControl extends NullableControl {

	private static final long serialVersionUID = 1L;

	protected static final String MUTABLE_TYPE_CONTROL_ALREADY_CREATED_PROPERTY_KEY = MutableTypeControl.class.getName()
			+ ".MUTABLE_TYPE_CONTROL_ALREADY_CREATED_PROPERTY_KEY";

	protected boolean recreationNeeded = false;

	public MutableTypeControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, input);
	}

	@Override
	protected AbstractEditorFormBuilder createSubFormBuilder(SwingRenderer swingRenderer, IFieldControlInput input,
			IContext subContext, Listener<Throwable> commitExceptionHandler) {
		final MutableTypeControl parent = (MutableTypeControl) input.getControlData().getSpecificProperties()
				.get(MUTABLE_TYPE_CONTROL_ALREADY_CREATED_PROPERTY_KEY);
		if (parent != null) {
			parent.recreationNeeded = true;
		}
		return new SubFormBuilder(swingRenderer, this, input, subContext, commitExceptionHandler) {

			@Override
			protected Map<String, Object> getEncapsulatedFieldSpecificProperties() {
				Map<String, Object> result = new HashMap<String, Object>(
						super.getEncapsulatedFieldSpecificProperties());
				result.put(MUTABLE_TYPE_CONTROL_ALREADY_CREATED_PROPERTY_KEY, MutableTypeControl.this);
				return result;
			}

			@Override
			public Form createEditorForm(boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
				if (parent != null) {
					return new Form(swingRenderer, new Object(), IInfoFilter.DEFAULT);
				} else {
					return super.createEditorForm(realTimeLinkWithParent, exclusiveLinkWithParent);
				}
			}

		};
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		Object value = ErrorOccurrence.tryCatch(new Accessor<Object>() {
			@Override
			public Object get() {
				return data.getValue();
			}
		});
		if (value == null) {
			return false;
		}
		if (!(value instanceof ErrorOccurrence)) {
			if (!data.getType().supports(value)) {
				return false;
			}
		}
		data.addInBuffer(value);
		boolean result = super.refreshUI(refreshStructure);
		if (refreshStructure) {
			if (data.getCaption().length() == 0) {
				((JComponent) currentSubControl).setBorder(null);
			}
		}
		if (recreationNeeded) {
			result = false;
		}
		nullStatusControl.setVisible(false);
		return result;
	}

	@Override
	protected boolean isCaptionDisplayedOnNullStatusControl() {
		return false;
	}

	@Override
	protected IContext getSubContext() {
		return new CustomContext("MutableInstance");
	}

	@Override
	public String toString() {
		return "MutableTypeControl [data=" + data + "]";
	}
}
