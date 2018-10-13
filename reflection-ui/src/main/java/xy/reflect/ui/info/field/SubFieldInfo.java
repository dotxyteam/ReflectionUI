package xy.reflect.ui.info.field;

import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.FututreActionBuilder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SubFieldInfo extends AbstractInfo implements IFieldInfo {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo theField;
	protected IFieldInfo theSubField;
	protected FututreActionBuilder undoJobBuilder;

	public SubFieldInfo(ReflectionUI reflectionUI, IFieldInfo theField, IFieldInfo theSubField) {
		super();
		this.reflectionUI = reflectionUI;
		this.theField = theField;
		this.theSubField = theSubField;
	}

	public SubFieldInfo(ITypeInfo type, String fieldName, String subFieldName) {
		this.theField = ReflectionUIUtils.findInfoByName(type.getFields(), fieldName);
		if (this.theField == null) {
			throw new ReflectionUIError("Field '" + fieldName + "' not found in type '" + type.getName() + "'");
		}
		this.theSubField = ReflectionUIUtils.findInfoByName(this.theField.getType().getFields(), subFieldName);
		if (this.theSubField == null) {
			throw new ReflectionUIError(
					"Sub-Field '" + subFieldName + "' not found in field type '" + theField.getType().getName() + "'");
		}
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 1.0;
	}

	@Override
	public ITypeInfo getType() {
		return theSubField.getType();
	}

	@Override
	public String getName() {
		return theField.getName() + "." + theSubField.getName();
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.composeMessage(theField.getCaption(), theSubField.getCaption());
	}

	@Override
	public Object getValue(Object object) {
		Object fieldValue = getTheFieldValue(object);
		return theSubField.getValue(fieldValue);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object fieldValue = getTheFieldValue(object);
		return theSubField.getValueOptions(fieldValue);
	}

	@Override
	public void setValue(Object object, Object subFieldValue) {
		Object fieldValue = getTheFieldValue(object);
		theSubField.setValue(fieldValue, subFieldValue);
		if (isTheFieldUpdatePerformedAfterInvocation()) {
			if (undoJobBuilder != null) {
				Runnable theFieldUndoJob = theField.getNextUpdateCustomUndoJob(object, fieldValue);
				if (theFieldUndoJob == null) {
					theFieldUndoJob = ReflectionUIUtils.createDefaultUndoJob(reflectionUI, object, theField);
				}
				undoJobBuilder.setOption("theFieldUndoJob", theFieldUndoJob);
			}
			theField.setValue(object, fieldValue);
		}
		if (undoJobBuilder != null) {
			undoJobBuilder.build();
		}
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object subFieldValue) {
		Object fieldValue = getTheFieldValue(object);
		final Runnable theSubFieldUndoJob = theSubField.getNextUpdateCustomUndoJob(fieldValue, subFieldValue);
		if (theSubFieldUndoJob == null) {
			undoJobBuilder = null;
			return null;
		}
		undoJobBuilder = new FututreActionBuilder();
		return undoJobBuilder.will(new FututreActionBuilder.FuturePerformance() {
			@Override
			public void perform(Map<String, Object> options) {
				theSubFieldUndoJob.run();
				if (isTheFieldUpdatePerformedAfterInvocation()) {
					Runnable theFieldUndoJob = (Runnable) options.get("theFieldUndoJob");
					theFieldUndoJob.run();
				}
			}
		});
	}

	protected boolean isTheFieldUpdatePerformedAfterInvocation() {
		if (isGetOnly()) {
			return false;
		}
		return !theField.isGetOnly();
	}

	@Override
	public boolean isGetOnly() {
		if (theField.getValueReturnMode() == ValueReturnMode.CALCULATED) {
			if (theField.isGetOnly()) {
				return true;
			}
		}
		return theSubField.isGetOnly();
	}

	protected Object getTheFieldValue(Object object) {
		Object result = theField.getValue(object);
		if (result == null) {
			throw new ReflectionUIError("Sub-field error: Parent field value is missing");
		}
		return result;
	}

	@Override
	public boolean isNullValueDistinct() {
		return theSubField.isNullValueDistinct();
	}

	@Override
	public String getNullValueLabel() {
		return theSubField.getNullValueLabel();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.combine(theField.getValueReturnMode(), theSubField.getValueReturnMode());
	}

	@Override
	public InfoCategory getCategory() {
		return theField.getCategory();
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return Math.max(theField.getAutoUpdatePeriodMilliseconds(), theSubField.getAutoUpdatePeriodMilliseconds());
	}

	@Override
	public String getOnlineHelp() {
		return theSubField.getOnlineHelp();
	}

	public boolean isFormControlMandatory() {
		return theSubField.isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return theSubField.isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return theSubField.getFormControlFilter();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return theSubField.getSpecificProperties();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((theField == null) ? 0 : theField.hashCode());
		result = prime * result + ((theSubField == null) ? 0 : theSubField.hashCode());
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
		SubFieldInfo other = (SubFieldInfo) obj;
		if (theField == null) {
			if (other.theField != null)
				return false;
		} else if (!theField.equals(other.theField))
			return false;
		if (theSubField == null) {
			if (other.theSubField != null)
				return false;
		} else if (!theSubField.equals(other.theSubField))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubFieldInfo [theField=" + theField + ", theSubField=" + theSubField + "]";
	}

}
