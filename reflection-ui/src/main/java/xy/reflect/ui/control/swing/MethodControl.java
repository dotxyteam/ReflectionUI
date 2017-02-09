package xy.reflect.ui.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class MethodControl extends JButton {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IMethodInfo method;

	public MethodControl(SwingRenderer swingRenderer, Object object, IMethodInfo method) {
		this.swingRenderer = swingRenderer;
		this.object = object;
		this.method = method;
		initialize();
	}

	protected void initialize() {
		String caption = method.getCaption();
		{
			if (caption.length() > 0) {
				if (method.getParameters().size() > 0) {
					caption += "...";
				}
				setText(swingRenderer.prepareStringToDisplay(caption));
			}
		}
		String toolTipText = "";
		{
			if (method.getParameters().size() > 0) {
				toolTipText += "Parameter(s): " + ReflectionUIUtils.formatParameterList(method.getParameters());
			}
			if ((method.getOnlineHelp() != null) && (method.getOnlineHelp().trim().length() > 0)) {
				if (toolTipText.length() > 0) {
					toolTipText += ":\n";
				}
				toolTipText += method.getOnlineHelp();
			}
			if (toolTipText.length() > 0) {
				SwingRendererUtils.setMultilineToolTipText(this, swingRenderer.prepareStringToDisplay(toolTipText));
			}
		}
		setIcon(SwingRendererUtils.getMethodIcon(swingRenderer, object, method));
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MethodAction action = swingRenderer.createMethodAction(object, method);
				action.setShouldDisplayReturnValueIfAny(true);
				action.setRetunValueWindowDetached(method.getValueReturnMode() == ValueReturnMode.COPY);
				action.setModificationStack(
						ReflectionUIUtils.findParentFormModificationStack(MethodControl.this, swingRenderer));
				try {
					action.actionPerformed(e);
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(MethodControl.this, t);
				}
			}
		});
	}

}
