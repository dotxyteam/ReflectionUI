/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.control.swing.plugin;

import java.awt.ComponentOrientation;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

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

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.DelayedUpdateProcess;
import xy.reflect.ui.util.NumberUtils;
import xy.reflect.ui.util.ReflectionUIError;

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
		protected DelayedUpdateProcess dataUpdateProcess = new DelayedUpdateProcess() {
			@Override
			protected void commit() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Spinner.this.commitChanges();
					}
				});
			}

			@Override
			protected long getCommitDelayMilliseconds() {
				return Spinner.this.getCommitDelayMilliseconds();
			}
		};

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
											return;
										}
										int caretPosition = textField.getCaretPosition();
										Spinner.this.setValue(value);
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
				}
				Number value = (Number) data.getValue();
				if (value == null) {
					value = (Number) ((SpinnerNumberModel) getModel()).getMinimum();
				}
				setValue(value);
				return true;
			} finally {
				listenerDisabled = false;
			}
		}

		protected Number getConvertedNumber(String s) {
			Number result = parseNumber(s);
			result = NumberUtils.convertNumberToTargetClass(result, numberClass);
			return result;
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return false;
		}

		protected void onSpin() {
			if (listenerDisabled) {
				return;
			}
			dataUpdateProcess.cancelCommitSchedule();
			dataUpdateProcess.scheduleCommit();
		}

		protected void onFocusLoss() {
			if (dataUpdateProcess.isCommitScheduled()) {
				dataUpdateProcess.cancelCommitSchedule();
				commitChanges();
			}
		}

		protected long getCommitDelayMilliseconds() {
			return 500;
		}

		protected void commitChanges() {
			Object value = NumberUtils.convertNumberToTargetClass((Number) Spinner.this.getValue(), numberClass);
			data.setValue(value);
		}

		@Override
		public boolean isAutoManaged() {
			return false;
		}

		@Override
		public boolean requestCustomFocus() {
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
