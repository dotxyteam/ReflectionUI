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
package xy.reflect.ui.control.swing.builder;

import java.awt.Component;

import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This is a standard editor window factory class.
 * 
 * @author olitank
 *
 */
public class StandardEditorBuilder extends AbstractEditorBuilder {

	protected SwingRenderer swingRenderer;
	protected Component ownerComponent;
	protected Object rootObject;

	/**
	 * Constructs a standard editor window builder.
	 * 
	 * @param swingRenderer  The renderer object used to generate the controls.
	 * @param ownerComponent the component that will own the editor dialog.
	 * @param rootObject     The local object that will be viewed/modified by the
	 *                       editor window.
	 */
	public StandardEditorBuilder(SwingRenderer swingRenderer, Component ownerComponent, Object rootObject) {
		this.swingRenderer = swingRenderer;
		this.ownerComponent = ownerComponent;
		this.rootObject = rootObject;
		if (rootObject == null) {
			throw new ReflectionUIError();
		}
	}

	public Object getRootObject() {
		return rootObject;
	}

	@Override
	protected IContext getContext() {
		return null;
	}

	@Override
	protected IContext getSubContext() {
		return null;
	}

	@Override
	protected boolean isDialogCancellable() {
		return false;
	}

	@Override
	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	@Override
	protected Component getOwnerComponent() {
		return ownerComponent;
	}

	@Override
	protected ModificationStack getParentModificationStack() {
		return null;
	}

	@Override
	protected String getParentModificationTitle() {
		return null;
	}

	@Override
	protected boolean isParentModificationFake() {
		return false;
	}

	@Override
	protected boolean canCommitToParent() {
		return false;
	}

	@Override
	protected IModification createCommittingModification(Object newObjectValue) {
		return null;
	}

	@Override
	protected void handleRealtimeLinkCommitException(Throwable t) {
		throw new ReflectionUIError();
	}

	@Override
	protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
		return null;
	}

	@Override
	protected ValueReturnMode getReturnModeFromParent() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	@Override
	protected boolean isNullValueDistinct() {
		return false;
	}

	@Override
	protected Object loadValue() {
		return rootObject;
	}

	@Override
	protected IInfoFilter getEncapsulatedFormFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	protected boolean isEncapsulatedFormEmbedded() {
		return true;
	}

}
