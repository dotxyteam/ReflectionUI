
package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field proxy allowing to make the base field seem nullable. The null status of
 * the field is actually given by another boolean field.
 * 
 * @author olitank
 *
 */
public class ImportedNullStatusFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo nullStatusField;
	protected ITypeInfo objectType;

	public ImportedNullStatusFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, IFieldInfo nullStatusField,
			ITypeInfo objectType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.nullStatusField = nullStatusField;
		this.objectType = objectType;
	}

	protected boolean getNullStatus(Object object) {
		Object nullStatus = nullStatusField.getValue(object);
		if (!(nullStatus instanceof Boolean)) {
			throw new ReflectionUIError("Invalid null status field value (boolean expected): '" + nullStatus + "'");
		}
		return (Boolean) nullStatus;
	}

	@Override
	public boolean isGetOnly() {
		return super.isGetOnly() && nullStatusField.isGetOnly();
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

			@Override
			public Object invoke(Object ignore, InvocationData invocationData) {
				return ImportedNullStatusFieldInfo.super.getValue(object);
			}

			@Override
			public ITypeInfo getReturnValueType() {
				return ImportedNullStatusFieldInfo.this.getType();
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return Collections.emptyList();
			}
		});
	}

	@Override
	public ITypeInfo getType() {
		return new InfoProxyFactory() {

			@Override
			protected boolean isConcrete(ITypeInfo type) {
				return true;
			}

			@Override
			public String toString() {
				return "ConcreteTypeMaker [field=" + ImportedNullStatusFieldInfo.this + "]";
			}

		}.wrapTypeInfo(super.getType());
	}

	@Override
	public Object getValue(Object object) {
		if (getNullStatus(object)) {
			return super.getValue(object);
		} else {
			return null;
		}
	}

	@Override
	public void setValue(Object object, Object value) {
		if (value == null) {
			nullStatusField.setValue(object, Boolean.FALSE);
		} else {
			nullStatusField.setValue(object, Boolean.TRUE);
			if (!super.isGetOnly()) {
				super.setValue(object, value);
			}
		}
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(final Object object, final Object newValue) {
		if (newValue == null) {
			return nullStatusField.getNextUpdateCustomUndoJob(object, Boolean.FALSE);
		} else {
			final Runnable nullStatusFieldUndoJob = ReflectionUIUtils.getNextUpdateCustomOrDefaultUndoJob(object,
					nullStatusField, Boolean.TRUE);
			final Runnable baseUndoJob = ReflectionUIUtils.getNextUpdateCustomOrDefaultUndoJob(object, base, newValue);
			return new Runnable() {
				@Override
				public void run() {
					if (!ImportedNullStatusFieldInfo.super.isGetOnly()) {
						baseUndoJob.run();
					}
					nullStatusFieldUndoJob.run();
				}
			};
		}
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(final Object object, final Object newValue) {
		if (newValue == null) {
			return nullStatusField.getPreviousUpdateCustomRedoJob(object, Boolean.FALSE);
		} else {
			final Runnable nullStatusFieldUndoJob = ReflectionUIUtils.getPreviousUpdateCustomOrDefaultRedoJob(object,
					nullStatusField, Boolean.TRUE);
			final Runnable baseUndoJob = ReflectionUIUtils.getPreviousUpdateCustomOrDefaultRedoJob(object, base,
					newValue);
			return new Runnable() {
				@Override
				public void run() {
					if (!ImportedNullStatusFieldInfo.super.isGetOnly()) {
						baseUndoJob.run();
					}
					nullStatusFieldUndoJob.run();
				}
			};
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
		result = prime * result + ((nullStatusField == null) ? 0 : nullStatusField.hashCode());
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
		ImportedNullStatusFieldInfo other = (ImportedNullStatusFieldInfo) obj;
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
			return false;
		if (nullStatusField == null) {
			if (other.nullStatusField != null)
				return false;
		} else if (!nullStatusField.equals(other.nullStatusField))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImportedNullStatusFieldInfo [base=" + base + ", nullStatusField=" + nullStatusField + "]";
	}

}
