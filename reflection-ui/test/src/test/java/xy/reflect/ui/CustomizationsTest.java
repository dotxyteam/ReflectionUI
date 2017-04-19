package xy.reflect.ui;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.customization.SwingCustomizer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizations;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CustomizationsTest {

	public static void main(String[] args) throws Exception {
		final InfoCustomizations infoCustomizations = new InfoCustomizations();
		ReflectionUI reflectionUI = new ReflectionUI() {
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				ITypeInfo result = super.getTypeInfo(typeSource);
				result = infoCustomizations.get(this, result);
				return result;
			}
		};
		File tmpCustomizationsFile = File.createTempFile(CustomizationsTest.class.getName(), ".icu");
		tmpCustomizationsFile.deleteOnExit();
		SwingRenderer swingRenderer = new SwingCustomizer(reflectionUI, infoCustomizations,
				tmpCustomizationsFile.getPath()){

					@Override
					protected boolean areCustomizationToolsDisabled() {
						return false;
					}

			
			
		};
		swingRenderer.openObjectFrame(new Sample(), null, null);
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
			throw new Exception(ReflectionUIUtils.getPrintedStackTrace(new AssertionError()));
		}

		public void callWithManyParams(int i, String s, Date d, Color c) throws Exception {
		}
	}
}
