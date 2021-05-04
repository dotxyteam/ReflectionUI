import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.util.MoreSystemProperties;

public class AllExamples {

	public static final List<Class<?>> CLASSES = Arrays.asList(xy.reflect.ui.example.ATMSimulator.class,
			xy.reflect.ui.example.AudioPlayer.class, xy.reflect.ui.example.Calculator.class,
			xy.reflect.ui.example.ControlScreen.class, xy.reflect.ui.example.ControlScreen2.class,
			xy.reflect.ui.example.CreditCardPayment.class, xy.reflect.ui.example.CurrencyConverter.class,
			xy.reflect.ui.example.Employee.class, xy.reflect.ui.example.FastFood.class,
			xy.reflect.ui.example.FileExplorer.class, xy.reflect.ui.example.HelloWorld.class,
			xy.reflect.ui.example.InputSettings.class, xy.reflect.ui.example.Library.class,
			xy.reflect.ui.example.LoginScreen.class, xy.reflect.ui.example.LoginScreen2.class,
			xy.reflect.ui.example.LoginScreen3.class, xy.reflect.ui.example.LoginScreen4.class,
			xy.reflect.ui.example.LoginScreen5.class, xy.reflect.ui.example.LoginScreen6.class,
			xy.reflect.ui.example.MailClient.class, xy.reflect.ui.example.Meteo.class,
			xy.reflect.ui.example.ReflectionUITutorial.class, xy.reflect.ui.example.Restaurant.class,
			xy.reflect.ui.example.SaySomething.class, xy.reflect.ui.example.SaySomething2.class,
			xy.reflect.ui.example.ServiceAccountCreation.class, xy.reflect.ui.example.TextEditor.class);

	public static void main(String[] args) throws Exception {
		System.setProperty(MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS, "true");
		for (Class<?> c : CLASSES) {
			System.out.println("Running " + c.getName());
			c.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
		}
	}

}
