package xy.reflect.ui.info.method;

import java.util.List;

import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public abstract class AbstractConstructorMethodInfo implements IMethodInfo {

	private ITypeInfo ownerType;

	public AbstractConstructorMethodInfo(ITypeInfo ownerType) {
		super();
		this.ownerType = ownerType;
	}

	@Override
	public String getCaption() {
		return "Create " + getReturnValueType().getCaption();
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return ownerType;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(getCaption());
		if (getParameters().size() > 0) {
			result.append(" - specify ");
			int iParam = 0;
			List<IParameterInfo> parameters = getParameters();
			for (IParameterInfo param : parameters) {
				if (iParam > 0) {
					if (iParam == parameters.size() - 1) {
						result.append(" and ");
					} else {
						result.append(", ");
					}
				}
				result.append("<" + param.getCaption() + ">");
				iParam++;
			}
		}
		return result.toString();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

}
