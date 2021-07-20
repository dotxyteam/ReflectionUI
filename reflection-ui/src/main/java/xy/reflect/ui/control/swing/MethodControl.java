


package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Control that displays a button allowing to invoke a method.
 * 
 * @author olitank
 *
 */
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
			return SwingRendererUtils.loadImageThroughCache(data.getBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
		}
	}

	@Override
	public Color retrieveBorderColor() {
		if (data.getBorderColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(data.getBorderColor());
		}
	}

	@Override
	public String retrieveText() {
		return swingRenderer.prepareMessageToDisplay(
				ReflectionUIUtils.formatMethodControlCaption(data.getCaption(), data.getParameters()));
	}

	@Override
	public String retrieveToolTipText() {
		return swingRenderer.prepareMessageToDisplay(ReflectionUIUtils.formatMethodControlTooltipText(data.getCaption(),
				data.getOnlineHelp(), data.getParameters()));
	}

	@Override
	public Icon retrieveIcon() {
		return swingRenderer.getMethodIcon(data);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		MethodAction action = swingRenderer.createMethodAction(input);
		action.actionPerformed(e);
	}

	@Override
	public String toString() {
		return "MethodControl [data=" + data + "]";
	}

}
