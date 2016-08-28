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
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
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
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationTypeInfo;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.MethodParametersAsTypeInfo;
import xy.reflect.ui.info.type.util.VirtualFieldWrapperTypeInfo;
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

	protected ReflectionUI reflectionUI;
	protected Map<JPanel, Object> objectByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, ModificationStack> modificationStackByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Boolean> fieldsUpdateListenerDisabledByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, IInfoCollectionSettings> infoCollectionSettingsByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, JLabel> statusLabelByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> fieldControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<MethodControlPlaceHolder>>> methodControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<MethodControlPlaceHolder, IMethodInfo> methodByControlPlaceHoler = new MapMaker().weakKeys()
			.makeMap();
	protected Map<IMethodInfo, InvocationData> lastInvocationDataByMethod = new HashMap<IMethodInfo, InvocationData>();
	protected Map<FieldControlPlaceHolder, Component> captionControlByFieldControlPlaceHolder = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, JTabbedPane> categoriesTabbedPaneByForm = new MapMaker().weakKeys().makeMap();

	public SwingRenderer(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
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

	protected Map<JPanel, Boolean> getFieldsUpdateListenerDisabledByForm() {
		return fieldsUpdateListenerDisabledByForm;
	}

	public Map<JPanel, IInfoCollectionSettings> getInfoCollectionSettingsByForm() {
		return infoCollectionSettingsByForm;
	}

	public Map<JPanel, JLabel> getStatusLabelByForm() {
		return statusLabelByForm;
	}

	public Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> getFieldControlPlaceHoldersByCategoryByForm() {
		return fieldControlPlaceHoldersByCategoryByForm;
	}

	public Map<JPanel, Map<InfoCategory, List<MethodControlPlaceHolder>>> getMethodControlPlaceHoldersByCategoryByForm() {
		return methodControlPlaceHoldersByCategoryByForm;
	}

	public Map<MethodControlPlaceHolder, IMethodInfo> getMethodByControl() {
		return methodByControlPlaceHoler;
	}

	protected void adjustWindowBounds(Window window) {
		Rectangle bounds = window.getBounds();
		Rectangle maxBounds = ReflectionUIUtils.getMaximumWindowBounds(window);
		if (bounds.width < maxBounds.width / 3) {
			bounds.grow((maxBounds.width / 3 - bounds.width) / 2, 0);
		}
		bounds = maxBounds.intersection(bounds);
		window.setBounds(bounds);
	}

	protected void applyCommonWindowConfiguration(Window window, Component content,
			List<? extends Component> toolbarControls, String title, Image iconImage) {
		if (window instanceof JFrame) {
			((JFrame) window).setTitle(title);
		} else if (window instanceof JDialog) {
			((JDialog) window).setTitle(title);
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

	protected List<Component> createCommonToolbarControls(final JPanel form) {
		List<Component> result = new ArrayList<Component>();
		Object object = getObjectByForm().get(form);
		if (object != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
			if ((type.getOnlineHelp() != null) && (type.getOnlineHelp().trim().length() > 0)) {
				result.add(createOnlineHelpControl(type.getOnlineHelp()));
			}
		}
		boolean addModificationStackControls = false;
		{
			for (IFieldInfo field : getDisplayedFields(form)) {
				if (addModificationStackControls) {
					break;
				}
				if (!field.isGetOnly()) {
					addModificationStackControls = true;
				}
			}
			for (IMethodInfo method : getDisplayedMethods(form)) {
				if (addModificationStackControls) {
					break;
				}
				if (!method.isReadOnly()) {
					addModificationStackControls = true;
				}
			}
		}
		if (addModificationStackControls) {
			final ModificationStack stack = getModificationStackByForm().get(form);
			if (stack == null) {
				return null;
			}
			result.addAll(new ModificationStackControls(stack).createControls(reflectionUI));
		}
		return result;
	}

	protected JDialog createDialog(Component ownerComponent, Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls, final Runnable whenClosing) {
		Window owner = SwingRendererUtils.getWindowAncestorOrSelf(ownerComponent);
		JDialog dialog = new JDialog(owner, reflectionUI.prepareStringToDisplay(title)) {
			protected static final long serialVersionUID = 1L;
			protected boolean disposed = false;

			@Override
			public void dispose() {
				if (disposed) {
					return;
				}
				super.dispose();
				if (whenClosing != null) {
					try {
						whenClosing.run();
					} catch (Throwable t) {
						handleExceptionsFromDisplayedUI(this, t);
					}
				}
				disposed = true;
			}
		};
		applyCommonWindowConfiguration(dialog, content, toolbarControls, title, iconImage);
		dialog.setResizable(true);
		return dialog;
	}

	protected JButton createDialogClosingButton(String caption, final Runnable action, final JDialog[] dialogHolder) {
		final JButton result = new JButton(reflectionUI.prepareStringToDisplay(caption));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (action != null) {
						action.run();
					}
				} catch (Throwable t) {
					handleExceptionsFromDisplayedUI(result, t);
				} finally {
					dialogHolder[0].dispose();
				}
			}
		});
		return result;
	}

	protected FieldControlPlaceHolder createFieldControlPlaceHolder(Object object, IFieldInfo field) {
		return new FieldControlPlaceHolder(object, field);
	}

	protected JPanel createFieldsPanel(List<FieldControlPlaceHolder> fielControlPlaceHolders) {
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

	protected JFrame createFrame(Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls) {
		final JFrame frame = new JFrame();
		applyCommonWindowConfiguration(frame, content, toolbarControls, title, iconImage);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	protected MethodControl createMethodControl(final Object object, final IMethodInfo method) {
		return new MethodControl(this, object, method);
	}

	protected JPanel createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
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

	protected JTabbedPane createMultipleInfoCategoriesComponent(final SortedSet<InfoCategory> allCategories,
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
			tabbedPane.addTab(reflectionUI.prepareStringToDisplay(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutControls(fieldControlPlaceHolders, methodControlPlaceHolders, tabContent);

			JPanel buttonsPanel = new JPanel();
			tab.add(buttonsPanel, BorderLayout.SOUTH);
			buttonsPanel.setLayout(new BorderLayout());
			buttonsPanel.setBorder(BorderFactory.createTitledBorder(""));

			ArrayList<InfoCategory> allCategoriesAsList = new ArrayList<InfoCategory>(allCategories);
			final int tabIndex = allCategoriesAsList.indexOf(category);
			int tabCount = allCategoriesAsList.size();

			if (tabIndex > 0) {
				JButton previousCategoryButton = new JButton(reflectionUI.prepareStringToDisplay("<"));
				buttonsPanel.add(previousCategoryButton, BorderLayout.WEST);
				previousCategoryButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tabbedPane.setSelectedIndex(tabIndex - 1);
					}
				});
			}

			if (tabIndex < (tabCount - 1)) {
				JButton nextCategoryButton = new JButton(reflectionUI.prepareStringToDisplay(">"));
				buttonsPanel.add(nextCategoryButton, BorderLayout.EAST);
				nextCategoryButton.addActionListener(new ActionListener() {

	@Override
	public void actionPerformed(ActionEvent e) {
		tabbedPane.setSelectedIndex(tabIndex + 1);
	}

	});}}return tabbedPane;}

	public JPanel createObjectForm(Object object) {
		return createObjectForm(object, IInfoCollectionSettings.DEFAULT);
	}

	public JPanel createObjectForm(final Object object, IInfoCollectionSettings settings) {
		final ModificationStack modifStack = new ModificationStack(reflectionUI.getObjectTitle(object));
		JPanel result = new JPanel() {

			private static final long serialVersionUID = 1L;
			JPanel form = this;
			IModificationListener fieldsUpdateListener = new IModificationListener() {
				@Override
				public void handleEvent(Object event) {
					if (Boolean.TRUE.equals(getFieldsUpdateListenerDisabledByForm().get(form))) {
						return;
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							refreshAllFieldControls(form, false);
							validateForm(form);
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
		fillForm(result);
		return result;
	}

	public boolean hasCustomFieldControl(Object object, IFieldInfo field) {
		if (field.getType() instanceof IEnumerationTypeInfo) {
			return true;
		} else if (field.getType().getPolymorphicInstanceSubTypes() != null) {
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
		} else if (field.getType().getPolymorphicInstanceSubTypes() != null) {
			return new PolymorphicEmbeddedForm(this, object, field);
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

	protected Component createNonNullFieldValueControl(Object object, IFieldInfo field) {
		Component customFieldControl = createCustomNonNullFieldValueControl(object, field);
		if (customFieldControl != null) {
			return customFieldControl;
		} else {
			if (Boolean.TRUE.equals(field.getSpecificProperties().get(SwingSpecificProperty.CREATE_EMBEDDED_FORM))) {
				return new EmbeddedFormControl(this, object, field);
			} else {
				return new DialogAccessControl(this, object, field);
			}
		}
	}

	protected Component createCustomNonNullFieldValueControl(Object object, IFieldInfo field) {
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

	protected Component createOptionsControl(final Object object, final IFieldInfo field) {
		return new EnumerationControl(this, object, new FieldInfoProxy(field) {

			@Override
			public ITypeInfo getType() {
				return new ArrayAsEnumerationTypeInfo(reflectionUI, field.getValueOptions(object),
						field.getCaption() + " Value Options");
			}

		});
	}

	protected Component createOnlineHelpControl(String onlineHelp) {
		final JButton result = new JButton(SwingRendererUtils.HELP_ICON);
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result, reflectionUI.prepareStringToDisplay(onlineHelp));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingRendererUtils.showTooltipNow(result);
			}
		});
		return result;
	}

	protected Component createStatusBar(JPanel form) {
		JLabel result = new JLabel();
		result.setOpaque(true);
		result.setFont(new JToolTip().getFont());
		result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		getStatusLabelByForm().put(form, result);
		return result;
	}

	protected Component createToolBar(List<? extends Component> toolbarControls) {
		JPanel result = new JPanel();
		result.setBorder(BorderFactory.createRaisedBevelBorder());
		result.setLayout(new FlowLayout(FlowLayout.CENTER));
		for (Component tool : toolbarControls) {
			result.add(tool);
		}
		return result;
	}

	protected Container createWindowContentPane(Window window, Component content,
			List<? extends Component> toolbarControls) {
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		if (content != null) {
			JPanel form = SwingRendererUtils.findForm(content, this);
			if (form != null) {
				contentPane.add(createStatusBar(form), BorderLayout.NORTH);
				validateForm(form);
			}
			content = new JScrollPane(new ScrollPaneOptions(content, true, false));
			contentPane.add(content, BorderLayout.CENTER);
		}
		if (toolbarControls != null) {
			contentPane.add(createToolBar(toolbarControls), BorderLayout.SOUTH);
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

	protected void fillForm(JPanel form) {
		Object object = getObjectByForm().get(form);
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		form.setLayout(new BorderLayout());
		fillForm(form, object, type);
	}

	protected void fillForm(JPanel form, Object object, ITypeInfo type) {
		IInfoCollectionSettings settings = getInfoCollectionSettingsByForm().get(form);

		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FieldControlPlaceHolder>>();
		getFieldControlPlaceHoldersByCategoryByForm().put(form, fieldControlPlaceHoldersByCategory);
		List<IFieldInfo> fields = type.getFields();
		for (IFieldInfo field : fields) {
			if (settings.excludeField(field)) {
				continue;
			}
			if (!field.isGetOnly()) {
				field = makeFieldModificationsUndoable(field, form);
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
			if (!method.isReadOnly()) {
				method = makeMethodModificationsUndoable(method, form);
			}
			MethodControlPlaceHolder methodControlPlaceHolder = createMethodControlPlaceHolder(object, method);
			getMethodByControl().put(methodControlPlaceHolder, method);
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
		} else {
			form.add(formContent, BorderLayout.CENTER);
			formContent.setLayout(new BorderLayout());
			JTabbedPane categoriesControl = createMultipleInfoCategoriesComponent(allCategories,
					fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory);
			categoriesTabbedPaneByForm.put(form, categoriesControl);
			formContent.add(categoriesControl, BorderLayout.CENTER);
		}
	}

	protected InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1);
	}

	protected MethodControlPlaceHolder createMethodControlPlaceHolder(Object object, IMethodInfo method) {
		return new MethodControlPlaceHolder(object, method);
	}

	public List<FieldControlPlaceHolder> getAllFieldControlPlaceHolders(JPanel form) {
		List<FieldControlPlaceHolder> result = new ArrayList<FieldControlPlaceHolder>();
		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = getFieldControlPlaceHoldersByCategoryByForm()
				.get(form);
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory.get(category);
			result.addAll(fieldControlPlaceHolders);
		}
		return result;
	}

	public List<MethodControlPlaceHolder> getAllMethodControlPlaceHolders(JPanel form) {
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

	public List<Component> getFieldControlsByName(JPanel form, String fieldName) {
		List<Component> result = new ArrayList<Component>();
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getAllFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getField().getName())) {
				result.add(fieldControlPlaceHolder.getFieldControl());
			}
		}
		return result;
	}

	public int getFocusedFieldControlPaceHolderIndex(JPanel subForm) {
		int i = 0;
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getAllFieldControlPlaceHolders(subForm)) {
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

	public IFieldInfo getFormUpdatingField(Object object, String fieldName) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(type.getFields(), fieldName);
		for (JPanel form : getForms(object)) {
			field = makeFieldModificationsUndoable(field, form);
		}
		return field;
	}

	public IMethodInfo getFormUpdatingMethod(Object object, String methodSignature) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		IMethodInfo method = ReflectionUIUtils.findMethodBySignature(type.getMethods(), methodSignature);
		if (method == null) {
			return null;
		}
		for (JPanel form : getForms(object)) {
			method = makeMethodModificationsUndoable(method, form);
		}
		return method;
	}

	public List<Component> getMethodControlsBySignature(JPanel form, String methodSignature) {
		List<Component> result = new ArrayList<Component>();
		Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHolderByCategory = getMethodControlPlaceHoldersByCategoryByForm()
				.get(form);
		for (List<MethodControlPlaceHolder> methodControlPlaceHolders : methodControlPlaceHolderByCategory.values()) {
			for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
				IMethodInfo method = getMethodByControl().get(methodControlPlaceHolder);
				if (ReflectionUIUtils.getMethodInfoSignature(method).equals(methodSignature)) {
					result.add(methodControlPlaceHolder.getMethodControl());
				}
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
			if (type instanceof IEnumerationTypeInfo) {
				IEnumerationItemInfo valueInfo = ((IEnumerationTypeInfo) type).getValueInfo(object);
				if (valueInfo != null) {
					Image result = SwingRendererUtils.getIconImageFromInfo(valueInfo);
					if (result != null) {
						return result;
					}
				}
			}
			Image result = SwingRendererUtils.getIconImageFromInfo(type);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public void handleComponentSizeChange(Component c) {
		Window window = SwingUtilities.getWindowAncestor(c);
		if (window != null) {
			window.validate();
			JScrollPane scrollPane = null;
			Container contentPane = SwingRendererUtils.getContentPane(window);
			if (contentPane != null) {
				for (Component mayBeScrollPanel : contentPane.getComponents()) {
					if (mayBeScrollPanel instanceof JScrollPane) {
						scrollPane = (JScrollPane) mayBeScrollPanel;
						break;
					}
				}
			}
			if (scrollPane != null) {
				JScrollBar verticalScrollBVar = scrollPane.getVerticalScrollBar();
				int heightToGrow = verticalScrollBVar.getMaximum() - verticalScrollBVar.getVisibleAmount();
				Dimension windowSize = window.getSize();
				windowSize.height += heightToGrow;
				window.setSize(windowSize);
				adjustWindowBounds(window);
			}
		}
	}

	public void handleExceptionsFromDisplayedUI(Component activatorComponent, final Throwable t) {
		reflectionUI.logError(t);
		openErrorDialog(activatorComponent, "An Error Occured", t);
	}

	protected IFieldInfo handleValueChangeErrors(IFieldInfo field,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		return new FieldInfoProxy(field) {

			@Override
			public void setValue(Object object, Object value) {
				if (!reflectionUI.equals(super.getValue(object), value)) {
					try {
						super.setValue(object, value);
						fieldControlPlaceHolder.displayError(null);
					} catch (final Throwable t) {
						fieldControlPlaceHolder.displayError(new ReflectionUIError(t));
					}
				}
			}

		};
	}

	protected void layoutControlPanels(JPanel parentForm, JPanel fieldsPanel, JPanel methodsPanel) {
		parentForm.setLayout(new BorderLayout());
		parentForm.add(fieldsPanel, BorderLayout.CENTER);
		parentForm.add(methodsPanel, BorderLayout.SOUTH);
	}

	protected void layoutControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, JPanel parentForm) {
		JPanel fieldsPanel = createFieldsPanel(fielControlPlaceHolders);
		JPanel methodsPanel = createMethodsPanel(methodControlPlaceHolders);
		layoutControlPanels(parentForm, fieldsPanel, methodsPanel);
	}

	public IFieldInfo makeFieldModificationsUndoable(final IFieldInfo field, final JPanel form) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(Object object, Object newValue) {
				ModificationStack stack = getModificationStackByForm().get(form);
				stack.apply(new SetFieldValueModification(reflectionUI, object, field, newValue));
			}
		};
	}

	public IMethodInfo makeMethodModificationsUndoable(final IMethodInfo method, final JPanel form) {
		return new MethodInfoProxy(method) {

			@Override
			public Object invoke(Object object, InvocationData invocationData) {
				ModificationStack stack = getModificationStackByForm().get(form);
				Object result;
				try {
					result = super.invoke(object, invocationData);
				} catch (Throwable t) {
					stack.invalidate();
					throw new ReflectionUIError(t);
				}
				IModification undoModif = method.getUndoModification(object, invocationData);
				if (undoModif == null) {
					stack.invalidate();
				} else {
					stack.pushUndo(undoModif);
				}
				return result;

			}

		};
	}

	public boolean onMethodInvocationRequest(final Component activatorComponent, final Object object,
			final IMethodInfo method, final Object[] returnValueHolder) {
		if (method.getParameters().size() > 0) {
			return openMethoExecutionSettingDialog(activatorComponent, object, method, returnValueHolder);
		} else {
			final boolean shouldDisplayReturnValue = (returnValueHolder == null)
					&& (method.getReturnValueType() != null);
			final Object[] returnValueToDisplay = new Object[1];
			final boolean[] exceptionThrownHoler = new boolean[] { false };
			showBusyDialogWhile(activatorComponent, new Runnable() {
				@Override
				public void run() {
					try {
						Object result = method.invoke(object, new InvocationData());
						if (returnValueHolder != null) {
							returnValueHolder[0] = result;
						}
						if (shouldDisplayReturnValue) {
							returnValueToDisplay[0] = result;
						}
					} catch (Throwable t) {
						exceptionThrownHoler[0] = true;
						throw new ReflectionUIError(t);
					}
				}
			}, reflectionUI.getMethodTitle(object, method, null, "Execution"));
			if (shouldDisplayReturnValue && !exceptionThrownHoler[0]) {
				openMethodReturnValueWindow(activatorComponent, object, method, returnValueToDisplay[0]);
			}
		}
		return true;
	}

	public Object onTypeInstanciationRequest(Component activatorComponent, ITypeInfo type, boolean silent) {
		try {
			List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
			if ((polyTypes != null) && (polyTypes.size() > 0)) {
				if (polyTypes.size() == 1) {
					type = polyTypes.get(0);
				} else {
					if (silent) {
						type = polyTypes.get(0);
					} else {
						ArrayAsEnumerationTypeInfo enumType = new ArrayAsEnumerationTypeInfo(reflectionUI,
								polyTypes.toArray(), SwingRenderer.class.getName()
										+ "#onTypeInstanciationRequest(): PolymorphicInstanceSubTypes As Enumeration") {

							@Override
							public IEnumerationItemInfo getValueInfo(Object object) {
								final ITypeInfo polyTypesItem = (ITypeInfo) object;
								final IEnumerationItemInfo baseValueInfo = super.getValueInfo(object);
								return new IEnumerationItemInfo() {

									@Override
									public Map<String, Object> getSpecificProperties() {
										Map<String, Object> result = new HashMap<String, Object>(
												baseValueInfo.getSpecificProperties());
										result.put(SwingSpecificProperty.KEY_ICON_IMAGE_PATH,
												polyTypesItem.getSpecificProperties()
														.get(SwingSpecificProperty.KEY_ICON_IMAGE_PATH));
										result.put(SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND,
												polyTypesItem.getSpecificProperties()
														.get(SwingSpecificProperty.KEY_ICON_IMAGE_PATH_KIND));
										return result;
									}

									@Override
									public String getOnlineHelp() {
										return baseValueInfo.getOnlineHelp();
									}

									@Override
									public String getName() {
										return baseValueInfo.getName();
									}

									@Override
									public String getCaption() {
										return baseValueInfo.getCaption();
									}
								};
							}

						};
						for (ITypeInfo polyType : polyTypes) {
							enumType.registerArrayItem(polyType);
						}
						Object resultEnumItem;
						try {
							resultEnumItem = openSelectionDialog(activatorComponent, enumType, null,
									MessageFormat.format("Choose the type of ''{0}''", type.getCaption()), null);
							if (resultEnumItem == null) {
								return null;
							}
						} finally {
							for (ITypeInfo polyType : polyTypes) {
								enumType.unregisterArrayItem(polyType);
							}
						}
						type = (ITypeInfo) resultEnumItem;
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
				IMethodInfo constructor = constructors.get(0);
				if (silent) {
					return constructor.invoke(null, new InvocationData());
				} else {
					Object[] returnValueHolder = new Object[1];
					onMethodInvocationRequest(activatorComponent, null, constructor, returnValueHolder);
					return returnValueHolder[0];
				}
			}

			constructors = new ArrayList<IMethodInfo>(constructors);
			Collections.sort(constructors, new Comparator<IMethodInfo>() {

	@Override
	public int compare(IMethodInfo o1, IMethodInfo o2) {
		return new Integer(o1.getParameters().size()).compareTo(new Integer(o2.getParameters().size()));
	}});

	if(silent){

	IMethodInfo smallerConstructor = constructors
			.get(0);return smallerConstructor.invoke(null,new InvocationData());}else{
	IMethodInfo chosenContructor = openSelectionDialog(activatorComponent, constructors, null,
			reflectionUI.prepareStringToDisplay("Choose an option:"), null);if(chosenContructor==null){return null;}
	Object[] returnValueHolder = new Object[1];

	onMethodInvocationRequest(activatorComponent, null, chosenContructor, returnValueHolder);
				return returnValueHolder[0];
			}}catch(

	Throwable t){throw new ReflectionUIError("Could not create an instance of type '"+type+"': "+t.toString(),t);

	}

	}

	public void openErrorDialog(Component activatorComponent, String title, final Throwable error) {
		Component errorComponent = new JOptionPane(
				createObjectForm(VirtualFieldWrapperTypeInfo.wrap(reflectionUI,
						new Object[] { ReflectionUIUtils.getPrettyMessage(error) }, "Message", "", true)),
				JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {});

		JDialog[] dialogHolder = new JDialog[1];

		List<Component> buttons = new ArrayList<Component>();
		final JButton deatilsButton = new JButton(reflectionUI.prepareStringToDisplay("Details"));
		deatilsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openErrorDetailsDialog(deatilsButton, error);
			}
		});
		buttons.add(deatilsButton);
		buttons.add(createDialogClosingButton("Close", null, dialogHolder));

		dialogHolder[0] = createDialog(activatorComponent, errorComponent, title, null, buttons, null);
		showDialog(dialogHolder[0], true);

	}

	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		openObjectDialog(activatorComponent, error, true);
	}

	public void openMethodReturnValueWindow(Component activatorComponent, Object object, IMethodInfo method,
			Object returnValue) {
		if (returnValue == null) {
			String msg = "No data returned!";
			openMessageDialog(activatorComponent, msg, reflectionUI.getMethodTitle(object, method, null, "Result"));
		} else {
			openObjectFrame(returnValue, reflectionUI.getMethodTitle(object, method, returnValue, "Execution Result"));
		}
	}

	protected boolean openMethoExecutionSettingDialog(final Component activatorComponent, final Object object,
			final IMethodInfo method, final Object[] returnValueHolder) {
		final boolean shouldDisplayReturnValue = (returnValueHolder == null) && (method.getReturnValueType() != null);
		final boolean[] exceptionThrownHolder = new boolean[] { false };
		final Object[] returnValueToDisplay = new Object[1];
		final InvocationData invocationData;
		if (lastInvocationDataByMethod.containsKey(method)) {
			invocationData = lastInvocationDataByMethod.get(method);
		} else {
			invocationData = new InvocationData();
		}
		JPanel methodForm = createObjectForm(
				new MethodParametersAsTypeInfo(reflectionUI, method).getInstance(object, invocationData));
		final boolean[] invokedStatusHolder = new boolean[] { false };
		final JDialog[] methodDialogHolder = new JDialog[1];
		List<Component> toolbarControls = new ArrayList<Component>();
		String doc = method.getOnlineHelp();
		if ((doc != null) && (doc.trim().length() > 0)) {
			toolbarControls.add(createOnlineHelpControl(doc));
		}
		JButton invokeButton = new JButton(method.getCaption());
		{
			invokeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					lastInvocationDataByMethod.put(method, invocationData);
					showBusyDialogWhile(activatorComponent, new Runnable() {
						@Override
						public void run() {
							try {
								Object result = method.invoke(object, invocationData);
								if (returnValueHolder != null) {
									returnValueHolder[0] = result;
								}
								if (shouldDisplayReturnValue) {
									returnValueToDisplay[0] = result;
								}
							} catch (Throwable t) {
								exceptionThrownHolder[0] = true;
								throw new ReflectionUIError(t);
							}
						}
					}, reflectionUI.getMethodTitle(object, method, null, "Execution"));
					if (shouldDisplayReturnValue) {
						if (!exceptionThrownHolder[0]) {
							openMethodReturnValueWindow(activatorComponent, object, method, returnValueToDisplay[0]);
						}
					} else {
						methodDialogHolder[0].dispose();
					}
				}
			});
			toolbarControls.add(invokeButton);
		}
		JButton closeButton = new JButton(shouldDisplayReturnValue ? "Close" : "Cancel");
		{
			closeButton.addActionListener(new ActionListener() {

	@Override
				public void actionPerformed(ActionEvent e) {
					methodDialogHolder[0].dispose();
				}

	});toolbarControls.add(closeButton);}methodDialogHolder[0]=createDialog(activatorComponent,methodForm,reflectionUI.getMethodTitle(object,method,null,"Setting"),null,toolbarControls,null);showDialog(methodDialogHolder[0],true);if(shouldDisplayReturnValue){return true;}else{return invokedStatusHolder[0];}}

	public boolean openObjectDialogAndGetUpdateStatus(Component activatorComponent,
			Accessor<Object> valueAccessor, boolean isGetOnly, 
			ModificationStack parentStack) {
		Image iconImage = getObjectIconImage(valueAccessor.get());
		String title = reflectionUI.getObjectTitle(valueAccessor.get());
		return openObjectDialogAndGetUpdateStatus(activatorComponent, valueAccessor, isGetOnly, title, iconImage, parentStack);
	}

	public boolean openObjectDialogAndGetUpdateStatus(Component activatorComponent,
			Accessor<Object> valueAccessor, boolean isGetOnly, String title, Image iconImage,
			ModificationStack parentStack) {
		boolean[] changeDetectedHolder = new boolean[] { false };
		openObjectDialog(activatorComponent, valueAccessor, isGetOnly, title, iconImage, true, IInfoCollectionSettings.DEFAULT,
				parentStack, true, null, changeDetectedHolder, null);
		return changeDetectedHolder[0];
	}

	public boolean openObjectDialogAndGetConfirmation(Component activatorComponent, Object object, final String title,
			Image iconImage, boolean modal) {
		boolean[] okPressedHolder = new boolean[1];
		openObjectDialog(activatorComponent, object, title, iconImage, modal, true, okPressedHolder);
		return okPressedHolder[0];
	}

	public boolean openObjectDialogAndGetConfirmation(Component activatorComponent, Object object, boolean modal) {
		return openObjectDialogAndGetConfirmation(activatorComponent, object, reflectionUI.getObjectTitle(object),
				getObjectIconImage(object), modal);
	}

	public void openObjectDialog(Component activatorComponent, Object object, boolean modal) {
		openObjectDialog(activatorComponent, object, reflectionUI.getObjectTitle(object), getObjectIconImage(object), modal);
	}

	public void openObjectDialog(Component activatorComponent, Object object, final String title, Image iconImage,
			boolean modal) {
		openObjectDialog(activatorComponent, object, title, iconImage, modal, false, null);
	}

	public void openObjectDialog(Component activatorComponent, Object object, final String title, Image iconImage,
			boolean modal, boolean cancellable, boolean[] okPressedHolder) {
		Accessor<?> valueAccessor = Accessor.returning(object, false);
		boolean isGetOnly = true;
		ModificationStack parentModificationStack = null;
		List<Component> additionalToolbarControls = null;
		boolean[] changeDetectedHolder = null;
		openObjectDialog(activatorComponent, valueAccessor, isGetOnly, title, iconImage, modal,
				IInfoCollectionSettings.DEFAULT, parentModificationStack, cancellable, okPressedHolder,
				changeDetectedHolder, additionalToolbarControls);
	}

	public <T> void openObjectDialog(Component activatorComponent, Accessor<T> valueAccessor, boolean isGetOnly,
			String title, Image iconImage, boolean modal, IInfoCollectionSettings settings,
			ModificationStack parentModificationStack, boolean cancellable, boolean[] okPressedHolder,
			boolean[] changeDetectedHolder, List<Component> additionalToolbarControls) {
		JDialog dialog = createObjectDialog(activatorComponent, valueAccessor, isGetOnly, title, iconImage, settings,
				parentModificationStack, cancellable, okPressedHolder, changeDetectedHolder, additionalToolbarControls);
		showDialog(dialog, modal);
	}

	public <T> JDialog createObjectDialog(Component activatorComponent, final Accessor<T> valueAccessor,
			final boolean isGetOnly, final String title, Image iconImage, final IInfoCollectionSettings settings,
			final ModificationStack parentModificationStack, boolean cancellable, boolean[] okPressedHolder,
			final boolean[] changeDetectedHolder, List<Component> additionalToolbarControls) {
		final Object[] valueHolder = new Object[] { valueAccessor.get() };
		final Object object;
		if (hasCustomFieldControl(valueHolder[0])) {
			String fieldName = getDefaultFieldCaption(valueHolder[0]);
			object = VirtualFieldWrapperTypeInfo.wrap(reflectionUI, valueHolder, fieldName, title, isGetOnly);
		} else {
			object = valueHolder[0];
		}

		final ModificationStack[] modificationStackHolder = new ModificationStack[1];
		final boolean[] finalOkPressedHolder;
		if (okPressedHolder == null) {
			finalOkPressedHolder = new boolean[] { false };
		} else {
			finalOkPressedHolder = okPressedHolder;
		}
		Runnable whenClosingDialog = new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if (changeDetectedHolder != null) {
					changeDetectedHolder[0] = isChangeDetected();
				}
				if ((finalOkPressedHolder == null) || finalOkPressedHolder[0]) {
					if (isChangeDetected()) {
						if (parentModificationStack != null) {
							if (modificationStackHolder[0].isInvalidated()) {
								if (!isGetOnly) {
									valueAccessor.set((T) valueHolder[0]);
								}
								parentModificationStack.invalidate();
							} else {
								parentModificationStack.beginComposite();
								if (!isGetOnly) {
									valueAccessor.set((T) valueHolder[0]);
								}
								parentModificationStack.pushUndo(new CompositeModification(null, UndoOrder.LIFO,
										modificationStackHolder[0].getUndoModifications(UndoOrder.LIFO)));
								parentModificationStack.endComposite(title, UndoOrder.FIFO);
							}
						} else {
							if (!isGetOnly) {
								valueAccessor.set((T) valueHolder[0]);
							}
						}
					}
				} else {
					if (modificationStackHolder[0] != null) {
						if (!modificationStackHolder[0].isInvalidated()) {
							if (modificationStackHolder[0].getNumberOfUndoUnits() > 0) {
								modificationStackHolder[0].undoAll();
								if (changeDetectedHolder != null) {
									changeDetectedHolder[0] = false;
								}
							}
						}
					}
				}
			}

			private boolean isChangeDetected() {
				if (modificationStackHolder[0] != null) {
					if (modificationStackHolder[0].getNumberOfUndoUnits() > 0) {
						return true;
					}
					if (modificationStackHolder[0].isInvalidated()) {
						return true;
					}
				}
				return false;
			}
		};

		JPanel form = createObjectForm(object, settings);
		modificationStackHolder[0] = getModificationStackByForm().get(form);
		List<Component> toolbarControls = new ArrayList<Component>();
		List<Component> commonToolbarControls = createCommonToolbarControls(form);
		if (commonToolbarControls != null) {
			toolbarControls.addAll(commonToolbarControls);
		}
		if (additionalToolbarControls != null) {
			toolbarControls.addAll(additionalToolbarControls);
		}
		JDialog[] dialogHolder = new JDialog[1];
		if (cancellable) {
			final List<JButton> okCancelButtons = createStandardOKCancelDialogButtons(dialogHolder,
					finalOkPressedHolder);
			toolbarControls.addAll(okCancelButtons);
		} else {
			toolbarControls.add(createDialogClosingButton("Close", null, dialogHolder));
		}
		dialogHolder[0] = createDialog(activatorComponent, form, title, iconImage, toolbarControls, whenClosingDialog);
		return dialogHolder[0];
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		JFrame frame = createObjectFrame(object, title, iconImage);
		frame.setVisible(true);
	}

	public void openObjectFrame(Object object, final String title) {
		openObjectFrame(object, title, getObjectIconImage(object));
	}

	public void openObjectFrame(Object object) {
		openObjectFrame(object, reflectionUI.getObjectTitle(object), getObjectIconImage(object));
	}

	public JFrame createObjectFrame(Object object, String title, Image iconImage) {
		final Object[] valueHolder = new Object[] { object };
		String fieldName = BooleanTypeInfo.isCompatibleWith(valueHolder[0].getClass()) ? "Is True" : "Value";
		if (hasCustomFieldControl(object)) {
			object = VirtualFieldWrapperTypeInfo.wrap(reflectionUI, valueHolder, fieldName, title, true);
		}
		JPanel form = createObjectForm(object);
		JFrame frame = createFrame(form, title, iconImage, createCommonToolbarControls(form));
		return frame;
	}

	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent, final List<T> choices, T initialSelection,
			String message, String title) {
		if (choices.size() == 0) {
			throw new ReflectionUIError();
		}
		ArrayAsEnumerationTypeInfo enumType = new ArrayAsEnumerationTypeInfo(reflectionUI, choices.toArray(),
				"Selection Dialog Array As Enumeration") {
			Map<Object, String> captions = new HashMap<Object, String>();
			Map<Object, Image> iconImages = new HashMap<Object, Image>();

			{
				for (Object choice : choices) {
					captions.put(choice, SwingRenderer.this.reflectionUI.toString(choice));
					iconImages.put(choice, getObjectIconImage(choice));
				}
			}

			@Override
			public IEnumerationItemInfo getValueInfo(final Object object) {
				return new IEnumerationItemInfo() {
					@Override
					public Map<String, Object> getSpecificProperties() {
						Map<String, Object> result = new HashMap<String, Object>();
						result.put(SwingSpecificProperty.KEY_ICON_IMAGE, iconImages.get(object));
						return result;
					}

					@Override
					public String getOnlineHelp() {
						return null;
					}

					@Override
					public String getName() {
						return captions.get(object);
					}

					@Override
					public String getCaption() {
						return captions.get(object);
					}
				};
			}

		};
		Object resultEnumItem;
		for (Object choice : choices) {
			enumType.registerArrayItem(choice);
		}
		try {
			resultEnumItem = openSelectionDialog(parentComponent, enumType, initialSelection, message, title);
		} finally {
			for (Object choice : choices) {
				enumType.unregisterArrayItem(choice);
			}
		}
		if (resultEnumItem == null) {
			return null;
		}
		T result = (T) resultEnumItem;
		return result;

	}

	public Object openSelectionDialog(Component parentComponent, IEnumerationTypeInfo enumType, Object initialEnumItem,
			String message, String title) {
		if (initialEnumItem == null) {
			initialEnumItem = enumType.getPossibleValues()[0];
		}
		final Object[] chosenItemHolder = new Object[] { initialEnumItem };
		final Object chosenItemAsField = VirtualFieldWrapperTypeInfo.wrap(reflectionUI, chosenItemHolder, message,
				"Selection", false);
		if (openObjectDialogAndGetConfirmation(parentComponent, chosenItemAsField, title,
				getObjectIconImage(chosenItemAsField), true)) {
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
		final Object valueAsField = VirtualFieldWrapperTypeInfo.wrap(reflectionUI, valueHolder, dataName, "Selection",
				false);
		if (openObjectDialogAndGetConfirmation(parentComponent, valueAsField, title, getObjectIconImage(valueAsField),
				true)) {
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
		JDialog[] dialogHolder = new JDialog[1];
		final boolean[] okPressedHolder = new boolean[] { false };

		List<JButton> toolbarControls = createStandardOKCancelDialogButtons(dialogHolder, okPressedHolder);

		dialogHolder[0] = createDialog(activatorComponent,
				new JLabel("<HTML><BR>" + question + "<BR><BR><HTML>", SwingConstants.CENTER), title, null,
				toolbarControls, null);
		showDialog(dialogHolder[0], true);
		return okPressedHolder[0];
	}

	protected List<JButton> createStandardOKCancelDialogButtons(JDialog[] dialogHolder,
			final boolean[] okPressedHolder) {
		List<JButton> result = new ArrayList<JButton>();
		result.add(createDialogClosingButton("OK", new Runnable() {
			@Override
			public void run() {
				okPressedHolder[0] = true;
			}
		}, dialogHolder));
		result.add(createDialogClosingButton("Cancel", new Runnable() {
			@Override
			public void run() {
				okPressedHolder[0] = false;
			}
		}, dialogHolder));
		return result;
	}

	protected String getDefaultFieldCaption(Object fieldValue) {
		return BooleanTypeInfo.isCompatibleWith(fieldValue.getClass()) ? "Is True" : "Value";
	}

	public List<IFieldInfo> getDisplayedFields(JPanel form) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getAllFieldControlPlaceHolders(form)) {
			result.add(fieldControlPlaceHolder.getField());
		}
		return result;
	}

	public List<IMethodInfo> getDisplayedMethods(JPanel form) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (MethodControlPlaceHolder methodControlPlaceHolder : getAllMethodControlPlaceHolders(form)) {
			result.add(methodControlPlaceHolder.getMethod());
		}
		return result;
	}

	public void refreshAllFieldControls(JPanel form, boolean recreate) {
		int focusedFieldControlPaceHolderIndex = getFocusedFieldControlPaceHolderIndex(form);
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getAllFieldControlPlaceHolders(form)) {
			fieldControlPlaceHolder.refreshUI(recreate);
			updateFieldControlLayout(fieldControlPlaceHolder);
		}
		if (focusedFieldControlPaceHolderIndex != -1) {
			final FieldControlPlaceHolder toFocus = getAllFieldControlPlaceHolders(form)
					.get(focusedFieldControlPaceHolderIndex);
			toFocus.requestFocus();
		}
	}

	public void refreshFieldControlsByName(JPanel form, String fieldName, boolean recreate) {
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getAllFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getField().getName())) {
				fieldControlPlaceHolder.refreshUI(recreate);
				updateFieldControlLayout(fieldControlPlaceHolder);
			}
		}
	}

	public void showBusyDialogWhile(final Component ownerComponent, final Runnable runnable, String title) {
		final JXBusyLabel busyLabel = new JXBusyLabel();
		busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		busyLabel.setText("Please wait...");
		busyLabel.setVerticalTextPosition(SwingConstants.TOP);
		busyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		final JDialog dialog = createDialog(ownerComponent, busyLabel, title, null, null, null);
		final Thread thread = new Thread(title) {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable t) {
					handleExceptionsFromDisplayedUI(dialog, t);
				} finally {
					busyLabel.setBusy(false);
					dialog.dispose();
				}
			}
		};
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thread.interrupt();
			}
		});
		busyLabel.setBusy(true);
		thread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new ReflectionUIError(e);
		}
		if (busyLabel.isBusy()) {
			showDialog(dialog, true, false);
		}
	}

	protected void showDialog(JDialog dialog, boolean modal) {
		showDialog(dialog, modal, true);
	}

	protected void showDialog(JDialog dialog, boolean modal, boolean closeable) {
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

	public void openMessageDialog(Component activatorComponent, String msg, String title) {
		JDialog[] dialogHolder = new JDialog[1];
		JButton okButton = createDialogClosingButton("Close", null, dialogHolder);
		dialogHolder[0] = createDialog(activatorComponent,
				new JLabel("<HTML><BR>" + msg + "<BR><BR><HTML>", SwingConstants.CENTER), title, null,
				Collections.singletonList(okButton), null);
		showDialog(dialogHolder[0], true);
	}

	protected void updateFieldControlLayout(FieldControlPlaceHolder fieldControlPlaceHolder) {
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

		boolean fieldControlHasCaption = (fieldControl instanceof IFieldControl)
				&& ((IFieldControl) fieldControl).showCaption();
		int spacing = 5;
		if (!fieldControlHasCaption) {
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
			if (fieldControlHasCaption) {
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

	protected Component createSeparateCaptionControl(String caption) {
		return new JLabel(reflectionUI.prepareStringToDisplay(caption + ": "));
	}

	protected void validateForm(JPanel form) {
		Object object = getObjectByForm().get(form);
		if (object == null) {
			return;
		}
		JLabel statusLabel = getStatusLabelByForm().get(form);
		if (statusLabel == null) {
			return;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		try {
			type.validate(object);
			statusLabel.setVisible(false);
		} catch (Exception e) {
			statusLabel.setIcon(SwingRendererUtils.ERROR_ICON);
			statusLabel.setBackground(new Color(255, 245, 242));
			statusLabel.setForeground(new Color(255, 0, 0));
			String errorMsg = new ReflectionUIError(e).toString();
			statusLabel.setText(ReflectionUIUtils.multiToSingleLine(errorMsg));
			SwingRendererUtils.setMultilineToolTipText(statusLabel, errorMsg);
			statusLabel.setVisible(true);
		}
	}

	public final boolean hasCustomFieldControl(Object fieldValue) {
		Object valueAsField = VirtualFieldWrapperTypeInfo.wrap(reflectionUI, new Object[] { fieldValue }, "", "",
				false);
		ITypeInfo valueAsFieldType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(valueAsField));
		IFieldInfo field = valueAsFieldType.getFields().get(0);
		return hasCustomFieldControl(valueAsField, field);
	}

	public class FieldControlPlaceHolder extends JPanel {

		protected static final long serialVersionUID = 1L;
		protected Object object;
		protected IFieldInfo field;
		protected Component fieldControl;

		public FieldControlPlaceHolder(Object object, IFieldInfo field) {
			super();
			this.object = object;
			field = handleValueChangeErrors(field, this);
			this.field = field;
			setLayout(new BorderLayout());
			refreshUI(false);
		}

		public Component getFieldControl() {
			return fieldControl;
		}

		public IFieldInfo getField() {
			return field;
		}

		public void refreshUI(boolean recreate) {
			if (recreate) {
				if (fieldControl != null) {
					remove(fieldControl);
					fieldControl = null;
				}
			}
			if (fieldControl == null) {
				fieldControl = SwingRenderer.this.createFieldControl(object, field);
				add(fieldControl, BorderLayout.CENTER);
				handleComponentSizeChange(this);
			} else {
				if (!(((fieldControl instanceof IFieldControl) && ((IFieldControl) fieldControl).refreshUI()))) {
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
			if (!((fieldControl instanceof IFieldControl) && ((IFieldControl) fieldControl).displayError(error))) {
				if (error != null) {
					handleExceptionsFromDisplayedUI(fieldControl, error);
					refreshUI(false);
				}
			}
		}

		public boolean showCaption() {
			if (((fieldControl instanceof IFieldControl) && ((IFieldControl) fieldControl).showCaption())) {
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
			this.method = method;
			setLayout(new BorderLayout());
			refreshUI(false);
		}

		public Component getMethodControl() {
			return methodControl;
		}

		public IMethodInfo getMethod() {
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
				handleComponentSizeChange(this);
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

	public static class SwingSpecificProperty {
		public static final String KEY_ICON_IMAGE = SwingSpecificProperty.class.getSimpleName() + ".KEY_ICON_IMAGE";
		public static final String KEY_ICON_IMAGE_PATH = SwingSpecificProperty.class.getSimpleName()
				+ ".KEY_ICON_IMAGE_PATH";
		public static final String KEY_ICON_IMAGE_PATH_KIND = SwingSpecificProperty.class.getSimpleName()
				+ ".KEY_ICON_IMAGE_PATH_KIND";
		public static final String CREATE_EMBEDDED_FORM = SwingSpecificProperty.class.getSimpleName()
				+ ".CREATE_EMBEDDED_FORM";

		public static final String VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE = SwingSpecificProperty.class.getSimpleName()
				+ ".VALUE_PATH_TYPE_KIND_ABSOLUTE_FILE";
		public static final String VALUE_PATH_TYPE_KIND_RELATIVE_FILE = SwingSpecificProperty.class.getSimpleName()
				+ ".VALUE_PATH_TYPE_KIND_RELATIVE_FILE";
		public static final String VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE = SwingSpecificProperty.class.getSimpleName()
				+ ".VALUE_PATH_TYPE_KIND_CLASSPATH_RESOURCE";

	}
}