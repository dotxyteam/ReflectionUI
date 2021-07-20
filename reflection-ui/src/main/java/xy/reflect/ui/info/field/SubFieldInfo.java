
package xy.reflect.ui.info.field;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.FututreActionBuilder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual field allowing to view/edit a value returned by a field owned by an
 * object returned by another parent field.
 * 
 * @author olitank
 *
 */
public class SubFieldInfo extends AbstractInfo implements IFieldInfo {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo theField;
	protected IFieldInfo theSubField;
	protected FututreActionBuilder undoJobBuilder;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public SubFieldInfo(ReflectionUI reflectionUI, IFieldInfo theField, IFieldInfo theSubField,
			ITypeInfo containingType) {
		super();
		this.reflectionUI = reflectionUI;
		this.theField = theField;
		this.theSubField = theSubField;
		this.containingType = containingType;
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
	public void onControlVisibilityChange(Object object, boolean visible) {
		Object fieldValue = expectTheFieldValue(object);
		theSubField.onControlVisibilityChange(fieldValue, visible);
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(theSubField.getType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(containingType.getName(), SubFieldInfo.this.getName());
				}

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", subField="
							+ theSubField.getName() + ", field=" + theField.getName() + ", containingType="
							+ containingType.getName() + "]";
				}
			});
		}
		return type;
	}

	@Override
	public String getName() {
		return theField.getName() + "." + theSubField.getName();
	}

	@Override
	public String getCaption() {
		return theSubField.getCaption();
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		Object fieldValue = expectTheFieldValue(object);
		return theSubField.getAlternativeConstructors(fieldValue);
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		Object fieldValue = expectTheFieldValue(object);
		return theSubField.getAlternativeListItemConstructors(fieldValue);
	}

	@Override
	public Object getValue(Object object) {
		Object fieldValue = expectTheFieldValue(object);
		return theSubField.getValue(fieldValue);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		Object fieldValue = expectTheFieldValue(object);
		return theSubField.hasValueOptions(fieldValue);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object fieldValue = expectTheFieldValue(object);
		return theSubField.getValueOptions(fieldValue);
	}

	@Override
	public void setValue(Object object, Object subFieldValue) {
		Object fieldValue = expectTheFieldValue(object);
		theSubField.setValue(fieldValue, subFieldValue);
		if (isTheFieldUpdatePerformedAfterInvocation()) {
			if (undoJobBuilder != null) {
				Runnable theFieldUndoJob = ReflectionUIUtils.getNextUpdateUndoJob(object, theField, fieldValue);
				undoJobBuilder.setOption("theFieldUndoJob", theFieldUndoJob);
			}
			theField.setValue(object, fieldValue);
		}
		if (undoJobBuilder != null) {
			undoJobBuilder.build();
			undoJobBuilder = null;
		}
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object subFieldValue) {
		Object fieldValue = expectTheFieldValue(object);
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

	@Override
	public boolean isTransient() {
		return theSubField.isTransient();
	}

	protected Object expectTheFieldValue(Object object) {
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
