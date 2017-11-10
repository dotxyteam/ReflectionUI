package xy.reflect.ui.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class MethodControl extends JButton implements ActionListener {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IMethodControlInput input;
	protected IMethodControlData data;

	public MethodControl(SwingRenderer swingRenderer, IMethodControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		initialize();
	}

	protected void initialize() {
		String caption = ReflectionUIUtils.formatMethodControlCaption(data);
		setText(swingRenderer.prepareStringToDisplay(caption));
		String toolTipText = ReflectionUIUtils.formatMethodControlTooltipText(data);
		if (toolTipText.length() > 0) {
			SwingRendererUtils.setMultilineToolTipText(this, swingRenderer.prepareStringToDisplay(toolTipText));
		}
		setIcon(SwingRendererUtils.getMethodIcon(swingRenderer, data));
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MethodAction action = swingRenderer.createMethodAction(input);
		try {
			action.actionPerformed(e);
		} catch (Throwable t) {
			swingRenderer.handleExceptionsFromDisplayedUI(MethodControl.this, t);
		}
	}

	@Override
	public String toString() {
		return "MethodControl [data=" + data + "]";
	}

}
