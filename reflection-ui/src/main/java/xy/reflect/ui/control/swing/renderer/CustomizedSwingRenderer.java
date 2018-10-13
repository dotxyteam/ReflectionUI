package xy.reflect.ui.control.swing.renderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.plugin.ColorPickerPlugin;
import xy.reflect.ui.control.swing.plugin.DetailedListControlPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.ImageViewPlugin;
import xy.reflect.ui.control.swing.plugin.OptionButtonsPlugin;
import xy.reflect.ui.control.swing.plugin.SliderPlugin;
import xy.reflect.ui.control.swing.plugin.SpinnerPlugin;
import xy.reflect.ui.control.swing.plugin.CustomCheckBoxPlugin;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CustomizedSwingRenderer extends SwingRenderer {

	protected static CustomizedSwingRenderer defaultInstance;

	protected String infoCustomizationsFilePath;

	public static CustomizedSwingRenderer getDefault() {
		if (defaultInstance == null) {
			Class<?> customClass = SystemProperties.getAlternateCustomizedSwingRendererClass();
			if (customClass != null) {
				try {
					defaultInstance = (CustomizedSwingRenderer) customClass
							.getConstructor(CustomizedUI.class, String.class).newInstance(CustomizedUI.getDefault(),
									SystemProperties.getDefaultInfoCustomizationsFilePath());
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			} else {
				defaultInstance = new CustomizedSwingRenderer(CustomizedUI.getDefault(),
						SystemProperties.getDefaultInfoCustomizationsFilePath());
			}
		}
		return defaultInstance;
	}

	public CustomizedSwingRenderer(CustomizedUI customizedUI, String infoCustomizationsFilePath) {
		super(customizedUI);
		this.infoCustomizationsFilePath = infoCustomizationsFilePath;
		if (infoCustomizationsFilePath != null) {
			File file = new File(infoCustomizationsFilePath);
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
	}

	public CustomizedUI getCustomizedUI() {
		return (CustomizedUI) getReflectionUI();
	}

	public InfoCustomizations getInfoCustomizations() {
		return getCustomizedUI().getInfoCustomizations();
	}

	public String getInfoCustomizationsOutputFilePath() {
		return infoCustomizationsFilePath;
	}

	public List<IFieldControlPlugin> getFieldControlPlugins() {
		List<IFieldControlPlugin> result = new ArrayList<IFieldControlPlugin>();
		result.add(new OptionButtonsPlugin());
		result.add(new SliderPlugin());
		result.add(new SpinnerPlugin());
		result.add(new FileBrowserPlugin());
		result.add(new ColorPickerPlugin());
		result.add(new ImageViewPlugin());
		result.add(new CustomCheckBoxPlugin());
		result.add(new DetailedListControlPlugin());
		return result;
	}

}
