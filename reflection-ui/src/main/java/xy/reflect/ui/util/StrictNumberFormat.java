/*
 * 
 */
package xy.reflect.ui.util;

import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;

/**
 * <p>
 * Decorator for a {@link NumberFormat} which only accepts values which can be
 * completely parsed by the delegate format. If the value can only be partially
 * parsed, the decorator will refuse to parse the value.
 * </p>
 */
public class StrictNumberFormat extends NumberFormat {

	private static final long serialVersionUID = 1L;

	private final NumberFormat delegate;

	/**
	 * Decorate <code>delegate</code> to make sure if parser everything or nothing
	 *
	 * @param delegate The delegate format
	 */
	public StrictNumberFormat(NumberFormat delegate) {
		this.delegate = delegate;
	}

	@Override
	public Number parse(String source, ParsePosition pos) {
		int initialIndex = pos.getIndex();
		Number result = delegate.parse(source, pos);
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

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		return delegate.format(obj, toAppendTo, pos);
	}

	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		return delegate.format(number, toAppendTo, pos);
	}

	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		return delegate.format(number, toAppendTo, pos);
	}

	public boolean isParseIntegerOnly() {
		return delegate.isParseIntegerOnly();
	}

	public void setParseIntegerOnly(boolean value) {
		delegate.setParseIntegerOnly(value);
	}

	public boolean isGroupingUsed() {
		return delegate.isGroupingUsed();
	}

	public void setGroupingUsed(boolean newValue) {
		delegate.setGroupingUsed(newValue);
	}

	public int getMaximumIntegerDigits() {
		return delegate.getMaximumIntegerDigits();
	}

	public void setMaximumIntegerDigits(int newValue) {
		delegate.setMaximumIntegerDigits(newValue);
	}

	public int getMinimumIntegerDigits() {
		return delegate.getMinimumIntegerDigits();
	}

	public void setMinimumIntegerDigits(int newValue) {
		delegate.setMinimumIntegerDigits(newValue);
	}

	public int getMaximumFractionDigits() {
		return delegate.getMaximumFractionDigits();
	}

	public void setMaximumFractionDigits(int newValue) {
		delegate.setMaximumFractionDigits(newValue);
	}

	public int getMinimumFractionDigits() {
		return delegate.getMinimumFractionDigits();
	}

	public void setMinimumFractionDigits(int newValue) {
		delegate.setMinimumFractionDigits(newValue);
	}

	public Currency getCurrency() {
		return delegate.getCurrency();
	}

	public void setCurrency(Currency currency) {
		delegate.setCurrency(currency);
	}

	public RoundingMode getRoundingMode() {
		return delegate.getRoundingMode();
	}

	public void setRoundingMode(RoundingMode roundingMode) {
		delegate.setRoundingMode(roundingMode);
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
		StrictNumberFormat other = (StrictNumberFormat) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StrictNumberFormat [delegate=" + delegate + "]";
	}

}