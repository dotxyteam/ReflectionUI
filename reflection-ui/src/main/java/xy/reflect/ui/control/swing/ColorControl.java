package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.control.data.IControlData;

public class ColorControl extends DialogAccessControl{
	protected static final long serialVersionUID = 1L;

	public ColorControl(SwingRenderer swingRenderer, IControlData data) {
		super(swingRenderer, data);
	}

	@Override
	protected JLabel createStatusControl() {
		JLabel result =  new JLabel(" ");
		result.setOpaque(true);
		result.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		return result;
	}

	
	
	@Override
	protected void updateStatusControl() {
		((JLabel)statusControl).setBackground((Color) data.getValue());
	}

	@Override
	protected Component createButton() {
		return null;
	}

	
	@Override
	public void requestFocus() {
		statusControl.requestFocus();
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

}
