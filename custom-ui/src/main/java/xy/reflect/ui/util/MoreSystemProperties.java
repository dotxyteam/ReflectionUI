
package xy.reflect.ui.util;

/**
 * Contains the system properties exposed by the library.
 * 
 * @author olitank
 *
 */
public class MoreSystemProperties extends SystemProperties {
	
	public  static void main(String[] args) {
		System.out.println(describe());
	}

	@Usage("If the value of this property is \"true\" then the UI customization tools will be hidden.")
	public static final String HIDE_INFO_CUSTOMIZATIONS_TOOLS = PREFIX + ".infoCustomizationsToolsHidden";
	@Usage("If the value of this property is set then the customizations that were specified for the UI customization tools will be editable and saved to the specified output file.")
	public static final String INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH = PREFIX
			+ ".customizationToolsCustomizationsFilePath";

	public static boolean areCustomizationToolsDisabled() {
		return System.getProperty(HIDE_INFO_CUSTOMIZATIONS_TOOLS, "false").equals("true");
	}

	public static String getInfoCustomizationToolsCustomizationsFilePath() {
		return System.getProperty(INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH);
	}

	public static boolean isInfoCustomizationToolsCustomizationAllowed() {
		return getInfoCustomizationToolsCustomizationsFilePath() != null;
	}

	public static String describe() {
		return describe(MoreSystemProperties.class);
	}

}
