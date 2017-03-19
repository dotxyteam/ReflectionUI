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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXBusyLabel;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.input.FieldControlDataProxy;
import xy.reflect.ui.control.input.DefaultMethodControlData;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.control.input.IMethodControlData;
import xy.reflect.ui.control.input.IMethodControlInput;
import xy.reflect.ui.control.input.MethodControlDataProxy;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationFactory;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.component.ScrollPaneOptions;
import xy.reflect.ui.util.component.WrapLayout;

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
	protected Map<String, InvocationData> lastInvocationDataByMethodSignature = new HashMap<String, InvocationData>();
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

	public Map<String, InvocationData> getLastInvocationDataByMethodSignature() {
		return lastInvocationDataByMethodSignature;
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

	public FieldControlPlaceHolder createFieldControlPlaceHolder(JPanel form, IFieldInfo field) {
		return new FieldControlPlaceHolder(form, field);
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

	public MethodAction createMethodAction(IMethodControlInput input) {
		return new MethodAction(this, input);
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
		return createForm(object, IInfoFilter.NO_FILTER);
	}

	public JPanel createForm(final Object object, IInfoFilter infoFilter) {
		final String formTitle = "Form (" + ReflectionUIUtils.toString(reflectionUI, object) + ")";
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
							updateFormStatusBarInBackground(form);
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

	public Component createCustomFieldControl(IFieldControlInput input, boolean nullable) {
		if (nullable) {
			return null;
		}
		ITypeInfo fieldType = input.getControlData().getType();
		if (fieldType instanceof IListTypeInfo) {
			return new ListControl(this, input);
		} else {
			final Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassforName(fieldType.getName());
			} catch (ClassNotFoundException e) {
				return null;
			}
			if (javaType == Color.class) {
				return new ColorControl(this, input);
			} else if (BooleanTypeInfo.isCompatibleWith(javaType)) {
				return new CheckBoxControl(this, input);
			} else if (TextualTypeInfo.isCompatibleWith(javaType)) {
				if (javaType == String.class) {
					return new TextControl(this, input);
				} else {
					return new PrimitiveValueControl(this, input) {

						private static final long serialVersionUID = 1L;

						@Override
						protected Class<?> getPrimitiveJavaType() {
							return javaType;
						}

					};
				}
			} else if (FileTypeInfo.isCompatibleWith(javaType)) {
				return new FileControl(this, input);
			} else {
				return null;
			}
		}
	}

	public Component createCustomMethodControl(IMethodControlInput input) {
		// TODO Auto-generated method stub
		return null;
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
						updateFormStatusBarInBackground(form);
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

	public void recreateFormContent(final JPanel form) {
		preservingFormFocusAsMuchAsPossible(form, new Runnable() {
			@Override
			public void run() {
				form.removeAll();
				fillForm(form);
				SwingRendererUtils.handleComponentSizeChange(form);

			}
		});
	}

	public void preservingFormFocusAsMuchAsPossible(final JPanel form, Runnable runnable) {
		final boolean formContainsFocus = SwingRendererUtils.hasOrContainsFocus(form);
		final Object formFocusDetails;
		if (formContainsFocus) {
			formFocusDetails = getFormFocusDetails(form);
		} else {
			formFocusDetails = null;
		}
		try {
			runnable.run();
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (formContainsFocus) {
						if (formFocusDetails != null) {
							requestFormDetailedFocus(form, formFocusDetails);
						} else {
							form.requestFocusInWindow();
						}
					}
				}
			});
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
			FieldControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(form, field);
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
			MethodControlPlaceHolder methodControlPlaceHolder = createMethodControlPlaceHolder(form, method);
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

	public MethodControlPlaceHolder createMethodControlPlaceHolder(JPanel form, IMethodInfo method) {
		return new MethodControlPlaceHolder(form, method);
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

	public IFieldControlData getFormAwareFieldControlData(final JPanel form, String fieldName) {
		List<FieldControlPlaceHolder> fieldControlPlaceHolders = getFieldControlPlaceHoldersByName(form, fieldName);
		if (fieldControlPlaceHolders.size() == 0) {
			return null;
		}
		return fieldControlPlaceHolders.get(0).getInitialControlData();
	}

	public IMethodControlData getFormAwareMethodControlData(final JPanel form, String methodSignature) {
		List<MethodControlPlaceHolder> methodControlPlaceHolders = getMethodControlPlaceHoldersBySignature(form,
				methodSignature);
		if (methodControlPlaceHolders.size() == 0) {
			return null;
		}
		return methodControlPlaceHolders.get(0).getInitialControlData();
	}

	public List<FieldControlPlaceHolder> getFieldControlPlaceHoldersByName(JPanel form, String fieldName) {
		List<FieldControlPlaceHolder> result = new ArrayList<FieldControlPlaceHolder>();
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getModificationsTarget().getName())) {
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

	public Image getFieldIconImage(IFieldControlData data) {
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

	public Image getMethodIconImage(IMethodControlData data) {
		return SwingRendererUtils.getCachedIconImage(this, data.getSpecificProperties());
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

			List<IMethodInfo> workingConstructors;
			if (type.isConcrete()) {
				workingConstructors = type.getConstructors();
			} else {
				workingConstructors = Collections.emptyList();
			}

			if (workingConstructors.size() == 0) {
				if (silent) {
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

			if (workingConstructors.size() == 1) {
				final IMethodInfo constructor = workingConstructors.get(0);
				if (silent) {
					return constructor.invoke(null, new InvocationData());
				} else {
					MethodAction methodAction = createMethodAction(new IMethodControlInput() {

						ModificationStack dummyModificationStack = new ModificationStack(null);

						@Override
						public IInfo getModificationsTarget() {
							return constructor;
						}

						@Override
						public ModificationStack getModificationStack() {
							return dummyModificationStack;
						}

						@Override
						public IMethodControlData getControlData() {
							return new DefaultMethodControlData(null, null, constructor);
						}
					});
					methodAction.setShouldDisplayReturnValueIfAny(false);
					methodAction.execute(activatorComponent);
					return methodAction.getReturnValue();
				}
			}

			workingConstructors = new ArrayList<IMethodInfo>(workingConstructors);
			Collections.sort(workingConstructors, new Comparator<IMethodInfo>() {

				@Override
				public int compare(IMethodInfo o1, IMethodInfo o2) {
					return new Integer(o1.getParameters().size()).compareTo(new Integer(o2.getParameters().size()));
				}
			});

			if (silent) {
				IMethodInfo smallerConstructor = workingConstructors.get(0);
				return smallerConstructor.invoke(null, new InvocationData());
			} else {
				final ArrayAsEnumerationFactory enumFactory = new ArrayAsEnumerationFactory(reflectionUI,
						workingConstructors.toArray(), "ConstructorSelection [type=" + type.getName() + "]", "") {
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
				final IMethodInfo chosenContructor = (IMethodInfo) enumFactory.unwrapInstance(resultEnumItem);
				if (chosenContructor == null) {
					return null;
				}
				MethodAction methodAction = createMethodAction(new IMethodControlInput() {

					ModificationStack dummyModificationStack = new ModificationStack(null);

					@Override
					public IInfo getModificationsTarget() {
						return chosenContructor;
					}

					@Override
					public ModificationStack getModificationStack() {
						return dummyModificationStack;
					}

					@Override
					public IMethodControlData getControlData() {
						return new DefaultMethodControlData(null, null, chosenContructor);
					}
				});
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
				true, true).wasOkPressed()) {
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
				true).wasOkPressed()) {
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
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getJOptionPane(prepareStringToDisplay(question), JOptionPane.QUESTION_MESSAGE));
		dialogBuilder.setTitle(title);
		showDialog(dialogBuilder.createDialog(), true);
		return dialogBuilder.wasOkPressed();
	}

	public void openInformationDialog(Component activatorComponent, String msg, String title, Image iconImage) {
		DialogBuilder dialogBuilder = new DialogBuilder(this, activatorComponent);
		JLabel toDisplay = new JLabel("<HTML><CENTER>" + msg + "</CENTER></HTML>", JLabel.CENTER);
		toDisplay.setBorder(BorderFactory.createTitledBorder(""));

		List<Component> buttons = new ArrayList<Component>();
		buttons.add(dialogBuilder.createDialogClosingButton("Close", null));

		dialogBuilder.setTitle(title);
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getJOptionPane(prepareStringToDisplay(msg), JOptionPane.INFORMATION_MESSAGE));
		dialogBuilder.setToolbarComponents(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	public void openErrorDialog(Component activatorComponent, String title, final Throwable error) {
		DialogBuilder dialogBuilder = new DialogBuilder(this, activatorComponent);

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
		dialogBuilder.setContentComponent(SwingRendererUtils.getJOptionPane(
				prepareStringToDisplay(ReflectionUIUtils.getPrettyErrorMessage(error)), JOptionPane.ERROR_MESSAGE));
		dialogBuilder.setToolbarComponents(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		openObjectDialog(activatorComponent, error);
	}

	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object) {
		return openObjectDialog(activatorComponent, object, getObjectTitle(object), getObjectIconImage(object), false,
				true);
	}

	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			final Image iconImage, final boolean cancellable, boolean modal) {
		StandardEditorBuilder dialogBuilder = getObjectDialogBuilder(activatorComponent, object, title, iconImage,
				cancellable);
		showDialog(dialogBuilder.createDialog(), modal);
		return dialogBuilder;
	}

	public StandardEditorBuilder getObjectDialogBuilder(Component activatorComponent, Object object, final String title,
			final Image iconImage, final boolean cancellable) {
		return new StandardEditorBuilder(this, activatorComponent, object) {

			@Override
			protected DialogBuilder createDelegateDialogBuilder() {
				return createDialogBuilder(ownerComponent);
			}

			@Override
			public boolean isCancellable() {
				return cancellable;
			}

			@Override
			public String getEditorTitle() {
				return title;
			}

			@Override
			public Image getObjectIconImage() {
				return iconImage;
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
		StandardEditorBuilder dialogBuilder = getObjectDialogBuilder(null, object, title, iconImage, false);
		JPanel editorPanel = dialogBuilder.createEditorPanel();
		JFrame frame = createFrame(editorPanel, title, iconImage, createCommonToolbarControls(editorPanel));
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

	public void refreshFieldControlsByName(final JPanel form, final String fieldName, final boolean recreate) {
		preservingFormFocusAsMuchAsPossible(form, new Runnable() {
			@Override
			public void run() {
				for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
					if (fieldName.equals(fieldControlPlaceHolder.getModificationsTarget().getName())) {
						fieldControlPlaceHolder.refreshUI(recreate);
						updateFieldControlLayout(fieldControlPlaceHolder);
					}
				}
				SwingRendererUtils.handleComponentSizeChange(form);
			}
		});

	}

	public void refreshAllFieldControls(final JPanel form, final boolean recreate) {
		preservingFormFocusAsMuchAsPossible(form, new Runnable() {
			@Override
			public void run() {
				for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
					fieldControlPlaceHolder.refreshUI(recreate);
					updateFieldControlLayout(fieldControlPlaceHolder);
				}
				SwingRendererUtils.handleComponentSizeChange(form);
			}
		});
	}

	public Object getFormFocusDetails(JPanel form) {
		InfoCategory focusedCategory = getDisplayedInfoCategory(form);
		int focusedFieldIndex = getFocusedFieldControlPaceHolderIndex(form);
		if ((focusedCategory == null) && (focusedFieldIndex == -1)) {
			return null;
		}
		Object focusedFieldFocusDetails = null;
		Class<?> focusedFieldControlClass = null;
		if (focusedFieldIndex != -1) {
			final FieldControlPlaceHolder focusedFieldControlPaceHolder = getFieldControlPlaceHolders(form)
					.get(focusedFieldIndex);
			Component focusedFieldControl = focusedFieldControlPaceHolder.getFieldControl();
			if (focusedFieldControl instanceof IAdvancedFieldControl) {
				focusedFieldFocusDetails = ((IAdvancedFieldControl) focusedFieldControl).getFocusDetails();
				focusedFieldControlClass = focusedFieldControl.getClass();
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("focusedCategory", focusedCategory);
		result.put("focusedFieldIndex", focusedFieldIndex);
		result.put("focusedFieldControlClass", focusedFieldControlClass);
		result.put("focusedFieldFocusDetails", focusedFieldFocusDetails);
		return result;
	}

	public void requestFormDetailedFocus(JPanel form, Object focusDetails) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) focusDetails;
		InfoCategory focusedCategory = (InfoCategory) map.get("focusedCategory");
		int focusedFieldIndex = (Integer) map.get("focusedFieldIndex");
		Class<?> focusedFieldControlClass = (Class<?>) map.get("focusedFieldControlClass");
		Object focusedFieldFocusDetails = map.get("focusedFieldFocusDetails");

		if (focusedCategory != null) {
			setDisplayedInfoCategory(form, focusedCategory);
		}
		if (focusedFieldIndex != -1) {
			FieldControlPlaceHolder fieldControlPaceHolderToFocusOn = getFieldControlPlaceHolders(form)
					.get(focusedFieldIndex);
			Component focusedFieldControl = fieldControlPaceHolderToFocusOn.getFieldControl();
			if (focusedFieldFocusDetails != null) {
				if (focusedFieldControl.getClass().equals(focusedFieldControlClass)) {
					if (focusedFieldControl instanceof IAdvancedFieldControl) {
						((IAdvancedFieldControl) focusedFieldControl).requestDetailedFocus(focusedFieldFocusDetails);
					} else {
						focusedFieldControl.requestFocusInWindow();
					}
				} else {
					focusedFieldControl.requestFocusInWindow();
				}
			} else {
				focusedFieldControl.requestFocusInWindow();
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
							JDialog dialog = dialogBuilder.getCreatedDialog();
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
			final JDialog dialog = dialogBuilder.createDialog();
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

	public void updateFormStatusBar(final JPanel form) {
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

	public void updateFormStatusBarInBackground(final JPanel form) {
		new Thread("Validator: " + getObjectByForm().get(form)) {
			@Override
			public void run() {
				updateFormStatusBar(form);
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

	public class FieldControlPlaceHolder extends JPanel implements IFieldControlInput {

		protected static final long serialVersionUID = 1L;
		protected Component fieldControl;
		protected JPanel form;
		protected IFieldInfo field;
		protected String errorMessageDisplayedOnPlaceHolder;
		protected IFieldControlData controlData;

		public FieldControlPlaceHolder(JPanel form, IFieldInfo field) {
			super();
			this.form = form;
			this.field = field;
			setLayout(new BorderLayout());
			refreshUI(false);
		}

		public IFieldInfo getField() {
			return field;
		}

		public Object getObject() {
			return getObjectByForm().get(form);
		}

		@Override
		public IFieldControlData getControlData() {
			return controlData;
		}

		public void setControlData(IFieldControlData controlData) {
			this.controlData = controlData;
		}

		@Override
		public ModificationStack getModificationStack() {
			return ReflectionUIUtils.findParentFormModificationStack(this, SwingRenderer.this);
		}

		public IFieldControlData makeFieldModificationsUndoable(final IFieldControlData data) {
			return new FieldControlDataProxy(data) {

				@Override
				public void setValue(Object newValue) {
					Component c = fieldControl;
					if ((c instanceof IAdvancedFieldControl)) {
						IAdvancedFieldControl fieldControl = (IAdvancedFieldControl) c;
						if (fieldControl.handlesModificationStackUpdate()) {
							data.setValue(newValue);
							return;
						}
					}
					SwingRendererUtils.setValueThroughModificationStack(data, newValue, getModificationStack(),
							getModificationsTarget());
				}
			};
		}

		public IFieldControlData handleValueAccessIssues(final IFieldControlData data) {
			return new FieldControlDataProxy(data) {

				Object lastFieldValue;
				boolean lastFieldValueInitialized = false;
				Throwable lastValueUpdateError;

				@Override
				public Object getValue() {
					try {
						if (lastValueUpdateError != null) {
							throw lastValueUpdateError;
						}
						lastFieldValue = data.getValue();
						lastFieldValueInitialized = true;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								displayError(null);
							}
						});
					} catch (final Throwable t) {
						if (!lastFieldValueInitialized) {
							throw new ReflectionUIError(t);
						} else {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									displayError(ReflectionUIUtils.getPrettyErrorMessage(t));
								}
							});
						}
					}
					return lastFieldValue;
				}

				@Override
				public void setValue(Object newValue) {
					try {
						lastFieldValue = newValue;
						data.setValue(newValue);
						lastValueUpdateError = null;
					} catch (Throwable t) {
						lastValueUpdateError = t;
					}
				}

			};
		}

		public IFieldControlData indicateWhenBusy(final IFieldControlData data) {
			return new FieldControlDataProxy(data) {

				private boolean isBusyIndicationDisabled() {
					JPanel form = SwingRendererUtils.findParentForm(FieldControlPlaceHolder.this, SwingRenderer.this);
					return Boolean.TRUE.equals(getBusyIndicationDisabledByForm().get(form));
				}

				@Override
				public Object getValue() {
					if (isBusyIndicationDisabled()) {
						return super.getValue();
					}
					return SwingRendererUtils.showBusyDialogWhileGettingFieldValue(FieldControlPlaceHolder.this,
							SwingRenderer.this, data);
				}

				@Override
				public void setValue(final Object value) {
					if (isBusyIndicationDisabled()) {
						super.setValue(value);
						return;
					}
					SwingRendererUtils.showBusyDialogWhileSettingFieldValue(FieldControlPlaceHolder.this,
							SwingRenderer.this, data, value);
				}

				@Override
				public Runnable getCustomUndoUpdateJob(Object value) {
					if (isBusyIndicationDisabled()) {
						return super.getCustomUndoUpdateJob(value);
					}
					final Runnable result = data.getCustomUndoUpdateJob(value);
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
							}, AbstractModification.getUndoTitle("Setting " + data.getCaption()));
						}
					};
				}

			};
		}

		public Component getFieldControl() {
			return fieldControl;
		}

		@Override
		public IInfo getModificationsTarget() {
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
				try {
					controlData = getInitialControlData();
					fieldControl = createFieldControl();
				} catch (Throwable t) {
					fieldControl = createUIRefreshErrorControl(t);
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
						fieldControl.requestFocusInWindow();
					}
				}
			}
		}

		public IFieldControlData getInitialControlData() {
			Object object = getObject();
			Object[] valueOptions = field.getValueOptions(object);
			final IFieldInfo finalField;
			if (valueOptions == null) {
				finalField = field;
			} else {
				ITypeInfo ownerType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
				final ArrayAsEnumerationFactory enumFactory = new ArrayAsEnumerationFactory(reflectionUI, valueOptions,
						"ValueOptions [ownerType=" + ownerType.getName() + ", field=" + field.getName() + "]", "");
				final ITypeInfo enumType = reflectionUI.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
				finalField = new FieldInfoProxy(field) {

					@Override
					public Object getValue(Object object) {
						Object value = super.getValue(object);
						return enumFactory.getInstance(value);
					}

					@Override
					public void setValue(Object object, Object value) {
						value = enumFactory.unwrapInstance(value);
						super.setValue(object, value);
					}

					@Override
					public Runnable getCustomUndoUpdateJob(Object object, Object value) {
						value = enumFactory.unwrapInstance(value);
						return super.getCustomUndoUpdateJob(object, value);
					}

					@Override
					public ITypeInfo getType() {
						return enumType;
					}

				};
			}
			IFieldControlData result = new IFieldControlData() {

				@Override
				public Object getValue() {
					return finalField.getValue(getObject());
				}

				@Override
				public void setValue(Object value) {
					finalField.setValue(getObject(), value);
				}

				@Override
				public String getCaption() {
					return finalField.getCaption();
				}

				@Override
				public Runnable getCustomUndoUpdateJob(Object value) {
					return finalField.getCustomUndoUpdateJob(getObject(), value);
				}

				@Override
				public ITypeInfo getType() {
					return finalField.getType();
				}

				@Override
				public boolean isGetOnly() {
					return finalField.isGetOnly();
				}

				@Override
				public ValueReturnMode getValueReturnMode() {
					return finalField.getValueReturnMode();
				}

				@Override
				public boolean isNullable() {
					return finalField.isNullable();
				}

				@Override
				public String getNullValueLabel() {
					return finalField.getNullValueLabel();
				}

				@Override
				public Map<String, Object> getSpecificProperties() {
					return finalField.getSpecificProperties();
				}

				@Override
				public String toString() {
					return "InitialControlData[ of=" + FieldControlPlaceHolder.this + "]";
				}

			};
			result = indicateWhenBusy(result);
			result = handleValueAccessIssues(result);
			result = makeFieldModificationsUndoable(result);
			return result;
		}

		public Component createFieldControl() {
			Component result = createCustomFieldControl(this, controlData.isNullable());
			if (result != null) {
				return result;
			}
			if (controlData.getType() instanceof IEnumerationTypeInfo) {
				return new EnumerationControl(SwingRenderer.this, this);
			} else if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(controlData.getType())
					&& !DesktopSpecificProperty.isPolymorphicControlForbidden(
							DesktopSpecificProperty.accessControlDataProperties(controlData))) {
				return new PolymorphicControl(SwingRenderer.this, this);
			} else {
				if (controlData.isNullable()) {
					if (!controlData.isGetOnly()) {
						return new NullableControl(SwingRenderer.this, this);
					}
				}
				Object value = controlData.getValue();
				if (value == null) {
					return new NullControl(SwingRenderer.this, this);
				}
				result = createCustomFieldControl(this, false);
				if (result != null) {
					return result;
				}
				final ITypeInfo valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
				if (!valueType.equals(controlData.getType())) {
					controlData = new FieldControlDataProxy(controlData) {
						@Override
						public ITypeInfo getType() {
							return valueType;
						}
					};
					return createFieldControl();
				}
				if (DesktopSpecificProperty
						.isSubFormExpanded(DesktopSpecificProperty.accessControlDataProperties(controlData))) {
					return new EmbeddedFormControl(SwingRenderer.this, this);
				} else {
					return new DialogAccessControl(SwingRenderer.this, this);
				}
			}
		}

		public Component createUIRefreshErrorControl(final Throwable t) {
			reflectionUI.logError(t);
			NullControl result = new NullControl(SwingRenderer.this, this) {

				private static final long serialVersionUID = 1L;

				@Override
				protected Object getText() {
					return new ReflectionUIError(t).toString();
				}

			};
			SwingRendererUtils.setErrorBorder(result);
			return result;
		}

		public void displayError(String msg) {
			boolean done = (fieldControl instanceof IAdvancedFieldControl)
					&& ((IAdvancedFieldControl) fieldControl).displayError(msg);
			if (!done && (msg != null)) {
				if (errorMessageDisplayedOnPlaceHolder == null) {
					errorMessageDisplayedOnPlaceHolder = msg;
					SwingRendererUtils.setErrorBorder(this);
					handleExceptionsFromDisplayedUI(fieldControl, new ReflectionUIError(msg));
				}
			} else {
				errorMessageDisplayedOnPlaceHolder = null;
				setBorder(null);
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
		public boolean requestFocusInWindow() {
			if (fieldControl != null) {
				return fieldControl.requestFocusInWindow();
			}
			return false;
		}

	}

	public class MethodControlPlaceHolder extends JPanel implements IMethodControlInput {

		protected static final long serialVersionUID = 1L;
		protected JPanel form;
		protected Component methodControl;
		protected IMethodInfo method;
		protected IMethodControlData controlData;

		public MethodControlPlaceHolder(JPanel form, IMethodInfo method) {
			super();
			this.form = form;
			this.method = method;
			setLayout(new BorderLayout());
			refreshUI(false);
		}

		public IMethodControlData makeMethodModificationsUndoable(final IMethodControlData data) {
			return new MethodControlDataProxy(data) {

				@Override
				public Object invoke(InvocationData invocationData) {
					return SwingRendererUtils.invokeMethodThroughModificationStack(data, invocationData,
							getModificationStack(), getModificationsTarget());
				}

			};
		}

		public IMethodControlData indicateWhenBusy(final IMethodControlData data) {
			return new MethodControlDataProxy(data) {

				@Override
				public Object invoke(final InvocationData invocationData) {
					return SwingRendererUtils.showBusyDialogWhileInvokingMethod(MethodControlPlaceHolder.this,
							SwingRenderer.this, data, invocationData);
				}

				@Override
				public Runnable getUndoJob(InvocationData invocationData) {
					final Runnable result = data.getUndoJob(invocationData);
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
									.getUndoTitle(ReflectionUIUtils.composeMessage(data.getCaption(), "Execution")));
						}
					};
				}

			};
		}

		public Component getMethodControl() {
			return methodControl;
		}

		public Object getObject() {
			return getObjectByForm().get(form);
		}

		@Override
		public IMethodControlData getControlData() {
			return controlData;
		}

		@Override
		public IInfo getModificationsTarget() {
			return method;
		}

		@Override
		public ModificationStack getModificationStack() {
			return ReflectionUIUtils.findParentFormModificationStack(this, SwingRenderer.this);
		}

		public IMethodInfo getMethod() {
			return method;
		}

		public Component createMethodControl() {
			Component result = createCustomMethodControl(this);
			if (result != null) {
				return result;
			}
			return new MethodControl(SwingRenderer.this, this);
		}

		public void refreshUI(boolean recreate) {
			if (recreate) {
				if (methodControl != null) {
					remove(methodControl);
					methodControl = null;
				}
			}
			if (methodControl == null) {
				controlData = getInitialControlData();
				methodControl = createMethodControl();
				add(methodControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			} else {
				boolean hadFocus = SwingRendererUtils.hasOrContainsFocus(methodControl);
				remove(methodControl);
				methodControl = null;
				refreshUI(false);
				if (hadFocus) {
					methodControl.requestFocusInWindow();
				}
			}
		}

		public IMethodControlData getInitialControlData() {
			IMethodControlData result = new IMethodControlData() {

				@Override
				public void validateParameters(InvocationData invocationData) throws Exception {
					method.validateParameters(getObject(), invocationData);
				}

				@Override
				public boolean isReadOnly() {
					return method.isReadOnly();
				}

				@Override
				public Object invoke(InvocationData invocationData) {
					return method.invoke(getObject(), invocationData);
				}

				@Override
				public ValueReturnMode getValueReturnMode() {
					return method.getValueReturnMode();
				}

				@Override
				public Runnable getUndoJob(InvocationData invocationData) {
					return method.getUndoJob(getObject(), invocationData);
				}

				@Override
				public ITypeInfo getReturnValueType() {
					return method.getReturnValueType();
				}

				@Override
				public List<IParameterInfo> getParameters() {
					return method.getParameters();
				}

				@Override
				public String getNullReturnValueLabel() {
					return method.getNullReturnValueLabel();
				}

				@Override
				public String getOnlineHelp() {
					return method.getOnlineHelp();
				}

				@Override
				public Map<String, Object> getSpecificProperties() {
					return method.getSpecificProperties();
				}

				@Override
				public String getCaption() {
					return method.getCaption();
				}

				@Override
				public String getMethodSignature() {
					return ReflectionUIUtils.getMethodSignature(method);
				}

				public ITypeInfo getMethodOwnerType() {
					Object objet = getObject();
					if (objet == null) {
						return null;
					}
					return reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(objet));
				}

				@Override
				public String toString() {
					return "InitialControlData [of=" + MethodControlPlaceHolder.this + "]";
				}

			};

			result = indicateWhenBusy(result);
			result = makeMethodModificationsUndoable(result);
			return result;
		}

		@Override
		public boolean requestFocusInWindow() {
			if (methodControl != null) {
				return methodControl.requestFocusInWindow();
			}
			return false;
		}

	}
}