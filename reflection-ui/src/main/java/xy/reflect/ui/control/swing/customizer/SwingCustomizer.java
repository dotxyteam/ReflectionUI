package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class SwingCustomizer extends SwingRenderer {

	public static final String CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY = SwingCustomizer.class.getName()
			+ ".CUSTOMIZATIONS_FORBIDDEN";

	protected InfoCustomizations infoCustomizations;
	protected String infoCustomizationsOutputFilePath;
	protected CustomizationTools customizationTools;
	protected CustomizationOptions customizationOptions;
	protected CustomizationController customizationController;

	public SwingCustomizer(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations,
			String infoCustomizationsOutputFilePath) {
		super(reflectionUI);
		this.customizationTools = createCustomizationTools();
		this.customizationOptions = createCustomizationOptions();
		this.customizationController = createCustomizationController();
		this.infoCustomizations = infoCustomizations;
		this.infoCustomizationsOutputFilePath = infoCustomizationsOutputFilePath;
		if (infoCustomizationsOutputFilePath != null) {
			File file = new File(infoCustomizationsOutputFilePath);
			if (!file.exists()) {
				try {
					infoCustomizations.saveToFile(file,
							ReflectionUIUtils.getDebugLogListener(SwingCustomizer.this.getReflectionUI()));
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			}
		}
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

	public InfoCustomizations getInfoCustomizations() {
		return infoCustomizations;
	}

	public String getInfoCustomizationsOutputFilePath() {
		return infoCustomizationsOutputFilePath;
	}

	public CustomizationTools getCustomizationTools() {
		return customizationTools;
	}

	public CustomizationOptions getCustomizationOptions() {
		return customizationOptions;
	}

	public CustomizationController getCustomizationController() {
		return customizationController;
	}

	@Override
	public JPanel createForm(Object object, IInfoFilter infoFilter) {
		final JPanel result = super.createForm(object, infoFilter);
		if (isCustomizationsEditorEnabled()) {
			result.addAncestorListener(new AncestorListener() {

				@Override
				public void ancestorRemoved(AncestorEvent event) {
					getCustomizationController().formRemoved(result);
				}

				@Override
				public void ancestorMoved(AncestorEvent event) {
				}

				@Override
				public void ancestorAdded(AncestorEvent event) {
					getCustomizationController().formAdded(result);
				}
			});
		}
		return result;
	}

	public boolean isCustomizationsEditorEnabled() {
		return infoCustomizationsOutputFilePath != null;
	}

	@Override
	public void layoutFormControls(Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
			Container container) {
		JPanel form;
		if (SwingRendererUtils.isForm(container, this)) {
			form = (JPanel) container;
		} else {
			form = SwingRendererUtils.findParentForm(container, this);
		}
		if (form == null) {
			throw new ReflectionUIError();
		}
		Object object = getObjectByForm().get(form);
		if (areCustomizationsEditable(object)) {
			container.setLayout(new BorderLayout());
			JPanel newContainer = new JPanel();
			{
				container.add(newContainer, BorderLayout.CENTER);
				Border newContainerBporder;
				{
					int borderThickness = 2;
					newContainerBporder = BorderFactory
							.createLineBorder(getCustomizationTools().getToolsForegroundColor(), borderThickness);
					newContainerBporder = BorderFactory.createCompoundBorder(newContainerBporder, BorderFactory
							.createLineBorder(getCustomizationTools().getToolsBackgroundColor(), borderThickness));
					newContainerBporder = BorderFactory.createCompoundBorder(newContainerBporder, BorderFactory
							.createLineBorder(getCustomizationTools().getToolsForegroundColor(), borderThickness));
					newContainer.setBorder(newContainerBporder);
				}
				super.layoutFormControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
						newContainer);
			}
			JPanel typeCustomizationsControl = new JPanel();
			{
				typeCustomizationsControl.setLayout(new BorderLayout());
				typeCustomizationsControl.add(customizationTools.makeButtonForTypeInfo(object), BorderLayout.CENTER);
				container.add(SwingRendererUtils.flowInLayout(typeCustomizationsControl, GridBagConstraints.CENTER),
						BorderLayout.NORTH);
			}
		} else {
			super.layoutFormControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
					container);
		}

	}

	@Override
	public CustomizingFieldControlPlaceHolder createFieldControlPlaceHolder(JPanel form, IFieldInfo field) {
		return new CustomizingFieldControlPlaceHolder(this, form, field);
	}

	@Override
	public CustomizingMethodControlPlaceHolder createMethodControlPlaceHolder(JPanel form, IMethodInfo method) {
		return new CustomizingMethodControlPlaceHolder(this, form, method);
	}

	public ImageIcon getCustomizationsIcon() {
		return SwingRendererUtils.CUSTOMIZATION_ICON;
	}

	public boolean areCustomizationsEditable(Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (!isCustomizationsEditorEnabled()) {
			return false;
		}
		if (!customizationOptions.isInEditMode()) {
			return false;
		}
		if (!infoCustomizations
				.equals(type.getSpecificProperties().get(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY))) {
			return false;
		}
		if (Boolean.TRUE.equals(type.getSpecificProperties().get(CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY))) {
			return false;
		}
		return true;
	}
}
