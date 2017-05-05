package xy.reflect.ui.control.swing.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
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
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.component.AlternativeWindowDecorationsPanel;

public class CustomizationTools {

	protected final SwingCustomizer swingCustomizer;
	protected SwingRenderer toolsRenderer;
	protected CustomizationToolsUI toolsUI;
	protected InfoCustomizationsFactory toolsCustomizationsFactory;

	public CustomizationTools(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
		toolsUI = createToolsUI();
		toolsCustomizationsFactory = createInfoCustomizationsFactory();
		toolsRenderer = createToolsRenderer();
		URL url = ReflectionUI.class.getResource("resource/customizations-tools.icu");
		try {
			File customizationsFile = FileUtils.getStreamAsFile(url.openStream());
			String customizationsFilePath = customizationsFile.getPath();
			toolsCustomizationsFactory.getInfoCustomizations().loadFromFile(new File(customizationsFilePath),
					ReflectionUIUtils.getDebugLogListener(swingCustomizer.getReflectionUI()));
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}

	}

	public SwingRenderer getToolsRenderer() {
		return toolsRenderer;
	}

	public CustomizationToolsUI getToolsUI() {
		return toolsUI;
	}

	public InfoCustomizationsFactory getToolsCustomizationsFactory() {
		return toolsCustomizationsFactory;
	}

	protected JButton makeButton() {
		JButton result = new JButton(this.swingCustomizer.getCustomizationsIcon());
		result.setForeground(getToolsForegroundColor());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		return result;
	}

	protected SwingRenderer createToolsRenderer() {
		if (isToolsCustomizationAllowed()) {
			String customizationToolsCustomizationsOutputFilePath = System
					.getProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH);
			return new SwingCustomizer(toolsUI, toolsCustomizationsFactory.getInfoCustomizations(),
					customizationToolsCustomizationsOutputFilePath) {

				@Override
				protected CustomizationTools createCustomizationTools() {
					return new CustomizationTools(this) {

						@Override
						protected boolean isToolsCustomizationAllowed() {
							return false;
						}

					};
				}

				@Override
				public Container createWindowContentPane(Window window, Component content,
						List<? extends Component> toolbarControls) {
					Container result = super.createWindowContentPane(window, content, toolbarControls);
					return createAlternativeWindowDecorationsPanel(SwingRendererUtils.getWindowTitle(window), window,
							result);
				}

			};
		} else {
			return new SwingRenderer(toolsUI) {

				@Override
				public Container createWindowContentPane(Window window, Component content,
						List<? extends Component> toolbarControls) {
					Container result = super.createWindowContentPane(window, content, toolbarControls);
					return createAlternativeWindowDecorationsPanel(SwingRendererUtils.getWindowTitle(window), window,
							result);
				}

			};
		}
	}

	protected boolean isToolsCustomizationAllowed() {
		return SystemProperties.isInfoCustomizationToolsCustomizationAllowed();
	}

	protected Container createAlternativeWindowDecorationsPanel(String windowTitle, Window window,
			Component windowContent) {
		return new AlternativeWindowDecorationsPanel(SwingRendererUtils.getWindowTitle(window), window, windowContent) {

			private static final long serialVersionUID = 1L;

			@Override
			public Color getDecorationsBackgroundColor() {
				return getToolsBackgroundColor();
			}

			@Override
			public Color getDecorationsForegroundColor() {
				return getToolsForegroundColor();
			}

		};
	}

	protected Color getToolsForegroundColor() {
		return new Color(0, 255, 255);
	}

	protected Color getToolsBackgroundColor() {
		return new Color(0, 0, 0);
	}

	protected CustomizationToolsUI createToolsUI() {
		return new CustomizationToolsUI(swingCustomizer);
	}

	protected InfoCustomizationsFactory createInfoCustomizationsFactory() {
		return new InfoCustomizationsFactory(toolsUI, new InfoCustomizations());
	}

	public Component makeButtonForTypeInfo(final Object object) {
		final ITypeInfo customizedType = this.swingCustomizer.getReflectionUI()
				.getTypeInfo(this.swingCustomizer.getReflectionUI().getTypeInfoSource(object));
		final JButton result = makeButton();
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

				showMenu(popupMenu, result);
			}
		});
		return result;
	}

	protected void openTypeCustomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			ITypeInfo customizedType) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		updateTypeCustomization(tc, customizedType);
		openCustomizationEditor(customizerButton, tc);
	}

	protected void updateTypeCustomization(TypeCustomization tc, ITypeInfo customizedType) {
		try {
			for (IFieldInfo field : customizedType.getFields()) {
				InfoCustomizations.getFieldCustomization(tc, field.getName(), true);
			}
			for (IMethodInfo method : customizedType.getMethods()) {
				InfoCustomizations.getMethodCustomization(tc, method.getSignature(), true);
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(tc, method.getSignature(), true);
				updateMethodCustomization(mc, method);
			}
			for (IMethodInfo ctor : customizedType.getConstructors()) {
				InfoCustomizations.getMethodCustomization(tc, ctor.getSignature(), true);
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(tc, ctor.getSignature(), true);
				updateMethodCustomization(mc, ctor);
			}
		} catch (Throwable t) {
			swingCustomizer.getReflectionUI().logDebug(t);
		}
	}

	protected void openCustomizationEditor(final JButton customizerButton, final Object customization) {
		StandardEditorBuilder dialogBuilder = new StandardEditorBuilder(toolsRenderer, customizerButton,
				customization) {

			@Override
			public boolean isCancellable() {
				return true;
			}

		};
		dialogBuilder.showDialog();
		if (customizerButton != null) {
			if (!dialogBuilder.isCancelled()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						rebuildCustomizerForm(customizerButton);
					}
				});
			}
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

	public Component makeButtonForFieldInfo(final FieldControlPlaceHolder fieldControlPlaceHolder) {
		final JButton result = makeButton();
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

	protected List<JMenuItem> makeMenuItemsForFieldControlPlugins(final JButton customizerButton,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		Component fieldControl = fieldControlPlaceHolder.getFieldControl();
		if (fieldControl instanceof NullableControl) {
			fieldControl = ((NullableControl) fieldControl).getSubControl();
		}
		final IFieldControlPlugin currentPlugin = swingCustomizer.getPluginByFieldControl().get(fieldControl);
		if (currentPlugin != null) {
			if (currentPlugin instanceof ICustomizableFieldControlPlugin) {
				result.add(((ICustomizableFieldControlPlugin) currentPlugin).makeFieldCustomizerMenuItem(
						customizerButton, fieldControlPlaceHolder, swingCustomizer.getInfoCustomizations(),
						CustomizationTools.this));
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
							rebuildCustomizerForm(customizerButton);
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
								rebuildCustomizerForm(customizerButton);
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

	protected List<JMenuItem> makeMenuItemsForFieldTypeInfo(final JButton customizerButton,
			final FieldControlPlaceHolder fieldControlPlaceHolder, final String mainCaption,
			final boolean infoCustomizationsShared, final ITypeInfo fieldType) {
		final InfoCustomizations infoCustomizations;
		if (infoCustomizationsShared) {
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
						openTypeCustomizationDialog(customizerButton, infoCustomizations, fieldType);
					}
				}));
		if (fieldType instanceof IListTypeInfo) {
			result.add(makeMenuItemForListInfo(customizerButton, infoCustomizations, (IListTypeInfo) fieldType));
		}
		if (fieldType instanceof IEnumerationTypeInfo) {
			result.add(
					makeMenuItemForEnumeration(customizerButton, infoCustomizations, (IEnumerationTypeInfo) fieldType));
		}

		if (!infoCustomizationsShared) {
			if (swingCustomizer.getCustomizationOptions().areFieldSharedTypeOptionsDisplayed()) {
				final JMenu sharedTypeInfoSubMenu = new JMenu(
						CustomizationTools.this.toolsRenderer.prepareStringToDisplay("Shared"));
				result.add(sharedTypeInfoSubMenu);
				for (JMenuItem menuItem : makeMenuItemsForFieldTypeInfo(customizerButton, fieldControlPlaceHolder,
						mainCaption, true, fieldType)) {
					sharedTypeInfoSubMenu.add(menuItem);
				}
			}
		}
		return result;

	}

	protected JMenuItem makeMenuItemForListInfo(final JButton customizerButton,
			final InfoCustomizations infoCustomizations, final IListTypeInfo customizedListType) {
		JMenu result = new JMenu(this.toolsRenderer.prepareStringToDisplay("List"));
		{
			result.add(new AbstractAction(this.toolsRenderer.prepareStringToDisplay("Move Columns...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListColumnsOrderDialog(customizerButton, infoCustomizations, customizedListType);
				}
			});
			result.add(new AbstractAction(this.swingCustomizer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListCutomizationDialog(customizerButton, infoCustomizations, customizedListType);
				}
			});
		}
		return result;
	}

	protected JMenuItem makeMenuItemForEnumeration(final JButton customizerButton,
			final InfoCustomizations infoCustomizations, final IEnumerationTypeInfo customizedEnumType) {
		JMenu result = new JMenu(this.toolsRenderer.prepareStringToDisplay("Enumeration"));
		{
			result.add(new AbstractAction(this.toolsRenderer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openEnumerationCutomizationDialog(customizerButton, infoCustomizations, customizedEnumType);
				}
			});
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
		mc.setHidden(true);
		rebuildCustomizerForm(customizerButton);
	}

	protected void hideField(JButton customizerButton, ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		fc.setHidden(true);
		rebuildCustomizerForm(customizerButton);
	}

	protected void moveField(JButton customizerButton, ITypeInfo customizedType, String fieldName, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		tc.moveField(customizedType.getFields(), fieldName, offset);
		rebuildCustomizerForm(customizerButton);
	}

	protected void moveMethod(JButton customizerButton, ITypeInfo customizedType, String methodSignature, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		tc.moveMethod(customizedType.getMethods(), methodSignature, offset);
		rebuildCustomizerForm(customizerButton);
	}

	@SuppressWarnings("unchecked")
	protected void openListColumnsOrderDialog(final JButton customizerButton, InfoCustomizations infoCustomizations,
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
		StandardEditorBuilder dialogStatus = toolsRenderer.openObjectDialog(customizerButton, columnOrder,
				"Columns Order", this.swingCustomizer.getCustomizationsIcon().getImage(), true, true);
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
					rebuildCustomizerForm(customizerButton);
				}
			});
		}
	}

	protected void openEnumerationCutomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			final IEnumerationTypeInfo customizedEnumType) {
		EnumerationCustomization ec = InfoCustomizations.getEnumerationCustomization(infoCustomizations,
				customizedEnumType.getName(), true);
		updateEnumerationCustomization(ec, customizedEnumType);
		openCustomizationEditor(customizerButton, ec);
	}

	protected void updateEnumerationCustomization(EnumerationCustomization ec,
			IEnumerationTypeInfo customizedEnumType) {
		try {
			for (Object item : customizedEnumType.getPossibleValues()) {
				IEnumerationItemInfo itemInfo = customizedEnumType.getValueInfo(item);
				InfoCustomizations.getEnumerationItemCustomization(ec, itemInfo.getName(), true);
			}
		} catch (Throwable t) {
			swingCustomizer.getReflectionUI().logDebug(t);
		}
	}

	protected void openListCutomizationDialog(JButton customizerButton, InfoCustomizations infoCustomizations,
			final IListTypeInfo customizedListType) {
		ITypeInfo customizedItemType = customizedListType.getItemType();
		String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
		ListCustomization lc = InfoCustomizations.getListCustomization(infoCustomizations, customizedListType.getName(),
				itemTypeName, true);
		updateListCustomization(lc, customizedListType);
		openCustomizationEditor(customizerButton, lc);
	}

	protected void updateListCustomization(ListCustomization lc, IListTypeInfo customizedListType) {
		try {
			for (IColumnInfo column : customizedListType.getStructuralInfo().getColumns()) {
				InfoCustomizations.getColumnCustomization(lc, column.getName(), true);
			}
			ITypeInfo customizedItemType = customizedListType.getItemType();
			if (customizedItemType != null) {
				TypeCustomization t = InfoCustomizations.getTypeCustomization(swingCustomizer.getInfoCustomizations(),
						customizedItemType.getName(), true);
				updateTypeCustomization(t, customizedItemType);
			}
		} catch (Throwable t) {
			swingCustomizer.getReflectionUI().logDebug(t);
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
		updateMethodCustomization(mc, customizedMethod);
		openCustomizationEditor(customizerButton, mc);
	}

	protected void updateMethodCustomization(MethodCustomization mc, IMethodInfo customizedMethod) {
		try {
			for (IParameterInfo param : customizedMethod.getParameters()) {
				InfoCustomizations.getParameterCustomization(mc, param.getName(), true);
			}
		} catch (Throwable t) {
			swingCustomizer.getReflectionUI().logDebug(t);
		}
	}

	public Component makeButtonForMethodInfo(final MethodControlPlaceHolder methodControlPlaceHolder) {
		final JButton result = makeButton();
		SwingRendererUtils.setMultilineToolTipText(result, toolsRenderer
				.prepareStringToDisplay(getCustomizationTitle(methodControlPlaceHolder.getMethod().getSignature())));
		result.addActionListener(new ActionListener() {

			private String getMethodInfoSignature() {
				return methodControlPlaceHolder.getMethod().getSignature();
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

	public void rebuildCustomizerForm(JButton customizerButton) {
		JPanel form = SwingRendererUtils.findParentForm(customizerButton, this.swingCustomizer);
		rebuildCustomizerForm(form);
	}

	public void rebuildCustomizerForm(JPanel form) {
		try {
			swingCustomizer.recbuildForm(form);
		} catch (Throwable t) {
			swingCustomizer.handleExceptionsFromDisplayedUI(form, t);
		}
	}

	public static class ColumnOrderItem {
		protected IColumnInfo columnInfo;

		public ColumnOrderItem(IColumnInfo columnInfo) {
			super();
			this.columnInfo = columnInfo;
		}

		public IColumnInfo getColumnInfo() {
			return columnInfo;
		}

		public String getColumnName() {
			return columnInfo.getName();
		}

		public String getColumnCaption() {
			return columnInfo.getCaption();
		}

		@Override
		public String toString() {
			return columnInfo.getCaption();
		}

	}

}