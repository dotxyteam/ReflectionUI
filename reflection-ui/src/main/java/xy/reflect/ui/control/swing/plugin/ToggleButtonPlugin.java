
package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control plugin that allows to display boolean values with toggle
 * buttons.
 * 
 * @author olitank
 *
 */
public class ToggleButtonPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Toggle Button";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Boolean.class.equals(javaType) || boolean.class.equals(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new ToggleButtonConfiguration();
	}

	@Override
	public ToggleButtonControl createControl(Object renderer, IFieldControlInput input) {
		return new ToggleButtonControl((SwingRenderer) renderer, input);
	}

	public static class ToggleButtonConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public ResourcePath iconImagePath;
	}

	public class ToggleButtonControl extends AbstractControlButton implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;

		protected boolean listenerDisabled = false;

		protected ToggleButtonConfiguration controlConfiguration;

		public ToggleButtonControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			setModel(new DefaultButtonModel() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isArmed() {
					return super.isArmed() || isSelected();
				}

			});
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (listenerDisabled) {
						return;
					}
					boolean newValue = !ToggleButtonControl.this.isSelected();
					try {
						data.setValue(newValue);
						listenerDisabled = true;
						try {
							setSelected(newValue);
						} finally {
							listenerDisabled = false;
						}
					} catch (Throwable t) {
						swingRenderer.handleException(ToggleButtonControl.this, t);
					}
				}
			});
			refreshUI(true);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			if (refreshStructure) {
				updateControlConfiguration();
				String tooltipText = data.getOnlineHelp();
				if ((tooltipText != null) && (tooltipText.length() > 0)) {
					setToolTipText(SwingRendererUtils
							.adaptToolTipTextToMultiline(swingRenderer.prepareMessageToDisplay(tooltipText)));
				}
				setEnabled(!data.isGetOnly());
				if (initialized) {
					initialized = false;
					initialize();
				}
			}
			listenerDisabled = true;
			try {
				setSelected(Boolean.TRUE.equals(data.getValue()));
			} finally {
				listenerDisabled = false;
			}
			return true;
		}

		protected void updateControlConfiguration() {
			controlConfiguration = (ToggleButtonConfiguration) loadControlCustomization(input);
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public void validateControlData(ValidationSession session) throws Exception {
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
		}

		@Override
		public boolean requestCustomFocus() {
			return false;
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
		public boolean displayError(Throwable error) {
			return false;
		}

		@Override
		public Image retrieveBackgroundImage() {
			if (data.getButtonBackgroundImagePath() == null) {
				return null;
			} else {
				return SwingRendererUtils.loadImageThroughCache(data.getButtonBackgroundImagePath(),
						ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()), swingRenderer);
			}
		}

		@Override
		public Font retrieveCustomFont() {
			if (data.getButtonCustomFontResourcePath() == null) {
				return null;
			} else {
				return SwingRendererUtils.loadFontThroughCache(data.getButtonCustomFontResourcePath(),
						ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()), swingRenderer);
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
			return swingRenderer.prepareMessageToDisplay(data.getCaption());
		}

		@Override
		public String retrieveToolTipText() {
			return super.getToolTipText();
		}

		@Override
		public Icon retrieveIcon() {
			Image image = swingRenderer.getIconImage(controlConfiguration.iconImagePath);
			if (image == null) {
				return null;
			}
			return SwingRendererUtils.getIcon(image);
		}

	}
}
