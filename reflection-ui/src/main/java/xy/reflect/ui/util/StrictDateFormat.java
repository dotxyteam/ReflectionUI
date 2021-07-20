/*
 * 
 */
package xy.reflect.ui.util;

import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * <p>
 * Decorator for a {@link DateFormat} which only accepts values which can be
 * completely parsed by the delegate format. If the value can only be partially
 * parsed, the decorator will refuse to parse the value.
 * </p>
 */
public class StrictDateFormat extends DateFormat {

	private static final long serialVersionUID = 1L;

	private final DateFormat delegate;

	/**
	 * Decorate <code>delegate</code> to make sure if parser everything or nothing
	 *
	 * @param delegate The delegate format
	 */
	public StrictDateFormat(DateFormat delegate) {
		this.delegate = delegate;
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		int initialIndex = pos.getIndex();
		Date result = delegate.parse(source, pos);
		if (result != null && pos.getIndex() < source.length()) {
			int errorIndex = pos.getIndex();
			pos.setIndex(initialIndex);
			pos.setErrorIndex(errorIndex);
			return null;
		}
		return result;
	}

	@Override
	public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
		return delegate.formatToCharacterIterator(obj);
	}

	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		return delegate.format(number, toAppendTo, pos);
	}

	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		return delegate.format(number, toAppendTo, pos);
	}

	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		return delegate.format(date, toAppendTo, fieldPosition);
	}

	public void setCalendar(Calendar newCalendar) {
		delegate.setCalendar(newCalendar);
	}

	public Calendar getCalendar() {
		return delegate.getCalendar();
	}

	public void setNumberFormat(NumberFormat newNumberFormat) {
		delegate.setNumberFormat(newNumberFormat);
	}

	public NumberFormat getNumberFormat() {
		return delegate.getNumberFormat();
	}

	public void setTimeZone(TimeZone zone) {
		delegate.setTimeZone(zone);
	}

	public TimeZone getTimeZone() {
		return delegate.getTimeZone();
	}

	public void setLenient(boolean lenient) {
		delegate.setLenient(lenient);
	}

	public boolean isLenient() {
		return delegate.isLenient();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		StrictDateFormat other = (StrictDateFormat) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StrictDateFormat [delegate=" + delegate + "]";
	}

}