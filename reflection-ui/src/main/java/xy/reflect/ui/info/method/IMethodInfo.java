package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public interface IMethodInfo extends IInfo {

	public IMethodInfo NULL_METHOD_INFO = new IMethodInfo() {

		@Override
		public String getName() {
			return "NULL_METHOD_INFO";
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public String getSignature() {
			return ReflectionUIUtils.buildMethodSignature(this);
		}

		@Override
		public String getConfirmationMessage(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
		}

		@Override
		public boolean isNullReturnValueDistinct() {
			return false;
		}

		@Override
		public boolean isReturnValueDetached() {
			return false;
		}

		@Override
		public boolean isReturnValueIgnored() {
			return false;
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
			return true;
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
		public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
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

	String getSignature();

	ITypeInfo getReturnValueType();

	List<IParameterInfo> getParameters();

	Object invoke(Object object, InvocationData invocationData);

	boolean isReadOnly();

	String getNullReturnValueLabel();

	InfoCategory getCategory();

	Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData);

	void validateParameters(Object object, InvocationData invocationData) throws Exception;

	ValueReturnMode getValueReturnMode();

	ResourcePath getIconImagePath();

	boolean isReturnValueDetached();

	boolean isNullReturnValueDistinct();

	boolean isReturnValueIgnored();

	String getConfirmationMessage(Object object, InvocationData invocationData);

	boolean isHidden();

}
