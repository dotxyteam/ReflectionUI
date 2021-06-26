package xy.reflect.ui.example;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * Credit card payment GUI generated using only the XML declarative
 * customizations.
 * 
 * @author olitank
 *
 */
public class CreditCardPayment {

	public static void main(String[] args) {
		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "creditCardPayment.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new CreditCardPayment());
			}
		});
	}

	private String ownerName;
	private Type type;
	private String number;
	private String code;
	private int expirationMonth = 1;
	private int expirationYear = 2019;

	public void proceed() {
		throw new UnsupportedOperationException();
	}

	public void validate() throws Exception {
		if (type == null) {
			throw new Exception("Card type name not specified!");
		}
		if ((ownerName == null) || (ownerName.length() == 0)) {
			throw new Exception("Owner name not specified!");
		}
		if ((number == null) || (number.length() == 0)) {
			throw new Exception("Card number not specified!");
		}
		if ((code == null) || (code.length() == 0)) {
			throw new Exception("Code name not specified!");
		}
		if (expirationMonth == 0) {
			throw new Exception("Expiration month not specified!");
		}
		if (expirationYear == 0) {
			throw new Exception("Expiration year not specified!");
		}
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getExpirationMonth() {
		return expirationMonth;
	}

	public void setExpirationMonth(int expirationMonth) {
		this.expirationMonth = expirationMonth;
	}

	public int getExpirationYear() {
		return expirationYear;
	}

	public void setExpirationYear(int expirationYear) {
		this.expirationYear = expirationYear;
	}

	public enum Type {
		Visa, Mastercard, Discover
	}
}
