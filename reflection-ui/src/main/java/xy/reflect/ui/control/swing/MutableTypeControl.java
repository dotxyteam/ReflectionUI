
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
 * {@link IFieldControlData#isNullValueDistinct()} returns false. It prevents
 * its sub-control from encountering a null value. Such a null value would cause
 * the current control to be destroyed and replaced by a more suitable one.
 * 
 * The {@link #MUTABLE_TYPE_CONTROL_INFO_PROPERTY_KEY} specific property key is
 * used to prevent the creation of multiple nested mutable controls for the same
 * control input.
 * 
 * @author olitank
 *
 */
public class MutableTypeControl extends NullableControl {

	private static final long serialVersionUID = 1L;

	protected static final String MUTABLE_TYPE_CONTROL_INFO_PROPERTY_KEY = MutableTypeControl.class.getName()
			+ ".MUTABLE_TYPE_CONTROL_INFO_PROPERTY_KEY";

	protected Info info;

	public MutableTypeControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, input);
	}

	@Override
	protected void initialize() {
		info = new Info();
		super.initialize();
	}

	@Override
	protected AbstractEditorFormBuilder createSubFormBuilder(SwingRenderer swingRenderer, IFieldControlInput input,
			IContext subContext, Listener<Throwable> commitExceptionHandler) {
		Info alreadyCreatedMutableTypeControlInfo = (Info) input.getControlData().getSpecificProperties()
				.get(MUTABLE_TYPE_CONTROL_INFO_PROPERTY_KEY);
		if (alreadyCreatedMutableTypeControlInfo != null) {
			alreadyCreatedMutableTypeControlInfo.setRecreationNeeded(true);
		}
		return new SubFormBuilder(swingRenderer, this, input, subContext, commitExceptionHandler) {

			@Override
			protected Map<String, Object> getEncapsulatedFieldSpecificProperties() {
				Map<String, Object> result = new HashMap<String, Object>(
						super.getEncapsulatedFieldSpecificProperties());
				result.put(MUTABLE_TYPE_CONTROL_INFO_PROPERTY_KEY, info);
				return result;
			}

			@Override
			public Form createEditorForm(boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
				if (alreadyCreatedMutableTypeControlInfo != null) {
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
		final boolean[] result = new boolean[1];
		data.withInBuffer(value, new Runnable() {			
			@Override
			public void run() {
				result[0] = MutableTypeControl.super.refreshUI(refreshStructure);
			}
		});
		if (refreshStructure) {
			if (data.getCaption().length() == 0) {
				((JComponent) currentSubControl).setBorder(null);
			}
		}
		if (info.isRecreationNeeded()) {
			result[0] = false;
		}
		nullStatusControl.setVisible(false);
		return result[0];
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

	protected static class Info {
		protected boolean recreationNeeded = false;

		public boolean isRecreationNeeded() {
			return recreationNeeded;
		}

		public void setRecreationNeeded(boolean recreationNeeded) {
			this.recreationNeeded = recreationNeeded;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (recreationNeeded ? 1231 : 1237);
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
			Info other = (Info) obj;
			if (recreationNeeded != other.recreationNeeded)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "MutableTypeControl.Info [recreationNeeded=" + recreationNeeded + "]";
		}

	}

}
