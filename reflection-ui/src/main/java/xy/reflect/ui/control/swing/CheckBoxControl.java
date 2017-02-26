package xy.reflect.ui.control.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;

public class CheckBoxControl extends JCheckBox implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;
	

	public CheckBoxControl(final SwingRenderer swingRenderer, FieldControlPlaceHolder placeHolder) {
		this.swingRenderer = swingRenderer;
		this.data = placeHolder.getControlData();
		
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBorderPainted(true);
		setBorder(BorderFactory.createTitledBorder(""));
		if (data.isGetOnly()) {
			setEnabled(false);
		}

		setSelected((Boolean) data.getValue());
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.setValue(isSelected());
			}
		});
	}
	
	
	@Override
	public boolean showCaption() {
		setText(swingRenderer.prepareStringToDisplay(data.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public Object getFocusDetails() {
		return null;
	}

	@Override
	public void requestDetailedFocus(Object focusDetails) {
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	
}
