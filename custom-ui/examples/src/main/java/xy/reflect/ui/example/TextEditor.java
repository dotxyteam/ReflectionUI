package xy.reflect.ui.example;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.IOUtils;

/**
 * Text editor GUI generated with the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class TextEditor implements Serializable {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws IOException {
		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "textEditor.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new TextEditor());
			}
		});
	}

	private String text = "";

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void save(File file) {
		try {
			IOUtils.write(file, text, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void load(File file) {
		try {
			text = IOUtils.read(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
