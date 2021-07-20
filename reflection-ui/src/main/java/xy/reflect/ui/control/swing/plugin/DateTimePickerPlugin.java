
package xy.reflect.ui.control.swing.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXDatePicker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.JXDateTimePicker;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReschedulableTask;
import xy.reflect.ui.util.StrictDateFormat;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.MiscUtils;

/**
 * Field control plugin that allows to display and update adequately
 * {@link Date} values. Time (hours, minutes, seconds, ...) is handled by this
 * control.
 * 
 * @author olitank
 *
 */
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
			public ITypeInfo getType() {
				return new DateTypeInfoProxyFactory(((SwingRenderer) renderer).getReflectionUI())
						.wrapTypeInfo(super.getType());
			}

		};
	}

	protected static class DateTypeInfoProxyFactory extends InfoProxyFactory {

		protected ReflectionUI reflectionUI;

		public DateTypeInfoProxyFactory(ReflectionUI reflectionUI) {
			this.reflectionUI = reflectionUI;
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			if (DateConstructor.isCompatibleWith(type)) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>();
				result.add(new DateConstructor(reflectionUI, type));
				return result;
			}
			return super.getConstructors(type);
		}

		@Override
		protected boolean isConcrete(ITypeInfo type) {
			if (DateConstructor.isCompatibleWith(type)) {
				return true;
			}
			return super.isConcrete(type);
		}

	}

	protected static class DateConstructor extends AbstractConstructorInfo {

		protected ReflectionUI reflectionUI;
		protected ITypeInfo type;
		protected ITypeInfo returnType;

		public DateConstructor(ReflectionUI reflectionUI, ITypeInfo type) {
			this.reflectionUI = reflectionUI;
			this.type = type;
		}

		@Override
		public ITypeInfo getReturnValueType() {
			if (returnType == null) {
				returnType = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(type.getSource()) {
					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return null;
					}

					@Override
					protected String getTypeInfoProxyFactoryIdentifier() {
						return "ConstructorReturnValueTypeInfoProxyFactory [of=" + getClass().getName() + "]";
					}
				});
			}
			return returnType;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object ignore, InvocationData invocationData) {
			return new Date();
		}

		public static boolean isCompatibleWith(ITypeInfo type) {
			Class<?> dateClass;
			try {
				dateClass = ClassUtils.getCachedClassforName(type.getName());
			} catch (ClassNotFoundException e) {
				return false;
			}
			return Date.class.isAssignableFrom(dateClass);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			DateConstructor other = (DateConstructor) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ColorConstructor [type=" + type + "]";
		}

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
		protected ReschedulableTask textEditorChangesCommittingProcess = new ReschedulableTask() {
			@Override
			protected void execute() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							DateTimePicker.this.commitTextEditorChanges();
						} catch (Throwable t) {
							swingRenderer.handleObjectException(DateTimePicker.this, t);
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
				return DateTimePicker.this.getCommitDelayMilliseconds();
			}
		};
		protected boolean initialized = false;
		protected Throwable currentConversionError;
		protected String currentDataErrorMessage;

		public DateTimePicker(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			setupEvents();
			refreshUI(true);
			this.initialized = true;
		}

		@Override
		public void setFormats(DateFormat... formats) {
			DateFormat[] strictFormats = new DateFormat[formats.length];
			for (int i = 0; i < formats.length; i++) {
				strictFormats[i] = new StrictDateFormat(formats[i]);
			}
			super.setFormats(strictFormats);
		}

		@Override
		public void updateUI() {
			super.updateUI();
			if (initialized) {
				refreshUI(true);
			}
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
					try {
						Date value = getDate();
						if (value.equals(data.getValue())) {
							return;
						}
						data.setValue(getDate());
					} catch (Throwable t) {
						swingRenderer.handleObjectException(DateTimePicker.this, t);
					}
				}
			});
			final JFormattedTextField editor = DateTimePicker.this.getEditor();
			editor.getDocument().addDocumentListener(new DocumentListener() {

				private void anyUpdate() {
					if (listenerDisabled) {
						return;
					}
					try {
						textEditorChangesCommittingProcess.cancelSchedule();
						Date value = getDateFromTextEditor();
						if (value == null) {
							return;
						}
						if (value.equals(data.getValue())) {
							return;
						}
						textEditorChangesCommittingProcess.schedule();
					} catch (Throwable t) {
						swingRenderer.handleObjectException(DateTimePicker.this, t);
					}
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
					try {
						onFocusLoss();
					} catch (Throwable t) {
						swingRenderer.handleObjectException(DateTimePicker.this, t);
					}
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
						getEditor().setBackground(new JXDateTimePicker().getBackground());
						getEditor().setForeground(new JXDateTimePicker().getForeground());
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
				currentConversionError = null;
				updateErrorDisplay();
				setDate(date);
				JFormattedTextField editor = this.getEditor();
				editor.setValue(date);
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
			Date result;
			try {
				result = (Date) formatter.stringToValue(string);
				currentConversionError = null;
			} catch (Throwable t) {
				currentConversionError = t;
				return null;
			} finally {
				updateErrorDisplay();
			}
			return result;
		}

		protected void onFocusLoss() {
			if (textEditorChangesCommittingProcess.isScheduled()) {
				textEditorChangesCommittingProcess.cancelSchedule();
				commitTextEditorChanges();
			}
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
		public void validateSubForms() throws Exception {
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
		}

		@Override
		public String toString() {
			return "DateTimePicker [data=" + data + "]";
		}
	}

}
