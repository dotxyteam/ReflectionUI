


package xy.reflect.ui.info.type.iterable.util;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;

/**
 * Dynamic list action proxy class. The methods in this class should be
 * overriden to provide custom information.
 * 
 * @author olitank
 *
 */
public class DynamicListActionProxy implements IDynamicListAction {

	protected IDynamicListAction base;

	public DynamicListActionProxy(IDynamicListAction base) {
		this.base = base;
	}

	@Override
	public String getName() {
		return base.getName();
	}

	@Override
	public String getCaption() {
		return base.getCaption();
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return base.getParametersValidationCustomCaption();
	}

	@Override
	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	@Override
	public List<ItemPosition> getPostSelection() {
		return base.getPostSelection();
	}

	@Override
	public boolean isEnabled(Object object) {
		return base.isEnabled(object);
	}

	@Override
	public String getSignature() {
		return base.getSignature();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return base.getReturnValueType();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return base.getParameters();
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		return base.invoke(object, invocationData);
	}

	@Override
	public boolean isReadOnly() {
		return base.isReadOnly();
	}

	@Override
	public String getNullReturnValueLabel() {
		return base.getNullReturnValueLabel();
	}

	@Override
	public InfoCategory getCategory() {
		return base.getCategory();
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return base.getNextInvocationUndoJob(object, invocationData);
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		base.validateParameters(object, invocationData);
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	@Override
	public ResourcePath getIconImagePath() {
		return base.getIconImagePath();
	}

	@Override
	public boolean isReturnValueDetached() {
		return base.isReturnValueDetached();
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return base.isNullReturnValueDistinct();
	}

	@Override
	public boolean isReturnValueIgnored() {
		return base.isReturnValueIgnored();
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return base.getConfirmationMessage(object, invocationData);
	}

	@Override
	public boolean isHidden() {
		return base.isHidden();
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean b) {
		base.onControlVisibilityChange(object, b);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		DynamicListActionProxy other = (DynamicListActionProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ListActionProxy [signature=" + getSignature() + ", base=" + base + "]";
	}

}
