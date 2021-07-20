


package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field proxy allowing to change the type by performing the specified value
 * conversions.
 * 
 * @author olitank
 *
 */
public class ChangedTypeFieldInfo extends FieldInfoProxy {

	protected ITypeInfo newType;
	protected Filter<Object> conversionMethod;
	protected Filter<Object> reverseConversionMethod;
	protected boolean nullValueConverted;

	public ChangedTypeFieldInfo(IFieldInfo base, ITypeInfo newType, Filter<Object> conversionMethod,
			Filter<Object> reverseConversionMethod, boolean nullValueConverted) {
		super(base);
		this.newType = newType;
		this.conversionMethod = conversionMethod;
		this.reverseConversionMethod = reverseConversionMethod;
		this.nullValueConverted = nullValueConverted;
	}

	protected Object convert(Object value) {
		if (conversionMethod == null) {
			return value;
		}
		if (value == null) {
			if (!nullValueConverted) {
				return null;
			}
		}
		try {
			return conversionMethod.get(value);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	protected Object revertConversion(Object value) {
		if (reverseConversionMethod == null) {
			return value;
		}
		if (value == null) {
			return null;
		}
		try {
			return reverseConversionMethod.get(value);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public Object getValue(Object object) {
		Object value = super.getValue(object);
		Object result = convert(value);
		return result;
	}

	@Override
	public void setValue(Object object, Object value) {
		super.setValue(object, revertConversion(value));
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(final Object object, final Object newValue) {
		return super.getNextUpdateCustomUndoJob(object, revertConversion(newValue));
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object[] result = super.getValueOptions(object);
		if (result == null) {
			return null;
		}
		Object[] convertedResult = new Object[result.length];
		for (int i = 0; i < result.length; i++) {
			convertedResult[i] = convert(result[i]);
		}
		return convertedResult;
	}

	@Override
	public ITypeInfo getType() {
		return newType;
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		List<IMethodInfo> superResult = super.getAlternativeConstructors(object);
		if (superResult == null) {
			return null;
		}
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo superCtor : superResult) {
			result.add(new MethodInfoProxy(superCtor) {

				@Override
				public ITypeInfo getReturnValueType() {
					return newType;
				}

				@Override
				public Object invoke(Object object, InvocationData invocationData) {
					return convert(super.invoke(object, invocationData));
				}

			});
		}
		return result;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.CALCULATED;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((conversionMethod == null) ? 0 : conversionMethod.hashCode());
		result = prime * result + ((newType == null) ? 0 : newType.hashCode());
		result = prime * result + ((reverseConversionMethod == null) ? 0 : reverseConversionMethod.hashCode());
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
		ChangedTypeFieldInfo other = (ChangedTypeFieldInfo) obj;
		if (conversionMethod == null) {
			if (other.conversionMethod != null)
				return false;
		} else if (!conversionMethod.equals(other.conversionMethod))
			return false;
		if (newType == null) {
			if (other.newType != null)
				return false;
		} else if (!newType.equals(other.newType))
			return false;
		if (reverseConversionMethod == null) {
			if (other.reverseConversionMethod != null)
				return false;
		} else if (!reverseConversionMethod.equals(other.reverseConversionMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChangedTypeField [newType=" + newType + ", conversionMethod=" + conversionMethod
				+ ", reverseConversionMethod=" + reverseConversionMethod + "]";
	}

}
