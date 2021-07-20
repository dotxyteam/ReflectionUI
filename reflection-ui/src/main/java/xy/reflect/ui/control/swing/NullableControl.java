


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
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.CustomContext;
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
import xy.reflect.ui.control.swing.util.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
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
		data.addInBuffer(value);
		refreshNullStatusControl(refreshStructure);
		data.addInBuffer(value);
		refreshSubControl(refreshStructure);
		if (!Arrays.asList(getComponents()).contains(currentSubControl) || refreshStructure) {
			removeAll();
			if (!isCaptionDisplayedOnNullStatusControl()) {
				add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.CENTER), BorderLayout.WEST);
				add(currentSubControl, BorderLayout.CENTER);
				nullStatusControl.setText("");
				((JComponent) currentSubControl).setBorder(
						BorderFactory.createTitledBorder(swingRenderer.prepareMessageToDisplay(data.getCaption())));
				{
					if (data.getLabelForegroundColor() != null) {
						((TitledBorder) ((JComponent) currentSubControl).getBorder())
								.setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
					}
					if (data.getBorderColor() != null) {
						((TitledBorder) ((JComponent) currentSubControl).getBorder()).setBorder(
								BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					}
				}
			} else {
				add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.WEST), BorderLayout.NORTH);
				add(currentSubControl, BorderLayout.CENTER);
				nullStatusControl.setText(swingRenderer.prepareMessageToDisplay(data.getCaption()));
				((JComponent) currentSubControl).setBorder(BorderFactory.createTitledBorder(""));
				if (data.getBorderColor() != null) {
					((TitledBorder) ((JComponent) currentSubControl).getBorder()).setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				}
			}
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		return true;
	}

	protected boolean isCaptionDisplayedOnNullStatusControl() {
		return !(currentSubControl instanceof NullControl) || !isSubControlDisplayed();
	}

	protected boolean isSubControlDisplayed() {
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
			refreshUI(false);
		} else {
			Object newValue = swingRenderer.onTypeInstanciationRequest(NullableControl.this, data.getType());
			if (newValue == null) {
				return;
			}
			ReflectionUIUtils.setFieldValueThroughModificationStack(data, newValue, input.getModificationStack(),
					ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
			refreshUI(false);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					requestCustomFocus();
				}
			});
		}
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(currentSubControl, swingRenderer);
	}

	protected void refreshNullStatusControl(boolean refreshStructure) {
		setNullStatusControlState(data.getValue() == null);
		if (refreshStructure) {
			nullStatusControl.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
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
				data.addInBuffer(value);
				subFormBuilder.refreshEditorForm((Form) currentSubControl, refreshStructure);
				return;
			}
		}
		// change the displayed sub-control
		if ((value == null) && (currentError == null)) {
			if (currentSubControl instanceof Form) {
				// clear the sub-form before hiding it
				data.addInBuffer(null);
				subFormBuilder.refreshEditorForm((Form) currentSubControl, refreshStructure);
			}
			if (nullControl == null) {
				nullControl = createNullControl();
			}
			currentSubControl = nullControl;
		} else {
			if (subForm == null) {
				data.addInBuffer(value);
				subForm = createSubForm();
				if (currentError != null) {
					// display the current error (over the last valid value)
					data.addInBuffer(new ErrorOccurrence(currentError));
					subFormBuilder.refreshEditorForm(subForm, refreshStructure);
				}
			} else {
				if (currentError != null) {
					// display the current error (over the last valid value)
					value = new ErrorOccurrence(new ErrorWithDefaultValue(currentError, value));
				}
				data.addInBuffer(value);
				subFormBuilder.refreshEditorForm(subForm, refreshStructure);
			}
			currentSubControl = subForm;
		}
		currentSubControl.setVisible(isSubControlDisplayed());
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
					swingRenderer.handleObjectException(NullableControl.this, t);
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
		return new SubFormBuilder(swingRenderer, input, subContext, commitExceptionHandler);
	}

	protected IContext getSubContext() {
		return new CustomContext("NullableInstance");
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public void validateSubForms() throws Exception {
		if (currentSubControl instanceof Form) {
			((Form) currentSubControl).validateForm();
		}
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
		if (currentSubControl instanceof Form) {
			((Form) currentSubControl).addMenuContributionTo(menuModel);
		}
	}

	@Override
	public String toString() {
		return "NullableControl [data=" + data + "]";
	}

	protected static class SubFormBuilder extends AbstractEditorFormBuilder {

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected IContext subContext;
		protected Listener<Throwable> commitExceptionHandler;

		public SubFormBuilder(SwingRenderer swingRenderer, IFieldControlInput input, IContext subContext,
				Listener<Throwable> commitExceptionHandler) {
			this.swingRenderer = swingRenderer;
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
		protected void handleRealtimeLinkCommitException(Throwable t) {
			commitExceptionHandler.handle(t);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
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
		protected boolean isParentModificationFake() {
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
