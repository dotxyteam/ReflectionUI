package xy.reflect.ui.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class MethodControl extends JButton {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IMethodInfo method;

	public MethodControl(final ReflectionUI reflectionUI, Object object,
			IMethodInfo method) {
		String caption = method.getCaption();
		String toolTipText = "";
		if (method.getParameters().size() > 0) {
			caption += "...";
			toolTipText += "Parameter(s): "
					+ ReflectionUIUtils.formatParameterList(method
							.getParameters());
		}
		if ((method.getOnlineHelp() != null)
				&& (method.getOnlineHelp().trim().length() > 0)) {
			if (toolTipText.length() > 0) {
				toolTipText += ":\n";
			}
			toolTipText += method.getOnlineHelp();
		}
		if (toolTipText.length() > 0) {
			SwingRendererUtils.setMultilineToolTipText(this,
					reflectionUI.prepareUIString(toolTipText));
		}
		setText(reflectionUI.prepareUIString(caption));
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.method = method;

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					activated();
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(
							MethodControl.this, t);
				}

			}
		});
	}

	protected void activated() {
		reflectionUI.getSwingRenderer().onMethodInvocationRequest(MethodControl.this,
				MethodControl.this.object, MethodControl.this.method, null);
	}
}
