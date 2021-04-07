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
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.editor.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;

public class NullableControl extends ControlPanel implements IAdvancedFieldControl {

	protected SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected IFieldControlData data;
	protected JCheckBox nullStatusControl;
	protected Component subControl;
	protected IFieldControlInput input;
	protected ITypeInfo subControlValueType;
	protected AbstractEditorFormBuilder subFormBuilder;
	protected Runnable nullControlActivationAction;

	public NullableControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				IFieldControlData result = super.getControlData();
				result = SwingRendererUtils.handleErrors(swingRenderer, result, NullableControl.this);
				return result;
			}
		};
		this.data = input.getControlData();

		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		nullStatusControl = createNullStatusControl();
		refreshUI(true);
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		refreshNullStatusControl(refreshStructure);
		refreshSubControl(refreshStructure);
		if (!Arrays.asList(getComponents()).contains(subControl) || refreshStructure) {
			removeAll();
			if (isSubControlAlwaysDisplayed()) {
				add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.CENTER), BorderLayout.WEST);
				add(subControl, BorderLayout.CENTER);
				nullStatusControl.setText("");
				((JComponent) subControl).setBorder(
						BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
				{
					if (data.getLabelForegroundColor() != null) {
						((TitledBorder) ((JComponent) subControl).getBorder())
								.setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
					}
					if (data.getBorderColor() != null) {
						((TitledBorder) ((JComponent) subControl).getBorder()).setBorder(
								BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
					}
				}
			} else {
				add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.WEST), BorderLayout.NORTH);
				add(subControl, BorderLayout.CENTER);
				nullStatusControl.setText(swingRenderer.prepareStringToDisplay(data.getCaption()));
				((JComponent) subControl).setBorder(BorderFactory.createTitledBorder(""));
				if (data.getBorderColor() != null) {
					((TitledBorder) ((JComponent) subControl).getBorder()).setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				}
			}
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		return true;
	}

	protected boolean isSubControlAlwaysDisplayed() {
		return data.getNullValueLabel() != null;
	}

	public Component getSubControl() {
		return subControl;
	}

	protected void setNullStatusControlState(boolean b) {
		((JCheckBox) nullStatusControl).setSelected(!b);
	}

	protected boolean getNullStatusControlState() {
		return !((JCheckBox) nullStatusControl).isSelected();
	}

	protected void onNullingControlStateChange() {
		if (getNullStatusControlState()) {
			ReflectionUIUtils.setValueThroughModificationStack(data, null, input.getModificationStack());
		} else {
			nullControlActivationAction.run();
		}
		refreshUI(false);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				requestCustomFocus();
			}
		});
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(subControl, swingRenderer);
	}

	protected void refreshNullStatusControl(boolean refreshStructure) {
		setNullStatusControlState(data.getValue() == null);
		if (refreshStructure) {
			nullStatusControl.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
			nullStatusControl.setEnabled(!data.isGetOnly());
		}
	}

	public void refreshSubControl(boolean refreshStructure) {
		Object value = data.getValue();
		if (value != null) {
			ITypeInfo newValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
			if (newValueType.equals(subControlValueType)) {
				if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
					subFormBuilder.refreshEditorForm((Form) subControl, refreshStructure);
					return;
				}
			}
		}
		if (value == null) {
			subControlValueType = null;
			subControl = createNullControl();
		} else {
			subControlValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
			subControl = createSubForm();
		}
		if (subControl instanceof NullControl) {
			subControl.setVisible(isSubControlAlwaysDisplayed());
		}
	}

	protected JCheckBox createNullStatusControl() {
		JCheckBox result = new JCheckBox();
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullableControl.this, t);
				}
			}
		});
		return result;
	}

	protected Component createNullControl() {
		NullControl result = new NullControl(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(super.getControlData()) {

					@Override
					public String getCaption() {
						return "";
					}

					@Override
					public void setValue(Object value) {
						ReflectionUIUtils.setValueThroughModificationStack(base, value, input.getModificationStack());
					}

				};
			}
		});
		if (!data.isGetOnly()) {
			nullControlActivationAction = result.getActivationAction();
			result.setActivationAction(new Runnable() {
				@Override
				public void run() {
					setNullStatusControlState(false);
					onNullingControlStateChange();
				}
			});
		}
		return result;
	}

	protected Component createSubForm() {
		subFormBuilder = new AbstractEditorFormBuilder() {

			@Override
			public IContext getContext() {
				return input.getContext();
			}

			@Override
			public IContext getSubContext() {
				return new CustomContext("NullableInstance");
			}

			@Override
			public boolean isEncapsulatedFormEmbedded() {
				return data.isFormControlEmbedded();
			}

			@Override
			public boolean isNullValueDistinct() {
				return false;
			}

			@Override
			public boolean canCommitToParent() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createCommittingModification(Object newObjectValue) {
				return new FieldControlDataModification(data, newObjectValue);
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public ValueReturnMode getReturnModeFromParent() {
				return data.getValueReturnMode();
			}

			@Override
			public String getParentModificationTitle() {
				return FieldControlDataModification.getTitle(data.getCaption());
			}

			@Override
			public IInfoFilter getEncapsulatedFormFilter() {
				IInfoFilter result = data.getFormControlFilter();
				if (result == null) {
					result = IInfoFilter.DEFAULT;
				}
				return result;
			}

			@Override
			public ITypeInfoSource getDeclaredNonSpecificTypeInfoSource() {
				return data.getType().getSource();
			}

			@Override
			public ModificationStack getParentModificationStack() {
				return input.getModificationStack();
			}

			@Override
			public Object getInitialValue() {
				return data.getValue();
			}
		};
		Form result = subFormBuilder.createEditorForm(true, false);
		return result;
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
	public void validateSubForm() throws Exception {
		if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
			((Form) subControl).validateForm();
		}
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
		if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
			((Form) subControl).addMenuContributionTo(menuModel);
		}
	}

	public ITypeInfo getSubControlValueType() {
		return subControlValueType;
	}

	@Override
	public String toString() {
		return "NullableControl [data=" + data + "]";
	}
}
