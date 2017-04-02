package xy.reflect.ui.control.swing.customization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.MethodControlPlaceHolder;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
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
			mainCustomizationsControl.add(customizationTools.createTypeInfoCustomizer(infoCustomizations, object),
					BorderLayout.CENTER);
			mainCustomizationsControl.add(customizationTools.createSaveControl(), BorderLayout.EAST);
			form.add(SwingRendererUtils.flowInLayout(mainCustomizationsControl, GridBagConstraints.CENTER),
					BorderLayout.NORTH);
			int spacing = SwingRendererUtils.getStandardCharacterWidth(form) * 2;
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
					infoCustomizationsComponent = customizationTools.createFieldInfoCustomizer(infoCustomizations,
							this);
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
			public void refreshUI(boolean recreate) {
				if (areCustomizationsEditable(getObject())) {
					refreshInfoCustomizationsControl();
				}
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (infoCustomizationsComponent == null) {
					infoCustomizationsComponent = customizationTools.createMethodInfoCustomizer(infoCustomizations,
							this);
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

	protected boolean areCustomizationsEditable(Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (!SystemProperties.areInfoCustomizationToolsAuthorized()) {
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
