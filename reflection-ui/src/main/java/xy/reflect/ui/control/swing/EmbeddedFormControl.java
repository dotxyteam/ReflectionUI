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
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;

public class EmbeddedFormControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected Component textControl;
	protected Component iconControl;
	protected Object subFormObject;
	protected Form subForm;
	protected IFieldControlInput input;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				IFieldControlData result = super.getControlData();
				result = SwingRendererUtils.handleErrors(swingRenderer, result, EmbeddedFormControl.this);
				return result;
			}
		};
		this.data = retrieveData();

		setLayout(new BorderLayout());
		refreshUI(true);
	}

	protected IFieldControlData retrieveData() {
		return input.getControlData();
	}

	public Form getSubForm() {
		return subForm;
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(subForm, swingRenderer);
	}

	protected void forwardSubFormModifications() {
		if (!ReflectionUIUtils.canEditSeparateObjectValue(
				ReflectionUIUtils.isValueImmutable(swingRenderer.getReflectionUI(), subFormObject),
				data.getValueReturnMode(), !data.isGetOnly())) {
			ModificationStack childModifStack = subForm.getModificationStack();
			childModifStack.addListener(new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					refreshUI(false);
				}
			});
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = Accessor.returning(data.getValueReturnMode());
			Accessor<Boolean> childValueReplacedGetter = Accessor.returning(Boolean.FALSE);
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (data.isGetOnly()) {
						return null;
					}
					return new FieldControlDataModification(data, subFormObject);
				}
			};
			Accessor<String> childModifTitleGetter = new Accessor<String>() {
				@Override
				public String get() {
					return FieldControlDataModification.getTitle(data.getCaption());
				}
			};
			Accessor<ModificationStack> masterModifStackGetter = new Accessor<ModificationStack>() {
				@Override
				public ModificationStack get() {
					return input.getModificationStack();
				}
			};
			boolean exclusiveLinkWithParent = Boolean.TRUE.equals(input.getControlData().getSpecificProperties()
					.get(EncapsulatedObjectFactory.IS_ENCAPSULATION_FIELD_PROPERTY_KEY));
			subForm.setModificationStack(new SlaveModificationStack(swingRenderer, subForm, childModifAcceptedGetter,
					childValueReturnModeGetter, childValueReplacedGetter, commitModifGetter, childModifTitleGetter,
					masterModifStackGetter, exclusiveLinkWithParent));
		}
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			if (data.getCaption().length() > 0) {
				setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
				if (data.getLabelForegroundColor() != null) {
					((TitledBorder) getBorder()).setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
				}
				if (data.getBorderColor() != null) {
					((TitledBorder) getBorder()).setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				}
			} else {
				setBorder(null);
			}
		}
		if (subForm == null) {
			subFormObject = data.getValue();
			if (subFormObject == null) {
				throw new ReflectionUIError();
			}
			IInfoFilter filter = data.getFormControlFilter();
			subForm = swingRenderer.createForm(subFormObject, filter);
			add(subForm, BorderLayout.CENTER);
			forwardSubFormModifications();
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			final Object newSubFormObject = data.getValue();
			if (newSubFormObject == null) {
				throw new ReflectionUIError();
			}
			if (newSubFormObject == subFormObject) {
				subForm.refresh(refreshStructure);
			} else {
				final ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
				final ITypeInfo newSubFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newSubFormObject));
				if (subFormObjectType.equals(newSubFormObjectType)) {
					subForm.setObject(newSubFormObject);
					swingRenderer.showBusyDialogWhile(this, new Runnable() {
						@Override
						public void run() {
							subFormObjectType.onFormVisibilityChange(subFormObject, false);
							newSubFormObjectType.onFormVisibilityChange(newSubFormObject, true);
						}
					}, "Refreshing " + swingRenderer.getObjectTitle(newSubFormObject) + "...");
					subFormObject = newSubFormObject;
					subForm.refresh(refreshStructure);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public void validateSubForm() throws Exception {
		subForm.validateForm();
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
		subForm.addMenuContribution(menuModel);
	}

	@Override
	public String toString() {
		return "EmbeddedFormControl [data=" + data + "]";
	}

}
