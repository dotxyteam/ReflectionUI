package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;
	protected Runnable action;

	public NullControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				final IFieldControlData baseControlData = super.getControlData();
				return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {

					@Override
					public Object getValue() {
						String result = baseControlData.getNullValueLabel();
						if (result == null) {
							result = "";
						}
						return result;
					}

					@Override
					public ITypeInfo getType() {
						return new DefaultTypeInfo(swingRenderer.getReflectionUI(), String.class);
					}

					@Override
					public String getCaption() {
						return baseControlData.getCaption();
					}

				};
			}

		});
		setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				runAction();
			}
		});
	}

	protected void runAction() {
		if (action != null) {
			try {
				action.run();
			} catch (Throwable t) {
				swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
			}
		}
	}

	@Override
	protected Component createTextComponent() {
		final JTextArea result = new JTextArea();
		result.setEditable(false);
		((JComponent) result).setBorder(null);
		if ("".equals(data.getValue())) {
			result.setBackground(swingRenderer.getNullColor());
		} else {
			result.setBackground(ReflectionUIUtils.getDisabledTextBackgroundColor());
		}
		result.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				runAction();
			}
		});
		return result;
	}

	@Override
	protected JScrollPane createScrollPane() {
		JScrollPane result = super.createScrollPane();
		result.setViewportBorder(null);
		return result;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public String toString() {
		return "NullControl [data=" + data + "]";
	}

}
