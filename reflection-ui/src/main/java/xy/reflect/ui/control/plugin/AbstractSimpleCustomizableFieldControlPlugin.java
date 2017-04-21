package xy.reflect.ui.control.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.customization.CustomizationTools;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.info.type.factory.InfoCustomizations;
import xy.reflect.ui.info.type.factory.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractSimpleCustomizableFieldControlPlugin extends AbstractSimpleFieldControlPlugin
		implements ICustomizableFieldControlPlugin {

	protected abstract Component createControl(Object renderer, IFieldControlInput input,
			AbstractConfiguration controlConfiguration);

	protected abstract AbstractConfiguration getDefaultControlConfiguration();

	@Override
	public JMenuItem makeFieldCustomizerMenuItem(final JButton customizer,
			final FieldControlPlaceHolder fieldControlPlaceHolder, final InfoCustomizations infoCustomizations,
			final CustomizationTools customizationTools) {
		return new JMenuItem(new AbstractAction(
				customizationTools.getToolsRenderer().prepareStringToDisplay(getControlTitle() + " Options...")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				FieldCustomization fieldCustomization = customizationTools
						.getFieldCustomization(fieldControlPlaceHolder, infoCustomizations);
				Object controlConfiguration = getControlCustomization(fieldCustomization);
				StandardEditorBuilder status = customizationTools.getToolsRenderer().openObjectDialog(customizer,
						controlConfiguration, null, null, true, true);
				if (status.isCancelled()) {
					return;
				}
				storeControlCustomization(controlConfiguration, fieldCustomization);
				customizationTools.rebuildCustomizerForm(customizer);
			}
		});
	}

	protected void storeControlCustomization(Object controlConfiguration, FieldCustomization fieldCustomization) {
		Map<String, Object> specificProperties = fieldCustomization.getSpecificProperties();
		specificProperties = new HashMap<String, Object>(specificProperties);
		specificProperties.put(getIdentifier(), ReflectionUIUtils.serializeToHexaText(controlConfiguration));
		fieldCustomization.setSpecificProperties(specificProperties);
	}

	protected AbstractConfiguration getControlCustomization(Map<String, Object> specificProperties) {
		String text = (String) specificProperties.get(getIdentifier());
		if (text == null) {
			return getDefaultControlConfiguration();
		}
		return (AbstractConfiguration) ReflectionUIUtils.deserializeFromHexaText(text);
	}

	protected AbstractConfiguration getControlCustomization(FieldCustomization fieldCustomization) {
		return getControlCustomization(fieldCustomization.getSpecificProperties());
	}

	protected AbstractConfiguration getControlCustomization(IFieldControlInput input) {
		return getControlCustomization(input.getControlData().getSpecificProperties());
	}

	@Override
	public Component createControl(Object renderer, IFieldControlInput input) {
		AbstractConfiguration controlConfiguration = getControlCustomization(input);
		return createControl(renderer, input, controlConfiguration);
	}

	protected static abstract class AbstractConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;

	}

}