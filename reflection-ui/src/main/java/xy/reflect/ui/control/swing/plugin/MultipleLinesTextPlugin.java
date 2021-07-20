


package xy.reflect.ui.control.swing.plugin;

import java.awt.Dimension;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control plugin that allows to display multiline text. The height of the
 * control is fixed.
 * 
 * @author olitank
 *
 */
public class MultipleLinesTextPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Multiple Lines Text";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return String.class.equals(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new MultipleLinesTextConfiguration();
	}

	@Override
	public MultipleLinesTextControl createControl(Object renderer, IFieldControlInput input) {
		return new MultipleLinesTextControl((SwingRenderer) renderer, input);
	}

	public static class MultipleLinesTextConfiguration extends AbstractConfiguration {

		private static final long serialVersionUID = 1L;

		public ControlDimensionSpecification length = new ControlDimensionSpecification();

		public int getLenghthInPixels() {
			if (length.unit == ControlSizeUnit.PIXELS) {
				return length.value;
			} else if (length.unit == ControlSizeUnit.SCREEN_PERCENT) {
				Dimension screenSize = MiscUtils.getDefaultScreenSize();
				return Math.round((length.value / 100f) * screenSize.height);
			} else {
				throw new ReflectionUIError();
			}
		}

		public enum ControlSizeUnit {
			PIXELS, SCREEN_PERCENT
		}

		public static class ControlDimensionSpecification implements Serializable {

			private static final long serialVersionUID = 1L;

			public int value = 40;
			public ControlSizeUnit unit = ControlSizeUnit.SCREEN_PERCENT;

		}

	}

	public class MultipleLinesTextControl extends TextControl {

		private static final long serialVersionUID = 1L;

		public MultipleLinesTextControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected Dimension getDynamicPreferredSize(ControlScrollPane scrollPane, Dimension defaultSize) {
			Dimension result = super.getDynamicPreferredSize(scrollPane, defaultSize);
			MultipleLinesTextConfiguration controlCustomization = (MultipleLinesTextConfiguration) loadControlCustomization(
					input);
			int configuredHeight = controlCustomization.getLenghthInPixels();
			result.height = configuredHeight;
			return result;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			super.refreshUI(refreshStructure);
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
					setBorder(BorderFactory.createEmptyBorder());
				}
				textComponent.setBorder(BorderFactory.createEmptyBorder());
			}
			return true;
		}

		@Override
		public String toString() {
			return "MultipleLinesTextControl [data=" + data + "]";
		}
	}

}
