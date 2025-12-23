
package xy.reflect.ui.info.menu;

import java.util.function.Supplier;

import xy.reflect.ui.info.ResourcePath;

/**
 * This class represents a menu item that will be used to execute a given
 * method.
 * 
 * @author olitank
 *
 */
public class CustomActionMenuItemInfo extends AbstractActionMenuItemInfo {

	protected Supplier<Boolean> enablementStateSupplier;
	protected Runnable runnable;

	public CustomActionMenuItemInfo(String name, ResourcePath iconImagePath,
			Supplier<Boolean> enablementStateSupplier, Runnable runnable) {
		super(name, iconImagePath);
		this.enablementStateSupplier = enablementStateSupplier;
		this.runnable = runnable;
	}

	public Supplier<Boolean> getEnablementStateSupplier() {
		return enablementStateSupplier;
	}

	public void setEnablementStateSupplier(Supplier<Boolean> enablementStateSupplier) {
		this.enablementStateSupplier = enablementStateSupplier;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((enablementStateSupplier == null) ? 0 : enablementStateSupplier.hashCode());
		result = prime * result + ((runnable == null) ? 0 : runnable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomActionMenuItemInfo other = (CustomActionMenuItemInfo) obj;
		if (enablementStateSupplier == null) {
			if (other.enablementStateSupplier != null)
				return false;
		} else if (!enablementStateSupplier.equals(other.enablementStateSupplier))
			return false;
		if (runnable == null) {
			if (other.runnable != null)
				return false;
		} else if (!runnable.equals(other.runnable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CustomActionMenuItemInfo [name=" + caption + "]";
	}

}
