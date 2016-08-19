import xy.reflect.ui.control.swing.CustomizingSwingRenderer;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.util.SystemProperties;

public class CustomizeInfoCustimizations {

	public static void main(String[] args) {
		System.setProperty(SystemProperties.AUTHORIZE_INFO_CUSTOMIZATIONS_CONTROLS, "true");
		System.setProperty(SystemProperties.DISCARD_META_INFO_CUSTOMIZATIONS, "true");
		String infoCustomizationsOutputFilePath = "D:/prog/git/ReflectionUI/reflection-ui/src/main/resources/xy/reflect/ui/resource/customizations-tools.icu";
		CustomizingSwingRenderer renderer = new CustomizingSwingRenderer(
				CustomizingSwingRenderer.getCustomizationToolsUI(),
				CustomizingSwingRenderer.getCustomizationToolsCustomizations(), infoCustomizationsOutputFilePath);
		renderer.openObjectFrame(new InfoCustomizations());
	}

}
