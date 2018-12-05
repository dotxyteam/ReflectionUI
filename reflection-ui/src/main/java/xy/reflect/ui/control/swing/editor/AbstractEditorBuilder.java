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
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.WindowManager;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class AbstractEditorBuilder extends AbstractEditorFormBuilder {

	protected DialogBuilder dialogBuilder;
	protected Form createdEditorForm;
	protected JFrame createdFrame;
	protected boolean parentModificationStackImpacted = false;

	public abstract Component getOwnerComponent();

	public boolean isCancellable() {
		Object encapsualted = getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsualted));
		return encapsulatedObjectType.isModificationStackAccessible();
	}

	public String getEditorWindowTitle() {
		Object encapsulatedObject = getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(encapsulatedObject));
		return encapsulatedObjectType.getCaption();
	}

	public Image getEditorWindowIconImage() {
		ensureObjectValueIsInitialized();
		Image result = getSwingRenderer().getObjectIconImage(initialObjectValue);
		if (result == null) {
			ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
			IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
			if (appInfo.getIconImagePath() != null) {
				result = SwingRendererUtils.loadImageThroughcache(appInfo.getIconImagePath(),
						ReflectionUIUtils.getErrorLogListener(reflectionUI));
			}
		}
		return result;
	}

	public String getCancelCaption() {
		return "Cancel";
	}

	public String getOKCaption() {
		return "OK";
	}

	public String getCloseCaption() {
		return "Close";
	}

	public List<Component> getAdditionalToolbarComponents() {
		return Collections.emptyList();
	}

	protected List<Component> createAnyWindowToolbarControls() {
		List<Component> result = new ArrayList<Component>();
		List<Component> commonToolbarControls = createdEditorForm.createToolbarControls();
		if (commonToolbarControls != null) {
			result.addAll(commonToolbarControls);
		}
		List<Component> additionalToolbarComponents = getAdditionalToolbarComponents();
		if (additionalToolbarComponents != null) {
			result.addAll(additionalToolbarComponents);
		}
		return result;
	}

	public JFrame createFrame() {
		createdEditorForm = createForm(false, false);
		createdFrame = new JFrame();
		WindowManager windowManager = getSwingRenderer().createWindowManager(createdFrame);
		windowManager.set(createdEditorForm, new Accessor<List<Component>>() {
			@Override
			public List<Component> get() {
				return new ArrayList<Component>(createAnyWindowToolbarControls());
			}
		}, getEditorWindowTitle(), getEditorWindowIconImage());
		createdFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return createdFrame;
	}

	public JFrame getCreatedFrame() {
		return createdFrame;
	}

	public void createAndShowFrame() {
		getSwingRenderer().showFrame(createFrame());
	}

	protected DialogBuilder createDelegateDialogBuilder() {
		return getSwingRenderer().getDialogBuilder(getOwnerComponent());
	}

	public JDialog createDialog() {
		createdEditorForm = createForm(false, false);
		dialogBuilder = createDelegateDialogBuilder();
		dialogBuilder.setContentComponent(createdEditorForm);
		dialogBuilder.setTitle(getEditorWindowTitle());
		dialogBuilder.setIconImage(getEditorWindowIconImage());

		dialogBuilder.setToolbarComponentsAccessor(new Accessor<List<Component>>() {
			@Override
			public List<Component> get() {
				List<Component> toolbarControls = new ArrayList<Component>(createAnyWindowToolbarControls());
				if (isCancellable()) {
					List<JButton> okCancelButtons = dialogBuilder.createStandardOKCancelDialogButtons(getOKCaption(),
							getCancelCaption());
					toolbarControls.addAll(okCancelButtons);
				} else {
					toolbarControls.add(dialogBuilder.createDialogClosingButton(getCloseCaption(), null));
				}
				return toolbarControls;
			}
		});
		return dialogBuilder.createDialog();
	}

	public JDialog getCreatedDialog() {
		if (dialogBuilder == null) {
			return null;
		}
		return dialogBuilder.getCreatedDialog();
	}

	public void createAndShowDialog() {
		getSwingRenderer().showDialog(createDialog(), true);
		getSwingRenderer().showBusyDialogWhile(getOwnerComponent(), new Runnable() {
			@Override
			public void run() {
				if (hasParentObject()) {
					if (canPotentiallyModifyParentObject()) {
						impactParent();
					}
				} else {
					if (isCancelled()) {
						ModificationStack modifStack = getObjectModificationStack();
						modifStack.undoAll();
						if (modifStack.wasInvalidated()) {
							getSwingRenderer().getReflectionUI().logDebug(
									"WARNING: Cannot undo completely invalidated modification stack: " + modifStack);
						}
					}
				}
			}
		}, getCumulatedModificationsTitle());
	}

	public Form getCreatedEditorForm() {
		return createdEditorForm;
	}

	public void impactParent() {
		ModificationStack parentObjectModifStack = getParentObjectModificationStack();
		if (parentObjectModifStack == null) {
			return;
		}
		ModificationStack valueModifStack = getObjectModificationStack();
		ValueReturnMode valueReturnMode = getObjectValueReturnMode();
		Object currentValue = getCurrentObjectValue();
		boolean valueReplaced = isObjectValueReplaced();
		IModification commitModif;
		if (!canCommit()) {
			commitModif = null;
		} else {
			commitModif = createCommitModification(currentValue);
		}
		boolean valueModifAccepted = shouldAcceptNewObjectValue(currentValue) && ((!isCancellable()) || !isCancelled());
		String editSessionTitle = getCumulatedModificationsTitle();
		parentModificationStackImpacted = ReflectionUIUtils.finalizeSeparateObjectValueEditSession(
				parentObjectModifStack, valueModifStack, valueModifAccepted, valueReturnMode, valueReplaced,
				commitModif, editSessionTitle,
				ReflectionUIUtils.getDebugLogListener(getSwingRenderer().getReflectionUI()));
	}

	public boolean isCancelled() {
		if (dialogBuilder == null) {
			return false;
		}
		return !dialogBuilder.wasOkPressed();
	}

	public ModificationStack getObjectModificationStack() {
		if (createdEditorForm == null) {
			return null;
		}
		return createdEditorForm.getModificationStack();
	}

	public boolean isParentModificationStackImpacted() {
		return parentModificationStackImpacted;
	}

}
