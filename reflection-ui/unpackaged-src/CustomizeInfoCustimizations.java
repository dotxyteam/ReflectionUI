import java.io.File;
import java.io.IOException;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.CustomizableSwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SystemProperties;

public class CustomizeInfoCustimizations {

	public static void main(String[] args) {
		System.setProperty(SystemProperties.AUTHORIZE_INFO_CUSTOMIZATIONS_CONTROLS, "true");
		System.setProperty(SystemProperties.DISCARD_META_INFO_CUSTOMIZATIONS, "true");
		final InfoCustomizations infoCustomizations = new InfoCustomizations();
		String infoCustomizationsOutputFilePath = "D:/prog/git/ReflectionUI/reflection-ui/src/main/resources/xy/reflect/ui/resource/info-customizations-types.icu";
		try {
			infoCustomizations.loadFromFile(new File(infoCustomizationsOutputFilePath));
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return infoCustomizations.get(this, super.getTypeInfo(typeSource));
			}

		};
		CustomizableSwingRenderer renderer = new CustomizableSwingRenderer(reflectionUI, infoCustomizations,
				infoCustomizationsOutputFilePath);
		renderer.openObjectFrame(new InfoCustomizations());
	}

}
