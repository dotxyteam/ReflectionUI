package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import xy.reflect.ui.control.input.FieldControlDataProxy;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;
	protected Runnable action;

	public NullControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, input);
	}

	@Override
	protected Component createTextComponent() {
		Component result = super.createTextComponent();
		result.addMouseListener(new MouseAdapter() {
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
			result.setBackground(SwingRendererUtils.getNullColor());
		}
		((JComponent) result).setBorder(null);
		return result;
	}

	protected Object getText() {
		return input.getControlData().getNullValueLabel();
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	@Override
	protected IFieldControlData retrieveData() {
		return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {
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
