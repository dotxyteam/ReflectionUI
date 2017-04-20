package xy.reflect.ui.control.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.customization.CustomizationTools;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.info.type.factory.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractSimpleCustomizableFieldControlPlugin extends AbstractSimpleFieldControlPlugin
		implements ICustomizableFieldControlPlugin {

	protected abstract Component createControl(Object renderer, IFieldControlInput input,
			AbstractCustomization controlCustomization);

	protected abstract AbstractCustomization getDefaultControlCustomization();

	protected abstract String getControlTitle();

	@Override
	public JMenuItem makeFieldCustomizerMenuItem(final Component customizedFormComponent,
			final FieldCustomization fieldCustomization, final CustomizationTools customizationTools) {
		return new JMenuItem(new AbstractAction(
				customizationTools.getToolsRenderer().prepareStringToDisplay(getControlTitle() + " Options...")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Object controlCustomization = getControlCustomization(getControlCutomizationStorageId(),
						fieldCustomization);
				StandardEditorBuilder status = customizationTools.getToolsRenderer()
						.openObjectDialog(customizedFormComponent, controlCustomization, null, null, true, true);
				if (status.isCancelled()) {
					return;
				}
				storeControlCustomization(controlCustomization, fieldCustomization);
				customizationTools.rebuildForm(customizedFormComponent);
			}
		});
	}

	protected String getControlCutomizationStorageId() {
		return getDefaultControlCustomization().getClass().getName();
	}

	protected void storeControlCustomization(Object controlCustomization, FieldCustomization fieldCustomization) {
		Map<String, Object> specificProperties = fieldCustomization.getSpecificProperties();
		specificProperties = new HashMap<String, Object>(specificProperties);
		specificProperties.put(controlCustomization.getClass().getName(),
				ReflectionUIUtils.serializeToHexaText(controlCustomization));
		fieldCustomization.setSpecificProperties(specificProperties);
	}

	protected AbstractCustomization getControlCustomization(String storageId, Map<String, Object> specificProperties) {
		String text = (String) specificProperties.get(storageId);
		if (text == null) {
			return  getDefaultControlCustomization();
		}
		return (AbstractCustomization) ReflectionUIUtils.deserializeFromHexaText(text);
	}

	protected AbstractCustomization getControlCustomization(String storageId, FieldCustomization fieldCustomization) {
		return getControlCustomization(storageId, fieldCustomization.getSpecificProperties());
	}

	protected AbstractCustomization getControlCustomization(String storageId, IFieldControlInput input) {
		return getControlCustomization(storageId, input.getControlData().getSpecificProperties());
	}

	@Override
	public Component createControl(Object renderer, IFieldControlInput input) {
		AbstractCustomization controlCustomization = getControlCustomization(getControlCutomizationStorageId(), input);
		return createControl(renderer, input, controlCustomization);
	}

}