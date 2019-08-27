/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.info.method;

import java.util.Collections;
import java.util.List;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PresetInvocationDataMethodInfo extends MethodInfoProxy {

	protected InvocationData invocationData;

	public PresetInvocationDataMethodInfo(IMethodInfo base, InvocationData invocationData) {
		super(base);
		this.invocationData = invocationData;
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return Collections.emptyList();
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		return super.invoke(object, this.invocationData);
	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
		return super.getNextInvocationUndoJob(object, this.invocationData);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((invocationData == null) ? 0 : invocationData.hashCode());
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
		if (invocationData == null) {
			if (other.invocationData != null)
				return false;
		} else if (!invocationData.equals(other.invocationData))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PresetInvocationDataMethod [base=" + base + ", invocationData=" + invocationData + "]";
	}

}
