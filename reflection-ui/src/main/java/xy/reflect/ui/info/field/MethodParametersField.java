package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.util.MethodSetupObjectFactory;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodParametersField implements IFieldInfo {

	protected ReflectionUI reflectionUI;
	protected IMethodInfo method;
	protected String fieldName;

	protected MethodSetupObjectFactory factory;
	protected InvocationData invocationData = new InvocationData();

	public MethodParametersField(ReflectionUI reflectionUI, IMethodInfo method, String fieldName) {
		this.reflectionUI = reflectionUI;
		this.method = method;
		this.fieldName = fieldName;

		this.factory = createFactory();
	}

	private MethodSetupObjectFactory createFactory() {
		return new MethodSetupObjectFactory(reflectionUI, method);
	}

	@Override
	public String getName() {
		return fieldName;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(factory.getInstanceTypeInfoSource());
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		return factory.getInstance(object, invocationData);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new ReflectionUIError();
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public boolean isValueNullable() {
		return false;
	}

	@Override
	public boolean isGetOnly() {
		return true;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	public InfoCategory getCategory() {
		return method.getCategory();
	}

}
