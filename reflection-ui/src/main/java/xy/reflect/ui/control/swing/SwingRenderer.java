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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
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

import sun.swing.SwingUtilities2;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldAsOne.ListItem;
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
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.CompositeModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SetFieldValueModification;
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

	public static final SwingRenderer DEFAULT = createDefault();

	protected ReflectionUI reflectionUI;
	protected Map<JPanel, Object> objectByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, ModificationStack> modificationStackByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Boolean> fieldsUpdateListenerDisabledByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, IInfoCollectionSettings> infoCollectionSettingsByForm = new MapMaker().weakKeys().makeMap();
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

	protected static SwingRenderer createDefault() {
		if (SystemProperties.areDefaultInfoCustomizationsActive()
				&& SystemProperties.areDefaultInfoCustomizationsEditable()) {
			return new SwingCustomizer(ReflectionUI.DEFAULT, InfoCustomizations.DEFAULT,
					SystemProperties.getDefaultInfoCustomizationsFilePath());
		} else {
			return new SwingRenderer(ReflectionUI.DEFAULT);
		}
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

	public Map<JPanel, IInfoCollectionSettings> getInfoCollectionSettingsByForm() {
		return infoCollectionSettingsByForm;
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

	public void adjustWindowBounds(Window window) {
		Rectangle bounds = window.getBounds();
		Rectangle maxBounds = ReflectionUIUtils.getMaximumWindowBounds(window);
		if (bounds.width < maxBounds.width / 3) {
			bounds.grow((maxBounds.width / 3 - bounds.width) / 2, 0);
		}
		bounds = maxBounds.intersection(bounds);
		window.setBounds(bounds);
	}

	public void setupWindow(Window window, Component content, List<? extends Component> toolbarControls, String title,
			Image iconImage) {
		if (window instanceof JFrame) {
			((JFrame) window).setTitle(prepareStringToDisplay(title));
		} else if (window instanceof JDialog) {
			((JDialog) window).setTitle(prepareStringToDisplay(title));
		}
		Container contentPane = createWindowContentPane(window, content, toolbarControls);
		SwingRendererUtils.setContentPane(window, contentPane);
		window.pack();
		window.setLocationRelativeTo(null);
		Rectangle bounds = window.getBounds();
		bounds.grow(50, 10);
		window.setBounds(bounds);
		adjustWindowBounds(window);
		if (iconImage == null) {
			window.setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
		} else {
			window.setIconImage(iconImage);
		}
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
			IFieldInfo field = fieldControlPlaceHolder.getFormAwareField();
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
		return new MethodControl(createMethodAction(object, method));
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
		final JTabbedPane tabbedPane = new AutoResizeTabbedPane();
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
		return createForm(object, IInfoCollectionSettings.DEFAULT);
	}

	public JPanel createForm(final Object object, IInfoCollectionSettings settings) {
		final ModificationStack modifStack = new ModificationStack(getObjectTitle(object));
		JPanel result = new JPanel() {

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
		getInfoCollectionSettingsByForm().put(result, settings);
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

	public Component createFieldControl(final Object object, final IFieldInfo field) {
		if (field.getType() instanceof IEnumerationTypeInfo) {
			return new EnumerationControl(this, object, field);
		} else if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(field.getType())) {
			return new PolymorphicControl(this, object, field);
		} else if (field.getValueOptions(object) != null) {
			return createOptionsControl(object, field);
		} else {
			if (field.isNullable()) {
				return new NullableControl(this, object, field, new Accessor<Component>() {
					@Override
					public Component get() {
						return createNonNullFieldValueControl(object, field);
					}
				});
			} else {
				return createNonNullFieldValueControl(object, field);
			}
		}
	}

	public Component createNonNullFieldValueControl(Object object, IFieldInfo field) {
		Component customFieldControl = createCustomNonNullFieldValueControl(object, field);
		if (customFieldControl != null) {
			return customFieldControl;
		} else {
			if (DesktopSpecificProperty.isSubFormExpanded(DesktopSpecificProperty.accessInfoProperties(field))) {
				return new EmbeddedFormControl(this, object, field);
			} else {
				return new DialogAccessControl(this, object, field);
			}
		}
	}

	public Component createCustomNonNullFieldValueControl(Object object, IFieldInfo field) {
		ITypeInfo fieldType = field.getType();
		if (fieldType instanceof IListTypeInfo) {
			return new ListControl(this, object, field);
		} else {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassforName(fieldType.getName());
			} catch (ClassNotFoundException e) {
				return null;
			}
			if (javaType == Color.class) {
				return new ColorControl(this, object, field);
			} else if (BooleanTypeInfo.isCompatibleWith(javaType)) {
				return new CheckBoxControl(this, object, field);
			} else if (TextualTypeInfo.isCompatibleWith(javaType)) {
				if (javaType == String.class) {
					return new TextControl(this, object, field);
				} else {
					return new PrimitiveValueControl(this, object, field, javaType);
				}
			} else if (FileTypeInfo.isCompatibleWith(javaType)) {
				return new FileControl(this, object, field);
			} else {
				return null;
			}
		}
	}

	public Component createOptionsControl(final Object object, final IFieldInfo field) {
		ITypeInfo ownerType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		final ArrayAsEnumerationFactory enumFactory = new ArrayAsEnumerationFactory(reflectionUI,
				field.getValueOptions(object), ownerType.getName() + "." + field.getName() + ".ValueOptions", "");
		ITypeInfo enumType = reflectionUI.getTypeInfo(enumFactory.getTypeInfoSource());

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, enumType);
		encapsulation.setFieldCaption("");
		final Object encapsulated = encapsulation.getInstance(new Accessor<Object>() {

			@Override
			public Object get() {
				Object value = field.getValue(object);
				value = enumFactory.getInstance(value);
				return value;
			}

			@Override
			public void set(Object value) {
				value = enumFactory.unwrapInstance(value);
				field.setValue(object, value);
			}

		});
		return new EnumerationControl(this, encapsulated, encapsulation.getValueField());

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
		JPanel contentPane = new JPanel();
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
			content = new JScrollPane(new ScrollPaneOptions(content, true, false));
			contentPane.add(content, BorderLayout.CENTER);
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
		IInfoCollectionSettings settings = getInfoCollectionSettingsByForm().get(form);

		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FieldControlPlaceHolder>>();
		getFieldControlPlaceHoldersByCategoryByForm().put(form, fieldControlPlaceHoldersByCategory);
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		List<IFieldInfo> fields = type.getFields();
		for (IFieldInfo field : fields) {
			if (settings.excludeField(field)) {
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
			if (settings.excludeMethod(method)) {
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

	public IFieldInfo getFormAwareField(JPanel form, String fieldName) {
		List<FieldControlPlaceHolder> fieldControlPlaceHolders = getFieldControlPlaceHoldersByName(form, fieldName);
		if (fieldControlPlaceHolders.size() == 0) {
			return null;
		}
		return fieldControlPlaceHolders.get(0).getFormAwareField();
	}

	public IMethodInfo getFormAwareMethod(JPanel form, String methodSignature) {
		List<MethodControlPlaceHolder> methodControlPlaceHolders = getMethodControlPlaceHoldersBySignature(form,
				methodSignature);
		if (methodControlPlaceHolders.size() == 0) {
			return null;
		}
		return methodControlPlaceHolders.get(0).getFormAwareMethod();
	}

	public List<FieldControlPlaceHolder> getFieldControlPlaceHoldersByName(JPanel form, String fieldName) {
		List<FieldControlPlaceHolder> result = new ArrayList<FieldControlPlaceHolder>();
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getFormAwareField().getName())) {
				result.add(fieldControlPlaceHolder);
			}
		}
		return result;
	}

	public List<MethodControlPlaceHolder> getMethodControlPlaceHoldersBySignature(JPanel form, String methodSignature) {
		List<MethodControlPlaceHolder> result = new ArrayList<MethodControlPlaceHolder>();
		for (MethodControlPlaceHolder methodControlPlaceHolder : getMethodControlPlaceHolders(form)) {
			if (ReflectionUIUtils.getMethodInfoSignature(methodControlPlaceHolder.getFormAwareMethod())
					.equals(methodSignature)) {
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
			Image result = SwingRendererUtils.getIconImageFromInfo(type);
			if (result != null) {
				return result;
			}
		}
		return null;
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
						final ArrayAsEnumerationFactory enumFactory = new ArrayAsEnumerationFactory(reflectionUI,
								polyTypes.toArray(), type.getName() + ".PolymorphicInstanceSubTypesEnumeration", "") {
							@Override
							protected Map<String, Object> getItemSpecificProperties(Object arrayItem) {
								ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
								Map<String, Object> result = new HashMap<String, Object>();
								File iconImageFile = DesktopSpecificProperty
										.getIconImageFile(DesktopSpecificProperty.accessInfoProperties(polyTypesItem));
								DesktopSpecificProperty.setIconImageFile(result, iconImageFile);
								return result;
							}

							@Override
							protected String getItemOnlineHelp(Object arrayItem) {
								ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
								return polyTypesItem.getOnlineHelp();
							}

							@Override
							protected String getItemName(Object arrayItem) {
								ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
								return polyTypesItem.getName();
							}

							@Override
							protected String getItemCaption(Object arrayItem) {
								ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
								return polyTypesItem.getCaption();
							}
						};
						IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
								.getTypeInfo(enumFactory.getTypeInfoSource());
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
					MethodAction methodAction = new MethodAction(this, null, constructor);
					methodAction.setShouldDisplayReturnValue(false);
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
				final IMethodInfo chosenContructor = openSelectionDialog(activatorComponent, constructors, null,
						"Choose an option", null);
				if (chosenContructor == null) {
					return null;
				}
				MethodAction methodAction = new MethodAction(this, null, chosenContructor);
				methodAction.setShouldDisplayReturnValue(false);
				methodAction.execute(activatorComponent);
				return methodAction.getReturnValue();
			}
		} catch (

		Throwable t) {
			throw new ReflectionUIError("Could not create an instance of type '" + type + "': " + t.toString(), t);

		}

	}

	public void openErrorDialog(Component activatorComponent, String title, final Throwable error) {
		DialogBuilder dialogBuilder = new DialogBuilder(this, activatorComponent);
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI,
				new TextualTypeInfo(reflectionUI, String.class));
		encapsulation.setTypeCaption("Error");
		encapsulation.setFieldCaption("Message");
		encapsulation.setFieldGetOnly(true);
		encapsulation.setFieldNullable(false);
		Object toDisplay = encapsulation.getInstance(new Object[] { ReflectionUIUtils.getPrettyMessage(error) });
		Component errorComponent = new JOptionPane(createForm(toDisplay), JOptionPane.ERROR_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, new Object[] {});

		JDialog[] dialogHolder = new JDialog[1];

		List<Component> buttons = new ArrayList<Component>();
		final JButton deatilsButton = new JButton(prepareStringToDisplay("Details"));
		deatilsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openErrorDetailsDialog(deatilsButton, error);
			}
		});
		buttons.add(deatilsButton);
		buttons.add(dialogBuilder.createDialogClosingButton("Close", null));

		dialogBuilder.setTitle(title);
		dialogBuilder.setContentComponent(errorComponent);
		dialogBuilder.setToolbarComponents(buttons);

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
		ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(this, activatorComponent, object);
		dialogBuilder.setTitle(title);
		dialogBuilder.setIconImage(iconImage);
		dialogBuilder.setCancellable(cancellable);
		;
		showDialog(dialogBuilder.build(), modal);
		return dialogBuilder;
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
				"SelectionDialogArrayAsEnumeration", "") {

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
				SwingRendererUtils.setIconImage(properties, iconImages.get(choice));
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
				.getTypeInfo(enumFactory.getTypeInfoSource());
		Object resultEnumItem = openSelectionDialog(parentComponent, enumType,
				enumFactory.getInstance(initialSelection), message, title);
		if (resultEnumItem == null) {
			return null;
		}
		T result = (T) enumFactory.unwrapInstance(resultEnumItem);
		return result;

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
	public <T> T openInputDialog(Component parentComponent, T initialValue, String dataName, String title) {
		if (initialValue == null) {
			throw new ReflectionUIError();
		}
		final Object[] valueHolder = new Object[] { initialValue };
		ITypeInfo initialValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(initialValue));

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, initialValueType);
		encapsulation.setTypeCaption("Input");
		encapsulation.setFieldCaption(dataName);
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
		DialogBuilder dialogBuilder = new DialogBuilder(this, activatorComponent);
		dialogBuilder.setToolbarComponents(dialogBuilder.createStandardOKCancelDialogButtons());
		dialogBuilder
				.setContentComponent(new JLabel("<HTML><BR>" + question + "<BR><BR><HTML>", SwingConstants.CENTER));
		dialogBuilder.setTitle(title);
		showDialog(dialogBuilder.build(), true);
		return dialogBuilder.isOkPressed();
	}

	public String getDefaultFieldCaption(Object fieldValue) {
		return BooleanTypeInfo.isCompatibleWith(fieldValue.getClass()) ? "Is True" : "Value";
	}

	public List<IFieldInfo> getDisplayedFields(JPanel form) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			result.add(fieldControlPlaceHolder.getFormAwareField());
		}
		return result;
	}

	public List<IMethodInfo> getDisplayedMethods(JPanel form) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (MethodControlPlaceHolder methodControlPlaceHolder : getMethodControlPlaceHolders(form)) {
			result.add(methodControlPlaceHolder.getFormAwareMethod());
		}
		return result;
	}

	public void refreshAllFieldControls(JPanel form, boolean recreate) {
		int focusedFieldControlPaceHolderIndex = getFocusedFieldControlPaceHolderIndex(form);
		Object focusDetails = null;
		Class<?> focusedControlClass = null;
		{
			if (focusedFieldControlPaceHolderIndex != -1) {
				final FieldControlPlaceHolder focusedFieldControlPaceHolder = getFieldControlPlaceHolders(form)
						.get(focusedFieldControlPaceHolderIndex);
				Component focusedFieldControl = focusedFieldControlPaceHolder.getFieldControl();
				if (focusedFieldControl instanceof IAdvancedFieldControl) {
					focusDetails = ((IAdvancedFieldControl) focusedFieldControl).getFocusDetails();
					focusedControlClass = focusedFieldControl.getClass();
				}
			}
		}

		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			fieldControlPlaceHolder.refreshUI(recreate);
			updateFieldControlLayout(fieldControlPlaceHolder);
		}

		if (focusedFieldControlPaceHolderIndex != -1) {
			final FieldControlPlaceHolder fieldControlPaceHolderToFocusOn = getFieldControlPlaceHolders(form)
					.get(focusedFieldControlPaceHolderIndex);
			fieldControlPaceHolderToFocusOn.requestFocus();
			if (focusDetails != null) {
				Component focusedFieldControl = fieldControlPaceHolderToFocusOn.getFieldControl();
				if (focusedFieldControl.getClass().equals(focusedControlClass)) {
					if (focusedFieldControl instanceof IAdvancedFieldControl) {
						((IAdvancedFieldControl) focusedFieldControl).requestDetailedFocus(focusDetails);
					}
				}
			}
		}
	}

	public void refreshFieldControlsByName(JPanel form, String fieldName, boolean recreate) {
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getFormAwareField().getName())) {
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
			final DialogBuilder dialogBuilder = new DialogBuilder(SwingRenderer.this, activatorComponent);
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

	public void openMessageDialog(Component activatorComponent, String msg, String title, Image iconImage) {
		DialogBuilder dialogBuilder = new DialogBuilder(this, activatorComponent);
		JButton okButton = dialogBuilder.createDialogClosingButton("Close", null);
		dialogBuilder.setToolbarComponents(Collections.singletonList(okButton));
		dialogBuilder.setContentComponent(
				new JLabel("<HTML><BR><CENTER>" + ReflectionUIUtils.escapeHTML(msg, true) + "</CENTER><BR><BR><HTML>",
						SwingConstants.CENTER));
		dialogBuilder.setTitle(title);
		dialogBuilder.setIconImage(iconImage);
		showDialog(dialogBuilder.build(), true);
	}

	public void updateFieldControlLayout(FieldControlPlaceHolder fieldControlPlaceHolder) {
		Component fieldControl = fieldControlPlaceHolder.getFieldControl();
		IFieldInfo field = fieldControlPlaceHolder.getFormAwareField();
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
				IFieldInfo field = fieldControlPlaceHolder.getFormAwareField();
				try {
					((IAdvancedFieldControl) fieldControl).validateSubForm();
				} catch (Exception e) {
					throw new ReflectionUIError(ReflectionUIUtils.composeTitle(field.getCaption(), e.toString()), e);
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
		protected IFieldInfo field;
		protected Component fieldControl;
		protected Object lastFieldValue;
		protected Runnable lastFieldValueUpdate;

		public FieldControlPlaceHolder(Object object, IFieldInfo field) {
			super();
			this.object = object;
			field = makeFieldModificationsUndoable(field);
			field = handleValueErrors(field);
			field = indicateWhenBusy(field);
			this.field = field;
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
					JPanel form = SwingRendererUtils.findParentForm(FieldControlPlaceHolder.this, SwingRenderer.this);
					ModificationStack stack = getModificationStackByForm().get(form);
					SetFieldValueModification modif = SetFieldValueModification.create(reflectionUI, object, field,
							newValue);
					try {
						stack.apply(modif);
					} catch (Throwable t) {
						stack.invalidate();
						throw new ReflectionUIError(t);
					}
				}
			};
		}

		public IFieldInfo handleValueErrors(final IFieldInfo field) {
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
					final Object[] result = new Object[1];
					showBusyDialogWhile(FieldControlPlaceHolder.this, new Runnable() {
						public void run() {
							result[0] = field.getValue(object);
						}
					}, "Getting " + field.getCaption());
					return result[0];
				}

				@Override
				public void setValue(final Object object, final Object value) {
					if (isBusyIndicationDisabled()) {
						super.setValue(object, value);
						return;
					}
					showBusyDialogWhile(FieldControlPlaceHolder.this, new Runnable() {
						public void run() {
							field.setValue(object, value);
						}
					}, "Setting " + field.getCaption());
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
							}, ModificationStack.getUndoTitle("Setting " + field.getCaption()));
						}
					};
				}

			};
		}

		public Component getFieldControl() {
			return fieldControl;
		}

		public IFieldInfo getFormAwareField() {
			return field;
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
				fieldControl = SwingRenderer.this.createFieldControl(object, field);
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
		protected IMethodInfo method;
		protected Component methodControl;

		public MethodControlPlaceHolder(Object object, IMethodInfo method) {
			super();
			this.object = object;
			method = makeMethodModificationsUndoable(method);
			method = indicateWhenBusy(method);
			this.method = method;
			setLayout(new BorderLayout());
			refreshUI(false);
		}

		public IMethodInfo makeMethodModificationsUndoable(final IMethodInfo method) {
			return new MethodInfoProxy(method) {

				@Override
				public Object invoke(Object object, InvocationData invocationData) {
					JPanel form = SwingRendererUtils.findParentForm(MethodControlPlaceHolder.this, SwingRenderer.this);
					ModificationStack stack = getModificationStackByForm().get(form);
					return SwingRendererUtils.invokeMethodAndAllowToUndo(object, method, invocationData, stack);
				}

			};
		}

		public IMethodInfo indicateWhenBusy(final IMethodInfo method) {
			return new MethodInfoProxy(method) {

				@Override
				public Object invoke(final Object object, final InvocationData invocationData) {
					final Object[] result = new Object[1];
					showBusyDialogWhile(MethodControlPlaceHolder.this, new Runnable() {
						public void run() {
							result[0] = method.invoke(object, invocationData);
						}
					}, ReflectionUIUtils.composeTitle(method.getCaption(), "Execution"));
					return result[0];
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
							}, ModificationStack
									.getUndoTitle(ReflectionUIUtils.composeTitle(method.getCaption(), "Execution")));
						}
					};
				}

			};
		}

		public Component getMethodControl() {
			return methodControl;
		}

		public IMethodInfo getFormAwareMethod() {
			return method;
		}

		public void refreshUI(boolean recreate) {
			if (recreate) {
				if (methodControl != null) {
					remove(methodControl);
					methodControl = null;
				}
			}
			if (methodControl == null) {
				methodControl = SwingRenderer.this.createMethodControl(object, method);
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