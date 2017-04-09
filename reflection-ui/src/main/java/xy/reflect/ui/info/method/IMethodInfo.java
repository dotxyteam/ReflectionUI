package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;

public interface IMethodInfo extends IInfo {

	public IMethodInfo NULL_METHOD_INFO = new IMethodInfo() {

		@Override
		public String getName() {
			return "NULL_METHOD_INFO";
		}

		@Override
		public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
			return null;
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.DIRECT_OR_PROXY;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return null;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
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
		public String getNullReturnValueLabel() {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	ITypeInfo getReturnValueType();

	List<IParameterInfo> getParameters();

	Object invoke(Object object, InvocationData invocationData);

	boolean isReadOnly();

	String getNullReturnValueLabel();

	InfoCategory getCategory();

	Runnable getUndoJob(Object object, InvocationData invocationData);

	void validateParameters(Object object, InvocationData invocationData) throws Exception;

	ValueReturnMode getValueReturnMode();

	ITypeInfoProxyFactory getReturnValueTypeSpecificities();

}
