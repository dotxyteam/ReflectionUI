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

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.undo.ModificationStack;

public class DefaultFieldControlInput implements IFieldControlInput {

	private ReflectionUI reflectionUI;
	private Object object;
	private IFieldInfo field;

	public DefaultFieldControlInput(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
	}

	public DefaultFieldControlInput(ReflectionUI reflectionUI) {
		this(reflectionUI, null, IFieldInfo.NULL_FIELD_INFO);
	}

	@Override
	public ModificationStack getModificationStack() {
		return new ModificationStack(null);
	}

	@Override
	public IFieldControlData getControlData() {
		return new DefaultFieldControlData(reflectionUI, object, field);
	}

	@Override
	public IContext getContext() {
		return IContext.NULL_CONTEXT;
	}

}
