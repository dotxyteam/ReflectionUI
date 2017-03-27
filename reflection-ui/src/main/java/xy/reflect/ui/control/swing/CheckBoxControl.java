package xy.reflect.ui.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;

public class CheckBoxControl extends JCheckBox implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	public CheckBoxControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.data = input.getControlData();

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
		setText(swingRenderer.prepareStringToDisplay(data.getCaption()));
	}

	@Override
	public boolean showsCaption() {
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
	public boolean requestDetailedFocus(Object focusDetails) {
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public String toString() {
		return "CheckBoxControl [data=" + data + "]";
	}

}
