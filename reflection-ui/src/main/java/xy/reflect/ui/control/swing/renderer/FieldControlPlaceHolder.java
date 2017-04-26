package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;

import javax.swing.JPanel;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ValueOptionsAsEnumerationField;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class FieldControlPlaceHolder extends JPanel implements IFieldControlInput {

	/**
	 * 
	 */
	private final SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected Component fieldControl;
	protected JPanel form;
	protected IFieldInfo field;
	protected String errorMessageDisplayedOnPlaceHolder;
	protected IFieldControlData controlData;
	protected IFieldControlData lastInitialControlData;

	public FieldControlPlaceHolder(SwingRenderer swingRenderer, JPanel form, IFieldInfo field) {
		super();
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.field = field;
		setLayout(new BorderLayout());
		refreshUI(false);
	}

	public IFieldInfo getField() {
		return field;
	}

	public Object getObject() {
		return this.swingRenderer.getObjectByForm().get(form);
	}

	@Override
	public IFieldControlData getControlData() {
		return controlData;
	}

	public void setControlData(IFieldControlData controlData) {
		this.controlData = controlData;
	}

	@Override
	public String getContextIdentifier() {
		ITypeInfo objectType = this.swingRenderer.reflectionUI.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(getObject()));
		return "FieldContext [fieldName=" + field.getName() + ", containingType=" + objectType.getName() + "]";
	}

	@Override
	public ModificationStack getModificationStack() {
		return this.swingRenderer.getModificationStackByForm().get(form);
	}

	public IFieldControlData makeFieldModificationsUndoable(final IFieldControlData data) {
		return new FieldControlDataProxy(data) {

			@Override
			public void setValue(Object newValue) {
				Component c = fieldControl;
				if ((c instanceof IAdvancedFieldControl)) {
					IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
					if (fieldControl.handlesModificationStackUpdate()) {
						data.setValue(newValue);
						return;
					}
				}
				ReflectionUIUtils.setValueThroughModificationStack(data, newValue, getModificationStack(),
						getModificationsTarget());
			}
		};
	}

	public IFieldControlData handleValueAccessIssues(final IFieldControlData data) {
		return new FieldControlDataProxy(data) {

			Object lastFieldValue;
			boolean lastFieldValueInitialized = false;
			Throwable lastValueUpdateError;

			@Override
			public Object getValue() {
				try {
					if (lastValueUpdateError != null) {
						throw lastValueUpdateError;
					}
					lastFieldValue = data.getValue();
					lastFieldValueInitialized = true;
					displayError(null);
				} catch (final Throwable t) {
					if (!lastFieldValueInitialized) {
						throw new ReflectionUIError(t);
					} else {
						t.printStackTrace();
						displayError(ReflectionUIUtils.getPrettyErrorMessage(t));
					}
				}
				return lastFieldValue;

			}

			@Override
			public void setValue(Object newValue) {
				try {
					lastFieldValue = newValue;
					data.setValue(newValue);
					lastValueUpdateError = null;
				} catch (Throwable t) {
					lastValueUpdateError = t;
				}
			}

		};
	}

	public IFieldControlData indicateWhenBusy(final IFieldControlData data) {
		return new FieldControlDataProxy(data) {

			private boolean isBusyIndicationDisabled() {
				JPanel form = SwingRendererUtils.findParentForm(FieldControlPlaceHolder.this, swingRenderer);
				return Boolean.TRUE.equals(FieldControlPlaceHolder.this.swingRenderer.getBusyIndicationDisabledByForm().get(form));
			}

			@Override
			public Object getValue() {
				if (isBusyIndicationDisabled()) {
					return super.getValue();
				}
				return SwingRendererUtils.showBusyDialogWhileGettingFieldValue(FieldControlPlaceHolder.this,
						swingRenderer, data);
			}

			@Override
			public void setValue(final Object value) {
				if (isBusyIndicationDisabled()) {
					super.setValue(value);
					return;
				}
				SwingRendererUtils.showBusyDialogWhileSettingFieldValue(FieldControlPlaceHolder.this,
						swingRenderer, data, value);
			}

			@Override
			public Runnable getCustomUndoUpdateJob(Object newValue) {
				if (isBusyIndicationDisabled()) {
					return super.getCustomUndoUpdateJob(newValue);
				}
				final Runnable result = data.getCustomUndoUpdateJob(newValue);
				if (result == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						FieldControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(FieldControlPlaceHolder.this, new Runnable() {
							public void run() {
								result.run();
							}
						}, AbstractModification.getUndoTitle("Setting " + data.getCaption()));
					}
				};
			}

		};
	}

	public Component getFieldControl() {
		return fieldControl;
	}

	@Override
	public IInfo getModificationsTarget() {
		return field;
	}

	public void refreshUI(boolean recreate) {
		if (recreate) {
			if (fieldControl != null) {
				remove(fieldControl);
				fieldControl = null;
			}
		}
		if (fieldControl == null) {
			try {
				controlData = lastInitialControlData = getInitialControlData();
				fieldControl = createFieldControl();
			} catch (Throwable t) {
				fieldControl = this.swingRenderer.createErrorControl(t);
			}
			add(fieldControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			if (isFieldControlObsolete()) {
				refreshUI(true);
			} else {
				boolean refreshed = false;
				if (fieldControl instanceof IAdvancedFieldControl) {
					try {
						refreshed = ((IAdvancedFieldControl) fieldControl).refreshUI();
					} catch (Throwable t) {
						refreshed = false;
					}
				}
				if (!refreshed) {
					remove(fieldControl);
					fieldControl = null;
					refreshUI(false);
				}
			}
		}
	}

	protected boolean isFieldControlObsolete() {
		IFieldControlData newInitialControlData;
		try {
			newInitialControlData = getInitialControlData();
		} catch (Throwable t) {
			return true;
		}
		if (!newInitialControlData.equals(lastInitialControlData)) {
			return true;
		}
		return false;
	}

	public IFieldControlData getInitialControlData() {
		Object object = getObject();
		IFieldInfo field = FieldControlPlaceHolder.this.field;
		Object[] valueOptions = field.getValueOptions(object);
		if (valueOptions != null) {
			field = new ValueOptionsAsEnumerationField(this.swingRenderer.reflectionUI, object, field);
		}
		final ITypeInfoProxyFactory typeSpecificities = field.getTypeSpecificities();
		if (typeSpecificities != null) {
			field = new FieldInfoProxy(field) {
				@Override
				public ITypeInfo getType() {
					return typeSpecificities.get(super.getType());
				}
			};
		}
		final IFieldInfo finalField = field;
		IFieldControlData result = new InitialFieldControlData(finalField);
		result = indicateWhenBusy(result);
		result = handleValueAccessIssues(result);
		result = makeFieldModificationsUndoable(result);
		return result;
	}

	public Component createFieldControl() {
		if (!controlData.isFormControlMandatory()) {
			Component result = this.swingRenderer.createCustomFieldControl(this);
			if (result != null) {
				return result;
			}
		}
		if (controlData.isValueNullable()) {
			return new NullableControl(this.swingRenderer, this);
		}
		Object value = controlData.getValue();
		final ITypeInfo actualValueType = this.swingRenderer.reflectionUI.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(value));
		if (!controlData.getType().getName().equals(actualValueType.getName())) {
			controlData = new FieldControlDataProxy(controlData) {
				@Override
				public ITypeInfo getType() {
					return actualValueType;
				}
			};
			return createFieldControl();
		}
		if (controlData.isFormControlEmbedded()) {
			return new EmbeddedFormControl(this.swingRenderer, this);
		} else {
			return new DialogAccessControl(this.swingRenderer, this);
		}
	}

	public void displayError(String msg) {
		boolean done = (fieldControl instanceof IAdvancedFieldControl)
				&& ((IAdvancedFieldControl) fieldControl).displayError(msg);
		if (!done && (msg != null)) {
			if (errorMessageDisplayedOnPlaceHolder == null) {
				errorMessageDisplayedOnPlaceHolder = msg;
				SwingRendererUtils.setErrorBorder(this);
				this.swingRenderer.handleExceptionsFromDisplayedUI(fieldControl, new ReflectionUIError(msg));
			}
		} else {
			errorMessageDisplayedOnPlaceHolder = null;
			setBorder(null);
		}
	}

	public boolean showsCaption() {
		if (((fieldControl instanceof IAdvancedFieldControl)
				&& ((IAdvancedFieldControl) fieldControl).showsCaption())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "FieldControlPlaceHolder [form=" + form + ", field=" + field + "]";
	}

	protected class InitialFieldControlData implements IFieldControlData {

		protected IFieldInfo finalField;

		public InitialFieldControlData(IFieldInfo finalField) {
			this.finalField = finalField;
		}

		@Override
		public Object getValue() {
			return finalField.getValue(getObject());
		}

		@Override
		public void setValue(Object value) {
			finalField.setValue(getObject(), value);
		}

		@Override
		public String getCaption() {
			return finalField.getCaption();
		}

		@Override
		public Runnable getCustomUndoUpdateJob(Object newValue) {
			return finalField.getCustomUndoUpdateJob(getObject(), newValue);
		}

		@Override
		public ITypeInfo getType() {
			return finalField.getType();
		}

		@Override
		public boolean isGetOnly() {
			return finalField.isGetOnly();
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return finalField.getValueReturnMode();
		}

		@Override
		public boolean isValueNullable() {
			return finalField.isValueNullable();
		}

		@Override
		public String getNullValueLabel() {
			return finalField.getNullValueLabel();
		}

		public boolean isFormControlMandatory() {
			return finalField.isFormControlMandatory();
		}

		public boolean isFormControlEmbedded() {
			return finalField.isFormControlEmbedded();
		}

		public IInfoFilter getFormControlFilter() {
			return finalField.getFormControlFilter();
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return finalField.getSpecificProperties();
		}

		private FieldControlPlaceHolder getOuterType() {
			return FieldControlPlaceHolder.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((finalField == null) ? 0 : finalField.hashCode());
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
			InitialFieldControlData other = (InitialFieldControlData) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (finalField == null) {
				if (other.finalField != null)
					return false;
			} else if (!finalField.equals(other.finalField))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InitialFieldControlData [of=" + getOuterType() + ", finalField=" + finalField + "]";
		}

	}

}