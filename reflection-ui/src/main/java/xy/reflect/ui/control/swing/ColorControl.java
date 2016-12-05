package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.info.field.IFieldInfo;

public class ColorControl extends DialogAccessControl{
	protected static final long serialVersionUID = 1L;

	public ColorControl(SwingRenderer swingRenderer, Object object,
			IFieldInfo field) {
		super(swingRenderer, object, field);
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
		((JLabel)statusControl).setBackground((Color) field.getValue(object));
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
		field.setValue(object, newColor);
		updateControls();
	}

}
