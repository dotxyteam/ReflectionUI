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
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ReflectionUIError;

public class ImportedNullStatusFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo nullStatusField;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public ImportedNullStatusFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, IFieldInfo nullStatusField,
			ITypeInfo containingType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.nullStatusField = nullStatusField;
		this.containingType = containingType;
	}

	@Override
	public boolean isGetOnly() {
		return super.isGetOnly() && nullStatusField.isGetOnly();
	}

	@Override
	public Object getValue(Object object) {
		if (getNullStatus(object)) {
			return super.getValue(object);
		} else {
			return null;
		}
	}

	protected boolean getNullStatus(Object object) {
		Object nullStatus = nullStatusField.getValue(object);
		if (!(nullStatus instanceof Boolean)) {
			throw new ReflectionUIError("Invalid null status field value (boolean expected): '" + nullStatus + "'");
		}
		return (Boolean) nullStatus;
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
			final Runnable job1 = nullStatusField.getNextUpdateCustomUndoJob(object, Boolean.TRUE);
			final Runnable job2 = super.getNextUpdateCustomUndoJob(object, newValue);
			if ((job1 == null) && (job2 == null)) {
				return null;
			}
			return new Runnable() {
				@Override
				public void run() {
					if (job1 != null) {
						job1.run();
					}
					if (job2 != null) {
						job2.run();
					}
				}
			};
		}
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = super.getType();
			type = new InfoProxyFactory() {

				@Override
				protected boolean isConcrete(ITypeInfo type) {
					return true;
				}

				@Override
				protected List<IMethodInfo> getConstructors(final ITypeInfo type) {
					return Collections.<IMethodInfo>singletonList(new AbstractConstructorInfo() {

						@Override
						public Object invoke(Object parentObject, InvocationData invocationData) {
							return ImportedNullStatusFieldInfo.super.getValue(parentObject);
						}

						@Override
						public ITypeInfo getReturnValueType() {
							return type;
						}

						@Override
						public List<IParameterInfo> getParameters() {
							return Collections.emptyList();
						}
					});
				}

				@Override
				public String toString() {
					return "setFakeValueContructor [field=" + ImportedNullStatusFieldInfo.this + "]";
				}

			}.wrapTypeInfo(type);
			type = reflectionUI.getTypeInfo(new TypeInfoSourceProxy(type.getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return new SpecificitiesIdentifier(containingType.getName(),
							ImportedNullStatusFieldInfo.this.getName());
				}
			});
		}
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((containingType == null) ? 0 : containingType.hashCode());
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
		if (containingType == null) {
			if (other.containingType != null)
				return false;
		} else if (!containingType.equals(other.containingType))
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
