package xy.reflect.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

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
			toolTipText += method.toString();
		}
		if ((method.getDocumentation() != null)
				&& (method.getDocumentation().trim().length() > 0)) {
			toolTipText += ":\n" + method.getDocumentation();
		}
		if (toolTipText.length() > 0) {
			ReflectionUIUtils.setMultilineToolTipText(this, reflectionUI.translateUIString(toolTipText));
		}
		setText(reflectionUI.translateUIString(caption));
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.method = method;

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					activated();
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(
							MethodControl.this, t);
				}

			}
		});
		
		if(method.isReadOnly()){
			setEnabled(false);
		}
	}

	protected void activated() {
		reflectionUI.onMethodInvocationRequest(MethodControl.this,
				MethodControl.this.object, MethodControl.this.method, null,
				true);
	}
}
