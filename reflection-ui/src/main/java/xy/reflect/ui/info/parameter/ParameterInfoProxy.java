package xy.reflect.ui.info.parameter;

import xy.reflect.ui.info.type.ITypeInfo;

public class ParameterInfoProxy implements IParameterInfo {

	protected IParameterInfo base;

	public ParameterInfoProxy(IParameterInfo base) {
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public ITypeInfo getType() {
		return base.getType();
	}

	public boolean isNullable() {
		return base.isNullable();
	}

	public Object getDefaultValue() {
		return base.getDefaultValue();
	}

	public int getPosition() {
		return base.getPosition();
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return base.equals(((ParameterInfoProxy) obj).base);
	}

	@Override
	public String toString() {
		return base.toString();
	}

	@Override
	public String getDocumentation() {
		return base.getDocumentation();
	}
	

}