package xy.reflect.ui.example;

import java.io.IOException;
import java.util.Date;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * Service Account Creation GUI generated with the XML declarative
 * customizations.
 * 
 * @author olitank
 *
 */
public class ServiceAccountCreation {

	public static void main(String[] args) throws IOException {
		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./")
						+ "serviceAccountCreation.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new ServiceAccountCreation());
			}
		});
	}

	private String firstName;
	private String lastName;
	private Date birthDate;
	private String companyName;
	private String password;
	private String verifiedPassword;
	private CardType cardType;
	private String cardNumber;
	private String cardCode;
	private Date cardExpirationDate;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVerifiedPassword() {
		return verifiedPassword;
	}

	public void setVerifiedPassword(String verifiedPassword) {
		this.verifiedPassword = verifiedPassword;
	}

	public CardType getCardType() {
		return cardType;
	}

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getCardCode() {
		return cardCode;
	}

	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}

	public Date getCardExpirationDate() {
		return cardExpirationDate;
	}

	public void setCardExpirationDate(Date cardExpirationDate) {
		this.cardExpirationDate = cardExpirationDate;
	}

	public enum CardType {
		Visa, MasterCard, Discover
	}

	public void joinUs() {

	}
}
