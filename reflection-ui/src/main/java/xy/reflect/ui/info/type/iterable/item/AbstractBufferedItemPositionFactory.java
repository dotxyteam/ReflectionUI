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
package xy.reflect.ui.info.type.iterable.item;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a sub-class of {@link AbstractItemPositionFactory} that only
 * creates {@link BufferedItemPosition} instances.
 * 
 * @author olitank
 *
 */
public abstract class AbstractBufferedItemPositionFactory extends AbstractItemPositionFactory {

	protected Object[] bufferedRootListRawValue;
	protected Object bufferedRootListValue;
	protected Map<Integer, BufferedItemPosition> bufferedRootItemPositionByIndex = new HashMap<Integer, BufferedItemPosition>();

	protected abstract Object getNonBufferedRootListValue();

	protected abstract void setNonBufferedRootListValue(Object rootListValue);

	@Override
	public BufferedItemPosition getRootItemPosition(int index) {
		if (bufferedRootItemPositionByIndex.containsKey(index)) {
			return bufferedRootItemPositionByIndex.get(index);
		}
		BufferedItemPosition result = (BufferedItemPosition) super.getRootItemPosition(index);
		bufferedRootItemPositionByIndex.put(index, result);
		return result;
	}

	@Override
	protected BufferedItemPosition createItemPosition() {
		return new BufferedItemPosition();
	}

	@Override
	public Object getRootListValue() {
		if (bufferedRootListValue == null) {
			bufferedRootListValue = getNonBufferedRootListValue();
		}
		return bufferedRootListValue;
	}

	@Override
	public Object[] getRootListRawValue() {
		if (bufferedRootListRawValue == null) {
			bufferedRootListRawValue = super.getRootListRawValue();
		}
		return bufferedRootListRawValue;
	}

	@Override
	public void setRootListValue(Object rootListValue) {
		setNonBufferedRootListValue(rootListValue);
		refresh();
	}

	/**
	 * Updates the root list buffer so that root list items will have up-to-date
	 * values.
	 */
	public void refresh() {
		bufferedRootListValue = null;
		bufferedRootListRawValue = null;
		bufferedRootItemPositionByIndex.clear();
	}

	/**
	 * Updates all the buffers of all item positions created (directly or
	 * indirectly) by this factory so that all items will have up-to-date values.
	 */
	public void refreshAll() {
		for (int index : bufferedRootItemPositionByIndex.keySet()) {
			BufferedItemPosition bufferedRootItemPosition = bufferedRootItemPositionByIndex.get(index);
			if (bufferedRootItemPosition == null) {
				continue;
			}
			bufferedRootItemPosition.refreshBranch();
		}
		refresh();
	}

}
