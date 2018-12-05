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
package xy.reflect.ui.control.swing.editor;

import java.awt.Component;

import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;

public class StandardEditorBuilder extends AbstractEditorBuilder {

	protected SwingRenderer swingRenderer;
	protected Component ownerComponent;
	protected Object rootObject;
	protected ITypeInfo rootObjectType;

	public StandardEditorBuilder(SwingRenderer swingRenderer, Component ownerComponent, Object rootObject) {
		this.swingRenderer = swingRenderer;
		this.ownerComponent = ownerComponent;
		this.rootObject = rootObject;
		if (rootObject == null) {
			throw new ReflectionUIError();
		}
		this.rootObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(rootObject));
	}

	@Override
	public IContext getContext() {
		return null;
	}

	@Override
	public IContext getSubContext() {
		return null;
	}

	@Override
	public boolean isCancellable() {
		return false;
	}

	@Override
	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	@Override
	public Component getOwnerComponent() {
		return ownerComponent;
	}

	@Override
	public ModificationStack getParentObjectModificationStack() {
		return null;
	}

	@Override
	public String getCumulatedModificationsTitle() {
		return null;
	}

	@Override
	public boolean canCommit() {
		return false;
	}

	@Override
	public IModification createCommitModification(Object newObjectValue) {
		return null;
	}

	@Override
	public ITypeInfoSource getObjectDeclaredNonSpecificTypeInfoSource() {
		return null;
	}

	@Override
	public ValueReturnMode getObjectValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	public boolean isObjectNullValueDistinct() {
		return false;
	}

	@Override
	public Object getInitialObjectValue() {
		return rootObject;
	}

	@Override
	public IInfoFilter getObjectFormFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public boolean isObjectFormExpanded() {
		return true;
	}

}
