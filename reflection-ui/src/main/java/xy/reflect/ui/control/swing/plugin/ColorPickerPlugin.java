package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class ColorPickerPlugin extends AbstractSimpleFieldControlPlugin {

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	public String getControlTitle() {
		return "Color Picker";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return javaType.equals(Color.class);
	}

	@Override
	protected boolean displaysDistinctNullValue() {
		return false;
	}

	@Override
	public Component createControl(Object renderer, IFieldControlInput input) {
		return new ColorControl((SwingRenderer) renderer, input);
	}

	protected class ColorControl extends DialogAccessControl {
		protected static final long serialVersionUID = 1L;

		public ColorControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected JLabel createStatusControl(IFieldControlInput input) {
			JLabel result = new JLabel(" ");
			result.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			return result;
		}

		@Override
		protected void updateStatusControl() {
			Color newColor = (Color) data.getValue();
			if (newColor == null) {
				((JLabel) statusControl).setOpaque(false);
				((JLabel) statusControl).setBackground(null);
			} else {
				((JLabel) statusControl).setOpaque(true);
				((JLabel) statusControl).setBackground(newColor);
			}
		}

		@Override
		protected Component createChangeControl() {
			return null;
		}

		@Override
		protected void openDialog() {
			Color newColor = JColorChooser.showDialog(this, "Choose a color", statusControl.getBackground());
			if (newColor == null) {
				return;
			}
			data.setValue(newColor);
			updateControls();
		}

		@Override
		public String toString() {
			return "ColorControl [data=" + data + "]";
		}

	}

}
