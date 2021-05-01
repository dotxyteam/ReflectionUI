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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.editor.AbstractEditorWindowBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays a button allowing to open an editor for the field
 * value.
 * 
 * @author olitank
 *
 */
public class DialogAccessControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected Component statusControl;
	protected Component iconControl;
	protected Component actionControl;
	protected IFieldControlInput input;

	public DialogAccessControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				IFieldControlData result = super.getControlData();
				result = SwingRendererUtils.handleErrors(swingRenderer, result, DialogAccessControl.this);
				return result;
			}
		};
		this.data = input.getControlData();

		setLayout(new GridBagLayout());
		statusControl = createStatusControl(input);
		actionControl = createActionControl();
		iconControl = createIconControl();

		if (actionControl != null) {
			GridBagConstraints c = new GridBagConstraints();
			add(SwingRendererUtils.flowInLayout(actionControl, GridBagConstraints.CENTER), c);
		}
		if (statusControl != null) {
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(statusControl, c);
		}
		if (iconControl != null) {
			GridBagConstraints c = new GridBagConstraints();
			add(SwingRendererUtils.flowInLayout(iconControl, GridBagConstraints.CENTER), c);
		}

		refreshUI(true);
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (statusControl != null) {
			updateStatusControl(refreshStructure);
		}
		if (iconControl != null) {
			updateIconControl(refreshStructure);
		}
		if (actionControl != null) {
			updateActionControl();
		}
		return true;
	}

	protected Component createIconControl() {
		return new JLabel();
	}

	protected Component createActionControl() {
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public Image retrieveBackgroundImage() {
				if (data.getButtonBackgroundImagePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadImageThroughCache(data.getButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
				}
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (data.getButtonBackgroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBackgroundColor());
				}
			}

			@Override
			public Color retrieveForegroundColor() {
				if (data.getButtonForegroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonForegroundColor());
				}
			}

			@Override
			public Color retrieveBorderColor() {
				if (data.getButtonBorderColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBorderColor());
				}
			}

			@Override
			public String retrieveCaption() {
				return "...";
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				result.width = result.height;
				return result;
			}

		};
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					openDialog(DialogAccessControl.this);
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(result, t);
				}
			}
		});
		return result;
	}

	protected Component createStatusControl(IFieldControlInput input) {
		return new TextControl(swingRenderer, new FieldControlInputProxy(input) {

			@Override
			public IFieldControlData getControlData() {
				return new DefaultFieldControlData(swingRenderer.getReflectionUI()) {

					@Override
					public Object getValue() {
						Object fieldValue = DialogAccessControl.this.data.getValue();
						return ReflectionUIUtils.toString(swingRenderer.getReflectionUI(), fieldValue);
					}

					@Override
					public ITypeInfo getType() {
						return new DefaultTypeInfo(swingRenderer.getReflectionUI(),
								new JavaTypeInfoSource(String.class, null));
					}

				};
			}
		});
	}

	protected void openDialog(Component owner) {
		AbstractEditorWindowBuilder subDialogBuilder = getSubDialogBuilder(owner);
		subDialogBuilder.createAndShowDialog();
		if (subDialogBuilder.isParentModificationStackImpacted()) {
			refreshUI(false);
		}
	}

	protected AbstractEditorWindowBuilder getSubDialogBuilder(final Component owner) {
		return new AbstractEditorWindowBuilder() {

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
				return true;
			}

			@Override
			protected boolean isNullValueDistinct() {
				return false;
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
			protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
				return data.getType().getSource();
			}

			@Override
			protected Object getInitialValue() {
				return data.getValue();
			}

			@Override
			protected String getParentModificationTitle() {
				return FieldControlDataModification.getTitle(data.getCaption());
			}

			@Override
			protected ModificationStack getParentModificationStack() {
				return input.getModificationStack();
			}

			@Override
			protected Component getOwnerComponent() {
				return owner;
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
			protected IInfoFilter getEncapsulatedFormFilter() {
				return data.getFormControlFilter();
			}
		};
	}

	protected void updateActionControl() {
		((AbstractControlButton) actionControl).updateStyle();
	}

	protected void updateIconControl(boolean refreshStructure) {
		ImageIcon icon = SwingRendererUtils.getObjectIcon(swingRenderer, data.getValue());
		if (icon != null) {
			icon = SwingRendererUtils.getSmallIcon(icon);
		}
		((JLabel) iconControl).setIcon(icon);
		iconControl.setVisible(((JLabel) iconControl).getIcon() != null);
	}

	protected void updateStatusControl(boolean refreshStructure) {
		((TextControl) statusControl).refreshUI(refreshStructure);
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean showsCaption() {
		return false;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public boolean requestCustomFocus() {
		if (data.isGetOnly()) {
			return false;
		}
		if (SwingRendererUtils.requestAnyComponentFocus(statusControl, swingRenderer)) {
			return true;
		}
		if (SwingRendererUtils.requestAnyComponentFocus(actionControl, swingRenderer)) {
			return true;
		}
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "DialogAccessControl [data=" + data + "]";
	}

}
