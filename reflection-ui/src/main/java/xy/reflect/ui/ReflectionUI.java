package xy.reflect.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import xy.reflect.ui.control.IFieldControl;
import xy.reflect.ui.control.MethodControl;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultiSubListField.VirtualItem;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.ArrayTypeInfo;
import xy.reflect.ui.info.type.DefaultBooleanTypeInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.FileTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.MethodParametersAsTypeInfo;
import xy.reflect.ui.info.type.PrecomputedTypeInfoInstanceWrapper;
import xy.reflect.ui.info.type.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.StandardEnumerationTypeInfo;
import xy.reflect.ui.info.type.StandardMapAsListTypeInfo;
import xy.reflect.ui.info.type.StandardMapAsListTypeInfo.StandardMapEntry;
import xy.reflect.ui.undo.CompositeModification;
import xy.reflect.ui.undo.ModificationProxy;
import xy.reflect.ui.undo.ModificationProxyConfiguration;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationOrder;
import xy.reflect.ui.undo.SetFieldValueModification;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.component.AutoResizeTabbedPane;
import xy.reflect.ui.util.component.ScrollPaneOptions;
import xy.reflect.ui.util.component.WrapLayout;

import com.google.common.collect.MapMaker;

public class ReflectionUI {

	protected Map<JPanel, Object> objectByForm = new MapMaker().weakValues()
			.makeMap();
	protected Map<JPanel, ModificationStack> modificationStackByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<String, FieldControlPlaceHolder>> controlPlaceHolderByFieldNameByForm = new MapMaker()
			.weakKeys().makeMap();
	private Map<JPanel, IInfoCollectionSettings> infoCollectionSettingsByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, JLabel> statusLabelByForm = new MapMaker().weakKeys()
			.makeMap();
	protected Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> fieldControlPlaceHoldersByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<InfoCategory, List<Component>>> methodControlsByCategoryByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<Component, IMethodInfo> methodByControl = new MapMaker()
			.weakKeys().makeMap();

	public static void main(String[] args) {
		try {
			ReflectionUI reflectionUI = new ReflectionUI();
			Object object = reflectionUI.onTypeInstanciationRequest(null,
					reflectionUI.getTypeInfo(new JavaTypeInfoSource(
							Object.class)), false);
			if (object == null) {
				return;
			}
			reflectionUI.openObjectFrame(object,
					reflectionUI.getObjectKind(object),
					reflectionUI.getObjectIconImage(object));
		} catch (Throwable t) {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, t.toString(), null,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public Map<JPanel, Object> getObjectByForm() {
		return objectByForm;
	}

	public Map<JPanel, ModificationStack> getModificationStackByForm() {
		return modificationStackByForm;
	}

	public Map<JPanel, JLabel> getStatusLabelByForm() {
		return statusLabelByForm;
	}

	public Map<JPanel, IInfoCollectionSettings> getInfoCollectionSettingsByForm() {
		return infoCollectionSettingsByForm;
	}

	public Map<JPanel, Map<InfoCategory, List<FieldControlPlaceHolder>>> getFieldControlPlaceHoldersByCategoryByForm() {
		return fieldControlPlaceHoldersByCategoryByForm;
	}

	public Map<JPanel, Map<InfoCategory, List<Component>>> getMethodControlsByCategoryByForm() {
		return methodControlsByCategoryByForm;
	}

	public Map<JPanel, Map<String, FieldControlPlaceHolder>> getControlPlaceHolderByFieldNameByForm() {
		return controlPlaceHolderByFieldNameByForm;
	}

	public Map<Component, IMethodInfo> getMethodByControl() {
		return methodByControl;
	}

	public boolean canCopy(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof Serializable) {
			return true;
		}
		return false;
	}

	public Object copy(Object object) {
		if (object == null) {
			return null;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(
					baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object copy = ois.readObject();
			return copy;
		} catch (Throwable t) {
			throw new ReflectionUIError("Could not copy object: "
					+ t.toString());
		}
	}

	public boolean equals(Object value1, Object value2) {
		return ReflectionUIUtils.equalsOrBothNull(value1, value2);
	}

	public String toString(Object object) {
		if (object == null) {
			return null;
		}
		return getTypeInfo(getTypeInfoSource(object)).toString(object);
	}

	public void openObjectFrame(Object object) {
		openObjectFrame(object, getObjectKind(object),
				getObjectIconImage(object));
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		JPanel form = createObjectForm(object);
		JFrame frame = createFrame(
				form,
				title,
				iconImage,
				createCommonToolbarControls(form,
						IInfoCollectionSettings.DEFAULT));
		frame.setVisible(true);
	}

	public List<Component> createCommonToolbarControls(final JPanel form,
			IInfoCollectionSettings settings) {
		List<Component> result = new ArrayList<Component>();
		Object object = getObjectByForm().get(form);
		if (object != null) {
			ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
			if ((type.getDocumentation() != null)
					&& (type.getDocumentation().trim().length() > 0)) {
				result.add(createDocumentationControl(type.getDocumentation()));
			}
		}
		if (!settings.allReadOnly()) {
			final ModificationStack stack = getModificationStackByForm().get(
					form);
			if (stack == null) {
				return null;
			}
			result.addAll(stack.createControls(this));
		}
		return result;
	}

	public JFrame createFrame(Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls) {
		final JFrame frame = new JFrame();
		applyCommonWindowConfiguration(frame, content, toolbarControls, title,
				iconImage);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	public JPanel createObjectForm(Object object) {
		return createObjectForm(object, IInfoCollectionSettings.DEFAULT);
	}

	public JPanel createObjectForm(Object object,
			IInfoCollectionSettings settings) {
		JPanel result = new JPanel();

		getObjectByForm().put(result, object);
		getModificationStackByForm().put(result,
				new ModificationStack(getObjectKind(object)));
		getInfoCollectionSettingsByForm().put(result, settings);

		fillForm(object, result, settings);

		return result;
	}

	public Component createDocumentationControl(String documentation) {
		final JButton result = new JButton(ReflectionUIUtils.HELP_ICON);
		result.setPreferredSize(new Dimension(result.getPreferredSize().height,
				result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		ReflectionUIUtils.setMultilineToolTipText(result,
				translateUIString(documentation));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ReflectionUIUtils.showTooltipNow(result);
			}
		});
		return result;
	}

	public void fillForm(Object object, JPanel form,
			IInfoCollectionSettings settings) {
		form.setLayout(new BorderLayout());

		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));

		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FieldControlPlaceHolder>>();
		getFieldControlPlaceHoldersByCategoryByForm().put(form,
				fieldControlPlaceHoldersByCategory);
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
				field = handleFieldUpdates(field, form, false);
			}
			FieldControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(
					object, field);
			{
				Map<String, FieldControlPlaceHolder> controlPlaceHolderByFieldName = controlPlaceHolderByFieldNameByForm
						.get(form);
				if (controlPlaceHolderByFieldName == null) {
					controlPlaceHolderByFieldName = new HashMap<String, FieldControlPlaceHolder>();
					controlPlaceHolderByFieldNameByForm.put(form,
							controlPlaceHolderByFieldName);
				}
				if (controlPlaceHolderByFieldName.containsKey(field.getName())) {
					throw new ReflectionUIError("Duplicate field name: '"
							+ field.getName() + "'");
				}
				controlPlaceHolderByFieldName.put(field.getName(),
						fieldControlPlaceHolder);
			}
			{
				InfoCategory category = field.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
						.get(category);
				if (fieldControlPlaceHolders == null) {
					fieldControlPlaceHolders = new ArrayList<ReflectionUI.FieldControlPlaceHolder>();
					fieldControlPlaceHoldersByCategory.put(category,
							fieldControlPlaceHolders);
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
					method = handleMethodUpdates(method, form);
				}
			}
			Component methodControl = createMethodControl(object, method);
			getMethodByControl().put(methodControl, method);
			if (settings.allReadOnly()) {
				if (!method.isReadOnly()) {
					if (methodControl instanceof JComponent) {
						ReflectionUIUtils.disableComponentTree(
								(JComponent) methodControl, false);
					} else {
						continue;
					}
				}
			}
			{
				InfoCategory category = method.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<Component> methodControls = methodControlsByCategory
						.get(category);
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
			List<Component> methodControls = methodControlsByCategory
					.get(allCategories.first());
			if (methodControls == null) {
				methodControls = Collections.emptyList();
			}
			layoutControls(fieldControlPlaceHolders, methodControls,
					formContent);
		} else if (allCategories.size() > 1) {
			form.add(formContent, BorderLayout.CENTER);
			formContent.setLayout(new BorderLayout());
			formContent.add(
					createMultipleInfoCategoriesComponent(allCategories,
							fieldControlPlaceHoldersByCategory,
							methodControlsByCategory), BorderLayout.CENTER);
		}
	}

	public IMethodInfo handleMethodUpdates(IMethodInfo method, JPanel form) {
		method = validateFormAfterMethodExecution(method, form);
		method = refreshFieldsAfterMethodModifications(method, form);
		method = makeMethodModificationsUndoable(method, form);
		return method;
	}

	public IFieldInfo handleFieldUpdates(IFieldInfo field, JPanel form,
			boolean refreshOwnControl) {
		field = validateFormAfterFieldValueChange(field, form);
		field = refreshFieldsAfterFieldModification(field, form,
				refreshOwnControl);
		field = makeFieldModificationsUndoable(field, form);
		return field;
	}

	public IMethodInfo validateFormAfterMethodExecution(IMethodInfo method,
			final JPanel form) {
		return new MethodInfoProxy(method) {

			@Override
			public Object invoke(Object object,
					Map<Integer, Object> valueByParameterPosition) {
				try {
					return super.invoke(object, valueByParameterPosition);
				} finally {
					validateForm(form);
				}
			}

		};
	}

	public Component createMultipleInfoCategoriesComponent(
			final SortedSet<InfoCategory> allCategories,
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<Component>> methodControlsByCategory) {
		final JTabbedPane tabbedPane = new AutoResizeTabbedPane();
		for (final InfoCategory category : allCategories) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
					.get(category);
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<Component> methodControls = methodControlsByCategory
					.get(category);
			if (methodControls == null) {
				methodControls = Collections.emptyList();
			}

			JPanel tab = new JPanel();
			tabbedPane.addTab(translateUIString(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutControls(fieldControlPlaceHolders, methodControls, tabContent);

			JPanel buttonsPanel = new JPanel();
			tab.add(buttonsPanel, BorderLayout.SOUTH);
			buttonsPanel.setLayout(new BorderLayout());
			buttonsPanel.setBorder(BorderFactory.createTitledBorder(""));

			ArrayList<InfoCategory> allCategoriesAsList = new ArrayList<InfoCategory>(
					allCategories);
			final int tabIndex = allCategoriesAsList.indexOf(category);
			int tabCount = allCategoriesAsList.size();

			if (tabIndex > 0) {
				JButton previousCategoryButton = new JButton(
						translateUIString("<"));
				buttonsPanel.add(previousCategoryButton, BorderLayout.WEST);
				previousCategoryButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						tabbedPane.setSelectedIndex(tabIndex - 1);
					}
				});
			}

			if (tabIndex < (tabCount - 1)) {
				JButton nextCategoryButton = new JButton(translateUIString(">"));
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

	public InfoCategory getNullInfoCategory() {
		return new InfoCategory("General", -1);
	}

	public ITypeInfoSource getTypeInfoSource(Object object) {
		if (object instanceof PrecomputedTypeInfoInstanceWrapper) {
			return ((PrecomputedTypeInfoInstanceWrapper) object)
					.getPrecomputedTypeInfoSource();
		} else if (object instanceof StandardMapEntry) {
			return new PrecomputedTypeInfoSource(
					((StandardMapEntry<?, ?>) object).getTypeInfo());
		} else {
			return new JavaTypeInfoSource(object.getClass());
		}
	}

	public FieldControlPlaceHolder createFieldControlPlaceHolder(Object object,
			IFieldInfo field) {
		return new FieldControlPlaceHolder(object, field);
	}

	public IFieldInfo handleValueChangeErrors(IFieldInfo field,
			final FieldControlPlaceHolder fieldControlPlaceHolder) {
		return new FieldInfoProxy(field) {

			@Override
			public void setValue(Object object, Object value) {
				try {
					if (!ReflectionUI.this
							.equals(super.getValue(object), value)) {
						super.setValue(object, value);
					}
					fieldControlPlaceHolder.displayError(null);
				} catch (final Throwable t) {
					fieldControlPlaceHolder.displayError(new ReflectionUIError(
							t));
				}
			}

		};
	}

	public IFieldInfo validateFormAfterFieldValueChange(IFieldInfo field,
			final JPanel form) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(Object object, Object value) {
				super.setValue(object, value);
				validateForm(form);
			}

		};
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
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
		try {
			type.validate(object);
			statusLabel.setVisible(false);
		} catch (Exception e) {
			statusLabel.setIcon(ReflectionUIUtils.ERROR_ICON);
			String errorMsg = new ReflectionUIError(e).toString();
			statusLabel.setText(ReflectionUIUtils.multiToSingleLine(errorMsg));
			ReflectionUIUtils.setMultilineToolTipText(statusLabel, errorMsg);
			statusLabel.setVisible(true);
		}
	}

	public IFieldInfo makeFieldModificationsUndoable(final IFieldInfo field,
			final JPanel form) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(Object object, Object newValue) {
				ModificationStack stack = getModificationStackByForm()
						.get(form);
				stack.apply(new SetFieldValueModification(ReflectionUI.this,
						object, field, newValue), false);
			}
		};
	}

	public IMethodInfo makeMethodModificationsUndoable(
			final IMethodInfo method, final JPanel form) {
		return new MethodInfoProxy(method) {

			@Override
			public Object invoke(Object object,
					Map<Integer, Object> valueByParameterPosition) {
				ModificationStack stack = getModificationStackByForm()
						.get(form);
				Object result;
				try {
					result = super.invoke(object, valueByParameterPosition);
				} catch (Throwable t) {
					stack.invalidate();
					throw new ReflectionUIError(t);
				}
				IModification undoModif = method.getUndoModification(object,
						valueByParameterPosition);
				if (undoModif == null) {
					stack.invalidate();
				} else {
					stack.pushUndo(undoModif);
				}
				return result;

			}

		};
	}

	public IFieldInfo refreshFieldsAfterFieldModification(
			final IFieldInfo field, final JPanel form,
			final boolean refreshOwnControl) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(final Object object, Object newValue) {
				super.setValue(object, newValue);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
							IInfoCollectionSettings settings = getInfoCollectionSettingsByForm()
									.get(form);
							for (IFieldInfo fieldToRefresh : type.getFields()) {
								if ((settings != null)
										&& settings
												.excludeField(fieldToRefresh)) {
									continue;
								}
								if (!refreshOwnControl) {
									if (field.getName().equals(
											fieldToRefresh.getName())) {
										continue;
									}
								}
								refreshFieldControl(form,
										fieldToRefresh.getName());
							}
						} catch (Throwable t) {
							handleExceptionsFromDisplayedUI(form, t);
						}
					}
				});
			}
		};
	}

	public IMethodInfo refreshFieldsAfterMethodModifications(
			final IMethodInfo method, final JPanel form) {
		return new MethodInfoProxy(method) {

			@Override
			public Object invoke(Object object,
					Map<Integer, Object> valueByParameterPosition) {
				try {
					return super.invoke(object, valueByParameterPosition);
				} finally {
					refreshAllFieldControls(form);
				}
			}

			@Override
			public IModification getUndoModification(final Object object,
					Map<Integer, Object> valueByParameterPosition) {
				IModification result = super.getUndoModification(object,
						valueByParameterPosition);
				if (result == null) {
					return null;
				}
				result = new ModificationProxy(result,
						new ModificationProxyConfiguration() {
							@Override
							public void executeAfterApplication() {
								refreshAllFieldControls(form);
							}
						});
				return result;
			}

		};
	}

	public void refreshAllFieldControls(JPanel form) {
		Object object = getObjectByForm().get(form);
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
		IInfoCollectionSettings settings = getInfoCollectionSettingsByForm()
				.get(form);
		for (IFieldInfo field : type.getFields()) {
			if ((settings != null) && settings.excludeField(field)) {
				continue;
			}
			refreshFieldControl(form, field.getName());
		}
	}

	public void refreshFieldControl(JPanel form, String fieldName) {
		Object object = getObjectByForm().get(form);
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(type.getFields(),
				fieldName);
		if (field == null) {
			return;
		}
		Map<String, FieldControlPlaceHolder> controlPlaceHolderByFieldName = controlPlaceHolderByFieldNameByForm
				.get(form);
		FieldControlPlaceHolder fieldControlPlaceHolder = controlPlaceHolderByFieldName
				.get(field.getName());
		fieldControlPlaceHolder.refreshUI();
		updateFieldControlLayout(fieldControlPlaceHolder);
	}

	public List<Component> getFieldControlsOf(JPanel form, String fieldName) {
		List<Component> result = new ArrayList<Component>();
		Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = getFieldControlPlaceHoldersByCategoryByForm()
				.get(form);
		for (List<FieldControlPlaceHolder> fieldControlPlaceHolders : fieldControlPlaceHoldersByCategory
				.values()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHolders) {
				if (fieldControlPlaceHolder.getField().getName()
						.equals(fieldName)) {
					result.add(fieldControlPlaceHolder.getFieldControl());
				}
			}
		}
		return result;
	}

	public List<Component> getMethodControlsOf(JPanel form,
			String methodSignature) {
		List<Component> result = new ArrayList<Component>();
		Map<InfoCategory, List<Component>> methodControlByCategory = getMethodControlsByCategoryByForm()
				.get(form);
		for (List<Component> methodControls : methodControlByCategory.values()) {
			for (Component methodControl : methodControls) {
				IMethodInfo method = getMethodByControl().get(methodControl);
				if (ReflectionUIUtils.getMethodInfoSignature(method).equals(
						methodSignature)) {
					result.add(methodControl);
				}
			}
		}
		return result;
	}

	public List<JPanel> getForms(Object object) {
		return ReflectionUIUtils.getKeysFromValue(getObjectByForm(), object);
	}

	public IFieldInfo getFormsUpdatingField(Object object, String fieldName) {
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(type.getFields(),
				fieldName);
		for (JPanel form : getForms(object)) {
			field = handleFieldUpdates(field, form, true);
		}
		return field;
	}

	public IMethodInfo getFormsUpdatingMethod(Object object,
			String methodSignature) {
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
		IMethodInfo method = ReflectionUIUtils.findMethodBtSignature(
				type.getMethods(), methodSignature);
		if (method == null) {
			return null;
		}
		for (JPanel form : getForms(object)) {
			method = handleMethodUpdates(method, form);
		}
		return method;
	}

	public void layoutControls(
			List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<Component> methodControls, JPanel parentForm) {
		JPanel fieldsPanel = createFieldsPanel(fielControlPlaceHolders);
		JPanel methodsPanel = createMethodsPanel(methodControls);
		layoutControlPanels(parentForm, fieldsPanel, methodsPanel);
	}

	public void layoutControlPanels(JPanel parentForm, JPanel fieldsPanel,
			JPanel methodsPanel) {
		parentForm.setLayout(new BorderLayout());
		parentForm.add(fieldsPanel, BorderLayout.CENTER);
		parentForm.add(methodsPanel, BorderLayout.SOUTH);
	}

	public JPanel createMethodsPanel(final List<Component> methodControls) {
		JPanel methodsPanel = new JPanel();
		methodsPanel.setLayout(new WrapLayout(WrapLayout.CENTER));
		for (final Component methodControl : methodControls) {
			JPanel methodControlContainer = new JPanel() {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension result = super.getPreferredSize();
					if (result == null) {
						return super.getPreferredSize();
					}
					int maxMethodControlWidth = 0;
					for (final Component methodControl : methodControls) {
						Dimension controlPreferredSize = methodControl
								.getPreferredSize();
						if (controlPreferredSize != null) {
							maxMethodControlWidth = Math.max(
									maxMethodControlWidth,
									controlPreferredSize.width);
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

	public JPanel createFieldsPanel(
			List<FieldControlPlaceHolder> fielControlPlaceHolders) {
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new GridBagLayout());
		int spacing = 5;
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FieldControlPlaceHolder fieldControlPlaceHolder = fielControlPlaceHolders
					.get(i);
			{
				GridBagConstraints layoutConstraints = new GridBagConstraints();
				layoutConstraints.gridy = i;
				fieldsPanel.add(fieldControlPlaceHolder, layoutConstraints);
				updateFieldControlLayout(fieldControlPlaceHolder);
			}
			IFieldInfo field = fieldControlPlaceHolder.getField();
			if ((field.getDocumentation() != null)
					&& (field.getDocumentation().trim().length() > 0)) {
				GridBagConstraints layoutConstraints = new GridBagConstraints();
				layoutConstraints.insets = new Insets(spacing, spacing,
						spacing, spacing);
				layoutConstraints.gridx = 2;
				layoutConstraints.gridy = i;
				layoutConstraints.weighty = 1.0;
				fieldsPanel.add(
						createDocumentationControl(field.getDocumentation()),
						layoutConstraints);
			}

		}
		return fieldsPanel;
	}

	public void updateFieldControlLayout(
			FieldControlPlaceHolder fieldControlPlaceHolder) {
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
			JLabel captionControl = new JLabel(
					translateUIString(field.getCaption() + ": "));
			GridBagConstraints layoutConstraints = new GridBagConstraints();
			layoutConstraints.insets = new Insets(spacing, spacing, spacing,
					spacing);
			layoutConstraints.gridx = 0;
			layoutConstraints.gridy = i;
			layoutConstraints.weighty = 1.0;
			layoutConstraints.anchor = GridBagConstraints.WEST;
			container.add(captionControl, layoutConstraints);
			fieldControlPlaceHolder.setCaptionControl(captionControl);
		}
		{
			GridBagConstraints layoutConstraints = new GridBagConstraints();
			layoutConstraints.insets = new Insets(spacing, spacing, spacing,
					spacing);
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

	public void handleExceptionsFromDisplayedUI(Component activatorComponent,
			final Throwable t) {
		logError(new ReflectionUIError(t));
		openErrorDialog(activatorComponent,
				translateUIString("An Error Occured"), new ReflectionUIError(t));
	}

	public void logError(ReflectionUIError t) {
		t.printStackTrace();
	}

	public void openErrorDialog(Component activatorComponent, String title,
			final ReflectionUIError error) {
		JTextArea textArea = new JTextArea(error.toString());
		textArea.setEditable(false);
		textArea.setMargin(new Insets(5, 5, 5, 5));
		textArea.setBorder(BorderFactory.createTitledBorder(""));
		Component errorComponent = new JOptionPane(new JScrollPane(textArea),
				JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] {});

		JDialog[] dialogArray = new JDialog[1];

		List<Component> buttons = new ArrayList<Component>();
		final JButton deatilsButton = new JButton(translateUIString("Details"));
		deatilsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openObjectDialog(deatilsButton, error,
						translateUIString("Error Details"),
						getObjectIconImage(error), true, null, null, null,
						null, IInfoCollectionSettings.READ_ONLY);
			}
		});
		buttons.add(deatilsButton);
		buttons.addAll(createDialogOkCancelButtons(dialogArray, null, "Close",
				null, false));

		dialogArray[0] = createDialog(activatorComponent, errorComponent,
				title, null, buttons, null);
		showDialog(dialogArray[0], true);

	}

	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		ITypeInfo result;
		if (typeSource instanceof PrecomputedTypeInfoSource) {
			result = ((PrecomputedTypeInfoSource) typeSource)
					.getPrecomputedType();
		} else if (typeSource instanceof JavaTypeInfoSource) {
			JavaTypeInfoSource javaTypeSource = (JavaTypeInfoSource) typeSource;
			if (StandardCollectionTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				Class<?> itemType = ReflectionUIUtils
						.getJavaGenericTypeParameter(javaTypeSource,
								Collection.class, 0);
				result = new StandardCollectionTypeInfo(this,
						javaTypeSource.getJavaType(), itemType);
			} else if (StandardMapAsListTypeInfo
					.isCompatibleWith(javaTypeSource.getJavaType())) {
				Class<?> keyType = ReflectionUIUtils
						.getJavaGenericTypeParameter(javaTypeSource, Map.class,
								0);
				Class<?> valueType = ReflectionUIUtils
						.getJavaGenericTypeParameter(javaTypeSource, Map.class,
								1);
				result = new StandardMapAsListTypeInfo(this,
						javaTypeSource.getJavaType(), keyType, valueType);
			} else if (javaTypeSource.getJavaType().isArray()) {
				Class<?> itemType = javaTypeSource.getJavaType()
						.getComponentType();
				result = new ArrayTypeInfo(this, javaTypeSource.getJavaType(),
						itemType);
			} else if (javaTypeSource.getJavaType().isEnum()) {
				result = new StandardEnumerationTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (DefaultBooleanTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				result = new DefaultBooleanTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (DefaultTextualTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				result = new DefaultTextualTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (FileTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				result = new FileTypeInfo(this);
			} else {
				result = new DefaultTypeInfo(this, javaTypeSource.getJavaType());
			}
		} else {
			throw new ReflectionUIError();
		}
		return result;
	}

	public Component createMethodControl(final Object object,
			final IMethodInfo method) {
		return new MethodControl(this, object, method);
	}

	public String getMethodTitle(Object object, IMethodInfo method,
			Object returnValue, String context) {
		String result = method.getCaption();
		if (object != null) {
			result = composeTitle(getObjectKind(object), result);
		}
		if (context != null) {
			result = composeTitle(result, context);
		}
		if (returnValue != null) {
			result = composeTitle(result, getObjectKind(returnValue));
		}
		return result;
	}

	public String composeTitle(String contextTitle, String localTitle) {
		if (contextTitle == null) {
			return localTitle;
		}
		return contextTitle + " - " + localTitle;
	}

	public boolean onMethodInvocationRequest(
			final Component activatorComponent, final Object object,
			final IMethodInfo method, Object[] returnValueArray,
			boolean displayReturnValue) {
		if (returnValueArray == null) {
			returnValueArray = new Object[1];
		}
		final boolean[] exceptionThrownArray = new boolean[] { false };
		if (method.getParameters().size() > 0) {
			if (!openMethoExecutionSettingDialog(activatorComponent, object,
					method, returnValueArray, exceptionThrownArray)) {
				return false;
			}
		} else {
			final Object[] finalReturnValueArray = returnValueArray;
			showBusyDialogWhile(activatorComponent, new Runnable() {
				@Override
				public void run() {
					try {
						finalReturnValueArray[0] = method.invoke(object,
								Collections.<Integer, Object> emptyMap());
					} catch (Throwable t) {
						exceptionThrownArray[0] = true;
						throw new ReflectionUIError(t);
					}
				}
			}, getMethodTitle(object, method, null, "Execution"));
		}
		if (displayReturnValue && !exceptionThrownArray[0]) {
			if (method.getReturnValueType() != null) {
				openMethodReturnValueWindow(activatorComponent, object, method,
						returnValueArray[0]);
			}
		}
		return true;
	}

	public void showBusyDialogWhile(final Component ownerComponent,
			final Runnable runnable, String title) {
		final JXBusyLabel busyLabel = new JXBusyLabel();
		busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		busyLabel.setText("Please wait...");
		busyLabel.setVerticalTextPosition(SwingConstants.TOP);
		busyLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		final JDialog dialog = createDialog(ownerComponent, busyLabel, title,
				null, null, null);
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

	public boolean openMethoExecutionSettingDialog(
			final Component activatorComponent, final Object object,
			final IMethodInfo method, final Object[] returnValueArray,
			final boolean[] exceptionThrownArray) {
		final Map<Integer, Object> valueByParameterPosition = new HashMap<Integer, Object>();
		JPanel methodForm = createObjectForm(new PrecomputedTypeInfoInstanceWrapper(
				new MethodParametersAsTypeInfo.InstanceInfo(object,
						valueByParameterPosition),
				new MethodParametersAsTypeInfo(this, method)));
		final boolean[] invokedStatusArray = new boolean[] { false };
		final JDialog[] methodDialogArray = new JDialog[1];
		List<Component> toolbarControls = new ArrayList<Component>();
		String doc = method.getDocumentation();
		if ((doc != null) && (doc.trim().length() > 0)) {
			toolbarControls.add(createDocumentationControl(doc));
		}
		toolbarControls.addAll(createDialogOkCancelButtons(methodDialogArray,
				invokedStatusArray, method.getCaption(), new Runnable() {
					@Override
					public void run() {
						showBusyDialogWhile(activatorComponent, new Runnable() {
							@Override
							public void run() {
								try {
									returnValueArray[0] = method.invoke(object,
											valueByParameterPosition);
								} catch (Throwable t) {
									exceptionThrownArray[0] = true;
									throw new ReflectionUIError(t);
								}
							}
						}, getMethodTitle(object, method, null, "Execution"));
					}

				}, true));

		methodDialogArray[0] = createDialog(activatorComponent, methodForm,
				getMethodTitle(object, method, null, "Setting"), null,
				toolbarControls, null);

		showDialog(methodDialogArray[0], true);
		return invokedStatusArray[0];
	}

	public void openMethodReturnValueWindow(Component activatorComponent,
			Object object, IMethodInfo method, Object returnValue) {
		if (returnValue == null) {
			String msg = "'" + method.getCaption()
					+ "' excution returned no result!";
			showMessageDialog(activatorComponent, msg,
					getMethodTitle(object, method, null, "Result"));
		} else {
			openValueFrame(
					returnValue,
					IInfoCollectionSettings.DEFAULT,
					getMethodTitle(object, method, returnValue,
							"Execution Result"));
		}
	}

	public void showMessageDialog(Component activatorComponent, String msg,
			String title) {
		JDialog[] dialogArray = new JDialog[1];
		showDialog(
				dialogArray[0] = createDialog(
						activatorComponent,
						new JLabel("<HTML><BR>" + msg + "<BR><BR><HTML>",
								SwingConstants.CENTER),
						title,
						null,
						createDialogOkCancelButtons(dialogArray, null, null,
								null, false), null), true);
	}

	public void openObjectDialog(Component parent, Object object, boolean modal) {
		openObjectDialog(parent, object, getObjectKind(object),
				getObjectIconImage(object), modal);
	}

	public void openObjectDialog(Component parent, Object object, String title,
			Image iconImage, boolean modal) {
		openObjectDialog(parent, object, title, iconImage, modal, null, null,
				null, null, IInfoCollectionSettings.DEFAULT);
	}

	public void openObjectDialog(Component parent, Object object, String title,
			Image iconImage, boolean modal,
			List<Component> additionalToolbarControls,
			boolean[] okPressedArray, Runnable whenClosingDialog,
			ModificationStack[] modificationstackArray,
			IInfoCollectionSettings settings) {
		JPanel form = createObjectForm(object, settings);
		if (modificationstackArray == null) {
			modificationstackArray = new ModificationStack[1];
		}
		modificationstackArray[0] = getModificationStackByForm().get(form);
		List<Component> toolbarControls = new ArrayList<Component>();
		if (!settings.allReadOnly()) {
			List<Component> commonToolbarControls = createCommonToolbarControls(
					form, settings);
			if (commonToolbarControls != null) {
				toolbarControls.addAll(commonToolbarControls);
			}
		}
		if (additionalToolbarControls != null) {
			toolbarControls.addAll(additionalToolbarControls);
		}
		JDialog[] dialogArray = new JDialog[1];
		if (settings.allReadOnly()) {
			toolbarControls.addAll(createDialogOkCancelButtons(dialogArray,
					null, "Close", null, false));
		} else {
			toolbarControls.addAll(createDialogOkCancelButtons(dialogArray,
					okPressedArray, "OK", null, true));
		}
		dialogArray[0] = createDialog(parent, form, title, iconImage,
				toolbarControls, whenClosingDialog);
		showDialog(dialogArray[0], modal);
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

	public JDialog createDialog(Component ownerComponent, Component content,
			String title, Image iconImage,
			List<? extends Component> toolbarControls,
			final Runnable whenClosing) {
		Window owner = ReflectionUIUtils
				.getWindowAncestorOrSelf(ownerComponent);
		JDialog dialog = new JDialog(owner, title) {
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
		applyCommonWindowConfiguration(dialog, content, toolbarControls, title,
				iconImage);
		dialog.setResizable(true);
		return dialog;
	}

	public void applyCommonWindowConfiguration(Window window,
			Component content, List<? extends Component> toolbarControls,
			String title, Image iconImage) {
		if (window instanceof JFrame) {
			((JFrame) window).setTitle(title);
		} else if (window instanceof JDialog) {
			((JDialog) window).setTitle(title);
		}
		Container contentPane = createWindowContentPane(window, content,
				toolbarControls);
		ReflectionUIUtils.setContentPane(window, contentPane);
		window.pack();
		window.setLocationRelativeTo(null);
		Rectangle bounds = window.getBounds();
		bounds.grow(50, 10);
		window.setBounds(bounds);
		adjustWindowBounds(window);
		if (iconImage == null) {
			window.setIconImage(new BufferedImage(1, 1,
					BufferedImage.TYPE_INT_ARGB));
		} else {
			window.setIconImage(iconImage);
		}
	}

	public Container createWindowContentPane(Window window, Component content,
			List<? extends Component> toolbarControls) {
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		if (content != null) {
			JPanel form = ReflectionUIUtils.findForm(content, this);
			if (form != null) {
				contentPane.add(createStatusBar(form), BorderLayout.NORTH);
				validateForm(form);
			}
			content = new JScrollPane(new ScrollPaneOptions(content, true,
					false));
			contentPane.add(content, BorderLayout.CENTER);
		}
		if (toolbarControls != null) {
			contentPane.add(createToolBar(toolbarControls), BorderLayout.SOUTH);
		}
		return contentPane;
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

	public Component createStatusBar(JPanel form) {
		JLabel result = new JLabel();
		result.setOpaque(true);
		result.setFont(new JToolTip().getFont());
		result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getStatusLabelByForm().put(form, result);
		return result;
	}

	public ReflectionUI getSubReflectionUI() {
		return this;
	}

	public String translateUIString(String string) {
		return string;
	}

	public Object onTypeInstanciationRequest(Component activatorComponent,
			ITypeInfo type, boolean silent) {
		try {
			List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
			if ((polyTypes != null) && (polyTypes.size() > 0)) {
				if (polyTypes.size() == 1) {
					type = polyTypes.get(0);
				} else {
					if (silent) {
						type = polyTypes.get(0);
					} else {
						type = openSelectionDialog(
								activatorComponent,
								polyTypes,
								null,
								MessageFormat
										.format(translateUIString("Choose the type of ''{0}'':"),
												type.getCaption()), null);
						if (type == null) {
							return null;
						}
					}
				}
			}

			List<IMethodInfo> constructors = type.getConstructors();
			if (constructors.size() == 0) {
				if (type.isConcrete() || silent) {
					throw new ReflectionUIError(
							"No accessible constructor found");
				} else {
					type = openConcreteClassSelectionDialog(activatorComponent,
							type);
					if (type == null) {
						return null;
					} else {
						return onTypeInstanciationRequest(activatorComponent,
								type, silent);
					}
				}
			}

			if (constructors.size() == 1) {
				IMethodInfo constructor = constructors.get(0);
				if (silent) {
					return constructor.invoke(null,
							Collections.<Integer, Object> emptyMap());
				} else {
					Object[] returnValueArray = new Object[1];
					onMethodInvocationRequest(activatorComponent, null,
							constructor, returnValueArray, false);
					return returnValueArray[0];
				}
			}

			constructors = new ArrayList<IMethodInfo>(constructors);
			Collections.sort(constructors, new Comparator<IMethodInfo>() {
				@Override
				public int compare(IMethodInfo o1, IMethodInfo o2) {
					return new Integer(o1.getParameters().size())
							.compareTo(new Integer(o2.getParameters().size()));
				}
			});

			if (silent) {
				IMethodInfo smallerConstructor = constructors.get(0);
				return smallerConstructor.invoke(null,
						Collections.<Integer, Object> emptyMap());
			} else {
				IMethodInfo chosenContructor = openSelectionDialog(
						activatorComponent, constructors, null,
						translateUIString("Choose an option:"), null);
				if (chosenContructor == null) {
					return null;
				}
				Object[] returnValueArray = new Object[1];
				onMethodInvocationRequest(activatorComponent, null,
						chosenContructor, returnValueArray, false);
				return returnValueArray[0];
			}
		} catch (Throwable t) {
			throw new ReflectionUIError(
					"Could not create an instance of type '" + type + "': "
							+ t.toString(), t);
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T openSelectionDialog(Component parentComponent,
			List<T> choices, T initialSelection, String message, String title) {
		return (T) JOptionPane.showInputDialog(parentComponent,
				translateUIString(message), translateUIString(title),
				JOptionPane.QUESTION_MESSAGE, null, choices.toArray(),
				initialSelection);
	}

	public ITypeInfo openConcreteClassSelectionDialog(
			Component parentComponent, ITypeInfo type) {
		String className = JOptionPane.showInputDialog(parentComponent,
				translateUIString("Class name of the '" + type.getCaption()
						+ "' you want to create:"));
		if (className == null) {
			return null;
		}
		try {
			return getTypeInfo(new JavaTypeInfoSource(Class.forName(className)));
		} catch (ClassNotFoundException e) {
			throw new ReflectionUIError(e);
		}
	}

	public String getObjectKind(Object object) {
		if (object == null) {
			return "(Not found)";
		}
		if (object instanceof VirtualItem) {
			return ((VirtualItem) object).toString();
		}
		if (object instanceof StandardMapEntry<?, ?>) {
			String result = "Entry";
			Object key = ((StandardMapEntry<?, ?>) object).getKey();
			result += (key == null) ? "" : (" (" + toString(key) + ")");
			return result;
		}
		return getTypeInfo(getTypeInfoSource(object)).getCaption();
	}

	public Image getObjectIconImage(Object object) {
		return null;
	}

	public boolean openValueDialog(Component activatorComponent,
			final Accessor<Object> valueAccessor,
			final IInfoCollectionSettings settings,
			final ModificationStack parentModificationStack,
			final String title, final boolean[] changeDetectedArray) {
		final Object[] valueArray = new Object[] { valueAccessor.get() };
		final ITypeInfo valueTypeInfo = getTypeInfo(getTypeInfoSource(valueArray[0]));
		final Object toOpen;
		if (valueTypeInfo.hasCustomFieldControl()) {
			toOpen = ReflectionUIUtils.wrapValueAsField(this, valueArray,
					"Value", title, settings.allReadOnly());
		} else {
			toOpen = valueArray[0];
		}

		final boolean[] okPressedArray = new boolean[] { false };
		final ModificationStack[] modificationstackArray = new ModificationStack[1];

		Runnable whenClosingDialog = new Runnable() {
			@Override
			public void run() {
				if (okPressedArray[0]) {
					Object oldValue = valueAccessor.get();
					if (!ReflectionUI.this.equals(oldValue, valueArray[0])) {
						valueAccessor.set(valueArray[0]);
						changeDetectedArray[0] = true;
					} else if ((oldValue == valueArray[0])
							&& (valueArray[0] != null)
							&& !valueArray[0].getClass().isPrimitive()) {
						if (modificationstackArray[0] != null) {
							if (modificationstackArray[0]
									.getNumberOfUndoUnits() > 0) {
								changeDetectedArray[0] = true;
								if (parentModificationStack != null) {
									List<IModification> undoModifications = new ArrayList<IModification>();
									undoModifications
											.addAll(Arrays
													.asList(modificationstackArray[0]
															.getUndoModifications(ModificationOrder.LIFO)));
									parentModificationStack
											.pushUndo(new CompositeModification(
													ModificationStack
															.getUndoTitle("Edit "
																	+ title),
													ModificationOrder.LIFO,
													undoModifications));
								}
							}
						}
					}
				} else {
					if (modificationstackArray[0] != null) {
						modificationstackArray[0].undoAll(false);
					}
				}
			}
		};

		openObjectDialog(activatorComponent, toOpen, title,
				getObjectIconImage(valueArray[0]), true, null, okPressedArray,
				whenClosingDialog, modificationstackArray, settings);

		return okPressedArray[0];

	}

	public void openValueFrame(Object value,
			final IInfoCollectionSettings settings, final String title) {
		final Object[] valueArray = new Object[] { value };
		final ITypeInfo valueTypeInfo = getTypeInfo(getTypeInfoSource(valueArray[0]));
		final Object toOpen;
		if (valueTypeInfo.hasCustomFieldControl()) {
			toOpen = ReflectionUIUtils.wrapValueAsField(this, valueArray,
					"Value", title, settings.allReadOnly());
		} else {
			toOpen = valueArray[0];
		}
		openObjectFrame(toOpen, title, getObjectIconImage(valueArray[0]));
	}

	public List<JButton> createDialogOkCancelButtons(
			final JDialog[] dialogArray, final boolean[] okPressedArray,
			String okCaption, final Runnable okAction,
			boolean createCancelButton) {
		List<JButton> result = new ArrayList<JButton>();

		final JButton okButton = new JButton(
				translateUIString((okCaption != null) ? okCaption : "OK"));
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
					translateUIString("Cancel"));
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

	public String getFieldTitle(Object object, IFieldInfo field) {
		String result = composeTitle(getObjectKind(object), field.getCaption());
		Object fieldValue = field.getValue(object);
		if (fieldValue != null) {
			String fieldValueKind = getObjectKind(field.getValue(object));
			if (!field.getCaption().equals(fieldValueKind)) {
				result = composeTitle(result, fieldValueKind);
			}
		}
		return result;
	}

	public void adjustWindowBounds(Window window) {
		Rectangle bounds = window.getBounds();
		Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();
		if (bounds.width < maxBounds.width / 3) {
			bounds.grow((maxBounds.width / 3 - bounds.width) / 2, 0);
		}
		bounds = maxBounds.intersection(bounds);
		window.setBounds(bounds);
	}

	public Color getNullColor() {
		return new JTextArea().getDisabledTextColor();
	}

	public void handleComponentSizeChange(Component c) {
		Window window = SwingUtilities.getWindowAncestor(c);
		if (window != null) {
			window.validate();
			JScrollPane scrollPane = null;
			Container contentPane = ReflectionUIUtils.getContentPane(window);
			if (contentPane != null) {
				for (Component mayBeScrollPanel : contentPane.getComponents()) {
					if (mayBeScrollPanel instanceof JScrollPane) {
						scrollPane = (JScrollPane) mayBeScrollPanel;
						break;
					}
				}
			}
			if (scrollPane != null) {
				JScrollBar verticalScrollBVar = scrollPane
						.getVerticalScrollBar();
				int heightToGrow = verticalScrollBVar.getMaximum()
						- verticalScrollBVar.getVisibleAmount();
				Dimension windowSize = window.getSize();
				windowSize.height += heightToGrow;
				window.setSize(windowSize);
				adjustWindowBounds(window);
			}
		}
	}

	protected class FieldControlPlaceHolder extends JPanel {

		protected static final long serialVersionUID = 1L;
		protected Object object;
		protected IFieldInfo field;
		protected Component fieldControl;
		private Component captionControl;

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
				fieldControl = field.getType()
						.createFieldControl(object, field);
				add(fieldControl, BorderLayout.CENTER);
			} else {
				if (!(((fieldControl instanceof IFieldControl) && ((IFieldControl) fieldControl)
						.refreshUI()))) {
					remove(fieldControl);
					fieldControl = null;
					refreshUI();
				}
			}

			handleComponentSizeChange(this);
		}

		public void displayError(ReflectionUIError error) {
			if (!((fieldControl instanceof IFieldControl) && ((IFieldControl) fieldControl)
					.displayError(error))) {
				if (error != null) {
					handleExceptionsFromDisplayedUI(fieldControl, error);
					refreshUI();
				}
			}
		}

		public boolean showCaption() {
			if (((fieldControl instanceof IFieldControl) && ((IFieldControl) fieldControl)
					.showCaption())) {
				return true;
			} else {
				return false;
			}
		}

	}

}
