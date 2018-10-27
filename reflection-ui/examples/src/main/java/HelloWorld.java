import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class HelloWorld implements Serializable {

	public static void main(String[] args) {
		SwingRenderer.getDefault().openObjectFrame(new HelloWorld());
	}

	private static final long serialVersionUID = 1L;

	private String name = "world";
	private Language language = Language.English;
	private boolean upperCase = false;
	private List<String> history = new ArrayList<String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public boolean isUpperCase() {
		return upperCase;
	}

	public void setUpperCase(boolean upperCase) {
		this.upperCase = upperCase;
	}

	public List<String> getHistory() {
		return history;
	}

	public void setHistory(List<String> history) {
		this.history = history;
	}

	public String sayHello() {
		String result = "";
		if (language == Language.English) {
			result += "Hello";
		} else if (language == Language.French) {
			result += "Bonjour";
		} else if (language == Language.Spanish) {
			result += "Hola";
		} else {
			throw new AssertionError();
		}
		result += " " + name + "!";
		if (upperCase) {
			result = result.toUpperCase();
		}
		history.add(result);
		return result;
	}

	public enum Language {
		English, French, Spanish
	}

}
