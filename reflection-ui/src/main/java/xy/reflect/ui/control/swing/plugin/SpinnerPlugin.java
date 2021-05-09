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
package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

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

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ReflectionUtils;
import xy.reflect.ui.util.ReschedulableTask;
import xy.reflect.ui.util.ConversionUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

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
			javaType = ReflectionUtils.primitiveToWrapperClass(javaType);
		}
		return Number.class.isAssignableFrom(javaType);
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
						Spinner.this.commitChanges();
					}
				});
			}

			@Override
			protected long getExecutionDelayMilliseconds() {
				return Spinner.this.getCommitDelayMilliseconds();
			}
		};

		public Spinner(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			try {
				this.numberClass = ReflectionUtils.getCachedClassforName(input.getControlData().getType().getName());
				if (this.numberClass.isPrimitive()) {
					this.numberClass = ReflectionUtils.primitiveToWrapperClass(numberClass);
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
					onSpin();
				}
			});
			addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if ("editor".equals(evt.getPropertyName())) {
						JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) Spinner.this.getEditor();
						final JFormattedTextField textField = (JFormattedTextField) editor.getTextField();
						textField.setHorizontalAlignment(JTextField.LEFT);
						textField.getDocument().addDocumentListener(new DocumentListener() {

							private void anyUpdate() {
								if (listenerDisabled) {
									return;
								}
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										String string = textField.getText();
										Object value;
										DefaultFormatter formatter = ((DefaultFormatter) textField.getFormatter());
										try {
											value = formatter.stringToValue(string);
										} catch (ParseException e) {
											swingRenderer.getReflectionUI().logError(e);
											displayError(MiscUtils.getPrettyErrorMessage(e));
											return;
										}
										int caretPosition = textField.getCaretPosition();
										Spinner.this.setValue(value);
										displayError(null);
										textField.setCaretPosition(
												Math.min(caretPosition, textField.getText().length()));
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
								onFocusLoss();
							}

							@Override
							public void focusGained(FocusEvent e) {
							}
						});

					}
				}
			});
		}

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
				Number value = (Number) data.getValue();
				if (value == null) {
					value = (Number) ((SpinnerNumberModel) getModel()).getMinimum();
				}
				setValue(value);
				displayError(null);
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

		@Override
		public boolean displayError(String msg) {
			SwingRendererUtils.displayErrorOnBorderAndTooltip(this, this, msg, swingRenderer);
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
		public void validateSubForm() throws Exception {
		}

		@Override
		public void addMenuContribution(MenuModel menuModel) {
		}

		@Override
		public String toString() {
			return "Spinner [data=" + data + "]";
		}
	}

}
