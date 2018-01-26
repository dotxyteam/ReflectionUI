package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.control.FieldContext;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.NullControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ValueOptionsAsEnumerationFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.DelayedUpdateProcess;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class FieldControlPlaceHolder extends JPanel implements IFieldControlInput {

	protected static final long serialVersionUID = 1L;

	protected final SwingRenderer swingRenderer;
	protected Component fieldControl;
	protected JPanel form;
	protected IFieldInfo field;
	protected String errorMessageDisplayedOnPlaceHolder;
	protected IFieldControlData controlData;
	protected IFieldControlData lastInitialControlData;
	protected boolean layoutInContainerUpdateNeeded = true;
	protected int positionInContainer = -1;

	public FieldControlPlaceHolder(SwingRenderer swingRenderer, JPanel form, IFieldInfo field) {
		super();
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.field = field;
		setLayout(new BorderLayout());
		refreshUI(false);
		if (field.getAutoUpdatePeriodMilliseconds() >= 0) {
			refreshUIPeriodically();
		}
	}

	public void refreshUIPeriodically() {
		addAncestorListener(new AncestorListener() {

			Thread thread;

			@Override
			public void ancestorAdded(AncestorEvent event) {
				thread = new Thread("AutoUpdater of " + FieldControlPlaceHolder.this) {
					boolean updating = false;

					@Override
					public void run() {
						while (true) {
							if (field.getAutoUpdatePeriodMilliseconds() > 0) {
								try {
									sleep(field.getAutoUpdatePeriodMilliseconds());
								} catch (InterruptedException e) {
									interrupt();
								}
							}
							while (updating) {
								if (isInterrupted()) {
									break;
								}
								try {
									sleep(1);
								} catch (InterruptedException e) {
									interrupt();
									break;
								}
							}
							if (isInterrupted()) {
								break;
							}
							updating = true;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									refreshUI(false);
									if (isLayoutInContainerUpdateNeeded()) {
										swingRenderer.updateFieldControlLayoutInContainer(FieldControlPlaceHolder.this);
									}
									updating = false;
								}
							});
						}
					}
				};
				thread.start();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				thread.interrupt();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
	}

	public IFieldInfo getField() {
		return field;
	}

	public Object getObject() {
		return this.swingRenderer.getObjectByForm().get(form);
	}

	public JPanel getForm() {
		return form;
	}

	@Override
	public IFieldControlData getControlData() {
		return controlData;
	}

	public void setControlData(IFieldControlData controlData) {
		this.controlData = controlData;
	}

	public boolean isLayoutInContainerUpdateNeeded() {
		return layoutInContainerUpdateNeeded;
	}

	public void setLayoutInContainerUpdateNeeded(boolean layoutUpdateNeeded) {
		this.layoutInContainerUpdateNeeded = layoutUpdateNeeded;
	}

	public int getPositionInContainer() {
		return positionInContainer;
	}

	public void setPositionInContainer(int positionInContainer) {
		this.positionInContainer = positionInContainer;
	}

	@Override
	public IContext getContext() {
		ITypeInfo objectType = this.swingRenderer.reflectionUI
				.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(getObject()));
		return new FieldContext(objectType, field);
	}

	@Override
	public ModificationStack getModificationStack() {
		return this.swingRenderer.getModificationStackByForm().get(form);
	}

	public IFieldControlData handleStressfulUpdates(final IFieldControlData data) {
		if (!(swingRenderer.getDataUpdateDelayMilliseconds() > 0)) {
			return data;
		}
		return new FieldControlDataProxy(data) {

			Object delayedFieldValue;
			boolean delaying = false;
			DelayedUpdateProcess delayedUpdateProcess = new DelayedUpdateProcess() {
				{
					setDelayMilliseconds(swingRenderer.getDataUpdateDelayMilliseconds());
				}

				@Override
				public void run() {
					try {
						data.setValue(delayedFieldValue);
					} finally {
						delaying = false;
					}
				}
			};

			@Override
			public Object getValue() {
				if (delaying) {
					return delayedFieldValue;
				} else {
					return data.getValue();
				}
			}

			@Override
			public void setValue(Object newValue) {
				delayedFieldValue = newValue;
				delaying = true;
				delayedUpdateProcess.schedule();
			}
		};
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
				return Boolean.TRUE
						.equals(FieldControlPlaceHolder.this.swingRenderer.getBusyIndicationDisabledByForm().get(form));
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
				SwingRendererUtils.showBusyDialogWhileSettingFieldValue(FieldControlPlaceHolder.this, swingRenderer,
						data, value);
			}

			@Override
			public Runnable getNextUpdateCustomUndoJob(Object newValue) {
				if (isBusyIndicationDisabled()) {
					return super.getNextUpdateCustomUndoJob(newValue);
				}
				final Runnable result = data.getNextUpdateCustomUndoJob(newValue);
				if (result == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						FieldControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(FieldControlPlaceHolder.this,
								new Runnable() {
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
			layoutInContainerUpdateNeeded = true;
		} else {
			if (isFieldControlObsolete()) {
				refreshUI(true);
			} else {
				boolean refreshed = false;
				if (fieldControl instanceof IAdvancedFieldControl) {
					try {
						refreshed = ((IAdvancedFieldControl) fieldControl).refreshUI();
					} catch (Throwable ignore) {
					}
				}
				if (!refreshed) {
					refreshUI(true);
				}
			}
		}
	}

	public boolean isFieldControlObsolete() {
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
		if (ValueOptionsAsEnumerationFieldInfo.hasValueOptions(object, field)) {
			field = new ValueOptionsAsEnumerationFieldInfo(this.swingRenderer.reflectionUI, object, field);
		}
		final IInfoProxyFactory typeSpecificities = field.getTypeSpecificities();
		if (typeSpecificities != null) {
			field = new FieldInfoProxy(field) {
				@Override
				public ITypeInfo getType() {
					return typeSpecificities.wrapType(super.getType());
				}
			};
		}
		final IFieldInfo finalField = field;
		IFieldControlData result = new InitialFieldControlData(finalField);
		result = indicateWhenBusy(result);
		result = handleValueAccessIssues(result);
		result = makeFieldModificationsUndoable(result);
		result = handleStressfulUpdates(result);
		return result;
	}

	public Component createFieldControl() {
		if (!controlData.isFormControlMandatory()) {
			Component result = this.swingRenderer.createCustomFieldControl(this);
			if (result != null) {
				return result;
			}
		}
		if (controlData.isNullValueDistinct()) {
			return new NullableControl(this.swingRenderer, this);
		}
		Object value = controlData.getValue();
		if (value == null) {
			return new NullControl(swingRenderer, this);
		}
		final ITypeInfo actualValueType = this.swingRenderer.reflectionUI
				.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(value));
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
		public Runnable getNextUpdateCustomUndoJob(Object newValue) {
			return finalField.getNextUpdateCustomUndoJob(getObject(), newValue);
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
		public boolean isNullValueDistinct() {
			return finalField.isNullValueDistinct();
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