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
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NullableControl extends JPanel implements IRefreshableControl,
		ICanShowCaptionControl, ICanDisplayErrorControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected JCheckBox nullingControl;
	protected Component subControl;
	protected DefaultTypeInfo defaultTypeInfo;
	protected boolean captionShown = false;

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
		nullingControl = new JCheckBox();
		nullingControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(
							NullableControl.this, t);
				}
			}
		});

		if (!field.isReadOnly()) {
			add(nullingControl, BorderLayout.WEST);
		}

		refreshUI();
	}

	protected void setShouldBeNull(boolean b) {
		nullingControl.setSelected(!b);
	}

	protected boolean shoulBeNull() {
		return !nullingControl.isSelected();
	}

	@Override
	public void refreshUI() {
		Object value = field.getValue(object);
		setShouldBeNull(value == null);
		updateSubControl(value);
	}

	protected void onNullingControlStateChange() {
		Object newValue;
		if (!shoulBeNull()) {
			try {
				newValue = reflectionUI.onTypeInstanciationRequest(this,
						field.getType(), false);
			} catch (Throwable t) {
				reflectionUI.handleExceptionsFromDisplayedUI(this, t);
				newValue = null;
			}
			if (newValue == null) {
				setShouldBeNull(true);
				return;
			}
		} else {
			newValue = null;
		}

		field.setValue(object, newValue);
		updateSubControl(newValue);
		subControl.requestFocus();		
	}

	public void updateSubControl(Object newValue) {
		boolean shouldUpdateCaption = false;
		if ((newValue != null) && !(subControl instanceof NullControl)
				&& (subControl instanceof IRefreshableControl)) {
			((IRefreshableControl) subControl).refreshUI();
		} else {
			if (subControl != null) {
				remove(subControl);
			}
			if (newValue != null) {
				subControl = defaultTypeInfo.createNonNullFieldValueControl(
						object, field);
				add(subControl, BorderLayout.CENTER);
			} else {
				subControl = createNullControl(reflectionUI, new Runnable() {
					@Override
					public void run() {
						if (!field.isReadOnly()) {
							setShouldBeNull(false);
							onNullingControlStateChange();
						}
					}
				});
				add(subControl, BorderLayout.CENTER);
			}
			if (captionShown) {
				shouldUpdateCaption = true;
			}
		}

		if (shouldUpdateCaption) {
			updateCaption();
		}

		ReflectionUIUtils.updateLayout(this);
	}

	protected Component createNullControl(ReflectionUI reflectionUI,
			Runnable onMousePress) {
		return new NullControl(reflectionUI, onMousePress);
	}

	@Override
	public void showCaption() {
		updateCaption();
		captionShown = true;
	}

	protected void updateCaption() {
		if (subControl instanceof ICanShowCaptionControl) {
			reflectionUI.setFieldControlCaption(NullableControl.this, null);
			((ICanShowCaptionControl) subControl).showCaption();
		} else {
			reflectionUI.setFieldControlCaption(NullableControl.this,
					field.getCaption());
		}
	}

	@Override
	public void displayError(String error) {
		if ((!shoulBeNull()) && (subControl instanceof ICanDisplayErrorControl)) {
			((ICanDisplayErrorControl) subControl).displayError(error);
		} else {
			if (error != null) {
				reflectionUI.handleExceptionsFromDisplayedUI(subControl,
						new ReflectionUIError(error));
				refreshUI();
			}
		}
	}

}
