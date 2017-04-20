package xy.reflect.ui.control.swing.customization;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
	protected SwingRenderer customizationToolsRenderer;
	protected CustomizationToolsUI customizationToolsUI;
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
		customizationToolsUI = createToolsUI();
		customizationToolsRenderer = createToolsRenderer();

	}

	public SwingRenderer getToolsRenderer() {
		return customizationToolsRenderer;
	}

	public CustomizationToolsUI getToolsUI() {
		return customizationToolsUI;
	}

	public InfoCustomizations getToolsCustomizations() {
		return customizationToolsCustomizations;
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
			return new SwingCustomizer(customizationToolsUI, customizationToolsCustomizations,
					customizationToolsCustomizationsOutputFilePath) {

				@Override
				protected CustomizationTools createCustomizationTools() {
					return new CustomizationTools(this) {

						@Override
						protected SwingRenderer createToolsRenderer() {
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

	protected CustomizationToolsUI createToolsUI() {
		return new CustomizationToolsUI(swingCustomizer);
	}

	public JButton makeSaveControl() {
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

	public Component makeTypeInfoCustomizer(final InfoCustomizations infoCustomizations, final Object object) {
		final ITypeInfo customizedType = this.swingCustomizer.getReflectionUI()
				.getTypeInfo(this.swingCustomizer.getReflectionUI().getTypeInfoSource(object));
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		result.setToolTipText(
				customizationToolsRenderer.prepareStringToDisplay(getCustomizationTitle(customizedType.getName())));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(CustomizationTools.this.customizationToolsRenderer
						.prepareStringToDisplay("Type Options (Shared)...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openTypeCustomizationDialog(result, infoCustomizations, customizedType);
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Refresh")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							rebuildForm(result);
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Lock")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							hideCustomizationTools(result, customizedType.getName());
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
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
		openCustomizationEditor(activatorComponent, t);
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

	protected void openCustomizationEditor(final Component activatorComponent, final Object customization) {
		StandardEditorBuilder dialogBuilder = new StandardEditorBuilder(customizationToolsRenderer, activatorComponent,
				customization) {

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
					rebuildForm(activatorComponent);
				}
			});
		}
	}

	public Component makeFieldInfoCustomizer(final InfoCustomizations infoCustomizations,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		SwingRendererUtils.setMultilineToolTipText(result, customizationToolsRenderer
				.prepareStringToDisplay(getCustomizationTitle(fieldControlPlaceHolder.getField().getName())));
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
				IFieldControlData controlData = fieldControlPlaceHolder.getControlData();
				if (controlData == null) {
					return null;
				}
				return controlData.getType();
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
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Hide")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							hideField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName());
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Up")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(), -1);
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Down")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(), 1);
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move To Top")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(),
									Short.MIN_VALUE);
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move To Bottom")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(),
									Short.MAX_VALUE);
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});

				Component fieldControl = fieldControlPlaceHolder.getFieldControl();
				if (fieldControl instanceof NullableControl) {
					fieldControl = ((NullableControl) fieldControl).getSubControl();
				}
				IFieldControlPlugin fieldControlPlugin = swingCustomizer.getPluginByFieldControl().get(fieldControl);
				if (fieldControlPlugin != null) {
					if (fieldControlPlugin instanceof ICustomizableFieldControlPlugin) {
						TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations,
								getParentFormObjectCustomizedType().getName(), true);
						FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, getFieldName(), true);
						popupMenu.add(((ICustomizableFieldControlPlugin) fieldControlPlugin)
								.makeFieldCustomizerMenuItem(fieldControlPlaceHolder, fc, CustomizationTools.this));
					}
				}

				for (JMenuItem menuItem : makeFieldTypeInfoCustomizers(fieldControlPlaceHolder, "Type Options...",
						infoCustomizations, getFieldCustomization().getSpecificTypeCustomizations(), getFieldType())) {
					popupMenu.add(menuItem);
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

	public List<JMenuItem> makeFieldTypeInfoCustomizers(final FieldControlPlaceHolder fieldControlPlaceHolder,
			final String mainCaption, final InfoCustomizations sharedInfoCustomizations,
			final InfoCustomizations specificTypeCustomizations, final ITypeInfo fieldType) {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		result.add(new JMenuItem(new AbstractAction(
				CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay(mainCaption)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				openTypeCustomizationDialog(fieldControlPlaceHolder, specificTypeCustomizations, fieldType);
			}
		}));
		if (fieldType instanceof IListTypeInfo) {
			result.add(makeListInfoCustomizer(fieldControlPlaceHolder, specificTypeCustomizations,
					(IListTypeInfo) fieldType));
		}
		if (fieldType instanceof IEnumerationTypeInfo) {
			result.add(makeEnumerationCustomizer(fieldControlPlaceHolder, specificTypeCustomizations,
					(IEnumerationTypeInfo) fieldType));
		}

		if (sharedInfoCustomizations != null) {
			if (swingCustomizer.getCustomizationOptions().areFieldSharedTypeOptionsDisplayed()) {
				final JMenu sharedTypeInfoSubMenu = new JMenu(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Shared"));
				result.add(sharedTypeInfoSubMenu);
				for (JMenuItem menuItem : makeFieldTypeInfoCustomizers(fieldControlPlaceHolder, mainCaption, null,
						sharedInfoCustomizations, fieldType)) {
					sharedTypeInfoSubMenu.add(menuItem);
				}
			}
		}
		return result;

	}

	public JMenuItem makeListInfoCustomizer(final Component activatorComponent,
			final InfoCustomizations infoCustomizations, final IListTypeInfo customizedListType) {
		JMenu result = new JMenu(this.customizationToolsRenderer.prepareStringToDisplay("List"));
		{
			result.add(new AbstractAction(this.customizationToolsRenderer.prepareStringToDisplay("Move Columns...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListColumnsOrderDialog(activatorComponent, infoCustomizations, customizedListType);
				}
			});
			result.add(new AbstractAction(this.swingCustomizer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openListCutomizationDialog(activatorComponent, infoCustomizations, customizedListType);
				}
			});
		}
		return result;
	}

	public JMenuItem makeEnumerationCustomizer(final Component activatorComponent,
			final InfoCustomizations infoCustomizations, final IEnumerationTypeInfo customizedEnumType) {
		JMenu result = new JMenu(this.customizationToolsRenderer.prepareStringToDisplay("Enumeration"));
		{
			result.add(new AbstractAction(this.customizationToolsRenderer.prepareStringToDisplay("More Options...")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openEnumerationCutomizationDialog(activatorComponent, infoCustomizations, customizedEnumType);
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
		rebuildForm(activatorComponent);
	}

	protected void hideField(Component activatorComponent, ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		fc.setHidden(true);
		rebuildForm(activatorComponent);
	}

	protected void moveField(Component activatorComponent, ITypeInfo customizedType, String fieldName, int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		tc.moveField(customizedType.getFields(), fieldName, offset);
		rebuildForm(activatorComponent);
	}

	protected void moveMethod(Component activatorComponent, ITypeInfo customizedType, String methodSignature,
			int offset) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.swingCustomizer.getInfoCustomizations(),
				customizedType.getName(), true);
		tc.moveMethod(customizedType.getMethods(), methodSignature, offset);
		rebuildForm(activatorComponent);
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
					rebuildForm(activatorComponent);
				}
			});
		}
	}

	protected void openEnumerationCutomizationDialog(Component activatorComponent,
			InfoCustomizations infoCustomizations, final IEnumerationTypeInfo customizedEnumType) {
		EnumerationCustomization ec = InfoCustomizations.getEnumerationCustomization(infoCustomizations,
				customizedEnumType.getName(), true);
		updateEnumerationCustomization(ec, customizedEnumType);
		openCustomizationEditor(activatorComponent, ec);
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
		openCustomizationEditor(activatorComponent, lc);
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

	protected void openFieldCutomizationDialog(Component activatorComponent, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String fieldName) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		FieldCustomization fc = InfoCustomizations.getFieldCustomization(t, fieldName, true);
		openCustomizationEditor(activatorComponent, fc);
	}

	protected void openMethodCutomizationDialog(Component activatorComponent, InfoCustomizations infoCustomizations,
			final ITypeInfo customizedType, String methodSignature) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations, customizedType.getName(),
				true);
		MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, methodSignature, true);
		openCustomizationEditor(activatorComponent, mc);
	}

	protected void updateMethodCustomization(MethodCustomization mc, IMethodInfo customizedMethod) {
		for (IParameterInfo param : customizedMethod.getParameters()) {
			InfoCustomizations.getParameterCustomization(mc, param.getName(), true);
		}
	}

	public Component makeMethodInfoCustomizer(final InfoCustomizations infoCustomizations,
			final MethodControlPlaceHolder methodControlPlaceHolder) {
		final JButton result = createToolAccessButton(this.swingCustomizer.getCustomizationsIcon());
		SwingRendererUtils.setMultilineToolTipText(result, customizationToolsRenderer.prepareStringToDisplay(
				getCustomizationTitle(ReflectionUIUtils.getMethodSignature(methodControlPlaceHolder.getMethod()))));
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
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Hide")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							hideMethod(result, getParentFormObjectCustomizedType(), getMethodInfoSignature());
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Left")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveMethod(result, getParentFormObjectCustomizedType(), getMethodInfoSignature(), -1);
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Right")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							moveMethod(result, getParentFormObjectCustomizedType(), getMethodInfoSignature(), 1);
						} catch (Throwable t) {
							customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, t);
						}
					}
				});
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

	protected String getCustomizationTitle(String targetName) {
		String result = "Customize";
		if (targetName.length() > 0) {
			result += " (" + targetName + ")";
		}
		return result;
	}

	public void rebuildForm(Component customizedFormComponent) {
		final JPanel form;
		if (SwingRendererUtils.isForm(customizedFormComponent, this.swingCustomizer)) {
			form = (JPanel) customizedFormComponent;
		} else {
			form = SwingRendererUtils.findParentForm(customizedFormComponent, this.swingCustomizer);
		}
		swingCustomizer.recreateFormContent(form);
		swingCustomizer.updateFormStatusBarInBackground(form);
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