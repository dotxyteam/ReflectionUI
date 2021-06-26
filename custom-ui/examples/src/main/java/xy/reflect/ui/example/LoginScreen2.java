package xy.reflect.ui.example;

import java.io.IOException;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * Login screen GUI generated with the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class LoginScreen2 {

	public static void main(String[] args) throws IOException {
		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "loginScreen2.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new LoginScreen2());
			}
		});
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

	public void signUp() {
		throw new UnsupportedOperationException();
	}

}
