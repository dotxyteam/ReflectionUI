package xy.reflect.ui;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.Date;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class NullDisplayTest {

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(NullDisplayTest.class.getName(), ".icu");
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
				swingCustomizer.openObjectFrame(new NullDisplayTest());
			}
		});
	}
	
	
	public String string;
	public Character primitiveWrapper;
	public Byte slided;
	public Float spinned;
	public Date date;
	public Date dateTime;
	public Color color;
	public File file;
	public Image image;
	public TestEnum enumerated;
	public Boolean booleanWrapper;
	public Collection<Object> list;
	

	public enum TestEnum{
		E1, E2, E3
	}
}
