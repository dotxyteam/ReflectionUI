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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.jdesktop.swingx.JXBusyLabel;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.customization.SwingCustomizer;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.plugin.ColorPickerPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.SliderPlugin;
import xy.reflect.ui.control.swing.plugin.SpinnerPlugin;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ValueOptionsAsEnumerationField;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.factory.FilterredTypeFactory;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.factory.InfoCustomizations;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ResourcePath;
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
	protected Map<JPanel, Container> categoriesTabbedPaneByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Boolean> busyIndicationDisabledByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Boolean> refreshRequestQueuedByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Boolean> refreshRequestExecutingByForm = new MapMaker().weakKeys().makeMap();
	protected Map<Component, IFieldControlPlugin> pluginByFieldControl = new MapMaker().weakKeys().makeMap();

	public SwingRenderer(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "SwingRenderer.DEFAULT";
		} else {
			return super.toString();
		}
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

	protected Map<JPanel, Boolean> getRefreshRequestQueuedByForm() {
		return refreshRequestQueuedByForm;
	}

	protected Map<JPanel, Boolean> getRefreshRequestExecutingByForm() {
		return refreshRequestExecutingByForm;
	}

	public Map<Component, IFieldControlPlugin> getPluginByFieldControl() {
		return pluginByFieldControl;
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
			window.setIconImage(SwingRendererUtils.NULL_IMAGE);
		} else {
			window.setIconImage(iconImage);
		}
		Container contentPane = createWindowContentPane(window, content, toolbarControls);
		SwingRendererUtils.setContentPane(window, contentPane);
		SwingRendererUtils.adjustWindowInitialBounds(window);
	}

	public List<Component> createFormCommonToolbarControls(final JPanel form) {
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
			final ModificationStack modificationStack = getModificationStackByForm().get(form);
			if (modificationStack != null) {
				result.addAll(new ModificationStackControls(modificationStack).create(this));
			}
		}
		return result;

	}

	public FieldControlPlaceHolder createFieldControlPlaceHolder(JPanel form, IFieldInfo field) {
		return new FieldControlPlaceHolder(form, field);
	}

	public Container createFieldsPanel(List<FieldControlPlaceHolder> fielControlPlaceHolders) {
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new GridBagLayout());
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FieldControlPlaceHolder fieldControlPlaceHolder = fielControlPlaceHolders.get(i);
			{
				GridBagConstraints layoutConstraints = new GridBagConstraints();
				layoutConstraints.gridy = i;
				fieldsPanel.add(fieldControlPlaceHolder, layoutConstraints);
				updateFieldControlLayout(fieldControlPlaceHolder, true);
			}
			IFieldInfo field = fieldControlPlaceHolder.getField();
			if ((field.getOnlineHelp() != null) && (field.getOnlineHelp().trim().length() > 0)) {
				GridBagConstraints layoutConstraints = new GridBagConstraints();
				int spacing = getLayoutSpacing();
				layoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
				layoutConstraints.gridx = 2;
				layoutConstraints.gridy = i;
				layoutConstraints.weighty = 1.0;
				fieldsPanel.add(createOnlineHelpControl(field.getOnlineHelp()), layoutConstraints);
			}

		}
		return fieldsPanel;
	}

	public int getLayoutSpacing() {
		return 5;
	}

	public JFrame createFrame(Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls) {
		JFrame frame = new JFrame();
		setupWindow(frame, content, toolbarControls, title, iconImage);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	public MethodAction createMethodAction(IMethodControlInput input) {
		return new MethodAction(this, input);
	}

	public Container createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
		Container result = new JPanel();
		result.setLayout(new WrapLayout(WrapLayout.CENTER, getLayoutSpacing(), getLayoutSpacing()));
		for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
			result.add(methodControlPlaceHolder);
		}
		return result;
	}

	public Container createMultipleInfoCategoriesComponent(final SortedSet<InfoCategory> allCategories,
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
		final JPanel result = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public String toString() {
				return "Form [id=" + hashCode() + ", object=" + getObjectByForm().get(this) + "]";
			}

		};
		getObjectByForm().put(result, object);
		getModificationStackByForm().put(result, new ModificationStack(result.toString()));
		if (infoFilter != null) {
			getInfoFilterByForm().put(result, infoFilter);
		} else {
			getInfoFilterByForm().remove(result);
		}
		result.addAncestorListener(new AncestorListener() {

			IModificationListener fieldsUpdateListener = new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					if (Boolean.TRUE.equals(getFieldsUpdateListenerDisabledByForm().get(result))) {
						return;
					}
					ensureFormGetsRefreshed(result);
				}
			};

			@Override
			public void ancestorAdded(AncestorEvent event) {
				ModificationStack modifStack = getModificationStackByForm().get(result);
				modifStack.addListener(fieldsUpdateListener);
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				ModificationStack modifStack = getModificationStackByForm().get(result);
				modifStack.removeListener(fieldsUpdateListener);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
		result.setLayout(new BorderLayout());
		fillForm(result);
		return result;
	}

	public void ensureFormGetsRefreshed(final JPanel form) {
		new Thread("Refresher[of=" + form + "]") {
			@Override
			public void run() {
				if (isRefreshRequestQueued()) {
					return;
				}
				setRefreshRequestQueued(true);
				while (isRefreshRequestExecuting()) {
					try {
						sleep(100);
					} catch (InterruptedException e) {
						throw new ReflectionUIError(e);
					}
				}
				setRefreshRequestQueued(false);
				setRefreshRequestExecuting(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							refreshAllFieldControls(form, false);
							updateFormStatusBarInBackground(form);
							Object object = getObjectByForm().get(form);
							for (JPanel otherForm : SwingRendererUtils.findObjectForms(object, SwingRenderer.this)) {
								if (otherForm != form) {
									ModificationStack otherModifStack = getModificationStackByForm().get(otherForm);
									if (otherForm.isDisplayable()) {
										getFieldsUpdateListenerDisabledByForm().put(otherForm, Boolean.TRUE);
										otherModifStack.invalidate();
										getFieldsUpdateListenerDisabledByForm().put(otherForm, Boolean.FALSE);
									}
								}
							}
						} finally {
							setRefreshRequestExecuting(false);
						}
					}
				});
			}

			private void setRefreshRequestExecuting(boolean b) {
				getRefreshRequestExecutingByForm().put(form, b);
			}

			private void setRefreshRequestQueued(boolean b) {
				getRefreshRequestQueuedByForm().put(form, b);
			}

			private boolean isRefreshRequestExecuting() {
				return Boolean.TRUE.equals(getRefreshRequestExecutingByForm().get(form));
			}

			private boolean isRefreshRequestQueued() {
				return Boolean.TRUE.equals(getRefreshRequestQueuedByForm().get(form));
			}
		}.start();
	}

	public Component createErrorControl(final Throwable t) {
		reflectionUI.logError(t);
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		result.add(
				new NullControl(SwingRenderer.this, new FieldControlInputProxy(IFieldControlInput.NULL_CONTROL_INPUT) {
					@Override
					public IFieldControlData getControlData() {
						return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {
							@Override
							public String getNullValueLabel() {
								return ReflectionUIUtils.getPrettyErrorMessage(t);
							}
						};
					}
				}), BorderLayout.CENTER);
		SwingRendererUtils.setErrorBorder(result);
		return result;
	}

	public Component createCustomFieldControl(IFieldControlInput input) {
		IFieldControlPlugin currentPlugin = null;
		String chosenPluginId = (String) input.getControlData().getSpecificProperties()
				.get(IFieldControlPlugin.CHOSEN_PROPERTY_KEY);
		if (!IFieldControlPlugin.NONE_IDENTIFIER.equals(chosenPluginId)) {
			for (IFieldControlPlugin plugin : getFieldControlPlugins()) {
				if (plugin.getIdentifier().equals(chosenPluginId)) {
					if (plugin.handles(input)) {
						currentPlugin = plugin;
						break;
					}
				}
			}
		}

		if (currentPlugin == null) {
			if (input.getControlData().getType() instanceof IEnumerationTypeInfo) {
				return new EnumerationControl(SwingRenderer.this, input);
			}
			if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(input.getControlData().getType())) {
				return new PolymorphicControl(SwingRenderer.this, input);
			}
			if (!input.getControlData().isValueNullable()) {
				ITypeInfo fieldType = input.getControlData().getType();
				if (fieldType instanceof IListTypeInfo) {
					return new ListControl(this, input);
				}
				final Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(fieldType.getName());
				} catch (ClassNotFoundException e) {
					return null;
				}
				if (boolean.class.equals(javaType) || Boolean.class.equals(javaType)) {
					return new CheckBoxControl(this, input);
				}
				if (ClassUtils.isPrimitiveClassOrWrapper(javaType)) {
					return new PrimitiveValueControl(this, input, javaType);
				}
				if (String.class.equals(javaType)) {
					return new TextControl(this, input);
				}
			}
		}

		if (currentPlugin == null) {
			if (!IFieldControlPlugin.NONE_IDENTIFIER.equals(chosenPluginId)) {
				for (IFieldControlPlugin plugin : getFieldControlPlugins()) {
					if (plugin.handles(input)) {
						currentPlugin = plugin;
						break;
					}
				}
			}
		}

		if (currentPlugin != null) {
			Component result;
			try {
				result = currentPlugin.createControl(SwingRenderer.this, input);
			} catch (Throwable t) {
				result = createErrorControl(t);
			}
			getPluginByFieldControl().put(result, currentPlugin);
			return result;
		}

		return null;
	}

	public List<IFieldControlPlugin> getFieldControlPlugins() {
		List<IFieldControlPlugin> result = new ArrayList<IFieldControlPlugin>();
		result.add(new SliderPlugin());
		result.add(new SpinnerPlugin());
		result.add(new FileBrowserPlugin());
		result.add(new ColorPickerPlugin());
		return result;
	}

	public Component createCustomMethodControl(IMethodControlInput input) {
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
			final JScrollPane scrollPane = createWindowScrollPane(content);
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

	public JScrollPane createWindowScrollPane(Component content) {
		return new JScrollPane(new ScrollPaneOptions(content, true, false));
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
		final Object formFocusDetails = getFormFocusDetails(form);
		final InfoCategory focusedCategory = getDisplayedInfoCategory(form);
		try {
			runnable.run();
		} finally {
			if (focusedCategory != null) {
				setDisplayedInfoCategory(form, focusedCategory);
			}
			final boolean success;
			if (formFocusDetails != null) {
				success = requestFormDetailedFocus(form, formFocusDetails);
			} else {
				success = false;
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					boolean successConfirmedLater = SwingRendererUtils.hasOrContainsFocus(form);
					if ((formFocusDetails != null) && (!success || !successConfirmedLater)) {
						reflectionUI.logDebug("WARNING: Failed to restore focus of " + form + "\n\tfocusedCategory="
								+ focusedCategory + "\n\tformFocusDetails=" + formFocusDetails + "\n\tsuccess="
								+ success + "\n\tsuccessConfirmedLater=" + successConfirmedLater);
					}
				}
			});
		}
	}

	public void setDisplayedInfoCategory(JPanel form, InfoCategory category) {
		JTabbedPane categoriesControl = (JTabbedPane) categoriesTabbedPaneByForm.get(form);
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
		JTabbedPane categoriesControl = (JTabbedPane) categoriesTabbedPaneByForm.get(form);
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
		if (infoFilter == null) {
			infoFilter = IInfoFilter.DEFAULT;
		}

		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FieldControlPlaceHolder>>();
		getFieldControlPlaceHoldersByCategoryByForm().put(form, fieldControlPlaceHoldersByCategory);
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		type = new FilterredTypeFactory(infoFilter).get(type);
		List<IFieldInfo> fields = type.getFields();
		for (IFieldInfo field : fields) {
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
			Container categoriesControl = createMultipleInfoCategoriesComponent(allCategories,
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

	public IFieldInfo getFormField(final JPanel form, String fieldName) {
		List<FieldControlPlaceHolder> fieldControlPlaceHolders = getFieldControlPlaceHoldersByName(form, fieldName);
		if (fieldControlPlaceHolders.size() == 0) {
			return null;
		}
		return fieldControlPlaceHolders.get(0).getField();
	}

	public IMethodInfo getFormMethod(final JPanel form, String methodSignature) {
		List<MethodControlPlaceHolder> methodControlPlaceHolders = getMethodControlPlaceHoldersBySignature(form,
				methodSignature);
		if (methodControlPlaceHolders.size() == 0) {
			return null;
		}
		return methodControlPlaceHolders.get(0).getMethod();
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
			String imagePathSpecification = type.getIconImagePath();
			if (imagePathSpecification == null) {
				return null;
			}
			Image result = SwingRendererUtils.loadImageThroughcache(new ResourcePath(imagePathSpecification),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Image getMethodIconImage(IMethodControlData data) {
		String imagePathSpecification = data.getIconImagePath();
		if (imagePathSpecification == null) {
			return null;
		}
		return SwingRendererUtils.loadImageThroughcache(new ResourcePath(imagePathSpecification),
				ReflectionUIUtils.getErrorLogListener(reflectionUI));
	}

	public void handleExceptionsFromDisplayedUI(Component activatorComponent, final Throwable t) {
		reflectionUI.logError(t);
		openErrorDialog(activatorComponent, "An Error Occured", t);
	}

	public void layoutControlPanels(JPanel parentForm, Container fieldsPanel, Container methodsPanel) {
		parentForm.setLayout(new BorderLayout());
		if (fieldsPanel != null) {
			parentForm.add(fieldsPanel, BorderLayout.CENTER);
		}
		if (methodsPanel != null) {
			parentForm.add(methodsPanel, BorderLayout.SOUTH);
		}
	}

	public void layoutControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, JPanel parentForm) {
		Container fieldsPanel = (fielControlPlaceHolders.size() == 0) ? null
				: createFieldsPanel(fielControlPlaceHolders);
		Container methodsPanel = (methodControlPlaceHolders.size() == 0) ? null
				: createMethodsPanel(methodControlPlaceHolders);
		layoutControlPanels(parentForm, fieldsPanel, methodsPanel);
	}

	public Object onTypeInstanciationRequest(final Component activatorComponent, ITypeInfo type) {
		try {

			if (type.isConcrete() && (type.getConstructors().size() > 0)) {
				List<IMethodInfo> constructors = type.getConstructors();
				final IMethodInfo chosenConstructor;
				if (constructors.size() == 1) {
					chosenConstructor = constructors.get(0);
				} else {
					constructors = new ArrayList<IMethodInfo>(constructors);
					Collections.sort(constructors, new Comparator<IMethodInfo>() {

						@Override
						public int compare(IMethodInfo o1, IMethodInfo o2) {
							return new Integer(o1.getParameters().size())
									.compareTo(new Integer(o2.getParameters().size()));
						}
					});

					final GenericEnumerationFactory enumFactory = new GenericEnumerationFactory(reflectionUI,
							constructors.toArray(), "ConstructorSelection [type=" + type.getName() + "]", "") {
						protected String getItemCaption(Object choice) {
							return ReflectionUIUtils.getContructorDescription((IMethodInfo) choice);
						}
					};
					IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
							.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
					Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null, "Choose an option",
							"Create " + type.getCaption());
					if (resultEnumItem == null) {
						return null;
					}
					chosenConstructor = (IMethodInfo) enumFactory.unwrapInstance(resultEnumItem);
					if (chosenConstructor == null) {
						return null;
					}
				}
				final ITypeInfo finalType = type;
				MethodAction methodAction = createMethodAction(new IMethodControlInput() {

					ModificationStack dummyModificationStack = new ModificationStack(null);

					@Override
					public IInfo getModificationsTarget() {
						return chosenConstructor;
					}

					@Override
					public ModificationStack getModificationStack() {
						return dummyModificationStack;
					}

					@Override
					public String getContextIdentifier() {
						return "ContructorContext [type=" + finalType.getName() + ", signature="
								+ ReflectionUIUtils.getMethodSignature(chosenConstructor) + "]";
					}

					@Override
					public IMethodControlData getControlData() {
						return new DefaultMethodControlData(null, chosenConstructor);
					}
				});
				methodAction.setShouldDisplayReturnValueIfAny(false);
				methodAction.execute(activatorComponent);
				return methodAction.getReturnValue();
			} else {
				if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
					List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
					if (polyTypes.size() == 1) {
						return onTypeInstanciationRequest(activatorComponent, polyTypes.get(0));
					} else {
						final PolymorphicTypeOptionsFactory enumFactory = new PolymorphicTypeOptionsFactory(
								reflectionUI, type);
						IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) reflectionUI
								.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
						Object resultEnumItem = openSelectionDialog(activatorComponent, enumType, null,
								"Choose a type:", "New '" + type.getCaption() + "'");
						if (resultEnumItem == null) {
							return null;
						}
						return onTypeInstanciationRequest(activatorComponent,
								(ITypeInfo) enumFactory.unwrapInstance(resultEnumItem));
					}
				} else {
					String typeCaption = type.getCaption();
					String msg;
					if (typeCaption.length() == 0) {
						msg = "Create";
					} else {
						msg = "Create " + type.getCaption() + " of type";
					}
					String className = openInputDialog(activatorComponent, "", msg, null);
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
						return onTypeInstanciationRequest(activatorComponent, type);
					}
				}
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

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, enumType, "Selection",
				message);
		encapsulation.setFieldGetOnly(false);
		encapsulation.setFieldNullable(false);
		Object encapsulatedChosenItem = encapsulation.getInstance(chosenItemHolder);

		if (!openObjectDialog(parentComponent, encapsulatedChosenItem, title,
				getObjectIconImage(encapsulatedChosenItem), true, true).isCancelled()) {
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

		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(reflectionUI, initialValueType, "Input",
				valueCaption);
		encapsulation.setFieldGetOnly(false);
		encapsulation.setFieldNullable(false);
		Object encapsulatedValue = encapsulation.getInstance(valueHolder);

		if (!openObjectDialog(parentComponent, encapsulatedValue, title, getObjectIconImage(encapsulatedValue), true,
				true).isCancelled()) {
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
		DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
		dialogBuilder.setToolbarComponents(dialogBuilder.createStandardOKCancelDialogButtons(yesCaption, noCaption));
		dialogBuilder.setContentComponent(SwingRendererUtils.getMessageJOptionPane(prepareStringToDisplay(question),
				JOptionPane.QUESTION_MESSAGE));
		dialogBuilder.setTitle(title);
		showDialog(dialogBuilder.createDialog(), true);
		return dialogBuilder.wasOkPressed();
	}

	public void openInformationDialog(Component activatorComponent, String msg, String title, Image iconImage) {
		DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);

		List<Component> buttons = new ArrayList<Component>();
		buttons.add(dialogBuilder.createDialogClosingButton("Close", null));

		dialogBuilder.setTitle(title);
		dialogBuilder.setContentComponent(
				SwingRendererUtils.getMessageJOptionPane(prepareStringToDisplay(msg), JOptionPane.INFORMATION_MESSAGE));
		dialogBuilder.setToolbarComponents(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	public void openErrorDialog(Component activatorComponent, String title, final Throwable error) {
		DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);

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
		dialogBuilder.setContentComponent(SwingRendererUtils.getMessageJOptionPane(
				prepareStringToDisplay(ReflectionUIUtils.getPrettyErrorMessage(error)), JOptionPane.ERROR_MESSAGE));
		dialogBuilder.setToolbarComponents(buttons);

		showDialog(dialogBuilder.createDialog(), true);
	}

	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		openObjectDialog(activatorComponent, error);
	}

	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object) {
		return openObjectDialog(activatorComponent, object, null, null, false, true);
	}

	public StandardEditorBuilder openObjectDialog(Component activatorComponent, Object object, final String title,
			final Image iconImage, final boolean cancellable, boolean modal) {
		StandardEditorBuilder editorBuilder = getEditorBuilder(activatorComponent, object, title, iconImage,
				cancellable);
		showDialog(editorBuilder.createDialog(), modal);
		return editorBuilder;
	}

	public StandardEditorBuilder getEditorBuilder(Component activatorComponent, final Object object, final String title,
			final Image iconImage, final boolean cancellable) {
		return new StandardEditorBuilder(this, activatorComponent, object) {

			@Override
			protected DialogBuilder createDelegateDialogBuilder() {
				return getDialogBuilder(ownerComponent);
			}

			@Override
			public boolean isCancellable() {
				return cancellable;
			}

			@Override
			public String getEditorWindowTitle() {
				if (title == null) {
					return super.getEditorWindowTitle();
				}
				return title;
			}

			@Override
			public Image getObjectIconImage() {
				if (iconImage == null) {
					return super.getObjectIconImage();
				}
				return iconImage;
			}

		};
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		StandardEditorBuilder editorBuilder = getEditorBuilder(null, object, title, iconImage, false);
		showFrame(editorBuilder.createFrame());

	}

	public void showFrame(JFrame frame) {
		frame.setVisible(true);
	}

	public void openObjectFrame(Object object) {
		openObjectFrame(object, null, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent, final List<T> choices, T initialSelection,
			String message, String title) {
		if (choices.size() == 0) {
			throw new ReflectionUIError();
		}
		final GenericEnumerationFactory enumFactory = new GenericEnumerationFactory(reflectionUI, choices.toArray(),
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
			protected String getItemIconImagePath(Object choice) {
				Image image = iconImages.get(choice);
				return SwingRendererUtils.putImageInCached(image).getSpecification();
			}

			@Override
			protected String getItemName(Object choice) {
				return "Option [caption=" + captions.get(choice) + "]";
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

	public DialogBuilder getDialogBuilder(Component activatorComponent) {
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
						updateFieldControlLayout(fieldControlPlaceHolder, false);
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
					updateFieldControlLayout(fieldControlPlaceHolder, false);
				}
				SwingRendererUtils.handleComponentSizeChange(form);
			}
		});
	}

	public Object getFormFocusDetails(JPanel form) {
		List<FieldControlPlaceHolder> fieldControlPlaceHolders = getFieldControlPlaceHolders(form);
		int focusedFieldIndex = -1;
		{
			int i = 0;
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHolders) {
				if (SwingRendererUtils.hasOrContainsFocus(fieldControlPlaceHolder)) {
					focusedFieldIndex = i;
					break;
				}
				i++;
			}
		}
		if (focusedFieldIndex == -1) {
			return null;
		}
		Object focusedFieldFocusDetails = null;
		if (focusedFieldIndex != -1) {
			final FieldControlPlaceHolder focusedFieldControlPaceHolder = fieldControlPlaceHolders
					.get(focusedFieldIndex);
			Component focusedFieldControl = focusedFieldControlPaceHolder.getFieldControl();
			if (focusedFieldControl instanceof IAdvancedFieldControl) {
				focusedFieldFocusDetails = ((IAdvancedFieldControl) focusedFieldControl).getFocusDetails();
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("focusedFieldIndex", focusedFieldIndex);
		result.put("focusedFieldFocusDetails", focusedFieldFocusDetails);
		return result;
	}

	public boolean requestFormDetailedFocus(JPanel form, Object focusDetails) {
		List<FieldControlPlaceHolder> fieldControlPlaceHolders = getFieldControlPlaceHolders(form);
		if (focusDetails == null) {
			if (fieldControlPlaceHolders.size() > 0) {
				return SwingRendererUtils.requestAnyComponentFocus(fieldControlPlaceHolders.get(0).getFieldControl(),
						null, this);
			} else {
				return false;
			}
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) focusDetails;
		int focusedFieldIndex = (Integer) map.get("focusedFieldIndex");
		Object focusedFieldFocusDetails = map.get("focusedFieldFocusDetails");

		if ((focusedFieldIndex != -1) && (focusedFieldIndex < fieldControlPlaceHolders.size())) {
			Component focusedFieldControl = fieldControlPlaceHolders.get(focusedFieldIndex).getFieldControl();
			return SwingRendererUtils.requestAnyComponentFocus(focusedFieldControl, focusedFieldFocusDetails, this);
		}
		return false;
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
			final DialogBuilder dialogBuilder = getDialogBuilder(activatorComponent);
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

	public void updateFieldControlLayout(FieldControlPlaceHolder fieldControlPlaceHolder, boolean initialUpdate) {

		boolean shouldHaveSeparateCaptionControl = !fieldControlPlaceHolder.showsCaption()
				&& (fieldControlPlaceHolder.getField().getCaption().length() > 0);

		if (!initialUpdate) {
			boolean hasSeparateCaptionControl = captionControlByFieldControlPlaceHolder
					.containsKey(fieldControlPlaceHolder);
			if (hasSeparateCaptionControl == shouldHaveSeparateCaptionControl) {
				if (!hasSeparateCaptionControl) {

				}
				return;
			}
		}

		Container container = fieldControlPlaceHolder.getParent();
		GridBagLayout layout = (GridBagLayout) container.getLayout();
		int i = layout.getConstraints(fieldControlPlaceHolder).gridy;

		container.remove(fieldControlPlaceHolder);
		Component captionControl = captionControlByFieldControlPlaceHolder.get(fieldControlPlaceHolder);
		if (captionControl != null) {
			container.remove(captionControl);
			captionControlByFieldControlPlaceHolder.remove(fieldControlPlaceHolder);
		}

		int spacing = 5;
		if (shouldHaveSeparateCaptionControl) {
			captionControl = createSeparateCaptionControl(fieldControlPlaceHolder.getField().getCaption());
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
			if (!shouldHaveSeparateCaptionControl) {
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

	public Color getNullColor() {
		return SwingRendererUtils.getNonEditableTextBackgroundColor();
	}

	public class FieldControlPlaceHolder extends JPanel implements IFieldControlInput {

		protected static final long serialVersionUID = 1L;
		protected Component fieldControl;
		protected JPanel form;
		protected IFieldInfo field;
		protected String errorMessageDisplayedOnPlaceHolder;
		protected IFieldControlData controlData;
		protected IFieldControlData lastInitialControlData;

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
		public String getContextIdentifier() {
			ITypeInfo objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			return "FieldContext [fieldName=" + field.getName() + ", containingType=" + objectType.getName() + "]";
		}

		@Override
		public ModificationStack getModificationStack() {
			return getModificationStackByForm().get(form);
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
					ReflectionUIUtils.setValueThroughModificationStack(data, newValue, getModificationStack(),
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
									t.printStackTrace();
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
					controlData = lastInitialControlData = getInitialControlData();
					fieldControl = createFieldControl();
				} catch (Throwable t) {
					fieldControl = createErrorControl(t);
				}
				add(fieldControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			} else {
				if (isFieldControlObsolete()) {
					refreshUI(true);
				} else {
					if (!(((fieldControl instanceof IAdvancedFieldControl)
							&& ((IAdvancedFieldControl) fieldControl).refreshUI()))) {
						remove(fieldControl);
						fieldControl = null;
						refreshUI(false);
					}
				}
			}
		}

		protected boolean isFieldControlObsolete() {
			IFieldControlData newInitialControlData;
			try {
				newInitialControlData = getInitialControlData();
			} catch (Throwable t) {
				return true;
			}
			if (!newInitialControlData.equals(lastInitialControlData)) {
				return true;
			}
			return false;
		}

		public IFieldControlData getInitialControlData() {
			Object object = getObject();
			IFieldInfo field = FieldControlPlaceHolder.this.field;
			Object[] valueOptions = field.getValueOptions(object);
			if (valueOptions != null) {
				field = new ValueOptionsAsEnumerationField(reflectionUI, object, field);
			}
			final ITypeInfoProxyFactory typeSpecificities = field.getTypeSpecificities();
			if (typeSpecificities != null) {
				field = new FieldInfoProxy(field) {
					@Override
					public ITypeInfo getType() {
						return typeSpecificities.get(super.getType());
					}
				};
			}
			final IFieldInfo finalField = field;
			IFieldControlData result = new InitialFieldControlData(finalField);
			result = indicateWhenBusy(result);
			result = handleValueAccessIssues(result);
			result = makeFieldModificationsUndoable(result);
			return result;
		}

		public Component createFieldControl() {
			if (!controlData.isFormControlMandatory()) {
				Component result = createCustomFieldControl(this);
				if (result != null) {
					return result;
				}
			}
			if (controlData.isValueNullable()) {
				return new NullableControl(SwingRenderer.this, this);
			}
			Object value = controlData.getValue();
			final ITypeInfo actualValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
			if (!controlData.getType().getName().equals(actualValueType.getName())) {
				controlData = new FieldControlDataProxy(controlData) {
					@Override
					public ITypeInfo getType() {
						return actualValueType;
					}
				};
				return createFieldControl();
			}
			if (controlData.isFormControlEmbedded()) {
				return new EmbeddedFormControl(SwingRenderer.this, this);
			} else {
				return new DialogAccessControl(SwingRenderer.this, this);
			}
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

		public boolean showsCaption() {
			if (((fieldControl instanceof IAdvancedFieldControl)
					&& ((IAdvancedFieldControl) fieldControl).showsCaption())) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return "FieldControlPlaceHolder [form=" + form + ", field=" + field + "]";
		}

		protected class InitialFieldControlData implements IFieldControlData {

			protected IFieldInfo finalField;

			public InitialFieldControlData(IFieldInfo finalField) {
				this.finalField = finalField;
			}

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
			public boolean isValueNullable() {
				return finalField.isValueNullable();
			}

			@Override
			public String getNullValueLabel() {
				return finalField.getNullValueLabel();
			}

			public boolean isFormControlMandatory() {
				return finalField.isFormControlMandatory();
			}

			public boolean isFormControlEmbedded() {
				return finalField.isFormControlEmbedded();
			}

			public IInfoFilter getFormControlFilter() {
				return finalField.getFormControlFilter();
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return finalField.getSpecificProperties();
			}

			private FieldControlPlaceHolder getOuterType() {
				return FieldControlPlaceHolder.this;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((finalField == null) ? 0 : finalField.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				InitialFieldControlData other = (InitialFieldControlData) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (finalField == null) {
					if (other.finalField != null)
						return false;
				} else if (!finalField.equals(other.finalField))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "InitialFieldControlData [of=" + getOuterType() + ", finalField=" + finalField + "]";
			}

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

		@Override
		public Dimension getPreferredSize() {
			Dimension result = super.getPreferredSize();
			if (result == null) {
				return super.getPreferredSize();
			}
			int maxMethodControlWidth = 0;
			for (final MethodControlPlaceHolder methodControlPlaceHolder : getMethodControlPlaceHolders(form)) {
				Component methodControl = methodControlPlaceHolder.getMethodControl();
				Dimension controlPreferredSize = methodControl.getPreferredSize();
				if (controlPreferredSize != null) {
					maxMethodControlWidth = Math.max(maxMethodControlWidth, controlPreferredSize.width);
				}
			}
			result.width = maxMethodControlWidth;
			return result;
		}

		public IMethodControlData makeMethodModificationsUndoable(final IMethodControlData data) {
			return new MethodControlDataProxy(data) {

				@Override
				public Object invoke(InvocationData invocationData) {
					return ReflectionUIUtils.invokeMethodThroughModificationStack(data, invocationData,
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
			return getModificationStackByForm().get(form);
		}

		@Override
		public String getContextIdentifier() {
			ITypeInfo objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
			return "MethodContext [methodSignature=" + ReflectionUIUtils.getMethodSignature(method)
					+ ", containingType=" + objectType.getName() + "]";
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
				remove(methodControl);
				methodControl = null;
				refreshUI(false);
			}
		}

		public IMethodControlData getInitialControlData() {
			IMethodInfo method = MethodControlPlaceHolder.this.method;
			final ITypeInfoProxyFactory typeSpecificities = method.getReturnValueTypeSpecificities();
			if (method.getReturnValueType() != null) {
				if (typeSpecificities != null) {
					method = new MethodInfoProxy(method) {
						@Override
						public ITypeInfo getReturnValueType() {
							return typeSpecificities.get(super.getReturnValueType());
						}
					};
				}
			}
			final IMethodInfo finalMethod = method;
			IMethodControlData result = new InitialMethodControlData(finalMethod);

			result = indicateWhenBusy(result);
			result = makeMethodModificationsUndoable(result);
			return result;
		}

		@Override
		public String toString() {
			return "MethodControlPlaceHolder [form=" + form + ", method=" + method + "]";
		}

		protected class InitialMethodControlData implements IMethodControlData {
			protected IMethodInfo finalMethod;

			public InitialMethodControlData(IMethodInfo finalMethod) {
				this.finalMethod = finalMethod;
			}

			@Override
			public boolean isReturnValueNullable() {
				return finalMethod.isReturnValueNullable();
			}

			@Override
			public boolean isReturnValueDetached() {
				return finalMethod.isReturnValueDetached();
			}

			@Override
			public void validateParameters(InvocationData invocationData) throws Exception {
				finalMethod.validateParameters(getObject(), invocationData);
			}

			@Override
			public boolean isReadOnly() {
				return finalMethod.isReadOnly();
			}

			@Override
			public Object invoke(InvocationData invocationData) {
				return finalMethod.invoke(getObject(), invocationData);
			}

			@Override
			public ValueReturnMode getValueReturnMode() {
				return finalMethod.getValueReturnMode();
			}

			@Override
			public Runnable getUndoJob(InvocationData invocationData) {
				return finalMethod.getUndoJob(getObject(), invocationData);
			}

			@Override
			public ITypeInfo getReturnValueType() {
				return finalMethod.getReturnValueType();
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return finalMethod.getParameters();
			}

			@Override
			public String getNullReturnValueLabel() {
				return finalMethod.getNullReturnValueLabel();
			}

			@Override
			public String getOnlineHelp() {
				return finalMethod.getOnlineHelp();
			}

			@Override
			public Map<String, Object> getSpecificProperties() {
				return finalMethod.getSpecificProperties();
			}

			@Override
			public String getCaption() {
				return finalMethod.getCaption();
			}

			@Override
			public String getMethodSignature() {
				return ReflectionUIUtils.getMethodSignature(finalMethod);
			}

			@Override
			public String getIconImagePath() {
				return finalMethod.getIconImagePath();
			}

			private Object getOuterType() {
				return MethodControlPlaceHolder.this;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((finalMethod == null) ? 0 : finalMethod.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				InitialMethodControlData other = (InitialMethodControlData) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (finalMethod == null) {
					if (other.finalMethod != null)
						return false;
				} else if (!finalMethod.equals(other.finalMethod))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "InitialControlData [of=" + MethodControlPlaceHolder.this + "]";
			}

		};

	}
}