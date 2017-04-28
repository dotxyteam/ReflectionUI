package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.menu.IMenuElementPosition;

public abstract class AbstractConstructorInfo implements IMethodInfo {

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getCaption() {
		return "Create " + getReturnValueType().getCaption();
	}

	@Override
	public IMenuElementPosition getMenuItemPosition() {
		return null;
	}

	@Override
	public String getIconImagePath() {
		return null;
	}

	@Override
	public boolean isReturnValueNullable() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return true;
	}

	@Override
	public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.CALCULATED;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Runnable getUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

}
