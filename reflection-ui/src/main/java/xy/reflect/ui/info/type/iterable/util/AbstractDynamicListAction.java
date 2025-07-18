
package xy.reflect.ui.info.type.iterable.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This class exists as convenience for creating {@link IDynamicListAction}
 * objects. The implemented methods have a default behavior.
 * 
 * @author olitank
 *
 */
public abstract class AbstractDynamicListAction extends AbstractInfo implements IDynamicListAction {

	@Override
	public boolean isEnabled(Object object) {
		return true;
	}

	@Override
	public IValidationJob getReturnValueAbstractFormValidationJob(Object object, Object returnValue) {
		return null;
	}

	@Override
	public boolean isControlReturnValueValiditionEnabled() {
		return false;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public DisplayMode getDisplayMode() {
		return DisplayMode.TOOLBAR_AND_CONTEXT_MENU;
	}

	@Override
	public List<ItemPosition> getPostSelection() {
		return null;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isRelevant(Object object) {
		return true;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return null;
	}

	@Override
	public String getExecutionSuccessMessage() {
		return null;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
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
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
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
	public ITypeInfo getReturnValueType() {
		return null;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.emptyList();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.INDETERMINATE;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public Runnable getPreviousInvocationCustomRedoJob(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public int hashCode() {
		return getSignature().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!getSignature().equals(((AbstractDynamicListAction) obj).getSignature())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ListAction[name=" + getName() + "]";
	}

}
