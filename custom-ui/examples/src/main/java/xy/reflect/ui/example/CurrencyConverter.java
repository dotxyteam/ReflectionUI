package xy.reflect.ui.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Currency converter GUI generated using only the XML declarative
 * customizations.
 * 
 * @author olitank
 *
 */
public class CurrencyConverter {

	public static void main(String[] args) {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "currencyConverter.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new CurrencyConverter());
			}
		});
	}

	private Currency sourceCurrency = Currency.USD;
	private Currency targetCurrency = Currency.EUR;
	private String sourceCurrencyAmount = "1";

	public Currency getSourceCurrency() {
		return sourceCurrency;
	}

	public void setSourceCurrency(Currency sourceCurrency) {
		this.sourceCurrency = sourceCurrency;
	}

	public Currency getTargetCurrency() {
		return targetCurrency;
	}

	public void setTargetCurrency(Currency targetCurrency) {
		this.targetCurrency = targetCurrency;
	}

	public String getSourceCurrencyAmount() {
		return sourceCurrencyAmount;
	}

	public void setSourceCurrencyAmount(String sourceCurrencyAmount) {
		this.sourceCurrencyAmount = sourceCurrencyAmount;
	}

	public String getTargetCurrencyAmount() throws Exception {
		return Objects.toString(getSourceCurrencyAmountValue() * getFactorFromUSD(targetCurrency.name())
				/ getFactorFromUSD(sourceCurrency.name()));
	}

	private double getSourceCurrencyAmountValue() throws ScriptException {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		return ((Number) engine.eval(sourceCurrencyAmount)).doubleValue();
	}

	public void push0() {
		sourceCurrencyAmount += "0";
	}

	public void push000() {
		sourceCurrencyAmount += "000";
	}

	public void push1() {
		sourceCurrencyAmount += "1";
	}

	public void push2() {
		sourceCurrencyAmount += "2";
	}

	public void push3() {
		sourceCurrencyAmount += "3";
	}

	public void push4() {
		sourceCurrencyAmount += "4";
	}

	public void push5() {
		sourceCurrencyAmount += "5";
	}

	public void push6() {
		sourceCurrencyAmount += "6";
	}

	public void push7() {
		sourceCurrencyAmount += "7";
	}

	public void push8() {
		sourceCurrencyAmount += "8";
	}

	public void push9() {
		sourceCurrencyAmount += "9";
	}

	public void pushDot() {
		sourceCurrencyAmount += ".";
	}

	public void pushDelete() {
		if (sourceCurrencyAmount.length() == 0) {
			return;
		}
		sourceCurrencyAmount = sourceCurrencyAmount.substring(0, sourceCurrencyAmount.length() - 1);
	}

	public void pushMinus() {
		sourceCurrencyAmount += "-";
	}

	public void pushPlus() {
		sourceCurrencyAmount += "+";
	}

	public void pushStar() {
		sourceCurrencyAmount += "*";
	}

	public void pushDiv() {
		sourceCurrencyAmount += "/";
	}

	public void pushEqual() throws ScriptException {
		sourceCurrencyAmount = Objects.toString(getSourceCurrencyAmountValue());
	}

	public void pushLeftParenthesis() {
		sourceCurrencyAmount += "(";
	}

	public void pushRightParenthesis() {
		sourceCurrencyAmount += ")";
	}

	public Date now() {
		return new Date();
	}

	public void rotateCurrencies() {
		Currency tmp = sourceCurrency;
		sourceCurrency = targetCurrency;
		targetCurrency = tmp;
	}

	private static double getFactorFromUSD(String currencyName) throws MalformedURLException, Exception {
		String json = sendHttpGetRequest(new URL(
				"http://api.exchangeratesapi.io/latest?&access_key=0ae324f9a47ecfc4c1f527595e6738d0&base=USD&symbols="
						+ currencyName));
		if (!json.contains("\"success\":true")) {
			throw new RuntimeException(json);
		}
		String beforeRateString = "\"" + currencyName + "\":";
		String afterRateString = "},\"base\":\"USD\"";
		String rateString = json.substring(json.indexOf(beforeRateString) + beforeRateString.length(),
				json.indexOf(afterRateString));
		return Double.valueOf(rateString);
	}

	private static String sendHttpGetRequest(URL url) throws Exception {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	public enum Currency {
		AUD, BGN, BRL, CAD, CHF, CNY, CZK, DKK, EUR, GBP, HKD, HRK, HUF, IDR, ILS, INR, ISK, JPY, KRW, MXN, MYR, NOK,
		NZD, PHP, PLN, RON, RUB, SEK, SGD, THB, TRY, USD, ZAR
	}
}