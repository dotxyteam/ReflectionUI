package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.SwingRenderer;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.InfoProxyGenerator;
import xy.reflect.ui.info.type.util.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
public class InfoCustomizationsControls {

	protected ReflectionUI customizationsUI = new ReflectionUI() {

		String customizationsFilePath;
		ReflectionUI thisReflectionUI = this;

		@Override
		public String getInfoCustomizationsFilePath() {
			if ("true".equals(System.getProperty(SystemProperties.DISCARD_META_INFO_CUSTOMIZATIONS))) {
				return null;
			}
			if (customizationsFilePath == null) {
				URL url = ReflectionUI.class.getResource("resource/info-customizations-types.icu");
				try {
					File customizationsFile = ReflectionUIUtils.getStreamAsFile(url.openStream());
					customizationsFilePath = customizationsFile.getPath();
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			}
			return customizationsFilePath;
		}

		@Override
		protected SwingRenderer createSwingRenderer() {
			return new SwingRenderer(this) {

				@Override
				protected boolean areInfoCustomizationsControlsAuthorized() {
					return false;
				}

			};
		}

		@Override
		public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
			return new InfoProxyGenerator() {
				@Override
				protected List<IFieldInfo> getFields(ITypeInfo type) {
					if (type.getName().equals(TypeCustomization.class.getName())) {
						List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
						result.add(getIconImageFileField());
						return result;
					} else {
						return super.getFields(type);
					}
				}

			}.get(super.getTypeInfo(typeSource));
		}
		
		private IFieldInfo getIconImageFileField() {
			return new IFieldInfo() {

				@Override
				public String getName() {
					return "iconImageFile";
				}

				@Override
				public String getCaption() {
					return "Icon Image File";
				}

				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public Map<String, Object> getSpecificProperties() {
					return null;
				}

				@Override
				public ITypeInfo getType() {
					return new FileTypeInfo(thisReflectionUI);
				}

				@Override
				public Object getValue(Object object) {
					TypeCustomization t = (TypeCustomization) object;
					Map<String, Object> properties = t.getSpecificProperties();
					if (properties == null) {
						return new File("");
					}
					String filePath = (String) properties
							.get(SwingRenderer.SwingSpecificProperty.ICON_IMAGE_FILE_PATH);
					if (filePath == null) {
						return new File("");
					}
					return new File(filePath);
				}

				@Override
				public Object[] getValueOptions(Object object) {
					return null;
				}
				
				@Override
				public void setValue(Object object, Object value) {
					TypeCustomization t = (TypeCustomization) object;
					File file = (File) value;
					String filePath;
					if(file == null){
						filePath = null;
					}
					if(file.isAbsolute()){
						if(thisReflectionUI.getSwingRenderer().openQuestionDialog(null, "Make this path relative ?", thisReflectionUI.getObjectTitle(t))){
							file = ReflectionUIUtils.relativizeFile(new File("."), file);
						}						
					}
					filePath = file.getPath();
					if(filePath.length() == 0){
						filePath = null;
					}
					Map<String, Object> properties = t.getSpecificProperties();
					if(properties == null){
						properties = new HashMap<String, Object>();
						t.setSpecificProperties(properties);
					}
					properties.put(SwingRenderer.SwingSpecificProperty.ICON_IMAGE_FILE_PATH,
							filePath);
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public boolean isGetOnly() {
					return false;
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}

			};
		}


	};
	protected InfoCustomizations infoCustomizations;
	private ReflectionUI reflectionUI;
	private String infoCustomizationsFilePath;

	public InfoCustomizationsControls(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations,
			String infoCustomizationsFilePath) {
		this.reflectionUI = reflectionUI;
		this.infoCustomizations = infoCustomizations;
		this.infoCustomizationsFilePath = infoCustomizationsFilePath;
		File file = new File(reflectionUI.getInfoCustomizationsFilePath());
		if (!file.exists()) {
			try {
				infoCustomizations.saveToFile(file);
			} catch (IOException e) {
				throw new ReflectionUIError(e);
			}
		}

	}

	public void openInfoCustomizationsWindow(InfoCustomizations infoCustomizations) {
		customizationsUI.getSwingRenderer().openObjectFrame(infoCustomizations,
				customizationsUI.getObjectTitle(infoCustomizations), getMainImageIcon().getImage());
	}

	public JButton createSaveControl() {
		final File file = new File(infoCustomizationsFilePath);
		final JButton result = new JButton(SwingRendererUtils.SAVE_ICON);
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					infoCustomizations.saveToFile(file);
				} catch (IOException e1) {
					customizationsUI.getSwingRenderer().handleExceptionsFromDisplayedUI(result, e1);
				}
			}
		});
		return result;
	}

	public Component createTypeInfoCustomizer(final String typeName) {
		final JButton result = new JButton(customizationsUI.prepareStringToDisplay("Customizations..."),
				getMainImageIcon());
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		final TypeCustomization t = infoCustomizations.getTypeCustomization(typeName, true);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (customizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(result, t,
						customizationsUI.getObjectTitle(t), getMainImageIcon().getImage(), true)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							update(typeName);
						}
					});
				}
			}
		});
		return result;
	}

	public Component createFieldInfoCustomizer(final ITypeInfo customizedType, final String fieldName) {
		final JButton result = new JButton(getMainImageIcon());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result,
				customizationsUI.prepareStringToDisplay("Customize this field display"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Customize...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openFieldCutomizationDialog(result, customizedType, fieldName);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Up")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(customizedType, fieldName, -1);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Down")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(customizedType, fieldName, 1);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move To Top")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(customizedType, fieldName, Short.MIN_VALUE);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move To Bottom")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(customizedType, fieldName, Short.MAX_VALUE);
					}
				});
				popupMenu.show(result, result.getWidth(), result.getHeight());
			}
		});
		return result;
	}

	protected void moveField(ITypeInfo customizedType, String fieldName, int offset) {
		TypeCustomization tc = infoCustomizations.getTypeCustomization(customizedType.getName(), true);
		tc.moveField(customizedType.getFields(), fieldName, offset);
		update(customizedType.getName());
	}

	protected void moveMethod(ITypeInfo customizedType, String methodSignature, int offset) {
		TypeCustomization tc = infoCustomizations.getTypeCustomization(customizedType.getName(), true);
		tc.moveMethod(customizedType.getMethods(), methodSignature, offset);
		update(customizedType.getName());
	}

	protected void openFieldCutomizationDialog(Component activatorComponent, final ITypeInfo customoizedType,
			String fieldName) {
		FieldCustomization fc = infoCustomizations.getFieldCustomization(customoizedType.getName(), fieldName, true);
		if (customizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(activatorComponent, fc,
				customizationsUI.getObjectTitle(fc), getMainImageIcon().getImage(), true)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					update(customoizedType.getName());
				}
			});
		}
	}

	protected void openMethodCutomizationDialog(Component activatorComponent, final ITypeInfo customizedType,
			String methodSignature) {
		MethodCustomization mc = infoCustomizations.getMethodCustomization(customizedType.getName(), methodSignature,
				true);
		if (customizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(activatorComponent, mc,
				customizationsUI.getObjectTitle(mc), getMainImageIcon().getImage(), true)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					update(customizedType.getName());
				}
			});
		}
	}

	public Component createMethodInfoCustomizer(final ITypeInfo customizedType, final String methodSignature) {
		final JButton result = new JButton(getMainImageIcon());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result,
				customizationsUI.prepareStringToDisplay("Customize this method display"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Customize...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openMethodCutomizationDialog(result, customizedType, methodSignature);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Left")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveMethod(customizedType, methodSignature, -1);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Right")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveMethod(customizedType, methodSignature, 1);
					}
				});
				popupMenu.show(result, result.getWidth(), result.getHeight());
			}
		});
		return result;
	}

	protected ImageIcon getMainImageIcon() {
		return SwingRendererUtils.CUSTOMIZATION_ICON;
	}

	protected void update(String typeName) {
		for (Object object : reflectionUI.getSwingRenderer().getObjectByForm().values()) {
			ITypeInfo objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
			if (typeName.equals(objectType.getName())) {
				for (JPanel form : reflectionUI.getSwingRenderer().getForms(object)) {
					reflectionUI.getSwingRenderer().recreateFormContent(form);
				}
			}
		}
		TypeCustomization t = infoCustomizations.getTypeCustomization(typeName);
		for (JPanel form : customizationsUI.getSwingRenderer().getForms(t)) {
			customizationsUI.getSwingRenderer().refreshAllFieldControls(form, false);
		}
		for (JPanel form : customizationsUI.getSwingRenderer().getForms(infoCustomizations)) {
			customizationsUI.getSwingRenderer().refreshAllFieldControls(form, false);
		}
	}
}
