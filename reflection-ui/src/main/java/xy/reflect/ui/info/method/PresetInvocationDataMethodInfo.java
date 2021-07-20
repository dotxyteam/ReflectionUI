


package xy.reflect.ui.info.method;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Zero-parameter method proxy that actually holds the parameter values and then
 * allows to execute the base method without requiring these parameter values.
 * 
 * @author olitank
 *
 */
public class PresetInvocationDataMethodInfo extends MethodInfoProxy {

	protected TextualStorage invocationDataStorage;
	protected List<IParameterInfo> parameters;

	public PresetInvocationDataMethodInfo(IMethodInfo base, TextualStorage invocationDataStorage) {
		super(base);
		this.invocationDataStorage = invocationDataStorage;
	}

	public static String buildPresetMethodName(String baseMethodSignature, int index) {
		return "preset" + ((index != -1) ? new DecimalFormat("00").format(index + 1) : "") + "Of-"
				+ ReflectionUIUtils.buildNameFromMethodSignature(baseMethodSignature);
	}

	public static String buildLegacyPresetMethodName(String baseMethodName, int index) {
		return baseMethodName + ".savedInvocation" + index;
	}

	@Override
	public String getName() {
		return buildPresetMethodName(base.getSignature(), -1);
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public List<IParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = new ArrayList<IParameterInfo>(base.getParameters());
			SortedSet<Integer> presetParameterPositions = new TreeSet<Integer>();
			InvocationData presetInvocationData = (InvocationData) invocationDataStorage.load();
			presetParameterPositions.addAll(presetInvocationData.getDefaultParameterValues().keySet());
			presetParameterPositions.addAll(presetInvocationData.getProvidedParameterValues().keySet());
			List<Integer> reversedPresetParameterPositions = new ArrayList<Integer>(presetParameterPositions);
			Collections.reverse(reversedPresetParameterPositions);
			for (int parameterPosition : reversedPresetParameterPositions) {
				parameters.remove(parameterPosition);
			}
		}
		return parameters;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		return super.invoke(object, buildFinalInvocationData(invocationData));
	}

	protected InvocationData buildFinalInvocationData(InvocationData invocationData) {
		InvocationData presetInvocationData = (InvocationData) invocationDataStorage.load();
		InvocationData finalInvocationData = new InvocationData(presetInvocationData);
		finalInvocationData.getProvidedParameterValues().clear();
		finalInvocationData.getDefaultParameterValues().clear();
		for (IParameterInfo param : base.getParameters()) {
			finalInvocationData.getProvidedParameterValues().put(param.getPosition(),
					presetInvocationData.getParameterValue(param.getPosition()));
		}
		for (IParameterInfo param : getParameters()) {
			finalInvocationData.getProvidedParameterValues().put(param.getPosition(),
					invocationData.getParameterValue(param.getPosition()));
		}
		return finalInvocationData;
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return super.getNextInvocationUndoJob(object, buildFinalInvocationData(invocationData));
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((invocationDataStorage == null) ? 0 : invocationDataStorage.hashCode());
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
		PresetInvocationDataMethodInfo other = (PresetInvocationDataMethodInfo) obj;
		if (invocationDataStorage == null) {
			if (other.invocationDataStorage != null)
				return false;
		} else if (!invocationDataStorage.equals(other.invocationDataStorage))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PresetInvocationDataMethod [base=" + base + ", invocationDataStorage=" + invocationDataStorage + "]";
	}

}
