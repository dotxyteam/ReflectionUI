
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;

import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.IFieldInfo;

/**
 * This is a sub-class of {@link FieldControlPlaceHolder} that allows to install
 * field customization tools.
 * 
 * @author olitank
 *
 */
public class CustomizingFieldControlPlaceHolder extends FieldControlPlaceHolder {

	private static final long serialVersionUID = 1L;
	protected Component infoCustomizationsComponent;

	public CustomizingFieldControlPlaceHolder(CustomizingForm form, IFieldInfo field) {
		super(form, field);
	}

	@Override
	public void refreshUI(boolean recreate) {
		refreshInfoCustomizationsControl();
		super.refreshUI(recreate);
	}

	public void refreshInfoCustomizationsControl() {
		if (((CustomizingForm) form).areCustomizationsEditable(getObject()) == (infoCustomizationsComponent != null)) {
			return;
		}
		if (infoCustomizationsComponent == null) {
			infoCustomizationsComponent = ((SwingCustomizer) swingRenderer).getCustomizationTools()
					.makeButtonForField(this);
			add(infoCustomizationsComponent, BorderLayout.EAST);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			remove(infoCustomizationsComponent);
			infoCustomizationsComponent = null;
			refreshInfoCustomizationsControl();
		}
	}

}
