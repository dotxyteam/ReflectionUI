package xy.reflect.ui.control.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import xy.reflect.ui.control.input.ControlDataProxy;
import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.control.input.IControlInput;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;
	protected Runnable action;

	public NullControl(final SwingRenderer swingRenderer, IControlInput input) {
		super(swingRenderer, input);
		textComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (action != null) {
					try {
						action.run();
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
					}
				}
			}
		});
		if (getText() == null) {
			textComponent.setBackground(SwingRendererUtils.getNullColor());
		}
	}

	protected Object getText() {
		return input.getControlData().getNullValueLabel();
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	@Override
	protected IControlData retrieveData() {
		return new ControlDataProxy(IControlData.NULL_CONTROL_DATA) {
			@Override
			public Object getValue() {
				return getText();
			}

			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
			}

			@Override
			public String getCaption() {
				return input.getControlData().getCaption();
			}

		};
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

}
