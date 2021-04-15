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

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

/**
 * This is a base class for editor window factories.
 * 
 * @author olitank
 *
 */
public abstract class AbstractEditorWindowBuilder extends AbstractEditorFormBuilder {

	protected DialogBuilder dialogBuilder;
	protected Form createdEditorForm;
	protected JFrame createdFrame;
	protected boolean parentModificationStackImpacted = false;

	/**
	 * @return the owner component of the editor dialog or null.
	 */
	public abstract Component getOwnerComponent();

	/**
	 * @return whether the editing session should be cancellable (a cancel button
	 *         will be displayed) or not. Note that it only makes sense if a editor
	 *         dialog (not a frame) is created.
	 */
	public boolean isCancellable() {
		Object encapsualted = getCapsule();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsualted));
		return encapsulatedObjectType.isModificationStackAccessible();
	}

	/**
	 * @return the title of the editor window.
	 */
	public String getEditorWindowTitle() {
		Object encapsulatedObject = getCapsule();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsulatedObject));
		return encapsulatedObjectType.getCaption();
	}

	/**
	 * @return the icon image of the editor window.
	 */
	public Image getEditorWindowIconImage() {
		ensureIsInitialized();
		Image result = getSwingRenderer().getObjectIconImage(initialObjectValue);
		if (result == null) {
			ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
			IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
			if (appInfo.getIconImagePath() != null) {
				result = SwingRendererUtils.loadImageThroughCache(appInfo.getIconImagePath(),
						ReflectionUIUtils.getErrorLogListener(reflectionUI));
			}
		}
		return result;
	}

	/**
	 * @return the text of the 'Cancel' button.
	 */
	public String getCancelCaption() {
		return "Cancel";
	}

	/**
	 * @return the text of the 'OK' button.
	 */
	public String getOKCaption() {
		return "OK";
	}

	/**
	 * @return the text of the 'Close' button.
	 */
	public String getCloseCaption() {
		return "Close";
	}

	/**
	 * @return additional controls that will be laid on the button bar.
	 */
	public List<Component> getAdditionalButtonBarControls() {
		return Collections.emptyList();
	}

	/**
	 * @return common controls that will be laid on the button bar.
	 */
	protected List<Component> createCommonButtonBarControls() {
		List<Component> result = new ArrayList<Component>();
		List<Component> commonButtonBarControls = createdEditorForm.createButtonBarControls();
		if (commonButtonBarControls != null) {
			result.addAll(commonButtonBarControls);
		}
		List<Component> additionalButtonBarComponents = getAdditionalButtonBarControls();
		if (additionalButtonBarComponents != null) {
			result.addAll(additionalButtonBarComponents);
		}
		return result;
	}

	/**
	 * Creates and returns the editor frame.
	 * 
	 * @return the created editor frame.
	 */
	public JFrame createFrame() {
		createdEditorForm = createEditorForm(false, false);
		createdFrame = new JFrame();
		WindowManager windowManager = getSwingRenderer().createWindowManager(createdFrame);
		windowManager.set(createdEditorForm, new ArrayList<Component>(createCommonButtonBarControls()),
				getEditorWindowTitle(), getEditorWindowIconImage());
		createdFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return createdFrame;
	}

	/**
	 * @return the created editor frame.
	 */
	public JFrame getCreatedFrame() {
		return createdFrame;
	}

	/**
	 * Creates and shows the editor frame.
	 */
	public void createAndShowFrame() {
		getSwingRenderer().showFrame(createFrame());
	}

	/**
	 * @return the dialog builder used to build the editor dialog.
	 */
	protected DialogBuilder createDelegateDialogBuilder() {
		DialogBuilder dialogBuilder = getSwingRenderer().getDialogBuilder(getOwnerComponent());
		ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
		Object object = getCapsule();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));

		if (type.getFormButtonBackgroundColor() != null) {
			dialogBuilder.setButtonBackgroundColor(SwingRendererUtils.getColor(type.getFormButtonBackgroundColor()));
		}

		if (type.getFormButtonForegroundColor() != null) {
			dialogBuilder.setButtonForegroundColor(SwingRendererUtils.getColor(type.getFormButtonForegroundColor()));
		}

		if (type.getFormButtonBorderColor() != null) {
			dialogBuilder.setButtonBorderColor(SwingRendererUtils.getColor(type.getFormButtonBorderColor()));
		}

		if (type.getFormButtonBackgroundImagePath() != null) {
			dialogBuilder.setButtonBackgroundImage(SwingRendererUtils.loadImageThroughCache(
					type.getFormButtonBackgroundImagePath(), ReflectionUIUtils.getErrorLogListener(reflectionUI)));
		}
		return dialogBuilder;
	}

	/**
	 * Creates and returns the editor dialog.
	 * 
	 * @return the created editor dialog.
	 */
	public JDialog createDialog() {
		createdEditorForm = createEditorForm(false, false);
		dialogBuilder = createDelegateDialogBuilder();
		dialogBuilder.setContentComponent(createdEditorForm);
		dialogBuilder.setTitle(getEditorWindowTitle());
		dialogBuilder.setIconImage(getEditorWindowIconImage());

		List<Component> buttonBarControls = new ArrayList<Component>(createCommonButtonBarControls());
		{
			if (isCancellable()) {
				List<JButton> okCancelButtons = dialogBuilder.createStandardOKCancelDialogButtons(getOKCaption(),
						getCancelCaption());
				buttonBarControls.addAll(okCancelButtons);
			} else {
				buttonBarControls.add(dialogBuilder.createDialogClosingButton(getCloseCaption(), null));
			}
			dialogBuilder.setButtonBarControls(buttonBarControls);
		}
		return dialogBuilder.createDialog();
	}

	/**
	 * @return the created editor dialog.
	 */
	public JDialog getCreatedDialog() {
		if (dialogBuilder == null) {
			return null;
		}
		return dialogBuilder.getCreatedDialog();
	}

	/**
	 * Creates and shows the editor dialog.
	 */
	public void createAndShowDialog() {
		getSwingRenderer().showDialog(createDialog(), true);
		getSwingRenderer().showBusyDialogWhile(getOwnerComponent(), new Runnable() {
			@Override
			public void run() {
				if (hasParentObject()) {
					if (mayModifyParentObject()) {
						impactParent();
					}
				} else {
					if (isCancelled()) {
						ModificationStack modifStack = getModificationStack();
						modifStack.undoAll();
						if (modifStack.wasInvalidated()) {
							getSwingRenderer().getReflectionUI().logDebug(
									"WARNING: Cannot undo completely invalidated modification stack: " + modifStack);
						}
					}
				}
			}
		}, getParentModificationTitle());
	}

	/**
	 * @return the created editor form.
	 */
	public Form getCreatedEditorForm() {
		return createdEditorForm;
	}

	/**
	 * Update the parent object and its modification stack according to the target
	 * value/object modifications and the current editor builder specifications.
	 */
	public void impactParent() {
		ModificationStack parentObjectModifStack = getParentModificationStack();
		if (parentObjectModifStack == null) {
			return;
		}
		ModificationStack valueModifStack = getModificationStack();
		ValueReturnMode valueReturnMode = getReturnModeFromParent();
		Object currentValue = getCurrentValue();
		boolean valueReplaced = isValueReplaced();
		IModification committingModif;
		if (!canCommitToParent()) {
			committingModif = null;
		} else {
			committingModif = createCommittingModification(currentValue);
		}
		boolean valueModifAccepted = shouldAcceptNewObjectValue(currentValue) && ((!isCancellable()) || !isCancelled());
		String parentObjectModifTitle = getParentModificationTitle();
		parentModificationStackImpacted = ReflectionUIUtils.finalizeSubModifications(parentObjectModifStack,
				valueModifStack, valueModifAccepted, valueReturnMode, valueReplaced, committingModif,
				parentObjectModifTitle, ReflectionUIUtils.getDebugLogListener(getSwingRenderer().getReflectionUI()));
	}

	/**
	 * @return whether the user cancelled the editor dialog.
	 */
	public boolean isCancelled() {
		if (dialogBuilder == null) {
			return false;
		}
		return !dialogBuilder.wasOkPressed();
	}

	/**
	 * @return the modification stack of the target value/object.
	 */
	public ModificationStack getModificationStack() {
		if (createdEditorForm == null) {
			return null;
		}
		return createdEditorForm.getModificationStack();
	}

	/**
	 * @return whether a potential modification of the parent object is detected.
	 */
	public boolean isParentModificationStackImpacted() {
		return parentModificationStackImpacted;
	}

}
