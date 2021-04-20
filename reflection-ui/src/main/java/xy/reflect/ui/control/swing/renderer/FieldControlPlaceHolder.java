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
package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlInput;
import xy.reflect.ui.control.ErrorHandlingFieldControlData;
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
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ValueOptionsAsEnumerationFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Instances of this class are field control containers.
 * 
 * They provide common field control features as error display, undo management,
 * busy indication, updates synchronization, etc. These features can be
 * customized/disabled by making the control override the @
 * {@link IAdvancedFieldControl} interface.
 * 
 * They also generate the input data that will be used by the
 * {@link #createFieldControl()} method and passed to the control constructor
 * directly or with some control-specific proxy layers.
 * 
 * @author olitank
 *
 */
public class FieldControlPlaceHolder extends ControlPanel implements IFieldControlInput {

	protected static final long serialVersionUID = 1L;

	public static final String COMMON_CONTROL_MANAGEMENT_ENABLED_PROPERTY_KEY = FieldControlPlaceHolder.class.getName()
			+ ".COMMON_CONTROL_MANAGEMENT_ENABLED";

	protected final SwingRenderer swingRenderer;
	protected Component fieldControl;
	protected Form form;
	protected IFieldInfo field;
	protected IFieldControlData controlData;
	protected IFieldControlData lastInitialControlData;
	protected boolean layoutInContainerUpdateNeeded = true;
	protected int positionInContainer = -1;
	protected boolean ancestorVisible = false;
	protected AutoUpdater autoRefreshThread;
	protected Component siblingCaptionControl;
	protected Component siblingOnlineHelpControl;

	public FieldControlPlaceHolder(SwingRenderer swingRenderer, Form form, IFieldInfo field) {
		super();
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.field = field;
		setLayout(new BorderLayout());
		manageVisibiltyChanges();
		refreshUI(false);
	}

	public Component getSiblingCaptionControl() {
		return siblingCaptionControl;
	}

	public void setSiblingCaptionControl(Component siblingCaptionControl) {
		this.siblingCaptionControl = siblingCaptionControl;
	}

	public Component getSiblingOnlineHelpControl() {
		return siblingOnlineHelpControl;
	}

	public void setSiblingOnlineHelpControl(Component siblingOnlineHelpControl) {
		this.siblingOnlineHelpControl = siblingOnlineHelpControl;
	}

	protected void manageVisibiltyChanges() {
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				FieldControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(FieldControlPlaceHolder.this,
						new Runnable() {
							@Override
							public void run() {
								FieldControlPlaceHolder.this.field.onControlVisibilityChange(getObject(), true);
							}
						}, FieldControlPlaceHolder.this.swingRenderer.getObjectTitle(getObject()) + " - "
								+ FieldControlPlaceHolder.this.field.getCaption() + " - Setting up...");
				ancestorVisible = true;
				updateAutoRefeshState();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				FieldControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(FieldControlPlaceHolder.this,
						new Runnable() {
							@Override
							public void run() {
								FieldControlPlaceHolder.this.field.onControlVisibilityChange(getObject(), false);
							}
						}, FieldControlPlaceHolder.this.swingRenderer.getObjectTitle(getObject()) + " - "
								+ FieldControlPlaceHolder.this.field.getCaption() + " - Cleaning up...");
				ancestorVisible = false;
				updateAutoRefeshState();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
	}

	public void updateAutoRefeshState() {
		if (isAutoRefreshActive()) {
			stopAutoRefresh();
		}
		if ((field.getAutoUpdatePeriodMilliseconds() >= 0) && ancestorVisible) {
			startAutoRefresh();
		}
	}

	public boolean isAutoRefreshActive() {
		return (autoRefreshThread != null) && (autoRefreshThread.isRunning());
	}

	public AutoUpdater createAutoRefreshThread() {
		return new AutoUpdater();
	}

	protected void startAutoRefresh() {
		if (isAutoRefreshActive()) {
			return;
		}
		autoRefreshThread = createAutoRefreshThread();
		autoRefreshThread.start();
	}

	protected void stopAutoRefresh() {
		if (!isAutoRefreshActive()) {
			return;
		}
		autoRefreshThread.stop();
		autoRefreshThread = null;
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

	protected IFieldControlData makeFieldModificationsUndoable(final IFieldControlData data) {
		return new FieldControlDataProxy(data) {

			@Override
			public void setValue(Object newValue) {
				if (isFieldControlAutoManaged()) {
					super.setValue(newValue);
					return;
				}
				ReflectionUIUtils.setFieldValueThroughModificationStack(data, newValue, getModificationStack());
			}
		};
	}

	protected boolean isFieldControlAutoManaged() {
		Component c = fieldControl;
		if ((c instanceof IAdvancedFieldControl)) {
			IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
			if (fieldControl.isAutoManaged()) {
				return true;
			}
		}
		return false;
	}

	protected IFieldControlData handleValueAccessIssues(final IFieldControlData data) {
		return new ErrorHandlingFieldControlData(data) {

			String currentlyDisplayedErrorId;

			@Override
			protected void displayError(Throwable t) {
				boolean done = (fieldControl instanceof IAdvancedFieldControl) && ((IAdvancedFieldControl) fieldControl)
						.displayError((t == null) ? null : ReflectionUIUtils.getPrettyErrorMessage(t));
				if (!done && (t != null)) {
					String newErrorId = ReflectionUIUtils.getPrintedStackTrace(t);
					if (!newErrorId.equals(currentlyDisplayedErrorId)) {
						currentlyDisplayedErrorId = newErrorId;
						SwingRendererUtils.setErrorBorder(FieldControlPlaceHolder.this);
						swingRenderer.handleExceptionsFromDisplayedUI(fieldControl, t);
					}
				} else {
					currentlyDisplayedErrorId = null;
					setBorder(null);
				}
			}

			@Override
			public Object getValue() {
				if (isFieldControlAutoManaged()) {
					return data.getValue();
				}
				return super.getValue();
			}

			@Override
			public void setValue(Object newValue) {
				if (isFieldControlAutoManaged()) {
					data.setValue(newValue);
					return;
				}
				super.setValue(newValue);
			}

		};
	}

	protected IFieldControlData indicateWhenBusy(final IFieldControlData data) {
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
				if (isFieldControlAutoManaged()) {
					return super.getValue();
				}
				if (isBusyIndicationDisabled()) {
					return super.getValue();
				}
				return SwingRendererUtils.showBusyDialogWhileGettingFieldValue(FieldControlPlaceHolder.this,
						swingRenderer, data);
			}

			@Override
			public void setValue(final Object newValue) {
				if (isFieldControlAutoManaged()) {
					super.setValue(newValue);
					return;
				}
				if (isBusyIndicationDisabled()) {
					super.setValue(newValue);
					return;
				}
				SwingRendererUtils.showBusyDialogWhileSettingFieldValue(FieldControlPlaceHolder.this, swingRenderer,
						data, newValue);
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

	public void refreshUI(boolean refreshStructure) {
		if (fieldControl == null) {
			try {
				controlData = lastInitialControlData = getInitialControlData();
				fieldControl = createFieldControl();
			} catch (Throwable t) {
				swingRenderer.getReflectionUI().logError(t);
				fieldControl = createFieldErrorControl(t);
			}
			layoutFieldControl();
		} else {
			if (isFieldControlObsolete()) {
				destroyFieldControl();
				refreshUI(refreshStructure);
			} else {
				if (fieldControl instanceof IAdvancedFieldControl) {
					try {
						if (!((IAdvancedFieldControl) fieldControl).refreshUI(refreshStructure)) {
							destroyFieldControl();
							refreshUI(refreshStructure);
						}
					} catch (Throwable t) {
						destroyFieldControl();
						swingRenderer.getReflectionUI().logError(t);
						fieldControl = createFieldErrorControl(t);
						layoutFieldControl();
					}
				} else {
					destroyFieldControl();
					refreshUI(refreshStructure);
				}
			}
		}
		if (refreshStructure) {
			updateAutoRefeshState();
		}
	}

	protected void layoutFieldControl() {
		add(fieldControl, BorderLayout.CENTER);
		layoutInContainerUpdateNeeded = true;
		if (getParent() != null) {
			form.updateFieldControlLayoutInContainer(FieldControlPlaceHolder.this);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected void destroyFieldControl() {
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
		if (field.hasValueOptions(object)) {
			field = new ValueOptionsAsEnumerationFieldInfo(this.swingRenderer.reflectionUI, object, field);
		}
		final IFieldInfo finalField = field;
		IFieldControlData result = new InitialFieldControlData(finalField);
		result = handleValueAccessIssues(result);
		result = makeFieldModificationsUndoable(result);
		result = indicateWhenBusy(result);
		result = addControlManagementStatusProperty(result);
		return result;
	}

	protected IFieldControlData addControlManagementStatusProperty(IFieldControlData result) {
		return new FieldControlDataProxy(result) {

			@Override
			public Map<String, Object> getSpecificProperties() {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
				result.put(COMMON_CONTROL_MANAGEMENT_ENABLED_PROPERTY_KEY, !isFieldControlAutoManaged());
				return result;
			}
		};
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
				.getTypeInfo(new TypeInfoSourceProxy(this.swingRenderer.reflectionUI.getTypeInfoSource(value)) {
					SpecificitiesIdentifier specificitiesIdentifier = controlData.getType().getSource()
							.getSpecificitiesIdentifier();

					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return specificitiesIdentifier;
					}
				});
		if (!controlData.getType().getName().equals(actualValueType.getName())) {
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
					result = (Component) currentPlugin.createControl(swingRenderer, this);
				} catch (Throwable t) {
					swingRenderer.getReflectionUI().logError(t);
					result = createFieldErrorControl(t);
				}
				return result;
			} else {
				controlData = currentPlugin.filterDistinctNullValueControlData(swingRenderer, controlData);
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

		public InitialFieldControlData(IFieldInfo finalField) {
			super(swingRenderer.getReflectionUI(), form.getObject(), finalField);
		}

		@Override
		public Object getObject() {
			return form.getObject();
		}

		@Override
		public Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor) {
			if (selectableConstructor) {
				return swingRenderer.onTypeInstanciationRequest(FieldControlPlaceHolder.this, typeToInstanciate,
						getObject());
			} else {
				return ReflectionUIUtils.createDefaultInstance(typeToInstanciate, getObject());
			}
		}

		private FieldControlPlaceHolder getOuterType() {
			return FieldControlPlaceHolder.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + super.hashCode();
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
			if (!super.equals(other))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InitialFieldControlData [of=" + getOuterType() + ", finalField=" + getField() + "]";
		}

	}

	protected class AutoUpdater extends Timer {

		private static final long serialVersionUID = 1L;

		public AutoUpdater() {
			super(-1, null);
			setInitialDelay(0);
			if (field.getAutoUpdatePeriodMilliseconds() > Integer.MAX_VALUE) {
				setDelay(Integer.MAX_VALUE);
			} else if (field.getAutoUpdatePeriodMilliseconds() < Integer.MIN_VALUE) {
				setDelay(Integer.MIN_VALUE);
			} else {
				setDelay((int) field.getAutoUpdatePeriodMilliseconds());
			}
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executePeriodically();
				}
			});
			setRepeats(true);
		}

		protected void executePeriodically() {
			if (isWindowInactive()) {
				return;
			}
			update();
		}

		protected boolean isWindowInactive() {
			Window window = SwingUtilities.getWindowAncestor(FieldControlPlaceHolder.this);
			if (window == null) {
				return true;
			}
			if (!SwingRendererUtils.getFrontWindows().contains(window)) {
				return true;
			}
			return false;
		}

		protected void update() {
			refreshUI(false);
			if (isLayoutInContainerUpdateNeeded()) {
				form.updateFieldControlLayoutInContainer(FieldControlPlaceHolder.this);
			}
		}

		public String toString() {
			return "AutoUpdater of " + FieldControlPlaceHolder.this;
		}

	}

}
