package xy.reflect.ui.control.swing.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;
import xy.reflect.ui.util.component.WrapLayout;

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
	protected AbstractConfiguration getDefaultControlCustomization() {
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
					if (data.getForegroundColor() != null) {
						((TitledBorder) getBorder()).setTitleColor(SwingRendererUtils.getColor(data.getForegroundColor()));
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
			int i = 0;
			for (Enumeration<AbstractButton> radioButtonsEnum = buttonGroup.getElements(); radioButtonsEnum
					.hasMoreElements();) {
				AbstractButton button = radioButtonsEnum.nextElement();
				if (ReflectionUIUtils.equalsOrBothNull(currentValue, possibleValues.get(i))) {
					listenerDisabled = true;
					try {
						button.setSelected(true);
					} finally {
						listenerDisabled = false;
					}
					break;
				}
				i++;
			}
		}

		protected AbstractButton createButton(final Object value) {
			OptionButtonsConfiguration controlCustomization = (OptionButtonsConfiguration) loadControlCustomization(
					input);
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			AbstractButton result;
			if (controlCustomization.buttonType == OptionButtonType.RADIO) {
				result = new JRadioButton(swingRenderer.prepareStringToDisplay(itemInfo.getCaption()));
				result.setOpaque(false);
				result.setForeground(SwingRendererUtils.getColor(data.getForegroundColor()));
			} else if (controlCustomization.buttonType == OptionButtonType.TOGGLE) {
				result = new JToggleButton(swingRenderer.prepareStringToDisplay(itemInfo.getCaption()));
				result.setIcon(SwingRendererUtils.getEnumerationItemIcon(swingRenderer, itemInfo));
			} else {
				throw new ReflectionUIError();
			}
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (listenerDisabled) {
						return;
					}
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
		public boolean handlesModificationStackAndStress() {
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
