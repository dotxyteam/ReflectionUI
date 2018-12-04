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
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.renderer.CustomizedSwingRenderer;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
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
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.component.ControlPanel;

public class SwingCustomizer extends CustomizedSwingRenderer {

	public static void main(String[] args) throws Exception {
		Class<?> clazz = Object.class;
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instanciate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + MoreSystemProperties.describe();
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
		Object object = SwingCustomizer.getDefault().onTypeInstanciationRequest(null,
				SwingCustomizer.getDefault().getReflectionUI().getTypeInfo(new JavaTypeInfoSource(clazz, null)), null);
		if (object == null) {
			return;
		}
		SwingCustomizer.getDefault().openObjectFrame(object);
	}

	protected static SwingCustomizer defaultInstance;

	protected CustomizationTools customizationTools;
	protected CustomizationOptions customizationOptions;
	protected CustomizationController customizationController;
	protected String infoCustomizationsOutputFilePath;

	public static SwingCustomizer getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new SwingCustomizer(CustomizedUI.getDefault());
		}
		return defaultInstance;
	}

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
		this.customizationTools = createCustomizationTools();
		this.customizationOptions = createCustomizationOptions();
		this.customizationController = createCustomizationController();
	}

	public SwingCustomizer(CustomizedUI customizedUI) {
		this(customizedUI, SystemProperties.getDefaultInfoCustomizationsFilePath());
	}

	public String getInfoCustomizationsOutputFilePath() {
		return infoCustomizationsOutputFilePath;
	}

	public CustomizationController createCustomizationController() {
		return new CustomizationController(this);
	}

	public CustomizationOptions createCustomizationOptions() {
		return new CustomizationOptions(this);
	}

	public CustomizationTools createCustomizationTools() {
		return new CustomizationTools(this);
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
	public CustomizingForm createForm(Object object, IInfoFilter infoFilter) {
		return new CustomizingForm(this, object, infoFilter);
	}

	public boolean isCustomizationsEditorEnabled() {
		return (infoCustomizationsOutputFilePath != null) && !MoreSystemProperties.areCustomizationToolsDisabled();
	}

	public ImageIcon getCustomizationsIcon() {
		return SwingCustomizerUtils.CUSTOMIZATION_ICON;
	}

	public boolean areCustomizationsEditable(Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (!isCustomizationsEditorEnabled()) {
			return false;
		}
		if (!customizationOptions.isInEditMode()) {
			return false;
		}
		if (!getInfoCustomizations()
				.equals(type.getSpecificProperties().get(InfoCustomizations.CURRENT_CUSTOMIZATIONS_KEY))) {
			return false;
		}
		if (Boolean.TRUE.equals(type.getSpecificProperties().get(CUSTOMIZATIONS_FORBIDDEN_PROPERTY_KEY))) {
			return false;
		}
		return true;
	}

	public class CustomizingForm extends Form {
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

		public boolean isToolsAdded() {
			return toolsAdded;
		}

		@Override
		public void layoutMemberControls(
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
					super.layoutMemberControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
							newMembersPanel);
				}
				JPanel typeCustomizationsControl = new ControlPanel();
				{
					typeCustomizationsControl.setLayout(new BorderLayout());
					typeCustomizationsControl.add(customizationTools.makeButtonForTypeInfo(object),
							BorderLayout.CENTER);
					membersPanel.add(
							SwingRendererUtils.flowInLayout(typeCustomizationsControl, GridBagConstraints.CENTER),
							BorderLayout.NORTH);
				}
				toolsAdded = true;
			} else {
				super.layoutMemberControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory,
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
