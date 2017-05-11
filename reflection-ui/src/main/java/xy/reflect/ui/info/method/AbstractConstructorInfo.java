package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractConstructorInfo extends AbstractInfo implements IMethodInfo {

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public String getCaption() {
		return "Create " + getReturnValueType().getCaption();
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return true;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
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
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		return getReturnValueType().hashCode() + getParameters().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!getReturnValueType().equals(((AbstractConstructorInfo) obj).getReturnValueType())) {
			return false;
		}
		if (!getParameters().equals(((AbstractConstructorInfo) obj).getParameters())) {
			return false;
		}
		return true;
	}

}
