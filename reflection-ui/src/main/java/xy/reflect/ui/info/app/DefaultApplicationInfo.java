


package xy.reflect.ui.info.app;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;

/**
 * A default implementation of {@link IApplicationInfo}.
 * 
 * @author olitank
 *
 */
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
	public boolean isSystemIntegrationCrossPlatform() {
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
		return null;
	}

	@Override
	public ColorSpecification getMainForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainEditorForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainEditorBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainBorderColor() {
		return null;
	}

	@Override
	public ResourcePath getMainBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getMainButtonBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainButtonForegroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getMainButtonBorderColor() {
		return null;
	}

	@Override
	public ResourcePath getMainButtonBackgroundImagePath() {
		return null;
	}

	@Override
	public ColorSpecification getTitleBackgroundColor() {
		return null;
	}

	@Override
	public ColorSpecification getTitleForegroundColor() {
		return null;
	}

}
