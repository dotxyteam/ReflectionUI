package xy.reflect.ui.control.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public abstract class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;

	protected abstract Object getText();

	protected abstract void onMousePress();

	public NullControl(final SwingRenderer swingRenderer, FieldControlPlaceHolder placeHolder) {
		super(swingRenderer, placeHolder);
		textComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				try {
					onMousePress();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
				}
			}
		});
		if (getText() == null) {
			textComponent.setBackground(SwingRendererUtils.getNullColor());
		}
	}

	@Override
	protected IControlData retrieveData(FieldControlPlaceHolder placeHolder) {
		return new ControlDataProxy(IControlData.NULL_CONTROL_DATA) {
			@Override
			public Object getValue() {
				return getText();
			}

			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
			}

		};
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean showCaption() {
		return false;
	}

}
