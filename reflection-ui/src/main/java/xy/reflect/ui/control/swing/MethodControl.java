
package xy.reflect.ui.control.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.IAdvancedMethodControl;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Control that displays a button allowing to invoke a method.
 * 
 * @author olitank
 *
 */
public class MethodControl extends AbstractControlButton implements IAdvancedMethodControl, ActionListener {

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
	public Font retrieveCustomFont() {
		if (data.getCustomFontResourcePath() == null) {
			return null;
		} else {
			return SwingRendererUtils.loadFontThroughCache(data.getCustomFontResourcePath(),
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
		Image image = swingRenderer.getMethodIconImage(data);
		if (image == null) {
			return null;
		}
		return SwingRendererUtils.getIcon(image);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		MethodAction action = swingRenderer.createMethodAction(input);
		action.actionPerformed(e);
	}

	@Override
	public void validateSubForms(ValidationSession session) throws Exception {
		if (!data.isReturnValueValidityDetectionEnabled()) {
			return;
		}
		Form[] form = new Form[1];
		new MethodAction(swingRenderer, input) {
			private static final long serialVersionUID = 1L;

			{
				if (data.getParameters().size() > 0) {
					throw new ReflectionUIError(
							"Cannot validate the return value of this method that requires parameter value(s): '"
									+ data.getMethodSignature() + "'");
				}
				InvocationData invocationData = prepare(null);
				returnValue = input.getControlData().invoke(invocationData);
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							form[0] = createReturnValueEditorBuilder(null).createEditorForm(false, false);
						}
					});
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}	
			}
		};
		if(Thread.currentThread().isInterrupted()) {
			return;
		}
		form[0].validateForm(session);
	}

	@Override
	public String toString() {
		return "MethodControl [data=" + data + "]";
	}

}
