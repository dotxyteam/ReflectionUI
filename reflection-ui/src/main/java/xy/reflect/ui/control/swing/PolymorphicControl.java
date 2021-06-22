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
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.RejectedFieldControlInputException;
import xy.reflect.ui.control.swing.builder.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory.RecursivePolymorphismDetectionException;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that can display values of different types. It uses 2
 * sub-controls: an enumeration control to display possible types, and another
 * dynamic control to display the actual field value. Note that the constructor
 * throws a {@link RejectedFieldControlInputException} if it detects that it is
 * being recreated recursively inside the dynamic control.
 * 
 * @author olitank
 *
 */
public class PolymorphicControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected ITypeInfo polymorphicType;
	protected PolymorphicTypeOptionsFactory typeOptionsFactory;

	protected AbstractEditorFormBuilder typeEnumerationControlBuilder;
	protected AbstractEditorFormBuilder dynamicControlBuilder;
	protected Form dynamicControl;
	protected Form typeEnumerationControl;

	protected ITypeInfo dynamicControlInstanceType;
	protected IFieldControlInput input;
	protected Map<ITypeInfo, Object> subTypeInstanceCache = new HashMap<ITypeInfo, Object>();
	protected Object currentInstance;

	public PolymorphicControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		try {
			this.swingRenderer = swingRenderer;
			this.input = new FieldControlInputProxy(input) {
				@Override
				public IFieldControlData getControlData() {
					IFieldControlData result = super.getControlData();
					result = new ErrorHandlingFieldControlData(result, swingRenderer, PolymorphicControl.this);
					return result;
				}
			};
			this.data = input.getControlData();
			this.polymorphicType = input.getControlData().getType();
			this.typeOptionsFactory = new PolymorphicTypeOptionsFactory(swingRenderer.getReflectionUI(),
					polymorphicType);
			setLayout(new BorderLayout());
			refreshUI(true);
		} catch (RecursivePolymorphismDetectionException e) {
			throw new RejectedFieldControlInputException();
		}
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			if (data.getCaption().length() > 0) {
				setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareMessageToDisplay(data.getCaption())));
				if (data.getLabelForegroundColor() != null) {
					((TitledBorder) getBorder())
							.setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
				}
				if (data.getBorderColor() != null) {
					((TitledBorder) getBorder()).setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				}
			} else {
				setBorder(null);
			}
			subTypeInstanceCache.clear();
		}
		currentInstance = data.getValue();
		refreshTypeEnumerationControl(refreshStructure);
		refreshDynamicControl(refreshStructure);
		return true;
	}

	protected void onSubTypeSelection(ITypeInfo selectedSubType) {
		Object instance;
		if (selectedSubType == null) {
			instance = null;
		} else {
			instance = subTypeInstanceCache.get(selectedSubType);
			if (instance == null) {
				instance = swingRenderer.onTypeInstanciationRequest(PolymorphicControl.this, selectedSubType);
				if (instance == null) {
					return;
				}
			}
		}
		currentInstance = instance;
		refreshDynamicControl(false);
		ReflectionUIUtils.setFieldValueThroughModificationStack(data, currentInstance, input.getModificationStack(),
				ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
	}

	protected Form createTypeEnumerationControl() {
		Accessor<ITypeInfo> currentSubTypeAccessor = new Accessor<ITypeInfo>() {
			@Override
			public ITypeInfo get() {
				return getCurrentSubType();
			}
		};
		Listener<ITypeInfo> subTypeSelectionHandler = new Listener<ITypeInfo>() {
			@Override
			public void handle(final ITypeInfo selectedSubType) {
				try {
					onSubTypeSelection(selectedSubType);
				} catch (Throwable t) {
					swingRenderer.handleObjectException(PolymorphicControl.this, t);
					refreshUI(false);
				}
			}
		};
		typeEnumerationControlBuilder = new TypeEnumerationControlBuilder(swingRenderer, input, typeOptionsFactory,
				currentSubTypeAccessor, subTypeSelectionHandler);
		return typeEnumerationControlBuilder.createEditorForm(true, false);
	}

	protected ITypeInfo getCurrentSubType() {
		Object instance = currentInstance;
		if (instance == null) {
			return null;
		}
		ITypeInfo result = MiscUtils.getFirstKeyFromValue(subTypeInstanceCache, instance);
		if (result == null) {
			result = typeOptionsFactory.guessSubType(instance);
			if (result == null) {
				throw new ReflectionUIError("Failed to find a compatible sub-type for '" + instance
						+ "'. Sub-type options: " + typeOptionsFactory.getTypeOptions());
			}
			subTypeInstanceCache.put(result, instance);
		}
		return result;
	}

	protected void refreshTypeEnumerationControl(boolean refreshStructure) {
		if (typeEnumerationControl != null) {
			typeEnumerationControlBuilder.refreshEditorForm(typeEnumerationControl, refreshStructure);
		} else {
			add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Form createDynamicControl(final ITypeInfo instanceType) {
		Accessor<Object> currentInstanceAccessor = new Accessor<Object>() {
			@Override
			public Object get() {
				return currentInstance;
			}
		};
		dynamicControlBuilder = new DynamicControlBuilder(swingRenderer, input, instanceType, currentInstanceAccessor,
				new Listener<Throwable>() {
					@Override
					public void handle(Throwable t) {
						swingRenderer.handleObjectException(PolymorphicControl.this, t);
					}
				});
		return dynamicControlBuilder.createEditorForm(true, false);
	}

	protected void refreshDynamicControl(boolean refreshStructure) {
		ITypeInfo instanceType = getCurrentSubType();
		if ((dynamicControlInstanceType == null) && (instanceType == null)) {
			return;
		} else if ((dynamicControlInstanceType != null) && (instanceType == null)) {
			remove(dynamicControl);
			dynamicControl = null;
			dynamicControlInstanceType = null;
			SwingRendererUtils.handleComponentSizeChange(this);
		} else if ((dynamicControlInstanceType == null) && (instanceType != null)) {
			dynamicControl = createDynamicControl(instanceType);
			dynamicControlInstanceType = instanceType;
			add(dynamicControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			if (dynamicControlInstanceType.equals(instanceType)) {
				dynamicControlBuilder.refreshEditorForm(dynamicControl, refreshStructure);
			} else {
				remove(dynamicControl);
				dynamicControl = createDynamicControl(instanceType);
				dynamicControlInstanceType = instanceType;
				add(dynamicControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
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
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public boolean requestCustomFocus() {
		if (SwingRendererUtils.requestAnyComponentFocus(typeEnumerationControl, swingRenderer)) {
			return true;
		}
		if (dynamicControl != null) {
			if (SwingRendererUtils.requestAnyComponentFocus(dynamicControl, swingRenderer)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void validateSubForms() throws Exception {
		if (dynamicControl != null) {
			dynamicControl.validateForm();
		}
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
		if (dynamicControl != null) {
			dynamicControl.addMenuContributionTo(menuModel);
		}
	}

	@Override
	public String toString() {
		return "PolymorphicControl [data=" + data + "]";
	}

	protected static class TypeEnumerationControlBuilder extends AbstractEditorFormBuilder {

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected PolymorphicTypeOptionsFactory typeOptionsFactory;
		protected Accessor<ITypeInfo> currentSubTypeAccessor;
		protected Listener<ITypeInfo> subTypeSelectionHandler;

		public TypeEnumerationControlBuilder(SwingRenderer swingRenderer, IFieldControlInput input,
				PolymorphicTypeOptionsFactory typeOptionsFactory, Accessor<ITypeInfo> currentSubTypeAccessor,
				Listener<ITypeInfo> subTypeSelectionHandler) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.typeOptionsFactory = typeOptionsFactory;
			this.currentSubTypeAccessor = currentSubTypeAccessor;
			this.subTypeSelectionHandler = subTypeSelectionHandler;
		}

		@Override
		protected IContext getContext() {
			return input.getContext();
		}

		@Override
		protected IContext getSubContext() {
			return null;
		}

		@Override
		protected boolean isEncapsulatedFormEmbedded() {
			return false;
		}

		@Override
		protected boolean isNullValueDistinct() {
			return data.isNullValueDistinct();
		}

		@Override
		protected Object loadValue() {
			return typeOptionsFactory.getItemInstance(currentSubTypeAccessor.get());
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			return typeOptionsFactory.getInstanceTypeInfoSource(null);
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return ValueReturnMode.CALCULATED;
		}

		@Override
		protected boolean canCommitToParent() {
			return !data.isGetOnly();
		}

		@Override
		protected boolean shouldIntegrateNewObjectValue(Object value) {
			return true;
		}

		@Override
		protected IModification createCommittingModification(final Object value) {
			ITypeInfo selectedSubType = (value == null) ? null : (ITypeInfo) typeOptionsFactory.getInstanceItem(value);
			subTypeSelectionHandler.handle(selectedSubType);
			return IModification.NULL_MODIFICATION;
		}

		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			throw new ReflectionUIError();
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected String getParentModificationTitle() {
			return FieldControlDataModification.getTitle(data.getCaption());
		}

		@Override
		protected boolean isParentModificationFake() {
			return data.isTransient();
		}

		@Override
		protected IInfoFilter getEncapsulatedFormFilter() {
			return IInfoFilter.DEFAULT;
		}

		@Override
		protected ModificationStack getParentModificationStack() {
			return input.getModificationStack();
		}

	}

	protected static class DynamicControlBuilder extends AbstractEditorFormBuilder {

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected ITypeInfo instanceType;
		protected Accessor<Object> currentInstanceAccessor;
		protected Listener<Throwable> commitExceptionHandler;

		public DynamicControlBuilder(SwingRenderer swingRenderer, IFieldControlInput input, ITypeInfo instanceType,
				Accessor<Object> currentInstanceAccessor, Listener<Throwable> commitExceptionHandler) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.instanceType = instanceType;
			this.currentInstanceAccessor = currentInstanceAccessor;
			this.commitExceptionHandler = commitExceptionHandler;
		}

		@Override
		protected IContext getContext() {
			return input.getContext();
		}

		@Override
		protected IContext getSubContext() {
			return new CustomContext("PolymorphicInstance");
		}

		@Override
		protected boolean isEncapsulatedFormEmbedded() {
			return data.isFormControlEmbedded();
		}

		@Override
		protected boolean isNullValueDistinct() {
			return false;
		}

		@Override
		protected boolean canCommitToParent() {
			return !data.isGetOnly();
		}

		@Override
		protected IModification createCommittingModification(Object newObjectValue) {
			return new FieldControlDataModification(data, newObjectValue);
		}

		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			commitExceptionHandler.handle(t);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return data.getValueReturnMode();
		}

		@Override
		protected String getParentModificationTitle() {
			return FieldControlDataModification.getTitle(data.getCaption());
		}

		@Override
		protected boolean isParentModificationFake() {
			return data.isTransient();
		}

		@Override
		protected IInfoFilter getEncapsulatedFormFilter() {
			IInfoFilter result = data.getFormControlFilter();
			if (result == null) {
				result = IInfoFilter.DEFAULT;
			}
			return result;
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			return instanceType.getSource();
		}

		@Override
		protected ModificationStack getParentModificationStack() {
			return input.getModificationStack();
		}

		@Override
		protected Object loadValue() {
			return currentInstanceAccessor.get();
		}

	}

}
