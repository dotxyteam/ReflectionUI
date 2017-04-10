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
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IMethodControlData;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
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
import xy.reflect.ui.info.type.util.InfoCustomizations.FieldTypeSpecificities;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ResourcePath;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
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
		customizationToolsUI = createCustomizationToolsUI();
		customizationToolsRenderer = createCustomizationToolsRenderer();

	}

	public SwingRenderer getCustomizationToolsRenderer() {
		return customizationToolsRenderer;
	}

	public CustomizationToolsUI getCustomizationToolsUI() {
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

	protected CustomizationToolsUI createCustomizationToolsUI() {
		return new CustomizationToolsUI();
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
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Refresh")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						updateUI(result);
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Lock")) {
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
					updateUI(activatorComponent);
				}
			});
		}
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
						hideField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName());
					}
				});
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Move Up")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(fieldControlPlaceHolder, getParentFormObjectCustomizedType(), getFieldName(), -1);
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
				addMemberTypeInfoCustomizers(popupMenu, fieldControlPlaceHolder, "Type Options...", infoCustomizations,
						getFieldCustomization().getSpecificTypeCustomizations(), getFieldType());
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

	protected void addMemberTypeInfoCustomizers(JPopupMenu popupMenu, final Component activatorComponent,
			final String mainCaption, final InfoCustomizations sharedInfoCustomizations,
			final InfoCustomizations specificTypeCustomizations, final ITypeInfo memberType) {
		popupMenu.add(new AbstractAction(
				CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay(mainCaption)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				openTypeCustomizationDialog(activatorComponent, specificTypeCustomizations, memberType);
			}
		});
		if (memberType instanceof IListTypeInfo) {
			popupMenu.add(createListInfoCustomizer(specificTypeCustomizations, activatorComponent,
					(IListTypeInfo) memberType));
		}
		if (memberType instanceof IEnumerationTypeInfo) {
			popupMenu.add(createEnumerationCustomizer(activatorComponent, specificTypeCustomizations,
					(IEnumerationTypeInfo) memberType));
		}
		if (swingCustomizer.getCustomizationOptions().areFieldSharedTypeOptionsDisplayed()) {
			final JMenu sharedTypeInfoSubMenu = new JMenu(
					CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Shared"));
			popupMenu.add(sharedTypeInfoSubMenu);
			sharedTypeInfoSubMenu.add(new AbstractAction(
					CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay(mainCaption)) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openTypeCustomizationDialog(activatorComponent, sharedInfoCustomizations, memberType);
				}
			});
			if (memberType instanceof IListTypeInfo) {
				sharedTypeInfoSubMenu.add(createListInfoCustomizer(sharedInfoCustomizations, activatorComponent,
						(IListTypeInfo) memberType));
			}
			if (memberType instanceof IEnumerationTypeInfo) {
				sharedTypeInfoSubMenu.add(createEnumerationCustomizer(activatorComponent, sharedInfoCustomizations,
						(IEnumerationTypeInfo) memberType));
			}
		}

	}

	protected JMenuItem createListInfoCustomizer(final InfoCustomizations infoCustomizations,
			final Component activatorComponent, final IListTypeInfo customizedListType) {
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

	protected JMenuItem createEnumerationCustomizer(final Component activatorComponent,
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

			private ITypeInfo getMethodReturnValueType() {
				IMethodControlData controlData = methodControlPlaceHolder.getControlData();
				if (controlData == null) {
					return null;
				}
				return controlData.getReturnValueType();
			}

			private MethodCustomization getMethodCustomization() {
				TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations,
						getParentFormObjectCustomizedType().getName(), true);
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(t, getMethodInfoSignature(), true);
				return mc;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(
						CustomizationTools.this.customizationToolsRenderer.prepareStringToDisplay("Hide")) {
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
				if (getMethodReturnValueType() != null) {
					addMemberTypeInfoCustomizers(popupMenu, methodControlPlaceHolder, "Return Value Type Options...",
							infoCustomizations, getMethodCustomization().getSpecificReturnValueTypeCustomizations(),
							getMethodReturnValueType());
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

	protected class CustomizationToolsUI extends ReflectionUI {

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
						result.add(getCustomControlForbiddingField());
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
						TypeCustomization tc = findParentTypeCustomization((AbstractMemberCustomization) object,
								CustomizationTools.this.swingCustomizer.getInfoCustomizations());
						List<CustomizationCategory> categories = tc.getMemberCategories();
						return categories.toArray(new CustomizationCategory[categories.size()]);
					} else {
						return super.getValueOptions(object, field, containingType);
					}
				}

				protected TypeCustomization findParentTypeCustomization(AbstractMemberCustomization custumizationMember,
						InfoCustomizations infoCustomizations) {
					for (TypeCustomization tc : infoCustomizations.getTypeCustomizations()) {
						for (FieldCustomization fc : tc.getFieldsCustomizations()) {
							if (fc == custumizationMember) {
								return tc;
							}
						}
						for (MethodCustomization mc : tc.getMethodsCustomizations()) {
							if (mc == custumizationMember) {
								return tc;
							}
						}
					}
					return null;
				}

				@Override
				protected List<IMethodInfo> getMethods(ITypeInfo type) {
					if (type.getName().equals(ListCustomization.class.getName())) {
						List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
						result.add(getListItemTypeCustomizationDisplayMethod(swingCustomizer.getInfoCustomizations()));
						return result;
					} else {
						return super.getMethods(type);
					}
				}

				@Override
				protected String toString(ITypeInfo type, Object object) {
					if (object instanceof CustomizationCategory) {
						return ((CustomizationCategory) object).getCaption();
					} else {
						return super.toString(type, object);
					}
				}

			}.get(result);
			result = customizationToolsCustomizations.get(thisReflectionUI, result);
			return result;
		}

		protected IMethodInfo getListItemTypeCustomizationDisplayMethod(final InfoCustomizations infoCustomizations) {
			return new IMethodInfo() {

				@Override
				public boolean isReturnValueDetached() {
					return false;
				}

				@Override
				public Map<String, Object> getSpecificProperties() {
					return Collections.emptyMap();
				}

				@Override
				public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
					return null;
				}

				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public String getName() {
					return "displayItemTypeCustomization";
				}

				@Override
				public String getCaption() {
					return "Display Item Type Customization";
				}

				@Override
				public void validateParameters(Object object, InvocationData invocationData) throws Exception {
				}

				@Override
				public boolean isReadOnly() {
					return true;
				}

				@Override
				public Object invoke(final Object object, InvocationData invocationData) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							ListCustomization lc = (ListCustomization) object;
							if (lc.getItemTypeName() == null) {
								customizationToolsRenderer.openInformationDialog(null, "The item type is not defined",
										customizationToolsRenderer.getObjectTitle(lc),
										customizationToolsRenderer.getObjectIconImage(lc));
							} else {
								TypeCustomization t = InfoCustomizations.getTypeCustomization(infoCustomizations,
										lc.getItemTypeName());
								openCustomizationEditor(null, t);
							}
						}
					});
					return null;
				}

				@Override
				public ValueReturnMode getValueReturnMode() {
					return ValueReturnMode.DIRECT_OR_PROXY;
				}

				@Override
				public Runnable getUndoJob(Object object, InvocationData invocationData) {
					return null;
				}

				@Override
				public ITypeInfo getReturnValueType() {
					return null;
				}

				@Override
				public List<IParameterInfo> getParameters() {
					return Collections.emptyList();
				}

				@Override
				public String getNullReturnValueLabel() {
					return null;
				}

				@Override
				public InfoCategory getCategory() {
					return null;
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
					DesktopSpecificProperty.setSubFormExpanded(
							DesktopSpecificProperty.accessCustomizationsProperties(f), (Boolean) value);
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

		protected IFieldInfo getCustomControlForbiddingField() {
			return new IFieldInfo() {

				@Override
				public String getName() {
					return "customControlForbidden";
				}

				@Override
				public String getCaption() {
					return "Forbid Custom Control";
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
							.isCustumControlForbidden(DesktopSpecificProperty.accessCustomizationsProperties(f));
				}

				@Override
				public void setValue(Object object, Object value) {
					FieldCustomization f = (FieldCustomization) object;
					DesktopSpecificProperty.setCustumControlForbidden(
							DesktopSpecificProperty.accessCustomizationsProperties(f), (Boolean) value);
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
					DesktopSpecificProperty.setIconImageFilePath(
							DesktopSpecificProperty.accessCustomizationsProperties(c), pathSpecification);
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

	};

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