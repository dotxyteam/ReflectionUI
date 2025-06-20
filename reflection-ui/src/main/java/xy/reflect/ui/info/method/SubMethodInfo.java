
package xy.reflect.ui.info.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.FutureActionBuilder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Virtual method allowing to invoke a method owned by an object returned by a
 * parent field.
 * 
 * @author olitank
 *
 */
public class SubMethodInfo extends AbstractInfo implements IMethodInfo {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo theField;
	protected IMethodInfo theSubMethod;
	protected FutureActionBuilder undoJobBuilder;
	protected FutureActionBuilder redoJobBuilder;
	protected ITypeInfo objectType;

	public SubMethodInfo(ReflectionUI reflectionUI, IFieldInfo theField, IMethodInfo theSubMethod,
			ITypeInfo objectType) {
		this.reflectionUI = reflectionUI;
		this.theField = theField;
		this.theSubMethod = theSubMethod;
		this.objectType = objectType;
	}

	public SubMethodInfo(ITypeInfo type, String fieldName, String subMethodSignature) {
		this.theField = ReflectionUIUtils.findInfoByName(type.getFields(), fieldName);
		if (this.theField == null) {
			throw new ReflectionUIError("Field '" + fieldName + "' not found in type '" + type.getName() + "'");
		}
		this.theSubMethod = ReflectionUIUtils.findMethodBySignature(this.theField.getType().getMethods(),
				subMethodSignature);
		if (this.theSubMethod == null) {
			throw new ReflectionUIError("Sub-Method '" + subMethodSignature + "' not found in field type '"
					+ theField.getType().getName() + "'");
		}
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isRelevant(Object object) {
		return true;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
		Object fieldValue = expectTheFieldValue(object);
		theSubMethod.onControlVisibilityChange(fieldValue, visible);
	}

	@Override
	public boolean isEnabled(Object object) {
		Object fieldValue = expectTheFieldValue(object);
		return theSubMethod.isEnabled(fieldValue);
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public ITypeInfo getReturnValueType() {
		if (theSubMethod.getReturnValueType() == null) {
			return null;
		} else {
			return reflectionUI.getTypeInfo(new TypeInfoSourceProxy(theSubMethod.getReturnValueType().getSource()) {
				@Override
				public SpecificitiesIdentifier getSpecificitiesIdentifier() {
					return null;
				}

				@Override
				protected String getTypeInfoProxyFactoryIdentifier() {
					return "MethodReturnValueTypeInfoProxyFactory [of=" + getClass().getName() + ", subMethod="
							+ theSubMethod.getSignature() + ", field=" + theField.getName() + ", objectType="
							+ objectType.getName() + "]";
				}
			});
		}
	}

	@Override
	public String getName() {
		return theField.getName() + "." + theSubMethod.getName();
	}

	@Override
	public String getCaption() {
		return theSubMethod.getCaption();
	}

	@Override
	public String getParametersValidationCustomCaption() {
		return theSubMethod.getParametersValidationCustomCaption();
	}

	@Override
	public String getExecutionSuccessMessage() {
		return theSubMethod.getExecutionSuccessMessage();
	}

	protected Object expectTheFieldValue(Object object) {
		Object result = theField.getValue(object);
		if (result == null) {
			throw new ReflectionUIError("Sub-method error: Parent field value is missing");
		}
		return result;
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		Object fieldValue = expectTheFieldValue(object);
		return theSubMethod.getConfirmationMessage(fieldValue, invocationData);
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, final InvocationData invocationData) {
		Object fieldValue = expectTheFieldValue(object);
		final Runnable theSubMethodUndoJob = theSubMethod.getNextInvocationUndoJob(fieldValue, invocationData);
		if (theSubMethodUndoJob == null) {
			undoJobBuilder = null;
			return null;
		}
		undoJobBuilder = new FutureActionBuilder();
		return undoJobBuilder.will(new FutureActionBuilder.FuturePerformance() {
			@Override
			public void perform(Map<String, Object> options) {
				undoInvocation(object, theSubMethodUndoJob, fieldValue, options);
			}
		});
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object fieldValue = expectTheFieldValue(object);
		Object result = theSubMethod.invoke(fieldValue, invocationData);
		if (isTheFieldUpdatePerformedAfterTheSubMethodInvocation()) {
			if (undoJobBuilder != null) {
				Runnable theFieldUndoJob = ReflectionUIUtils.getNextUpdateCustomOrDefaultUndoJob(object, theField,
						fieldValue);
				undoJobBuilder.setOption("theFieldUndoJob", theFieldUndoJob);
			}
			theField.setValue(object, fieldValue);
		}
		if (undoJobBuilder != null) {
			undoJobBuilder.build();
			undoJobBuilder = null;
		}
		return result;

	}

	@Override
	public Runnable getPreviousInvocationCustomRedoJob(Object object, InvocationData invocationData) {
		Object fieldValue = expectTheFieldValue(object);
		final Runnable theSubMethodRedoJob = ReflectionUIUtils.getPreviousInvocationCustomOrDefaultRedoJob(fieldValue,
				theSubMethod, invocationData);
		redoJobBuilder = new FutureActionBuilder();
		return redoJobBuilder.will(new FutureActionBuilder.FuturePerformance() {
			@Override
			public void perform(Map<String, Object> options) {
				redoInvocation(theSubMethodRedoJob, options);
			}
		});
	}

	protected void undoInvocation(Object object, Runnable theSubMethodUndoJob, Object fieldValue,
			Map<String, Object> undoJobBuilderOptions) {
		theSubMethodUndoJob.run();
		if (isTheFieldUpdatePerformedAfterTheSubMethodInvocation()) {
			if (redoJobBuilder != null) {
				Runnable theFieldRedoJob = ReflectionUIUtils.getPreviousUpdateCustomOrDefaultRedoJob(object, theField,
						fieldValue);
				redoJobBuilder.setOption("theFieldRedoJob", theFieldRedoJob);
			}
			Runnable theFieldUndoJob = (Runnable) undoJobBuilderOptions.get("theFieldUndoJob");
			theFieldUndoJob.run();
		}
		if (redoJobBuilder != null) {
			redoJobBuilder.build();
			redoJobBuilder = null;
		}
	}

	protected void redoInvocation(Runnable theSubMethodRedoJob, Map<String, Object> redoJobBuilderOptions) {
		theSubMethodRedoJob.run();
		if (isTheFieldUpdatePerformedAfterTheSubMethodInvocation()) {
			Runnable theFieldRedoJob = (Runnable) redoJobBuilderOptions.get("theFieldRedoJob");
			theFieldRedoJob.run();
		}
	}

	protected boolean isTheFieldUpdatePerformedAfterTheSubMethodInvocation() {
		if (isReadOnly()) {
			return false;
		}
		return !theField.isGetOnly();
	}

	@Override
	public boolean isReadOnly() {
		if (theField.getValueReturnMode() == ValueReturnMode.CALCULATED) {
			if (theField.isGetOnly()) {
				return true;
			}
		}
		return theSubMethod.isReadOnly();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.combine(theField.getValueReturnMode(), theSubMethod.getValueReturnMode());
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return theSubMethod.isNullReturnValueDistinct();
	}

	@Override
	public String getNullReturnValueLabel() {
		return theSubMethod.getNullReturnValueLabel();
	}

	@Override
	public InfoCategory getCategory() {
		return theField.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return theSubMethod.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return theSubMethod.getSpecificProperties();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		for (IParameterInfo param : theSubMethod.getParameters()) {
			result.add(new ParameterInfoProxy(param) {

				@Override
				public Object getDefaultValue(Object object) {
					Object fieldValue = expectTheFieldValue(object);
					return super.getDefaultValue(fieldValue);
				}

				@Override
				public boolean hasValueOptions(Object object) {
					Object fieldValue = expectTheFieldValue(object);
					return super.hasValueOptions(fieldValue);
				}

				@Override
				public Object[] getValueOptions(Object object) {
					Object fieldValue = expectTheFieldValue(object);
					return super.getValueOptions(fieldValue);
				}

			});
		}
		return result;
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		Object fieldValue = expectTheFieldValue(object);
		theSubMethod.validateParameters(fieldValue, invocationData);
	}

	@Override
	public boolean isReturnValueValidityDetectionEnabled() {
		return theSubMethod.isReturnValueDetached();
	}

	@Override
	public boolean isReturnValueDetached() {
		return theSubMethod.isReturnValueDetached();
	}

	@Override
	public boolean isReturnValueIgnored() {
		return theSubMethod.isReturnValueIgnored();
	}

	public ResourcePath getIconImagePath() {
		return theSubMethod.getIconImagePath();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((theField == null) ? 0 : theField.hashCode());
		result = prime * result + ((theSubMethod == null) ? 0 : theSubMethod.hashCode());
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
		SubMethodInfo other = (SubMethodInfo) obj;
		if (theField == null) {
			if (other.theField != null)
				return false;
		} else if (!theField.equals(other.theField))
			return false;
		if (theSubMethod == null) {
			if (other.theSubMethod != null)
				return false;
		} else if (!theSubMethod.equals(other.theSubMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubMethodInfo [theField=" + theField + ", theSubMethod=" + theSubMethod + "]";
	}

}
