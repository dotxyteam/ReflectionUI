package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.menu.IMenuElementPosition;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;

public class MethodInfoProxy implements IMethodInfo {

	protected IMethodInfo base;

	public MethodInfoProxy(IMethodInfo base) {
		this.base = base;
	}

	public IMethodInfo getBase() {
		return base;
	}

	@Override
	public IMenuElementPosition getMenuItemPosition() {
		return null;
	}

	public boolean isReturnValueNullable() {
		return base.isReturnValueNullable();
	}

	public boolean isReturnValueDetached() {
		return base.isReturnValueDetached();
	}

	public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
		return base.getReturnValueTypeSpecificities();
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	public Object invoke(Object object, InvocationData invocationData) {
		return base.invoke(object, invocationData);
	}

	public String getNullReturnValueLabel() {
		return base.getNullReturnValueLabel();
	}

	@Override
	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	@Override
	public InfoCategory getCategory() {
		return base.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	@Override
	public Runnable getUndoJob(Object object, InvocationData invocationData) {
		return base.getUndoJob(object, invocationData);
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		base.validateParameters(object, invocationData);
	}

	public String getIconImagePath() {
		return base.getIconImagePath();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
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
		MethodInfoProxy other = (MethodInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodInfoProxy [base=" + base + "]";
	}

}
