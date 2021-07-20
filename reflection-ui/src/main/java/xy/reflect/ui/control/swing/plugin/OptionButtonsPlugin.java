


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
							BorderFactory.createTitledBorder(swingRenderer.prepareMessageToDisplay(data.getCaption())));
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
				possibleValues = Arrays.asList(enumType.getValues());
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
					button.setSelected(MiscUtils.equalsOrBothNull(currentValue, possibleValues.get(i)));
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
				result = new JRadioButton(swingRenderer.prepareMessageToDisplay(itemInfo.getCaption()));
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
					public String retrieveText() {
						return enumType.isDynamicEnumeration() ? itemInfo.getCaption()
								: swingRenderer.prepareMessageToDisplay(itemInfo.getCaption());
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
					try {
						result.setSelected(true);
						data.setValue(value);
					} catch (Throwable t) {
						swingRenderer.handleObjectException(OptionButtons.this, t);
					}
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
		public void validateSubForms() throws Exception {
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
		}

		@Override
		public String toString() {
			return "OptionButtons [data=" + data + "]";
		}
	}

}
