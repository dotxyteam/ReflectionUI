package xy.reflect.ui.control.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class MethodControl extends JButton {

	protected static final long serialVersionUID = 1L;

	protected MethodAction action;

	public MethodControl(MethodAction action) {
		this.action = action;
		initialize();

	}

	protected void initialize() {
		SwingRenderer swingRenderer = action.getSwingRenderer();
		IMethodInfo method = action.getMethod();

		String caption = method.getCaption();
		String toolTipText = "";
		if (method.getParameters().size() > 0) {
			caption += "...";
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
		setText(swingRenderer.prepareStringToDisplay(caption));

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.actionPerformed(e);
			}
		});
	}

	public MethodAction getAction() {
		return action;
	}


}
