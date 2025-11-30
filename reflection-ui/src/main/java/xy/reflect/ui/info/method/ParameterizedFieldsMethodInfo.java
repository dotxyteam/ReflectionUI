
package xy.reflect.ui.info.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.FieldAsParameterInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IrreversibleModificationException;
import xy.reflect.ui.util.FutureActionBuilder;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Method proxy that have additional virtual parameters allowing view/update the
 * values of specified fields.
 * 
 * @author olitank
 *
 */
public class ParameterizedFieldsMethodInfo extends MethodInfoProxy {

	protected ReflectionUI reflectionUI;
	protected List<IFieldInfo> parameterizedFields;
	protected ITypeInfo objectType;
	protected FutureActionBuilder undoJobBuilder;
	protected FutureActionBuilder redoJobBuilder;
	protected List<FieldAsParameterInfo> generatedParameters;

	public ParameterizedFieldsMethodInfo(ReflectionUI reflectionUI, IMethodInfo method,
			List<IFieldInfo> parameterizedFields, ITypeInfo objectType) {
		super(method);
		this.reflectionUI = reflectionUI;
		this.parameterizedFields = parameterizedFields;
		this.objectType = objectType;
		this.generatedParameters = generateParameters();
	}

	protected List<FieldAsParameterInfo> generateParameters() {
		List<FieldAsParameterInfo> result = new ArrayList<FieldAsParameterInfo>();
		int startPosition = super.getParameters().size();
		for (int i = 0; i < parameterizedFields.size(); i++) {
			final IFieldInfo field = parameterizedFields.get(i);
			final int position = i + startPosition;
			result.add(new FieldAsParameterInfo(reflectionUI, field, position) {

				@Override
				public String getName() {
					return "field." + field.getName();
				}
			});
		}
		return result;
	}

	@Override
	public List<IParameterInfo> getParameters() {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>(super.getParameters());
		result.addAll(generatedParameters);
		return result;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public String getCaption() {
		String result = super.getCaption();
		if ((super.getParameters().size() == 0) && (generatedParameters.size() > 0) && !result.endsWith("...")) {
			result += "...";
		}
		return result;
	}

	@Override
	public boolean isReadOnly() {
		if (!super.isReadOnly()) {
			return false;
		}
		return true;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		undoJobBuilder = new FutureActionBuilder();
		return undoJobBuilder.will(new FutureActionBuilder.FuturePerformance() {
			@Override
			public void perform(Map<String, Object> options) {
				undoInvocation(object, invocationData, options);
			}
		});
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		InvocationData newInvocationData = new InvocationData(invocationData);
		for (FieldAsParameterInfo generatedParameter : generatedParameters) {
			Object value = invocationData.getParameterValue(generatedParameter.getPosition());
			if (undoJobBuilder != null) {
				undoJobBuilder.setOption(getUndoJobName(generatedParameter), ReflectionUIUtils
						.getNextUpdateCustomOrDefaultUndoJob(object, generatedParameter.getSourceField(), value));
			}
			generatedParameter.getSourceField().setValue(object, value);
			newInvocationData.getProvidedParameterValues().remove(generatedParameter.getPosition());
			newInvocationData.getDefaultParameterValues().remove(generatedParameter.getPosition());
		}
		Runnable baseMethodUndoJob = super.isReadOnly() ? new Runnable() {
			@Override
			public void run() {
			}
		} : super.getNextInvocationUndoJob(object, invocationData);
		Object result = super.invoke(object, newInvocationData);
		if (undoJobBuilder != null) {
			undoJobBuilder.setOption(getBaseMethodUndoJobName(), baseMethodUndoJob);
			undoJobBuilder.build();
			undoJobBuilder = null;
		}
		return result;
	}

	@Override
	public Runnable getPreviousInvocationCustomRedoJob(Object object, InvocationData invocationData) {
		redoJobBuilder = new FutureActionBuilder();
		return redoJobBuilder.will(new FutureActionBuilder.FuturePerformance() {
			@Override
			public void perform(Map<String, Object> options) {
				redoInvocation(options);
			}
		});
	}

	protected void undoInvocation(Object object, InvocationData invocationData,
			Map<String, Object> undoJobBuilderOptions) {
		Runnable baseMethodUndoJob = (Runnable) undoJobBuilderOptions.get(getBaseMethodUndoJobName());
		if (baseMethodUndoJob == null) {
			throw new IrreversibleModificationException();
		}
		Runnable baseMethodRedoJob;
		{
			if (super.isReadOnly()) {
				baseMethodRedoJob = new Runnable() {
					@Override
					public void run() {
					}
				};
			} else {
				baseMethodRedoJob = super.getPreviousInvocationCustomRedoJob(object, invocationData);
				if (baseMethodRedoJob == null) {
					baseMethodRedoJob = new Runnable() {
						@Override
						public void run() {
							ParameterizedFieldsMethodInfo.super.invoke(object, invocationData);
						}
					};
				}
			}
		}
		baseMethodUndoJob.run();
		for (int i = generatedParameters.size() - 1; i >= 0; i--) {
			FieldAsParameterInfo generatedParameter = generatedParameters.get(i);
			Runnable fieldUndoJob = (Runnable) undoJobBuilderOptions.get(getUndoJobName(generatedParameter));
			Object value = invocationData.getParameterValue(generatedParameter.getPosition());
			if (redoJobBuilder != null) {
				redoJobBuilder.setOption(getRedoJobName(generatedParameter), ReflectionUIUtils
						.getPreviousUpdateCustomOrDefaultRedoJob(object, generatedParameter.getSourceField(), value));
			}
			fieldUndoJob.run();
		}
		if (redoJobBuilder != null) {
			redoJobBuilder.setOption(getBaseMethodRedoJobName(), baseMethodRedoJob);
			redoJobBuilder.build();
			redoJobBuilder = null;
		}
	}

	protected void redoInvocation(Map<String, Object> redoJobBuilderOptions) {
		Runnable baseMethodRedoJob = (Runnable) redoJobBuilderOptions.get(getBaseMethodRedoJobName());
		if (baseMethodRedoJob == null) {
			throw new IrreversibleModificationException();
		}
		for (int i = generatedParameters.size() - 1; i >= 0; i--) {
			FieldAsParameterInfo generatedParameter = generatedParameters.get(i);
			Runnable fieldRedoJob = (Runnable) redoJobBuilderOptions.get(getRedoJobName(generatedParameter));
			fieldRedoJob.run();
		}
		baseMethodRedoJob.run();
	}

	protected String getBaseMethodUndoJobName() {
		return "baseMethodUndoJob";
	}

	protected String getBaseMethodRedoJobName() {
		return "baseMethodRedoJob";
	}

	protected String getUndoJobName(FieldAsParameterInfo generatedParameter) {
		return "field" + generatedParameter.getPosition() + "UndoJob";
	}

	protected String getRedoJobName(FieldAsParameterInfo generatedParameter) {
		return "field" + generatedParameter.getPosition() + "RedoJob";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
		result = prime * result + ((parameterizedFields == null) ? 0 : parameterizedFields.hashCode());
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
		ParameterizedFieldsMethodInfo other = (ParameterizedFieldsMethodInfo) obj;
		if (objectType == null) {
			if (other.objectType != null)
				return false;
		} else if (!objectType.equals(other.objectType))
			return false;
		if (parameterizedFields == null) {
			if (other.parameterizedFields != null)
				return false;
		} else if (!parameterizedFields.equals(other.parameterizedFields))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParameterizedFieldsMethodInfo [parameterizedFields=" + parameterizedFields + ", objectType="
				+ objectType + ", base=" + base + "]";
	}

}
