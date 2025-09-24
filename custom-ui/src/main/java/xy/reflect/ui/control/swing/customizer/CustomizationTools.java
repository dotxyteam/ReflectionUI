
package xy.reflect.ui.control.swing.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin.AbstractConfiguration;
import xy.reflect.ui.control.plugin.ICustomizableFieldControlPlugin;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.control.swing.plugin.ImageViewPlugin;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractVirtualFieldDeclaration;
import xy.reflect.ui.info.custom.InfoCustomizations.ConversionMethodFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldTypeSpecificities;
import xy.reflect.ui.info.custom.InfoCustomizations.JavaClassBasedTypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.Mapping;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.VirtualFieldDeclaration;
import xy.reflect.ui.info.field.MembersCapsuleFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.CustomizedListStructuralInfo.SubListGroupField;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.ImageIcon;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This class inserts controls (buttons, context menus, ...) in the generated
 * UIs that allow to customize them in real-time.
 * 
 * @author olitank
 *
 */
public class CustomizationTools {

	protected final SwingCustomizer swingCustomizer;
	protected CustomizationToolsRenderer toolsRenderer;
	protected CustomizationToolsUI toolsUI;

	public CustomizationTools(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
		toolsUI = createToolsUI();
		toolsRenderer = createToolsRenderer();
	}

	public CustomizationToolsRenderer getToolsRenderer() {
		return toolsRenderer;
	}

	public CustomizationToolsUI getToolsUI() {
		return toolsUI;
	}

	protected JButton makeButton() {
		JButton result = new JButton(this.swingCustomizer.getCustomizationsIcon());
		result.setForeground(toolsRenderer.getToolsForegroundColor());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setFocusable(false);
		return result;
	}

	protected CustomizationToolsRenderer createToolsRenderer() {
		return new CustomizationToolsRenderer(toolsUI);
	}

	protected CustomizationToolsUI createToolsUI() {
		InfoCustomizations infoCustomizations = new InfoCustomizations();
		try {
			String customizationsFilePath = MoreSystemProperties.getInfoCustomizationToolsCustomizationsFilePath();
			if (customizationsFilePath != null) {
				infoCustomizations.loadFromFile(new File(customizationsFilePath),
						ReflectionUIUtils.getDebugLogListener(swingCustomizer.getReflectionUI()));
			} else {
				infoCustomizations.loadFromStream(
						ReflectionUI.class.getResourceAsStream("resource/customizations-tools.icu"),
						ReflectionUIUtils.getDebugLogListener(swingCustomizer.getReflectionUI()));
			}

		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
		return new CustomizationToolsUI(infoCustomizations, swingCustomizer);
	}

	public Component makeButtonForType(final Object object) {
		final ITypeInfo customizedType = this.swingCustomizer.getReflectionUI()
				.getTypeInfo(this.swingCustomizer.getReflectionUI().getTypeInfoSource(object));
		final JButton result = makeButton();
		result.setToolTipText(toolsRenderer.prepareMessageToDisplay(getCustomizationTitle(customizedType.getName())));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(createMenuItem(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Type Options (Shared)...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(result, swingCustomizer.getInfoCustomizations(), customizedType);
					}
				}));
				popupMenu.add(createMenuItem(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Add Virtual Text Field...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							String title = "New Virtual Text Field";

							String fieldName = toolsRenderer.openInputDialog(result, "", "Field Name", title);
							if (fieldName == null) {
								return;
							}
							checkNewFieldNameAvailability(fieldName, customizedType);
							String text = toolsRenderer.openInputDialog(result, "", "Text", title);
							if (text == null) {
								return;
							}

							VirtualFieldDeclaration fieldDeclaration = new VirtualFieldDeclaration();
							fieldDeclaration.setFieldName(fieldName);
							JavaClassBasedTypeInfoFinder typeFinder = new InfoCustomizations.JavaClassBasedTypeInfoFinder();
							typeFinder.setClassName(String.class.getName());
							fieldDeclaration.setFieldTypeFinder(typeFinder);

							final TypeCustomization tc = InfoCustomizations.getTypeCustomization(
									swingCustomizer.getInfoCustomizations(), customizedType.getName(), true);

							final List<AbstractVirtualFieldDeclaration> fieldDeclarations = new ArrayList<AbstractVirtualFieldDeclaration>(
									tc.getVirtualFieldDeclarations());
							fieldDeclarations.add(fieldDeclaration);

							final FieldCustomization fc = InfoCustomizations.getFieldCustomization(tc, fieldName, true);
							final TextualStorage nullReplacement = new TextualStorage();
							nullReplacement.save(text);

							ModificationStack modificationStack = swingCustomizer.getCustomizationController()
									.getModificationStack();
							modificationStack.insideComposite(title, UndoOrder.getNormal(), new Accessor<Boolean>() {
								@Override
								public Boolean get() {
									changeCustomizationFieldValue(tc, "virtualFieldDeclarations", fieldDeclarations);
									changeCustomizationFieldValue(fc, "nullReplacement", nullReplacement);
									changeCustomizationFieldValue(fc, "getOnlyForced", true);
									return true;
								}
							}, false);
						} catch (Throwable t) {
							toolsRenderer.handleException(result, t);
						}
					}
				}));
				popupMenu.add(createMenuItem(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Add Virtual Image Field...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							String title = "New Virtual Image Field";

							String fieldName = toolsRenderer.openInputDialog(result, "", "Field Name", title);
							if (fieldName == null) {
								return;
							}
							checkNewFieldNameAvailability(fieldName, customizedType);
							File imageFile = toolsRenderer.openInputDialog(result, new File(""), "Image File", title);
							if (imageFile == null) {
								return;
							}
							Image image = ImageIO.read(imageFile);

							VirtualFieldDeclaration fieldDeclaration = new VirtualFieldDeclaration();
							fieldDeclaration.setFieldName(fieldName);
							JavaClassBasedTypeInfoFinder typeFinder = new InfoCustomizations.JavaClassBasedTypeInfoFinder();
							typeFinder.setClassName(Image.class.getName());
							fieldDeclaration.setFieldTypeFinder(typeFinder);

							final TypeCustomization tc = InfoCustomizations.getTypeCustomization(
									swingCustomizer.getInfoCustomizations(), customizedType.getName(), true);

							final List<AbstractVirtualFieldDeclaration> fieldDeclarations = new ArrayList<AbstractVirtualFieldDeclaration>(
									tc.getVirtualFieldDeclarations());
							fieldDeclarations.add(fieldDeclaration);

							final FieldCustomization fc = InfoCustomizations.getFieldCustomization(tc, fieldName, true);
							final TextualStorage nullReplacement = new TextualStorage();
							Mapping storageMapping = new Mapping();
							{
								ConversionMethodFinder conversionMethodFinder = new ConversionMethodFinder();
								{
									conversionMethodFinder.setConversionClassName(ImageIcon.class.getName());
									conversionMethodFinder.setConversionMethodSignature(ReflectionUIUtils
											.buildMethodSignature(new DefaultConstructorInfo(ReflectionUI.getDefault(),
													ImageIcon.class.getConstructor(Image.class), ImageIcon.class)));
									storageMapping.setConversionMethodFinder(conversionMethodFinder);
								}
								ConversionMethodFinder reverseConversionMethodFinder = new ConversionMethodFinder();
								{
									reverseConversionMethodFinder.setConversionClassName(ImageIcon.class.getName());
									reverseConversionMethodFinder.setConversionMethodSignature(ReflectionUIUtils
											.buildMethodSignature(new DefaultMethodInfo(ReflectionUI.getDefault(),
													ImageIcon.class.getMethod("getImage"), ImageIcon.class)));
									storageMapping.setReverseConversionMethodFinder(reverseConversionMethodFinder);
								}
								nullReplacement.setPreConversion(storageMapping);
							}
							nullReplacement.save(image);

							final TypeCustomization ftc = InfoCustomizations.getTypeCustomization(
									fc.getSpecificTypeCustomizations(), typeFinder.getClassName(), true);
							final Map<String, Object> specificProperties = new HashMap<String, Object>(
									ftc.getSpecificProperties());
							IFieldControlPlugin imagePlugin = SwingRendererUtils.findFieldControlPlugin(swingCustomizer,
									new ImageViewPlugin().getIdentifier());
							ReflectionUIUtils.updateFieldControlPluginValues(specificProperties,
									imagePlugin.getIdentifier(),
									(imagePlugin instanceof ICustomizableFieldControlPlugin)
											? ((ICustomizableFieldControlPlugin) imagePlugin)
													.getDefaultControlCustomization()
											: null);

							ModificationStack modificationStack = swingCustomizer.getCustomizationController()
									.getModificationStack();
							modificationStack.insideComposite(title, UndoOrder.getNormal(), new Accessor<Boolean>() {
								@Override
								public Boolean get() {
									changeCustomizationFieldValue(tc, "virtualFieldDeclarations", fieldDeclarations);
									changeCustomizationFieldValue(fc, "nullReplacement", nullReplacement);
									changeCustomizationFieldValue(fc, "getOnlyForced", true);
									changeCustomizationFieldValue(ftc, "specificProperties", specificProperties);
									return true;
								}
							}, false);
						} catch (Throwable t) {
							toolsRenderer.handleException(result, t);
						}
					}
				}));
				popupMenu.add(createMenuItem(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Refresh")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								swingCustomizer.getCustomizedUI().getCustomizedTypeCache().clear();
								Form form = SwingRendererUtils.findParentForm(result, swingCustomizer);
								try {
									form.refresh(true);
									if (SwingRendererUtils.findAncestorForms(form, swingCustomizer).size() > 0) {
										SwingRendererUtils.updateWindowMenu(form, swingCustomizer);
									}
								} catch (Throwable t) {
									swingCustomizer.handleException(form, t);
								}
							}
						}));

				showMenu(popupMenu, result);
			}
		});
		return result;
	}

	protected void openTypeCustomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			ITypeInfo customizedType) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		fillTypeCustomization(tc, customizedType);
		openCustomizationEditor(customizerButton, tc);
	}

	protected void openCustomizationEditor(final JButton customizerButton, final Object customization) {
		StandardEditorBuilder dialogBuilder = new StandardEditorBuilder(toolsRenderer, customizerButton,
				customization) {

			@Override
			protected boolean isDialogCancellable() {
				return true;
			}

			@Override
			protected ModificationStack getParentModificationStack() {
				return swingCustomizer.getCustomizationController().getModificationStack();
			}

		};
		dialogBuilder.createAndShowDialog();
	}

	public ITypeInfo getContainingObjectCustomizedType(FieldControlPlaceHolder fieldControlPlaceHolder) {
		return CustomizationTools.this.swingCustomizer.getReflectionUI()
				.getTypeInfo(CustomizationTools.this.swingCustomizer.getReflectionUI()
						.getTypeInfoSource(fieldControlPlaceHolder.getObject()));
	}

	public ITypeInfo getContainingObjectCustomizedType(MethodControlPlaceHolder methodControlPlaceHolder) {
		return CustomizationTools.this.swingCustomizer.getReflectionUI()
				.getTypeInfo(CustomizationTools.this.swingCustomizer.getReflectionUI()
						.getTypeInfoSource(methodControlPlaceHolder.getObject()));
	}

	public ITypeInfo getFieldControlDataCustomizedType(FieldControlPlaceHolder fieldControlPlaceHolder) {
		IFieldControlData controlData = fieldControlPlaceHolder.getControlData();
		if (controlData == null) {
			return null;
		}
		return controlData.getType();
	}

	public FieldCustomization getFieldCustomization(String typeName, String fieldName,
			InfoCustomizations infoCustomizations) {
		FieldCustomization fc = InfoCustomizations
				.getFieldCustomization(getTypeCustomization(typeName, infoCustomizations), fieldName, true);
		return fc;
	}

	public MethodCustomization getMethodCustomization(String typeName, String methodSignature,
			InfoCustomizations infoCustomizations) {
		MethodCustomization mc = InfoCustomizations
				.getMethodCustomization(getTypeCustomization(typeName, infoCustomizations), methodSignature, true);
		return mc;
	}

	public FieldCustomization getFieldCustomization(FieldControlPlaceHolder fieldControlPlaceHolder,
			InfoCustomizations infoCustomizations) {
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(
				getContaingTypeCustomization(fieldControlPlaceHolder, infoCustomizations),
				fieldControlPlaceHolder.getField().getName(), true);
		return fc;
	}

	public MethodCustomization getMethodCustomization(MethodControlPlaceHolder methodControlPlaceHolder,
			InfoCustomizations infoCustomizations) {
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(
				getContaingTypeCustomization(methodControlPlaceHolder, infoCustomizations),
				methodControlPlaceHolder.getMethod().getSignature(), true);
		return mc;
	}

	public TypeCustomization getContaingTypeCustomization(FieldControlPlaceHolder fieldControlPlaceHolder,
			InfoCustomizations infoCustomizations) {
		String objectTypeName = getContainingObjectCustomizedType(fieldControlPlaceHolder).getName();
		return getTypeCustomization(objectTypeName, infoCustomizations);
	}

	public TypeCustomization getContaingTypeCustomization(MethodControlPlaceHolder methodControlPlaceHolder,
			InfoCustomizations infoCustomizations) {
		String objectTypeName = getContainingObjectCustomizedType(methodControlPlaceHolder).getName();
		return getTypeCustomization(objectTypeName, infoCustomizations);
	}

	public TypeCustomization getTypeCustomization(String objectTypeName, InfoCustomizations infoCustomizations) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, objectTypeName, true);
		return t;
	}

	public Component makeButtonForField(final FieldControlPlaceHolder fieldControlPlaceHolder) {
		final JButton result = makeButton();
		result.setToolTipText(SwingRendererUtils.adaptToolTipTextToMultiline(toolsRenderer
				.prepareMessageToDisplay(getCustomizationTitle(fieldControlPlaceHolder.getField().getName()))));
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(createMenuItem(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Hide")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									hideField(result, getContainingObjectCustomizedType(fieldControlPlaceHolder),
											fieldControlPlaceHolder.getField().getName());
								} catch (Throwable t) {
									toolsRenderer.handleException(result, t);
								}
							}
						}));
				for (JMenuItem menuItem : makeMenuItemsForFieldPosition(result, fieldControlPlaceHolder)) {
					popupMenu.add(menuItem);
				}

				for (JMenuItem menuItem : makeMenuItemsForFieldEncapsulation(result, fieldControlPlaceHolder)) {
					popupMenu.add(menuItem);
				}

				for (JMenuItem menuItem : makeMenuItemsForFieldType(result, fieldControlPlaceHolder, false,
						getFieldControlDataCustomizedType(fieldControlPlaceHolder))) {
					popupMenu.add(menuItem);
				}

				popupMenu.add(createMenuItem(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("More Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openFieldCutomizationDialog(result, swingCustomizer.getInfoCustomizations(),
								getContainingObjectCustomizedType(fieldControlPlaceHolder),
								fieldControlPlaceHolder.getField().getName());
					}
				}));
				showMenu(popupMenu, result);
			}

		});
		return result;
	}

	public Component makeButtonForTextualStorageDataField(
			final FieldControlPlaceHolder textualStorageDatafieldControlPlaceHolder) {
		final JButton result = makeButton();
		result.setToolTipText(SwingRendererUtils
				.adaptToolTipTextToMultiline(toolsRenderer.prepareMessageToDisplay("Editing Options")));
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();

				List<Form> descendantForms = SwingRendererUtils
						.findDescendantForms(textualStorageDatafieldControlPlaceHolder, swingCustomizer);
				if (descendantForms.size() >= 2) {
					FieldControlPlaceHolder changeableFieldControlPlaceHolder = descendantForms.get(1)
							.getFieldControlPlaceHolder("");

					FieldCustomization fieldCustomization = getFieldCustomization(changeableFieldControlPlaceHolder,
							swingCustomizer.getInfoCustomizations());
					FieldTypeSpecificities infoCustomizations = fieldCustomization.getSpecificTypeCustomizations();

					for (JMenuItem menuItem : makeMenuItemsForFieldControlPlugins(result,
							changeableFieldControlPlaceHolder, infoCustomizations)) {
						popupMenu.add(menuItem);
					}

					showMenu(popupMenu, result);
				}

			}

		});
		return result;
	}

	protected List<JMenuItem> makeMenuItemsForFieldPosition(final JButton customizerButton,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		final JMenu positionSubMenu = createMenu(toolsRenderer.prepareMessageToDisplay("Position"));
		positionSubMenu.add(createMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Preceding")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(customizerButton, getContainingObjectCustomizedType(fieldControlPlaceHolder),
									fieldControlPlaceHolder.getField().getName(), -1);
						} catch (Throwable t) {
							toolsRenderer.handleException(customizerButton, t);
						}
					}
				}));
		positionSubMenu.add(createMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Following")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(customizerButton, getContainingObjectCustomizedType(fieldControlPlaceHolder),
									fieldControlPlaceHolder.getField().getName(), 1);
						} catch (Throwable t) {
							toolsRenderer.handleException(customizerButton, t);
						}
					}
				}));
		positionSubMenu.add(createMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("First")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(customizerButton, getContainingObjectCustomizedType(fieldControlPlaceHolder),
									fieldControlPlaceHolder.getField().getName(), Short.MIN_VALUE);
						} catch (Throwable t) {
							toolsRenderer.handleException(customizerButton, t);
						}
					}
				}));
		positionSubMenu.add(createMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Last")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(customizerButton, getContainingObjectCustomizedType(fieldControlPlaceHolder),
									fieldControlPlaceHolder.getField().getName(), Short.MAX_VALUE);
						} catch (Throwable t) {
							toolsRenderer.handleException(customizerButton, t);
						}
					}
				}));
		return Collections.<JMenuItem>singletonList(positionSubMenu);
	}

	protected List<JMenuItem> makeMenuItemsForMethodPosition(final JButton customizerButton,
			final MethodControlPlaceHolder methodControlPlaceHolder) {
		final JMenu positionSubMenu = createMenu(toolsRenderer.prepareMessageToDisplay("Position"));
		positionSubMenu.add(createMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Preceding")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveMethod(customizerButton, getContainingObjectCustomizedType(methodControlPlaceHolder),
									methodControlPlaceHolder.getMethod().getSignature(), -1);
						} catch (Throwable t) {
							toolsRenderer.handleException(customizerButton, t);
						}
					}
				}));
		positionSubMenu.add(createMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Following")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveMethod(customizerButton, getContainingObjectCustomizedType(methodControlPlaceHolder),
									methodControlPlaceHolder.getMethod().getSignature(), 1);
						} catch (Throwable t) {
							toolsRenderer.handleException(customizerButton, t);
						}
					}
				}));
		return Collections.<JMenuItem>singletonList(positionSubMenu);
	}

	protected List<JMenuItem> makeMenuItemsForFieldEncapsulation(final JButton customizerButton,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		final ITypeInfo objectCustomizedType = getContainingObjectCustomizedType(fieldControlPlaceHolder);
		final IFieldInfo field = fieldControlPlaceHolder.getField();
		Listener<String> encapsulator = new Listener<String>() {

			@Override
			public void handle(final String capsuleFieldName) {
				String title = "Move '" + field.getCaption() + "' Into '" + capsuleFieldName + "'";
				swingCustomizer.getCustomizationController().getModificationStack().insideComposite(title,
						UndoOrder.getNormal(), new Accessor<Boolean>() {
							@Override
							public Boolean get() {
								String capsuleTypeName = MembersCapsuleFieldInfo.buildTypeName(capsuleFieldName,
										objectCustomizedType.getName());
								TypeCustomization srcTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), objectCustomizedType.getName(), true);
								TypeCustomization dstTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), capsuleTypeName, true);
								transferFieldCustomizationSettings(srcTc, dstTc, field.getName());
								FieldCustomization srcFc = InfoCustomizations.getFieldCustomization(srcTc,
										field.getName());
								if (ReflectionUIUtils.findInfoByName(objectCustomizedType.getFields(),
										capsuleFieldName) == null) {
									if (field.getCategory() != null) {
										FieldCustomization capsuleFc = InfoCustomizations.getFieldCustomization(srcTc,
												capsuleFieldName, true);
										for (int i = 0; i < srcTc.getMemberCategories().size(); i++) {
											CustomizationCategory customizationCategory = srcTc.getMemberCategories()
													.get(i);
											if (customizationCategory.getCaption()
													.equals(field.getCategory().getCaption())
													&& (i == field.getCategory().getPosition())) {
												changeCustomizationFieldValue(capsuleFc, "categoryName",
														customizationCategory.getName());
												break;
											}
										}
									}
								}
								changeCustomizationFieldValue(srcFc, "encapsulationFieldName", capsuleFieldName);
								return true;
							}
						}, false);
			}
		};
		Runnable decapsulator = new Runnable() {

			@Override
			public void run() {
				String title = "Move '" + field.getCaption() + "' Out";
				swingCustomizer.getCustomizationController().getModificationStack().insideComposite(title,
						UndoOrder.getNormal(), new Accessor<Boolean>() {
							@Override
							public Boolean get() {
								String capsuleContainerTypeName = MembersCapsuleFieldInfo
										.extractobjectTypeName(objectCustomizedType.getName());
								TypeCustomization srcTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), objectCustomizedType.getName(), true);
								TypeCustomization dstTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), capsuleContainerTypeName, true);
								FieldCustomization srcFc = InfoCustomizations.getFieldCustomization(srcTc,
										field.getName());
								removeCustomizationItem(srcTc, "fieldsCustomizations", srcFc);
								FieldCustomization dstFc = InfoCustomizations.getFieldCustomization(dstTc,
										field.getName());
								changeCustomizationFieldValue(dstFc, "encapsulationFieldName", null);
								return true;
							}
						}, false);
			}
		};
		return makeMenuItemsForMemberEncapsulation(customizerButton, field, objectCustomizedType, encapsulator,
				decapsulator);

	}

	protected List<JMenuItem> makeMenuItemsForMethodEncapsulation(final JButton customizerButton,
			final MethodControlPlaceHolder methodControlPlaceHolder) {
		final ITypeInfo objectCustomizedType = getContainingObjectCustomizedType(methodControlPlaceHolder);
		final IMethodInfo method = methodControlPlaceHolder.getMethod();
		Listener<String> encapsulator = new Listener<String>() {

			@Override
			public void handle(final String capsuleFieldName) {
				String title = "Move '" + method.getSignature() + "' Into '" + capsuleFieldName + "'";
				swingCustomizer.getCustomizationController().getModificationStack().insideComposite(title,
						UndoOrder.getNormal(), new Accessor<Boolean>() {
							@Override
							public Boolean get() {
								String capsuleTypeName = MembersCapsuleFieldInfo.buildTypeName(capsuleFieldName,
										objectCustomizedType.getName());
								TypeCustomization srcTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), objectCustomizedType.getName(), true);
								TypeCustomization dstTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), capsuleTypeName, true);
								transferMethodCustomizationSettings(srcTc, dstTc, method.getSignature());
								MethodCustomization srcMc = InfoCustomizations.getMethodCustomization(srcTc,
										method.getSignature());
								if (ReflectionUIUtils.findInfoByName(objectCustomizedType.getFields(),
										capsuleFieldName) == null) {
									if (method.getCategory() != null) {
										FieldCustomization capsuleFc = InfoCustomizations.getFieldCustomization(srcTc,
												capsuleFieldName, true);
										for (int i = 0; i < srcTc.getMemberCategories().size(); i++) {
											CustomizationCategory customizationCategory = srcTc.getMemberCategories()
													.get(i);
											if (customizationCategory.getCaption()
													.equals(method.getCategory().getCaption())
													&& (i == method.getCategory().getPosition())) {
												changeCustomizationFieldValue(capsuleFc, "categoryName",
														customizationCategory.getName());
												break;
											}
										}
									}
								}
								changeCustomizationFieldValue(srcMc, "encapsulationFieldName", capsuleFieldName);
								return true;
							}
						}, false);
			}
		};
		Runnable decapsulator = new Runnable() {

			@Override
			public void run() {
				String title = "Move '" + method.getSignature() + "' Out";
				swingCustomizer.getCustomizationController().getModificationStack().insideComposite(title,
						UndoOrder.getNormal(), new Accessor<Boolean>() {
							@Override
							public Boolean get() {
								String capsuleContainerTypeName = MembersCapsuleFieldInfo
										.extractobjectTypeName(objectCustomizedType.getName());
								TypeCustomization srcTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), objectCustomizedType.getName(), true);
								TypeCustomization dstTc = InfoCustomizations.getTypeCustomization(
										swingCustomizer.getInfoCustomizations(), capsuleContainerTypeName, true);
								MethodCustomization srcMc = InfoCustomizations.getMethodCustomization(srcTc,
										method.getSignature());
								removeCustomizationItem(srcTc, "methodsCustomizations", srcMc);
								MethodCustomization dstMc = InfoCustomizations.getMethodCustomization(dstTc,
										method.getSignature());
								changeCustomizationFieldValue(dstMc, "encapsulationFieldName", null);
								return true;
							}
						}, false);
			}
		};
		return makeMenuItemsForMemberEncapsulation(customizerButton, method, objectCustomizedType, encapsulator,
				decapsulator);
	}

	protected List<JMenuItem> makeMenuItemsForMemberEncapsulation(final JButton customizerButton, final IInfo member,
			final ITypeInfo objectCustomizedType, final Listener<String> encapsulator, final Runnable decapsulator) {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		final JMenu encapsulateSubMenu = createMenu(toolsRenderer.prepareMessageToDisplay("Move Into"));
		{
			result.add(encapsulateSubMenu);
			Set<String> capsuleNames = new TreeSet<String>();
			{
				for (IFieldInfo customizedField : objectCustomizedType.getFields()) {
					FieldCustomization fieldCustomization = getFieldCustomization(objectCustomizedType.getName(),
							customizedField.getName(), swingCustomizer.getInfoCustomizations());
					if (fieldCustomization == null) {
						continue;
					}
					if (fieldCustomization.getEncapsulationFieldName() == null) {
						continue;
					}
					if (member instanceof IFieldInfo) {
						if (member.getName().equals(fieldCustomization.getEncapsulationFieldName())) {
							continue;
						}
					}
					capsuleNames.add(fieldCustomization.getEncapsulationFieldName());
				}
				for (IMethodInfo customizedMethod : objectCustomizedType.getMethods()) {
					MethodCustomization methodCustomization = getMethodCustomization(objectCustomizedType.getName(),
							customizedMethod.getSignature(), swingCustomizer.getInfoCustomizations());
					if (methodCustomization == null) {
						continue;
					}
					if (methodCustomization.getEncapsulationFieldName() == null) {
						continue;
					}
					if (member instanceof IFieldInfo) {
						if (member.getName().equals(methodCustomization.getEncapsulationFieldName())) {
							continue;
						}
					}
					capsuleNames.add(methodCustomization.getEncapsulationFieldName());
				}
			}
			for (final String capsuleName : capsuleNames) {
				encapsulateSubMenu
						.add(createMenuItem(new AbstractAction(toolsRenderer.prepareMessageToDisplay(capsuleName)) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								encapsulator.handle(capsuleName);
							}
						}));
			}
			encapsulateSubMenu
					.add(createMenuItem(new AbstractAction(toolsRenderer.prepareMessageToDisplay("New Field...")) {

						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent ev) {
							final String newFieldName = toolsRenderer.openInputDialog(encapsulateSubMenu, "",
									"Field Name", "Move Member Into");
							if (newFieldName == null) {
								return;
							}
							try {
								checkNewFieldNameAvailability(newFieldName, objectCustomizedType);
							} catch (IllegalArgumentException e) {
								toolsRenderer.handleException(customizerButton, e);
								return;
							}
							final TypeCustomization typeCustomization = getTypeCustomization(
									objectCustomizedType.getName(), swingCustomizer.getInfoCustomizations());
							final FieldCustomization capsuleFieldCustomization = InfoCustomizations
									.getFieldCustomization(typeCustomization, newFieldName, true);
							swingCustomizer.getCustomizationController().getModificationStack().insideComposite(
									"Move Member Into '" + newFieldName + "'", UndoOrder.getNormal(),
									new Accessor<Boolean>() {
										@Override
										public Boolean get() {
											changeCustomizationFieldValue(capsuleFieldCustomization,
													"formControlEmbeddingForced", true);
											encapsulator.handle(newFieldName);
											return true;
										}
									}, false);
						}
					}));
		}

		final MembersCapsuleFieldInfo containingCapsuleField = (MembersCapsuleFieldInfo) member.getSpecificProperties()
				.get(MembersCapsuleFieldInfo.CONTAINING_CAPSULE_FIELD_PROPERTY_KEY);
		if (containingCapsuleField != null) {
			result.add(createMenuItem(new AbstractAction(toolsRenderer.prepareMessageToDisplay("Move Out")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					decapsulator.run();
				}
			}));
		}
		return result;
	}

	protected void transferFieldCustomizationSettings(TypeCustomization srcTc, TypeCustomization dstTc,
			String fieldName) {
		FieldCustomization srcFc = InfoCustomizations.getFieldCustomization(srcTc, fieldName, true);
		FieldCustomization dstFc = InfoCustomizations.getFieldCustomization(dstTc, fieldName, true);
		if (srcFc.isInitial()) {
			removeCustomizationItem(dstTc, "fieldsCustomizations", dstFc);
		} else {
			for (IFieldInfo field : ReflectionUI.getDefault()
					.getTypeInfo(new JavaTypeInfoSource(FieldCustomization.class, null)).getFields()) {
				if (field.isGetOnly()) {
					continue;
				}
				if (field.getName().equals("uniqueIdentifier")) {
					continue;
				}
				if (field.getName().startsWith("category")) {
					continue;
				}
				if (field.getName().equals("encapsulationFieldName")) {
					continue;
				}
				if (field.getName().equals("duplicateGenerated")) {
					continue;
				}
				if (field.getName().equals("customSetterSignature")) {
					continue;
				}
				if (field.getName().equals("valueOptionsFieldName")) {
					continue;
				}
				if (field.getName().equals("getterGenerated")) {
					continue;
				}
				if (field.getName().equals("setterGenerated")) {
					continue;
				}
				if (field.getName().equals("nullStatusFieldExported")) {
					continue;
				}
				if (field.getName().equals("importedNullStatusFieldName")) {
					continue;
				}
				if (field.getName().equals("importedNullStatusFieldName")) {
					continue;
				}
				changeCustomizationFieldValue(dstFc, field.getName(),
						IOUtils.copyThroughSerialization((Serializable) field.getValue(srcFc)));
			}
		}
	}

	protected void transferMethodCustomizationSettings(TypeCustomization srcTc, TypeCustomization dstTc,
			String methodSignature) {
		MethodCustomization srcMc = InfoCustomizations.getMethodCustomization(srcTc, methodSignature, true);
		MethodCustomization dstMc = InfoCustomizations.getMethodCustomization(dstTc, methodSignature, true);
		if (srcMc.isInitial()) {
			removeCustomizationItem(dstTc, "methodsCustomizations", dstMc);
		} else {
			for (IFieldInfo field : ReflectionUI.getDefault()
					.getTypeInfo(new JavaTypeInfoSource(MethodCustomization.class, null)).getFields()) {
				if (field.isGetOnly()) {
					continue;
				}
				if (field.getName().equals("uniqueIdentifier")) {
					continue;
				}
				if (field.getName().startsWith("category")) {
					continue;
				}
				if (field.getName().equals("encapsulationFieldName")) {
					continue;
				}
				if (field.getName().equals("duplicateGenerated")) {
					continue;
				}
				if (field.getName().equals("returnValueFieldGenerated")) {
					continue;
				}
				if (field.getName().equals("parameterizedFieldNames")) {
					continue;
				}
				if (field.getName().equals("menuLocation")) {
					continue;
				}
				if (field.getName().equals("transactionalRole")) {
					continue;
				}
				if (field.getName().equals("enablementStatusFieldName")) {
					continue;
				}
				changeCustomizationFieldValue(dstMc, field.getName(),
						IOUtils.copyThroughSerialization((Serializable) field.getValue(srcMc)));
			}
		}
	}

	protected void checkNewFieldNameAvailability(String newFieldName, ITypeInfo objectCustomizedType)
			throws IllegalArgumentException {
		for (IFieldInfo field : objectCustomizedType.getFields()) {
			if (newFieldName.equals(field.getName())) {
				throw new IllegalArgumentException("Field name already used: '" + newFieldName + "'");
			}
		}
	}

	protected JMenuItem makeCustomizableFieldControlPluginMenuItem(ICustomizableFieldControlPlugin plugin,
			final JButton customizerButton, final FieldControlPlaceHolder fieldControlPlaceHolder,
			final InfoCustomizations infoCustomizations) {
		return createMenuItem(
				new AbstractAction(toolsRenderer.prepareMessageToDisplay(plugin.getControlTitle() + " Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						TypeCustomization typeCustomization = InfoCustomizations.getTypeCustomization(
								infoCustomizations, fieldControlPlaceHolder.getControlData().getType().getName(), true);
						AbstractConfiguration controlConfiguration = null;
						try {
							controlConfiguration = plugin
									.getControlCustomization(typeCustomization.getSpecificProperties());
						} catch (Throwable t) {
							controlConfiguration = plugin.getDefaultControlCustomization();
						}
						StandardEditorBuilder status = toolsRenderer.openObjectDialog(customizerButton,
								controlConfiguration, null, null, true, true);
						if (status.isCancelled()) {
							return;
						}
						Map<String, Object> newSpecificProperties = plugin.storeControlCustomization(
								controlConfiguration, typeCustomization.getSpecificProperties());
						CustomizationTools.this.changeCustomizationFieldValue(typeCustomization, "specificProperties",
								newSpecificProperties);
					}

				});
	}

	protected List<JMenuItem> makeMenuItemsForFieldControlPlugins(final JButton customizerButton,
			final FieldControlPlaceHolder fieldControlPlaceHolder, InfoCustomizations infoCustomizations) {
		if (fieldControlPlaceHolder.getControlData().isFormControlMandatory()) {
			return Collections.emptyList();
		}
		if (ReflectionUIUtils.isFieldControlPluginManagementDisabled(
				fieldControlPlaceHolder.getControlData().getType().getSpecificProperties())) {
			return Collections.emptyList();
		}
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		final TypeCustomization typeCustomization = getTypeCustomization(
				fieldControlPlaceHolder.getControlData().getType().getName(), infoCustomizations);
		final IFieldControlPlugin currentPlugin = SwingRendererUtils.getCurrentFieldControlPlugin(swingCustomizer,
				typeCustomization.getSpecificProperties(), fieldControlPlaceHolder);
		if (currentPlugin != null) {
			if (currentPlugin instanceof ICustomizableFieldControlPlugin) {
				result.add(makeCustomizableFieldControlPluginMenuItem((ICustomizableFieldControlPlugin) currentPlugin,
						customizerButton, fieldControlPlaceHolder, infoCustomizations));
			}
		}

		List<IFieldControlPlugin> potentialFieldControlPlugins = new ArrayList<IFieldControlPlugin>();
		{
			for (IFieldControlPlugin plugin : swingCustomizer.getFieldControlPlugins()) {
				if (plugin.handles(fieldControlPlaceHolder)) {
					potentialFieldControlPlugins.add(plugin);
				}
			}
		}
		if (potentialFieldControlPlugins.size() > 0) {
			JMenu changeFieldControlPluginMenu = createMenu(toolsRenderer.prepareMessageToDisplay("Change Control"));
			result.add(changeFieldControlPluginMenu);
			changeFieldControlPluginMenu.add(createCheckBoxMenuItem(
					new AbstractAction(toolsRenderer.prepareMessageToDisplay("Default Control")) {

						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							Map<String, Object> specificProperties = typeCustomization.getSpecificProperties();
							specificProperties = new HashMap<String, Object>(specificProperties);
							ReflectionUIUtils.updateFieldControlPluginValues(specificProperties, null, null);
							changeCustomizationFieldValue(typeCustomization, "specificProperties", specificProperties);
						}
					}, currentPlugin == null));
			for (final IFieldControlPlugin plugin : potentialFieldControlPlugins) {
				changeFieldControlPluginMenu.add(createCheckBoxMenuItem(
						new AbstractAction(toolsRenderer.prepareMessageToDisplay(plugin.getControlTitle())) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								Map<String, Object> specificProperties = typeCustomization.getSpecificProperties();
								specificProperties = new HashMap<String, Object>(specificProperties);
								ReflectionUIUtils.updateFieldControlPluginValues(specificProperties,
										plugin.getIdentifier(),
										(plugin instanceof ICustomizableFieldControlPlugin)
												? ((ICustomizableFieldControlPlugin) plugin)
														.getDefaultControlCustomization()
												: null);
								changeCustomizationFieldValue(typeCustomization, "specificProperties",
										specificProperties);
							}
						}, (currentPlugin != null) && currentPlugin.getIdentifier().equals(plugin.getIdentifier())));
			}
		}
		return result;

	}

	protected List<JMenuItem> makeMenuItemsForFieldType(final JButton customizerButton,
			final FieldControlPlaceHolder fieldControlPlaceHolder, final boolean infoCustomizationsShared,
			final ITypeInfo fieldType) {
		final InfoCustomizations infoCustomizations;
		if (infoCustomizationsShared) {
			infoCustomizations = swingCustomizer.getInfoCustomizations();
		} else {
			FieldCustomization fieldCustomization = getFieldCustomization(fieldControlPlaceHolder,
					swingCustomizer.getInfoCustomizations());
			infoCustomizations = fieldCustomization.getSpecificTypeCustomizations();
		}

		List<JMenuItem> result = new ArrayList<JMenuItem>();

		for (JMenuItem menuItem : makeMenuItemsForFieldControlPlugins(customizerButton, fieldControlPlaceHolder,
				infoCustomizations)) {
			result.add(menuItem);
		}

		result.add(createMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Type Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(customizerButton, infoCustomizations, fieldType);
					}
				}));
		if (fieldType instanceof IListTypeInfo) {
			result.add(makeMenuItemForList(customizerButton, fieldControlPlaceHolder, infoCustomizationsShared,
					(IListTypeInfo) fieldType));
		}
		if (fieldType instanceof IEnumerationTypeInfo) {
			result.add(
					makeMenuItemForEnumeration(customizerButton, infoCustomizations, (IEnumerationTypeInfo) fieldType));
		}

		if (!infoCustomizationsShared) {
			if (swingCustomizer.getCustomizationOptions().areFieldSharedTypeOptionsDisplayed()) {
				final JMenu sharedTypeInfoSubMenu = createMenu(
						CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Shared"));
				result.add(sharedTypeInfoSubMenu);
				for (JMenuItem menuItem : makeMenuItemsForFieldType(customizerButton, fieldControlPlaceHolder, true,
						fieldType)) {
					sharedTypeInfoSubMenu.add(menuItem);
				}
			}
		}
		return result;

	}

	protected JMenuItem makeMenuItemForList(final JButton customizerButton,
			FieldControlPlaceHolder fieldControlPlaceHolder, final boolean infoCustomizationsShared,
			final IListTypeInfo customizedListType) {
		final InfoCustomizations infoCustomizations;
		if (infoCustomizationsShared) {
			infoCustomizations = swingCustomizer.getInfoCustomizations();
		} else {
			FieldCustomization fieldCustomization = getFieldCustomization(fieldControlPlaceHolder,
					swingCustomizer.getInfoCustomizations());
			infoCustomizations = fieldCustomization.getSpecificTypeCustomizations();
		}
		JMenu result = createMenu(this.toolsRenderer.prepareMessageToDisplay("List"));
		{
			result.add(
					createMenuItem(new AbstractAction(this.toolsRenderer.prepareMessageToDisplay("Move Columns...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							ITypeInfo customizedItemType = customizedListType.getItemType();
							if (InfoCustomizationsFactory.isItemTypeChanged(infoCustomizations,
									customizedListType.getSpecificProperties())) {
								customizedItemType = InfoCustomizationsFactory.getOriginalItemType(infoCustomizations,
										customizedListType.getSpecificProperties());
							}
							String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
							ListCustomization lc = InfoCustomizations.getListCustomization(infoCustomizations,
									customizedListType.getName(), itemTypeName, true);
							IListStructuralInfo customizedListStructure = customizedListType.getStructuralInfo();
							List<IInfo> columns = MiscUtils.convertCollection(customizedListStructure.getColumns());
							openInfosOrderDialog(customizerButton, lc, "columnsCustomOrder", columns, "Columns Order");
						}
					}));
			result.add(
					createMenuItem(new AbstractAction(this.toolsRenderer.prepareMessageToDisplay("More Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openListCutomizationDialog(customizerButton, infoCustomizations, customizedListType);
						}
					}));
			Component fieldControl = fieldControlPlaceHolder.getFieldControl();
			if (fieldControl instanceof ListControl) {
				ItemPosition selected = ((ListControl) fieldControl).getSingleSelection();
				if (selected != null) {
					if (selected.getParentItemPosition() != null) {
						if (!(selected.getContainingListFieldIfNotRoot() instanceof SubListGroupField)) {
							JMenu listSelectionMenu = createMenu(
									this.toolsRenderer.prepareMessageToDisplay("Selected Sub-list"));
							listSelectionMenu.add(createMenuItem(
									new AbstractAction(this.toolsRenderer.prepareMessageToDisplay("More Options...")) {
										private static final long serialVersionUID = 1L;

										@Override
										public void actionPerformed(ActionEvent e) {
											final InfoCustomizations infoCustomizations;
											if (infoCustomizationsShared) {
												infoCustomizations = swingCustomizer.getInfoCustomizations();
											} else {
												Object parentItem = selected.getParentItemPosition().getItem();
												ITypeInfo parentItemType = swingCustomizer.getCustomizedUI()
														.getTypeInfo(swingCustomizer.getCustomizedUI()
																.getTypeInfoSource(parentItem));
												FieldCustomization fieldCustomization = InfoCustomizations
														.getFieldCustomization(
																InfoCustomizations.getTypeCustomization(
																		swingCustomizer.getInfoCustomizations(),
																		parentItemType.getName()),
																selected.getContainingListFieldIfNotRoot().getName(),
																true);
												infoCustomizations = fieldCustomization.getSpecificTypeCustomizations();
											}
											openListCutomizationDialog(customizerButton, infoCustomizations,
													(IListTypeInfo) selected.getContainingListFieldIfNotRoot()
															.getType());
										}
									}));
							result.add(listSelectionMenu);
						}
					}
				}
			}
		}
		return result;
	}

	protected JMenuItem makeMenuItemForEnumeration(final JButton customizerButton,
			final InfoCustomizations infoCustomizations, final IEnumerationTypeInfo customizedEnumType) {
		JMenu result = createMenu(this.toolsRenderer.prepareMessageToDisplay("Enumeration"));
		{
			result.add(createMenuItem(new AbstractAction(this.toolsRenderer.prepareMessageToDisplay("Move Items...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					EnumerationCustomization ec = InfoCustomizations.getEnumerationCustomization(infoCustomizations,
							customizedEnumType.getName(), true);
					List<IInfo> valueInfos = new ArrayList<IInfo>();
					for (Object value : customizedEnumType.getValues()) {
						valueInfos.add(customizedEnumType.getValueInfo(value));
					}
					openInfosOrderDialog(customizerButton, ec, "itemsCustomOrder", valueInfos,
							"Enumeration Items Order");
				}
			}));
			result.add(
					createMenuItem(new AbstractAction(this.toolsRenderer.prepareMessageToDisplay("More Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openEnumerationCutomizationDialog(customizerButton, infoCustomizations, customizedEnumType);
						}
					}));
		}
		return result;
	}

	protected JMenu createMenu(String text) {
		JMenu result = new JMenu(text);
		Color awtBackgroundColor = (toolsUI.getApplicationInfo().getMainBackgroundColor() != null)
				? SwingRendererUtils.getColor(toolsUI.getApplicationInfo().getMainBackgroundColor())
				: null;
		Color awtForegroundColor = (toolsUI.getApplicationInfo().getMainForegroundColor() != null)
				? SwingRendererUtils.getColor(toolsUI.getApplicationInfo().getMainForegroundColor())
				: null;
		Font labelCustomFont = (toolsUI.getApplicationInfo().getLabelCustomFontResourcePath() != null)
				? SwingRendererUtils
						.loadFontThroughCache(toolsUI.getApplicationInfo().getLabelCustomFontResourcePath(),
								ReflectionUIUtils.getErrorLogListener(toolsUI), toolsRenderer)
						.deriveFont(result.getFont().getStyle(), result.getFont().getSize())
				: null;
		if (awtBackgroundColor != null) {
			result.setBackground(awtBackgroundColor);
		}
		result.setOpaque(true);
		if (awtForegroundColor != null) {
			result.setForeground(awtForegroundColor);
		}
		if (labelCustomFont != null) {
			result.setFont(labelCustomFont);
		}
		return result;
	}

	protected JMenuItem createMenuItem(Action action) {
		JMenuItem result = new JMenuItem(action);
		Color backgroundColor = (toolsUI.getApplicationInfo().getMainBackgroundColor() != null)
				? SwingRendererUtils.getColor(toolsUI.getApplicationInfo().getMainBackgroundColor())
				: null;
		Color foregroundColor = (toolsUI.getApplicationInfo().getMainForegroundColor() != null)
				? SwingRendererUtils.getColor(toolsUI.getApplicationInfo().getMainForegroundColor())
				: null;
		Font labelCustomFont = (toolsUI.getApplicationInfo().getLabelCustomFontResourcePath() != null)
				? SwingRendererUtils
						.loadFontThroughCache(toolsUI.getApplicationInfo().getLabelCustomFontResourcePath(),
								ReflectionUIUtils.getErrorLogListener(toolsUI), toolsRenderer)
						.deriveFont(result.getFont().getStyle(), result.getFont().getSize())
				: null;
		if (backgroundColor != null) {
			result.setBackground(backgroundColor);
		}
		result.setOpaque(true);
		if (foregroundColor != null) {
			result.setForeground(foregroundColor);
		}
		if (labelCustomFont != null) {
			result.setFont(labelCustomFont);
		}
		return result;
	}

	protected JCheckBoxMenuItem createCheckBoxMenuItem(Action action, boolean selected) {
		JCheckBoxMenuItem result = new JCheckBoxMenuItem(action);
		result.setSelected(selected);
		Color backgroundColor = (toolsUI.getApplicationInfo().getMainBackgroundColor() != null)
				? SwingRendererUtils.getColor(toolsUI.getApplicationInfo().getMainBackgroundColor())
				: null;
		Color foregroundColor = (toolsUI.getApplicationInfo().getMainForegroundColor() != null)
				? SwingRendererUtils.getColor(toolsUI.getApplicationInfo().getMainForegroundColor())
				: null;
		Font labelCustomFont = (toolsUI.getApplicationInfo().getLabelCustomFontResourcePath() != null)
				? SwingRendererUtils
						.loadFontThroughCache(toolsUI.getApplicationInfo().getLabelCustomFontResourcePath(),
								ReflectionUIUtils.getErrorLogListener(toolsUI), toolsRenderer)
						.deriveFont(result.getFont().getStyle(), result.getFont().getSize())
				: null;
		if (backgroundColor != null) {
			result.setBackground(backgroundColor);
		}
		result.setOpaque(true);
		if (foregroundColor != null) {
			result.setForeground(foregroundColor);
		}
		if (labelCustomFont != null) {
			result.setFont(labelCustomFont);
		}
		return result;
	}

	protected void showMenu(JPopupMenu popupMenu, JButton source) {
		popupMenu.show(source, source.getWidth(), source.getHeight() / 2);
	}

	protected void hideMethod(JButton customizerButton, ITypeInfo customizedType, String methodSignature) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
		changeCustomizationFieldValue(mc, "hidden", true);
	}

	protected void hideField(JButton customizerButton, ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		changeCustomizationFieldValue(fc, "hidden", true);
	}

	public void changeCustomizationFieldValue(AbstractCustomization customization, String fieldName,
			Object fieldValue) {
		ITypeInfo customizationType = toolsUI.getTypeInfo(toolsUI.getTypeInfoSource(customization));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(customizationType.getFields(), fieldName);
		DefaultFieldControlData controlData = new DefaultFieldControlData(toolsUI, customization, field);
		ModificationStack modificationStack = swingCustomizer.getCustomizationController().getModificationStack();
		ReflectionUIUtils.setFieldValueThroughModificationStack(controlData, fieldValue, modificationStack,
				ReflectionUIUtils.getDebugLogListener(swingCustomizer.getReflectionUI()));
	}

	protected void removeCustomizationItem(AbstractCustomization container, String listFieldName,
			AbstractCustomization item) {
		ITypeInfo customizationType = toolsUI.getTypeInfo(toolsUI.getTypeInfoSource(container));
		IFieldInfo listField = ReflectionUIUtils.findInfoByName(customizationType.getFields(), listFieldName);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List tmpList = new ArrayList((List) listField.getValue(container));
		tmpList.remove(item);
		changeCustomizationFieldValue(container, listFieldName, tmpList);
	}

	protected void moveField(JButton customizerButton, ITypeInfo customizedType, String fieldName, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		List<IFieldInfo> customizedFields = customizedType.getFields();
		IFieldInfo customizedField = ReflectionUIUtils.findInfoByName(customizedFields, fieldName);
		if (customizedField == null) {
			return;
		}
		List<String> newOrder = InfoCustomizations.getInfosOrderAfterMove(customizedFields, customizedField, offset);
		changeCustomizationFieldValue(tc, "customFieldsOrder", newOrder);
	}

	protected void moveMethod(JButton customizerButton, ITypeInfo customizedType, String methodSignature, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		List<IMethodInfo> customizedMethods = customizedType.getMethods();
		IMethodInfo customizedMethod = ReflectionUIUtils.findMethodBySignature(customizedMethods, methodSignature);
		if (customizedMethod == null) {
			return;
		}
		List<String> newOrder = InfoCustomizations.getInfosOrderAfterMove(customizedMethods, customizedMethod, offset);
		changeCustomizationFieldValue(tc, "customMethodsOrder", newOrder);
	}

	protected void openInfosOrderDialog(final JButton customizerButton, AbstractCustomization customization,
			String customizationOrderFieldName, List<IInfo> currentInfoList, String title) {
		InfoOrderItem[] orderItems = new InfoOrderItem[currentInfoList.size()];
		for (int i = 0; i < currentInfoList.size(); i++) {
			orderItems[i] = new InfoOrderItem(currentInfoList.get(i));
		}
		StandardEditorBuilder dialogStatus = toolsRenderer.openObjectDialog(customizerButton, orderItems, title,
				this.swingCustomizer.getCustomizationsIcon().getImage(), true, true);
		if (!dialogStatus.isCancelled()) {
			orderItems = (InfoOrderItem[]) dialogStatus.getCurrentValue();
			List<String> newOrder = new ArrayList<String>();
			for (InfoOrderItem item : orderItems) {
				newOrder.add(item.getInfo().getName());
			}
			changeCustomizationFieldValue(customization, customizationOrderFieldName, newOrder);
		}
	}

	protected void openEnumerationCutomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			final IEnumerationTypeInfo customizedEnumType) {
		EnumerationCustomization ec = InfoCustomizations.getEnumerationCustomization(infoCustomizations,
				customizedEnumType.getName(), true);
		fillEnumerationCustomization(ec, customizedEnumType);
		openCustomizationEditor(customizerButton, ec);
	}

	protected void openListCutomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			final IListTypeInfo customizedListType) {
		ITypeInfo customizedItemType = customizedListType.getItemType();
		if (InfoCustomizationsFactory.isItemTypeChanged(infoCustomizations,
				customizedListType.getSpecificProperties())) {
			customizedItemType = InfoCustomizationsFactory.getOriginalItemType(infoCustomizations,
					customizedListType.getSpecificProperties());
		}
		String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
		ListCustomization lc = InfoCustomizations.getListCustomization(infoCustomizations, customizedListType.getName(),
				itemTypeName, true);
		fillListCustomization(lc, customizedListType);
		openCustomizationEditor(customizerButton, lc);
	}

	protected void fillEnumerationCustomization(EnumerationCustomization ec, IEnumerationTypeInfo customizedEnumType) {
		try {
			for (Object item : customizedEnumType.getValues()) {
				IEnumerationItemInfo itemInfo = customizedEnumType.getValueInfo(item);
				InfoCustomizations.getEnumerationItemCustomization(ec, itemInfo.getName(), true);
			}
		} catch (Throwable t) {
			toolsUI.logDebug(t);
		}
	}

	protected void fillListCustomization(ListCustomization lc, IListTypeInfo customizedListType) {
		try {
			for (IColumnInfo column : customizedListType.getStructuralInfo().getColumns()) {
				InfoCustomizations.getColumnCustomization(lc, column.getName(), true);
			}
		} catch (Throwable t) {
			toolsUI.logDebug(t);
		}
	}

	protected void fillMethodCustomization(MethodCustomization mc, IMethodInfo customizedMethod) {
		try {
			for (IParameterInfo param : customizedMethod.getParameters()) {
				InfoCustomizations.getParameterCustomization(mc, param.getName(), true);
			}
		} catch (Throwable t) {
			toolsUI.logDebug(t);
		}
	}

	protected void fillTypeCustomization(TypeCustomization tc, ITypeInfo customizedType) {
		try {
			for (IFieldInfo field : customizedType.getFields()) {
				InfoCustomizations.getFieldCustomization(tc, field.getName(), true);
			}
			for (IMethodInfo method : customizedType.getMethods()) {
				InfoCustomizations.getMethodCustomization(tc, method.getSignature(), true);
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(tc, method.getSignature(), true);
				fillMethodCustomization(mc, method);
			}
			for (IMethodInfo ctor : customizedType.getConstructors()) {
				InfoCustomizations.getMethodCustomization(tc, ctor.getSignature(), true);
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(tc, ctor.getSignature(), true);
				fillMethodCustomization(mc, ctor);
			}
		} catch (Throwable t) {
			toolsUI.logDebug(t);
		}
	}

	protected void openFieldCutomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		openCustomizationEditor(customizerButton, fc);
	}

	protected void openMethodCutomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String methodSignature) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(tc, methodSignature, true);
		IMethodInfo customizedMethod = ReflectionUIUtils.findMethodBySignature(customizedType.getMethods(),
				methodSignature);
		fillMethodCustomization(mc, customizedMethod);
		openCustomizationEditor(customizerButton, mc);
	}

	public Component makeButtonForMethod(final MethodControlPlaceHolder methodControlPlaceHolder) {
		final JButton result = makeButton();
		result.setToolTipText(SwingRendererUtils.adaptToolTipTextToMultiline(toolsRenderer
				.prepareMessageToDisplay(getCustomizationTitle(methodControlPlaceHolder.getMethod().getSignature()))));
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();

				popupMenu.add(createMenuItem(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("Hide")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									hideMethod(result, getContainingObjectCustomizedType(methodControlPlaceHolder),
											methodControlPlaceHolder.getMethod().getSignature());
								} catch (Throwable t) {
									toolsRenderer.handleException(result, t);
								}
							}
						}));

				for (JMenuItem menuItem : makeMenuItemsForMethodPosition(result, methodControlPlaceHolder)) {
					popupMenu.add(menuItem);
				}

				for (JMenuItem menuItem : makeMenuItemsForMethodEncapsulation(result, methodControlPlaceHolder)) {
					popupMenu.add(menuItem);
				}

				popupMenu.add(createMenuItem(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareMessageToDisplay("More Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openMethodCutomizationDialog(result, swingCustomizer.getInfoCustomizations(),
								getContainingObjectCustomizedType(methodControlPlaceHolder),
								methodControlPlaceHolder.getMethod().getSignature());
					}
				}));
				showMenu(popupMenu, result);
			}
		});
		return result;
	}

	protected String getCustomizationTitle(String targetName) {
		String result = "Customize";
		if (targetName.length() > 0) {
			result += " (" + targetName + ")";
		}
		return result;
	}

	public static class InfoOrderItem {
		protected IInfo info;

		public InfoOrderItem(IInfo info) {
			super();
			this.info = info;
		}

		public IInfo getInfo() {
			return info;
		}

		public String getName() {
			return info.getName();
		}

		public String getCaption() {
			return info.getCaption();
		}

		@Override
		public String toString() {
			return info.getCaption();
		}

	}

}
