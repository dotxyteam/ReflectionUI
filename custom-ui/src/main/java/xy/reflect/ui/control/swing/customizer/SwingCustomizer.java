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
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingCustomizerUtils;
import xy.reflect.ui.util.SystemProperties;

/**
 * This class is a sub-class of {@link SwingRenderer} that allows to customize
 * generated UIs by visually editing an {@link InfoCustomizations} instance and
 * instantly previewing the result.
 * 
 * @author olitank
 *
 */
public class SwingCustomizer extends CustomizedSwingRenderer {

	public static void main(String[] args) throws Exception {
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instanciate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + MoreSystemProperties.describe();
		final Class<?> clazz;
		if (args.length == 0) {
			clazz = Object.class;
		} else if (args.length == 1) {
			if (args[0].equals("--help")) {
				System.out.println(usageText);
				return;
			} else {
				clazz = Class.forName(args[0]);
			}
		} else {
			throw new IllegalArgumentException(usageText);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ReflectionUI reflectionUI = SwingCustomizer.getDefault().getReflectionUI();
				Object object = SwingCustomizer.getDefault().onTypeInstanciationRequest(null,
						reflectionUI.getTypeInfo(new JavaTypeInfoSource(reflectionUI, clazz, null)));
				if (object == null) {
					return;
				}
				SwingCustomizer.getDefault().openObjectFrame(object);
			}
		});
	}

	/**
	 * An {@link ITypeInfo} specific property key used to disable customization
	 * tools on forms associated to this {@link ITypeInfo} instances.
	 */
	public static final String CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY = SwingRenderer.class.getName()
			+ ".CUSTOMIZATIONS_FORBIDDEN";

	protected static SwingCustomizer defaultInstance;

	protected CustomizationTools customizationTools;
	protected CustomizationOptions customizationOptions;
	protected CustomizationController customizationController;
	protected String infoCustomizationsOutputFilePath;

	/**
	 * @return the default instance connected to {@link CustomizedUI#getDefault()}.
	 */
	public static SwingCustomizer getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new SwingCustomizer(CustomizedUI.getDefault());
			defaultInstance.infoCustomizationsOutputFilePath = SystemProperties.getDefaultInfoCustomizationsFilePath();
		}
		return defaultInstance;
	}

	/**
	 * A constructor allowing to specify the {@link CustomizedUI} instance and the
	 * path of the customizations file.
	 * 
	 * @param customizedUI                     The {@link CustomizedUI} instance to
	 *                                         use.
	 * @param infoCustomizationsOutputFilePath The path of the customizations file
	 *                                         to use.
	 */
	public SwingCustomizer(CustomizedUI customizedUI, String infoCustomizationsOutputFilePath) {
		super(customizedUI);
		if (infoCustomizationsOutputFilePath != null) {
			File file = new File(infoCustomizationsOutputFilePath);
			if (file.exists()) {
				try {
					getInfoCustomizations().loadFromFile(file,
							ReflectionUIUtils.getDebugLogListener(getCustomizedUI()));
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			} else {
				try {
					getInfoCustomizations().saveToFile(file, ReflectionUIUtils.getDebugLogListener(getCustomizedUI()));
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			}
		}
		this.infoCustomizationsOutputFilePath = infoCustomizationsOutputFilePath;
	}

	/**
	 * A constructor allowing to specify only the {@link CustomizedUI} instance.
	 * 
	 * @param customizedUI The {@link CustomizedUI} instance to use.
	 */
	public SwingCustomizer(CustomizedUI customizedUI) {
		this(customizedUI, null);
	}

	/**
	 * @return the path of the customizations file.
	 */
	public String getInfoCustomizationsOutputFilePath() {
		return infoCustomizationsOutputFilePath;
	}

	/**
	 * @return the {@link CustomizationTools} instance that is used.
	 */
	public CustomizationTools getCustomizationTools() {
		if (customizationTools == null) {
			customizationTools = createCustomizationTools();
		}
		return customizationTools;
	}

	/**
	 * @return the {@link CustomizationOptions} instance that is used.
	 */
	public CustomizationOptions getCustomizationOptions() {
		if (customizationOptions == null) {
			customizationOptions = createCustomizationOptions();
		}
		return customizationOptions;
	}

	/**
	 * @return the {@link CustomizationController} instance that is used.
	 */
	public CustomizationController getCustomizationController() {
		if (customizationController == null) {
			customizationController = createCustomizationController();
		}
		return customizationController;
	}

	/**
	 * @return whether customizations are enabled or not. Customizations are
	 *         disabled if the customizations file path is not defined (null) or if
	 *         the {@link MoreSystemProperties#HIDE_INFO_CUSTOMIZATIONS_TOOLS}
	 *         property is set to "true".
	 */
	public boolean isCustomizationsEditorEnabled() {
		return (infoCustomizationsOutputFilePath != null) && !MoreSystemProperties.areCustomizationToolsDisabled();
	}

	/**
	 * @return the main customization tools icon.
	 */
	public ImageIcon getCustomizationsIcon() {
		return SwingCustomizerUtils.CUSTOMIZATION_ICON;
	}

	protected CustomizationController createCustomizationController() {
		return new CustomizationController(this);
	}

	protected CustomizationOptions createCustomizationOptions() {
		return new CustomizationOptions(this);
	}

	protected CustomizationTools createCustomizationTools() {
		return new CustomizationTools(this);
	}

	@Override
	public CustomizingForm createForm(Object object, IInfoFilter infoFilter) {
		return new CustomizingForm(this, object, infoFilter);
	}

	protected class CustomizingForm extends Form {
		private static final long serialVersionUID = 1L;

		protected boolean toolsAdded;

		public CustomizingForm(SwingRenderer swingRenderer, Object object, IInfoFilter infoFilter) {
			super(swingRenderer, object, infoFilter);
			if (isCustomizationsEditorEnabled()) {
				addAncestorListener(new AncestorListener() {

					@Override
					public void ancestorRemoved(AncestorEvent event) {
						getCustomizationController().formRemoved(CustomizingForm.this);
					}

					@Override
					public void ancestorMoved(AncestorEvent event) {
					}

					@Override
					public void ancestorAdded(AncestorEvent event) {
						getCustomizationController().formAdded(CustomizingForm.this);
					}
				});
			}
		}

		/**
		 * @param object
		 * @return whether customizations are enabled for the specified object or not.
		 */
		public boolean areCustomizationsEditable(Object object) {
			if (!isCustomizationsEditorEnabled()) {
				return false;
			}
			if (!getCustomizationOptions().isInEditMode()) {
				return false;
			}
			if (!getInfoCustomizations()
					.equals(objectType.getSpecificProperties().get(InfoCustomizations.CURRENT_CUSTOMIZATIONS_KEY))) {
				return false;
			}
			if (Boolean.TRUE.equals(objectType.getSpecificProperties().get(CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY))) {
				return false;
			}
			return true;
		}

		/**
		 * @return whether customization tools are installed on the form or not.
		 */
		public boolean isToolsAdded() {
			return toolsAdded;
		}

		@Override
		public void layoutMembersControls(
				Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
				Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
				JPanel membersPanel) {
			if (areCustomizationsEditable(object)) {
				membersPanel.setLayout(new BorderLayout());
				JPanel newMembersPanel = new ControlPanel();
				{
					membersPanel.add(newMembersPanel, BorderLayout.CENTER);
					Border newMembersPanelBorder;
					{
						int borderThickness = 2;
						newMembersPanelBorder = BorderFactory.createLineBorder(
								getCustomizationTools().getToolsRenderer().getToolsForegroundColor(), borderThickness);
						newMembersPanelBorder = BorderFactory.createCompoundBorder(newMembersPanelBorder,
								BorderFactory.createLineBorder(
										getCustomizationTools().getToolsRenderer().getToolsBackgroundColor(),
										borderThickness));
						newMembersPanelBorder = BorderFactory.createCompoundBorder(newMembersPanelBorder,
								BorderFactory.createLineBorder(
										getCustomizationTools().getToolsRenderer().getToolsForegroundColor(),
										borderThickness));
						newMembersPanel.setBorder(newMembersPanelBorder);
					}
					super.layoutMembersControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
							newMembersPanel);
				}
				JPanel typeCustomizationsControl = new ControlPanel();
				{
					typeCustomizationsControl.setLayout(new BorderLayout());
					typeCustomizationsControl.add(getCustomizationTools().makeButtonForTypeInfo(object),
							BorderLayout.CENTER);
					membersPanel.add(
							SwingRendererUtils.flowInLayout(typeCustomizationsControl, GridBagConstraints.CENTER),
							BorderLayout.NORTH);
				}
				toolsAdded = true;
			} else {
				super.layoutMembersControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
						membersPanel);
				toolsAdded = false;
			}
		}

		@Override
		public void refresh(boolean refreshStructure) {
			if (areCustomizationsEditable(object) != toolsAdded) {
				removeAll();
				fieldControlPlaceHoldersByCategory.clear();
				methodControlPlaceHoldersByCategory.clear();
				super.refresh(true);
			} else {
				super.refresh(refreshStructure);
			}
		}

		@Override
		public CustomizingFieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
			return new CustomizingFieldControlPlaceHolder((SwingCustomizer) swingRenderer, this, field);
		}

		@Override
		public CustomizingMethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
			return new CustomizingMethodControlPlaceHolder((SwingCustomizer) swingRenderer, this, method);
		}

	}

}
