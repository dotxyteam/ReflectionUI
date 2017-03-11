package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class NullableControl extends JPanel implements IAdvancedFieldControl {

	protected SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected IControlData data;
	protected JCheckBox nullStatusControl;
	protected Component subControl;
	protected FieldControlPlaceHolder placeHolder;
	protected ITypeInfo subControlValueType;

	public NullableControl(SwingRenderer swingRenderer, FieldControlPlaceHolder placeHolder) {
		this.swingRenderer = swingRenderer;
		this.placeHolder = placeHolder;
		this.data = placeHolder.getControlData();
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		nullStatusControl = new JCheckBox();
		nullStatusControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
					subControl.requestFocus();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullableControl.this, t);
				}
			}
		});
		add(nullStatusControl, BorderLayout.WEST);
		refreshUI();
	}

	public Component getSubControl() {
		return subControl;
	}

	protected void setNullStatusControlState(boolean b) {
		nullStatusControl.setSelected(!b);
	}

	protected boolean getNullStatusControlState() {
		return !nullStatusControl.isSelected();
	}

	@Override
	public boolean refreshUI() {
		Object value = data.getValue();
		setNullStatusControlState(value == null);
		boolean hadFocus = (subControl != null) && SwingRendererUtils.hasOrContainsFocus(subControl);
		updateSubControl(value);
		if (hadFocus && (subControl != null)) {
			subControl.requestFocus();
		}
		return true;
	}

	protected void onNullingControlStateChange() {
		if (getNullStatusControlState()) {
			data.setValue(null);
		} else {
			Object newValue = null;
			try {
				newValue = this.swingRenderer.onTypeInstanciationRequest(this, placeHolder.getControlData().getType(),
						false);
			} catch (Throwable t) {
				swingRenderer.handleExceptionsFromDisplayedUI(this, t);
			}
			data.setValue(newValue);
		}
		refreshUI();
	}

	public void updateSubControl(Object newValue) {
		boolean updated = false;
		if (subControl instanceof IAdvancedFieldControl) {
			IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) subControl;
			if (newValue != null) {
				if (fieldControl.refreshUI()) {
					updated = true;
				}
			}
		}
		if (!updated) {
			if (subControl != null) {
				remove(subControl);
			}
			subControlValueType = (newValue == null) ? null
					: swingRenderer.getReflectionUI()
							.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newValue));
			subControl = swingRenderer.createFieldControl(placeHolder);
			if (subControl instanceof NullControl) {
				((NullControl) subControl).setAction(new Runnable() {
					@Override
					public void run() {
						setNullStatusControlState(false);
						onNullingControlStateChange();
					}
				});
			}
			add(subControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	@Override
	public boolean showCaption() {
		if (subControl instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) subControl).showCaption();
		} else {
			return false;
		}
	}

	@Override
	public boolean displayError(String msg) {
		if (subControl instanceof IAdvancedFieldControl) {
			return ((IAdvancedFieldControl) subControl).displayError(msg);
		} else {
			return false;
		}
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		if (getNullStatusControlState() == true) {
			return false;
		} else {
			if (subControl instanceof IAdvancedFieldControl) {
				return ((IAdvancedFieldControl) subControl).handlesModificationStackUpdate();
			} else {
				return false;
			}
		}
	}

	@Override
	public Object getFocusDetails() {
		Object subControlFocusDetails = null;
		Class<?> subControlClass = null;
		{
			if (subControl instanceof IAdvancedFieldControl) {
				subControlFocusDetails = ((IAdvancedFieldControl) subControl).getFocusDetails();
				subControlClass = subControl.getClass();
			}
		}
		if (subControlFocusDetails == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subControlFocusDetails", subControlFocusDetails);
		result.put("subControlClass", subControlClass);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		Object subControlFocusDetails = focusDetails.get("subControlFocusDetails");
		Class<?> subControlClass = (Class<?>) focusDetails.get("subControlClass");
		subControl.requestFocus();
		if (subControl instanceof IAdvancedFieldControl) {
			if (subControl.getClass().equals(subControlClass)) {
				((IAdvancedFieldControl) subControl).requestDetailedFocus(subControlFocusDetails);
			}
		}
	}

	@Override
	public void requestFocus() {
		if (subControl != null) {
			subControl.requestFocus();
		}
	}

	@Override
	public void validateSubForm() throws Exception {
		if (subControl instanceof IAdvancedFieldControl) {
			((IAdvancedFieldControl) subControl).validateSubForm();
		}
	}

	public ITypeInfo getSubControlValueType() {
		return subControlValueType;
	}
}