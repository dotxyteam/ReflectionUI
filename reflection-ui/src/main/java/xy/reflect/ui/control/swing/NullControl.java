package xy.reflect.ui.control.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;

	public NullControl(final SwingRenderer swingRenderer, final String text, final Runnable onMousePress) {
		super(swingRenderer, new ControlDataProxy(IControlData.NULL_CONTROL_DATA) {
			@Override
			public Object getValue() {
				return text;
			}

			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
			}

		});
		if (onMousePress != null) {
			textComponent.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					try {
						onMousePress.run();
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
					}
				}
			});
		}
		if (text == null) {
			textComponent.setBackground(SwingRendererUtils.getNullColor());
		}
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
