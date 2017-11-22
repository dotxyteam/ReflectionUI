package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIUtils;

public class FieldAsSetterInfo extends AbstractInfo implements IMethodInfo {

	protected IFieldInfo field;

	protected IParameterInfo parameter = new ParameterInfoProxy(IParameterInfo.NULL_PARAMETER_INFO) {
		
		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public String getCaption() {
			return field.getCaption();
		}

		@Override
		public ITypeInfo getType() {
			return field.getType();
		}

		@Override
		public boolean isNullValueDistinct() {
			return field.isNullValueDistinct();
		}

		@Override
		public int getPosition() {
			return 0;
		}

		@Override
		public String toString() {
			return "Parameter [of=" + FieldAsSetterInfo.this.toString() + "]";
		}

	};

	public FieldAsSetterInfo(IFieldInfo field) {
		this.field = field;
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		return null;
	}

	@Override
	public String getName() {
		return field.getName() + ".set";
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public ResourcePath getIconImagePath() {
		return null;
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return false;
	}

	@Override
	public boolean isReturnValueDetached() {
		return false;
	}

	@Override
	public boolean isReturnValueIgnored() {
		return false;
	}

	@Override
	public IInfoProxyFactory getReturnValueTypeSpecificities() {
		return null;
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.getDefaultMethodCaption(this);
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return field.getSpecificProperties();
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return null;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.<IParameterInfo>singletonList(parameter);
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object value = invocationData.getParameterValue(parameter);
		field.setValue(object, value);
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getNullReturnValueLabel() {
		return null;
	}

	@Override
	public InfoCategory getCategory() {
		return field.getCategory();
	}

	@Override
	public Runnable getNextInvocationUndoJob(final Object object, InvocationData invocationData) {
		Object value = invocationData.getParameterValue(parameter);
		Runnable result = field.getNextUpdateCustomUndoJob(object, value);
		if (result == null) {
			final Object oldValue = field.getValue(object);
			result = new Runnable() {
				@Override
				public void run() {
					field.setValue(object, oldValue);
				}
			};
		}
		return result;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
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
		FieldAsSetterInfo other = (FieldAsSetterInfo) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FieldAsSetter [field=" + field + "]";
	}

}
