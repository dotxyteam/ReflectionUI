package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlInput;
import xy.reflect.ui.control.FieldContext;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.CheckBoxControl;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.NullControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.PolymorphicControl;
import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ValueOptionsAsEnumerationFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.DelayedUpdateProcess;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;

public class FieldControlPlaceHolder extends ControlPanel implements IFieldControlInput {

	protected static final long serialVersionUID = 1L;

	protected final SwingRenderer swingRenderer;
	protected Component fieldControl;
	protected Form form;
	protected IFieldInfo field;
	protected String errorMessageDisplayedOnPlaceHolder;
	protected IFieldControlData controlData;
	protected IFieldControlData lastInitialControlData;
	protected boolean layoutInContainerUpdateNeeded = true;
	protected int positionInContainer = -1;
	protected boolean ancestorVisible = false;
	protected AutoUpdateThread autoUpdateThread;

	public FieldControlPlaceHolder(SwingRenderer swingRenderer, Form form, IFieldInfo field) {
		super();
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.field = field;
		setLayout(new BorderLayout());
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				ancestorVisible = true;
				updateAutoRefeshState();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				ancestorVisible = false;
				updateAutoRefeshState();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
		refreshUI(false);
	}

	public void updateAutoRefeshState() {
		if ((field.getAutoUpdatePeriodMilliseconds() >= 0) && ancestorVisible) {
			startAutoRefresh();
		} else {
			stopAutoRefresh();
		}
	}

	public boolean isAutoRefreshActive() {
		return (autoUpdateThread != null) && (autoUpdateThread.isAlive());
	}

	public void startAutoRefresh() {
		if (isAutoRefreshActive()) {
			return;
		}
		autoUpdateThread = createAutoUpdateThread();
		autoUpdateThread.start();
	}

	public AutoUpdateThread createAutoUpdateThread() {
		return new AutoUpdateThread();
	}

	public void stopAutoRefresh() {
		if (!isAutoRefreshActive()) {
			return;
		}
		while (autoUpdateThread.isAlive()) {
			autoUpdateThread.interrupt();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new ReflectionUIError(e);
			}
		}
	}

	public IFieldInfo getField() {
		return field;
	}

	public Object getObject() {
		return form.getObject();
	}

	public Form getForm() {
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
		return form.getModificationStack();
	}

	public IFieldControlData handleStressfulUpdates(final IFieldControlData data) {
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
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								refreshUI(false);
							}
						});
					}
				}
			};

			boolean isDelayedUpdateDisabled() {
				if (!(swingRenderer.getDataUpdateDelayMilliseconds() > 0)) {
					return true;
				}
				Component c = fieldControl;
				if ((c instanceof IAdvancedFieldControl)) {
					IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
					if (fieldControl.handlesModificationStackAndStress()) {
						return true;
					}
				}
				return false;
			}

			@Override
			public Object getValue() {
				if (isDelayedUpdateDisabled()) {
					return data.getValue();
				}
				if (delaying) {
					return delayedFieldValue;
				} else {
					return data.getValue();
				}
			}

			@Override
			public void setValue(Object newValue) {
				if (isDelayedUpdateDisabled()) {
					data.setValue(newValue);
					return;
				}
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
					if (fieldControl.handlesModificationStackAndStress()) {
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
						swingRenderer.getReflectionUI().logError(t);
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
				if (form.isBusyIndicationDisabled()) {
					return true;
				}
				if (field.getAutoUpdatePeriodMilliseconds() >= 0) {
					return true;
				}
				return false;
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

	public void refreshUI(boolean refreshStructure) {
		if (fieldControl == null) {
			try {
				controlData = lastInitialControlData = getInitialControlData();
				fieldControl = createFieldControl();
			} catch (Throwable t) {
				fieldControl = createFieldErrorControl(t);
			}
			add(fieldControl, BorderLayout.CENTER);
			layoutInContainerUpdateNeeded = true;
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			if (isFieldControlObsolete()) {
				destroyFieldControl();
				refreshUI(refreshStructure);
			} else {
				boolean refreshed = false;
				if (fieldControl instanceof IAdvancedFieldControl) {
					try {
						refreshed = ((IAdvancedFieldControl) fieldControl).refreshUI(refreshStructure);
					} catch (Throwable ignore) {
					}
				}
				if (!refreshed) {
					destroyFieldControl();
					refreshUI(refreshStructure);
				}
			}
		}
		if (refreshStructure) {
			updateAutoRefeshState();
		}
	}

	public void destroyFieldControl() {
		if (fieldControl != null) {
			remove(fieldControl);
			fieldControl = null;
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
					return typeSpecificities.wrapTypeInfo(super.getType());
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
			Component result = createCustomFieldControl();
			if (result != null) {
				return result;
			}
		}
		if (controlData.isNullValueDistinct()) {
			return new NullableControl(this.swingRenderer, this);
		}
		Object value = controlData.getValue();
		controlData = new BufferedFieldControlData(controlData, value);
		if (value == null) {
			return new NullControl(swingRenderer, this);
		}
		ITypeInfo actualValueType = this.swingRenderer.reflectionUI
				.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(value));
		if (!controlData.getType().getName().equals(actualValueType.getName())) {
			final IInfoProxyFactory typeSpecificities = field.getTypeSpecificities();
			if (typeSpecificities != null) {
				actualValueType = typeSpecificities.wrapTypeInfo(actualValueType);
			}
			final ITypeInfo finalActualValueType = actualValueType;
			controlData = new FieldControlDataProxy(controlData) {
				@Override
				public ITypeInfo getType() {
					return finalActualValueType;
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

	public Component createCustomFieldControl() {
		IFieldControlPlugin currentPlugin = getCurrentPlugin();

		if (currentPlugin == null) {
			if (controlData.getType() instanceof IEnumerationTypeInfo) {
				return new EnumerationControl(swingRenderer, this);
			}
			if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(controlData.getType())) {
				return new PolymorphicControl(swingRenderer, this);
			}
			if (!controlData.isNullValueDistinct()) {
				ITypeInfo fieldType = controlData.getType();
				if (fieldType instanceof IListTypeInfo) {
					return new ListControl(swingRenderer, this);
				}
				final Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(fieldType.getName());
				} catch (ClassNotFoundException e) {
					return null;
				}
				if (boolean.class.equals(javaType) || Boolean.class.equals(javaType)) {
					return new CheckBoxControl(swingRenderer, this);
				}
				if (ClassUtils.isPrimitiveClassOrWrapper(javaType)) {
					return new PrimitiveValueControl(swingRenderer, this);
				}
				if (String.class.equals(javaType)) {
					return new TextControl(swingRenderer, this);
				}
			}
		}

		if (currentPlugin != null) {
			if (!controlData.isNullValueDistinct() || currentPlugin.canDisplayDistinctNullValue()) {
				Component result;
				try {
					result = currentPlugin.createControl(swingRenderer, this);
				} catch (Throwable t) {
					result = createFieldErrorControl(t);
				}
				return result;
			} else {
				controlData = currentPlugin.filterDistinctNullValueControlData(controlData);
			}
		}

		return null;
	}

	public Component createFieldErrorControl(final Throwable t) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		reflectionUI.logError(t);
		JPanel result = new ControlPanel();
		result.setLayout(new BorderLayout());
		result.add(new NullControl(swingRenderer, new DefaultFieldControlInput(swingRenderer.getReflectionUI()) {
			@Override
			public IFieldControlData getControlData() {
				return new DefaultFieldControlData(swingRenderer.getReflectionUI()) {
					@Override
					public String getNullValueLabel() {
						return ReflectionUIUtils.getPrettyErrorMessage(t);
					}
				};
			}
		}), BorderLayout.CENTER);
		SwingRendererUtils.setErrorBorder(result);
		return result;
	}

	public IFieldControlPlugin getCurrentPlugin() {
		return SwingRendererUtils.getCurrentFieldControlPlugin(swingRenderer,
				controlData.getType().getSpecificProperties(), this);
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
		return "FieldControlPlaceHolder [field=" + field + ", form=" + form + "]";
	}

	protected class InitialFieldControlData extends DefaultFieldControlData {

		protected IFieldInfo finalField;

		public InitialFieldControlData(IFieldInfo finalField) {
			super(swingRenderer.getReflectionUI(), form.getObject(), finalField);
		}

		@Override
		public Object getObject() {
			return form.getObject();
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

	protected class AutoUpdateThread extends Thread {

		protected boolean updating = false;

		public AutoUpdateThread() {
			super("AutoUpdater of " + FieldControlPlaceHolder.this);
		}

		@Override
		public void run() {
			while (true) {
				sleepUntilNextUpdateTime();
				waitForCurrentUpdateEnd();
				sleepWhileWindowInactive();
				if (isInterrupted()) {
					break;
				}
				update();
			}
		}

		protected void sleepWhileWindowInactive() {
			Window window = SwingUtilities.getWindowAncestor(FieldControlPlaceHolder.this);
			if (window == null) {
				return;
			}
			while (true) {
				if (isInterrupted()) {
					break;
				}
				boolean blockedByModalDialog = false;
				for (Window ownedWindow : window.getOwnedWindows()) {
					if (ownedWindow.isVisible()) {
						if (ownedWindow instanceof Dialog) {
							Dialog dialog = (Dialog) ownedWindow;
							if (dialog.isModal()) {
								blockedByModalDialog = true;
							}
						}
					}
				}
				if (!blockedByModalDialog) {
					break;
				}
				relieveCPU();
			}
		}

		protected void relieveCPU() {
			try {
				sleep(10);
			} catch (InterruptedException e) {
				interrupt();
			}
		}

		protected void waitForCurrentUpdateEnd() {
			while (updating) {
				if (isInterrupted()) {
					break;
				}
				relieveCPU();
			}
		}

		protected void update() {
			updating = true;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					refreshUI(false);
					if (isLayoutInContainerUpdateNeeded()) {
						form.updateFieldControlLayoutInContainer(FieldControlPlaceHolder.this);
					}
					updating = false;
				}
			});
		}

		protected void sleepUntilNextUpdateTime() {
			if (field.getAutoUpdatePeriodMilliseconds() > 0) {
				try {
					sleep(field.getAutoUpdatePeriodMilliseconds());
				} catch (InterruptedException e) {
					interrupt();
				}
			}
		}
	}

}