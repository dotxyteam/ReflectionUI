


package xy.reflect.ui.info.app;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;

/**
 * Application information proxy class. The methods in this class should be overriden to
 * provide custom information.
 * 
 * @author olitank
 *
 */
public class ApplicationInfoProxy implements IApplicationInfo {

	protected IApplicationInfo base;

	public ApplicationInfoProxy(IApplicationInfo base) {
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public ColorSpecification getMainBackgroundColor() {
		return base.getMainBackgroundColor();
	}

	public ColorSpecification getMainForegroundColor() {
		return base.getMainForegroundColor();
	}

	public ColorSpecification getMainBorderColor() {
		return base.getMainBorderColor();
	}

	public ColorSpecification getMainEditorForegroundColor() {
		return base.getMainEditorForegroundColor();
	}

	public ColorSpecification getMainEditorBackgroundColor() {
		return base.getMainEditorBackgroundColor();
	}

	public ResourcePath getMainBackgroundImagePath() {
		return base.getMainBackgroundImagePath();
	}

	public ColorSpecification getMainButtonBackgroundColor() {
		return base.getMainButtonBackgroundColor();
	}

	public ColorSpecification getMainButtonForegroundColor() {
		return base.getMainButtonForegroundColor();
	}

	public ColorSpecification getMainButtonBorderColor() {
		return base.getMainButtonBorderColor();
	}

	public ColorSpecification getTitleBackgroundColor() {
		return base.getTitleBackgroundColor();
	}

	public ColorSpecification getTitleForegroundColor() {
		return base.getTitleForegroundColor();
	}

	public ResourcePath getMainButtonBackgroundImagePath() {
		return base.getMainButtonBackgroundImagePath();
	}

	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
	}

	public boolean isSystemIntegrationCrossPlatform() {
		return base.isSystemIntegrationCrossPlatform();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplicationInfoProxy other = (ApplicationInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ApplicationInfoProxy [base=" + base + "]";
	}

}
