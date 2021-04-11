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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXDatePicker;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.DelayedUpdateProcess;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.JXDateTimePicker;

public class DateTimePickerPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Date And Time Picker";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Date.class.isAssignableFrom(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new DateTimePickerConfiguration();
	}

	@Override
	public IFieldControlData filterDistinctNullValueControlData(final Object renderer, IFieldControlData controlData) {
		return new FieldControlDataProxy(controlData) {

			@Override
			public Object createValue(ITypeInfo typeToInstanciate, boolean selectableConstructor) {
				if (typeToInstanciate.getName().equals(Date.class.getName())) {
					return new Date();
				}
				return super.createValue(typeToInstanciate, selectableConstructor);
			}

		};
	}

	@Override
	public DateTimePicker createControl(Object renderer, IFieldControlInput input) {
		return new DateTimePicker((SwingRenderer) renderer, input);
	}

	public static class DateTimePickerConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String dateFormat = "yyyy-MM-dd";
		public String timeFormat = "HH:mm:ss";
	}

	public class DateTimePicker extends JXDateTimePicker implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected boolean listenerDisabled = false;
		protected DelayedUpdateProcess textEditorChangesCommittingProcess = new DelayedUpdateProcess() {
			@Override
			protected void commit() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						DateTimePicker.this.commitTextEditorChanges();
					}
				});
			}

			@Override
			protected long getCommitDelayMilliseconds() {
				return DateTimePicker.this.getCommitDelayMilliseconds();
			}
		};

		public DateTimePicker(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			setupEvents();
			refreshUI(true);
		}

		@Override
		public void setDate(Date date) {
			super.setDate(date);
			fixTimeSpinnersNotUpdatingIssue(date);
		}

		protected void fixTimeSpinnersNotUpdatingIssue(Date date) {
			JSpinner timeSpinner;
			try {
				Field timeSpinnerField = JXDateTimePicker.class.getDeclaredField("timeSpinner");
				timeSpinnerField.setAccessible(true);
				timeSpinner = (JSpinner) timeSpinnerField.get(this);
			} catch (Exception e) {
				throw new ReflectionUIError(e);
			}
			try {
				timeSpinner.setValue(date);
			} catch (Exception ignore) {
			}
		}

		@Override
		public void commitEdit() throws ParseException {
			fixSecondsAndMillisecondsNotSettableIssue();
		}

		protected void fixSecondsAndMillisecondsNotSettableIssue() throws ParseException {
			Date date = getDate();
			if (date != null) {
				JSpinner timeSpinner;
				try {
					Field timeSpinnerField = JXDateTimePicker.class.getDeclaredField("timeSpinner");
					timeSpinnerField.setAccessible(true);
					timeSpinner = (JSpinner) timeSpinnerField.get(this);
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
				Date time = (Date) timeSpinner.getValue();
				GregorianCalendar timeCalendar = new GregorianCalendar();
				timeCalendar.setTime(time);

				GregorianCalendar calendar = new GregorianCalendar();
				calendar.setTime(date);
				calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
				calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
				calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
				calendar.set(Calendar.MILLISECOND, timeCalendar.get(Calendar.MILLISECOND));

				Date newDate = calendar.getTime();
				setDate(newDate);
			}
			JFormattedTextField _dateField;
			try {
				Field _dateFieldField = JXDatePicker.class.getDeclaredField("_dateField");
				_dateFieldField.setAccessible(true);
				_dateField = (JFormattedTextField) _dateFieldField.get(this);
			} catch (Exception e) {
				throw new ReflectionUIError(e);
			}
			try {
				_dateField.commitEdit();
				fireActionPerformed(COMMIT_KEY);
			} catch (ParseException e) {
				throw e;
			}
		}

		protected void setupEvents() {
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (listenerDisabled) {
						return;
					}
					Date value = getDate();
					if (value.equals(data.getValue())) {
						return;
					}
					data.setValue(getDate());
				}
			});
			final JFormattedTextField editor = DateTimePicker.this.getEditor();
			editor.getDocument().addDocumentListener(new DocumentListener() {

				private void anyUpdate() {
					if (listenerDisabled) {
						return;
					}
					textEditorChangesCommittingProcess.cancelCommitSchedule();
					Date value = getDateFromTextEditor();
					if (value == null) {
						return;
					}
					if (value.equals(data.getValue())) {
						return;
					}
					textEditorChangesCommittingProcess.scheduleCommit();
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
			editor.setFocusLostBehavior(JFormattedTextField.PERSIST);
			editor.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
					onFocusLoss();
				}

				@Override
				public void focusGained(FocusEvent e) {
					restoreCaretPosition();
				}

				private void restoreCaretPosition() {
					int caretPosition = editor.getCaretPosition();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							editor.setCaretPosition(caretPosition);
						}
					});
				}
			});
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			listenerDisabled = true;
			try {
				if (refreshStructure) {
					DateTimePickerConfiguration controlCustomization = (DateTimePickerConfiguration) loadControlCustomization(
							input);
					setFormats(controlCustomization.dateFormat + " " + controlCustomization.timeFormat);
					setTimeFormat(new SimpleDateFormat(controlCustomization.timeFormat));
					setEnabled(!data.isGetOnly());
					if (data.getBorderColor() != null) {
						setBorder(BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					} else {
						setBorder(new JXDateTimePicker().getBorder());
					}
					if (data.isGetOnly()) {
						getEditor().setBackground(null);
						getEditor().setForeground(null);
					} else {
						if (data.getEditorBackgroundColor() != null) {
							getEditor().setBackground(SwingRendererUtils.getColor(data.getEditorBackgroundColor()));
						} else {
							getEditor().setBackground(new JXDateTimePicker().getBackground());
						}
						if (data.getEditorForegroundColor() != null) {
							getEditor().setForeground(SwingRendererUtils.getColor(data.getEditorForegroundColor()));
						} else {
							getEditor().setForeground(new JXDateTimePicker().getForeground());
						}
					}
				}
				Date date = (Date) data.getValue();
				setDate(date);
				return true;
			} finally {
				listenerDisabled = false;
			}
		}

		protected long getCommitDelayMilliseconds() {
			return 3000;
		}

		protected void commitTextEditorChanges() {
			Date value = getDateFromTextEditor();
			if (value == null) {
				return;
			}
			if (value.equals(data.getValue())) {
				return;
			}
			JFormattedTextField editor = getEditor();
			int caretPosition = editor.getCaretPosition();
			listenerDisabled = true;
			try {
				setDate((Date) value);
			} finally {
				listenerDisabled = false;
			}
			data.setValue(getDate());
			editor.setCaretPosition(Math.min(caretPosition, editor.getText().length()));
		}

		protected Date getDateFromTextEditor() {
			JFormattedTextField editor = getEditor();
			String string = editor.getText();
			AbstractFormatter formatter = editor.getFormatter();
			try {
				Date result = (Date) formatter.stringToValue(string);
				displayError(null);
				return result;
			} catch (ParseException e) {
				swingRenderer.getReflectionUI().logError(e);
				displayError(ReflectionUIUtils.getPrettyErrorMessage(e));
				return null;
			}
		}

		protected void onFocusLoss() {
			if (textEditorChangesCommittingProcess.isCommitScheduled()) {
				textEditorChangesCommittingProcess.cancelCommitSchedule();
				commitTextEditorChanges();
			}
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
			return "DateTimePicker [data=" + data + "]";
		}
	}

}
