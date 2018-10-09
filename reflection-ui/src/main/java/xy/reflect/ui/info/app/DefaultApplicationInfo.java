package xy.reflect.ui.info.app;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.util.SwingRendererUtils;

public class DefaultApplicationInfo implements IApplicationInfo {

	@Override
	public String getName() {
		return "application.default";
	}

	@Override
	public String getCaption() {
		return "Application";
	}

	@Override
	public boolean isSystemIntegrationNative() {
		return false;
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public ColorSpecification getMainBackgroundColor() {
		return SwingRendererUtils.getColorSpecification(new Color(245, 245, 245));
	}

	@Override
	public ColorSpecification getMainForegroundColor() {
		return SwingRendererUtils.getColorSpecification(new Color(0, 0, 0));
	}

	@Override
	public ResourcePath getMainBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getButtonBackgroundColor() {
		return SwingRendererUtils.getColorSpecification(new Color(95, 179, 216));
	}

	@Override
	public ColorSpecification getButtonForegroundColor() {
		return SwingRendererUtils.getColorSpecification(new Color(250, 250, 250));
	}

	@Override
	public ResourcePath getButtonBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getTitleBackgroundColor() {
		return SwingRendererUtils.getColorSpecification(new Color(18, 81, 117));
	}

	@Override
	public ColorSpecification getTitleForegroundColor() {
		return SwingRendererUtils.getColorSpecification(new Color(255, 255, 255));
	}

}
