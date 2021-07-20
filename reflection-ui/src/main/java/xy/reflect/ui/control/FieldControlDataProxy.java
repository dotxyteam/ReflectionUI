


package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Field control data proxy class. The methods in this class should be overriden to provide a
 * custom behavior.
 * 
 * @author olitank
 *
 */
public class FieldControlDataProxy implements IFieldControlData {

	protected IFieldControlData base;

	public FieldControlDataProxy(IFieldControlData base) {
		super();
		this.base = base;
	}

	public IFieldControlData getBase() {
		return base;
	}

	public Object getValue() {
		return base.getValue();
	}

	public void setValue(Object value) {
		base.setValue(value);
	}

	public String getCaption() {
		return base.getCaption();
	}

	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	public Runnable getNextUpdateCustomUndoJob(Object newValue) {
		return base.getNextUpdateCustomUndoJob(newValue);
	}

	public boolean isGetOnly() {
		return base.isGetOnly();
	}

	public boolean isTransient() {
		return base.isTransient();
	}

	public boolean isNullValueDistinct() {
		return base.isNullValueDistinct();
	}

	public String getNullValueLabel() {
		return base.getNullValueLabel();
	}

	public ITypeInfo getType() {
		return base.getType();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return base.getValueReturnMode();
	}

	public boolean isFormControlMandatory() {
		return base.isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return base.isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return base.getFormControlFilter();
	}

	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

	public ColorSpecification getLabelForegroundColor() {
		return base.getLabelForegroundColor();
	}

	public ColorSpecification getBorderColor() {
		return base.getBorderColor();
	}

	public ColorSpecification getEditorBackgroundColor() {
		return base.getEditorBackgroundColor();
	}

	public ColorSpecification getEditorForegroundColor() {
		return base.getEditorForegroundColor();
	}

	public ResourcePath getButtonBackgroundImagePath() {
		return base.getButtonBackgroundImagePath();
	}

	public ColorSpecification getButtonBackgroundColor() {
		return base.getButtonBackgroundColor();
	}

	public ColorSpecification getButtonForegroundColor() {
		return base.getButtonForegroundColor();
	}

	public ColorSpecification getButtonBorderColor() {
		return base.getButtonBorderColor();
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
		FieldControlDataProxy other = (FieldControlDataProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlDataProxy [base=" + base + "]";
	}

}
