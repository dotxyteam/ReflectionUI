package xy.reflect.ui.example;

import java.io.File;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.SystemProperties;

/**
 * CustomUI library demonstration: Each of the following methods demonstrates a
 * feature of the library.
 * 
 * @author olitank
 *
 */
public class CustomUITutorial {

	public static void main(String[] args) {
		openObjectDialog();
		disableDesignMode();
		changeCustomizationsFilePath();
		addHandCodedCustomizations();
	}

	private static void openObjectDialog() {
		/*
		 * Most basic use case: opening an object dialog. Note that by default the
		 * dialog will be in design mode. The customizations file will be stored by
		 * default in the current diectory.
		 */
		Object myObject = new HelloWorld();
		SwingCustomizer.getDefault().openObjectDialog(null, myObject, myObject + " Dialog in design mode");
	}

	private static void disableDesignMode() {
		/*
		 * Disable the design mode before opening the object dialog:
		 */
		System.setProperty(MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS, "true");
		Object myObject = new HelloWorld();
		SwingCustomizer.getDefault().openObjectDialog(null, myObject, "Desing mode disabled");
	}

	private static void changeCustomizationsFilePath() {
		File tmpDirectory;
		try {
			tmpDirectory = FileUtils.createTempDirectory();
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		File customizationsFile = new File(tmpDirectory, "myFile.icu");
		/*
		 * The following system property can be used to change the customizations file
		 * path:
		 */
		System.setProperty(SystemProperties.getDefaultInfoCustomizationsFilePath(), customizationsFile.getPath());
		Object myObject = new HelloWorld();
		SwingCustomizer.getDefault().openObjectDialog(null, myObject,
				"Customizations file => " + customizationsFile.getPath());
	}

	private static void addHandCodedCustomizations() {
		/*
		 * Hand-coded customizations can be added before or after the declarative
		 * customizations application.
		 */
		CustomizedUI customizedUI = new CustomizedUI() {

			@Override
			protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
				return new InfoProxyFactory() {

					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return "(added before customizations) " + super.getCaption(field, containingType);
					}

				}.wrapTypeInfo(super.getTypeInfoBeforeCustomizations(type));
			}

			@Override
			protected ITypeInfo getTypeInfoAfterCustomizations(ITypeInfo type) {
				return new InfoProxyFactory() {

					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType) + " (added after customizations)";
					}

				}.wrapTypeInfo(super.getTypeInfoAfterCustomizations(type));
			}

		};
		SwingCustomizer renderer = new SwingCustomizer(customizedUI,
				SystemProperties.getDefaultInfoCustomizationsFilePath());
		Object myObject = new HelloWorld();
		renderer.openObjectDialog(null, myObject, "Hand-Coded customizations added");
	}

}