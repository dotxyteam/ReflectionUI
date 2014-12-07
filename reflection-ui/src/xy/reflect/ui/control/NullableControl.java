package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NullableControl extends JPanel implements IRefreshableControl,
		ICanShowCaptionControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected JCheckBox checkbox;
	protected Component subControl;
	protected DefaultTypeInfo defaultTypeInfo;
	protected boolean showCaption = false;

	public NullableControl(ReflectionUI reflectionUI, Object object,
			IFieldInfo field, DefaultTypeInfo defaultObjectTypeInfo) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.defaultTypeInfo = defaultObjectTypeInfo;

		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());

		checkbox = new JCheckBox();
		add(checkbox, BorderLayout.WEST);
		final ActionListener checkboxListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					checkboxToValue();
					valueToControl();
				} catch (Throwable t) {
					reflectionUI.handleDisplayedUIExceptions(
							NullableControl.this, t);
				}

			}
		};
		checkbox.addActionListener(checkboxListener);
		if (field.isReadOnly()) {
			checkbox.setEnabled(false);
		}

		valueToControl();
		checkboxListener.actionPerformed(null);
	}

	@Override
	public void refreshUI() {
		valueToControl();
	}

	protected void checkboxToValue() {
		Object currentValue = field.getValue(object);
		if (checkbox.isSelected()) {
			if (currentValue == null) {
				Object newValue;
				try {
					newValue = reflectionUI.onTypeInstanciationRequest(
							field.getType(), this, true);
				} catch (Throwable t) {
					reflectionUI.handleDisplayedUIExceptions(checkbox, t);
					newValue = null;
				}
				if (newValue == null) {
					checkbox.setSelected(false);
				} else {
					field.setValue(object, newValue);
				}
			}
		} else {
			if (currentValue != null) {
				field.setValue(object, null);
			}
		}
	}

	public void valueToControl() {
		Object currentValue = field.getValue(object);
		checkbox.setSelected(currentValue != null);
		if ((currentValue != null) && !(subControl instanceof NullControl)
				&& (subControl instanceof IRefreshableControl)) {
			((IRefreshableControl) subControl).refreshUI();
		} else {
			if (subControl != null) {
				remove(subControl);
			}
			if (currentValue != null) {
				subControl = defaultTypeInfo.createNonNullFieldValueControl(
						object, field);
				add(subControl, BorderLayout.CENTER);
				subControl.requestFocus();
			} else {
				subControl = createNullControl(reflectionUI, new Runnable() {
					@Override
					public void run() {
						if (!field.isReadOnly()) {
							checkbox.setSelected(true);
							checkboxToValue();
							valueToControl();
						}
					}
				});
				add(subControl, BorderLayout.CENTER);
			}
		}

		if (showCaption) {
			if (subControl instanceof ICanShowCaptionControl) {
				reflectionUI.setFieldControlCaption(NullableControl.this, null);
				((ICanShowCaptionControl) subControl).showCaption();
			} else {
				reflectionUI.setFieldControlCaption(NullableControl.this,
						field.getCaption());
			}
		}

		ReflectionUIUtils.updateLayout(this);
	}

	protected Component createNullControl(ReflectionUI reflectionUI,
			Runnable onMousePress) {
		return new NullControl(reflectionUI, field, onMousePress);
	}

	@Override
	public void showCaption() {
		showCaption = true;
		refreshUI();
	}

}
