package xy.reflect.ui.control;

import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Base class of common implementations of {@link IFieldControlData}.
 * 
 * @author olitank
 *
 */
public abstract class AbstractFieldControlData implements IFieldControlData {

	protected ReflectionUI reflectionUI;

	/**
	 * @return the underlying object.
	 */
	protected abstract Object getObject();

	/**
	 * @return the underlying field information.
	 */
	protected abstract IFieldInfo getField();

	public AbstractFieldControlData(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	public Object getValue() {
		return getField().getValue(getObject());
	}

	@Override
	public void setValue(Object value) {
		getField().setValue(getObject(), value);
	}

	@Override
	public String getCaption() {
		return getField().getCaption();
	}

	@Override
	public String getOnlineHelp() {
		return getField().getOnlineHelp();
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object newValue) {
		return getField().getNextUpdateCustomUndoJob(getObject(), newValue);
	}

	@Override
	public ITypeInfo getType() {
		return getField().getType();
	}

	@Override
	public boolean isGetOnly() {
		return getField().isGetOnly();
	}

	@Override
	public boolean isTransient() {
		return getField().isTransient();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return getField().getValueReturnMode();
	}

	@Override
	public boolean isNullValueDistinct() {
		return getField().isNullValueDistinct();
	}

	@Override
	public String getNullValueLabel() {
		return getField().getNullValueLabel();
	}

	public boolean isFormControlMandatory() {
		return getField().isFormControlMandatory();
	}

	public boolean isFormControlEmbedded() {
		return getField().isFormControlEmbedded();
	}

	public IInfoFilter getFormControlFilter() {
		return getField().getFormControlFilter();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return getField().getSpecificProperties();
	}

	@Override
	public ColorSpecification getLabelForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormForegroundColor() != null) {
				return type.getFormForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainForegroundColor();
	}

	@Override
	public ColorSpecification getBorderColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormBorderColor() != null) {
				return type.getFormBorderColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainBorderColor();
	}

	@Override
	public ColorSpecification getEditorForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormEditorsForegroundColor() != null) {
				return type.getFormEditorsForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainEditorForegroundColor();
	}

	@Override
	public ColorSpecification getEditorBackgroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormEditorsBackgroundColor() != null) {
				return type.getFormEditorsBackgroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainEditorBackgroundColor();
	}

	@Override
	public ResourcePath getButtonBackgroundImagePath() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundImagePath() != null) {
				return type.getFormButtonBackgroundImagePath();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath();
	}

	@Override
	public ColorSpecification getButtonBackgroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBackgroundColor() != null) {
				return type.getFormButtonBackgroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBackgroundColor();
	}

	@Override
	public ColorSpecification getButtonForegroundColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonForegroundColor() != null) {
				return type.getFormButtonForegroundColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonForegroundColor();
	}

	@Override
	public ColorSpecification getButtonBorderColor() {
		if (getObject() != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			if (type.getFormButtonBorderColor() != null) {
				return type.getFormButtonBorderColor();
			}
		}
		return reflectionUI.getApplicationInfo().getMainButtonBorderColor();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		AbstractFieldControlData other = (AbstractFieldControlData) obj;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		if (getField() == null) {
			if (other.getField() != null)
				return false;
		} else if (!getField().equals(other.getField()))
			return false;
		if (getObject() == null) {
			if (other.getObject() != null)
				return false;
		} else if (!getObject().equals(other.getObject()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldControlData [object=" + getObject() + ", field=" + getField() + "]";
	}

}