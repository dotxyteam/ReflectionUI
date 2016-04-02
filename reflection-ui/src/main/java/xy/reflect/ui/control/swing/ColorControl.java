package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;

public class ColorControl extends DialogAccessControl{
	protected static final long serialVersionUID = 1L;

	public ColorControl(ReflectionUI reflectionUI, Object object,
			IFieldInfo field) {
		super(reflectionUI, object, field);
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
		Component result = super.createButton();
		if (field.isGetOnly()) {
			result.setVisible(false);
		}
		return result;
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
