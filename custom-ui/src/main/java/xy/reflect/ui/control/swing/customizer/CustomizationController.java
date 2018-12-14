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
package xy.reflect.ui.control.swing.customizer;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class CustomizationController {

	protected SwingCustomizer swingCustomizer;
	protected Set<Form> activeForms = new HashSet<Form>();
	protected JFrame window;
	protected Rectangle lastWindowBounds;
	protected StandardEditorBuilder windowBuilder;

	public CustomizationController(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
	}

	protected synchronized void formAdded(Form form) {
		if (activeForms.size() == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					openWindow();
				}
			});
		}
		activeForms.add(form);
	}

	protected synchronized void formRemoved(Form customizingComponent) {
		activeForms.remove(customizingComponent);
		if (activeForms.size() == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					closeWindow();
				}
			});
		}

	}

	protected void openWindow() {
		windowBuilder = createWindowBuilder();
		window = windowBuilder.createFrame();
		refreshCustomizedControlsOnModification();
		if (lastWindowBounds != null) {
			window.setBounds(lastWindowBounds);
		}
		window.setVisible(true);
	}

	protected void closeWindow() {
		lastWindowBounds = window.getBounds();
		window.dispose();
		window = null;
		windowBuilder = null;
	}

	protected StandardEditorBuilder createWindowBuilder() {
		SwingRenderer customizationsToolsRenderer = swingCustomizer.getCustomizationTools().getToolsRenderer();
		return new StandardEditorBuilder(customizationsToolsRenderer, null, this);
	}

	protected void refreshCustomizedControlsOnModification() {
		getModificationStack().addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingRendererUtils.refreshAllDisplayedFormsAndMenus(swingCustomizer, true);
					}
				});

			}
		});
	}

	public ModificationStack getModificationStack() {
		if (windowBuilder == null) {
			return null;
		}
		return windowBuilder.getModificationStack();
	}

	public String getInfoCustomizationsOutputFilePath() {
		return swingCustomizer.getInfoCustomizationsOutputFilePath();
	}

	public void saveCustomizations() {
		File file = new File(swingCustomizer.getInfoCustomizationsOutputFilePath());
		try {
			swingCustomizer.getInfoCustomizations().saveToFile(file,
					ReflectionUIUtils.getDebugLogListener(swingCustomizer.getReflectionUI()),
					"Generated with Custom UI (http://otksoftware.com/custom-ui)");
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
	}

	public boolean isInEditMode() {
		return swingCustomizer.getCustomizationOptions().isInEditMode();
	}

	public void setInEditMode(boolean inEditMode) {
		swingCustomizer.getCustomizationOptions().setInEditMode(inEditMode);
	}

	public InfoCustomizations getAllCustomizations() {
		return swingCustomizer.getInfoCustomizations();
	}

}
