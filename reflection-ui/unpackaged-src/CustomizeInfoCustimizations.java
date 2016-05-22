import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.util.SystemProperties;

public class CustomizeInfoCustimizations {

	public static void main(String[] args) {
		System.setProperty(SystemProperties.ENABLE_INFO_CUSTOMIZATIONS_CONTROLS, "true");
		System.setProperty(SystemProperties.INFO_CUSTOMIZATIONS_FILE, "D:/prog/git/ReflectionUI/reflection-ui/src/main/resources/xy/reflect/ui/resource/info-customizations-types.icu");
		ReflectionUI reflectionUI = new ReflectionUI();
		reflectionUI.getSwingRenderer().openObjectFrame(new InfoCustomizations(reflectionUI));
	}

}
