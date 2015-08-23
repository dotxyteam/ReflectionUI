package xy.reflect.ui.control.swing;

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
import xy.reflect.ui.util.SwingRendererUtils;

public class NullableControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected JCheckBox nullingControl;
	protected Component subControl;
	protected DefaultTypeInfo defaultTypeInfo;

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
					subControl.requestFocus();
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer()
							.handleExceptionsFromDisplayedUI(
									NullableControl.this, t);
				}
			}
		});

		if (!field.isReadOnly()) {
			add(nullingControl, BorderLayout.WEST);
		}

		refreshUI();
	}

	public Component getSubControl() {
		return subControl;
	}

	protected void setShouldBeNull(boolean b) {
		nullingControl.setSelected(!b);
	}

	protected boolean shoulBeNull() {
		return !nullingControl.isSelected();
	}

	@Override
	public boolean refreshUI() {
		Object value = field.getValue(object);
		setShouldBeNull(value == null);
		boolean hadFocus = (subControl != null)
				&& SwingRendererUtils.hasOrContainsFocus(subControl);
		updateSubControl(value);
		if (hadFocus && (subControl != null)) {
			subControl.requestFocus();
		}
		return true;
	}

	@Override
	public void requestFocus() {
		if (subControl != null) {
			subControl.requestFocus();
		}
	}

	protected void onNullingControlStateChange() {
		Object newValue;
		if (!shoulBeNull()) {
			try {
				newValue = reflectionUI.getSwingRenderer()
						.onTypeInstanciationRequest(this, field.getType(),
								false);
			} catch (Throwable t) {
				reflectionUI.getSwingRenderer()
						.handleExceptionsFromDisplayedUI(this, t);
				newValue = null;
			}
			if (newValue == null) {
				setShouldBeNull(true);
				return;
			}
		} else {
			newValue = null;
			remove(subControl);
			subControl = null;
		}
		field.setValue(object, newValue);
		reflectionUI.getSwingRenderer().refreshFieldControlsByName(
				SwingRendererUtils.findForm(this, reflectionUI),
				field.getName());
	}

	public void updateSubControl(Object newValue) {
		if (!((newValue != null) && (subControl instanceof IFieldControl) && (((IFieldControl) subControl)
				.refreshUI()))) {
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
			reflectionUI.getSwingRenderer().handleComponentSizeChange(this);
		}
	}

	protected Component createNullControl(ReflectionUI reflectionUI,
			Runnable onMousePress) {
		return new NullControl(reflectionUI, onMousePress);
	}

	@Override
	public boolean showCaption() {
		if (subControl instanceof IFieldControl) {
			return ((IFieldControl) subControl).showCaption();
		} else {
			return false;
		}
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		if (subControl instanceof IFieldControl) {
			return ((IFieldControl) subControl).displayError(error);
		} else {
			return false;
		}
	}

}
