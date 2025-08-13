
package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.AbstractFieldControlData;
import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.ErrorHandlingFieldControlData;
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
import xy.reflect.ui.control.swing.ComboBoxControl;
import xy.reflect.ui.control.swing.ErrorDisplayControl;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.MutableTypeControl;
import xy.reflect.ui.control.swing.NullControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.PolymorphicControl;
import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.builder.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ValueOptionsAsEnumerationFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Listener;
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

	public static final String CONTROL_BASED_MODIFICATION_STACK_MANAGEMENT_ENABLED_PROPERTY_KEY = FieldControlPlaceHolder.class
			.getName() + ".CONTROL_MODIFICATION_STACK_MANAGEMENT_ENABLED";
	public static final String CONTROL_BASED_VALUE_ACCESS_ERRORS_MANAGEMENT_ENABLED_PROPERTY_KEY = FieldControlPlaceHolder.class
			.getName() + ".CONTROL_BASED_VALUE_ACCESS_ERRORS_MANAGEMENT_ENABLED";

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

	@Override
	public Dimension getMinimumSize() {
		/*
		 * This method is overridden to avoid issues with the default parent form
		 * GridBagLayout. If another layout is used then this method may need to be
		 * overridden again. The minimum size must be equal to the preferred size (that
		 * may be dynamic in our case because of scroll bars that may appear) since when
		 * there is not enough space, the GridBagLayout sets its components to their
		 * minimum size, causing a brutal ugly effect.
		 */
		return super.getPreferredSize();
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
	public FieldContext getContext() {
		ITypeInfo objectType = this.swingRenderer.reflectionUI
				.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(getObject()));
		return new FieldContext(objectType.getName(), field.getName());
	}

	@Override
	public ModificationStack getModificationStack() {
		return form.getModificationStack();
	}

	protected IFieldControlData makeFieldModificationsUndoable(final IFieldControlData data) {
		return new FieldControlDataProxy(data) {

			@Override
			public void setValue(Object newValue) {
				if (isModificationStackManagedByFieldControl()) {
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
			protected void displayError(Throwable error) {
				if (error != null) {
					swingRenderer.getReflectionUI().logDebug(error);
				}
				boolean errorDisplayed = (fieldControl instanceof IAdvancedFieldControl)
						&& ((IAdvancedFieldControl) fieldControl).displayError((error == null) ? null : error);
				if (!errorDisplayed) {
					super.displayError(error);
				}
			}

			@Override
			public Object getValue() {
				if (areValueAccessErrorsManagedByFieldControl()) {
					return data.getValue();
				}
				return super.getValue();
			}

			@Override
			public void setValue(Object newValue) {
				if (areValueAccessErrorsManagedByFieldControl()) {
					data.setValue(newValue);
					return;
				}
				super.setValue(newValue);
			}

		};
	}

	protected boolean isModificationStackManagedByFieldControl() {
		Component c = fieldControl;
		if (c == null) {
			return false;
		}
		if ((c instanceof IAdvancedFieldControl)) {
			IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
			if (fieldControl.isModificationStackManaged()) {
				return true;
			}
		}
		return false;
	}

	protected boolean areValueAccessErrorsManagedByFieldControl() {
		Component c = fieldControl;
		if (c == null) {
			return false;
		}
		if ((c instanceof IAdvancedFieldControl)) {
			IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
			if (fieldControl.areValueAccessErrorsManaged()) {
				return true;
			}
		}
		return false;
	}

	public Component getFieldControl() {
		return fieldControl;
	}

	public void refreshUI(boolean refreshStructure) {
		try {
			setVisible(!field.isHidden() && field.isRelevant(getObject()));
		} catch (Throwable t) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					swingRenderer.handleException(FieldControlPlaceHolder.this, t);
				}
			});
		}
		if (siblingCaptionControl != null) {
			siblingCaptionControl.setVisible(isVisible());
		}
		if (siblingOnlineHelpControl != null) {
			siblingOnlineHelpControl.setVisible(isVisible());
		}
		if (!isVisible()) {
			return;
		}
		if (fieldControl == null) {
			try {
				fieldControl = createFieldControl();
				/*
				 * Save the criteria in a field since the controlData internal structure may
				 * change:
				 */
				lastFieldControlSelectionCriteria = getFieldControlSelectionCriteria(controlData);
			} catch (Throwable t) {
				fieldControl = createFieldErrorControl(t);
			}
			fieldControl.setName("fieldControl [field=" + field.getName() + ", parent=" + form.getName() + "]");
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
			updateListSelectionTargetData();
		}
	}

	protected void updateListSelectionTargetData() {
		if (fieldControl instanceof ListControl) {
			ITypeInfo objectType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(form.getObject()));
			IFieldInfo selectionTargetField = ((IListTypeInfo) controlData.getType())
					.getSelectionTargetField(objectType);
			FieldControlData selectionTargetData;
			if (selectionTargetField != null) {
				selectionTargetData = new FieldControlData(selectionTargetField);
			} else {
				selectionTargetData = null;
			}
			if (!MiscUtils.equalsOrBothNull(selectionTargetData,
					((ListControl) fieldControl).getSelectionTargetData())) {
				((ListControl) fieldControl).setSelectionTargetData(selectionTargetData);
			}
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

	protected IFieldControlData createControlData() {
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
		result = addControlBasedModificationStackManagementStatusProperty(result);
		result = addControlBasedValueAccessErrorsManagementStatusProperty(result);
		return result;
	}

	protected IFieldControlData addControlBasedModificationStackManagementStatusProperty(IFieldControlData result) {
		return new FieldControlDataProxy(result) {

			@Override
			public Map<String, Object> getSpecificProperties() {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
				result.put(CONTROL_BASED_MODIFICATION_STACK_MANAGEMENT_ENABLED_PROPERTY_KEY,
						!isModificationStackManagedByFieldControl());
				return result;
			}
		};
	}

	protected IFieldControlData addControlBasedValueAccessErrorsManagementStatusProperty(IFieldControlData result) {
		return new FieldControlDataProxy(result) {

			@Override
			public Map<String, Object> getSpecificProperties() {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
				result.put(CONTROL_BASED_VALUE_ACCESS_ERRORS_MANAGEMENT_ENABLED_PROPERTY_KEY,
						!areValueAccessErrorsManagedByFieldControl());
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
		final Object value = controlInput.getControlData().getValue();
		final BufferedFieldControlData bufferedFieldControlData = new BufferedFieldControlData(
				controlInput.getControlData());
		controlInput = new FieldControlInputProxy(controlInput) {

			@Override
			public IFieldControlData getControlData() {
				return bufferedFieldControlData;
			}
		};
		if (value == null) {
			try {
				final IFieldControlInput finalControlInput = controlInput;
				final Component[] result = new Component[1];
				bufferedFieldControlData.returningValue(value, new Runnable() {
					@Override
					public void run() {
						result[0] = new NullControl(swingRenderer, finalControlInput);
					}
				});
				return result[0];
			} catch (RejectedFieldControlInputException e) {
			}
		}
		if (!controlInput.getControlData().getType().isPrimitive()) {
			final SpecificitiesIdentifier specificitiesIdentifier = controlInput.getControlData().getType().getSource()
					.getSpecificitiesIdentifier();
			final ITypeInfo actualValueType = this.swingRenderer.reflectionUI
					.getTypeInfo(new TypeInfoSourceProxy(this.swingRenderer.reflectionUI.getTypeInfoSource(value)) {
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
					final IFieldControlInput finalControlInput = controlInput;
					final Component[] result = new Component[1];
					bufferedFieldControlData.returningValue(value, new Runnable() {
						@Override
						public void run() {
							result[0] = new MutableTypeControl(swingRenderer, finalControlInput);
						}
					});
					return result[0];
				} catch (RejectedFieldControlInputException e) {
				}
			}
		}
		if (controlInput.getControlData().isFormControlEmbedded()) {
			try {
				final IFieldControlInput finalControlInput = controlInput;
				final Component[] result = new Component[1];
				bufferedFieldControlData.returningValue(value, new Runnable() {
					@Override
					public void run() {
						result[0] = new EmbeddedFormControl(swingRenderer, finalControlInput);
					}
				});
				return result[0];
			} catch (RejectedFieldControlInputException e) {
			}
		} else {
			try {
				final IFieldControlInput finalControlInput = controlInput;
				final Component[] result = new Component[1];
				bufferedFieldControlData.returningValue(value, new Runnable() {
					@Override
					public void run() {
						result[0] = new DialogAccessControl(swingRenderer, finalControlInput);
					}
				});
				return result[0];
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
					return new ComboBoxControl(swingRenderer, controlInput);
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
				if (boolean.class.getName().equals(fieldType.getName())
						|| Boolean.class.getName().equals(fieldType.getName())) {
					try {
						return new CheckBoxControl(swingRenderer, controlInput);
					} catch (RejectedFieldControlInputException e) {
					}
				}
				if (Arrays.stream(ClassUtils.PRIMITIVE_CLASSES).anyMatch(c -> c.getName().equals(fieldType.getName()))
						|| Arrays.stream(ClassUtils.PRIMITIVE_WRAPPER_CLASSES)
								.anyMatch(c -> c.getName().equals(fieldType.getName()))) {
					try {
						return new PrimitiveValueControl(swingRenderer, controlInput);
					} catch (RejectedFieldControlInputException e) {
					}
				}
				if (String.class.getName().equals(fieldType.getName())) {
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
					return new NullableControl(this.swingRenderer, controlInput) {

						private static final long serialVersionUID = 1L;

						@Override
						protected AbstractEditorFormBuilder createSubFormBuilder(SwingRenderer swingRenderer,
								IFieldControlInput input, IContext subContext,
								Listener<Throwable> commitExceptionHandler) {
							return new SubFormBuilder(swingRenderer, this, input, subContext, commitExceptionHandler) {

								@Override
								protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
									return new TypeInfoSourceProxy(super.getEncapsulatedFieldDeclaredTypeSource()) {

										@Override
										protected String getTypeInfoProxyFactoryIdentifier() {
											return "FieldControlPluginSettingsCopyingTypeInfoProxyFactory [pluginIdentifier="
													+ currentPlugin.getIdentifier() + ", parentContext="
													+ getContext().getIdentifier() + "]";
										}

										@Override
										public ITypeInfo buildTypeInfo(ReflectionUI reflectionUI) {
											return new InfoProxyFactory() {

												@Override
												protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
													Map<String, Object> result = new HashMap<String, Object>(
															super.getSpecificProperties(type));
													SwingRendererUtils.setCurrentFieldControlPlugin(swingRenderer,
															result, currentPlugin);
													ReflectionUIUtils.setFieldControlPluginConfiguration(result,
															currentPlugin.getIdentifier(),
															ReflectionUIUtils.getFieldControlPluginConfiguration(
																	input.getControlData().getType()
																			.getSpecificProperties(),
																	currentPlugin.getIdentifier()));
													ReflectionUIUtils.setFieldControlPluginManagementDisabled(result,
															true);
													return result;
												}
											}.wrapTypeInfo(super.buildTypeInfo(reflectionUI));
										}

									};
								}

							};
						}

					};
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
		swingRenderer.getReflectionUI().logError(t);
		return new ErrorDisplayControl(swingRenderer, this, t);
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
