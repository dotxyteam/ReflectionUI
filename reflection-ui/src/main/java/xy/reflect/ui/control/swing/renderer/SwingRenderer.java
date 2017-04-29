package xy.reflect.ui.control.swing.renderer;

import javax.swing.event.AncestorListener;
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
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;

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
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.CheckBoxControl;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.ModificationStackControls;
import xy.reflect.ui.control.swing.NullControl;
import xy.reflect.ui.control.swing.PolymorphicControl;
import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.plugin.ColorPickerPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.SliderPlugin;
import xy.reflect.ui.control.swing.plugin.SpinnerPlugin;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractMenuItem;
import xy.reflect.ui.info.menu.ActionMenuItem;
import xy.reflect.ui.info.menu.IMenuElementPosition;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.factory.FilterredTypeFactory;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
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
	protected Map<Window, JPanel> mainFormByWindow = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> fieldControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<MethodControlPlaceHolder>>> methodControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<String, InvocationData> lastInvocationDataByMethodSignature = new HashMap<String, InvocationData>();
	protected Map<FieldControlPlaceHolder, Component> captionControlByFieldControlPlaceHolder = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Container> categoriesControlByForm = new MapMaker().weakKeys().makeMap();
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

	public Map<JPanel, Container> getCategoriesControlByForm() {
		return categoriesControlByForm;
	}

	public Map<JPanel, ModificationStack> getModificationStackByForm() {
		return modificationStackByForm;
	}

	public Map<JPanel, Boolean> getFieldsUpdateListenerDisabledByForm() {
		return fieldsUpdateListenerDisabledByForm;
	}

	public Map<JPanel, Boolean> getRefreshRequestQueuedByForm() {
		return refreshRequestQueuedByForm;
	}

	public Map<JPanel, Boolean> getRefreshRequestExecutingByForm() {
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

	public Map<Window, JPanel> getMainFormByWindow() {
		return mainFormByWindow;
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
		SwingRendererUtils.setTitle(window, prepareStringToDisplay(title));
		if (iconImage == null) {
			window.setIconImage(SwingRendererUtils.NULL_IMAGE);
		} else {
			window.setIconImage(iconImage);
		}

		Container contentPane = createWindowContentPane(window, content, toolbarControls);
		SwingRendererUtils.setContentPane(window, contentPane);

		if (SwingRendererUtils.isForm(content, this)) {
			JPanel form = (JPanel) content;
			JMenuBar menuBar = createMenuBar(form);
			SwingRendererUtils.setMenuBar(window, menuBar);
		}

		SwingRendererUtils.adjustWindowInitialBounds(window);
	}

	public JMenuBar createMenuBar(JPanel form) {
		JMenuBar result = new JMenuBar();
		return result;
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
		return new FieldControlPlaceHolder(this, form, field);
	}

	public Container createFieldsPanel(List<FieldControlPlaceHolder> fielControlPlaceHolders) {
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new GridBagLayout());
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FieldControlPlaceHolder fieldControlPlaceHolder = fielControlPlaceHolders.get(i);
			{
				fieldsPanel.add(fieldControlPlaceHolder);
				updateFieldControlLayout(fieldControlPlaceHolder, i);
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

	public Container createFormCategoriesControl(SortedSet<InfoCategory> allCategories,
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory) {
		final JTabbedPane result = new JTabbedPane();
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
			result.addTab(prepareStringToDisplay(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutFormCategoryControls(fieldControlPlaceHolders, methodControlPlaceHolders, tabContent);
		}
		return result;
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
							refreshForm(form, false);
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
					return new PrimitiveValueControl(this, input);
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

	public Container createWindowContentPane(final Window window, Component content,
			List<? extends Component> toolbarControls) {
		final JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		if (content != null) {
			if (SwingRendererUtils.isForm(content, this)) {
				final JPanel form = (JPanel) content;
				getMainFormByWindow().put(window, form);
				contentPane.add(createStatusBar(form), BorderLayout.NORTH);
				setStatusBarError(form, null);
				window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowOpened(WindowEvent e) {
						updateFormBasedWindowMenuBar(window);
						validateFormInBackgroundAndReportOnStatusBar(form);
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
				finalizeFormUpdate(form);
			}
		});
	}

	public void finalizeFormUpdate(JPanel form) {
		Window window = SwingUtilities.getWindowAncestor(form);
		if (window != null) {
			updateFormBasedWindowMenuBar(window);
		}
		SwingRendererUtils.handleComponentSizeChange(form);
		validateFormInBackgroundAndReportOnStatusBar(form);
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
		JTabbedPane categoriesControl = (JTabbedPane) getCategoriesControlByForm().get(form);
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
		JTabbedPane categoriesControl = (JTabbedPane) getCategoriesControlByForm().get(form);
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
		ITypeInfo type = getFormFilteredType(form);
		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = createFieldControlPlaceHoldersByCategory(
				type.getFields(), form);
		Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = createMethodControlPlaceHoldersByCategory(
				type.getMethods(), form);
		layoutFormControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory, form);
	}

	public void layoutFormControls(Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
			Container container) {
		SortedSet<InfoCategory> allCategories = new TreeSet<InfoCategory>();
		allCategories.addAll(fieldControlPlaceHoldersByCategory.keySet());
		allCategories.addAll(methodControlPlaceHoldersByCategory.keySet());
		if ((allCategories.size() == 1) && (getNullInfoCategory().equals(allCategories.iterator().next()))) {
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
			layoutFormCategoryControls(fieldControlPlaceHolders, methodControlPlaceHolders, container);
		} else if (allCategories.size() > 0) {
			container.setLayout(new BorderLayout());
			Container categoriesControl = createFormCategoriesControl(allCategories, fieldControlPlaceHoldersByCategory,
					methodControlPlaceHoldersByCategory);
			JPanel form = SwingRendererUtils.getForm(fieldControlPlaceHoldersByCategory,
					methodControlPlaceHoldersByCategory);
			getCategoriesControlByForm().put(form, categoriesControl);
			container.add(categoriesControl, BorderLayout.CENTER);
		}
	}

	public Map<InfoCategory, List<MethodControlPlaceHolder>> createMethodControlPlaceHoldersByCategory(
			List<IMethodInfo> methods, JPanel form) {
		Map<InfoCategory, List<MethodControlPlaceHolder>> result = new HashMap<InfoCategory, List<MethodControlPlaceHolder>>();
		for (IMethodInfo method : methods) {
			MethodControlPlaceHolder methodControlPlaceHolder = createMethodControlPlaceHolder(form, method);
			{
				InfoCategory category = method.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<MethodControlPlaceHolder> methodControlPlaceHolders = result.get(category);
				if (methodControlPlaceHolders == null) {
					methodControlPlaceHolders = new ArrayList<MethodControlPlaceHolder>();
					result.put(category, methodControlPlaceHolders);
				}
				methodControlPlaceHolders.add(methodControlPlaceHolder);
			}
		}
		getMethodControlPlaceHoldersByCategoryByForm().put(form, result);
		return result;
	}

	public Map<InfoCategory, List<FieldControlPlaceHolder>> createFieldControlPlaceHoldersByCategory(
			List<IFieldInfo> fields, JPanel form) {
		Map<InfoCategory, List<FieldControlPlaceHolder>> result = new HashMap<InfoCategory, List<FieldControlPlaceHolder>>();
		for (IFieldInfo field : fields) {
			FieldControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(form, field);
			{
				InfoCategory category = field.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = result.get(category);
				if (fieldControlPlaceHolders == null) {
					fieldControlPlaceHolders = new ArrayList<FieldControlPlaceHolder>();
					result.put(category, fieldControlPlaceHolders);
				}
				fieldControlPlaceHolders.add(fieldControlPlaceHolder);
			}
		}
		getFieldControlPlaceHoldersByCategoryByForm().put(form, result);
		return result;
	}

	public ITypeInfo getFormFilteredType(JPanel form) {
		Object object = getObjectByForm().get(form);
		IInfoFilter infoFilter = getInfoFilterByForm().get(form);
		if (infoFilter == null) {
			infoFilter = IInfoFilter.DEFAULT;
		}
		final ITypeInfo rawType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		ITypeInfo result = new FilterredTypeFactory(infoFilter) {

			List<IFieldInfo> fields;
			List<IMethodInfo> methods;
			{
				try {
					fields = super.getFields(rawType);
					methods = super.getMethods(rawType);
				} catch (final Throwable t) {
					fields = Collections.<IFieldInfo>singletonList(new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
						@Override
						public Object getValue(Object object) {
							throw new ReflectionUIError(t);
						}

						@Override
						public String getCaption() {
							return "Error";
						}
					});
					methods = Collections.emptyList();
				}
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				return fields;
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				return methods;
			}

		}.get(rawType);
		return result;
	}

	public InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1);
	}

	public MethodControlPlaceHolder createMethodControlPlaceHolder(JPanel form, IMethodInfo method) {
		return new MethodControlPlaceHolder(this, form, method);
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

	public Image getMenuIconImage(AbstractMenuItem menuItem) {
		String imagePathSpecification = menuItem.getIconImagePath();
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

	public void layoutFormCategoryPanels(Container container, Container fieldsPanel, Container methodsPanel) {
		container.setLayout(new BorderLayout());
		if (fieldsPanel != null) {
			container.add(fieldsPanel, BorderLayout.CENTER);
		}
		if (methodsPanel != null) {
			container.add(methodsPanel, BorderLayout.SOUTH);
		}
	}

	public void layoutFormCategoryControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, Container container) {
		Container fieldsPanel = (fielControlPlaceHolders.size() == 0) ? null
				: createFieldsPanel(fielControlPlaceHolders);
		Container methodsPanel = (methodControlPlaceHolders.size() == 0) ? null
				: createMethodsPanel(methodControlPlaceHolders);
		layoutFormCategoryPanels(container, fieldsPanel, methodsPanel);
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

	public void refreshForm(final JPanel form, final boolean recreate) {
		preservingFormFocusAsMuchAsPossible(form, new Runnable() {
			@Override
			public void run() {
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = getFieldControlPlaceHolders(form);
				for (int i = 0; i < fieldControlPlaceHolders.size(); i++) {
					FieldControlPlaceHolder fieldControlPlaceHolder = fieldControlPlaceHolders.get(i);
					fieldControlPlaceHolder.refreshUI(recreate);
					updateFieldControlLayout(fieldControlPlaceHolder, i);
				}
				finalizeFormUpdate(form);
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

	public void updateFieldControlLayout(FieldControlPlaceHolder fieldControlPlaceHolder, int fieldIndex) {

		boolean shouldHaveSeparateCaptionControl = !fieldControlPlaceHolder.showsCaption()
				&& (fieldControlPlaceHolder.getField().getCaption().length() > 0);

		Container container = fieldControlPlaceHolder.getParent();
		GridBagLayout layout = (GridBagLayout) container.getLayout();
		boolean initialUpdate = (layout.getConstraints(fieldControlPlaceHolder).gridx == -1);

		if (!initialUpdate) {
			boolean hasSeparateCaptionControl = captionControlByFieldControlPlaceHolder
					.containsKey(fieldControlPlaceHolder);
			if (hasSeparateCaptionControl == shouldHaveSeparateCaptionControl) {
				return;
			}
		}

		Component captionControl = captionControlByFieldControlPlaceHolder.get(fieldControlPlaceHolder);
		if (captionControl != null) {
			container.remove(captionControl);
			captionControlByFieldControlPlaceHolder.remove(fieldControlPlaceHolder);
		}

		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(fieldControlPlaceHolder.getObject()));
		ITypeInfo.FieldsLayout fieldsOrientation = type.getFieldsLayout();

		int spacing = 5;
		if (shouldHaveSeparateCaptionControl) {
			captionControl = createSeparateCaptionControl(fieldControlPlaceHolder.getField().getCaption());
			GridBagConstraints captionControlLayoutConstraints = new GridBagConstraints();
			captionControlLayoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
				captionControlLayoutConstraints.gridx = 0;
				captionControlLayoutConstraints.gridy = fieldIndex;
			} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
				captionControlLayoutConstraints.gridy = 0;
				captionControlLayoutConstraints.gridx = fieldIndex;
			} else {
				throw new ReflectionUIError();
			}
			captionControlLayoutConstraints.weighty = 1.0;
			captionControlLayoutConstraints.anchor = GridBagConstraints.WEST;
			container.add(captionControl, captionControlLayoutConstraints);
			captionControlByFieldControlPlaceHolder.put(fieldControlPlaceHolder, captionControl);
		}
		{
			GridBagConstraints fieldControlPlaceHolderLayoutConstraints = new GridBagConstraints();
			fieldControlPlaceHolderLayoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
				if (!shouldHaveSeparateCaptionControl) {
					fieldControlPlaceHolderLayoutConstraints.gridwidth = 2;
					fieldControlPlaceHolderLayoutConstraints.gridx = 0;
				} else {
					fieldControlPlaceHolderLayoutConstraints.gridx = 1;
				}
				fieldControlPlaceHolderLayoutConstraints.gridy = fieldIndex;
			} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
				if (!shouldHaveSeparateCaptionControl) {
					fieldControlPlaceHolderLayoutConstraints.gridheight = 2;
					fieldControlPlaceHolderLayoutConstraints.gridy = 0;
				} else {
					fieldControlPlaceHolderLayoutConstraints.gridy = 1;
				}
				fieldControlPlaceHolderLayoutConstraints.gridx = fieldIndex;
			} else {
				throw new ReflectionUIError();
			}
			fieldControlPlaceHolderLayoutConstraints.weightx = 1.0;
			fieldControlPlaceHolderLayoutConstraints.weighty = 1.0;
			fieldControlPlaceHolderLayoutConstraints.fill = GridBagConstraints.BOTH;
			container.remove(fieldControlPlaceHolder);
			container.add(fieldControlPlaceHolder, fieldControlPlaceHolderLayoutConstraints);
		}

		container.validate();
	}

	public Component createSeparateCaptionControl(String caption) {
		return new JLabel(prepareStringToDisplay(caption + ": "));
	}

	public void updateFormBasedWindowMenuBar(final Window window) {
		JPanel form = getMainFormByWindow().get(window);
		if (form == null) {
			return;
		}
		JMenuBar menuBar = SwingRendererUtils.getMenuBar(window);
		if (menuBar == null) {
			return;
		}
		MenuModel menuModel = new MenuModel();
		addFormMenuContribution(form, menuModel);
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			Component fieldControl = fieldControlPlaceHolder.getFieldControl();
			if (fieldControl instanceof IAdvancedFieldControl) {
				((IAdvancedFieldControl) fieldControl).addMenuContribution(menuModel);
			}
		}
		SwingRendererUtils.updateMenubar(menuBar, menuModel, this);
	}

	public void addFormMenuContribution(final JPanel form, MenuModel menuModel) {
		ITypeInfo type = getFormFilteredType(form);
		menuModel.merge(type.getMenuModel());
		for (final IMethodInfo method : type.getMethods()) {
			IMenuElementPosition menuItemPosition = method.getMenuItemPosition();
			if (menuItemPosition != null) {
				ActionMenuItem menuAction = new ActionMenuItem(menuItemPosition.getElementName(),
						method.getIconImagePath(), new Runnable() {
							@Override
							public void run() {
								IMethodControlInput methodActionInput = createMethodControlPlaceHolder(form, method);
								MethodAction methodAction = createMethodAction(methodActionInput);
								methodAction.setShouldDisplayReturnValueIfAny(true);
								methodAction.execute(form);
							}
						});
				menuModel.contribute(menuItemPosition, menuAction);
			}
		}
	}

	public void validateForm(JPanel form) throws Exception {
		Object object = getObjectByForm().get(form);
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		type.validate(object);
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getFieldControlPlaceHolders(form)) {
			Component fieldControl = fieldControlPlaceHolder.getFieldControl();
			if (fieldControl instanceof IAdvancedFieldControl) {
				try {
					((IAdvancedFieldControl) fieldControl).validateSubForm();
				} catch (Exception e) {
					String errorMsg = e.toString();
					IFieldInfo field = fieldControlPlaceHolder.getField();
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

	public void validateFormInBackgroundAndReportOnStatusBar(final JPanel form) {
		new Thread("Validator: " + getObjectByForm().get(form)) {
			@Override
			public void run() {
				try {
					validateForm(form);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setStatusBarError(form, null);
						}
					});
				} catch (Exception e) {
					final String errorMsg = new ReflectionUIError(e).toString();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setStatusBarError(form, errorMsg);
						}
					});
				}

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
}