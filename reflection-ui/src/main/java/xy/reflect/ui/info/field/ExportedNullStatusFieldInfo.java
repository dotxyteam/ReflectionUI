


package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field proxy allowing only to view and change the null status (as a boolean
 * value) of the base field.
 * 
 * @author olitank
 *
 */
public class ExportedNullStatusFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo objectType;
	protected ITypeInfo type;

	public ExportedNullStatusFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo objectType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.objectType = objectType;
	}

	protected Object valueToBoolean(Object value) {
		return value != null;
	}

	protected Object booleanTovalue(Boolean value) {
		if ((Boolean) value) {
			return ReflectionUIUtils.createDefaultInstance(super.getType());
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return base.getName() + ".nullStatus";
	}

	@Override
	public String getCaption() {
		return "Set " + base.getCaption();
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		return valueToBoolean(super.getValue(object));
	}

	@Override
	public void setValue(Object object, Object newValue) {
		boolean currentNullStatus = (Boolean) getValue(object);
		boolean newNullStatus = (Boolean) newValue;
		if (currentNullStatus == newNullStatus) {
			return;
		}
		super.setValue(object, booleanTovalue((Boolean) newValue));
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(final Object object, final Object newValue) {
		boolean currentNullStatus = (Boolean) getValue(object);
		boolean newNullStatus = (Boolean) newValue;
		if (currentNullStatus == newNullStatus) {
			return new Runnable() {
				@Override
				public void run() {
				}
			};
		}
		final Object baseNewValue = booleanTovalue((Boolean) newValue);
		return ReflectionUIUtils.getNextUpdateCustomOrDefaultUndoJob(object, base, baseNewValue);
	}
	
	@Override
	public Runnable getPreviousUpdateCustomRedoJob(final Object object, final Object newValue) {
		boolean currentNullStatus = (Boolean) getValue(object);
		boolean newNullStatus = (Boolean) newValue;
		if (currentNullStatus == newNullStatus) {
			return new Runnable() {
				@Override
				public void run() {
				}
			};
		}
		final Object baseNewValue = booleanTovalue((Boolean) newValue);
		return ReflectionUIUtils.getPreviousUpdateCustomOrDefaultRedoJob(object, base, baseNewValue);
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(boolean.class,
					new SpecificitiesIdentifier(objectType.getName(), ExportedNullStatusFieldInfo.this.getName())));
		}
		return type;
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
		result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
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
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NullStatusField [base=" + base + "]";
	}

}
