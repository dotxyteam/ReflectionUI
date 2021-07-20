


package xy.reflect.ui.control.swing.util;

import org.jdesktop.swingx.calendar.SingleDaySelectionModel;

import xy.reflect.ui.util.ReflectionUIError;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DateFormatter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.awt.*;

/**
 * An extended version the {@link JXDatePicker} allowing to display/edit time
 * (hour, minute, ...).
 */
public class JXDateTimePicker extends JXDatePicker {
	private static final long serialVersionUID = 1L;

	private JSpinner timeSpinner;
	private JPanel timePanel;
	private DateFormat timeFormat;

	public JXDateTimePicker() {
		super();
		getMonthView().setSelectionModel(new SingleDaySelectionModel());
	}

	public JXDateTimePicker(Date d) {
		this();
		setDate(d);
	}

	public void commitEdit() throws ParseException {
		commitTime();
		super.commitEdit();
	}

	public void cancelEdit() {
		super.cancelEdit();
		setTimeSpinners();
	}

	@Override
	public JPanel getLinkPanel() {
		super.getLinkPanel();
		if (timePanel == null) {
			timePanel = createTimePanel();
		}
		setTimeSpinners();
		return timePanel;
	}

	private JPanel createTimePanel() {
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new FlowLayout());
		SpinnerDateModel dateModel = new SpinnerDateModel();
		timeSpinner = new JSpinner(dateModel);
		timeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					commitEdit();
				} catch (ParseException e1) {
					throw new ReflectionUIError(e1);
				}
			}
		});
		if (timeFormat == null)
			timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
		updateTextFieldFormat();
		newPanel.add(timeSpinner);
		newPanel.setBackground(Color.WHITE);
		return newPanel;
	}

	private void updateTextFieldFormat() {
		if (timeSpinner == null)
			return;
		JFormattedTextField tf = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
		DefaultFormatterFactory factory = (DefaultFormatterFactory) tf.getFormatterFactory();
		DateFormatter formatter = (DateFormatter) factory.getDefaultFormatter();
		// Change the date format to only show the hours
		formatter.setFormat(timeFormat);
	}

	private void commitTime() {
		Date date = getDate();
		if (date != null) {
			Date time = (Date) timeSpinner.getValue();
			GregorianCalendar timeCalendar = new GregorianCalendar();
			timeCalendar.setTime(time);

			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			Date newDate = calendar.getTime();
			setDate(newDate);
		}

	}

	private void setTimeSpinners() {
		Date date = getDate();
		if (date != null) {
			timeSpinner.setValue(date);
		}
	}

	public DateFormat getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(DateFormat timeFormat) {
		this.timeFormat = timeFormat;
		updateTextFieldFormat();
	}

	public static void main(String[] args) {
		Date date = new Date();
		JFrame frame = new JFrame();
		frame.setTitle("Date Time Picker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JXDateTimePicker dateTimePicker = new JXDateTimePicker();
		dateTimePicker.setFormats(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM));
		dateTimePicker.setTimeFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM));

		dateTimePicker.setDate(date);

		frame.getContentPane().add(dateTimePicker);
		frame.pack();
		frame.setVisible(true);
	}
}
