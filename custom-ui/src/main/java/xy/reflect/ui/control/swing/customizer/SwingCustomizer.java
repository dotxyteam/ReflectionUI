
package xy.reflect.ui.control.swing.customizer;

import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingCustomizerUtils;
import xy.reflect.ui.util.SystemProperties;

/**
 * This class is a sub-class of {@link SwingRenderer} that allows to customize
 * generated UIs by visually editing an {@link InfoCustomizations} instance and
 * instantly previewing the result.
 * 
 * @author olitank
 *
 */
public class SwingCustomizer extends CustomizedSwingRenderer {

	public static void main(String[] args) throws Exception {
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instanciate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + MoreSystemProperties.describe();
		final Class<?> clazz;
		if (args.length == 0) {
			clazz = Object.class;
		} else if (args.length == 1) {
			if (args[0].equals("--help")) {
				System.out.println(usageText);
				return;
			} else {
				clazz = Class.forName(args[0]);
			}
		} else {
			throw new IllegalArgumentException(usageText);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ReflectionUI reflectionUI = SwingCustomizer.getDefault().getReflectionUI();
				Object object = SwingCustomizer.getDefault().onTypeInstanciationRequest(null,
						reflectionUI.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, clazz, null)));
				if (object == null) {
					return;
				}
				SwingCustomizer.getDefault().openObjectFrame(object);
			}
		});
	}

	/**
	 * An {@link ITypeInfo} specific property key used to disable customization
	 * tools on forms associated to this {@link ITypeInfo} instances.
	 */
	public static final String CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY = SwingRenderer.class.getName()
			+ ".CUSTOMIZATIONS_FORBIDDEN";

	protected static SwingCustomizer defaultInstance;

	protected CustomizationTools customizationTools;
	protected CustomizationOptions customizationOptions;
	protected CustomizationController customizationController;
	protected String infoCustomizationsOutputFilePath;

	/**
	 * @return the default instance connected to {@link CustomizedUI#getDefault()}.
	 */
	public static SwingCustomizer getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new SwingCustomizer(CustomizedUI.getDefault());
			defaultInstance.infoCustomizationsOutputFilePath = SystemProperties.getDefaultInfoCustomizationsFilePath();
		}
		return defaultInstance;
	}

	/**
	 * A constructor allowing to specify the {@link CustomizedUI} instance and the
	 * path of the customizations file.
	 * 
	 * @param customizedUI                     The {@link CustomizedUI} instance to
	 *                                         use.
	 * @param infoCustomizationsOutputFilePath The path of the customizations file
	 *                                         to use.
	 */
	public SwingCustomizer(CustomizedUI customizedUI, String infoCustomizationsOutputFilePath) {
		super(customizedUI);
		if (infoCustomizationsOutputFilePath != null) {
			File file = new File(infoCustomizationsOutputFilePath);
			if (file.exists()) {
				try {
					getInfoCustomizations().loadFromFile(file,
							ReflectionUIUtils.getDebugLogListener(getCustomizedUI()));
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			} else {
				try {
					getInfoCustomizations().saveToFile(file, ReflectionUIUtils.getDebugLogListener(getCustomizedUI()));
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			}
		}
		this.infoCustomizationsOutputFilePath = infoCustomizationsOutputFilePath;
	}

	/**
	 * A constructor allowing to specify only the {@link CustomizedUI} instance.
	 * 
	 * @param customizedUI The {@link CustomizedUI} instance to use.
	 */
	public SwingCustomizer(CustomizedUI customizedUI) {
		this(customizedUI, null);
	}

	/**
	 * @return the path of the customizations file.
	 */
	public String getInfoCustomizationsOutputFilePath() {
		return infoCustomizationsOutputFilePath;
	}

	/**
	 * @return the {@link CustomizationTools} instance that is used.
	 */
	public CustomizationTools getCustomizationTools() {
		if (customizationTools == null) {
			customizationTools = createCustomizationTools();
		}
		return customizationTools;
	}

	/**
	 * @return the {@link CustomizationOptions} instance that is used.
	 */
	public CustomizationOptions getCustomizationOptions() {
		if (customizationOptions == null) {
			customizationOptions = createCustomizationOptions();
		}
		return customizationOptions;
	}

	/**
	 * @return the {@link CustomizationController} instance that is used.
	 */
	public CustomizationController getCustomizationController() {
		if (customizationController == null) {
			customizationController = createCustomizationController();
		}
		return customizationController;
	}

	/**
	 * @return whether customizations are enabled or not. Customizations are
	 *         disabled if the customizations file path is not defined (null) or if
	 *         the {@link MoreSystemProperties#HIDE_INFO_CUSTOMIZATIONS_TOOLS}
	 *         property is set to "true".
	 */
	public boolean isCustomizationsEditorEnabled() {
		return (infoCustomizationsOutputFilePath != null) && !MoreSystemProperties.areCustomizationToolsDisabled();
	}

	/**
	 * @return the main customization tools icon.
	 */
	public ImageIcon getCustomizationsIcon() {
		return SwingCustomizerUtils.CUSTOMIZATION_ICON;
	}

	protected CustomizationController createCustomizationController() {
		return new CustomizationController(this);
	}

	protected CustomizationOptions createCustomizationOptions() {
		return new CustomizationOptions(this);
	}

	protected CustomizationTools createCustomizationTools() {
		if (!MoreSystemProperties.areCustomizationToolsDisabled()) {
			System.out.println("Set the following system property to disable the design mode:\n-D"
					+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");
		}
		return new CustomizationTools(this);
	}

	@Override
	public CustomizingForm createForm(Object object, IInfoFilter infoFilter) {
		return new CustomizingForm(this, object, infoFilter);
	}

}
