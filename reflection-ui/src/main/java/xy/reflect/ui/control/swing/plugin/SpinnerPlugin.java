


package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutorService;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReschedulableTask;
import xy.reflect.ui.util.ConversionUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control plugin that allows to use spinners.
 * 
 * @author olitank
 *
 */
public class SpinnerPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Spinner";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = ClassUtils.primitiveToWrapperClass(javaType);
		}
		return Number.class.isAssignableFrom(javaType) && Comparable.class.isAssignableFrom(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new SpinnerConfiguration();
	}

	@Override
	public Spinner createControl(Object renderer, IFieldControlInput input) {
		return new Spinner((SwingRenderer) renderer, input);
	}

	protected static Number parseNumber(String s) {
		try {
			return Long.valueOf(s);
		} catch (NumberFormatException ignore) {
		}
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException ignore) {
		}
		throw new ReflectionUIError("Cannot convert string to number (long or double): '" + s + "'");
	}

	public static class SpinnerConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String minimum = "0";
		public String maximum = "100";
		public String stepSize = "1";

		public void validate() {
			parseNumber(minimum);
			parseNumber(maximum);
			parseNumber(stepSize);
		}
	}

	public class Spinner extends JSpinner implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected boolean listenerDisabled = false;
		protected Class<?> numberClass;
		protected ReschedulableTask dataUpdateProcess = new ReschedulableTask() {
			@Override
			protected void execute() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							Spinner.this.commitChanges();
						} catch (Throwable t) {
							swingRenderer.handleObjectException(Spinner.this, t);
						}
					}
				});
			}

			@Override
			protected ExecutorService getTaskExecutor() {
				return swingRenderer.getDelayedUpdateExecutor();
			}

			@Override
			protected long getExecutionDelayMilliseconds() {
				return Spinner.this.getCommitDelayMilliseconds();
			}
		};
		protected Throwable currentConversionError;
		protected String currentDataErrorMessage;

		public Spinner(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			try {
				this.numberClass = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ClassUtils.primitiveToWrapperClass(numberClass);
				}
			} catch (ClassNotFoundException e1) {
				throw new ReflectionUIError(e1);
			}
			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			setupEvents();
			refreshUI(true);
		}

		protected void setupEvents() {
			addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					try {
						onSpin();
					} catch (Throwable t) {
						swingRenderer.handleObjectException(Spinner.this, t);
					}
				}
			});
			addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if ("editor".equals(evt.getPropertyName())) {
						JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) Spinner.this.getEditor();
						final JFormattedTextField textField = (JFormattedTextField) editor.getTextField();
						textField.setFormatterFactory(
								new DefaultFormatterFactory(ReflectionUIUtils.getDefaultNumberFormatter(numberClass)));
						textField.setHorizontalAlignment(JTextField.LEFT);
						textField.getDocument().addDocumentListener(new DocumentListener() {

							private void anyUpdate() {
								if (listenerDisabled) {
									return;
								}
								SwingUtilities.invokeLater(new Runnable() {

									@SuppressWarnings({ "unchecked", "rawtypes" })
									@Override
									public void run() {
										try {
											String string = textField.getText();
											DefaultFormatter formatter = ((DefaultFormatter) textField.getFormatter());
											Object value;
											try {
												value = formatter.stringToValue(string);
												SpinnerNumberModel spinnerNumberModel = (SpinnerNumberModel) getModel();
												if (((Comparable) value)
														.compareTo(spinnerNumberModel.getMaximum()) > 0) {
													value = spinnerNumberModel.getMaximum();
													textField.setText(formatter.valueToString(value));
												}
												if (((Comparable) value)
														.compareTo(spinnerNumberModel.getMinimum()) < 0) {
													value = spinnerNumberModel.getMinimum();
													textField.setText(formatter.valueToString(value));
												}
												currentConversionError = null;
											} catch (Throwable t) {
												currentConversionError = t;
												return;
											} finally {
												updateErrorDisplay();
											}
											int caretPosition = textField.getCaretPosition();
											Spinner.this.setValue(value);
											textField.setCaretPosition(
													Math.min(caretPosition, textField.getText().length()));
										} catch (Throwable t) {
											swingRenderer.handleObjectException(Spinner.this, t);
										}
									}
								});

							}

							@Override
							public void removeUpdate(DocumentEvent e) {
								anyUpdate();
							}

							@Override
							public void insertUpdate(DocumentEvent e) {
								anyUpdate();
							}

							@Override
							public void changedUpdate(DocumentEvent e) {
								anyUpdate();
							}
						});
						textField.addFocusListener(new FocusListener() {

							@Override
							public void focusLost(FocusEvent e) {
								try {
									onFocusLoss();
								} catch (Throwable t) {
									swingRenderer.handleObjectException(Spinner.this, t);
								}
							}

							@Override
							public void focusGained(FocusEvent e) {
							}
						});

					}
				}
			});
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public boolean refreshUI(boolean refreshStructure) {
			listenerDisabled = true;
			try {
				if (refreshStructure) {
					SpinnerConfiguration controlCustomization = (SpinnerConfiguration) loadControlCustomization(input);
					Number minimum = getConvertedNumber(controlCustomization.minimum);
					Number maximum = getConvertedNumber(controlCustomization.maximum);
					Number stepSize = getConvertedNumber(controlCustomization.stepSize);
					Number value = minimum;
					setModel(new SpinnerNumberModel(value, (Comparable<?>) minimum, (Comparable<?>) maximum, stepSize));
					setEnabled(!data.isGetOnly());
					if (data.getBorderColor() != null) {
						setBorder(BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					} else {
						setBorder(new JSpinner().getBorder());
					}
					if (data.isGetOnly()) {
						getEditor().getComponent(0)
								.setBackground(new JSpinner().getEditor().getComponent(0).getBackground());
						getEditor().getComponent(0)
								.setForeground(new JSpinner().getEditor().getComponent(0).getForeground());
					} else {
						if (data.getEditorBackgroundColor() != null) {
							getEditor().getComponent(0)
									.setBackground(SwingRendererUtils.getColor(data.getEditorBackgroundColor()));
						} else {
							getEditor().getComponent(0)
									.setBackground(new JSpinner().getEditor().getComponent(0).getBackground());
						}
						if (data.getEditorForegroundColor() != null) {
							getEditor().getComponent(0)
									.setForeground(SwingRendererUtils.getColor(data.getEditorForegroundColor()));
						} else {
							getEditor().getComponent(0)
									.setForeground(new JSpinner().getEditor().getComponent(0).getForeground());
						}
					}
				}
				SpinnerNumberModel spinnerNumberModel = (SpinnerNumberModel) getModel();
				Number value = (Number) data.getValue();
				if (value == null) {
					value = (Number) spinnerNumberModel.getMinimum();
				}
				if (((Comparable) value).compareTo(spinnerNumberModel.getMaximum()) > 0) {
					throw new ReflectionUIError("The value is greater than the maximum value: " + value + " > "
							+ spinnerNumberModel.getMaximum());
				}
				if (((Comparable) value).compareTo(spinnerNumberModel.getMinimum()) < 0) {
					throw new ReflectionUIError("The value is less than the minimum value: " + value + " < "
							+ spinnerNumberModel.getMinimum());
				}
				currentConversionError = null;
				updateErrorDisplay();
				setValue(value);
				return true;
			} finally {
				listenerDisabled = false;
			}
		}

		protected Number getConvertedNumber(String s) {
			Number result = parseNumber(s);
			result = ConversionUtils.convertNumberToTargetClass(result, numberClass);
			return result;
		}

		protected void updateErrorDisplay() {
			if (currentConversionError != null) {
				SwingRendererUtils.displayErrorOnBorderAndTooltip(this, this,
						MiscUtils.getPrettyErrorMessage(currentConversionError), swingRenderer);
				return;
			}
			if (currentDataErrorMessage != null) {
				SwingRendererUtils.displayErrorOnBorderAndTooltip(this, this, currentDataErrorMessage, swingRenderer);
				return;
			}
			SwingRendererUtils.displayErrorOnBorderAndTooltip(this, this, null, swingRenderer);
		}

		@Override
		public boolean displayError(String msg) {
			currentDataErrorMessage = msg;
			updateErrorDisplay();
			return true;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		protected void onSpin() {
			if (listenerDisabled) {
				return;
			}
			dataUpdateProcess.reschedule();
		}

		protected void onFocusLoss() {
			if (dataUpdateProcess.isScheduled()) {
				dataUpdateProcess.cancelSchedule();
				commitChanges();
			}
		}

		protected long getCommitDelayMilliseconds() {
			return 500;
		}

		protected void commitChanges() {
			Object value = ConversionUtils.convertNumberToTargetClass((Number) Spinner.this.getValue(), numberClass);
			data.setValue(value);
		}

		@Override
		public boolean isAutoManaged() {
			return false;
		}

		@Override
		public boolean requestCustomFocus() {
			if (data.isGetOnly()) {
				return false;
			}
			for (Component c : getComponents()) {
				if (SwingRendererUtils.requestAnyComponentFocus(c, swingRenderer)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void validateSubForms() throws Exception {
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
		}

		@Override
		public String toString() {
			return "Spinner [data=" + data + "]";
		}
	}

}
