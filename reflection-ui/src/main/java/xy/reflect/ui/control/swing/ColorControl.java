package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.util.SwingRendererUtils;

public class ColorControl extends DialogAccessControl {
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
