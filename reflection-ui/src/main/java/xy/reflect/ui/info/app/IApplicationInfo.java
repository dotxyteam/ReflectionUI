package xy.reflect.ui.info.app;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;

public interface IApplicationInfo extends IInfo {

	ColorSpecification getMainBackgroundColor();

	ColorSpecification getMainForegroundColor();

	ResourcePath getMainBackgroundImagePath();

	ColorSpecification getButtonBackgroundColor();

	ColorSpecification getButtonForegroundColor();

	ColorSpecification getTitleBackgroundColor();

	ColorSpecification getTitleForegroundColor();

	ResourcePath getButtonBackgroundImagePath();
	
	ResourcePath getIconImagePath();

	boolean isSystemIntegrationNative();


}
