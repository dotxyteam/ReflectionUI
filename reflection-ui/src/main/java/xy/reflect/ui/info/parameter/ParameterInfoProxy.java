package xy.reflect.ui.info.parameter;

import java.util.Collections;
import java.util.Map;

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

	public boolean isValueNullable() {
		return base.isValueNullable();
	}

	public Object getDefaultValue() {
		return base.getDefaultValue();
	}

	public int getPosition() {
		return base.getPosition();
	}

	

	

	@Override
	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}


	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
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
		ParameterInfoProxy other = (ParameterInfoProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterInfoProxy [base=" + base + "]";
	}

}
