package xy.reflect.ui.info.method;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractPersistenceMethod implements IMethodInfo{

	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;

	public AbstractPersistenceMethod() {
		super();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return null;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.<IParameterInfo>singletonList(new FileParameter());
	}

	@Override
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public InfoCategory getCategory() {
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
	public ValueReturnMode getValueReturnMode() {
		return null;
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return null;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	protected class FileParameter implements IParameterInfo {

		@Override
		public String getName() {
			return "file";
		}

		@Override
		public String getCaption() {
			return "File";
		}

		@Override
		public String getOnlineHelp() {
			return "The file.";
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.getTypeInfo(new JavaTypeInfoSource(File.class, null));
		}

		@Override
		public boolean isNullValueDistinct() {
			return false;
		}

		@Override
		public Object getDefaultValue(Object object) {
			return null;
		}

		@Override
		public int getPosition() {
			return 0;
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

	}

}