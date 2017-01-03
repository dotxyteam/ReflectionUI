package xy.reflect.ui.control.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class CheckBoxControl extends JCheckBox implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IControlData data;
	

	public CheckBoxControl(final SwingRenderer swingRenderer, final IControlData data) {
		this.swingRenderer = swingRenderer;
		this.data = data;
		
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBorderPainted(true);
		setBorder(BorderFactory.createTitledBorder(""));
		if (data.isGetOnly()) {
			setEnabled(false);
		}

		Boolean initialValue = (Boolean) data.getValue();
		setSelected(initialValue);
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onValueChange();
			}
		});
	}
	
	@Override
	public void setPalceHolder(FieldControlPlaceHolder fieldControlPlaceHolder) {
	}

	protected void onValueChange() {
		data.setValue(isSelected());
	}

	@Override
	public boolean showCaption(String caption) {
		setText(swingRenderer.prepareStringToDisplay(caption));
		return true;
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
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

	@Override
	public ITypeInfo getDynamicObjectType() {
		return null;
	}
	
	
}
