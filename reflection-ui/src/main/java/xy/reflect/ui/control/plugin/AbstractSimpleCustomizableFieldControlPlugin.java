/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.control.plugin;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.util.ReflectionUIUtils;

public abstract class AbstractSimpleCustomizableFieldControlPlugin extends AbstractSimpleFieldControlPlugin
		implements ICustomizableFieldControlPlugin {

	public abstract AbstractConfiguration getDefaultControlCustomization();

	@Override
	public JMenuItem makeFieldCustomizerMenuItem(final JButton customizerButton,
			final FieldControlPlaceHolder fieldControlPlaceHolder, final InfoCustomizations infoCustomizations,
			final ICustomizationTools customizationTools) {
		return new JMenuItem(new AbstractAction(
				customizationTools.getToolsRenderer().prepareStringToDisplay(getControlTitle() + " Options...")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				TypeCustomization typeCustomization = InfoCustomizations.getTypeCustomization(infoCustomizations,
						fieldControlPlaceHolder.getControlData().getType().getName(), true);
				AbstractConfiguration controlConfiguration = null;
				try {
					controlConfiguration = getControlCustomization(typeCustomization);
				} catch (Throwable t) {
					controlConfiguration = getDefaultControlCustomization();
				}
				StandardEditorBuilder status = customizationTools.getToolsRenderer().openObjectDialog(customizerButton,
						controlConfiguration, null, null, true, true);
				if (status.isCancelled()) {
					return;
				}
				storeControlCustomization(controlConfiguration, typeCustomization, customizationTools);
			}

		});
	}

	public void storeControlCustomization(AbstractConfiguration controlConfiguration,
			TypeCustomization typeCustomization, ICustomizationTools customizationTools) {
		Map<String, Object> specificProperties = typeCustomization.getSpecificProperties();
		specificProperties = new HashMap<String, Object>(specificProperties);
		storeControlCustomization(controlConfiguration, specificProperties);
		customizationTools.changeCustomizationFieldValue(typeCustomization, "specificProperties", specificProperties);
	}

	public AbstractConfiguration loadControlCustomization(Map<String, Object> specificProperties) {
		return (AbstractConfiguration) ReflectionUIUtils.getFieldControlPluginConfiguration(specificProperties,
				getIdentifier());
	}

	public void storeControlCustomization(AbstractConfiguration controlConfiguration,
			Map<String, Object> specificProperties) {
		ReflectionUIUtils.setFieldControlPluginConfiguration(specificProperties, getIdentifier(), controlConfiguration);
	}

	public AbstractConfiguration getControlCustomization(TypeCustomization typeCustomization) {
		AbstractConfiguration result = loadControlCustomization(typeCustomization.getSpecificProperties());
		if (result == null) {
			result = getDefaultControlCustomization();
		}
		return result;
	}

	public AbstractConfiguration loadControlCustomization(IFieldControlInput input) {
		AbstractConfiguration result = loadControlCustomization(
				input.getControlData().getType().getSpecificProperties());
		if (result == null) {
			result = getDefaultControlCustomization();
		}
		return result;
	}

	public static abstract class AbstractConfiguration implements Serializable {

		private static final long serialVersionUID = 1L;

	}

}
