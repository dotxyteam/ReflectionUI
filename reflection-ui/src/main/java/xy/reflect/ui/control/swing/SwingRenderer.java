package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.FieldControlData;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsOneListField.ListItem;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.map.StandardMapEntry;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationFactory;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.CompositeModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.component.AutoResizeTabbedPane;
import xy.reflect.ui.util.component.ScrollPaneOptions;
import xy.reflect.ui.util.component.WrapLayout;

@SuppressWarnings("unused")
public class SwingRenderer {

	protected static SwingRenderer defaultInstance;

	protected ReflectionUI reflectionUI;
	protected Map<JPanel, Object> objectByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, ModificationStack> modificationStackByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Boolean> fieldsUpdateListenerDisabledByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, IInfoFilter> infoFilterByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, JLabel> statusBarByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> fieldControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<MethodControlPlaceHolder>>> methodControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<IMethodInfo, InvocationData> lastInvocationDataByMethod = new HashMap<IMethodInfo, InvocationData>();
	protected Map<FieldControlPlaceHolder, Component> captionControlByFieldControlPlaceHolder = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, JTabbedPane> categoriesTabbedPaneByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Boolean> busyIndicationDisabledByForm = new MapMaker().weakKeys().makeMap();

	public SwingRenderer(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	public static SwingRenderer getDefault() {
		if (defaultInstance == null) {
			if (SystemProperties.areDefaultInfoCustomizationsActive()) {
				defaultInstance = new SwingCustomizer(ReflectionUI.getDefault(), InfoCustomizations.getDefault(),
						SystemProperties.getDefaultInfoCustomizationsFilePath());
			} else {
				defaultInstance = new SwingRenderer(ReflectionUI.getDefault());
			}
		}
		return defaultInstance;
	}

	public ReflectionUI getReflectionUI() {
		return reflectionUI;
	}

	public Map<JPanel, Object> getObjectByForm() {
		return objectByForm;
	}

	public Map<JPanel, ModificationStack> getModificationStackByForm() {
		return modificationStackByForm;
	}

	public Map<JPanel, Boolean> getFieldsUpdateListenerDisabledByForm() {
		return fieldsUpdateListenerDisabledByForm;
	}

	public Map<IMethodInfo, InvocationData> getLastInvocationDataByMethod() {
		return lastInvocationDataByMethod;
	}

	public Map<JPanel, IInfoFilter> getInfoFilterByForm() {
		return infoFilterByForm;
	}

	public Map<JPanel, JLabel> getStatusBarByForm() {
		return statusBarByForm;
	}

	public Map<JPanel, Boolean> getBusyIndicationDisabledByForm() {
		return busyIndicationDisabledByForm;
	}

	public Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> getFieldControlPlaceHoldersByCategoryByForm() {
		return fieldControlPlaceHoldersByCategoryByForm;
	}

	public Map<JPanel, Map<InfoCategory, List<MethodControlPlaceHolder>>> getMethodControlPlaceHoldersByCategoryByForm() {
		return methodControlPlaceHoldersByCategoryByForm;
	}

	public String prepareStringToDisplay(String string) {
		return string;
	}

	public String getObjectTitle(Object object) {
		if (object == null) {
			return "(Missing Value)";
		}
		return reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object)).getCaption();
	}

	public void setupWindow(Window window, Component content, List<? extends Component> toolbarControls, String title,
			Image iconImage) {
		if (window instanceof JFrame) {
			((JFrame) window).setTitle(prepareStringToDisplay(title));
		} else if (window instanceof JDialog) {
			((JDialog) window).setTitle(prepareStringToDisplay(title));
		}
		if (iconImage == null) {
			window.setIconImage(SwingRendererUtils.NULL_ICON_IMAGE);
		} else {
			window.setIconImage(iconImage);
		}
		Container contentPane = createWindowContentPane(window, content, toolbarControls);
		SwingRendererUtils.setContentPane(window, contentPane);
		SwingRendererUtils.adjustWindowInitialBounds(window);
	}

	public List<Component> createCommonToolbarControls(final JPanel form) {
		Object object = getObjectByForm().get(form);
		if (object == null) {
			return null;
		}
		List<Component> result = new ArrayList<Component>();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if ((type.getOnlineHelp() != null) && (type.getOnlineHelp().trim().length() > 0)) {
			result.add(createOnlineHelpControl(type.getOnlineHelp()));
		}
		if (type.isModificationStackAccessible()) {
			final ModificationStack stack = getModificationStackByForm().get(form);
			if (stack != null) {
				result.addAll(new ModificationStackControls(stack).createControls(this));
			}
		}
		return result;

	}

	public FieldControlPlaceHolder createFieldControlPlaceHolder(Object object, IFieldInfo field) {
		return new FieldControlPlaceHolder(object, field);
	}

	public JPanel createFieldsPanel(List<FieldControlPlaceHolder> fielControlPlaceHolders) {
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new GridBagLayout());
		int spacing = 5;
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FieldControlPlaceHolder fieldControlPlaceHolder = fielControlPlaceHolders.get(i);
			{
				GridBagConstraints layoutConstraints = new GridBagConstraints();
				layoutConstraints.gridy = i;
				fieldsPanel.add(fieldControlPlaceHolder, layoutConstraints);
				updateFieldControlLayout(fieldControlPlaceHolder);
			}
			IFieldInfo field = fieldControlPlaceHolder.getField();
			if ((field.getOnlineHelp() != null) && (field.getOnlineHelp().trim().length() > 0)) {
				GridBagConstraints layoutConstraints = new GridBagConstraints();
				layoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
				layoutConstraints.gridx = 2;
				layoutConstraints.gridy = i;
				layoutConstraints.weighty = 1.0;
				fieldsPanel.add(createOnlineHelpControl(field.getOnlineHelp()), layoutConstraints);
			}

		}
		return fieldsPanel;
	}

	public JFrame createFrame(Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls) {
		final JFrame frame = new JFrame();
		setupWindow(frame, content, toolbarControls, title, iconImage);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	public MethodControl createMethodControl(final Object object, final IMethodInfo method) {
		return new MethodControl(this, object, method);
	}

	public MethodAction createMethodAction(Object object, IMethodInfo method) {
		return new MethodAction(this, object, method);
	}

	public JPanel createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
		JPanel methodsPanel = new JPanel();
		methodsPanel.setLayout(new WrapLayout(WrapLayout.CENTER));
		for (final Component methodControl : methodControlPlaceHolders) {
			JPanel methodControlContainer = new JPanel() {
				protected static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension result = super.getPreferredSize();
					if (result == null) {
						return super.getPreferredSize();
					}
					int maxMethodControlWidth = 0;
					for (final Component methodControl : methodControlPlaceHolders) {
						Dimension controlPreferredSize = methodControl.getPreferredSize();
						if (controlPreferredSize != null) {
							maxMethodControlWidth = Math.max(maxMethodControlWidth, controlPreferredSize.width);
						}
					}
					result.width = maxMethodControlWidth;
					return result;
				}
			};

			methodControlContainer.setLayout(new BorderLayout());
			methodControlContainer.add(methodControl, BorderLayout.CENTER);
			methodsPanel.add(methodControlContainer);
		}
		return methodsPanel;
	}

	public JTabbedPane createMultipleInfoCategoriesComponent(final SortedSet<InfoCategory> allCategories,
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory) {
		final JTabbedPane tabbedPane = new JTabbedPane();
		for (final InfoCategory category : allCategories) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory.get(category);
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
					.get(category);
			if (methodControlPlaceHolders == null) {
				methodControlPlaceHolders = Collections.emptyList();
			}

			JPanel tab = new JPanel();
			tabbedPane.addTab(prepareStringToDisplay(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutControls(fieldControlPlaceHolders, methodControlPlaceHolders, tabContent);
		}
		return tabbedPane;
	}

	public JPanel createForm(Object object) {
		return createForm(object, IInfoFilter.DEFAULT);
	}

	public JPanel createForm(final Object object, IInfoFilter infoFilter) {
		final String formTitle = "Form of " + ReflectionUIUtils.toString(reflectionUI, object);
		final ModificationStack modifStack = new ModificationStack(formTitle);
		JPanel result = new JPanel() {

			@Override
			public String toString() {
				return formTitle;
			}

			private static final long serialVersionUID = 1L;
			JPanel form = this;
			IModificationListener fieldsUpdateListener = new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					if (Boolean.TRUE.equals(getFieldsUpdateListenerDisabledByForm().get(form))) {
						return;
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							refreshAllFieldControls(form, false);
							updateStatusBarInBackground(form);
							for (JPanel otherForm : getForms(object)) {
								if (otherForm != form) {
									ModificationStack otherModifStack = getModificationStackByForm().get(otherForm);
									getFieldsUpdateListenerDisabledByForm().put(otherForm, Boolean.TRUE);
									otherModifStack.invalidate();
									getFieldsUpdateListenerDisabledByForm().put(otherForm, Boolean.FALSE);
								}
							}
						}
					});
				}
			};

			@Override
			public void addNotify() {
				super.addNotify();
				modifStack.addListener(fieldsUpdateListener);
				getObjectByForm().put(this, object);
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				modifStack.removeListener(fieldsUpdateListener);
				getObjectByForm().remove(this);
			}

		};
		getObjectByForm().put(result, object);
		getModificationStackByForm().put(result, modifStack);
		getInfoFilterByForm().put(result, infoFilter);
		result.setLayout(new BorderLayout());
		fillForm(result);
		return result;
	}

	public boolean hasCustomFieldControl(Object object, IFieldInfo field) {
		if (field.getType() instanceof IEnumerationTypeInfo) {
			return true;
		} else if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(field.getType())) {
			return true;
		} else {
			if (field.getValueOptions(object) != null) {
				return true;
			} else {
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					return true;
				} else {
					Class<?> javaType;
					try {
						javaType = ClassUtils.getCachedClassforName(fieldType.getName());
					} catch (ClassNotFoundException e) {
						return false;
					}
					if (javaType == Color.class) {
						return true;
					} else if (BooleanTypeInfo.isCompatibleWith(javaType)) {
						return true;
					} else if (TextualTypeInfo.isCompatibleWith(javaType)) {
						return true;
					} else if (FileTypeInfo.isCompatibleWith(javaType)) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
	}

	public IControlData getFieldControlData(final Object object, final IFieldInfo field) {
		if (field.getValueOptions(object) != null) {
			ITypeInfo ownerType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
			final ArrayAsEnumerationFactory enumFactory = new ArrayAsEnumerationFactory(reflectionUI,
					field.getValueOptions(object),
					"ValueOptions [ownerType=" + ownerType.getName() + ", field=" + field.getName() + "]", "");
			final ITypeInfo enumType = reflectionUI.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
			return new FieldControlData(object, field) {

				@Override
				public Object getValue() {
					Object value = super.getValue();
					return enumFactory.getInstance(value);
				}

				@Override
				public void setValue(Object value) {
					value = enumFactory.unwrapInstance(value);
					field.setValue(object, value);
				}

				@Override
				public Runnable getCustomUndoUpadteJob(Object value) {
					value = enumFactory.unwrapInstance(value);
					return field.getCustomUndoUpdateJob(object, value);
				}

				@Override
				public ITypeInfo getType() {
					return enumType;
				}

			};
		}
		return new FieldControlData(object, field);
	}

	public Component createFieldControl(final IControlData fieldControlData) {
		if (fieldControlData.getType() instanceof IEnumerationTypeInfo) {
			return new EnumerationControl(this, fieldControlData);
		} else if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(fieldControlData.getType())) {
			return new PolymorphicControl(this, fieldControlData);
		} else {
			if (fieldControlData.isNullable()) {
				return new NullableControl(this, fieldControlData) {

					private static final long serialVersionUID = 1L;

					@Override
					protected Component createNonNullValueControl(IControlData data) {
						Component result = SwingRenderer.this.createFieldControl(data);
						return result;
					}

					@Override
					protected Object getDefaultValue() {
						Object newValue = null;
						try {
							newValue = this.swingRenderer.onTypeInstanciationRequest(this, data.getType(), false);
						} catch (Throwable t) {
							swingRenderer.handleExceptionsFromDisplayedUI(this, t);
							newValue = null;
						}
						return newValue;
					}

				};
			}
			Component result = createCustomNonNullFieldValueControl(fieldControlData);
			if (result != null) {
				return result;
			}
			Object value = fieldControlData.getValue();
			final ITypeInfo valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
			if (!valueType.equals(fieldControlData.getType())) {
				return createFieldControl(new ControlDataProxy(fieldControlData) {
					@Override
					public ITypeInfo getType() {
						return valueType;
					}
				});
			}
			if (DesktopSpecificProperty
					.isSubFormExpanded(DesktopSpecificProperty.accessControlDataProperties(fieldControlData))) {
				return new EmbeddedFormControl(this, fieldControlData);
			} else {
				return new DialogAccessControl(this, fieldControlData);
			}
		}
	}

	public Component createCustomNonNullFieldValueControl(IControlData data) {
		ITypeInfo fieldType = data.getType();
		if (fieldType instanceof IListTypeInfo) {
			return new ListControl(this, data);
		} else {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassforName(fieldType.getName());
			} catch (ClassNotFoundException e) {
				return null;
			}
			if (javaType == Color.class) {
				return new ColorControl(this, data);
			} else if (BooleanTypeInfo.isCompatibleWith(javaType)) {
				return new CheckBoxControl(this, data);
			} else if (TextualTypeInfo.isCompatibleWith(javaType)) {
				if (javaType == String.class) {
					return new TextControl(this, data);
				} else {
					return new PrimitiveValueControl(this, data, javaType);
				}
			} else if (FileTypeInfo.isCompatibleWith(javaType)) {
				return new FileControl(this, data);
			} else {
				return null;
			}
		}
	}

	public Component createOnlineHelpControl(String onlineHelp) {
		final JButton result = new JButton(SwingRendererUtils.HELP_ICON);
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result, prepareStringToDisplay(onlineHelp));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingRendererUtils.showTooltipNow(result);
			}
		});
		return result;
	}

	public Component createStatusBar(JPanel form) {
		JLabel result = new JLabel();
		result.setOpaque(true);
		result.setFont(new JToolTip().getFont());
		result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		getStatusBarByForm().put(form, result);
		return result;
	}

	public Component createToolBar(List<? extends Component> toolbarControls) {
		JPanel result = new JPanel();
		result.setBorder(BorderFactory.createRaisedBevelBorder());
		result.setLayout(new FlowLayout(FlowLayout.CENTER));
		for (Component tool : toolbarControls) {
			result.add(tool);
		}
		return result;
	}

	public Container createWindowContentPane(Window window, Component content,
			List<? extends Component> toolbarControls) {
		final JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		if (content != null) {
			if (SwingRendererUtils.isForm(content, this)) {
				final JPanel form = (JPanel) content;
				contentPane.add(createStatusBar(form), BorderLayout.NORTH);
				setStatusBarError(form, null);
				window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowOpened(WindowEvent e) {
						updateStatusBarInBackground(form);
					}
				});

			}
			final JScrollPane scrollPane = new JScrollPane(new ScrollPaneOptions(content, true, false));
			scrollPane.getViewport().setOpaque(false);
			contentPane.add(scrollPane, BorderLayout.CENTER);
		}
		if (toolbarControls != null) {
			if (toolbarControls.size() > 0) {
				contentPane.add(createToolBar(toolbarControls), BorderLayout.SOUTH);
			}
		}
		return contentPane;
	}

	public void recreateFormContent(JPanel form) {
		InfoCategory category = getDisplayedInfoCategory(form);
		form.removeAll();
		fillForm(form);
		if (category != null) {
			setDisplayedInfoCategory(form, category);
		}
		Window window = SwingUtilities.getWindowAncestor(form);
		if (window != null) {
			window.validate();
		}
	}

	public void setDisplayedInfoCategory(JPanel form, InfoCategory category) {
		JTabbedPane categoriesControl = categoriesTabbedPaneByForm.get(form);
		if (categoriesControl != null) {
			for (int i = 0; i < categoriesControl.getTabCount(); i++) {
				String categoryCaption = categoriesControl.getTitleAt(i);
				if (category.getCaption().equals(categoryCaption)) {
					if (category.getPosition() != -1) {
						if (category.getPosition() != i) {
							continue;
						}
					}
					categoriesControl.setSelectedIndex(i);
					return;
				}
			}
		}
	}

	public InfoCategory getDisplayedInfoCategory(JPanel form) {
		JTabbedPane categoriesControl = categoriesTabbedPaneByForm.get(form);
		if (categoriesControl != null) {
			int currentCategoryIndex = categoriesControl.getSelectedIndex();
			if (currentCategoryIndex != -1) {
				String currentCategoryCaption = categoriesControl.getTitleAt(currentCategoryIndex);
				return new InfoCategory(currentCategoryCaption, currentCategoryIndex);
			}
		}
		return null;
	}

	public void fillForm(JPanel form) {
		Object object = getObjectByForm().get(form);
		IInfoFilter infoFilter = getInfoFilterByForm().get(form);

		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FieldControlPlaceHolder>>();
		getFieldControlPlaceHoldersByCategoryByForm().put(form, fieldControlPlaceHoldersByCategory);
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		List<IFieldInfo> fields = type.getFields();
		for (IFieldInfo field : fields) {
			if (infoFilter.excludeField(field)) {
				continue;
			}
			FieldControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(object, field);
			{
				InfoCategory category = field.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
						.get(category);
				if (fieldControlPlaceHolders == null) {
					fieldControlPlaceHolders = new ArrayList<FieldControlPlaceHolder>();
					fieldControlPlaceHoldersByCategory.put(category, fieldControlPlaceHolders);
				}
				fieldControlPlaceHolders.add(fieldControlPlaceHolder);
			}
		}

		Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<MethodControlPlaceHolder>>();
		getMethodControlPlaceHoldersByCategoryByForm().put(form, methodControlPlaceHoldersByCategory);
		List<IMethodInfo> methods = type.getMethods();
		for (IMethodInfo method : methods) {
			if (infoFilter.excludeMethod(method)) {
				continue;
			}
			MethodControlPlaceHolder methodControlPlaceHolder = createMethodControlPlaceHolder(object, method);
			{
				InfoCategory category = method.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
						.get(category);
				if (methodControlPlaceHolders == null) {
					methodControlPlaceHolders = new ArrayList<MethodControlPlaceHolder>();
					methodControlPlaceHoldersByCategory.put(category, methodControlPlaceHolders);
				}
				methodControlPlaceHolders.add(methodControlPlaceHolder);
			}
		}

		JPanel formContent = new JPanel();

		SortedSet<InfoCategory> allCategories = new TreeSet<InfoCategory>();
		allCategories.addAll(fieldControlPlaceHoldersByCategory.keySet());
		allCategories.addAll(methodControlPlaceHoldersByCategory.keySet());
		if ((allCategories.size() == 1) && (getNullInfoCategory().equals(allCategories.iterator().next()))) {
			form.add(formContent, BorderLayout.CENTER);
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
					.get(allCategories.first());
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
					.get(allCategories.first());
			if (methodControlPlaceHolders == null) {
				methodControlPlaceHolders = Collections.emptyList();
			}
			layoutControls(fieldControlPlaceHolders, methodControlPlaceHolders, formContent);
		} else if (allCategories.size() > 0) {
			form.add(formContent, BorderLayout.CENTER);
			formContent.setLayout(new BorderLayout());
			JTabbedPane categoriesControl = createMultipleInfoCategoriesComponent(allCategories,
					fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory);
			categoriesTabbedPaneByForm.put(form, categoriesControl);
			formContent.add(categoriesControl, BorderLayout.CENTER);
		}
	}

	public InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1);
	}

	public MethodControlPlaceHolder createMethodControlPlaceHolder(Object object, IMethodInfo method) {
		return new MethodControlPlaceHolder(object, method);
	}

	public List<FieldControlPlaceHolder> getFieldControlPlaceHolders(JPanel form) {
		List<FieldControlPlaceHolder> result = new ArrayList<FieldControlPlaceHolder>();
		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = getFieldControlPlaceHoldersByCategoryByForm()
				.get(form);
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory.get(category);
			result.addAll(fieldControlPlaceHolders);
		}
		return result;
	}

	public List<MethodControlPlaceHolder> getMethodControlPlaceHolders(JPanel form) {
		List<MethodControlPlaceHolder> result = new ArrayList<MethodControlPlaceHolder>();
		Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = getMethodControlPlaceHoldersByCategoryByForm()
				.get(form);
		for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
			List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
					.get(category);
			result.addAll(methodControlPlaceHolders);
		}
		return result;
	}

	public int getFocusedFieldControlPaceHolderIndex(JPanel subForm) {
		int i = 0;
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(subForm)) {
			if (SwingRendererUtils.hasOrContainsFocus(fieldControlPlaceHolder)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public List<JPanel> getForms(Object object) {
		return ReflectionUIUtils.getKeysFromValue(getObjectByForm(), object);
	}

	public IFieldInfo getFormAwareField(final JPanel form, String fieldName) {
		List<FieldControlPlaceHolder> fieldControlPlaceHolders = getFieldControlPlaceHoldersByName(form, fieldName);
		if (fieldControlPlaceHolders.size() == 0) {
			return null;
		}
		IFieldInfo result = fieldControlPlaceHolders.get(0).getField();
		result = new FieldInfoProxy(result) {
			@Override
			public void setValue(Object object, Object value) {
				ModificationStack modifStack = getModificationStackByForm().get(form);
				SwingRendererUtils.setValueThroughModificationStack(object, base, value, modifStack);
			}
		};
		return result;
	}

	public IMethodInfo getFormAwareMethod(final JPanel form, String methodSignature) {
		List<MethodControlPlaceHolder> methodControlPlaceHolders = getMethodControlPlaceHoldersBySignature(form,
				methodSignature);
		if (methodControlPlaceHolders.size() == 0) {
			return null;
		}
		IMethodInfo result = methodControlPlaceHolders.get(0).getMethod();
		result = new MethodInfoProxy(result) {
			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				ModificationStack modifStack = getModificationStackByForm().get(form);
				return SwingRendererUtils.invokeMethodThroughModificationStack(object, base, invocationData,
						modifStack);
			}
		};
		return result;
	}

	public List<FieldControlPlaceHolder> getFieldControlPlaceHoldersByName(JPanel form, String fieldName) {
		List<FieldControlPlaceHolder> result = new ArrayList<FieldControlPlaceHolder>();
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getField().getName())) {
				result.add(fieldControlPlaceHolder);
			}
		}
		return result;
	}

	public List<MethodControlPlaceHolder> getMethodControlPlaceHoldersBySignature(JPanel form, String methodSignature) {
		List<MethodControlPlaceHolder> result = new ArrayList<MethodControlPlaceHolder>();
		for (MethodControlPlaceHolder methodControlPlaceHolder : getMethodControlPlaceHolders(form)) {
			if (ReflectionUIUtils.getMethodSignature(methodControlPlaceHolder.getMethod()).equals(methodSignature)) {
				result.add(methodControlPlaceHolder);
			}
		}
		return result;
	}

	public Color getNullColor() {
		return new JTextArea().getDisabledTextColor();
	}

	public Image getObjectIconImage(Object object) {
		if (object != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
			Image result = SwingRendererUtils.getCachedIconImage(this, type.getSpecificProperties());
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Image getControlDataIconImage(IControlData data) {
		Image result = SwingRendererUtils.getCachedIconImage(this, data.getSpecificProperties());
		if (result != null) {
			return result;
		}
		Object value = data.getValue();
		if (value == null) {
			return null;
		}
		return getObjectIconImage(value);
	}

	public Image getMethodIconImage(Object object, IMethodInfo method) {
		return SwingRendererUtils.getCachedIconImage(this, method.getSpecificProperties());
	}

	public void handleExceptionsFromDisplayedUI(Component activatorComponent, final Throwable t) {
		reflectionUI.logError(t);
		openErrorDialog(activatorComponent, "An Error Occured", t);
	}

	public void layoutControlPanels(JPanel parentForm, JPanel fieldsPanel, JPanel methodsPanel) {
		parentForm.setLayout(new BorderLayout());
		parentForm.add(fieldsPanel, BorderLayout.CENTER);
		parentForm.add(methodsPanel, BorderLayout.SOUTH);
	}

	public void layoutControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, JPanel parentForm) {
		JPanel fieldsPanel = createFieldsPanel(fielControlPlaceHolders);
		JPanel methodsPanel = createMethodsPanel(methodControlPlaceHolders);
		layoutControlPanels(parentForm, fieldsPanel, methodsPanel);
	}

	public Object onTypeInstanciationRequest(final Component activatorComponent, ITypeInfo type, boolean silent) {
		try {
			if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
				List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
				if (polyTypes.size() == 1) {
					type = polyTypes.get(0);
				} else {
					if (silent) {
						type = polyTypes.get(0);
					} else {
						final ArrayAsEnumerationFactory enumFactory = ReflectionUIUtils
								.getPolymorphicTypesEnumerationfactory(reflectionUI, type, polyTypes);
						IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
								.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
						Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null,
								"Choose a type:", "New '" + type.getCaption() + "'");
						if (resultEnumItem == null) {
							return null;
						}
						type = (ITypeInfo) enumFactory.unwrapInstance(resultEnumItem);
					}
				}
			}

			List<IMethodInfo> constructors = type.getConstructors();
			if (constructors.size() == 0) {
				if (type.isConcrete() || silent) {
					throw new ReflectionUIError("No accessible constructor found");
				} else {
					String className = openInputDialog(activatorComponent, "",
							"Create '" + type.getCaption() + "' of type", null);
					if (className == null) {
						return null;
					}
					try {
						type = reflectionUI.getTypeInfo(new JavaTypeInfoSource(Class.forName(className)));
					} catch (ClassNotFoundException e) {
						throw new ReflectionUIError(e);
					}
					if (type == null) {
						return null;
					} else {
						return onTypeInstanciationRequest(activatorComponent, type, silent);
					}
				}
			}

			if (constructors.size() == 1) {
				final IMethodInfo constructor = constructors.get(0);
				if (silent) {
					return constructor.invoke(null, new InvocationData());
				} else {
					MethodAction methodAction = createMethodAction(null, constructor);
					methodAction.setShouldDisplayReturnValueIfAny(false);
					methodAction.execute(activatorComponent);
					return methodAction.getReturnValue();
				}
			}

			constructors = new ArrayList<IMethodInfo>(constructors);
			Collections.sort(constructors, new Comparator<IMethodInfo>() {

				@Override
				public int compare(IMethodInfo o1, IMethodInfo o2) {
					return new Integer(o1.getParameters().size()).compareTo(new Integer(o2.getParameters().size()));
				}
			});

			if (silent) {
				IMethodInfo smallerConstructor = constructors.get(0);
				return smallerConstructor.invoke(null, new InvocationData());
			} else {
				final ArrayAsEnumerationFactory enumFactory = new ArrayAsEnumerationFactory(reflectionUI,
						constructors.toArray(), "ConstructorSelection [type=" + type.getName() + "]", "") {
					protected String getItemCaption(Object choice) {
						return DefaultConstructorInfo.getDescription((IMethodInfo) choice);
					}
				};
				IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
						.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
				Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null, "Choose an option",
						"Create '" + type.getCaption() + "'");
				if (resultEnumItem == null) {
					return null;
				}
				IMethodInfo chosenContructor = (IMethodInfo) enumFactory.unwrapInstance(resultEnumItem);
				if (chosenContructor == null) {
					return null;
				}
				MethodAction methodAction = new MethodAction(this, null, chosenContructor);
				methodAction.setShouldDisplayReturnValueIfAny(false);
				methodAction.execute(activatorComponent);
				return methodAction.getReturnValue();
			}
		} catch (

		Throwable t) {
			throw new ReflectionUIError(
					"Could not create an instance of type '" + type.getName() + "': " + t.toString(), t);

		}

	}

	public Object openSelectionDialog(Component parentComponent, IEnumerationTypeInfo enumType, Object initialEnumItem,
			String message, String title) {
		if (initialEnumItem == null) {
			initialEnumItem = enumType.getPossibleValues()[0];
		}
		final Object[] chosenItemHolder = new Object[] { initialEnumItem };

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, enumType);
		encapsulation.setTypeCaption("Selection");
		encapsulation.setFieldCaption(message);
		encapsulation.setFieldGetOnly(false);
		encapsulation.setFieldNullable(false);
		Object encapsulatedChosenItem = encapsulation.getInstance(chosenItemHolder);

		if (openObjectDialog(parentComponent, encapsulatedChosenItem, title, getObjectIconImage(encapsulatedChosenItem),
				true, true).isOkPressed()) {
			return chosenItemHolder[0];
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T openInputDialog(Component parentComponent, T initialValue, String valueCaption, String title) {
		if (initialValue == null) {
			throw new ReflectionUIError();
		}
		final Object[] valueHolder = new Object[] { initialValue };
		ITypeInfo initialValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(initialValue));

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, initialValueType);
		encapsulation.setTypeCaption("Input");
		encapsulation.setFieldCaption(valueCaption);
		encapsulation.setFieldGetOnly(false);
		encapsulation.setFieldNullable(false);
		Object encapsulatedValue = encapsulation.getInstance(valueHolder);

		if (openObjectDialog(parentComponent, encapsulatedValue, title, getObjectIconImage(encapsulatedValue), true,
				true).isOkPressed()) {
			return (T) valueHolder[0];
		} else {
			return null;
		}
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title) {
		return openQuestionDialog(activatorComponent, question, title, "Yes", "No");
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title, String yesCaption,
			String noCaption) {
		DialogBuilder dialogBuilder = createDialogBuilder(activatorComponent);
		dialogBuilder.setToolbarComponents(dialogBuilder.createStandardOKCancelDialogButtons(yesCaption, noCaption));
		dialogBuilder
				.setContentComponent(new JLabel("<HTML><BR>" + question + "<BR><BR><HTML>", SwingConstants.CENTER));
		dialogBuilder.setTitle(title);
		showDialog(dialogBuilder.build(), true);
		return dialogBuilder.isOkPressed();
	}

	public void openMessageDialog(Component activatorComponent, String msg, String title, Image iconImage) {
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI,
				new TextualTypeInfo(reflectionUI, String.class));
		encapsulation.setTypeCaption("Information");
		encapsulation.setFieldCaption("");
		encapsulation.setFieldGetOnly(true);
		encapsulation.setFieldNullable(false);
		encapsulation.setModificationStackAccessible(false);
		Object toDisplay = encapsulation.getInstance(new Object[] { msg });
		Component errorComponent = new JOptionPane(createForm(toDisplay), JOptionPane.ERROR_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, new Object[] {});

		openObjectDialog(activatorComponent, toDisplay);
	}

	public void openErrorDialog(Component activatorComponent, String title, final Throwable error) {
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI,
				new TextualTypeInfo(reflectionUI, String.class));
		encapsulation.setTypeCaption("Error");
		encapsulation.setFieldCaption("Message");
		encapsulation.setFieldGetOnly(true);
		encapsulation.setFieldNullable(false);
		encapsulation.setModificationStackAccessible(false);
		Map<String, Object> fieldSpecificProperties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setIconImage(fieldSpecificProperties, SwingRendererUtils.ERROR_ICON.getImage());
			encapsulation.setFieldSpecificProperties(fieldSpecificProperties);
		}
		Object toDisplay = encapsulation.getInstance(new Object[] { ReflectionUIUtils.getPrettyMessage(error) });

		ObjectDialogBuilder dialogBuilder = createObjectDialogBuilder(activatorComponent, toDisplay);
		dialogBuilder.setTitle(title);
		final JButton deatilsButton = new JButton(prepareStringToDisplay("Details"));
		deatilsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openErrorDetailsDialog(deatilsButton, error);
			}
		});
		dialogBuilder.setAdditionalToolbarComponents(Arrays.<Component>asList(deatilsButton));
		showDialog(dialogBuilder.build(), true);

	}

	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		openObjectDialog(activatorComponent, error);
	}

	public ObjectDialogBuilder openObjectDialog(Component activatorComponent, Object object) {
		return openObjectDialog(activatorComponent, object, getObjectTitle(object), getObjectIconImage(object), false,
				true);
	}

	public ObjectDialogBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			Image iconImage, boolean cancellable, boolean modal) {
		ObjectDialogBuilder dialogBuilder = createObjectDialogBuilder(activatorComponent, object);
		dialogBuilder.setTitle(title);
		dialogBuilder.setIconImage(iconImage);
		dialogBuilder.setCancellable(cancellable);
		showDialog(dialogBuilder.build(), modal);
		return dialogBuilder;
	}

	public ObjectDialogBuilder createObjectDialogBuilder(Component activatorComponent, Object object) {
		return new ObjectDialogBuilder(this, activatorComponent, object) {

			@Override
			protected DialogBuilder createDelegateDialogBuilder(Component ownerComponent) {
				return createDialogBuilder(ownerComponent);
			}

		};
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		JFrame frame = createObjectFrame(object, title, iconImage);
		frame.setVisible(true);
	}

	public void openObjectFrame(Object object, final String title) {
		openObjectFrame(object, title, getObjectIconImage(object));
	}

	public void openObjectFrame(Object object) {
		openObjectFrame(object, getObjectTitle(object), getObjectIconImage(object));
	}

	public JFrame createObjectFrame(Object object, String title, Image iconImage) {
		final Object[] valueHolder = new Object[] { object };
		String fieldCaption = BooleanTypeInfo.isCompatibleWith(valueHolder[0].getClass()) ? "Is True" : "Value";
		ITypeInfo objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (SwingRendererUtils.hasCustomControl(object, objectType, this)) {
			EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, objectType);
			encapsulation.setTypeCaption(title);
			encapsulation.setFieldCaption(fieldCaption);
			encapsulation.setFieldGetOnly(false);
			encapsulation.setFieldNullable(false);
			object = encapsulation.getInstance(valueHolder);
		}
		JPanel form = createForm(object);
		JFrame frame = createFrame(form, title, iconImage, createCommonToolbarControls(form));
		return frame;
	}

	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent, final List<T> choices, T initialSelection,
			String message, String title) {
		if (choices.size() == 0) {
			throw new ReflectionUIError();
		}
		final ArrayAsEnumerationFactory enumFactory = new ArrayAsEnumerationFactory(reflectionUI, choices.toArray(),
				"SelectionDialogArrayAsEnumeration [title=" + title + "]", "") {

			Map<Object, String> captions = new HashMap<Object, String>();
			Map<Object, Image> iconImages = new HashMap<Object, Image>();

			{
				for (Object choice : choices) {
					captions.put(choice, ReflectionUIUtils.toString(SwingRenderer.this.reflectionUI, choice));
					iconImages.put(choice, getObjectIconImage(choice));
				}
			}

			@Override
			protected Map<String, Object> getItemSpecificProperties(Object choice) {
				Map<String, Object> properties = new HashMap<String, Object>();
				DesktopSpecificProperty.setIconImage(properties, iconImages.get(choice));
				return properties;
			}

			@Override
			protected String getItemName(Object choice) {
				return "option(" + captions.get(choice) + ")";
			}

			@Override
			protected String getItemCaption(Object choice) {
				return captions.get(choice);
			}

		};
		IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
				.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
		Object resultEnumItem = openSelectionDialog(parentComponent, enumType,
				enumFactory.getInstance(initialSelection), message, title);
		if (resultEnumItem == null) {
			return null;
		}
		T result = (T) enumFactory.unwrapInstance(resultEnumItem);
		return result;

	}

	public DialogBuilder createDialogBuilder(Component activatorComponent) {
		return new DialogBuilder(this, activatorComponent);
	}

	public List<IFieldInfo> getDisplayedFields(JPanel form) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			result.add(fieldControlPlaceHolder.getField());
		}
		return result;
	}

	public List<IMethodInfo> getDisplayedMethods(JPanel form) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (MethodControlPlaceHolder methodControlPlaceHolder : getMethodControlPlaceHolders(form)) {
			result.add(methodControlPlaceHolder.getMethod());
		}
		return result;
	}

	public void refreshAllFieldControls(JPanel form, boolean recreate) {
		Object formFocusDetails = getFormFocusDetails(form);

		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			fieldControlPlaceHolder.refreshUI(recreate);
			updateFieldControlLayout(fieldControlPlaceHolder);
		}

		if (formFocusDetails != null) {
			setFormFocusDetails(form, formFocusDetails);
		}
	}

	public Object getFormFocusDetails(JPanel form) {
		int focusedFieldIndex = getFocusedFieldControlPaceHolderIndex(form);
		if (focusedFieldIndex == -1) {
			return null;
		}
		Object focusedFieldFocusDetails = null;
		Class<?> focusedFieldControlClass = null;
		{
			final FieldControlPlaceHolder focusedFieldControlPaceHolder = getFieldControlPlaceHolders(form)
					.get(focusedFieldIndex);
			Component focusedFieldControl = focusedFieldControlPaceHolder.getFieldControl();
			if (focusedFieldControl instanceof IAdvancedFieldControl) {
				focusedFieldFocusDetails = ((IAdvancedFieldControl) focusedFieldControl).getFocusDetails();
				focusedFieldControlClass = focusedFieldControl.getClass();
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("focusedFieldIndex", focusedFieldIndex);
		result.put("focusedFieldControlClass", focusedFieldControlClass);
		result.put("focusedFieldFocusDetails", focusedFieldFocusDetails);
		return result;
	}

	public void setFormFocusDetails(JPanel form, Object focusDetails) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) focusDetails;
		int focusedFieldIndex = (Integer) map.get("focusedFieldIndex");
		Class<?> focusedFieldControlClass = (Class<?>) map.get("focusedFieldControlClass");
		Object focusedFieldFocusDetails = map.get("focusedFieldFocusDetails");

		FieldControlPlaceHolder fieldControlPaceHolderToFocusOn = getFieldControlPlaceHolders(form)
				.get(focusedFieldIndex);
		fieldControlPaceHolderToFocusOn.requestFocus();
		if (focusedFieldFocusDetails != null) {
			Component focusedFieldControl = fieldControlPaceHolderToFocusOn.getFieldControl();
			if (focusedFieldControl.getClass().equals(focusedFieldControlClass)) {
				if (focusedFieldControl instanceof IAdvancedFieldControl) {
					((IAdvancedFieldControl) focusedFieldControl).requestDetailedFocus(focusedFieldFocusDetails);
				}
			}
		}
	}

	public void refreshFieldControlsByName(JPanel form, String fieldName, boolean recreate) {
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getField().getName())) {
				fieldControlPlaceHolder.refreshUI(recreate);
				updateFieldControlLayout(fieldControlPlaceHolder);
			}
		}
	}

	public void showBusyDialogWhile(final Component activatorComponent, final Runnable runnable, final String title) {
		final Throwable[] exceptionThrown = new Throwable[1];
		final Thread runner = new Thread("BusyDialogRunner: " + title) {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable t) {
					exceptionThrown[0] = t;
				}
			}
		};
		runner.start();
		try {
			runner.join(1000);
		} catch (InterruptedException e) {
			throw new ReflectionUIError(e);
		}
		if (runner.isAlive()) {
			final DialogBuilder dialogBuilder = createDialogBuilder(activatorComponent);
			final Thread closer = new Thread("BusyDialogCloser: " + title) {
				@Override
				public void run() {
					while (true) {
						if (!runner.isAlive()) {
							JDialog dialog = dialogBuilder.getBuiltDialog();
							if (dialog != null) {
								if (dialog.isVisible()) {
									dialog.dispose();
									break;
								}
							}
						}
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							throw new ReflectionUIError(e);
						}
					}
				}
			};
			closer.start();
			final JXBusyLabel busyLabel = new JXBusyLabel();
			busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
			busyLabel.setText("Please wait...");
			busyLabel.setVerticalTextPosition(SwingConstants.TOP);
			busyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			busyLabel.setBusy(true);
			dialogBuilder.setContentComponent(busyLabel);
			dialogBuilder.setTitle(title);
			final JDialog dialog = dialogBuilder.build();
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					runner.interrupt();
				}
			});
			showDialog(dialog, true, false);
		}
		if (exceptionThrown[0] != null) {
			throw new ReflectionUIError(exceptionThrown[0]);
		}
	}

	public void showDialog(JDialog dialog, boolean modal) {
		showDialog(dialog, modal, true);
	}

	public void showDialog(JDialog dialog, boolean modal, boolean closeable) {
		if (modal) {
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			if (closeable) {
				dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			} else {
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			}
			dialog.setVisible(true);
			dialog.dispose();
		} else {
			dialog.setModalityType(ModalityType.MODELESS);
			if (closeable) {
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			} else {
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			}
			dialog.setVisible(true);
		}

	}

	public void updateFieldControlLayout(FieldControlPlaceHolder fieldControlPlaceHolder) {
		Component fieldControl = fieldControlPlaceHolder.getFieldControl();
		IFieldInfo field = fieldControlPlaceHolder.getField();
		Container container = fieldControlPlaceHolder.getParent();

		GridBagLayout layout = (GridBagLayout) container.getLayout();
		int i = layout.getConstraints(fieldControlPlaceHolder).gridy;

		container.remove(fieldControlPlaceHolder);
		Component captionControl = captionControlByFieldControlPlaceHolder.get(fieldControlPlaceHolder);
		if (captionControl != null) {
			container.remove(captionControl);
			captionControlByFieldControlPlaceHolder.remove(fieldControlPlaceHolder);
		}

		boolean addDefaultFieldCaptionControl = !fieldControlPlaceHolder.showCaption()
				&& (field.getCaption().length() > 0);
		int spacing = 5;
		if (addDefaultFieldCaptionControl) {
			captionControl = createSeparateCaptionControl(field.getCaption());
			GridBagConstraints layoutConstraints = new GridBagConstraints();
			layoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			layoutConstraints.gridx = 0;
			layoutConstraints.gridy = i;
			layoutConstraints.weighty = 1.0;
			layoutConstraints.anchor = GridBagConstraints.WEST;
			container.add(captionControl, layoutConstraints);
			captionControlByFieldControlPlaceHolder.put(fieldControlPlaceHolder, captionControl);
		}
		{
			GridBagConstraints layoutConstraints = new GridBagConstraints();
			layoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			if (!addDefaultFieldCaptionControl) {
				layoutConstraints.gridwidth = 2;
				layoutConstraints.gridx = 0;
			} else {
				layoutConstraints.gridx = 1;
			}
			layoutConstraints.gridy = i;
			layoutConstraints.weightx = 1.0;
			layoutConstraints.weighty = 1.0;
			layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
			container.add(fieldControlPlaceHolder, layoutConstraints);
		}

		container.validate();
	}

	public Component createSeparateCaptionControl(String caption) {
		return new JLabel(prepareStringToDisplay(caption + ": "));
	}

	public void updateStatusBar(final JPanel form) {
		try {
			validateForm(form);
			setStatusBarError(form, null);
		} catch (Exception e) {
			String errorMsg = new ReflectionUIError(e).toString();
			setStatusBarError(form, errorMsg);
		}
	}

	public void validateForm(JPanel form) throws Exception {
		final Object object = getObjectByForm().get(form);
		if (object == null) {
			return;
		}
		final ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		type.validate(object);
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			Component fieldControl = fieldControlPlaceHolder.getFieldControl();
			if (fieldControl instanceof IAdvancedFieldControl) {
				IFieldInfo field = fieldControlPlaceHolder.getField();
				try {
					((IAdvancedFieldControl) fieldControl).validateSubForm();
				} catch (Exception e) {
					String errorMsg = e.toString();
					errorMsg = ReflectionUIUtils.composeMessage(field.getCaption(), errorMsg);
					InfoCategory fieldCategory = field.getCategory();
					if (fieldCategory != null) {
						errorMsg = ReflectionUIUtils.composeMessage(fieldCategory.getCaption(), errorMsg);
					}
					throw new ReflectionUIError(errorMsg, e);
				}
			}
		}
	}

	public void updateStatusBarInBackground(final JPanel form) {
		new Thread("Validator: " + getObjectByForm().get(form)) {
			@Override
			public void run() {
				updateStatusBar(form);
			}
		}.start();
	}

	public void setStatusBarError(JPanel form, String errorMsg) {
		JLabel statusBar = getStatusBarByForm().get(form);
		if (statusBar == null) {
			return;
		}
		if (errorMsg == null) {
			statusBar.setVisible(false);
		} else {
			statusBar.setIcon(SwingRendererUtils.ERROR_ICON);
			statusBar.setBackground(new Color(255, 245, 242));
			statusBar.setForeground(new Color(255, 0, 0));
			statusBar.setText(ReflectionUIUtils.multiToSingleLine(errorMsg));
			SwingRendererUtils.setMultilineToolTipText(statusBar, errorMsg);
			statusBar.setVisible(true);
		}
		SwingRendererUtils.handleComponentSizeChange(statusBar);
	}

	public class FieldControlPlaceHolder extends JPanel {

		protected static final long serialVersionUID = 1L;
		protected Object object;
		protected Component fieldControl;
		protected Object lastFieldValue;
		protected Runnable lastFieldValueUpdate;
		protected IFieldInfo field;
		protected IFieldInfo controlAwareField;

		public FieldControlPlaceHolder(Object object, IFieldInfo field) {
			super();
			this.object = object;
			this.field = field;
			this.controlAwareField = field;
			this.controlAwareField = indicateWhenBusy(this.controlAwareField);
			this.controlAwareField = makeFieldModificationsUndoable(this.controlAwareField);
			this.controlAwareField = handleValueAccessIssues(this.controlAwareField);
			setLayout(new BorderLayout());
			refreshUI(false);
		}

		public IFieldInfo makeFieldModificationsUndoable(final IFieldInfo field) {
			return new FieldInfoProxy(field) {

				@Override
				public void setValue(Object object, Object newValue) {
					Component c = fieldControl;
					if ((c instanceof IAdvancedFieldControl)) {
						IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
						if (fieldControl.handlesModificationStackUpdate()) {
							field.setValue(object, newValue);
							return;
						}
					}
					ModificationStack modifStack = ReflectionUIUtils
							.findParentFormModificationStack(FieldControlPlaceHolder.this, SwingRenderer.this);
					SwingRendererUtils.setValueThroughModificationStack(object, field, newValue, modifStack);
				}
			};
		}

		public IFieldInfo handleValueAccessIssues(final IFieldInfo field) {
			lastFieldValueUpdate = new Runnable() {
				@Override
				public void run() {
					lastFieldValue = field.getValue(object);
				}
			};
			return new FieldInfoProxy(field) {

				@Override
				public Object getValue(Object object) {
					return lastFieldValue;
				}

				@Override
				public void setValue(Object object, Object newValue) {
					try {
						field.setValue(object, newValue);
						lastFieldValue = newValue;
						displayError(null);
					} catch (Throwable t) {
						try {
							lastFieldValue = field.getValue(object);
						} catch (Throwable ignore) {
						}
						displayError(new ReflectionUIError(t));
					}
				}

			};
		}

		public IFieldInfo indicateWhenBusy(final IFieldInfo field) {
			return new FieldInfoProxy(field) {

				private boolean isBusyIndicationDisabled() {
					JPanel form = SwingRendererUtils.findParentForm(FieldControlPlaceHolder.this, SwingRenderer.this);
					return Boolean.TRUE.equals(getBusyIndicationDisabledByForm().get(form));
				}

				@Override
				public Object getValue(final Object object) {
					if (isBusyIndicationDisabled()) {
						return super.getValue(object);
					}
					return SwingRendererUtils.showBusyDialogWhileGettingFieldValue(FieldControlPlaceHolder.this,
							SwingRenderer.this, object, field);
				}

				@Override
				public void setValue(final Object object, final Object value) {
					if (isBusyIndicationDisabled()) {
						super.setValue(object, value);
						return;
					}
					SwingRendererUtils.showBusyDialogWhileSettingFieldValue(FieldControlPlaceHolder.this,
							SwingRenderer.this, object, field, value);
				}

				@Override
				public Runnable getCustomUndoUpdateJob(Object object, Object value) {
					if (isBusyIndicationDisabled()) {
						return super.getCustomUndoUpdateJob(object, value);
					}
					final Runnable result = field.getCustomUndoUpdateJob(object, value);
					if (result == null) {
						return null;
					}
					return new Runnable() {
						@Override
						public void run() {
							showBusyDialogWhile(FieldControlPlaceHolder.this, new Runnable() {
								public void run() {
									result.run();
								}
							}, AbstractModification.getUndoTitle("Setting " + field.getCaption()));
						}
					};
				}

			};
		}

		public Component getFieldControl() {
			return fieldControl;
		}

		public IFieldInfo getField() {
			return field;
		}

		public IFieldInfo getControlAwareField() {
			return controlAwareField;
		}

		public void refreshUI(boolean recreate) {
			lastFieldValueUpdate.run();
			if (recreate) {
				if (fieldControl != null) {
					remove(fieldControl);
					fieldControl = null;
				}
			}
			if (fieldControl == null) {
				IControlData fieldControlData = getFieldControlData(object, controlAwareField);
				fieldControl = createFieldControl(fieldControlData);
				if (fieldControl instanceof IAdvancedFieldControl) {
					((IAdvancedFieldControl) fieldControl).setPalceHolder(FieldControlPlaceHolder.this);
				}
				add(fieldControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			} else {
				if (!(((fieldControl instanceof IAdvancedFieldControl)
						&& ((IAdvancedFieldControl) fieldControl).refreshUI()))) {
					boolean hadFocus = SwingRendererUtils.hasOrContainsFocus(fieldControl);
					remove(fieldControl);
					fieldControl = null;
					refreshUI(false);
					if (hadFocus) {
						fieldControl.requestFocus();
					}
				}
			}
		}

		public void displayError(ReflectionUIError error) {
			if (!((fieldControl instanceof IAdvancedFieldControl)
					&& ((IAdvancedFieldControl) fieldControl).displayError(error))) {
				if (error != null) {
					handleExceptionsFromDisplayedUI(fieldControl, error);
					refreshUI(false);
				}
			}
		}

		public boolean showCaption() {
			if (((fieldControl instanceof IAdvancedFieldControl)
					&& ((IAdvancedFieldControl) fieldControl).showCaption())) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void requestFocus() {
			if (fieldControl != null) {
				fieldControl.requestFocus();
			}
		}

	}

	public class MethodControlPlaceHolder extends JPanel {

		protected static final long serialVersionUID = 1L;
		protected Object object;
		protected IMethodInfo controlAwareMethod;
		protected Component methodControl;
		protected IMethodInfo method;

		public MethodControlPlaceHolder(Object object, IMethodInfo method) {
			super();
			this.object = object;
			this.method = method;
			this.controlAwareMethod = method;
			this.controlAwareMethod = indicateWhenBusy(this.controlAwareMethod);
			this.controlAwareMethod = makeMethodModificationsUndoable(this.controlAwareMethod);
			setLayout(new BorderLayout());
			refreshUI(false);
		}

		public IMethodInfo makeMethodModificationsUndoable(final IMethodInfo method) {
			return new MethodInfoProxy(method) {

				@Override
				public Object invoke(Object object, InvocationData invocationData) {
					JPanel form = SwingRendererUtils.findParentForm(MethodControlPlaceHolder.this, SwingRenderer.this);
					ModificationStack stack = getModificationStackByForm().get(form);
					return SwingRendererUtils.invokeMethodThroughModificationStack(object, method, invocationData,
							stack);
				}

			};
		}

		public IMethodInfo indicateWhenBusy(final IMethodInfo method) {
			return new MethodInfoProxy(method) {

				@Override
				public Object invoke(final Object object, final InvocationData invocationData) {
					return SwingRendererUtils.showBusyDialogWhileInvokingMethod(MethodControlPlaceHolder.this,
							SwingRenderer.this, object, method, invocationData);
				}

				@Override
				public Runnable getUndoJob(Object object, InvocationData invocationData) {
					final Runnable result = method.getUndoJob(object, invocationData);
					if (result == null) {
						return null;
					}
					return new Runnable() {
						@Override
						public void run() {
							showBusyDialogWhile(MethodControlPlaceHolder.this, new Runnable() {
								public void run() {
									result.run();
								}
							}, AbstractModification
									.getUndoTitle(ReflectionUIUtils.composeMessage(method.getCaption(), "Execution")));
						}
					};
				}

			};
		}

		public Component getMethodControl() {
			return methodControl;
		}

		public IMethodInfo getMethod() {
			return method;
		}

		public IMethodInfo getControlAwareMethod() {
			return controlAwareMethod;
		}

		public void refreshUI(boolean recreate) {
			if (recreate) {
				if (methodControl != null) {
					remove(methodControl);
					methodControl = null;
				}
			}
			if (methodControl == null) {
				methodControl = SwingRenderer.this.createMethodControl(object, controlAwareMethod);
				add(methodControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			} else {
				boolean hadFocus = SwingRendererUtils.hasOrContainsFocus(methodControl);
				remove(methodControl);
				methodControl = null;
				refreshUI(false);
				if (hadFocus) {
					methodControl.requestFocus();
				}
			}
		}

		@Override
		public void requestFocus() {
			if (methodControl != null) {
				methodControl.requestFocus();
			}
		}

	}
}