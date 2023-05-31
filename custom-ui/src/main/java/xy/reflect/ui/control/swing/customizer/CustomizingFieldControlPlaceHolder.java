
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JButton;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

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
	protected Component textualStorageFieldControlPluginSelectionComponent;

	public CustomizingFieldControlPlaceHolder(CustomizingForm form, IFieldInfo field) {
		super(form, field);
	}

	@Override
	public void refreshUI(boolean recreate) {
		refreshInfoCustomizationsControl();
		super.refreshUI(recreate);
	}

	public void refreshInfoCustomizationsControl() {
		if (form.getObject() instanceof TextualStorage) {
			if (textualStorageFieldControlPluginSelectionComponent == null) {
				textualStorageFieldControlPluginSelectionComponent = new CustomizationTools(
						(SwingCustomizer) swingRenderer) {

					@Override
					public void changeCustomizationFieldValue(AbstractCustomization customization, String fieldName,
							Object fieldValue) {
						ITypeInfo customizationType = ReflectionUI.getDefault()
								.buildTypeInfo(ReflectionUI.getDefault().getTypeInfoSource(customization));
						IFieldInfo customizationField = ReflectionUIUtils.findInfoByName(customizationType.getFields(),
								fieldName);
						customizationField.setValue(customization, fieldValue);
						form.refresh(true);
					}

					@Override
					protected JButton makeButton() {
						JButton result = new JButton(this.swingCustomizer.getCustomizationsIcon());
						result.setPreferredSize(
								new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
						result.setFocusable(false);
						return result;
					}
				}.makeButtonForTextualStorageDataField(this);
				add(textualStorageFieldControlPluginSelectionComponent, BorderLayout.EAST);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
		}
		if (!(((CustomizingForm) form)
				.areCustomizationsEditable(getObject()) == (infoCustomizationsComponent != null))) {
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

}
