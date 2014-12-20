package xy.reflect.ui.control;

import java.awt.Dimension;
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
		if (method.getParameters().size() > 0) {
			caption += "...";
		}
		setText(caption);
		setToolTipText(caption);
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.method = method;

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					reflectionUI.onMethodInvocationRequest(MethodControl.this,
							MethodControl.this.object,
							MethodControl.this.method);
				} catch (Throwable t) {
					reflectionUI.handleDisplayedUIExceptions(
							MethodControl.this, t);
				}

			}
		});
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return super.getPreferredSize();
		}
		result.width = ReflectionUIUtils
				.getStandardCharacterWidth(this) * 20;
		return result;
	}

}
