
package xy.reflect.ui.control.swing.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.MiscUtils;
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
		return true;
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
		public int spacing = ITypeInfo.DEFAULT_FORM_SPACING * 2;
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
		protected Object lastSelectedValue;
		protected ControlPanel contentPane;

		public OptionButtons(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			this.enumType = (IEnumerationTypeInfo) data.getType();
			buttonGroup = new ButtonGroup();
			setLayout(new BorderLayout());
			add(SwingRendererUtils.flowInLayout(contentPane = new ControlPanel(), GridBagConstraints.CENTER),
					BorderLayout.CENTER);
			refreshUI(true);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			OptionButtonsConfiguration controlCustomization = (OptionButtonsConfiguration) loadControlCustomization(
					input);
			if (refreshStructure) {
				SwingRendererUtils.showFieldCaptionOnBorder(data, this, new Accessor<Border>() {
					@Override
					public Border get() {
						return new ControlPanel().getBorder();
					}
				}, swingRenderer);
				if (controlCustomization.layout == OptionButtonsLayout.HORIZONTAL_FLOW) {
					contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
				} else if (controlCustomization.layout == OptionButtonsLayout.VERTICAL_FLOW) {
					contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
				} else {
					throw new ReflectionUIError();
				}
				SwingRendererUtils.handleComponentSizeChange(this);
			}
			if (enumType.isDynamicEnumeration() || refreshStructure) {
				possibleValues = Arrays.asList(enumType.getValues());
				while (buttonGroup.getButtonCount() > 0) {
					AbstractButton button = buttonGroup.getElements().nextElement();
					buttonGroup.remove(button);
				}
				contentPane.removeAll();
				for (int i = 0; i < possibleValues.size(); i++) {
					final Object value = possibleValues.get(i);
					AbstractButton button = createButton(value);
					if (i > 0) {
						contentPane.add(Box.createRigidArea(
								new Dimension(controlCustomization.spacing, controlCustomization.spacing)));
					}
					contentPane.add(button);
					buttonGroup.add(button);

				}
				SwingRendererUtils.handleComponentSizeChange(this);
			}
			Object currentValue = data.getValue();
			updateSelection(currentValue);
			return true;
		}

		protected void updateSelection(Object currentValue) {
			listenerDisabled = true;
			try {
				int currentValueIndex = possibleValues.indexOf(currentValue);
				if (currentValueIndex == -1) {
					buttonGroup.clearSelection();
				} else {
					buttonGroup.setSelected(
							Collections.list(buttonGroup.getElements()).get(currentValueIndex).getModel(), true);
				}
			} finally {
				listenerDisabled = false;
			}
			lastSelectedValue = currentValue;
		}

		protected AbstractButton createButton(final Object value) {
			OptionButtonsConfiguration controlCustomization = (OptionButtonsConfiguration) loadControlCustomization(
					input);
			final IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			final AbstractButton result;
			if (controlCustomization.buttonType == OptionButtonType.RADIO) {
				result = new JRadioButton(swingRenderer.prepareMessageToDisplay(itemInfo.getCaption()));
				result.setOpaque(false);
				result.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
				if (data.getLabelCustomFontResourcePath() != null) {
					result.setFont(SwingRendererUtils
							.loadFontThroughCache(data.getLabelCustomFontResourcePath(),
									ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
							.deriveFont(result.getFont().getStyle(), result.getFont().getSize()));
				} else {
					result.setFont(new JRadioButton().getFont());
				}
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
					public Image retrieveBackgroundImage() {
						if (data.getButtonBackgroundImagePath() == null) {
							return null;
						} else {
							return SwingRendererUtils.loadImageThroughCache(data.getButtonBackgroundImagePath(),
									ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
						}
					}

					@Override
					public Font retrieveCustomFont() {
						if (data.getButtonCustomFontResourcePath() == null) {
							return null;
						} else {
							return SwingRendererUtils.loadFontThroughCache(data.getButtonCustomFontResourcePath(),
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
					public String retrieveText() {
						return enumType.isDynamicEnumeration() ? itemInfo.getCaption()
								: swingRenderer.prepareMessageToDisplay(itemInfo.getCaption());
					}

					@Override
					public String retrieveToolTipText() {
						return super.getToolTipText();
					}

					@Override
					public Icon retrieveIcon() {
						Image image = swingRenderer.getEnumerationItemIconImage(itemInfo);
						if (image == null) {
							return null;
						}
						return SwingRendererUtils.getIcon(image);
					}

				};
			} else {
				throw new ReflectionUIError();
			}
			String tooltipText = itemInfo.getOnlineHelp();
			if ((tooltipText != null) && (tooltipText.length() > 0)) {
				result.setToolTipText(SwingRendererUtils
						.adaptToolTipTextToMultiline(swingRenderer.prepareMessageToDisplay(tooltipText)));
			}
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (listenerDisabled) {
						return;
					}
					try {
						Object newValue = value;
						if (data.isNullValueDistinct()) {
							if (MiscUtils.equalsOrBothNull(lastSelectedValue, newValue)) {
								newValue = null;
							}
						}
						data.setValue(newValue);
						updateSelection(newValue);
					} catch (Throwable t) {
						swingRenderer.handleException(OptionButtons.this, t);
					}
				}
			});
			result.setEnabled(!data.isGetOnly());
			return result;
		}

		@Override
		public boolean displayError(Throwable error) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean isModificationStackManaged() {
			return false;
		}

		@Override
		public boolean areValueAccessErrorsManaged() {
			return false;
		}

		@Override
		public boolean requestCustomFocus() {
			if (data.isGetOnly()) {
				return false;
			}
			Enumeration<AbstractButton> buttonEnum = buttonGroup.getElements();
			while (buttonEnum.hasMoreElements()) {
				AbstractButton button = buttonEnum.nextElement();
				if (SwingRendererUtils.requestAnyComponentFocus(button, swingRenderer)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void validateControlData(ValidationSession session) throws Exception {
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
		}

	}

}
