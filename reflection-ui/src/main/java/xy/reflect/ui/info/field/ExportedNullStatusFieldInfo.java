package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ExportedNullStatusFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public ExportedNullStatusFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo containingType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.containingType = containingType;
	}

	protected Object valueToBoolean(Object value) {
		return value != null;
	}

	protected Object booleanTovalue(Boolean value, Object object) {
		if ((Boolean) value) {
			return ReflectionUIUtils.createDefaultInstance(super.getType(), object);
		} else {
			return null;
		}
	}

	@Override
	public Object getValue(Object object) {
		return valueToBoolean(super.getValue(object));
	}

	@Override
	public void setValue(Object object, Object value) {
		super.setValue(object, booleanTovalue((Boolean) value, object));
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(final Object object, final Object newValue) {
		final Object baseNewValue = booleanTovalue((Boolean) newValue, object);
		Runnable result = super.getNextUpdateCustomUndoJob(object, baseNewValue);
		if (result == null) {
			final Object baseOldValue = super.getValue(object);
			result = new Runnable() {
				@Override
				public void run() {
					base.setValue(object, baseOldValue);
				}
			};
		}
		return result;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(boolean.class,
					new SpecificitiesIdentifier(containingType.getName(), ExportedNullStatusFieldInfo.this.getName())));
		}
		return type;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultFieldCaption(this);
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExportedNullStatusFieldInfo other = (ExportedNullStatusFieldInfo) obj;
		if (containingType == null) {
			if (other.containingType != null)
				return false;
		} else if (!containingType.equals(other.containingType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NullStatusField []";
	}

}
