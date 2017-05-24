package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class CustomizingMethodControlPlaceHolder extends MethodControlPlaceHolder {

	private static final long serialVersionUID = 1L;
	protected Component infoCustomizationsComponent;

	public CustomizingMethodControlPlaceHolder(SwingCustomizer swingCustomizer, JPanel form, IMethodInfo method) {
		super(swingCustomizer, form, method);
	}

	@Override
	public void refreshUI(boolean recreate) {
		if (((SwingCustomizer) swingRenderer).areCustomizationsEditable(getObject())) {
			refreshInfoCustomizationsControl();
		}
		super.refreshUI(recreate);
	}

	protected void refreshInfoCustomizationsControl() {
		if (infoCustomizationsComponent == null) {
			infoCustomizationsComponent = ((SwingCustomizer) swingRenderer).getCustomizationTools()
					.makeButtonForMethodInfo(this);
			add(infoCustomizationsComponent, BorderLayout.WEST);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			remove(infoCustomizationsComponent);
			infoCustomizationsComponent = null;
			refreshInfoCustomizationsControl();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		if (infoCustomizationsComponent != null) {
			result.width += infoCustomizationsComponent.getPreferredSize().width;
		}
		return result;
	}

}
