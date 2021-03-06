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
package xy.reflect.ui.control.swing.editor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

/**
 * This is a base class for form-based editor control factories.
 * 
 * Each instance of this class handles a target value/object according to the
 * specifications provided through the implementation of the various class
 * methods.
 * 
 * Note that the target value/object is encapsulated in a virtual parent object
 * for practical reasons. The editor control is thus a form representing this
 * capsule.
 * 
 * This class also handles the complex relationship that may exists between the
 * target value/object and a potential parent object. The parent object form
 * will typically embed (real-time link) or maintain a detached parent-child
 * relationship (e.g.: child dialog) with the target value/object form.
 * 
 * A real-time link to the parent object can be exclusive or not, meaning that
 * the target value/object is the only child of its parent object. This
 * information is used when forwarding target value/object modifications to the
 * parent modification stack. Typically, calling
 * {@link ModificationStack#forget()} on the target value/object modification
 * stack will trigger the same call on the parent modification stack only and
 * only if only the link is exclusive. Otherwise
 * {@link ModificationStack#invalidate()} will be called instead.
 * 
 * @author olitank
 *
 */
public abstract class AbstractEditorFormBuilder {

	protected Object initialObjectValue;
	protected boolean objectValueInitialized = false;
	protected boolean objectValueReplaced = false;
	protected Accessor<Object> encapsulatedObjectValueAccessor;

	/**
	 * @return the renderer used to create the target value/object form(s).
	 */
	public abstract SwingRenderer getSwingRenderer();

	/**
	 * @return the parent object modification stack or null (if there is no parent
	 *         object).
	 */
	public abstract ModificationStack getParentModificationStack();

	/**
	 * @return the title or the title prefix (in case of multiple modifications) of
	 *         the modification(s) that will be communicated to the parent object
	 *         modification stack or null (if there is no parent object).
	 */
	public abstract String getParentModificationTitle();

	/**
	 * @return whether modifications of the target value/object can be committed
	 *         (using the result of {@link #createParentCommitModification(Object)})
	 *         to make them real for the parent object.
	 */
	public abstract boolean canCommitToParent();

	/**
	 * @param newObjectValue
	 * @return a modification that will be applied in order to make any target
	 *         value/object modification real for the parent object. Typically
	 *         primitive field values would need to be committed (set back) to their
	 *         parent object after modification.
	 */
	public abstract IModification createParentCommitModification(Object newObjectValue);

	/**
	 * @return source of the declared type information that will be used to handle
	 *         the target value/object. If null is returned then this type
	 *         information source will be dynamically inferred from the target
	 *         value/object. Note that calling
	 *         {@link ITypeInfoSource#getSpecificitiesIdentifier()} on the result
	 *         should return null.
	 */
	public abstract ITypeInfoSource getDeclaredNonSpecificTypeInfoSource();

	/**
	 * @return the return mode (from the parent object) of the target value/object
	 *         (null can be returned if there is no parent object).
	 */
	public abstract ValueReturnMode getReturnModeFromParent();

	/**
	 * @return true if and only if the null value can be set and then must be
	 *         distinctly displayed.
	 */
	public abstract boolean isNullValueDistinct();

	/**
	 * @return the initial target object/value.
	 */
	public abstract Object getInitialValue();

	/**
	 * @return an object that will be used to uniquely name the capsule type (or
	 *         null).
	 */
	public abstract IContext getContext();

	/**
	 * @return an secondary object that will be used to uniquely name the capsule
	 *         type (or null).
	 */
	public abstract IContext getSubContext();

	/**
	 * Ensures that the initial target value/object has been acquired.
	 */
	protected void ensureIsInitialized() {
		if (objectValueInitialized) {
			return;
		}
		encapsulatedObjectValueAccessor = new Accessor<Object>() {

			Object object = initialObjectValue = getInitialValue();

			@Override
			public Object get() {
				return object;
			}

			@Override
			public void set(Object t) {
				object = t;
				objectValueReplaced = true;
			}

		};
		objectValueInitialized = true;
	}

	/**
	 * @return whether the initial target value/object has been acquired.
	 */
	public boolean isInitialized() {
		return objectValueInitialized;
	}

	/**
	 * @return whether the target object/value has been replaced during the lifetime
	 *         of the editor control. Typically immutable objects like primitive
	 *         wrappers would be replaced on every modification.
	 */
	public boolean isValueReplaced() {
		return objectValueReplaced;
	}

	/**
	 * @return whether the target object/value can be replaced during the lifetime
	 *         of the editor control.
	 */
	public boolean canReplaceValue() {
		return canCommitToParent();
	}

	/**
	 * @return the current target object/value.
	 */
	public Object getCurrentValue() {
		ensureIsInitialized();
		return encapsulatedObjectValueAccessor.get();
	}

	/**
	 * @return the capsule holding the target value/object.
	 */
	public Object getCapsule() {
		ensureIsInitialized();
		return getEncapsulation().getInstance(encapsulatedObjectValueAccessor);
	}

	/**
	 * @return the name of the capsule type.
	 */
	public String getCapsuleTypeName() {
		String contextDeclaraion;
		{
			IContext context = getContext();
			if (context == null) {
				contextDeclaraion = "";
			} else {
				contextDeclaraion = "context=" + context.getIdentifier() + ", ";
			}
		}
		String subContextDeclaraion;
		{
			IContext subContext = getSubContext();
			if (subContext == null) {
				subContextDeclaraion = "";
			} else {
				subContextDeclaraion = "subContext=" + subContext.getIdentifier() + ", ";
			}
		}
		return "Encapsulation [" + contextDeclaraion + subContextDeclaraion + "encapsulatedObjectType="
				+ getSwingRenderer().getReflectionUI().getTypeInfo(getEncapsulatedFieldNonSpecificTypeSource())
						.getName()
				+ "]";
	}

	/**
	 * @return the target value/object capsule factory.
	 */
	public EncapsulatedObjectFactory getEncapsulation() {
		ITypeInfo fieldType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getEncapsulatedFieldNonSpecificTypeSource());
		EncapsulatedObjectFactory result = new EncapsulatedObjectFactory(getSwingRenderer().getReflectionUI(),
				getCapsuleTypeName(), fieldType) {

			@Override
			protected boolean hasFieldValueOptions() {
				return hasEncapsulatedFieldValueOptions();
			}

			@Override
			protected Object[] getFieldValueOptions() {
				return getEncapsulatedFieldValueOptions();
			}

		};
		result.setTypeModificationStackAccessible(isCapsuleTypeModificationStackAccessible());
		result.setTypeCaption(getCapsuleTypeCaption());
		Map<String, Object> typeSpecificProperties = new HashMap<String, Object>();
		{
			result.setTypeSpecificProperties(typeSpecificProperties);
		}
		result.setFieldName(getEncapsulatedFieldName());
		result.setFieldCaption(getEncapsulatedFieldCaption());
		result.setFieldGetOnly(isEncapsulatedFieldGetOnly());
		result.setFieldNullValueDistinct(isNullValueDistinct());
		result.setFieldValueReturnMode(getEncapsulatedFieldValueReturnMode());
		result.setFieldFormControlEmbedded(isEncapsulatedFormEmbedded());
		result.setFieldFormControlFilter(getEncapsulatedFormFilter());
		result.setFieldFormControlMandatory(isCustomEncapsulatedControlForbidden());
		result.setFieldSpecificProperties(getEncapsulatedFieldSpecificProperties());
		return result;
	}

	/**
	 * @return a form filter that will used (in case the target value/object is
	 *         represented by a generic form control).
	 */
	public abstract IInfoFilter getEncapsulatedFormFilter();

	/**
	 * @return whether the target object/value form is embedded in the editor
	 *         control or displayed in a child dialog. Note that this method has no
	 *         impact in case the target value/object is not represented by a
	 *         generic form control.
	 */
	public abstract boolean isEncapsulatedFormEmbedded();

	/**
	 * @return true if the target value/object is forcibly displayed as a generic
	 *         form (not a custom control).
	 */
	public boolean isCustomEncapsulatedControlForbidden() {
		return false;
	}

	/**
	 * @return the name of the encapsulated field that will return target
	 *         value/object.
	 */
	protected String getEncapsulatedFieldName() {
		return "";
	}

	/**
	 * @return the specific properties of the encapsulated field information.
	 */
	protected Map<String, Object> getEncapsulatedFieldSpecificProperties() {
		return Collections.emptyMap();
	}

	/**
	 * @return the encapsulated field value return mode (equals to
	 *         {@link #getReturnModeFromParent()} is there is a parent object).
	 */
	protected ValueReturnMode getEncapsulatedFieldValueReturnMode() {
		return hasParentObject() ? getReturnModeFromParent() : ValueReturnMode.DIRECT_OR_PROXY;
	}

	/**
	 * @return whether the encapsulated field value options.
	 */
	protected boolean hasEncapsulatedFieldValueOptions() {
		return false;
	}

	/**
	 * @return the encapsulated field value options.
	 */
	protected Object[] getEncapsulatedFieldValueOptions() {
		return null;
	}

	/**
	 * @return whether the encapsulated field is get-only.
	 */
	protected boolean isEncapsulatedFieldGetOnly() {
		return isInReadOnlyMode() || !canReplaceValue();
	}

	/**
	 * @return true if and only if the undo/redo/etc features should be made
	 *         available typically when the capsule is the root of a window.
	 */
	protected boolean isCapsuleTypeModificationStackAccessible() {
		return !isInReadOnlyMode();
	}

	public String getEncapsulatedFieldCaption() {
		return "";
	}

	public ITypeInfoSource getEncapsulatedFieldNonSpecificTypeSource() {
		ITypeInfoSource result = getDeclaredNonSpecificTypeInfoSource();
		if (result != null) {
			return result;
		}
		ensureIsInitialized();
		if (initialObjectValue != null) {
			return getSwingRenderer().getReflectionUI().getTypeInfoSource(initialObjectValue);
		}
		return new JavaTypeInfoSource(Object.class, null);
	}

	public String getCapsuleTypeCaption() {
		return getSwingRenderer().getReflectionUI().getTypeInfo(getEncapsulatedFieldNonSpecificTypeSource())
				.getCaption();
	}

	/**
	 * @return whether the editor control is refreshed every time a modification of
	 *         the target value/object is detected. It typically allows to keep a
	 *         calculated read-only target value/object coherent by resetting it to
	 *         its initial state whenever its temporary value is modified.
	 */
	protected boolean isEditorFormRefreshedOnModification() {
		return isInReadOnlyMode();
	}

	/**
	 * @return whether the target value/object modifications have no impact on the
	 *         parent object. If there is no parent object then false should be
	 *         returned.
	 */
	protected boolean isInReadOnlyMode() {
		return hasParentObject() ? !canPotentiallyModifyParentObject() : false;
	}

	/**
	 * @return whether the editor control is expected to be empty or not.
	 */
	public boolean isFormEmpty() {
		Object capsule = getCapsule();
		ITypeInfo capsuleType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(capsule));
		IFieldInfo encapsulatedField = capsuleType.getFields().get(0);
		if (encapsulatedField.isNullValueDistinct()) {
			return false;
		}
		Object object = getCurrentValue();
		if (object == null) {
			return false;
		}
		ITypeInfo actualObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(object));
		if (!SwingRendererUtils.isFormEmpty(actualObjectType, getEncapsulatedFormFilter(), getSwingRenderer())) {
			return false;
		}
		return true;
	}

	/**
	 * Creates and return the editor control.
	 * 
	 * @param realTimeLinkWithParent
	 *            Whether a real-time link should be maintained with the parent
	 *            object.
	 * @param exclusiveLinkWithParent
	 *            Whether the real-time link with the parent object (if existing) is
	 *            exclusive or not.
	 * @return the created editor control.
	 */
	public Form createEditorForm(boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
		Object encapsulated = getCapsule();
		Form result = getSwingRenderer().createForm(encapsulated);
		setupLinkWithParent(result, realTimeLinkWithParent, exclusiveLinkWithParent);
		return result;
	}

	/**
	 * Installs the link between the target value/object editor control and its
	 * parent object form.
	 * 
	 * @param editorForm
	 *            The created target value/object editor control.
	 * @param realTimeLinkWithParent
	 *            Whether a real-time link should be maintained with the parent
	 *            object.
	 * @param exclusiveLinkWithParent
	 *            Whether the real-time link with the parent object (if existing) is
	 *            exclusive or not.
	 */
	protected void setupLinkWithParent(Form editorForm, boolean realTimeLinkWithParent,
			boolean exclusiveLinkWithParent) {
		if (realTimeLinkWithParent) {
			if (canPotentiallyModifyParentObject()) {
				forwardEditorFormModificationsToParentObject(editorForm, exclusiveLinkWithParent);
			}
			if (isEditorFormRefreshedOnModification()) {
				refreshEditorFormOnModification(editorForm);
			}
		}
	}

	/**
	 * @return whether the target value/object has a parent object or not.
	 */
	protected boolean hasParentObject() {
		return getParentModificationStack() != null;
	}

	/**
	 * @return whether modifications of the target value/object can impact the
	 *         parent object.
	 */
	public boolean canPotentiallyModifyParentObject() {
		if (!hasParentObject()) {
			return false;
		}
		ensureIsInitialized();
		return ReflectionUIUtils.canEditSeparateObjectValue(
				ReflectionUIUtils.isValueImmutable(getSwingRenderer().getReflectionUI(), initialObjectValue),
				getReturnModeFromParent(), canCommitToParent());
	}

	/**
	 * Installs a listener that will trigger the editor control refreshing whenever
	 * a modification of the target value/object is detected.
	 * 
	 * @param editorForm
	 *            The created editor control.
	 */
	protected void refreshEditorFormOnModification(final Form editorForm) {
		ModificationStack childModificationStack = editorForm.getModificationStack();
		childModificationStack.addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				refreshEditorForm(editorForm, false);
			}
		});
	}

	/**
	 * Refreshes the editor control.
	 * 
	 * @param editorForm
	 *            The created editor control.
	 * @param refreshStructure
	 *            Whether the editor control should update its structure to reflect
	 *            the recent meta-data change. Mainly used in design mode.
	 */
	public void refreshEditorForm(Form editorForm, boolean refreshStructure) {
		encapsulatedObjectValueAccessor.set(getInitialValue());
		editorForm.refresh(refreshStructure);
	}

	/**
	 * @param value
	 *            The new target value.
	 * @return whether the new target value passed as argument was accepted or
	 *         rejected, typically by a user.
	 */
	protected boolean shouldAcceptNewObjectValue(Object value) {
		return true;
	}

	/**
	 * Installs on the editor control a listener that will forward the target
	 * value/object modifications to the parent object modification stack.
	 * 
	 * @param editorForm
	 *            The created editor control.
	 * @param exclusiveLinkWithParent
	 *            Whether the real-time link with the parent object (if existing) is
	 *            exclusive or not.
	 */
	protected void forwardEditorFormModificationsToParentObject(final Form editorForm,
			boolean exclusiveLinkWithParent) {
		Accessor<Boolean> childModifAcceptedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return shouldAcceptNewObjectValue(getCurrentValue());
			}
		};
		Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
			@Override
			public ValueReturnMode get() {
				return getReturnModeFromParent();
			}
		};
		Accessor<Boolean> childValueReplacedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return (isValueReplaced());
			}
		};
		Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				if (!canCommitToParent()) {
					return null;
				}
				return createParentCommitModification(getCurrentValue());
			}
		};
		Accessor<String> childModifTitleGetter = new Accessor<String>() {
			@Override
			public String get() {
				return getParentModificationTitle();
			}
		};
		Accessor<ModificationStack> masterModifStackGetter = new Accessor<ModificationStack>() {

			@Override
			public ModificationStack get() {
				ModificationStack result = getParentModificationStack();
				if (result == null) {
					throw new ReflectionUIError();
				}
				return result;
			}
		};
		editorForm.setModificationStack(new SlaveModificationStack(getSwingRenderer(), editorForm,
				childModifAcceptedGetter, childValueReturnModeGetter, childValueReplacedGetter, commitModifGetter,
				childModifTitleGetter, masterModifStackGetter, exclusiveLinkWithParent));
	}

}
