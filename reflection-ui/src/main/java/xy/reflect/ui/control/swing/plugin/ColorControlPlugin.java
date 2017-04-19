package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class ColorControlPlugin implements IFieldControlPlugin {

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	public boolean handles(IFieldControlInput input) {
		if (input.getControlData().isValueNullable()) {
			return false;
		}
		final Class<?> javaType;
		try {
			javaType = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
		} catch (ClassNotFoundException e) {
			return false;
		}
		if (javaType != Color.class) {
			return false;
		}
		return true;
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
			result.setOpaque(true);
			result.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			return result;
		}

		@Override
		protected void updateStatusControl() {
			((JLabel) statusControl).setBackground((Color) data.getValue());
		}

		@Override
		protected Component createButton() {
			return null;
		}

		@Override
		public boolean requestDetailedFocus(Object focusDetails) {
			return SwingRendererUtils.requestAnyComponentFocus(statusControl, null, swingRenderer);
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
