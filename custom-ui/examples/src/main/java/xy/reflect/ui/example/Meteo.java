package xy.reflect.ui.example;

import java.util.Date;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Meteo GUI generated using only the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class Meteo {

	public static void main(String[] args) {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "meteo.icu");
		renderer.openObjectFrame(new Meteo());
	}

	public String getCountryName() {
		return "France";
	}

	public String getTownName() {
		return "Paris";
	}

	public int getCurrentTemperatureDegrees() {
		return 15;
	}

	public int getCurrentPressureBars() {
		return 1008;
	}

	public int getCurrentHumidityPercentage() {
		return 35;
	}

	public int getCurrentWindSpeedKilometerByHour() {
		return 101;
	}

	public Date getCurrentDateTime() {
		return new Date();
	}

	public WeatherSummary getMondaySummary() {
		return WeatherSummary.CLOUDY;
	}

	public WeatherSummary getTuesdaySummary() {
		return WeatherSummary.CLOUDY;
	}

	public WeatherSummary getWednesdaySummary() {
		return WeatherSummary.RAINY;
	}

	public WeatherSummary getThursdaySummary() {
		return WeatherSummary.SUNNY;
	}

	public WeatherSummary getFridaySummary() {
		return WeatherSummary.CLOUDY;
	}

	public WeatherSummary getSaturdaySummary() {
		return WeatherSummary.SUNNY;
	}

	public WeatherSummary getSundaySummary() {
		return WeatherSummary.RAINY;
	}

	public enum WeatherSummary {
		SUNNY, CLOUDY, RAINY
	}
}
