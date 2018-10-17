package xy.reflect.ui.control.swing.renderer;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.ReflectionUIError;

public class CustomizedSwingRenderer extends SwingRenderer {

	protected static CustomizedSwingRenderer defaultInstance;

	public static CustomizedSwingRenderer getDefault() {
		if (defaultInstance == null) {
			Class<?> customClass = SystemProperties.getAlternateDefaultCustomizedSwingRendererClass();
			if (customClass != null) {
				try {
					defaultInstance = (CustomizedSwingRenderer) customClass.getConstructor(CustomizedUI.class)
							.newInstance(CustomizedUI.getDefault());
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			} else {
				defaultInstance = new CustomizedSwingRenderer(CustomizedUI.getDefault());
			}
		}
		return defaultInstance;
	}

	public CustomizedSwingRenderer(CustomizedUI customizedUI) {
		super(customizedUI);
	}

	public CustomizedUI getCustomizedUI() {
		return (CustomizedUI) getReflectionUI();
	}

	public InfoCustomizations getInfoCustomizations() {
		return getCustomizedUI().getInfoCustomizations();
	}


}
