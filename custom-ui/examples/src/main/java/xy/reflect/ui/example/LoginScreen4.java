package xy.reflect.ui.example;

import java.io.IOException;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Login screen GUI generated with the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class LoginScreen4 {

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "loginScreen4.icu");
		renderer.openObjectFrame(new LoginScreen4());
	}

	private String userName;
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void signIn() {
		throw new UnsupportedOperationException();
	}

	public void signInWithFacebook() {
		throw new UnsupportedOperationException();
	}

	public void recoverForgottenPassword() {
		throw new UnsupportedOperationException();
	}

	public void signUp() {
		throw new UnsupportedOperationException();
	}

}
