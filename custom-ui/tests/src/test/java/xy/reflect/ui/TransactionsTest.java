package xy.reflect.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class TransactionsTest {

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(TransactionsTest.class.getName(), ".icu");
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
				swingCustomizer.openObjectFrame(new TransactionsTest());
			}
		});
	}

	public SubObject subObject = new SubObject();
	public List<SubObject> subObjectList = new ArrayList<TransactionsTest.SubObject>(
			Collections.singletonList(subObject));

	public SubObject returnSubObject() {
		return subObject;
	}

	public static class SubObject {
		private String valueBackup;
		public String value;
		public String transactionState;

		public void beginTransaction() {
			valueBackup = value;
			transactionState = "started";
		}

		public void commitTransaction() {
			valueBackup = null;
			transactionState = "committed";
		}

		public void rollbackTransaction() {
			value = valueBackup;
			valueBackup = null;
			transactionState = "rolled back";
		}

		@Override
		public String toString() {
			return "SubObject [value=" + value + ", transactionState=" + transactionState + "]";
		}

	}

}
