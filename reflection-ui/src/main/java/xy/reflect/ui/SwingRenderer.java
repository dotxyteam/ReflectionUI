package xy.reflect.ui;

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
import javax.swing.JCheckBox;
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

import xy.reflect.ui.control.swing.CheckBoxControl;
import xy.reflect.ui.control.swing.ColorControl;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.EnumerationControl;
import xy.reflect.ui.control.swing.FileControl;
import xy.reflect.ui.control.swing.IFieldControl;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.MethodControl;
import xy.reflect.ui.control.swing.NullControl;
import xy.reflect.ui.control.swing.PolymorphicEmbeddedForm;
import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationTypeInfo;
import xy.reflect.ui.info.type.util.MethodParametersAsTypeInfo;
import xy.reflect.ui.info.type.util.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.util.ValueFromVirtualFieldTypeInfo;
import xy.reflect.ui.undo.CompositeModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SetFieldValueModification;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.PrimitiveUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AutoResizeTabbedPane;
import xy.reflect.ui.util.component.ScrollPaneOptions;
import xy.reflect.ui.util.component.WrapLayout;

import com.google.common.collect.MapMaker;

public class SwingRenderer {

	protected ReflectionUI reflectionUI;
	protected Map<JPanel, Object> objectByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, ModificationStack> modificationStackByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, IInfoCollectionSettings> infoCollectionSettingsByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, JLabel> statusLabelByForm = new MapMaker().weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> fieldControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<Component>>> methodControlsByCategoryByForm = new MapMaker().weakKeys()
			.makeMap();
	protected Map<Component, IMethodInfo> methodByControl = new MapMaker().weakKeys().makeMap();
	protected Map<IMethodInfo, InvocationData> lastInvocationDataByMethod = new HashMap<IMethodInfo, InvocationData>();

	public SwingRenderer(ReflectionUI reflectionUI) {
		this.reflectionUI = reflectionUI;
	}

	public Map<JPanel, Object> getObjectByForm() {
		return objectByForm;
	}

	public Map<JPanel, ModificationStack> getModificationStackByForm() {
		return modificationStackByForm;
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

	public Map<JPanel, Map<InfoCategory, List<Component>>> getMethodControlsByCategoryByForm() {
		return methodControlsByCategoryByForm;
	}

	public Map<Component, IMethodInfo> getMethodByControl() {
		return methodByControl;
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

	public void applyCommonWindowConfiguration(Window window, Component content,
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

	public List<Component> createCommonToolbarControls(final JPanel form, IInfoCollectionSettings settings) {
		List<Component> result = new ArrayList<Component>();
		Object object = getObjectByForm().get(form);
		if (object != null) {
			ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
			if ((type.getOnlineHelp() != null) && (type.getOnlineHelp().trim().length() > 0)) {
				result.add(createOnlineHelpControl(type.getOnlineHelp()));
			}
		}
		if (!settings.allReadOnly()) {
			final ModificationStack stack = getModificationStackByForm().get(form);
			if (stack == null) {
				return null;
			}
			result.addAll(stack.createControls(reflectionUI));
		}
		return result;
	}

	public JDialog createDialog(Component ownerComponent, Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls, final Runnable whenClosing) {
		Window owner = SwingRendererUtils.getWindowAncestorOrSelf(ownerComponent);
		JDialog dialog = new JDialog(owner, reflectionUI.prepareUIString(title)) {
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

	public List<JButton> createDialogOkCancelButtons(final JDialog[] dialogArray, final boolean[] okPressedArray,
			String okCaption, final Runnable okAction, boolean createCancelButton, String cancelCaption) {
		List<JButton> result = new ArrayList<JButton>();

		final JButton okButton = new JButton(reflectionUI.prepareUIString((okCaption != null) ? okCaption : "OK"));
		result.add(okButton);
		if (okPressedArray != null) {
			okPressedArray[0] = false;
		}
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (okAction != null) {
						okAction.run();
					}
					if (okPressedArray != null) {
						okPressedArray[0] = true;
					}
				} catch (Throwable t) {
					handleExceptionsFromDisplayedUI(okButton, t);
				} finally {
					dialogArray[0].dispose();
				}
			}
		});

		if (createCancelButton) {
			final JButton cancelButton = new JButton(
					reflectionUI.prepareUIString((cancelCaption != null) ? cancelCaption : "Cancel"));
			result.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (okPressedArray != null) {
						okPressedArray[0] = false;
					}
					dialogArray[0].dispose();
				}
			});
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
		applyCommonWindowConfiguration(frame, content, toolbarControls, title, iconImage);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	public Component createMethodControl(final Object object, final IMethodInfo method) {
		return new MethodControl(reflectionUI, object, method);
	}

	public JPanel createMethodsPanel(final List<Component> methodControls) {
		JPanel methodsPanel = new JPanel();
		methodsPanel.setLayout(new WrapLayout(WrapLayout.CENTER));
		for (final Component methodControl : methodControls) {
			JPanel methodControlContainer = new JPanel() {
				protected static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension result = super.getPreferredSize();
					if (result == null) {
						return super.getPreferredSize();
					}
					int maxMethodControlWidth = 0;
					for (final Component methodControl : methodControls) {
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

	public Component createMultipleInfoCategoriesComponent(final SortedSet<InfoCategory> allCategories,
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<Component>> methodControlsByCategory) {
		final JTabbedPane tabbedPane = new AutoResizeTabbedPane();
		for (final InfoCategory category : allCategories) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory.get(category);
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<Component> methodControls = methodControlsByCategory.get(category);
			if (methodControls == null) {
				methodControls = Collections.emptyList();
			}

			JPanel tab = new JPanel();
			tabbedPane.addTab(reflectionUI.prepareUIString(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutControls(fieldControlPlaceHolders, methodControls, tabContent);

			JPanel buttonsPanel = new JPanel();
			tab.add(buttonsPanel, BorderLayout.SOUTH);
			buttonsPanel.setLayout(new BorderLayout());
			buttonsPanel.setBorder(BorderFactory.createTitledBorder(""));

			ArrayList<InfoCategory> allCategoriesAsList = new ArrayList<InfoCategory>(allCategories);
			final int tabIndex = allCategoriesAsList.indexOf(category);
			int tabCount = allCategoriesAsList.size();

			if (tabIndex > 0) {
				JButton previousCategoryButton = new JButton(reflectionUI.prepareUIString("<"));
				buttonsPanel.add(previousCategoryButton, BorderLayout.WEST);
				previousCategoryButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tabbedPane.setSelectedIndex(tabIndex - 1);
					}
				});
			}

			if (tabIndex < (tabCount - 1)) {
				JButton nextCategoryButton = new JButton(reflectionUI.prepareUIString(">"));
				buttonsPanel.add(nextCategoryButton, BorderLayout.EAST);
				nextCategoryButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tabbedPane.setSelectedIndex(tabIndex + 1);
					}
				});
			}
		}
		return tabbedPane;
	}

	public JPanel createObjectForm(Object object) {
		return createObjectForm(object, IInfoCollectionSettings.DEFAULT);
	}

	public JPanel createObjectForm(Object object, IInfoCollectionSettings settings) {
		final ModificationStack modifStack = new ModificationStack(reflectionUI.getObjectKind(object));
		JPanel result = new JPanel() {

			private static final long serialVersionUID = 1L;
			JPanel form = this;
			IModificationListener modifListener = new IModificationListener() {
				@Override
				public void handleEvent(Object event) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							refreshAllFieldControls(form);
							validateForm(form);
						}
					});
				}
			};

			@Override
			public void addNotify() {
				super.addNotify();
				modifStack.addListener(modifListener);
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				modifStack.removeListener(modifListener);
			}

		};
		getObjectByForm().put(result, object);
		getModificationStackByForm().put(result, modifStack);
		getInfoCollectionSettingsByForm().put(result, settings);
		fillForm(object, result, settings);
		return result;
	}

	public Component createOnlineHelpControl(String onlineHelp) {
		final JButton result = new JButton(SwingRendererUtils.HELP_ICON);
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result, reflectionUI.prepareUIString(onlineHelp));
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
		getStatusLabelByForm().put(form, result);
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
			JPanel form = SwingRendererUtils.findForm(content, reflectionUI);
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

	public void fillForm(Object object, JPanel form, IInfoCollectionSettings settings) {
		form.setLayout(new BorderLayout());

		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));

		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FieldControlPlaceHolder>>();
		getFieldControlPlaceHoldersByCategoryByForm().put(form, fieldControlPlaceHoldersByCategory);
		List<IFieldInfo> fields = type.getFields();
		for (IFieldInfo field : fields) {
			if (settings.excludeField(field)) {
				continue;
			}
			if (settings.allReadOnly()) {
				field = new FieldInfoProxy(field) {
					@Override
					public boolean isReadOnly() {
						return true;
					}
				};
			}
			if (!field.isReadOnly()) {
				field = makeFieldModificationsUndoable(field, form);
			}
			FieldControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(object, field);
			{
				InfoCategory category = field.getCategory();
				if (category == null) {
					category = reflectionUI.getNullInfoCategory();
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

		Map<InfoCategory, List<Component>> methodControlsByCategory = new HashMap<InfoCategory, List<Component>>();
		getMethodControlsByCategoryByForm().put(form, methodControlsByCategory);
		List<IMethodInfo> methods = type.getMethods();
		for (IMethodInfo method : methods) {
			if (settings.excludeMethod(method)) {
				continue;
			}
			if (!settings.allReadOnly()) {
				if (!method.isReadOnly()) {
					method = makeMethodModificationsUndoable(method, form);
				}
			}
			Component methodControl = createMethodControl(object, method);
			getMethodByControl().put(methodControl, method);
			if (settings.allReadOnly()) {
				if (!method.isReadOnly()) {
					if (methodControl instanceof JComponent) {
						SwingRendererUtils.disableComponentTree((JComponent) methodControl, false);
					} else {
						continue;
					}
				}
			}
			{
				InfoCategory category = method.getCategory();
				if (category == null) {
					category = reflectionUI.getNullInfoCategory();
				}
				List<Component> methodControls = methodControlsByCategory.get(category);
				if (methodControls == null) {
					methodControls = new ArrayList<Component>();
					methodControlsByCategory.put(category, methodControls);
				}
				methodControls.add(methodControl);
			}
		}

		JPanel formContent = new JPanel();

		SortedSet<InfoCategory> allCategories = new TreeSet<InfoCategory>();
		allCategories.addAll(fieldControlPlaceHoldersByCategory.keySet());
		allCategories.addAll(methodControlsByCategory.keySet());
		if (allCategories.size() == 1) {
			form.add(formContent, BorderLayout.CENTER);
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
					.get(allCategories.first());
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<Component> methodControls = methodControlsByCategory.get(allCategories.first());
			if (methodControls == null) {
				methodControls = Collections.emptyList();
			}
			layoutControls(fieldControlPlaceHolders, methodControls, formContent);
		} else if (allCategories.size() > 1) {
			form.add(formContent, BorderLayout.CENTER);
			formContent.setLayout(new BorderLayout());
			formContent.add(createMultipleInfoCategoriesComponent(allCategories, fieldControlPlaceHoldersByCategory,
					methodControlsByCategory), BorderLayout.CENTER);
		}
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
		Map<InfoCategory, List<Component>> methodControlByCategory = getMethodControlsByCategoryByForm().get(form);
		for (List<Component> methodControls : methodControlByCategory.values()) {
			for (Component methodControl : methodControls) {
				IMethodInfo method = getMethodByControl().get(methodControl);
				if (ReflectionUIUtils.getMethodInfoSignature(method).equals(methodSignature)) {
					result.add(methodControl);
				}
			}
		}
		return result;
	}

	public Color getNullColor() {
		return new JTextArea().getDisabledTextColor();
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

	public IFieldInfo handleValueChangeErrors(IFieldInfo field, final FieldControlPlaceHolder fieldControlPlaceHolder) {
		return new FieldInfoProxy(field) {

			@Override
			public void setValue(Object object, Object value) {
				try {
					if (!reflectionUI.equals(super.getValue(object), value)) {
						super.setValue(object, value);
					}
					fieldControlPlaceHolder.displayError(null);
				} catch (final Throwable t) {
					fieldControlPlaceHolder.displayError(new ReflectionUIError(t));
				}
			}

		};
	}

	public void layoutControlPanels(JPanel parentForm, JPanel fieldsPanel, JPanel methodsPanel) {
		parentForm.setLayout(new BorderLayout());
		parentForm.add(fieldsPanel, BorderLayout.CENTER);
		parentForm.add(methodsPanel, BorderLayout.SOUTH);
	}

	public void layoutControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<Component> methodControls, JPanel parentForm) {
		JPanel fieldsPanel = createFieldsPanel(fielControlPlaceHolders);
		JPanel methodsPanel = createMethodsPanel(methodControls);
		layoutControlPanels(parentForm, fieldsPanel, methodsPanel);
	}

	public IFieldInfo makeFieldModificationsUndoable(final IFieldInfo field, final JPanel form) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(Object object, Object newValue) {
				ModificationStack stack = getModificationStackByForm().get(form);
				stack.apply(new SetFieldValueModification(reflectionUI, object, field, newValue), false);
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
			final IMethodInfo method, final Object[] returnValueArray) {
		if (method.getParameters().size() > 0) {
			return openMethoExecutionSettingDialog(activatorComponent, object, method, returnValueArray);
		} else {
			final boolean shouldDisplayReturnValue = (returnValueArray == null)
					&& (method.getReturnValueType() != null);
			final Object[] returnValueToDisplay = new Object[1];
			final boolean[] exceptionThrownArray = new boolean[] { false };
			showBusyDialogWhile(activatorComponent, new Runnable() {
				@Override
				public void run() {
					try {
						Object result = method.invoke(object, new InvocationData());
						if (returnValueArray != null) {
							returnValueArray[0] = result;
						}
						if (shouldDisplayReturnValue) {
							returnValueToDisplay[0] = result;
						}
					} catch (Throwable t) {
						exceptionThrownArray[0] = true;
						throw new ReflectionUIError(t);
					}
				}
			}, reflectionUI.getMethodTitle(object, method, null, "Execution"));
			if (shouldDisplayReturnValue && !exceptionThrownArray[0]) {
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
						type = openSelectionDialog(activatorComponent, polyTypes, null,
								MessageFormat.format("Choose the type of ''{0}''", type.getCaption()), null);
						if (type == null) {
							return null;
						}
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
						type = null;
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
					Object[] returnValueArray = new Object[1];
					onMethodInvocationRequest(activatorComponent, null, constructor, returnValueArray);
					return returnValueArray[0];
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
				IMethodInfo chosenContructor = openSelectionDialog(activatorComponent, constructors, null,
						reflectionUI.prepareUIString("Choose an option:"), null);
				if (chosenContructor == null) {
					return null;
				}
				Object[] returnValueArray = new Object[1];
				onMethodInvocationRequest(activatorComponent, null, chosenContructor, returnValueArray);
				return returnValueArray[0];
			}
		} catch (Throwable t) {
			throw new ReflectionUIError("Could not create an instance of type '" + type + "': " + t.toString(), t);
		}

	}

	public void openErrorDialog(Component activatorComponent, String title, final Throwable error) {
		Component errorComponent = new JOptionPane(
				createObjectForm(ValueFromVirtualFieldTypeInfo.wrap(reflectionUI,
						new Object[] { ReflectionUIUtils.getPrettyMessage(error) }, "Message", "", true)),
				JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {});

		JDialog[] dialogArray = new JDialog[1];

		List<Component> buttons = new ArrayList<Component>();
		final JButton deatilsButton = new JButton(reflectionUI.prepareUIString("Details"));
		deatilsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openErrorDetailsDialog(deatilsButton, error);
			}
		});
		buttons.add(deatilsButton);
		buttons.addAll(createDialogOkCancelButtons(dialogArray, null, "Close", null, false, null));

		dialogArray[0] = createDialog(activatorComponent, errorComponent, title, null, buttons, null);
		showDialog(dialogArray[0], true);

	}

	public void openErrorDetailsDialog(Component activatorComponent, Throwable error) {
		openObjectDialog(activatorComponent, error, "Error Details", reflectionUI.getIconImage(error), true, null, null,
				null, null, IInfoCollectionSettings.READ_ONLY);
	}

	public void openMethodReturnValueWindow(Component activatorComponent, Object object, IMethodInfo method,
			Object returnValue) {
		if (returnValue == null) {
			String msg = "No data returned!";
			openMessageDialog(activatorComponent, msg, reflectionUI.getMethodTitle(object, method, null, "Result"));
		} else {
			openValueFrame(returnValue, reflectionUI.getMethodTitle(object, method, returnValue, "Execution Result"));
		}
	}

	public boolean openMethoExecutionSettingDialog(final Component activatorComponent, final Object object,
			final IMethodInfo method, final Object[] returnValueArray) {
		final boolean shouldDisplayReturnValue = (returnValueArray == null) && (method.getReturnValueType() != null);
		final boolean[] exceptionThrownArray = new boolean[] { false };
		final Object[] returnValueToDisplay = new Object[1];
		final InvocationData invocationData;		
		if(lastInvocationDataByMethod.containsKey(method)){
			invocationData = lastInvocationDataByMethod.get(method);
		}
		else{
			invocationData = new InvocationData();
		}
		JPanel methodForm = createObjectForm(new MethodParametersAsTypeInfo(reflectionUI, method)
				.getPrecomputedTypeInfoInstanceWrapper(object, invocationData));
		final boolean[] invokedStatusArray = new boolean[] { false };
		final JDialog[] methodDialogArray = new JDialog[1];
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
								if (returnValueArray != null) {
									returnValueArray[0] = result;
								}
								if (shouldDisplayReturnValue) {
									returnValueToDisplay[0] = result;
								}
							} catch (Throwable t) {
								exceptionThrownArray[0] = true;
								throw new ReflectionUIError(t);
							}
						}
					}, reflectionUI.getMethodTitle(object, method, null, "Execution"));
					if (shouldDisplayReturnValue) {
						if (!exceptionThrownArray[0]) {
							openMethodReturnValueWindow(activatorComponent, object, method, returnValueToDisplay[0]);
						}
					} else {
						methodDialogArray[0].dispose();
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
					methodDialogArray[0].dispose();
				}
			});
			toolbarControls.add(closeButton);
		}
		methodDialogArray[0] = createDialog(activatorComponent, methodForm,
				reflectionUI.getMethodTitle(object, method, null, "Setting"), null, toolbarControls, null);
		showDialog(methodDialogArray[0], true);
		if (shouldDisplayReturnValue) {
			return true;
		} else {
			return invokedStatusArray[0];
		}
	}

	public void openObjectDialog(Component parent, Object object, boolean modal) {
		openObjectDialog(parent, object, reflectionUI.getObjectKind(object), reflectionUI.getIconImage(object), modal);
	}

	public void openObjectDialog(Component parent, Object object, String title, Image iconImage, boolean modal) {
		openObjectDialog(parent, object, title, iconImage, modal, null, null, null, null,
				IInfoCollectionSettings.DEFAULT);
	}

	public void openObjectDialog(Component parent, Object object, String title, Image iconImage, boolean modal,
			List<Component> additionalToolbarControls, boolean[] okPressedArray, Runnable whenClosingDialog,
			ModificationStack[] modificationstackArray, IInfoCollectionSettings settings) {
		JPanel form = createObjectForm(object, settings);
		if (modificationstackArray == null) {
			modificationstackArray = new ModificationStack[1];
		}
		modificationstackArray[0] = getModificationStackByForm().get(form);
		List<Component> toolbarControls = new ArrayList<Component>();
		if (!settings.allReadOnly()) {
			List<Component> commonToolbarControls = createCommonToolbarControls(form, settings);
			if (commonToolbarControls != null) {
				toolbarControls.addAll(commonToolbarControls);
			}
		}
		if (additionalToolbarControls != null) {
			toolbarControls.addAll(additionalToolbarControls);
		}
		JDialog[] dialogArray = new JDialog[1];
		if (settings.allReadOnly()) {
			toolbarControls.addAll(createDialogOkCancelButtons(dialogArray, null, "Close", null, false, null));
		} else {
			toolbarControls.addAll(createDialogOkCancelButtons(dialogArray, okPressedArray, "OK", null, true, null));
		}
		dialogArray[0] = createDialog(parent, form, title, iconImage, toolbarControls, whenClosingDialog);
		showDialog(dialogArray[0], modal);
	}

	public void openObjectFrame(Object object) {
		openObjectFrame(object, reflectionUI.getObjectKind(object), reflectionUI.getIconImage(object));
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		JPanel form = createObjectForm(object);
		JFrame frame = createFrame(form, title, iconImage,
				createCommonToolbarControls(form, IInfoCollectionSettings.DEFAULT));
		frame.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent, List<T> choices, T initialSelection, String message,
			String title) {
		if (choices.size() == 0) {
			throw new ReflectionUIError();
		}
		if (initialSelection == null) {
			initialSelection = choices.get(0);
		}
		final Object[] chosenItemArray = new Object[] { initialSelection };
		ITypeInfo enumType = new ArrayAsEnumerationTypeInfo(reflectionUI, choices.toArray(), "");
		chosenItemArray[0] = new PrecomputedTypeInfoInstanceWrapper(chosenItemArray[0], enumType);
		final Object chosenItemAsField = ValueFromVirtualFieldTypeInfo.wrap(reflectionUI, chosenItemArray, message,
				"Selection", false);
		if (openValueDialog(parentComponent, Accessor.returning(chosenItemAsField), IInfoCollectionSettings.DEFAULT,
				null, title, new boolean[1])) {
			chosenItemArray[0] = ((PrecomputedTypeInfoInstanceWrapper) chosenItemArray[0]).getInstance();
			return (T) chosenItemArray[0];
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T openInputDialog(Component parentComponent, T initialValue, String dataName, String title) {
		if (initialValue == null) {
			throw new ReflectionUIError();
		}
		final Object[] valueArray = new Object[] { initialValue };
		final Object valueAsField = ValueFromVirtualFieldTypeInfo.wrap(reflectionUI, valueArray, dataName, "Selection",
				false);
		if (openValueDialog(parentComponent, Accessor.returning(valueAsField), IInfoCollectionSettings.DEFAULT, null,
				title, new boolean[1])) {
			return (T) valueArray[0];
		} else {
			return null;
		}
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title) {
		return openQuestionDialog(activatorComponent, question, title, "Yes", "No");
	}

	public boolean openQuestionDialog(Component activatorComponent, String question, String title, String yesCaption,
			String noCaption) {
		JDialog[] dialogArray = new JDialog[1];
		boolean[] okPressedArray = new boolean[] { false };
		showDialog(dialogArray[0] = createDialog(activatorComponent,
				new JLabel("<HTML><BR>" + question + "<BR><BR><HTML>", SwingConstants.CENTER), title, null,
				createDialogOkCancelButtons(dialogArray, okPressedArray, yesCaption, null, true, noCaption), null),
				true);
		return okPressedArray[0];
	}

	public boolean openValueDialog(Component activatorComponent, final Accessor<Object> valueAccessor,
			final IInfoCollectionSettings settings, final ModificationStack parentModificationStack, final String title,
			final boolean[] changeDetectedArray) {
		final Object[] valueArray = new Object[] { valueAccessor.get() };
		final Object toOpen;
		String fieldName = BooleanTypeInfo.isCompatibleWith(valueArray[0].getClass()) ? "Is True" : "Value";
		if (hasCustomFieldControl(valueArray[0])) {
			toOpen = ValueFromVirtualFieldTypeInfo.wrap(reflectionUI, valueArray, fieldName, title,
					settings.allReadOnly());
		} else {
			toOpen = valueArray[0];
		}

		final boolean[] okPressedArray = new boolean[] { false };
		final ModificationStack[] modificationstackArray = new ModificationStack[1];

		Runnable whenClosingDialog = new Runnable() {
			@Override
			public void run() {
				if (okPressedArray[0]) {
					if ((modificationstackArray[0] != null) && (modificationstackArray[0].getNumberOfUndoUnits() > 0)) {
						changeDetectedArray[0] = true;
						if (parentModificationStack != null) {
							Object oldValue = valueAccessor.get();
							if (reflectionUI.equals(oldValue, valueArray[0])) {
								parentModificationStack.pushUndo(new CompositeModification(
										ModificationStack.getUndoTitle("Edit " + title), UndoOrder.LIFO,
										modificationstackArray[0].getUndoModifications(UndoOrder.LIFO)));
							} else {
								parentModificationStack.beginComposite();
								valueAccessor.set(valueArray[0]);
								parentModificationStack.pushUndo(new CompositeModification(null, UndoOrder.LIFO,
										modificationstackArray[0].getUndoModifications(UndoOrder.LIFO)));
								parentModificationStack.endComposite(title, UndoOrder.FIFO);
							}
						}
					} else {
						Object oldValue = valueAccessor.get();
						if (!reflectionUI.equals(oldValue, valueArray[0])) {
							changeDetectedArray[0] = true;
							valueAccessor.set(valueArray[0]);
						}
					}
				} else {
					if (modificationstackArray[0] != null) {
						modificationstackArray[0].undoAll(false);
					}
				}
			}
		};

		openObjectDialog(activatorComponent, toOpen, title, reflectionUI.getIconImage(valueArray[0]), true, null,
				okPressedArray, whenClosingDialog, modificationstackArray, settings);

		return okPressedArray[0];

	}

	public void openValueFrame(Object value, final String title) {
		final Object[] valueArray = new Object[] { value };
		final Object toOpen;
		String fieldName = BooleanTypeInfo.isCompatibleWith(valueArray[0].getClass()) ? "Is True" : "Value";
		if (hasCustomFieldControl(value)) {
			toOpen = ValueFromVirtualFieldTypeInfo.wrap(reflectionUI, valueArray, fieldName, title, true);
		} else {
			toOpen = value;
		}
		openObjectFrame(toOpen, title, reflectionUI.getIconImage(value));
	}

	public void refreshAllFieldControls(JPanel form) {
		int focusedFieldControlPaceHolderIndex = getFocusedFieldControlPaceHolderIndex(form);
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getAllFieldControlPlaceHolders(form)) {
			fieldControlPlaceHolder.refreshUI();
			updateFieldControlLayout(fieldControlPlaceHolder);
		}
		if (focusedFieldControlPaceHolderIndex != -1) {
			final FieldControlPlaceHolder toFocus = getAllFieldControlPlaceHolders(form)
					.get(focusedFieldControlPaceHolderIndex);
			toFocus.requestFocus();
		}
	}

	public void refreshFieldControlsByName(JPanel form, String fieldName) {
		for (FieldControlPlaceHolder fieldControlPlaceHolder : getAllFieldControlPlaceHolders(form)) {
			if (fieldName.equals(fieldControlPlaceHolder.getField().getName())) {
				fieldControlPlaceHolder.refreshUI();
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

	public void openMessageDialog(Component activatorComponent, String msg, String title) {
		JDialog[] dialogArray = new JDialog[1];
		showDialog(dialogArray[0] = createDialog(activatorComponent,
				new JLabel("<HTML><BR>" + msg + "<BR><BR><HTML>", SwingConstants.CENTER), title, null,
				createDialogOkCancelButtons(dialogArray, null, null, null, false, null), null), true);
	}

	public void updateFieldControlLayout(FieldControlPlaceHolder fieldControlPlaceHolder) {
		Component fieldControl = fieldControlPlaceHolder.getFieldControl();
		IFieldInfo field = fieldControlPlaceHolder.getField();
		Container container = fieldControlPlaceHolder.getParent();

		GridBagLayout layout = (GridBagLayout) container.getLayout();
		int i = layout.getConstraints(fieldControlPlaceHolder).gridy;

		container.remove(fieldControlPlaceHolder);
		if (fieldControlPlaceHolder.getCaptionControl() != null) {
			container.remove(fieldControlPlaceHolder.getCaptionControl());
			fieldControlPlaceHolder.setCaptionControl(null);
		}

		boolean fieldControlHasCaption = (fieldControl instanceof IFieldControl)
				&& ((IFieldControl) fieldControl).showCaption();
		int spacing = 5;
		if (!fieldControlHasCaption) {
			JLabel captionControl = new JLabel(reflectionUI.prepareUIString(field.getCaption() + ": "));
			GridBagConstraints layoutConstraints = new GridBagConstraints();
			layoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			layoutConstraints.gridx = 0;
			layoutConstraints.gridy = i;
			layoutConstraints.weighty = 1.0;
			layoutConstraints.anchor = GridBagConstraints.WEST;
			container.add(captionControl, layoutConstraints);
			fieldControlPlaceHolder.setCaptionControl(captionControl);
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

	public void validateForm(JPanel form) {
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

	final public Component createFieldControl(Object object, IFieldInfo field) {
		if (field.getValueOptions(object) != null) {
			return createOptionsControl(object, field);
		} else if (field.getType().getPolymorphicInstanceSubTypes() != null) {
			return new PolymorphicEmbeddedForm(reflectionUI, object, field);
		} else if (field.isNullable()) {
			return new NullableControl(reflectionUI, object, field);
		} else {
			return createNonNullFieldValueControl(object, field);
		}
	}

	public Component createOptionsControl(final Object object, final IFieldInfo field) {
		return new EnumerationControl(reflectionUI, object, new FieldInfoProxy(field) {

			@Override
			public ITypeInfo getType() {
				return new ArrayAsEnumerationTypeInfo(reflectionUI, field.getValueOptions(object),
						field.getCaption() + " Value Options");
			}

		});
	}

	final public Component createNonNullFieldValueControl(Object object, IFieldInfo field) {
		Component result = createCustomFieldControl(object, field);
		if (result != null) {
			return result;
		} else {
			field = SwingRendererUtils.prepareEmbeddedFormCreation(reflectionUI, object, field);
			if (SwingRendererUtils.isEmbeddedFormCreationForbidden(field)) {
				return new DialogAccessControl(reflectionUI, object, field);
			} else {
				return new EmbeddedFormControl(reflectionUI, object, field);
			}
		}
	}

	public Component createCustomFieldControl(Object object, IFieldInfo field) {
		Object fieldValue = field.getValue(object);
		if (ListControl.isCompatibleWith(reflectionUI, fieldValue)) {
			return new ListControl(reflectionUI, object, field);
		} else if (EnumerationControl.isCompatibleWith(reflectionUI, fieldValue)) {
			return new EnumerationControl(reflectionUI, object, field);
		} else if (CheckBoxControl.isCompatibleWith(reflectionUI, fieldValue)) {
			return new CheckBoxControl(reflectionUI, object, field);
		} else if (TextControl.isCompatibleWith(reflectionUI, fieldValue)) {
			return new TextControl(reflectionUI, object, field);
		} else if (PrimitiveUtils.isPrimitiveTypeOrWrapper(fieldValue.getClass())) {
			return new PrimitiveValueControl(reflectionUI, object, field, fieldValue.getClass());
		} else if (FileControl.isCompatibleWith(reflectionUI, fieldValue)) {
			return new FileControl(reflectionUI, object, field);
		} else if (ColorControl.isCompatibleWith(reflectionUI, fieldValue)) {
			return new ColorControl(reflectionUI, object, field);
		} else {
			return null;
		}
	}

	public final boolean hasCustomFieldControl(Object object, IFieldInfo field) {
		return createCustomFieldControl(object, field) != null;
	}

	public final boolean hasCustomFieldControl(Object fieldValue) {
		Object valueAsField = ValueFromVirtualFieldTypeInfo.wrap(reflectionUI, new Object[] { fieldValue }, "", "",
				false);
		ITypeInfo valueAsFieldType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(valueAsField));
		IFieldInfo field = valueAsFieldType.getFields().get(0);
		return createCustomFieldControl(valueAsField, field) != null;
	}

	public class FieldControlPlaceHolder extends JPanel {

		protected static final long serialVersionUID = 1L;
		protected Object object;
		protected IFieldInfo field;
		protected Component fieldControl;
		protected Component captionControl;

		public FieldControlPlaceHolder(Object object, IFieldInfo field) {
			super();
			this.object = object;
			field = handleValueChangeErrors(field, this);
			this.field = field;
			setLayout(new BorderLayout());
			refreshUI();
		}

		public void setCaptionControl(Component captionControl) {
			this.captionControl = captionControl;
		}

		public Component getCaptionControl() {
			return captionControl;
		}

		public Component getFieldControl() {
			return fieldControl;
		}

		public IFieldInfo getField() {
			return field;
		}

		public void refreshUI() {
			if (fieldControl == null) {
				fieldControl = createFieldControl(object, field);
				add(fieldControl, BorderLayout.CENTER);
				handleComponentSizeChange(this);
			} else {
				if (!(((fieldControl instanceof IFieldControl) && ((IFieldControl) fieldControl).refreshUI()))) {
					boolean hadFocus = SwingRendererUtils.hasOrContainsFocus(fieldControl);
					remove(fieldControl);
					fieldControl = null;
					refreshUI();
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
					refreshUI();
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

	public class NullableControl extends JPanel implements IFieldControl {

		protected static final long serialVersionUID = 1L;
		protected ReflectionUI reflectionUI;
		protected Object object;
		protected IFieldInfo field;
		protected JCheckBox nullingControl;
		protected Component subControl;

		public NullableControl(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
			this.reflectionUI = reflectionUI;
			this.object = object;
			this.field = field;

			if (field == null) {
				System.out.println("debug");
			}

			initialize();
		}

		protected void initialize() {
			setLayout(new BorderLayout());
			nullingControl = new JCheckBox();
			nullingControl.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						onNullingControlStateChange();
						subControl.requestFocus();
					} catch (Throwable t) {
						reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(NullableControl.this, t);
					}
				}
			});

			if (!field.isReadOnly()) {
				add(nullingControl, BorderLayout.WEST);
			}

			refreshUI();
		}

		public Component getSubControl() {
			return subControl;
		}

		protected void setShouldBeNull(boolean b) {
			nullingControl.setSelected(!b);
		}

		protected boolean shoulBeNull() {
			return !nullingControl.isSelected();
		}

		@Override
		public boolean refreshUI() {
			Object value = field.getValue(object);
			setShouldBeNull(value == null);
			boolean hadFocus = (subControl != null) && SwingRendererUtils.hasOrContainsFocus(subControl);
			updateSubControl(value);
			if (hadFocus && (subControl != null)) {
				subControl.requestFocus();
			}
			return true;
		}

		@Override
		public void requestFocus() {
			if (subControl != null) {
				subControl.requestFocus();
			}
		}

		protected void onNullingControlStateChange() {
			Object newValue;
			if (!shoulBeNull()) {
				try {
					newValue = reflectionUI.getSwingRenderer().onTypeInstanciationRequest(this, field.getType(), false);
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(this, t);
					newValue = null;
				}
				if (newValue == null) {
					setShouldBeNull(true);
					return;
				}
			} else {
				newValue = null;
				remove(subControl);
				subControl = null;
			}
			field.setValue(object, newValue);
			reflectionUI.getSwingRenderer().refreshFieldControlsByName(SwingRendererUtils.findForm(this, reflectionUI),
					field.getName());
		}

		public void updateSubControl(Object newValue) {
			if (!((newValue != null) && (subControl instanceof IFieldControl)
					&& (((IFieldControl) subControl).refreshUI()))) {
				if (subControl != null) {
					remove(subControl);
				}
				if (newValue != null) {
					subControl = createNonNullFieldValueControl(object, field);
					add(subControl, BorderLayout.CENTER);
				} else {
					subControl = createNullControl(reflectionUI, new Runnable() {
						@Override
						public void run() {
							if (!field.isReadOnly()) {
								setShouldBeNull(false);
								onNullingControlStateChange();
								subControl.requestFocus();
							}
						}
					});
					add(subControl, BorderLayout.CENTER);
				}
				reflectionUI.getSwingRenderer().handleComponentSizeChange(this);
			}
		}

		protected Component createNullControl(ReflectionUI reflectionUI, Runnable onMousePress) {
			return new NullControl(reflectionUI, onMousePress);
		}

		@Override
		public boolean showCaption() {
			if (subControl instanceof IFieldControl) {
				return ((IFieldControl) subControl).showCaption();
			} else {
				return false;
			}
		}

		@Override
		public boolean displayError(ReflectionUIError error) {
			if (subControl instanceof IFieldControl) {
				return ((IFieldControl) subControl).displayError(error);
			} else {
				return false;
			}
		}

	}

}