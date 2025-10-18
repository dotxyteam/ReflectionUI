package xy.reflect.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class ListEmbeddedDetailsTest {

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(ListEmbeddedDetailsTest.class.getName(), ".icu");
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
						recustomizeAllFormsOnModification();
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
				swingCustomizer.openObjectFrame(new ListEmbeddedDetailsTest());
			}
		});
	}

	public static class Row {
		public Base base;
		public String string = "";
	}
	public abstract static class Base {

	}

	public static class Sub extends Base {
		public String subField = "";

		public Sub(String subField) {
			this.subField = subField;
		}
		
	}

	
	public List<Row> rows = new ArrayList<Row>();
}
