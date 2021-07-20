


package xy.reflect.ui.control.swing.renderer;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * A sub-class of {@link SwingRenderer} supporting customizations by default.
 * 
 * @author olitank
 *
 */
public class CustomizedSwingRenderer extends SwingRenderer {

	protected static CustomizedSwingRenderer defaultInstance;

	/**
	 * @return the default instance of this class. This instance is constructed with
	 *         the {@link CustomizedUI#getDefault()} return value.
	 */
	public static CustomizedSwingRenderer getDefault() {
		if (defaultInstance == null) {
			Class<?> customClass = SystemProperties.getAlternateDefaultCustomizedSwingRendererClass();
			if (customClass != null) {
				try {
					defaultInstance = (CustomizedSwingRenderer) customClass.getMethod("getDefault").invoke(null);
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

	public CustomizedSwingRenderer() {
		this(CustomizedUI.getDefault());
	}

	public CustomizedUI getCustomizedUI() {
		return (CustomizedUI) getReflectionUI();
	}

	public InfoCustomizations getInfoCustomizations() {
		return getCustomizedUI().getInfoCustomizations();
	}

}
