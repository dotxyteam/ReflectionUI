package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractConstructorInfo implements IMethodInfo {

	protected ITypeInfo ownerType;

	public AbstractConstructorInfo(ITypeInfo ownerType) {
		super();
		this.ownerType = ownerType;
	}

	@Override
	public String getName() {
		StringBuilder result = new StringBuilder("new");
		for (IParameterInfo param : getParameters()) {
			result.append("+param" + param.getType().getName());
		}
		return result.toString();
	}

	@Override
	public String getCaption() {
		return "Create " + getReturnValueType().getCaption();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return ownerType;
	}

	public static String getDescription(IMethodInfo ctor) {
		StringBuilder result = new StringBuilder(ctor.getCaption());
		if (ctor.getParameters().size() == 0) {
			result.append(" - by default");
		} else {
			result.append(" - specify ");
			result.append(ReflectionUIUtils.formatParameterList(ctor.getParameters()));
		}
		return result.toString();
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
