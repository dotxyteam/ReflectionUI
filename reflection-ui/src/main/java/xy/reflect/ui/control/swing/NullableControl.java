
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.ErrorOccurrence;
import xy.reflect.ui.control.ErrorWithDefaultValue;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.builder.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that allows to display/set/unset the null value. A sub-form is
 * used to display the non-null values.
 * 
 * @author olitank
 *
 */
public class NullableControl extends ControlPanel implements IAdvancedFieldControl {

	protected SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected BufferedFieldControlData data;
	protected JCheckBox nullStatusControl;
	protected Component currentSubControl;
	protected NullControl nullControl;
	protected Form subForm;
	protected AbstractEditorFormBuilder subFormBuilder;
	protected IFieldControlInput input;
	protected Throwable currentError;

	public NullableControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		input = new FieldControlInputProxy(input) {

			IFieldControlData errorHandlingFieldControlData = new ErrorHandlingFieldControlData(super.getControlData(),
					swingRenderer, null) {

				@Override
				protected void handleError(Throwable t) {
					currentError = t;
				}
			};

			BufferedFieldControlData bufferedFieldControlData = new BufferedFieldControlData(
					errorHandlingFieldControlData);

			@Override
			public IFieldControlData getControlData() {
				return bufferedFieldControlData;
			}
		};
		this.input = input;
		this.data = (BufferedFieldControlData) input.getControlData();
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		nullStatusControl = createNullStatusControl();
		refreshUI(true);
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		Object value = data.getValue();
		data.returningValue(value, new Runnable() {
			@Override
			public void run() {
				refreshNullStatusControl(refreshStructure);
			}
		});
		data.returningValue(value, new Runnable() {
			@Override
			public void run() {
				refreshSubControl(refreshStructure);
			}
		});
		if (!Arrays.asList(getComponents()).contains(currentSubControl) || refreshStructure) {
			removeAll();
			if (!shouldCaptionBeDisplayedOnNullStatusControl()) {
				add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.CENTER), BorderLayout.WEST);
				add(currentSubControl, BorderLayout.CENTER);
				nullStatusControl.setText("");
				SwingRendererUtils.showFieldCaptionOnBorder(data, (JComponent) currentSubControl,
						new Accessor<Border>() {
							@Override
							public Border get() {
								return new ControlPanel().getBorder();
							}
						}, swingRenderer);
			} else {
				add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.WEST), BorderLayout.NORTH);
				add(currentSubControl, BorderLayout.CENTER);
				nullStatusControl.setText(swingRenderer.prepareMessageToDisplay(data.getCaption()));
				if (data.getBorderColor() != null) {
					((JComponent) currentSubControl).setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				} else {
					((JComponent) currentSubControl).setBorder(BorderFactory.createTitledBorder(""));
				}
			}
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		return true;
	}

	protected boolean shouldCaptionBeDisplayedOnNullStatusControl() {
		return !(currentSubControl instanceof NullControl) || !shouldSubControlBeDisplayed();
	}

	protected boolean shouldSubControlBeDisplayed() {
		return !((currentSubControl instanceof NullControl) && (data.getNullValueLabel() == null));
	}

	public Component getSubControl() {
		return currentSubControl;
	}

	protected void setNullStatusControlState(boolean b) {
		((JCheckBox) nullStatusControl).setSelected(!b);
	}

	protected boolean getNullStatusControlState() {
		return !((JCheckBox) nullStatusControl).isSelected();
	}

	protected void onNullingControlStateChange() {
		if (getNullStatusControlState()) {
			ReflectionUIUtils.setFieldValueThroughModificationStack(data, null, input.getModificationStack(),
					ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
		} else {
			Object newValue;
			try {
				newValue = getNewValue();
			} catch (Throwable t) {
				refreshUI(false);
				throw new ReflectionUIError(t);
			}
			if (newValue == null) {
				refreshUI(false);
				return;
			}
			ReflectionUIUtils.setFieldValueThroughModificationStack(data, newValue, input.getModificationStack(),
					ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					requestCustomFocus();
				}
			});
		}
	}

	protected Object getNewValue() {
		return swingRenderer.onTypeInstantiationRequest(NullableControl.this, data.getType());
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(currentSubControl, swingRenderer);
	}

	protected void refreshNullStatusControl(boolean refreshStructure) {
		setNullStatusControlState(data.getValue() == null);
		if (refreshStructure) {
			nullStatusControl.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
			if (data.getLabelCustomFontResourcePath() != null) {
				nullStatusControl.setFont(SwingRendererUtils.loadFontThroughCache(data.getLabelCustomFontResourcePath(),
						ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
						.deriveFont(nullStatusControl.getFont().getStyle(), nullStatusControl.getFont().getSize()));
			} else {
				nullStatusControl.setFont(new JCheckBox().getFont());
			}
			nullStatusControl.setEnabled(!data.isGetOnly());
		}
	}

	public void refreshSubControl(boolean refreshStructure) {
		if (refreshStructure) {
			nullControl = null;
			subForm = null;
		}
		Object value = data.getValue();
		// refresh the currently displayed sub-form if possible
		if (value != null) {
			if (currentSubControl instanceof Form) {
				if (currentError != null) {
					// display the current error (over the last valid value)
					value = new ErrorOccurrence(new ErrorWithDefaultValue(currentError, value));
				}
				data.returningValue(value, new Runnable() {
					@Override
					public void run() {
						subFormBuilder.reloadValue((Form) currentSubControl, refreshStructure);
					}
				});
				return;
			}
		}
		// change the displayed sub-control
		if ((value == null) && (currentError == null)) {
			if (currentSubControl instanceof Form) {
				// clear the sub-form before hiding it
				data.returningValue(null, new Runnable() {
					@Override
					public void run() {
						subFormBuilder.reloadValue((Form) currentSubControl, refreshStructure);
					}
				});
			}
			if (nullControl == null) {
				nullControl = createNullControl();
			}
			currentSubControl = nullControl;
		} else {
			if (subForm == null) {
				data.returningValue(value, new Runnable() {
					@Override
					public void run() {
						subForm = createSubForm();
					}
				});
				if (currentError != null) {
					// display the current error (over the last valid value)
					data.returningValue(new ErrorOccurrence(currentError), new Runnable() {
						@Override
						public void run() {
							subFormBuilder.reloadValue(subForm, refreshStructure);
						}
					});
				}
			} else {
				if (currentError != null) {
					// display the current error (over the last valid value)
					value = new ErrorOccurrence(new ErrorWithDefaultValue(currentError, value));
				}
				data.returningValue(value, new Runnable() {
					@Override
					public void run() {
						subFormBuilder.reloadValue(subForm, refreshStructure);
					}
				});
			}
			currentSubControl = subForm;
		}
		if (currentSubControl.isVisible() != shouldSubControlBeDisplayed()) {
			currentSubControl.setVisible(shouldSubControlBeDisplayed());
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected JCheckBox createNullStatusControl() {
		JCheckBox result = new JCheckBox();
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
				} catch (Throwable t) {
					swingRenderer.handleException(NullableControl.this, t);
				}
			}
		});
		return result;
	}

	protected NullControl createNullControl() {
		NullControl result = new NullControl(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(super.getControlData()) {

					@Override
					public String getCaption() {
						return "";
					}

					@Override
					public void setValue(Object value) {
						ReflectionUIUtils.setFieldValueThroughModificationStack(base, value,
								input.getModificationStack(),
								ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
					}

				};
			}
		});
		if (!data.isGetOnly()) {
			result.setActivationAction(new Runnable() {
				@Override
				public void run() {
					setNullStatusControlState(false);
					onNullingControlStateChange();
				}
			});
		}
		return result;
	}

	protected Form createSubForm() {
		subFormBuilder = createSubFormBuilder(swingRenderer, input, getSubContext(), new Listener<Throwable>() {
			@Override
			public void handle(Throwable t) {
				throw new ReflectionUIError(t);
			}
		});
		Form result = subFormBuilder.createEditorForm(true, false);
		return result;
	}

	protected AbstractEditorFormBuilder createSubFormBuilder(SwingRenderer swingRenderer, IFieldControlInput input,
			IContext subContext, Listener<Throwable> commitExceptionHandler) {
		return new SubFormBuilder(swingRenderer, this, input, subContext, commitExceptionHandler);
	}

	protected IContext getSubContext() {
		return new CustomContext("NullableInstance");
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(Throwable error) {
		return false;
	}

	@Override
	public boolean isModificationStackManaged() {
		return true;
	}

	@Override
	public boolean areValueAccessErrorsManaged() {
		return true;
	}

	@Override
	public void validateControlData(ValidationSession session) throws Exception {
		if (currentSubControl instanceof Form) {
			((Form) currentSubControl).validateForm(session);
		}
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
		if (currentSubControl instanceof Form) {
			((Form) currentSubControl).addMenuContributionTo(menuModel);
		}
	}

	protected static class SubFormBuilder extends AbstractEditorFormBuilder {

		protected SwingRenderer swingRenderer;
		protected NullableControl ownerComponent;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected IContext subContext;
		protected Listener<Throwable> commitExceptionHandler;

		public SubFormBuilder(SwingRenderer swingRenderer, NullableControl ownerComponent, IFieldControlInput input,
				IContext subContext, Listener<Throwable> commitExceptionHandler) {
			this.swingRenderer = swingRenderer;
			this.ownerComponent = ownerComponent;
			this.input = input;
			this.data = input.getControlData();
			this.subContext = subContext;
			this.commitExceptionHandler = commitExceptionHandler;
		}

		@Override
		protected IContext getContext() {
			return input.getContext();
		}

		@Override
		protected IContext getSubContext() {
			return subContext;
		}

		@Override
		protected boolean isEncapsulatedFormEmbedded() {
			return data.isFormControlEmbedded();
		}

		@Override
		protected boolean isEncapsulatedisControlValueValiditionEnabled() {
			return data.isControlValueValiditionEnabled();
		}

		@Override
		protected boolean isNullValueDistinct() {
			return false;
		}

		@Override
		protected boolean canCommitToParent() {
			return !data.isGetOnly();
		}

		@Override
		protected IModification createCommittingModification(Object newObjectValue) {
			return new FieldControlDataModification(data, newObjectValue);
		}

		@Override
		protected IModification createUndoModificationsReplacement() {
			return ReflectionUIUtils.createUndoModificationsReplacement(data);
		}

		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			commitExceptionHandler.handle(t);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected Runnable getParentControlRefreshJob() {
			return new Runnable() {
				@Override
				public void run() {
					ownerComponent.refreshUI(false);
				}
			};
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return data.getValueReturnMode();
		}

		@Override
		protected String getParentModificationTitle() {
			return FieldControlDataModification.getTitle(data.getCaption());
		}

		@Override
		protected boolean isParentModificationVolatile() {
			return data.isTransient();
		}

		@Override
		protected IInfoFilter getEncapsulatedFormFilter() {
			IInfoFilter result = data.getFormControlFilter();
			if (result == null) {
				result = IInfoFilter.DEFAULT;
			}
			return result;
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			return data.getType().getSource();
		}

		@Override
		protected ModificationStack getParentModificationStack() {
			return input.getModificationStack();
		}

		@Override
		protected Object loadValue() {
			return data.getValue();
		}
	}

}
