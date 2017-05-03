package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
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
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

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
					infoCustomizations.saveToFile(file);
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
		return result;
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
				super.layoutFormControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
						newContainer);
			}
			JPanel mainCustomizationsControl = new JPanel();
			{
				mainCustomizationsControl.setLayout(new BorderLayout());
				mainCustomizationsControl.add(customizationTools.makeButtonForTypeInfo(object), BorderLayout.CENTER);
				mainCustomizationsControl.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(), 0, 0, 0));
				container.add(SwingRendererUtils.flowInLayout(mainCustomizationsControl, GridBagConstraints.CENTER),
						BorderLayout.NORTH);
			}
		} else {
			super.layoutFormControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
					container);
		}

	}

	@Override
	public FieldControlPlaceHolder createFieldControlPlaceHolder(JPanel form, IFieldInfo field) {
		return new FieldControlPlaceHolder(this, form, field) {
			private static final long serialVersionUID = 1L;
			protected Component infoCustomizationsComponent;

			@Override
			public void refreshUI(boolean recreate) {
				refreshInfoCustomizationsControl();
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (areCustomizationsEditable(getObject()) == (infoCustomizationsComponent != null)) {
					return;
				}
				if (infoCustomizationsComponent == null) {
					infoCustomizationsComponent = customizationTools.makeButtonForFieldInfo(this);
					add(infoCustomizationsComponent, BorderLayout.EAST);
					SwingRendererUtils.handleComponentSizeChange(this);
				} else {
					remove(infoCustomizationsComponent);
					infoCustomizationsComponent = null;
					refreshInfoCustomizationsControl();
				}
			}

		};
	}

	@Override
	public MethodControlPlaceHolder createMethodControlPlaceHolder(JPanel form, IMethodInfo method) {
		return new MethodControlPlaceHolder(this, form, method) {
			private static final long serialVersionUID = 1L;
			protected Component infoCustomizationsComponent;

			@Override
			public void refreshUI(boolean recreate) {
				if (areCustomizationsEditable(getObject())) {
					refreshInfoCustomizationsControl();
				}
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (infoCustomizationsComponent == null) {
					infoCustomizationsComponent = customizationTools.makeButtonForMethodInfo(this);
					add(infoCustomizationsComponent, BorderLayout.WEST);
					SwingRendererUtils.handleComponentSizeChange(this);
				} else {
					remove(infoCustomizationsComponent);
					infoCustomizationsComponent = null;
					refreshInfoCustomizationsControl();
				}
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				if (infoCustomizationsComponent != null) {
					result.width += infoCustomizationsComponent.getPreferredSize().width;
				}
				return result;
			}

		};
	}

	public ImageIcon getCustomizationsIcon() {
		return SwingRendererUtils.CUSTOMIZATION_ICON;
	}

	protected boolean areCustomizationToolsDisabled() {
		return !SystemProperties.areInfoCustomizationToolsAuthorized();
	}

	protected boolean areCustomizationsEditable(Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (areCustomizationToolsDisabled()) {
			return false;
		}
		if (!infoCustomizations
				.equals(type.getSpecificProperties().get(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY))) {
			return false;
		}
		if (infoCustomizationsOutputFilePath == null) {
			return false;
		}
		if (Boolean.TRUE.equals(customizationOptions.areCustomizationToolsHiddenFor(type.getName()))) {
			return false;
		}
		if (Boolean.TRUE.equals(type.getSpecificProperties().get(CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY))) {
			return false;
		}
		return true;
	}
}
