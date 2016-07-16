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
import xy.reflect.ui.SwingRenderer.SwingSpecificProperty;
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
			if (SystemProperties.isMetaInfoCustomizationDiscarded()) {
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
						result.add(getTypeIconImageFileField());
						return result;
					} else {
						return super.getFields(type);
					}
				}

			}.get(super.getTypeInfo(typeSource));
		}

	};
	protected InfoCustomizations infoCustomizations;
	protected ReflectionUI reflectionUI;
	protected String infoCustomizationsFilePath;

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
				customizationsUI.getObjectTitle(infoCustomizations), getCustomizationIcon().getImage());
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
				getCustomizationIcon());
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		final TypeCustomization t = infoCustomizations.getTypeCustomization(typeName, true);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (customizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(result, t,
						customizationsUI.getObjectTitle(t), getCustomizationIcon().getImage(), true)) {
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
		final JButton result = new JButton(getCustomizationIcon());
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
				customizationsUI.getObjectTitle(fc), getCustomizationIcon().getImage(), true)) {
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
				customizationsUI.getObjectTitle(mc), getCustomizationIcon().getImage(), true)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					update(customizedType.getName());
				}
			});
		}
	}

	public Component createMethodInfoCustomizer(final ITypeInfo customizedType, final String methodSignature) {
		final JButton result = new JButton(getCustomizationIcon());
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

	protected ImageIcon getCustomizationIcon() {
		return SwingRendererUtils.CUSTOMIZATION_ICON;
	}

	protected IFieldInfo getTypeIconImageFileField() {
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
				return new FileTypeInfo(customizationsUI);
			}

			@Override
			public Object getValue(Object object) {
				TypeCustomization t = (TypeCustomization) object;
				Map<String, Object> properties = t.getSpecificProperties();
				if (properties == null) {
					return new File("");
				}
				String filePath = (String) properties.get(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH);
				if (filePath == null) {
					return new File("");
				}
				String filePathKind = (String) properties
						.get(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND);
				if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(filePathKind)) {
					filePath = getClassPathResourcePrefix() + filePath;
				}
				return new File(filePath);
			}

			private String getClassPathResourcePrefix() {
				return "<class-path-resource> ";
			}

			@Override
			public Object[] getValueOptions(Object object) {
				return null;
			}

			@Override
			public void setValue(Object object, Object value) {
				TypeCustomization t = (TypeCustomization) object;
				File file = (File) value;
				String filePath = file.getPath();
				if (file == null) {
					filePath = null;
				} else {
					if (filePath.length() == 0) {
						filePath = null;
					}
				}
				String filePathKind = null;
				if (filePath != null) {
					if (filePath.startsWith(getClassPathResourcePrefix())) {
						filePath = filePath.substring(getClassPathResourcePrefix().length());
						filePathKind = SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE;
					} else {
						List<PathKindOption> pathKindOptions = getPathKindOptions(filePath);
						PathKindOption chosenPathKindOption;
						if (pathKindOptions.size() == 1) {
							chosenPathKindOption = pathKindOptions.get(0);
						} else {
							chosenPathKindOption = customizationsUI.getSwingRenderer().openSelectionDialog(null,
									pathKindOptions, null, "Choose an option", customizationsUI.getObjectTitle(t));
							if (chosenPathKindOption == null) {
								return;
							}
						}
						filePath = chosenPathKindOption.path;
						filePathKind = chosenPathKindOption.pathKind;
					}
				}
				Map<String, Object> properties = t.getSpecificProperties();
				if (properties == null) {
					properties = new HashMap<String, Object>();
					t.setSpecificProperties(properties);
				}
				properties.put(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH, filePath);
				properties.put(SwingRenderer.SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND, filePathKind);
			}

			private List<PathKindOption> getPathKindOptions(String filePath) {
				List<PathKindOption> result = new ArrayList<PathKindOption>();
				File file = new File(filePath);
				{
					File candidateResourceFile = new File(file.getAbsoluteFile().getPath());
					while (true) {
						File mostAncestorFile = candidateResourceFile.getParentFile();
						if (mostAncestorFile == null) {
							break;
						}
						while (mostAncestorFile.getParentFile() != null) {
							mostAncestorFile = mostAncestorFile.getParentFile();
						}
						candidateResourceFile = ReflectionUIUtils.relativizeFile(mostAncestorFile,
								candidateResourceFile);
						String candidateResourcePath = candidateResourceFile.getPath().replaceAll("\\\\", "/");
						URL resourceURL = getClass().getClassLoader().getResource(candidateResourcePath);
						if (resourceURL != null) {
							result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE,
									candidateResourcePath));
							break;
						}
					}
				}
				{
					File currentDir = new File(".");
					if (ReflectionUIUtils.isAncestor(currentDir, file)) {
						File relativeFile = ReflectionUIUtils.relativizeFile(currentDir, file);
						result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE,
								relativeFile.getPath()));
					}
				}
				{
					result.add(new PathKindOption(SwingSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE,
							file.getAbsolutePath()));
				}
				return result;
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

			class PathKindOption {
				String path;
				String pathKind;

				public PathKindOption(String pathKind, String path) {
					super();
					this.path = path;
					this.pathKind = pathKind;
				}

				@Override
				public String toString() {
					if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE.equals(pathKind)) {
						return "<absolute-file> " + path;
					} else if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_RELATIVE_FILE.equals(pathKind)) {
						return "<relative-file> " + path;
					} else if (SwingSpecificProperty.VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE.equals(pathKind)) {
						return getClassPathResourcePrefix() + path;
					} else {
						return "<illegal-path-kind: " + pathKind + "> " + path;
					}
				}
			}

		};
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
