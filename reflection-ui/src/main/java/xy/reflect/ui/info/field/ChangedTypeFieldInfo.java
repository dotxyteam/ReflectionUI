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
package xy.reflect.ui.info.field;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.AbstractPollingService;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.ReflectionUIError;

public class ChangedTypeFieldInfo extends FieldInfoProxy {

	protected ITypeInfo newType;
	protected Filter<Object> conversionMethod;
	protected Filter<Object> reverseConversionMethod;
	protected boolean nullValueConverted;
	protected long reverseSynchronizationPeriodMilliseconds;

	protected static Map<Object, Map<ChangedTypeFieldInfo, ReverseSynchronizer>> reverseSynchronizers = CacheBuilder
			.newBuilder().weakKeys().<Object, Map<ChangedTypeFieldInfo, ReverseSynchronizer>>build().asMap();

	public ChangedTypeFieldInfo(IFieldInfo base, ITypeInfo newType, Filter<Object> conversionMethod,
			Filter<Object> reverseConversionMethod, boolean nullValueConverted,
			long reverseSynchronizationPeriodMilliseconds) {
		super(base);
		this.newType = newType;
		this.conversionMethod = conversionMethod;
		this.reverseConversionMethod = reverseConversionMethod;
		this.nullValueConverted = nullValueConverted;
		this.reverseSynchronizationPeriodMilliseconds = reverseSynchronizationPeriodMilliseconds;
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
		ReverseSynchronizer reverseSynchronizer = getReverseSynchronizer(object, this);
		Object value;
		synchronized (reverseSynchronizer.getSynchronizationMutex()) {
			value = super.getValue(object);
		}
		Object result = convert(value);
		reverseSynchronizer.setLastValue(result);
		reverseSynchronizer.setReverseSynchronizationPeriodMilliseconds(reverseSynchronizationPeriodMilliseconds);
		return result;
	}

	@Override
	public void setValue(Object object, Object value) {
		ReverseSynchronizer reverseSynchronizer = getReverseSynchronizer(object, this);
		synchronized (reverseSynchronizer.getSynchronizationMutex()) {
			super_setValue(object, revertConversion(value));
		}
		reverseSynchronizer.setLastValue(value);
	}

	protected void super_setValue(Object object, Object value) {
		super.setValue(object, value);
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
		ReverseSynchronizer reverseSynchronizer = getReverseSynchronizer(object, this);
		reverseSynchronizer.setControlVisible(visible);
	}

	protected static synchronized ReverseSynchronizer getReverseSynchronizer(Object object,
			ChangedTypeFieldInfo field) {
		Map<ChangedTypeFieldInfo, ReverseSynchronizer> byField = reverseSynchronizers.get(object);
		if (byField == null) {
			byField = new HashMap<ChangedTypeFieldInfo, ChangedTypeFieldInfo.ReverseSynchronizer>();
			reverseSynchronizers.put(object, byField);
		}
		ReverseSynchronizer result = byField.get(field);
		if (result == null) {
			result = new ReverseSynchronizer(object, field);
			byField.put(field, result);
		}
		return result;
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

	protected static class ReverseSynchronizer extends AbstractPollingService {

		protected Object object;
		protected ChangedTypeFieldInfo field;
		protected Object lastValue;
		protected boolean controlVisible = false;
		protected long reverseSynchronizationPeriodMilliseconds = -1;
		protected Object synchronizationMutex = new Object();

		public ReverseSynchronizer(Object object, ChangedTypeFieldInfo field) {
			super();
			this.object = object;
			this.field = field;
		}

		public Object getSynchronizationMutex() {
			return synchronizationMutex;
		}

		@Override
		protected String getServiceName() {
			return field.getName() + " Reverse Synchronizer";
		}

		protected void updateState() {
			if ((reverseSynchronizationPeriodMilliseconds >= 0) && controlVisible) {
				start();
			} else {
				stop();
			}
		}

		public Object getObject() {
			return object;
		}

		public long getReverseSynchronizationPeriodMilliseconds() {
			return reverseSynchronizationPeriodMilliseconds;
		}

		public void setReverseSynchronizationPeriodMilliseconds(long reverseSynchronizationPeriodMilliseconds) {
			this.reverseSynchronizationPeriodMilliseconds = reverseSynchronizationPeriodMilliseconds;
			updateState();
		}

		public Object getLastValue() {
			return lastValue;
		}

		public void setLastValue(Object lastValue) {
			this.lastValue = lastValue;
			updateState();
		}

		public boolean isControlVisible() {
			return controlVisible;
		}

		public void setControlVisible(boolean controlVisible) {
			this.controlVisible = controlVisible;
			updateState();
		}

		@Override
		protected long getPeriodicSleepDurationMilliseconds() {
			return field.reverseSynchronizationPeriodMilliseconds;
		}

		@Override
		protected boolean doPollingAction() {
			synchronized (synchronizationMutex) {
				field.super_setValue(object, field.revertConversion(lastValue));
			}
			return true;
		}

	}
}
