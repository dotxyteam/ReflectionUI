package xy.reflect.ui.control.swing.customizer;

import java.awt.Component;
import java.awt.event.AWTEventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class CustomizationOptions {

	protected final SwingCustomizer swingCustomizer;
	protected AWTEventListener openWindowListener;

	protected final TreeSet<String> hiddenCustomizationToolsTypeNames = new TreeSet<String>();
	protected boolean fieldSharedTypeOptionsDisplayed = true;

	public CustomizationOptions(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
	}

	protected void openWindow(Component activatorComponent) {
		this.swingCustomizer.getCustomizationTools().getToolsRenderer().openObjectDialog(
				activatorComponent, CustomizationOptions.this,
				this.swingCustomizer.getCustomizationTools().getToolsRenderer()
						.getObjectTitle(CustomizationOptions.this),
				this.swingCustomizer.getCustomizationsIcon().getImage(), false, true);
	}

	public Set<String> getHiddenCustomizationToolsTypeNames() {
		return new HashSet<String>(hiddenCustomizationToolsTypeNames);
	}

	public void setHiddenCustomizationToolsTypeNames(Set<String> hiddenCustomizationToolsTypeNames) {
		Set<String> impacted = new HashSet<String>();
		impacted.addAll(this.hiddenCustomizationToolsTypeNames);
		impacted.addAll(hiddenCustomizationToolsTypeNames);
		impacted.removeAll(ReflectionUIUtils.getIntersection(this.hiddenCustomizationToolsTypeNames,
				hiddenCustomizationToolsTypeNames));

		this.hiddenCustomizationToolsTypeNames.clear();
		this.hiddenCustomizationToolsTypeNames.addAll(hiddenCustomizationToolsTypeNames);

		for (String typeName : impacted) {
			for (Map.Entry<JPanel, Object> entry : this.swingCustomizer.getObjectByForm().entrySet()) {
				Object object = entry.getValue();
				ITypeInfo objectType = this.swingCustomizer.getReflectionUI()
						.getTypeInfo(this.swingCustomizer.getReflectionUI().getTypeInfoSource(object));
				if (typeName.equals(objectType.getName())) {
					for (JPanel form : SwingRendererUtils.findObjectForms(object, this.swingCustomizer)) {
						this.swingCustomizer.getCustomizationTools().rebuildCustomizerForm(form);
					}
				}
			}
		}
	}

	public void hideFor(String typeName) {
		Set<String> newHiddenCustomizationToolsTypeNames = new HashSet<String>(getHiddenCustomizationToolsTypeNames());
		newHiddenCustomizationToolsTypeNames.add(typeName);
		setHiddenCustomizationToolsTypeNames(newHiddenCustomizationToolsTypeNames);
	}

	public boolean areHiddenFor(String typeName) {
		return hiddenCustomizationToolsTypeNames.contains(typeName);
	}

	public boolean areFieldSharedTypeOptionsDisplayed() {
		return fieldSharedTypeOptionsDisplayed;
	}

	public void setFieldSharedTypeOptionsDisplayed(boolean fieldSharedTypeOptionsDisplayed) {
		this.fieldSharedTypeOptionsDisplayed = fieldSharedTypeOptionsDisplayed;
	}

}