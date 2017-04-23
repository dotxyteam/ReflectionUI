package xy.reflect.ui.control.swing.customizer;

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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.plugin.ICustomizableFieldControlPlugin;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizations;
import xy.reflect.ui.info.type.factory.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.factory.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

public class CustomizationTools {

	protected final SwingCustomizer swingCustomizer;
	protected SwingRenderer toolsRenderer;
	protected CustomizationToolsUI toolsUI;
	protected InfoCustomizations toolsCustomizations;

	public CustomizationTools(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
		toolsCustomizations = new InfoCustomizations();
		URL url = ReflectionUI.class.getResource("resource/customizations-tools.icu");
		try {
			File customizationsFile = FileUtils.getStreamAsFile(url.openStream());
			String customizationsFilePath = customizationsFile.getPath();
			toolsCustomizations.loadFromFile(new File(customizationsFilePath));
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
		toolsUI = createToolsUI();
		toolsRenderer = createToolsRenderer();

	}

	public SwingRenderer getToolsRenderer() {
		return toolsRenderer;
	}

	public CustomizationToolsUI getToolsUI() {
		return toolsUI;
	}

	public InfoCustomizations getToolsCustomizations() {
		return toolsCustomizations;
	}

	protected JButton createToolAccessButton(ImageIcon imageIcon) {
		final JButton result = new JButton(imageIcon);
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		return result;
	}

	protected SwingRenderer createToolsRenderer() {
		if (SystemProperties.isInfoCustomizationToolsCustomizationAllowed()) {
			String customizationToolsCustomizationsOutputFilePath = System
					.getProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH);
			return new SwingCustomizer(toolsUI, toolsCustomizations, customizationToolsCustomizationsOutputFilePath) {

				@Override
				protected CustomizationTools createCustomizationTools() {
					return new CustomizationTools(this) {

						@Override
						protected SwingRenderer createToolsRenderer() {
							return new SwingRenderer(this.toolsUI);
						}

					};
				}

				@Override
				protected CustomizationOptions initializeCustomizationOptions() {
					return new CustomizationOptions(this);
				}

			};
		} else {
			return new SwingRenderer(toolsUI);
		}
	}

	protected CustomizationToolsUI createToolsUI() {
		return new CustomizationToolsUI(swingCustomizer);
	}

	public JButton makeSaveControl() {
		final JButton result = createToolAccessButton(SwingRendererUtils.SAVE_ALL_ICON);
		result.setToolTipText(toolsRenderer.prepareStringToDisplay("Save all the customizations"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final File file = new File(
						CustomizationTools.this.swingCustomizer.getInfoCustomizationsOutputFilePath());
				try {
					CustomizationTools.this.swingCustomizer.getInfoCustomizations().saveToFile(file);
				} catch (IOException e1) {
					toolsRenderer.handleExceptionsFromDisplayedUI(result, e1);
				}
			}
		});
		return result;
	}

	public Component makeCustomizerForTypeInfo(final Object object) {
		final ITypeInfo customizedType = this.swingCustomizer.getReflectionUI()
				.getTypeInfo(this.swingCustomizer.getReflectionUI().getTypeInfoSource(object));
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		result.setToolTipText(toolsRenderer.prepareStringToDisplay(getCustomizationTitle(customizedType.getName())));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Type Options (Shared)...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(result, swingCustomizer.getInfoCustomizations(), customizedType);
					}
				});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Refresh")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									rebuildCustomizerForm(result);
								} catch (Throwable t) {
									toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
								}
							}
						});
				popupMenu.add(new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Lock")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							hideCustomizationTools(result, customizedType.getName());
						} catch (Throwable t) {
							toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});

				showMenu(popupMenu, result);
			}
		});
		return result;
	}

	protected void hideCustomizationTools(JButton customizer, String typeName) {
		this.swingCustomizer.getCustomizationOptions().hideFor(typeName);
	}

	protected void openTypeCustomizationDialog(JButton customizer, InfoCustomizations infoCustomizations,
			ITypeInfo customizedType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		updateTypeCustomization(t, customizedType);
		openCustomizationEditor(customizer, t);
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

	protected void openCustomizationEditor(final JButton customizer, final Object customization) {
		StandardEditorBuilder dialogBuilder = new StandardEditorBuilder(toolsRenderer, customizer, customization) {

			@Override
			public boolean isCancellable() {
				return true;
			}

		};
		dialogBuilder.showDialog();
		if (!dialogBuilder.isCancelled()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					rebuildCustomizerForm(customizer);
				}
			});
		}
	}

	public ITypeInfo getContainingObjectType(FieldControlPlaceHolder fieldControlPlaceHolder) {
		return CustomizationTools.this.swingCustomizer.getReflectionUI()
				.getTypeInfo(CustomizationTools.this.swingCustomizer.getReflectionUI()
						.getTypeInfoSource(fieldControlPlaceHolder.getObject()));
	}

	public ITypeInfo getContainingObjectType(MethodControlPlaceHolder methodControlPlaceHolder) {
		return CustomizationTools.this.swingCustomizer.getReflectionUI()
				.getTypeInfo(CustomizationTools.this.swingCustomizer.getReflectionUI()
						.getTypeInfoSource(methodControlPlaceHolder.getObject()));
	}

	public ITypeInfo getControlDataType(FieldControlPlaceHolder fieldControlPlaceHolder) {
		IFieldControlData controlData = fieldControlPlaceHolder.getControlData();
		if (controlData == null) {
			return null;
		}
		return controlData.getType();
	}

	public FieldCustomization getFieldCustomization(FieldControlPlaceHolder fieldControlPlaceHolder,
			InfoCustomizations infoCustomizations) {
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(
				getContaingTypeCustomization(fieldControlPlaceHolder, infoCustomizations),
				fieldControlPlaceHolder.getField().getName(), true);
		return fc;
	}

	public TypeCustomization getContaingTypeCustomization(FieldControlPlaceHolder fieldControlPlaceHolder,
			InfoCustomizations infoCustomizations) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations,
				getContainingObjectType(fieldControlPlaceHolder).getName(), true);
		return t;
	}

	public Component makeCustomizerForFieldInfo(final FieldControlPlaceHolder fieldControlPlaceHolder) {
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		SwingRendererUtils.setMultilineToolTipText(result, toolsRenderer
				.prepareStringToDisplay(getCustomizationTitle(fieldControlPlaceHolder.getField().getName())));
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Hide")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							hideField(result, getContainingObjectType(fieldControlPlaceHolder),
									fieldControlPlaceHolder.getField().getName());
						} catch (Throwable t) {
							toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Move Up")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									moveField(result, getContainingObjectType(fieldControlPlaceHolder),
											fieldControlPlaceHolder.getField().getName(), -1);
								} catch (Throwable t) {
									toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
								}
							}
						});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Move Down")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									moveField(result, getContainingObjectType(fieldControlPlaceHolder),
											fieldControlPlaceHolder.getField().getName(), 1);
								} catch (Throwable t) {
									toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
								}
							}
						});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Move To Top")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(result, getContainingObjectType(fieldControlPlaceHolder),
									fieldControlPlaceHolder.getField().getName(), Short.MIN_VALUE);
						} catch (Throwable t) {
							toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Move To Bottom")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(result, getContainingObjectType(fieldControlPlaceHolder),
									fieldControlPlaceHolder.getField().getName(), Short.MAX_VALUE);
						} catch (Throwable t) {
							toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});

				for (JMenuItem menuItem : makeMenuItemsForFieldControlPlugins(result, fieldControlPlaceHolder)) {
					popupMenu.add(menuItem);
				}

				for (JMenuItem menuItem : makeMenuItemsForFieldTypeInfo(result, fieldControlPlaceHolder,
						"Type Options...", false, getControlDataType(fieldControlPlaceHolder))) {
					popupMenu.add(menuItem);
				}

				popupMenu.add(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareStringToDisplay("More Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openFieldCutomizationDialog(result, swingCustomizer.getInfoCustomizations(),
								getContainingObjectType(fieldControlPlaceHolder),
								fieldControlPlaceHolder.getField().getName());
					}
				});
				showMenu(popupMenu, result);
			}

		});
		return result;
	}

	protected List<JMenuItem> makeMenuItemsForFieldControlPlugins(final JButton customizer,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		Component fieldControl = fieldControlPlaceHolder.getFieldControl();
		if (fieldControl instanceof NullableControl) {
			fieldControl = ((NullableControl) fieldControl).getSubControl();
		}
		final IFieldControlPlugin currentPlugin = swingCustomizer.getPluginByFieldControl().get(fieldControl);
		if (currentPlugin != null) {
			if (currentPlugin instanceof ICustomizableFieldControlPlugin) {
				result.add(((ICustomizableFieldControlPlugin) currentPlugin).makeFieldCustomizerMenuItem(customizer,
						fieldControlPlaceHolder, swingCustomizer.getInfoCustomizations(), CustomizationTools.this));
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
			JMenu changeFieldControlPluginMenu = new JMenu(toolsRenderer.prepareStringToDisplay("Change Control"));
			result.add(changeFieldControlPluginMenu);
			changeFieldControlPluginMenu.add(
					new JCheckBoxMenuItem(new AbstractAction(toolsRenderer.prepareStringToDisplay("Default Control")) {

						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							FieldCustomization fieldCustomization = getFieldCustomization(fieldControlPlaceHolder,
									swingCustomizer.getInfoCustomizations());
							Map<String, Object> specificProperties = fieldCustomization.getSpecificProperties();
							specificProperties = new HashMap<String, Object>(specificProperties);
							specificProperties.put(IFieldControlPlugin.CHOSEN_PROPERTY_KEY,
									IFieldControlPlugin.NONE_IDENTIFIER);
							fieldCustomization.setSpecificProperties(specificProperties);
							rebuildCustomizerForm(customizer);
						}
					}) {
						private static final long serialVersionUID = 1L;
						{
							if (currentPlugin == null) {
								setSelected(true);
							}
						}
					});
			for (final IFieldControlPlugin plugin : potentialFieldControlPlugins) {
				changeFieldControlPluginMenu.add(new JCheckBoxMenuItem(
						new AbstractAction(toolsRenderer.prepareStringToDisplay(plugin.getControlTitle())) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								FieldCustomization fieldCustomization = getFieldCustomization(fieldControlPlaceHolder,
										swingCustomizer.getInfoCustomizations());
								Map<String, Object> specificProperties = fieldCustomization.getSpecificProperties();
								specificProperties = new HashMap<String, Object>(specificProperties);
								specificProperties.put(IFieldControlPlugin.CHOSEN_PROPERTY_KEY, plugin.getIdentifier());
								fieldCustomization.setSpecificProperties(specificProperties);
								rebuildCustomizerForm(customizer);
							}
						}) {
					private static final long serialVersionUID = 1L;
					{
						if (currentPlugin != null) {
							if (currentPlugin.getIdentifier().equals(plugin.getIdentifier())) {
								setSelected(true);
							}
						}
					}
				});
			}
		}

		return result;
	}

	protected List<JMenuItem> makeMenuItemsForFieldTypeInfo(final JButton customizer,
			final FieldControlPlaceHolder fieldControlPlaceHolder, final String mainCaption,
			final boolean infoCustomizationsShared, final ITypeInfo fieldType) {
		final InfoCustomizations infoCustomizations;
		if (!infoCustomizationsShared) {
			infoCustomizations = swingCustomizer.getInfoCustomizations();
		} else {
			FieldCustomization fieldCustomization = getFieldCustomization(fieldControlPlaceHolder,
					swingCustomizer.getInfoCustomizations());
			infoCustomizations = fieldCustomization.getSpecificTypeCustomizations();
		}

		List<JMenuItem> result = new ArrayList<JMenuItem>();
		result.add(new JMenuItem(
				new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay(mainCaption)) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(customizer, infoCustomizations, fieldType);
					}
				}));
		if (fieldType instanceof IListTypeInfo) {
			result.add(makeMenuItemForListInfo(customizer, infoCustomizations, (IListTypeInfo) fieldType));
		}
		if (fieldType instanceof IEnumerationTypeInfo) {
			result.add(makeMenuItemForEnumeration(customizer, infoCustomizations, (IEnumerationTypeInfo) fieldType));
		}

		if (!infoCustomizationsShared) {
			if (swingCustomizer.getCustomizationOptions().areFieldSharedTypeOptionsDisplayed()) {
				final JMenu sharedTypeInfoSubMenu = new JMenu(
						CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Shared"));
				result.add(sharedTypeInfoSubMenu);
				for (JMenuItem menuItem : makeMenuItemsForFieldTypeInfo(customizer, fieldControlPlaceHolder,
						mainCaption, true, fieldType)) {
					sharedTypeInfoSubMenu.add(menuItem);
				}
			}
		}
		return result;

	}

	protected JMenuItem makeMenuItemForListInfo(final JButton customizer, final InfoCustomizations infoCustomizations,
			final IListTypeInfo customizedListType) {
		JMenu result = new JMenu(this.toolsRenderer.prepareStringToDisplay("List"));
		{
			result.add(new AbstractAction(this.toolsRenderer.prepareStringToDisplay("Move Columns...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListColumnsOrderDialog(customizer, infoCustomizations, customizedListType);
				}
			});
			result.add(new AbstractAction(this.swingCustomizer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListCutomizationDialog(customizer, infoCustomizations, customizedListType);
				}
			});
		}
		return result;
	}

	protected JMenuItem makeMenuItemForEnumeration(final JButton customizer,
			final InfoCustomizations infoCustomizations, final IEnumerationTypeInfo customizedEnumType) {
		JMenu result = new JMenu(this.toolsRenderer.prepareStringToDisplay("Enumeration"));
		{
			result.add(new AbstractAction(this.toolsRenderer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openEnumerationCutomizationDialog(customizer, infoCustomizations, customizedEnumType);
				}
			});
		}
		return result;
	}

	protected void showMenu(JPopupMenu popupMenu, JButton source) {
		popupMenu.show(source, source.getWidth(), source.getHeight() / 2);
	}

	protected void hideMethod(JButton customizer, ITypeInfo customizedType, String methodSignature) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
		mc.setHidden(true);
		rebuildCustomizerForm(customizer);
	}

	protected void hideField(JButton customizer, ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		fc.setHidden(true);
		rebuildCustomizerForm(customizer);
	}

	protected void moveField(JButton customizer, ITypeInfo customizedType, String fieldName, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		tc.moveField(customizedType.getFields(), fieldName, offset);
		rebuildCustomizerForm(customizer);
	}

	protected void moveMethod(JButton customizer, ITypeInfo customizedType, String methodSignature, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		tc.moveMethod(customizedType.getMethods(), methodSignature, offset);
		rebuildCustomizerForm(customizer);
	}

	@SuppressWarnings("unchecked")
	protected void openListColumnsOrderDialog(final JButton customizer, InfoCustomizations infoCustomizations,
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
		StandardEditorBuilder dialogStatus = toolsRenderer.openObjectDialog(customizer, columnOrder, "Columns Order",
				this.swingCustomizer.getCustomizationsIcon().getImage(), true, true);
		if (!dialogStatus.isCancelled()) {
			columnOrder = (List<ColumnOrderItem>) dialogStatus.getCurrentObjectValue();
			List<String> newOrder = new ArrayList<String>();
			for (ColumnOrderItem item : columnOrder) {
				newOrder.add(item.getColumnInfo().getName());
			}
			lc.setColumnsCustomOrder(newOrder);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					rebuildCustomizerForm(customizer);
				}
			});
		}
	}

	protected void openEnumerationCutomizationDialog(JButton customizer, InfoCustomizations infoCustomizations,
			final IEnumerationTypeInfo customizedEnumType) {
		EnumerationCustomization ec = InfoCustomizations.getEnumerationCustomization(infoCustomizations,
				customizedEnumType.getName(), true);
		updateEnumerationCustomization(ec, customizedEnumType);
		openCustomizationEditor(customizer, ec);
	}

	protected void updateEnumerationCustomization(EnumerationCustomization ec,
			IEnumerationTypeInfo customizedEnumType) {
		for (Object item : customizedEnumType.getPossibleValues()) {
			IEnumerationItemInfo itemInfo = customizedEnumType.getValueInfo(item);
			InfoCustomizations.getEnumerationItemCustomization(ec, itemInfo.getName(), true);
		}
	}

	protected void openListCutomizationDialog(JButton customizer, InfoCustomizations infoCustomizations,
			final IListTypeInfo customizedListType) {
		ITypeInfo customizedItemType = customizedListType.getItemType();
		String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
		ListCustomization lc = InfoCustomizations.getListCustomization(infoCustomizations, customizedListType.getName(),
				itemTypeName, true);
		updateListCustomization(lc, customizedListType);
		openCustomizationEditor(customizer, lc);
	}

	protected void updateListCustomization(ListCustomization lc, IListTypeInfo customizedListType) {
		for (IColumnInfo column : customizedListType.getStructuralInfo().getColumns()) {
			InfoCustomizations.getColumnCustomization(lc, column.getName(), true);
		}
		ITypeInfo customizedItemType = customizedListType.getItemType();
		if (customizedItemType != null) {
			TypeCustomization t = InfoCustomizations.getTypeCustomization(swingCustomizer.getInfoCustomizations(),
					customizedItemType.getName(), true);
			updateTypeCustomization(t, customizedItemType);
		}
	}

	protected void openFieldCutomizationDialog(JButton customizer, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		openCustomizationEditor(customizer, fc);
	}

	protected void openMethodCutomizationDialog(JButton customizer, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String methodSignature) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
		openCustomizationEditor(customizer, mc);
	}

	protected void updateMethodCustomization(MethodCustomization mc, IMethodInfo customizedMethod) {
		for (IParameterInfo param : customizedMethod.getParameters()) {
			InfoCustomizations.getParameterCustomization(mc, param.getName(), true);
		}
	}

	public Component makeCustomizerForMethodInfo(final MethodControlPlaceHolder methodControlPlaceHolder) {
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		SwingRendererUtils.setMultilineToolTipText(result, toolsRenderer.prepareStringToDisplay(
				getCustomizationTitle(ReflectionUIUtils.getMethodSignature(methodControlPlaceHolder.getMethod()))));
		result.addActionListener(new ActionListener() {

			private String getMethodInfoSignature() {
				return ReflectionUIUtils.getMethodSignature(methodControlPlaceHolder.getMethod());
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Hide")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							hideMethod(result, getContainingObjectType(methodControlPlaceHolder),
									getMethodInfoSignature());
						} catch (Throwable t) {
							toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Move Left")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									moveMethod(result, getContainingObjectType(methodControlPlaceHolder),
											getMethodInfoSignature(), -1);
								} catch (Throwable t) {
									toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
								}
							}
						});
				popupMenu.add(
						new AbstractAction(CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Move Right")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									moveMethod(result, getContainingObjectType(methodControlPlaceHolder),
											getMethodInfoSignature(), 1);
								} catch (Throwable t) {
									toolsRenderer.handleExceptionsFromDisplayedUI(result, t);
								}
							}
						});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.toolsRenderer.prepareStringToDisplay("More Options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openMethodCutomizationDialog(result, swingCustomizer.getInfoCustomizations(),
								getContainingObjectType(methodControlPlaceHolder), getMethodInfoSignature());
					}
				});
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

	public void rebuildCustomizerForm(JButton customizer) {
		JPanel form = SwingRendererUtils.findParentForm(customizer, this.swingCustomizer);
		rebuildCustomizerForm(form);
	}

	public void rebuildCustomizerForm(JPanel form) {
		swingCustomizer.recreateFormContent(form);
		swingCustomizer.validateFormInBackgroundAndReport(form);
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