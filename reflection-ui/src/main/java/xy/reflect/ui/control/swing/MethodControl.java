package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractControlButton;

public class MethodControl extends AbstractControlButton implements ActionListener {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IMethodControlInput input;
	protected IMethodControlData data;

	public MethodControl(SwingRenderer swingRenderer, IMethodControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		addActionListener(this);
	}

	@Override
	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	@Override
	public Color retrieveBackgroundColor() {
		if (data.getBackgroundColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(data.getBackgroundColor());
		}
	}

	@Override
	public Color retrieveForegroundColor() {
		if (data.getForegroundColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(data.getForegroundColor());
		}

	}

	@Override
	public Image retrieveBackgroundImage() {
		if (data.getBackgroundImagePath() == null) {
			return null;
		} else {
			return SwingRendererUtils.loadImageThroughcache(data.getBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
		}
	}

	@Override
	public String retrieveCaption() {
		return ReflectionUIUtils.formatMethodControlCaption(data);
	}

	@Override
	public String retrieveToolTipText() {
		return ReflectionUIUtils.formatMethodControlTooltipText(data);
	}

	@Override
	public Icon retrieveIcon() {
		return SwingRendererUtils.getMethodIcon(swingRenderer, data);
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
