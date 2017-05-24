package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class CustomizingFieldControlPlaceHolder extends FieldControlPlaceHolder {

	private static final long serialVersionUID = 1L;
	protected Component infoCustomizationsComponent;

	public CustomizingFieldControlPlaceHolder(SwingCustomizer swingCustomizer, JPanel form, IFieldInfo field) {
		super(swingCustomizer, form, field);
	}

	@Override
	public void refreshUI(boolean recreate) {
		refreshInfoCustomizationsControl();
		super.refreshUI(recreate);
	}

	protected void refreshInfoCustomizationsControl() {
		if (((SwingCustomizer) swingRenderer)
				.areCustomizationsEditable(getObject()) == (infoCustomizationsComponent != null)) {
			return;
		}
		if (infoCustomizationsComponent == null) {
			infoCustomizationsComponent = ((SwingCustomizer) swingRenderer).getCustomizationTools()
					.makeButtonForFieldInfo(this);
			add(infoCustomizationsComponent, BorderLayout.EAST);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			remove(infoCustomizationsComponent);
			infoCustomizationsComponent = null;
			refreshInfoCustomizationsControl();
		}
	}

}
