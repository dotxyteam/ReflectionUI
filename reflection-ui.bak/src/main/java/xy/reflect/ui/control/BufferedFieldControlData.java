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
package xy.reflect.ui.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BufferedFieldControlData extends FieldControlDataProxy {

	protected List<Object> buffer = new ArrayList<Object>();

	public BufferedFieldControlData(IFieldControlData base, Object values) {
		super(base);
		buffer.addAll(Arrays.asList(values));
	}

	@Override
	public Object getValue() {
		if (buffer.size() > 0) {
			Object nextValue = buffer.remove(0);
			return nextValue;
		}
		return super.getValue();
	}

	@Override
	public void setValue(Object value) {
		buffer.clear();
		super.setValue(value);
	}

}
