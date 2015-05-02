package xy.reflect.ui.control;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class CheckBoxControl extends JCheckBox implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	
	public CheckBoxControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBorderPainted(true);
		setBorder(BorderFactory.createTitledBorder(""));
		if (field.isReadOnly()) {
			setEnabled(false);
		}

		Boolean initialValue = (Boolean) field.getValue(object);
		setText(reflectionUI.prepareUIString("Is True"));
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
		if (!caption.startsWith("Is ") && !caption.startsWith("Has ")) {
			caption += " (Is True)";
		}
		setText(reflectionUI.prepareUIString(caption));
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
