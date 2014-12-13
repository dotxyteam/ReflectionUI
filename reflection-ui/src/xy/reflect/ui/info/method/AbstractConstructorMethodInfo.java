package xy.reflect.ui.info.method;

import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public abstract class AbstractConstructorMethodInfo implements IMethodInfo{

	private ITypeInfo ownerType;

	public AbstractConstructorMethodInfo(ITypeInfo ownerType) {
		super();
		this.ownerType = ownerType;
	}
	
	@Override
	public String getCaption() {
		return "Create '" + getReturnValueType().getCaption() + "'";
	}
	
	

	@Override
	public String getName() {
		return  toString();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return ownerType;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(getCaption());
		if (getParameters().size() > 0) {
			result.append("' - from (");
			int iParam = 0;
			for (IParameterInfo param : getParameters()) {
				if (iParam == 0) {
					result.append(param.getCaption());
				} else {
					result.append(", " + param.getCaption());
				}
				iParam++;
			}
			result.append(")");
		}
		return result.toString();
	}
	

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String getCategoryCaption() {
		return null;
	}


}
