package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizations;
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

	public SwingCustomizer(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations,
			String infoCustomizationsOutputFilePath) {
		super(reflectionUI);
		this.customizationTools = createCustomizationTools();
		this.customizationOptions = initializeCustomizationOptions();
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

	protected CustomizationOptions initializeCustomizationOptions() {
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

	@Override
	public void fillForm(JPanel form) {
		Object object = getObjectByForm().get(form);
		if (areCustomizationsEditable(object)) {
			JPanel mainCustomizationsControl = new JPanel();
			mainCustomizationsControl.setLayout(new BorderLayout());
			mainCustomizationsControl.add(customizationTools.makeCustomizerForTypeInfo(object), BorderLayout.CENTER);
			mainCustomizationsControl.add(customizationTools.makeSaveControl(), BorderLayout.EAST);
			mainCustomizationsControl.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(), 0, 0, 0));
			form.add(SwingRendererUtils.flowInLayout(mainCustomizationsControl, GridBagConstraints.CENTER),
					BorderLayout.NORTH);
		}
		super.fillForm(form);
	}

	@Override
	public FieldControlPlaceHolder createFieldControlPlaceHolder(JPanel form, IFieldInfo field) {
		return new FieldControlPlaceHolder(form, field) {
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
					infoCustomizationsComponent = customizationTools.makeCustomizerForFieldInfo(this);
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
		return new MethodControlPlaceHolder(form, method) {
			private static final long serialVersionUID = 1L;
			protected Component infoCustomizationsComponent;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return result;
				}
				if (infoCustomizationsComponent != null) {
					result.width += infoCustomizationsComponent.getWidth();
				}
				return result;
			}

			@Override
			public void refreshUI(boolean recreate) {
				if (areCustomizationsEditable(getObject())) {
					refreshInfoCustomizationsControl();
				}
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (infoCustomizationsComponent == null) {
					infoCustomizationsComponent = customizationTools.makeCustomizerForMethodInfo(this);
					add(infoCustomizationsComponent, BorderLayout.WEST);
					SwingRendererUtils.handleComponentSizeChange(this);
				} else {
					remove(infoCustomizationsComponent);
					infoCustomizationsComponent = null;
					refreshInfoCustomizationsControl();
				}
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
		if (Boolean.TRUE.equals(customizationOptions.areHiddenFor(type.getName()))) {
			return false;
		}
		if (Boolean.TRUE.equals(type.getSpecificProperties().get(CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY))) {
			return false;
		}
		return true;
	}
}
