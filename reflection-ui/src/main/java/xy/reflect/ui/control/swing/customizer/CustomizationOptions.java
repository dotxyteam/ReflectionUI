package xy.reflect.ui.control.swing.customizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class CustomizationOptions {

	protected final SwingCustomizer swingCustomizer;

	protected final TreeSet<String> hiddenCustomizationToolsTypeNames = new TreeSet<String>();
	protected boolean fieldSharedTypeOptionsDisplayed = true;

	public CustomizationOptions(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
	}

	public Set<String> getHiddenCustomizationToolsTypeNames() {
		return new HashSet<String>(hiddenCustomizationToolsTypeNames);
	}

	public void setHiddenCustomizationToolsTypeNames(Set<String> hiddenCustomizationToolsTypeNames) {
		final Set<String> impactedTypeNames = new HashSet<String>();
		impactedTypeNames.addAll(this.hiddenCustomizationToolsTypeNames);
		impactedTypeNames.addAll(hiddenCustomizationToolsTypeNames);
		impactedTypeNames.removeAll(ReflectionUIUtils.getIntersection(this.hiddenCustomizationToolsTypeNames,
				hiddenCustomizationToolsTypeNames));

		this.hiddenCustomizationToolsTypeNames.clear();
		this.hiddenCustomizationToolsTypeNames.addAll(hiddenCustomizationToolsTypeNames);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				List<JPanel> formsToRefresh = SwingRendererUtils.getAllDisplayedForms(swingCustomizer);
				{
					for (JPanel form : new ArrayList<JPanel>(formsToRefresh)) {
						Object object = swingCustomizer.getObjectByForm().get(form);
						if (object == null) {
							formsToRefresh.remove(form);
						} else {
							ITypeInfo objectType = swingCustomizer.getReflectionUI()
									.getTypeInfo(swingCustomizer.getReflectionUI().getTypeInfoSource(object));
							if (!impactedTypeNames.contains(objectType.getName())) {
								formsToRefresh.remove(form);
							}
						}
					}
					for (JPanel form : new ArrayList<JPanel>(formsToRefresh)) {
						formsToRefresh.removeAll(SwingRendererUtils.findDescendantForms(form, swingCustomizer));
					}
				}

				for (JPanel form : formsToRefresh) {
					swingCustomizer.getCustomizationTools().rebuildCustomizerForm(form);
				}
			}
		});

	}

	public void hideCustomizationToolsFor(String typeName) {
		Set<String> newHiddenCustomizationToolsTypeNames = new HashSet<String>(getHiddenCustomizationToolsTypeNames());
		newHiddenCustomizationToolsTypeNames.add(typeName);
		setHiddenCustomizationToolsTypeNames(newHiddenCustomizationToolsTypeNames);
	}

	public boolean areCustomizationToolsHiddenFor(String typeName) {
		return hiddenCustomizationToolsTypeNames.contains(typeName);
	}

	public void hideAllCustomizationTools() {
		Set<String> newHiddenCustomizationToolsTypeNames = new HashSet<String>(getHiddenCustomizationToolsTypeNames());
		for (Object object : SwingRendererUtils.getAllDisplayedObjects(swingCustomizer)) {
			ITypeInfo objectType = swingCustomizer.getReflectionUI()
					.getTypeInfo(swingCustomizer.getReflectionUI().getTypeInfoSource(object));
			newHiddenCustomizationToolsTypeNames.add(objectType.getName());
		}
		setHiddenCustomizationToolsTypeNames(newHiddenCustomizationToolsTypeNames);
	}

	public void unhideAllCustomizationTools() {
		setHiddenCustomizationToolsTypeNames(Collections.<String>emptySet());
	}

	public boolean areFieldSharedTypeOptionsDisplayed() {
		return fieldSharedTypeOptionsDisplayed;
	}

	public void setFieldSharedTypeOptionsDisplayed(boolean fieldSharedTypeOptionsDisplayed) {
		this.fieldSharedTypeOptionsDisplayed = fieldSharedTypeOptionsDisplayed;
	}

}