package xy.reflect.ui.control.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class CheckBoxControl extends JCheckBox implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;


	public CheckBoxControl(final SwingRenderer swingRenderer, final Object object, final IFieldInfo field) {
		this.swingRenderer = swingRenderer;
		this.object = object;
		this.field = field;

		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBorderPainted(true);
		setBorder(BorderFactory.createTitledBorder(""));
		if (field.isGetOnly()) {
			setEnabled(false);
		}

		Boolean initialValue = (Boolean) field.getValue(object);
		setSelected(initialValue);
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onValueChange();
			}
		});
	}

	protected void onValueChange() {
		field.setValue(object, isSelected());
	}

	@Override
	public boolean showCaption() {
		String caption = field.getCaption();
		setText(swingRenderer.getReflectionUI().prepareStringToDisplay(caption));
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
}
