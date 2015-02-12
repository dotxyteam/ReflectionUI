package xy.reflect.ui.info.method;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

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
		if (getParameters().size() == 0) {
			result.append(" - by default");			
		}else{
			result.append(" - specify ");
			result.append(ReflectionUIUtils
					.formatParameterList(getParameters()));
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

	@Override
	public String getDocumentation() {
		return null;
	}

}
