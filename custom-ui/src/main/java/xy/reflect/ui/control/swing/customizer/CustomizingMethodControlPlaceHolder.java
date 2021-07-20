
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * This is a sub-class of {@link MethodControlPlaceHolder} that allows to
 * install method customization tools.
 * 
 * @author olitank
 *
 */
public class CustomizingMethodControlPlaceHolder extends MethodControlPlaceHolder {

	private static final long serialVersionUID = 1L;
	protected Component infoCustomizationsComponent;

	public CustomizingMethodControlPlaceHolder(CustomizingForm form,
			IMethodInfo method) {
		super(form, method);
	}

	@Override
	public void refreshUI(boolean refreshStructure) {
		refreshInfoCustomizationsControl();
		super.refreshUI(refreshStructure);
	}

	public void refreshInfoCustomizationsControl() {
		if (((CustomizingForm) form).areCustomizationsEditable(getObject()) == (infoCustomizationsComponent != null)) {
			return;
		}
		if (infoCustomizationsComponent == null) {
			infoCustomizationsComponent = ((SwingCustomizer) swingRenderer).getCustomizationTools()
					.makeButtonForMethod(this);
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
