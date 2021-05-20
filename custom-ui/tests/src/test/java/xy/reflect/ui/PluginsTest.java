package xy.reflect.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class PluginsTest {

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(PluginsTest.class.getName(), ".icu");
		tmpCustomizationsFile.deleteOnExit();
		reflectionUI.getInfoCustomizations().saveToFile(tmpCustomizationsFile, null);
		final SwingCustomizer swingCustomizer = new SwingCustomizer(reflectionUI, tmpCustomizationsFile.getPath()) {

			@Override
			public boolean isCustomizationsEditorEnabled() {
				return true;
			}

			@Override
			public CustomizationController createCustomizationController() {
				return new CustomizationController(this) {

					ModificationStack modificationStack = new ModificationStack(null);

					@Override
					protected void openWindow() {
						refreshCustomizedControlsOnModification();
					}

					@Override
					protected void closeWindow() {
					}

					@Override
					public ModificationStack getModificationStack() {
						return modificationStack;
					}

				};
			}

		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingCustomizer.openObjectFrame(new PluginsTest());
			}
		});
	}

	public int theInteger = 123;
	public float theFloat = 1.23f;
	public Date theDate = parseDateTime("2021-05-20 00:00:00");
	public boolean theBoolean = false;
	public Color theColor = Color.GREEN;
	public File theFile = new File(System.getProperty("java.io.tmpdir") + "/test.tst");
	public String theText = "hello world";
	public Image theImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
	public Object[] theArray = new Object[] { theInteger, theFloat, theDate, theBoolean, theColor, theFile, theText,
			theImage };

	private Date parseDateTime(String string) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
		} catch (ParseException e) {
			throw new AssertionError(e);
		}
	}
}
