package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodInfoProxy implements IMethodInfo {

	protected StackTraceElement[] instanciationTrace = ReflectionUIUtils
			.createDebugTrace();

	protected IMethodInfo base;

	public MethodInfoProxy(IMethodInfo base) {
		this.base = base;
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

	public Object invoke(Object object,
			Map<Integer, Object> valueByParameterPosition) {
		return base.invoke(object, valueByParameterPosition);
	}

	@Override
	public String toString() {
		return base.toString();
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
		return base.equals(((MethodInfoProxy) obj).base);
	}

	@Override
	public boolean isReadOnly() {
		return base.isReadOnly();
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
	public IModification getUndoModification(Object object,
			Map<Integer, Object> valueByParameterPosition) {
		return base.getUndoModification(object, valueByParameterPosition);
	}

	@Override
	public void validateParameters(Object object,
			Map<Integer, Object> valueByParameterPosition) throws Exception {
		base.validateParameters(object, valueByParameterPosition);
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}
}
