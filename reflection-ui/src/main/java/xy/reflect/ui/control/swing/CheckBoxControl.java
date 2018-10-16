package xy.reflect.ui.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.SwingRendererUtils;

public class CheckBoxControl extends JCheckBox implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	public CheckBoxControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		setOpaque(false);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					data.setValue(isSelected());
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(CheckBoxControl.this, t);
				}
			}
		});
		refreshUI(true);
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
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			setText(swingRenderer.prepareStringToDisplay(data.getCaption()));
			setForeground(SwingRendererUtils.getColor(data.getForegroundColor()));
			setEnabled(!data.isGetOnly());
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		setSelected(Boolean.TRUE.equals(data.getValue()));
		return true;
	}

	@Override
	public boolean handlesModificationStackAndStress() {
		return false;
	}

	@Override
	public boolean requestCustomFocus() {
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "CheckBoxControl [data=" + data + "]";
	}

}