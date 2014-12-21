package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.UIManager;

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
	protected JPanel nullingControl;
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

		nullingControl = new JPanel();
		int nullingControlSize = createNullControl(reflectionUI, null)
				.getPreferredSize().height;
		nullingControl.setPreferredSize(new Dimension(nullingControlSize,
				nullingControlSize));
		nullingControl.setOpaque(true);
		nullingControl.setBackground(ReflectionUIUtils
				.fixSeveralColorRenderingIssues(UIManager
						.getColor("TextField.shadow")));

		if (!field.isReadOnly()) {
			add(nullingControl, BorderLayout.EAST);
			nullingControl.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					try {
						setShouldBeNull(true);
						onNullStateChange();
					} catch (Throwable t) {
						reflectionUI.handleDisplayedUIExceptions(
								NullableControl.this, t);
					}
				}
			});
		}

		updateControl();
	}

	protected void setShouldBeNull(boolean b) {
		nullingControl.setVisible(!b);
	}

	protected boolean shoulBeNull() {
		return !nullingControl.isVisible();
	}

	@Override
	public void refreshUI() {
		updateControl();
	}

	protected void onNullStateChange() {
		Object currentValue = field.getValue(object);
		if (!shoulBeNull()) {
			if (currentValue == null) {
				Object newValue;
				try {
					newValue = reflectionUI.onTypeInstanciationRequest(
							field.getType(), this, true);
				} catch (Throwable t) {
					reflectionUI.handleDisplayedUIExceptions(this, t);
					newValue = null;
				}
				if (newValue == null) {
					setShouldBeNull(true);
				} else {
					field.setValue(object, newValue);
				}
			}
		} else {
			if (currentValue != null) {
				field.setValue(object, null);
			}
		}

		updateControl();
	}

	public void updateControl() {
		Object currentValue = field.getValue(object);
		setShouldBeNull(currentValue == null);
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
							setShouldBeNull(false);
							onNullStateChange();
						}
					}
				});
				add(subControl, BorderLayout.CENTER);
			}
		}

		if (showCaption) {
			updateCaption();
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
		updateCaption();
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

}
