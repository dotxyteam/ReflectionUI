package xy.reflect.ui.control;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IBooleanTypeInfo;

public class CheckBoxControl extends JCheckBox implements
		ICanShowCaptionControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected IBooleanTypeInfo booleanType;

	public CheckBoxControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.booleanType = (IBooleanTypeInfo) field.getType();

		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setBorderPainted(true);
		setBorder(BorderFactory.createTitledBorder(""));
		if (field.isReadOnly()) {
			setEnabled(false);
		}

		Boolean initialValue = booleanType.toBoolean(field.getValue(object));
		setText(reflectionUI.translateUIString("true"));
		setSelected(initialValue);
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onValueChange();
			}
		});
	}

	protected void onValueChange() {
		field.setValue(object, booleanType.fromBoolean(isSelected()));
	}

	@Override
	public void showCaption() {
		setText(reflectionUI.translateUIString(field.getCaption()));
	}

}
