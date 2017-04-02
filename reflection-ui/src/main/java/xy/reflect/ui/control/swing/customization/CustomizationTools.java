package xy.reflect.ui.control.swing.customization;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.StandardEditorBuilder;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.MethodControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.util.InfoCustomizations.AbstractInfoCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.AbstractMemberCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.type.util.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ResourcePath;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

public class CustomizationTools {

	protected final SwingCustomizer swingCustomizer;
	protected SwingRenderer customizationToolsRenderer;
	protected ReflectionUI customizationToolsUI;
	protected InfoCustomizations customizationToolsCustomizations;

	public CustomizationTools(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
		customizationToolsCustomizations = new InfoCustomizations();
		URL url = ReflectionUI.class.getResource("resource/customizations-tools.icu");
		try {
			File customizationsFile = FileUtils.getStreamAsFile(url.openStream());
			String customizationsFilePath = customizationsFile.getPath();
			customizationToolsCustomizations.loadFromFile(new File(customizationsFilePath));
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
		customizationToolsUI = createCustomizationToolsUI();
		customizationToolsRenderer = createCustomizationToolsRenderer();

	}

	public SwingRenderer getCustomizationToolsRenderer() {
		return customizationToolsRenderer;
	}

	public ReflectionUI getCustomizationToolsUI() {
		return customizationToolsUI;
	}

	public InfoCustomizations getCustomizationToolsCustomizations() {
		return customizationToolsCustomizations;
	}

	protected JButton createToolAccessButton(ImageIcon imageIcon) {
		final JButton result = new JButton(imageIcon);
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		return result;
	}

	protected SwingRenderer createCustomizationToolsRenderer() {
		if (SystemProperties.isInfoCustomizationToolsCustomizationAllowed()) {
			String customizationToolsCustomizationsOutputFilePath = System
					.getProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH);
			return new SwingCustomizer(customizationToolsUI, customizationToolsCustomizations,
					customizationToolsCustomizationsOutputFilePath) {

				@Override
				protected CustomizationTools createCustomizationTools() {
					return new CustomizationTools(this) {

						@Override
						protected SwingRenderer createCustomizationToolsRenderer() {
							return new SwingRenderer(this.customizationToolsUI);
						}

					};
				}

				@Override
				protected CustomizationOptions initializeCustomizationOptions() {
					return new CustomizationOptions(this);
				}

			};
		} else {
			return new SwingRenderer(customizationToolsUI);
		}
	}

	protected ReflectionUI createCustomizationToolsUI() {
		return new ReflectionUI() {

			ReflectionUI thisReflectionUI = this;

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				ITypeInfo result = super.getTypeInfo(typeSource);
				result = new TypeInfoProxyFactory() {
					@Override
					public String toString() {
						return CustomizationTools.class.getName() + TypeInfoProxyFactory.class.getSimpleName();
					}

					@Override
					protected List<IFieldInfo> getFields(ITypeInfo type) {
						if (type.getName().equals(TypeCustomization.class.getName())) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
							result.add(getIconImageFileField());
							return result;
						} else if (type.getName().equals(FieldCustomization.class.getName())) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
							result.add(getEmbeddedFormCreationField());
							return result;
						} else if (type.getName().equals(MethodCustomization.class.getName())) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
							result.add(getIconImageFileField());
							return result;
						} else {
							return super.getFields(type);
						}
					}

					@Override
					protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
						if ((object instanceof AbstractMemberCustomization) && field.getName().equals("category")) {
							for (TypeCustomization tc : CustomizationTools.this.swingCustomizer.getInfoCustomizations()
									.getTypeCustomizations()) {
								for (FieldCustomization fc : tc.getFieldsCustomizations()) {
									if (fc == object) {
										List<CustomizationCategory> categories = tc.getMemberCategories();
										return categories.toArray(new CustomizationCategory[categories.size()]);
									}
								}
								for (MethodCustomization mc : tc.getMethodsCustomizations()) {
									if (mc == object) {
										List<CustomizationCategory> categories = tc.getMemberCategories();
										return categories.toArray(new CustomizationCategory[categories.size()]);
									}
								}
							}
							throw new ReflectionUIError();
						} else {
							return super.getValueOptions(object, field, containingType);
						}
					}

				}.get(result);
				result = customizationToolsCustomizations.get(thisReflectionUI, result);
				return result;
			}

		};
	}

	protected IFieldInfo getEmbeddedFormCreationField() {
		return new IFieldInfo() {

			@Override
			public String getName() {
				return "expandSubForm";
			}

			@Override
			public String getCaption() {
				return "Expand Sub-form";
			}

			@Override
			public String getOnlineHelp() {
				return null;
			}

			@Override
			public String getNullValueLabel() {
				return null;
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return Collections.emptyMap();
			}

			@Override
			public ITypeInfo getType() {
				return new BooleanTypeInfo(customizationToolsRenderer.getReflectionUI(), boolean.class);
			}

			@Override
			public ITypeInfoProxyFactory getTypeSpecificities() {
				return null;
			}

			@Override
			public Object getValue(Object object) {
				FieldCustomization f = (FieldCustomization) object;
				return DesktopSpecificProperty
						.isSubFormExpanded(DesktopSpecificProperty.accessCustomizationsProperties(f));
			}

			@Override
			public void setValue(Object object, Object value) {
				FieldCustomization f = (FieldCustomization) object;
				DesktopSpecificProperty.setSubFormExpanded(DesktopSpecificProperty.accessCustomizationsProperties(f),
						(Boolean) value);
			}

			@Override
			public Runnable getCustomUndoUpdateJob(Object object, Object value) {
				return null;
			}

			@Override
			public Object[] getValueOptions(Object object) {
				return null;
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
			public ValueReturnMode getValueReturnMode() {
				return ValueReturnMode.CALCULATED;
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}

			@Override
			public String toString() {
				return getCaption();
			}

		};
	}

	protected IFieldInfo getIconImageFileField() {
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
			public String getNullValueLabel() {
				return null;
			}

			@Override
			public String getOnlineHelp() {
				return null;
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return Collections.emptyMap();
			}

			@Override
			public ITypeInfo getType() {
				return customizationToolsRenderer.getReflectionUI()
						.getTypeInfo(new JavaTypeInfoSource(ResourcePath.class));
			}

			@Override
			public ITypeInfoProxyFactory getTypeSpecificities() {
				return null;
			}

			@Override
			public Object[] getValueOptions(Object object) {
				return null;
			}

			@Override
			public Object getValue(Object object) {
				AbstractInfoCustomization c = (AbstractInfoCustomization) object;
				String pathSpecification = DesktopSpecificProperty
						.getIconImageFilePath(DesktopSpecificProperty.accessCustomizationsProperties(c));
				if (pathSpecification == null) {
					pathSpecification = "";
				}
				return new ResourcePath(pathSpecification);
			}

			@Override
			public void setValue(Object object, Object value) {
				String pathSpecification = ((ResourcePath) value).getSpecification();
				if (pathSpecification.equals("")) {
					pathSpecification = null;
				}
				AbstractInfoCustomization c = (AbstractInfoCustomization) object;
				DesktopSpecificProperty.setIconImageFilePath(DesktopSpecificProperty.accessCustomizationsProperties(c),
						pathSpecification);
			}

			@Override
			public Runnable getCustomUndoUpdateJob(Object object, Object value) {
				return null;
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
			public ValueReturnMode getValueReturnMode() {
				return ValueReturnMode.CALCULATED;
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}

			@Override
			public String toString() {
				return getCaption();
			}
		};
	}

	public JButton createSaveControl() {
		final JButton result = createToolAccessButton(SwingRendererUtils.SAVE_ALL_ICON);
		result.setToolTipText(customizationToolsRenderer.prepareStringToDisplay("Save all the customizations"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final File file = new File(
						CustomizationTools.this.swingCustomizer.getInfoCustomizationsOutputFilePath());
				try {
					CustomizationTools.this.swingCustomizer.getInfoCustomizations().saveToFile(file);
				} catch (IOException e1) {
					customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, e1);
				}
			}
		});
		return result;
	}

	public Component createTypeInfoCustomizer(final InfoCustomizations infoCustomizations, final Object object) {
		final ITypeInfo customizedType = this.swingCustomizer.getReflectionUI()
				.getTypeInfo(this.swingCustomizer.getReflectionUI().getTypeInfoSource(object));
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		result.setToolTipText(
				customizationToolsRenderer.prepareStringToDisplay(customizedType.getName() + " (Customize Display)"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Type Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(result, infoCustomizations, customizedType);
					}
				});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Refresh")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								updateUI(result);
							}
						});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Lock")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								hideCustomizationTools(result, customizedType.getName());
							}
						});

				showMenu(popupMenu, result);
			}
		});
		return result;
	}

	protected void hideCustomizationTools(Component activatorComponent, String typeName) {
		this.swingCustomizer.getCustomizationOptions().hideFor(typeName);
	}

	protected void openTypeCustomizationDialog(Component activatorComponent, InfoCustomizations infoCustomizations,
			ITypeInfo customizedType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		updateTypeCustomization(t, customizedType);
		openCustomizationEditor(activatorComponent, t, t.getTypeName());
	}

	protected void updateTypeCustomization(TypeCustomization t, ITypeInfo customizedType) {
		for (IFieldInfo field : customizedType.getFields()) {
			InfoCustomizations.getFieldCustomization(t, field.getName(), true);
		}
		for (IMethodInfo method : customizedType.getMethods()) {
			String methodSignature = ReflectionUIUtils.getMethodSignature(method);
			InfoCustomizations.getMethodCustomization(t, methodSignature, true);
			MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
			updateMethodCustomization(mc, method);
		}
		for (IMethodInfo ctor : customizedType.getConstructors()) {
			String methodSignature = ReflectionUIUtils.getMethodSignature(ctor);
			InfoCustomizations.getMethodCustomization(t, methodSignature, true);
			MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
			updateMethodCustomization(mc, ctor);
		}
	}

	protected void openCustomizationEditor(final Component activatorComponent, final Object customization,
			final String impactedTypeName) {
		AbstractEditorBuilder dialogBuilder = new AbstractEditorBuilder() {

			ModificationStack dummyParentModificationStack = new ModificationStack(null);

			@Override
			public Object getInitialObjectValue() {
				return customization;
			}
			
			@Override
			public boolean isObjectValueNullable() {
				return false;
			}

			@Override
			public boolean isObjectFormExpanded() {
				return true;
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return customizationToolsRenderer;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return ValueReturnMode.DIRECT_OR_PROXY;
			}

			@Override
			public String getEditorTitle() {
				return customizationToolsRenderer.getObjectTitle(customization);
			}

			@Override
			public Component getOwnerComponent() {
				return activatorComponent;
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return null;
			}

			@Override
			public IInfo getCumulatedModificationsTarget() {
				return null;
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				return IInfoFilter.NO_FILTER;
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return null;
			}

			@Override
			public ModificationStack getParentModificationStack() {
				return dummyParentModificationStack;
			}

			@Override
			public IModification createCommitModification(Object newObjectValue) {
				return null;
			}

			@Override
			public boolean canCommit() {
				return false;
			}

		};
		dialogBuilder.showDialog();
		if (dialogBuilder.isParentModificationStackImpacted()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateUI(activatorComponent);
				}
			});
		}
	}

	protected IModificationListener getCustomizedWindowsReloadingAdviser(final Component ownerComponent) {
		return new IModificationListener() {

			@Override
			public void handleUdno(IModification undoModification) {
			}

			@Override
			public void handleRedo(IModification modification) {
			}

			@Override
			public void handleInvalidate() {
			}

			@Override
			public void handleInvalidationCleared() {
			}

			@Override
			public void handlePush(IModification modification) {
				if (modification != null) {
					boolean showReloadWarning = false;
					IInfo modifTarget = modification.getTarget();
					if (modifTarget instanceof IFieldInfo) {
						IFieldInfo field = (IFieldInfo) modifTarget;
						if (field.getName().equals("undoManagementHidden")) {
							showReloadWarning = true;
						}
						if (field.getName().equals(getIconImageFileField().getName())) {
							showReloadWarning = true;
						}
						if (field.getName().equals("validating")) {
							showReloadWarning = true;
						}
						if (field.getName().equals("onlineHelp")) {
							showReloadWarning = true;
						}
					}
					if (showReloadWarning) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								customizationToolsRenderer.openInformationDialog(ownerComponent,
										"You must reload the customized windows\nto view all this change effects.",
										"Information",
										CustomizationTools.this.swingCustomizer.getCustomizationsIcon().getImage());
							}
						});
					}
				}
			}
		};
	}

	public Component createFieldInfoCustomizer(final InfoCustomizations infoCustomizations,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		SwingRendererUtils.setMultilineToolTipText(result, customizationToolsRenderer
				.prepareStringToDisplay(fieldControlPlaceHolder.getField().getName() + " (Customize Display)"));
		result.addActionListener(new ActionListener() {

			private ITypeInfo getParentFormObjectCustomizedType() {
				return CustomizationTools.this.swingCustomizer.getReflectionUI()
						.getTypeInfo(CustomizationTools.this.swingCustomizer.getReflectionUI()
								.getTypeInfoSource(fieldControlPlaceHolder.getObject()));
			}

			private String getFieldName() {
				return fieldControlPlaceHolder.getField().getName();
			}

			private ITypeInfo getFieldType() {
				return fieldControlPlaceHolder.getControlData().getType();
			}

			private FieldCustomization getFieldCustomization() {
				TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations,
						getParentFormObjectCustomizedType().getName(), true);
				FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, getFieldName(), true);
				return fc;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Hide")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								hideField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName());
							}
						});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Up")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(),
										-1);
							}
						});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Down")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(), 1);
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move To Top")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(),
								Short.MIN_VALUE);
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move To Bottom")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(),
								Short.MAX_VALUE);
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Type Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(fieldControlPlaceHolder,
								getFieldCustomization().getSpecificTypeCustomizations(), getFieldType());
					}
				});
				if (getFieldType() instanceof IListTypeInfo) {
					popupMenu.add(createListInfoCustomizer(getFieldCustomization().getSpecificTypeCustomizations(),
							fieldControlPlaceHolder, (IListTypeInfo) getFieldType()));
				}
				if (getFieldType() instanceof IEnumerationTypeInfo) {
					popupMenu.add(createEnumerationCustomizer(fieldControlPlaceHolder,
							getFieldCustomization().getSpecificTypeCustomizations(),
							(IEnumerationTypeInfo) getFieldType()));
				}
				if (swingCustomizer.getCustomizationOptions().areFieldSharedTypeOptionsDisplayed()) {
					final JMenu sharedTypeInfoSubMenu = new JMenu(
							CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Shared"));
					popupMenu.add(sharedTypeInfoSubMenu);
					sharedTypeInfoSubMenu.add(new AbstractAction(
							CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Type Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openTypeCustomizationDialog(fieldControlPlaceHolder, infoCustomizations, getFieldType());
						}
					});
					if (getFieldType() instanceof IListTypeInfo) {
						sharedTypeInfoSubMenu.add(createListInfoCustomizer(infoCustomizations, fieldControlPlaceHolder,
								(IListTypeInfo) getFieldType()));
					}
					if (getFieldType() instanceof IEnumerationTypeInfo) {
						sharedTypeInfoSubMenu.add(createEnumerationCustomizer(fieldControlPlaceHolder,
								infoCustomizations, (IEnumerationTypeInfo) getFieldType()));
					}
				}
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("More Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openFieldCutomizationDialog(fieldControlPlaceHolder, infoCustomizations,
								getParentFormObjectCustomizedType(), getFieldName());
					}
				});
				showMenu(popupMenu, result);
			}

		});
		return result;
	}

	protected JMenuItem createListInfoCustomizer(final InfoCustomizations infoCustomizations,
			final FieldControlPlaceHolder fieldControlPlaceHolder, final IListTypeInfo customizedListType) {
		JMenu result = new JMenu(this.customizationToolsRenderer.prepareStringToDisplay("List"));
		{
			result.add(new AbstractAction(this.customizationToolsRenderer.prepareStringToDisplay("Move Columns...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListColumnsOrderDialog(fieldControlPlaceHolder, infoCustomizations, customizedListType);
				}
			});
			if (customizedListType.getItemType() != null) {
				result.add(new AbstractAction(this.customizationToolsRenderer.prepareStringToDisplay("Item Type...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(fieldControlPlaceHolder, infoCustomizations,
								customizedListType.getItemType());
					}
				});
			}
			result.add(new AbstractAction(this.swingCustomizer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListCutomizationDialog(fieldControlPlaceHolder, infoCustomizations, customizedListType);
				}
			});
		}
		return result;
	}

	protected JMenuItem createEnumerationCustomizer(final FieldControlPlaceHolder fieldControlPlaceHolder,
			final InfoCustomizations infoCustomizations, final IEnumerationTypeInfo customizedEnumType) {
		JMenu result = new JMenu(this.customizationToolsRenderer.prepareStringToDisplay("Enumeration"));
		{
			result.add(new AbstractAction(this.customizationToolsRenderer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openEnumerationCutomizationDialog(fieldControlPlaceHolder, infoCustomizations, customizedEnumType);
				}
			});
		}
		return result;
	}

	protected void showMenu(JPopupMenu popupMenu, JButton source) {
		popupMenu.show(source, source.getWidth(), source.getHeight() / 2);
	}

	protected void hideMethod(Component activatorComponent, ITypeInfo customizedType, String methodSignature) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
		mc.setHidden(true);
		updateUI(activatorComponent);
	}

	protected void hideField(Component activatorComponent, ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		fc.setHidden(true);
		updateUI(activatorComponent);
	}

	protected void moveField(Component activatorComponent, ITypeInfo customizedType, String fieldName, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		try {
			tc.moveField(customizedType.getFields(), fieldName, offset);
		} catch (Throwable t) {
			this.customizationToolsRenderer.handleExceptionsFromDisplayedUI(activatorComponent, t);
		}
		updateUI(activatorComponent);
	}

	protected void moveMethod(Component activatorComponent, ITypeInfo customizedType, String methodSignature,
			int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		try {
			tc.moveMethod(customizedType.getMethods(), methodSignature, offset);
		} catch (Throwable t) {
			this.customizationToolsRenderer.handleExceptionsFromDisplayedUI(activatorComponent, t);
		}
		updateUI(activatorComponent);
	}

	@SuppressWarnings("unchecked")
	protected void openListColumnsOrderDialog(final Component activatorComponent, InfoCustomizations infoCustomizations,
			final IListTypeInfo customizedListType) {
		ITypeInfo customizedItemType = customizedListType.getItemType();
		String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
		ListCustomization lc = InfoCustomizations.getListCustomization(infoCustomizations, customizedListType.getName(),
				itemTypeName, true);
		IListStructuralInfo customizedListStructure = customizedListType.getStructuralInfo();
		List<ColumnOrderItem> columnOrder = new ArrayList<ColumnOrderItem>();
		for (final IColumnInfo c : customizedListStructure.getColumns()) {
			ColumnOrderItem orderItem = new ColumnOrderItem(c);
			columnOrder.add(orderItem);
		}
		StandardEditorBuilder dialogStatus = customizationToolsRenderer.openObjectDialog(activatorComponent,
				columnOrder, "Columns Order", this.swingCustomizer.getCustomizationsIcon().getImage(), true, true);
		if (dialogStatus.wasOkPressed()) {
			columnOrder = (List<ColumnOrderItem>) dialogStatus.getCurrentObjectValue();
			List<String> newOrder = new ArrayList<String>();
			for (ColumnOrderItem item : columnOrder) {
				newOrder.add(item.getColumnInfo().getName());
			}
			lc.setColumnsCustomOrder(newOrder);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateUI(activatorComponent);
				}
			});
		}
	}

	protected void openEnumerationCutomizationDialog(Component activatorComponent,
			InfoCustomizations infoCustomizations, final IEnumerationTypeInfo customizedEnumType) {
		EnumerationCustomization ec = InfoCustomizations.getEnumerationCustomization(infoCustomizations,
				customizedEnumType.getName(), true);
		updateEnumerationCustomization(ec, customizedEnumType);
		openCustomizationEditor(activatorComponent, ec, customizedEnumType.getName());
	}

	protected void updateEnumerationCustomization(EnumerationCustomization ec,
			IEnumerationTypeInfo customizedEnumType) {
		for (Object item : customizedEnumType.getPossibleValues()) {
			IEnumerationItemInfo itemInfo = customizedEnumType.getValueInfo(item);
			InfoCustomizations.getEnumerationItemCustomization(ec, itemInfo.getName(), true);
		}
	}

	protected void openListCutomizationDialog(Component activatorComponent, InfoCustomizations infoCustomizations,
			final IListTypeInfo customizedListType) {
		ITypeInfo customizedItemType = customizedListType.getItemType();
		String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
		ListCustomization lc = InfoCustomizations.getListCustomization(infoCustomizations, customizedListType.getName(),
				itemTypeName, true);
		updateListCustomization(lc, customizedListType);
		openCustomizationEditor(activatorComponent, lc, customizedListType.getName());
	}

	protected void updateListCustomization(ListCustomization lc, IListTypeInfo customizedListType) {
		for (IColumnInfo column : customizedListType.getStructuralInfo().getColumns()) {
			InfoCustomizations.getColumnCustomization(lc, column.getName(), true);
		}
	}

	protected void openFieldCutomizationDialog(Component activatorComponent, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		openCustomizationEditor(activatorComponent, fc, customizedType.getName());
	}

	protected void openMethodCutomizationDialog(Component activatorComponent, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String methodSignature) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
		openCustomizationEditor(activatorComponent, mc, customizedType.getName());
	}

	protected void updateMethodCustomization(MethodCustomization mc, IMethodInfo customizedMethod) {
		for (IParameterInfo param : customizedMethod.getParameters()) {
			InfoCustomizations.getParameterCustomization(mc, param.getName(), true);
		}
	}

	public Component createMethodInfoCustomizer(final InfoCustomizations infoCustomizations,
			final MethodControlPlaceHolder methodControlPlaceHolder) {
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		SwingRendererUtils.setMultilineToolTipText(result, customizationToolsRenderer.prepareStringToDisplay(
				ReflectionUIUtils.getMethodSignature(methodControlPlaceHolder.getMethod()) + " (Customize Display)"));
		result.addActionListener(new ActionListener() {

			private ITypeInfo getParentFormObjectCustomizedType() {
				return CustomizationTools.this.swingCustomizer.getReflectionUI()
						.getTypeInfo(CustomizationTools.this.swingCustomizer.getReflectionUI()
								.getTypeInfoSource(methodControlPlaceHolder.getObject()));
			}

			private String getMethodInfoSignature() {
				return ReflectionUIUtils.getMethodSignature(methodControlPlaceHolder.getMethod());
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Hide")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								hideMethod(result, getParentFormObjectCustomizedType(), getMethodInfoSignature());
							}
						});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Left")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveMethod(result, getParentFormObjectCustomizedType(), getMethodInfoSignature(), -1);
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Right")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveMethod(result, getParentFormObjectCustomizedType(), getMethodInfoSignature(), 1);
					}
				});
				final IMethodInfo customizedMethod = ReflectionUIUtils.findMethodBySignature(
						getParentFormObjectCustomizedType().getMethods(), getMethodInfoSignature());
				final ITypeInfo returnValueType = customizedMethod.getReturnValueType();
				if (returnValueType != null) {
					popupMenu.add(new AbstractAction(
							CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Method Return Type...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openTypeCustomizationDialog(result, infoCustomizations, returnValueType);
						}
					});
				}
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("More Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openMethodCutomizationDialog(result, infoCustomizations, getParentFormObjectCustomizedType(),
								getMethodInfoSignature());
					}
				});
				showMenu(popupMenu, result);
			}
		});
		return result;
	}

	protected void updateUI(Component customizedFormComponent) {
		JPanel form;
		if (SwingRendererUtils.isForm(customizedFormComponent, this.swingCustomizer)) {
			form = (JPanel) customizedFormComponent;
		} else {
			form = SwingRendererUtils.findParentForm(customizedFormComponent, this.swingCustomizer);
		}
		this.swingCustomizer.recreateFormContent(form);
		this.swingCustomizer.updateFormStatusBarInBackground(form);
	}

	protected class ColumnOrderItem {
		IColumnInfo columnInfo;

		public ColumnOrderItem(IColumnInfo columnInfo) {
			super();
			this.columnInfo = columnInfo;
		}

		public IColumnInfo getColumnInfo() {
			return columnInfo;
		}

		@Override
		public String toString() {
			return columnInfo.getCaption();
		}

	}

}