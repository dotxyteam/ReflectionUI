package xy.reflect.ui.info.field;

import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FieldInfoProxy implements IFieldInfo {

	protected StackTraceElement[] instanciationTrace = ReflectionUIUtils.createDebugStackTrace(1);

	protected IFieldInfo base;

	public FieldInfoProxy(IFieldInfo base) {
		this.base = base;
	}

	@Override
	public Object getValue(Object object) {
		return base.getValue(object);
	}

	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return base.getCustomUndoUpdateJob(object, value);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return base.getValueOptions(object);
	}

	@Override
	public ITypeInfo getType() {
		return base.getType();
	}

	@Override
	public String getCaption() {
		return base.getCaption();
	}

	@Override
	public void setValue(Object object, Object value) {
		base.setValue(object, value);
	}

	@Override
	public boolean isNullable() {
		return base.isNullable();
	}

	@Override
	public boolean isGetOnly() {
		return base.isGetOnly();
	}

	@Override
	public boolean isValueDetached() {
		return base.isValueDetached();
	}

	@Override
	public String toString() {
		return base.toString();
	}

	@Override
	public String getName() {
		return base.getName();
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return base.equals(((FieldInfoProxy) obj).base);
	}

	@Override
	public InfoCategory getCategory() {
		return base.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return base.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return base.getSpecificProperties();
	}

}
