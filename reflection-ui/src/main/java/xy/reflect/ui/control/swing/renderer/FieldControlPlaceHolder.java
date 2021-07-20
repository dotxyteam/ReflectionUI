
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
import xy.reflect.ui.control.AbstractFieldControlData;
import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlInput;
import xy.reflect.ui.control.FieldContext;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.RejectedFieldControlInputException;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.CheckBoxControl;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.MutableTypeControl;
import xy.reflect.ui.control.swing.NullControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.PolymorphicControl;
import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ValueOptionsAsEnumerationFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Instances of this class are field control containers.
 * 
 * They provide common field control features as error display, undo management,
 * busy indication, updates synchronization, etc. These features can be tweaked
 * by making the field control implement the {@link IAdvancedFieldControl}
 * interface.
 * 
 * They also generate the input that will be used by the
 * {@link #createFieldControl()} method and passed to the control constructor.
 * 
 * @author olitank
 *
 */
public class FieldControlPlaceHolder extends ControlPanel implements IFieldControlInput {

	protected static final long serialVersionUID = 1L;

	public static final String CONTROL_AUTO_MANAGEMENT_ENABLED_PROPERTY_KEY = FieldControlPlaceHolder.class.getName()
			+ ".COMMON_CONTROL_MANAGEMENT_ENABLED";

	protected final SwingRenderer swingRenderer;
	protected Component fieldControl;
	protected Form form;
	protected IFieldInfo field;
	protected IFieldControlData controlData;
	protected boolean layoutInContainerUpdateNeeded = true;
	protected int positionInContainer = -1;
	protected boolean ancestorVisible = false;
	protected AutoUpdater autoRefreshThread;
	protected Component siblingCaptionControl;
	protected Component siblingOnlineHelpControl;
	protected Map<String, Object> lastFieldControlSelectionCriteria;

	public FieldControlPlaceHolder(Form form, IFieldInfo field) {
		super();
		this.swingRenderer = form.getSwingRenderer();
		this.form = form;
		this.field = field;
		this.controlData = createControlData();
		setName("fieldControlPlaceHolder [field=" + field.getName() + ", parent=" + form.getName() + "]");
		setLayout(new BorderLayout());
		manageVisibiltyChanges();
	}

	public void initializeUI() {
		refreshUI(true);
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
				.buildTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(getObject()));
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
					data.setValue(newValue);
					return;
				}
				ReflectionUIUtils.setFieldValueThroughModificationStack(data, newValue, getModificationStack(),
						ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
			}
		};
	}

	protected IFieldControlData handleValueAccessIssues(final IFieldControlData data) {
		return new ErrorHandlingFieldControlData(data, swingRenderer, FieldControlPlaceHolder.this) {

			@Override
			protected void handleError(Throwable t) {
				final String newErrorId = (t == null) ? null : t.toString();
				if (MiscUtils.equalsOrBothNull(newErrorId, currentlyDisplayedErrorId)) {
					return;
				}
				if (t != null) {
					swingRenderer.getReflectionUI().logDebug(t);
				}
				currentlyDisplayedErrorId = newErrorId;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						boolean done = (fieldControl instanceof IAdvancedFieldControl)
								&& ((IAdvancedFieldControl) fieldControl)
										.displayError((t == null) ? null : MiscUtils.getPrettyErrorMessage(t));
						if (!done && (t != null)) {
							FieldControlPlaceHolder.this.setBorder(SwingRendererUtils.getErrorBorder());
							showErrorDialog(t);
						} else {
							FieldControlPlaceHolder.this.setBorder(null);
						}
					}
				});
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

	protected boolean isFieldControlAutoManaged() {
		Component c = fieldControl;
		if (c == null) {
			return false;
		}
		if ((c instanceof IAdvancedFieldControl)) {
			IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
			if (fieldControl.isAutoManaged()) {
				return true;
			}
		}
		return false;
	}

	public Component getFieldControl() {
		return fieldControl;
	}

	public void refreshUI(boolean refreshStructure) {
		if (fieldControl == null) {
			try {
				fieldControl = createFieldControl();
				/*
				 * Save the criteria in a field since the controlData internal structure may
				 * change:
				 */
				lastFieldControlSelectionCriteria = getFieldControlSelectionCriteria(controlData);
				fieldControl.setName("fieldControl [field=" + field.getName() + ", parent=" + form.getName() + "]");
			} catch (Throwable t) {
				fieldControl = createFieldErrorControl(t);
			}
			layoutFieldControl();
		} else {
			if ((fieldControl instanceof IAdvancedFieldControl)
					&& !(refreshStructure && !getFieldControlSelectionCriteria(createControlData())
							.equals(lastFieldControlSelectionCriteria))) {
				try {
					if (!((IAdvancedFieldControl) fieldControl).refreshUI(refreshStructure)) {
						destroyFieldControl();
						refreshUI(refreshStructure);
					}
				} catch (Throwable t) {
					destroyFieldControl();
					fieldControl = createFieldErrorControl(t);
					layoutFieldControl();
				}
			} else {
				destroyFieldControl();
				if (refreshStructure) {
					controlData = createControlData();
				}
				refreshUI(refreshStructure);
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
		remove(fieldControl);
		fieldControl = null;
	}

	public IFieldControlData createControlData() {
		IFieldInfo field = FieldControlPlaceHolder.this.field;
		if (field.hasValueOptions(getObject())) {
			field = new ValueOptionsAsEnumerationFieldInfo(this.swingRenderer.reflectionUI, field) {
				@Override
				protected Object getObject() {
					return FieldControlPlaceHolder.this.getObject();
				}
			};
		}
		final IFieldInfo finalField = field;
		IFieldControlData result = new FieldControlData(finalField);
		result = handleValueAccessIssues(result);
		result = makeFieldModificationsUndoable(result);
		result = addControlAutoManagementStatusProperty(result);
		return result;
	}

	protected IFieldControlData addControlAutoManagementStatusProperty(IFieldControlData result) {
		return new FieldControlDataProxy(result) {

			@Override
			public Map<String, Object> getSpecificProperties() {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
				result.put(CONTROL_AUTO_MANAGEMENT_ENABLED_PROPERTY_KEY, !isFieldControlAutoManaged());
				return result;
			}
		};
	}

	public Component createFieldControl() {
		IFieldControlInput controlInput = this;
		if (!controlInput.getControlData().isFormControlMandatory()) {
			Component result = createCustomFieldControl();
			if (result != null) {
				return result;
			}
		}
		if (controlInput.getControlData().isNullValueDistinct()) {
			try {
				return new NullableControl(this.swingRenderer, controlInput);
			} catch (RejectedFieldControlInputException e) {
			}
		}
		Object value = controlInput.getControlData().getValue();
		controlInput = new FieldControlInputProxy(controlInput) {
			BufferedFieldControlData bufferedFieldControlData = new BufferedFieldControlData(super.getControlData(),
					value);

			@Override
			public IFieldControlData getControlData() {
				return bufferedFieldControlData;
			}
		};
		if (value == null) {
			try {
				return new NullControl(swingRenderer, controlInput);
			} catch (RejectedFieldControlInputException e) {
			}
		}
		final SpecificitiesIdentifier specificitiesIdentifier = controlInput.getControlData().getType().getSource()
				.getSpecificitiesIdentifier();
		final ITypeInfo actualValueType = this.swingRenderer.reflectionUI
				.buildTypeInfo(new TypeInfoSourceProxy(this.swingRenderer.reflectionUI.getTypeInfoSource(value)) {
					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return specificitiesIdentifier;
					}

					@Override
					protected String getTypeInfoProxyFactoryIdentifier() {
						return "ActualFieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", form="
								+ form.getName() + ", field=" + field.getName() + "]";
					}
				});
		if (!controlInput.getControlData().getType().getName().equals(actualValueType.getName())) {
			controlInput = new FieldControlInputProxy(controlInput) {
				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {
						@Override
						public ITypeInfo getType() {
							return actualValueType;
						}
					};
				}
			};
			try {
				return new MutableTypeControl(this.swingRenderer, controlInput);
			} catch (RejectedFieldControlInputException e) {
			}
		}
		if (controlInput.getControlData().isFormControlEmbedded()) {
			try {
				return new EmbeddedFormControl(this.swingRenderer, controlInput);
			} catch (RejectedFieldControlInputException e) {
			}
		} else {
			try {
				return new DialogAccessControl(this.swingRenderer, controlInput);
			} catch (RejectedFieldControlInputException e) {
			}
		}
		throw new RejectedFieldControlInputException();
	}

	public Component createCustomFieldControl() {
		IFieldControlInput controlInput = this;

		IFieldControlPlugin currentPlugin = getCurrentPlugin();
		if (currentPlugin == null) {
			if (controlInput.getControlData().getType() instanceof IEnumerationTypeInfo) {
				try {
					return new EnumerationControl(swingRenderer, controlInput);
				} catch (RejectedFieldControlInputException e) {
				}
			}
			if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(controlInput.getControlData().getType())) {
				try {
					return new PolymorphicControl(swingRenderer, controlInput);
				} catch (RejectedFieldControlInputException e) {
				}
			}
			if (!controlInput.getControlData().isNullValueDistinct()) {
				ITypeInfo fieldType = controlInput.getControlData().getType();
				if (fieldType instanceof IListTypeInfo) {
					try {
						return new ListControl(swingRenderer, controlInput);
					} catch (RejectedFieldControlInputException e) {
					}
				}
				final Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(fieldType.getName());
				} catch (ClassNotFoundException e) {
					return null;
				}
				if (boolean.class.equals(javaType) || Boolean.class.equals(javaType)) {
					try {
						return new CheckBoxControl(swingRenderer, controlInput);
					} catch (RejectedFieldControlInputException e) {
					}
				}
				if (ClassUtils.isPrimitiveClassOrWrapper(javaType)) {
					try {
						return new PrimitiveValueControl(swingRenderer, controlInput);
					} catch (RejectedFieldControlInputException e) {
					}
				}
				if (String.class.equals(javaType)) {
					try {
						return new TextControl(swingRenderer, controlInput);
					} catch (RejectedFieldControlInputException e) {
					}
				}
			}
		}

		if (currentPlugin != null) {
			if (controlInput.getControlData().isNullValueDistinct() && !currentPlugin.canDisplayDistinctNullValue()) {
				controlInput = new FieldControlInputProxy(controlInput) {
					@Override
					public IFieldControlData getControlData() {
						return currentPlugin.filterDistinctNullValueControlData(swingRenderer, super.getControlData());
					}
				};
				try {
					return new NullableControl(this.swingRenderer, controlInput);
				} catch (RejectedFieldControlInputException e) {
				}
			} else {
				try {
					try {
						return (Component) currentPlugin.createControl(swingRenderer, controlInput);
					} catch (RejectedFieldControlInputException e) {
					}
				} catch (Throwable t) {
					return createFieldErrorControl(t);
				}
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
						return MiscUtils.getPrettyErrorMessage(t);
					}
				};
			}
		}), BorderLayout.CENTER);
		result.setBorder(SwingRendererUtils.getErrorBorder());
		result.setName("errorFieldControl [field=" + field.getName() + ", parent=" + form.getName() + "]");
		return result;
	}

	protected Map<String, Object> getFieldControlSelectionCriteria(IFieldControlData controlData) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("typeName", controlData.getType().getName());
		result.put("polymorphic", ReflectionUIUtils.hasPolymorphicInstanceSubTypes(controlData.getType()));
		result.put("fieldControlPluginIdentifier",
				ReflectionUIUtils.getFieldControlPluginIdentifier(controlData.getType().getSpecificProperties()));
		result.put("nullValueDistinct", controlData.isNullValueDistinct());
		result.put("formControlEmbedded", controlData.isFormControlEmbedded());
		result.put("formControlMandatory", controlData.isFormControlMandatory());
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

	protected class FieldControlData extends AbstractFieldControlData {

		protected IFieldInfo finalField;

		public FieldControlData(IFieldInfo finalField) {
			super(swingRenderer.getReflectionUI());
			this.finalField = finalField;
		}

		@Override
		protected Object getObject() {
			return form.getObject();
		}

		@Override
		protected IFieldInfo getField() {
			return finalField;
		}

		private FieldControlPlaceHolder getEnclosingInstance() {
			return FieldControlPlaceHolder.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
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
			FieldControlData other = (FieldControlData) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (!super.equals(other))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InitialFieldControlData [of=" + getEnclosingInstance() + ", finalField=" + getField() + "]";
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
