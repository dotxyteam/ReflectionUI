package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.BorderFactory;
import  javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.DialogBuilder;
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
	public ColorControl createControl(Object renderer, IFieldControlInput input) {
		return new ColorControl((SwingRenderer) renderer, input);
	}

	public class ColorControl extends DialogAccessControl {
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
		protected void openDialog(Component owner) {
			DialogBuilder dialogBuilder = swingRenderer.getDialogBuilder(owner);
			dialogBuilder.setTitle( "Choose a color");
			Color initialColor = statusControl.getBackground();
			JColorChooser colorChooser = new JColorChooser(initialColor != null ? initialColor : Color.white);
			dialogBuilder.setContentComponent(colorChooser);
			dialogBuilder.setToolbarComponents(dialogBuilder.createStandardOKCancelDialogButtons(null, null));
			swingRenderer.showDialog(dialogBuilder.createDialog(), true);			
			if (!dialogBuilder.wasOkPressed()) {
				return;
			}
			Color newColor  = colorChooser.getColor();
			data.setValue(newColor);
			updateControls();
		}

		@Override
		public String toString() {
			return "ColorControl [data=" + data + "]";
		}

	}

}
