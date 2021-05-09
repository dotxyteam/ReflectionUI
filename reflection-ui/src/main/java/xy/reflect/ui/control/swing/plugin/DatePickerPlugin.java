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
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXDatePicker;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReschedulableTask;

/**
 * Field control plugin that allows to display and update adequately
 * {@link Date} values. Time (hours, minutes, seconds, ...) is not handled by
 * this control.
 * 
 * @author olitank
 *
 */
public class DatePickerPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Date Picker";
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
		return new DatePickerConfiguration();
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
	public DatePicker createControl(Object renderer, IFieldControlInput input) {
		return new DatePicker((SwingRenderer) renderer, input);
	}

	public static class DatePickerConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public String format = "yyyy-MM-dd";
	}

	public class DatePicker extends JXDatePicker implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected boolean listenerDisabled = false;
		protected ReschedulableTask textEditorChangesCommittingProcess = new ReschedulableTask() {
			@Override
			protected void execute() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						DatePicker.this.commitTextEditorChanges();
					}
				});
			}

			@Override
			protected long getExecutionDelayMilliseconds() {
				return DatePicker.this.getCommitDelayMilliseconds();
			}
		};
		protected boolean initialized = false;

		public DatePicker(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			setupEvents();
			refreshUI(true);
			this.initialized = true;
		}

		@Override
		public void updateUI() {
			super.updateUI();
			if (initialized) {
				refreshUI(true);
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
			final JFormattedTextField editor = DatePicker.this.getEditor();
			editor.getDocument().addDocumentListener(new DocumentListener() {

				private void anyUpdate() {
					if (listenerDisabled) {
						return;
					}
					textEditorChangesCommittingProcess.cancelSchedule();
					Date value = getDateFromTextEditor();
					if (value == null) {
						return;
					}
					if (value.equals(data.getValue())) {
						return;
					}
					textEditorChangesCommittingProcess.schedule();
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
					DatePickerConfiguration controlCustomization = (DatePickerConfiguration) loadControlCustomization(
							input);
					setFormats(controlCustomization.format);
					setEnabled(!data.isGetOnly());
					if (data.getBorderColor() != null) {
						setBorder(BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					} else {
						setBorder(new JXDatePicker().getBorder());
					}
					if (data.isGetOnly()) {
						getEditor().setBackground(new JXDatePicker().getBackground());
						getEditor().setForeground(new JXDatePicker().getForeground());
					} else {
						if (data.getEditorBackgroundColor() != null) {
							getEditor().setBackground(SwingRendererUtils.getColor(data.getEditorBackgroundColor()));
						} else {
							getEditor().setBackground(new JXDatePicker().getBackground());
						}
						if (data.getEditorForegroundColor() != null) {
							getEditor().setForeground(SwingRendererUtils.getColor(data.getEditorForegroundColor()));
						} else {
							getEditor().setForeground(new JXDatePicker().getForeground());
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
			JFormattedTextField editor = this.getEditor();
			String string = editor.getText();
			AbstractFormatter formatter = editor.getFormatter();
			try {
				Date result = (Date) formatter.stringToValue(string);
				displayError(null);
				return result;
			} catch (ParseException e) {
				swingRenderer.getReflectionUI().logError(e);
				displayError(MiscUtils.getPrettyErrorMessage(e));
				return null;
			}
		}

		protected void onFocusLoss() {
			if (textEditorChangesCommittingProcess.isScheduled()) {
				textEditorChangesCommittingProcess.cancelSchedule();
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
			if (data.isGetOnly()) {
				return false;
			}
			return SwingRendererUtils.requestAnyComponentFocus(getEditor(), swingRenderer);
		}

		@Override
		public void validateSubForm() throws Exception {
		}

		@Override
		public void addMenuContribution(MenuModel menuModel) {
		}

		@Override
		public String toString() {
			return "DatePicker [data=" + data + "]";
		}
	}

}
