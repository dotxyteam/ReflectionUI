package xy.reflect.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;

public class MethodControl extends JButton {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IMethodInfo method;

	public MethodControl(final ReflectionUI reflectionUI, Object object,
			IMethodInfo method) {
		String caption = method.getCaption();
		if (method.getParameters().size() > 0) {
			caption += "...";
			setToolTipText(method.toString());
		}
		setText(caption);
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
	}

	protected void activated() {
		reflectionUI.onMethodInvocationRequest(MethodControl.this,
				MethodControl.this.object,
				MethodControl.this.method, null, true );
	}
}
