/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
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
	protected InvocationData presetInvocationData;

	public PresetInvocationDataMethodInfo(IMethodInfo base, TextualStorage invocationDataStorage) {
		super(base);
		this.invocationDataStorage = (TextualStorage) ReflectionUIUtils.copy(ReflectionUIUtils.STANDARD_REFLECTION,
				invocationDataStorage);
		this.presetInvocationData = (InvocationData) invocationDataStorage.load();
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
		List<IParameterInfo> result = new ArrayList<IParameterInfo>(base.getParameters());
		SortedSet<Integer> presetParameterPositions = new TreeSet<Integer>();
		presetParameterPositions.addAll(presetInvocationData.getDefaultParameterValues().keySet());
		presetParameterPositions.addAll(presetInvocationData.getProvidedParameterValues().keySet());
		List<Integer> reversedPresetParameterPositions = new ArrayList<Integer>(presetParameterPositions);
		Collections.reverse(reversedPresetParameterPositions);
		for (int parameterPosition : reversedPresetParameterPositions) {
			result.remove(parameterPosition);
		}
		return result;
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		return super.invoke(object, buildFinalInvocationData(invocationData));
	}

	protected InvocationData buildFinalInvocationData(InvocationData invocationData) {
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
		return "PresetInvocationDataMethod [base=" + base + ", invocationData=" + presetInvocationData + "]";
	}

}
