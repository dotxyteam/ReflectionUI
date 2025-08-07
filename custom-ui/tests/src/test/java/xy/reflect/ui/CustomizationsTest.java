package xy.reflect.ui;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.MiscUtils;

public class CustomizationsTest {

	public static void main(String[] args) throws Exception {
		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(CustomizationsTest.class.getName(), ".icu");
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
				swingCustomizer.openObjectFrame(new Sample(), null, null);
			}
		});
	}

	public static class Sample {
		public Object anyObject;

		public Object returnAnyObject() {
			return anyObject;
		}

		public Exception theException = new Exception();
		public Exception theException2 = new Exception();

		public String theChoice;
		public String[] theChoiceOptions = new String[] { "a", "z", "e", "r", "t", "y" };

		private String theString = "azerty";

		public String getTheString() {
			return theString;
		}

		public void setTheString(String theString) {
			this.theString = theString;
		}

		public String getExceptionneableInfo() throws Exception {
			return "ExceptionneableInfo";
		}

		public List<Sample> children = new ArrayList<CustomizationsTest.Sample>();

		private Date theDate = new Date();

		public Date getTheDate() {
			return theDate;
		}

		public void doLongTask() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public Object returnNothing() {
			return null;
		}

		public void throwException() throws Exception {
			throw new Exception(MiscUtils.getPrintedStackTrace(new AssertionError()));
		}

		public void callWithManyParams(int i, String s, Date d, Color c) throws Exception {
		}
	}
}
