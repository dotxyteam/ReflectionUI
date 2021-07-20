


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

/**
 * Base class of virtual persistence method specifications.
 * 
 * @author olitank
 *
 */
public abstract class AbstractPersistenceMethod implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;

	public AbstractPersistenceMethod(ReflectionUI reflectionUI, ITypeInfo containingType) {
		this.reflectionUI = reflectionUI;
		this.containingType = containingType;
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
	public boolean isEnabled(Object object) {
		return true;
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
		public boolean isHidden() {
			return false;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public ITypeInfo getType() {
			return reflectionUI.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, File.class, null));
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
		public boolean hasValueOptions(Object object) {
			return false;
		}

		@Override
		public Object[] getValueOptions(Object object) {
			return null;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		AbstractPersistenceMethod other = (AbstractPersistenceMethod) obj;
		if (containingType == null) {
			if (other.containingType != null)
				return false;
		} else if (!containingType.equals(other.containingType))
			return false;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

}
