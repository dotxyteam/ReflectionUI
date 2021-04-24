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
package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.control.swing.util.WrapLayout;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control plugin that displays toggle buttons allowing to choose an
 * enumeration item.
 * 
 * @author olitank
 *
 */
public class OptionButtonsPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Option Buttons";
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		throw new ReflectionUIError();
	}

	@Override
	public boolean handles(IFieldControlInput input) {
		if (!(input.getControlData().getType() instanceof IEnumerationTypeInfo)) {
			return false;
		}
		return true;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new OptionButtonsConfiguration();
	}

	@Override
	public OptionButtons createControl(Object renderer, IFieldControlInput input) {
		return new OptionButtons((SwingRenderer) renderer, input);
	}

	public static class OptionButtonsConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public OptionButtonsLayout layout = OptionButtonsLayout.HORIZONTAL_FLOW;
		public OptionButtonType buttonType = OptionButtonType.RADIO;
	}

	public enum OptionButtonsLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

	public enum OptionButtonType {
		RADIO, TOGGLE
	}

	public class OptionButtons extends ControlPanel implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected boolean listenerDisabled = false;
		protected ButtonGroup buttonGroup;
		protected IEnumerationTypeInfo enumType;
		protected List<Object> possibleValues;

		public OptionButtons(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.enumType = (IEnumerationTypeInfo) data.getType();
			buttonGroup = new ButtonGroup();
			refreshUI(true);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			OptionButtonsConfiguration controlCustomization = (OptionButtonsConfiguration) loadControlCustomization(
					input);
			if (refreshStructure) {
				if (data.getCaption().length() > 0) {
					setBorder(
							BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
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
				if (controlCustomization.layout == OptionButtonsLayout.HORIZONTAL_FLOW) {
					setLayout(new WrapLayout());
				} else if (controlCustomization.layout == OptionButtonsLayout.VERTICAL_FLOW) {
					setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				} else {
					throw new ReflectionUIError();
				}
			}
			if (enumType.isDynamicEnumeration() || refreshStructure) {
				possibleValues = Arrays.asList(enumType.getPossibleValues());
				while (buttonGroup.getButtonCount() > 0) {
					AbstractButton button = buttonGroup.getElements().nextElement();
					remove(button);
					buttonGroup.remove(button);
				}
				for (final Object value : possibleValues) {
					AbstractButton button = createButton(value);
					add(button);
					buttonGroup.add(button);

				}
			}
			Object currentValue = data.getValue();
			setSelectedValue(currentValue);
			return true;
		}

		protected void setSelectedValue(Object currentValue) {
			buttonGroup.clearSelection();
			int i = 0;
			for (Enumeration<AbstractButton> radioButtonsEnum = buttonGroup.getElements(); radioButtonsEnum
					.hasMoreElements();) {
				AbstractButton button = radioButtonsEnum.nextElement();
				listenerDisabled = true;
				try {
					button.setSelected(ReflectionUIUtils.equalsOrBothNull(currentValue, possibleValues.get(i)));
				} finally {
					listenerDisabled = false;
				}
				button.repaint();
				i++;
			}
		}

		protected AbstractButton createButton(final Object value) {
			OptionButtonsConfiguration controlCustomization = (OptionButtonsConfiguration) loadControlCustomization(
					input);
			final IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			final AbstractButton result;
			if (controlCustomization.buttonType == OptionButtonType.RADIO) {
				result = new JRadioButton(swingRenderer.prepareStringToDisplay(itemInfo.getCaption()));
				result.setOpaque(false);
				result.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
			} else if (controlCustomization.buttonType == OptionButtonType.TOGGLE) {
				result = new AbstractControlButton() {

					private static final long serialVersionUID = 1L;

					{
						setModel(new DefaultButtonModel() {

							private static final long serialVersionUID = 1L;

							@Override
							public boolean isArmed() {
								return super.isArmed() || isSelected();
							}

						});
					}

					@Override
					public SwingRenderer getSwingRenderer() {
						return OptionButtons.this.swingRenderer;
					}

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
						return itemInfo.getCaption();
					}

					@Override
					public Icon retrieveIcon() {
						return swingRenderer.getEnumerationItemIcon(itemInfo);
					}

				};
			} else {
				throw new ReflectionUIError();
			}
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (listenerDisabled) {
						return;
					}
					result.setSelected(true);
					data.setValue(value);
				}
			});
			result.setEnabled(!data.isGetOnly());
			return result;
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean isAutoManaged() {
			return false;
		}

		@Override
		public boolean requestCustomFocus() {
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
			return "OptionButtons [data=" + data + "]";
		}
	}

}
