package xy.reflect.ui.example;

import java.util.Date;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * Meteo GUI generated using only the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class Meteo {

	public static void main(String[] args) {
		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "meteo.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new Meteo());
			}
		});
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

	public void refresh() {

	}

	public enum WeatherSummary {
		SUNNY, CLOUDY, RAINY
	}
}
