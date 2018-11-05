package xy.reflect.ui.control.swing.renderer;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * A sub-class of {@link SwingRenderer} supporting customizations by default.
 * 
 * @author olitank
 *
 */
public class CustomizedSwingRenderer extends SwingRenderer {

	public static void main(String[] args) throws Exception {
		Class<?> clazz = Object.class;
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instanciate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + SystemProperties.describe();
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
		Object object = CustomizedSwingRenderer.getDefault().onTypeInstanciationRequest(null,
				CustomizedSwingRenderer.getDefault().getReflectionUI().getTypeInfo(new JavaTypeInfoSource(clazz, null)),
				null);
		if (object == null) {
			return;
		}
		CustomizedSwingRenderer.getDefault().openObjectFrame(object);
	}

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
