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

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractControlButton;

public class ModificationStackControls {

	protected ModificationStack modificationStack;
	protected Form form;

	public ModificationStackControls(Form form) {
		super();
		this.form = form;
		this.modificationStack = form.getModificationStack();
	}

	protected JButton createButton(final SwingRenderer swingRenderer, final String label, final Runnable action,
			final Accessor<Boolean> enabled, final Accessor<String> tooltipText) {
		final ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final Object object = form.getObject();
		final ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		final JButton result = new AbstractControlButton() {

			protected static final long serialVersionUID = 1L;
			IModificationListener listener = new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							updateState();
						}
					});
				}
			};
			{
				updateState();
				modificationStack.addListener(listener);
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public Image retrieveBackgroundImage() {
				if (type.getFormButtonBackgroundImagePath() != null) {
					return SwingRendererUtils.loadImageThroughCache(type.getFormButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI));
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath() != null) {
					return SwingRendererUtils.loadImageThroughCache(
							reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI));
				}
				return null;
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (type.getFormButtonBackgroundColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonBackgroundColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonBackgroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveForegroundColor() {
				if (type.getFormButtonForegroundColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonForegroundColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonForegroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonForegroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveBorderColor() {
				if (type.getFormButtonBorderColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonBorderColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBorderColor() != null) {
					return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainButtonBorderColor());
				}
				return null;
			}

			@Override
			public String retrieveCaption() {
				return label;
			}

			@Override
			public String retrieveToolTipText() {
				return tooltipText.get();
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				modificationStack.removeListener(listener);
			}

			protected void updateState() {
				setEnabled(enabled.get());
				SwingRendererUtils.setMultilineToolTipText(this,
						swingRenderer.prepareStringToDisplay(tooltipText.get()));
			}

		};
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swingRenderer.showBusyDialogWhile(result, new Runnable() {

					@Override
					public void run() {
						try {
							action.run();
						} catch (final Throwable t) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									swingRenderer.handleExceptionsFromDisplayedUI(result, t);
								}
							});
						}
					}
				}, "Running: " + tooltipText.get());
			}
		});
		result.setEnabled(enabled.get());
		return result;
	}

	public List<Component> create(SwingRenderer swingRenderer) {
		List<Component> result = new ArrayList<Component>();
		result.add(createUndoButton(swingRenderer));
		result.add(createRedoButton(swingRenderer));
		result.add(createResetButton(swingRenderer));
		return result;
	}

	protected Component createUndoButton(final SwingRenderer swingRenderer) {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				modificationStack.undo();
			}
		};
		Accessor<Boolean> enabled = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return modificationStack.canUndo();
			}
		};
		Accessor<String> tooltipText = new Accessor<String>() {
			@Override
			public String get() {
				IModification nextUndoModif = modificationStack.getNextUndoModification();
				if (nextUndoModif != null) {
					return nextUndoModif.getTitle();
				} else {
					return null;
				}
			}
		};
		JButton button = createButton(swingRenderer, "Undo", action, enabled, tooltipText);
		return button;
	}

	protected Component createRedoButton(final SwingRenderer swingRenderer) {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				modificationStack.redo();
			}
		};
		Accessor<Boolean> enabled = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return modificationStack.canRedo();
			}
		};
		Accessor<String> tooltipText = new Accessor<String>() {
			@Override
			public String get() {
				IModification nextRedoModif = modificationStack.getNextRedoModification();
				if (nextRedoModif != null) {
					return nextRedoModif.getTitle();
				} else {
					return null;
				}
			}
		};
		JButton button = createButton(swingRenderer, "Redo", action, enabled, tooltipText);
		return button;
	}

	protected Component createResetButton(final SwingRenderer swingRenderer) {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				modificationStack.undoAll();
			}
		};
		Accessor<Boolean> enabled = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return modificationStack.canReset();
			}
		};
		Accessor<String> tooltipText = new Accessor<String>() {
			@Override
			public String get() {
				return null;
			}
		};
		JButton button = createButton(swingRenderer, "Reset", action, enabled, tooltipText);
		return button;
	}

}
