package xy.reflect.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.ICanShowCaptionControl;
import xy.reflect.ui.control.IRefreshableControl;
import xy.reflect.ui.control.MethodControl;
import xy.reflect.ui.control.ModificationStack;
import xy.reflect.ui.control.ModificationStack.IModification;
import xy.reflect.ui.info.FieldInfoProxy;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultiSubListField.VirtualItem;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ArrayTypeInfo;
import xy.reflect.ui.info.type.DefaultBooleanTypeInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.FileTypeInfo;
import xy.reflect.ui.info.type.IPrecomputedTypeInfoInstance;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.StandardEnumerationTypeInfo;
import xy.reflect.ui.info.type.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.StandardMapListTypeInfo;
import xy.reflect.ui.info.type.StandardMapListTypeInfo.StandardMapEntry;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.component.ScrollPaneOptions;
import xy.reflect.ui.util.component.SimpleLayout;
import xy.reflect.ui.util.component.WrapLayout;
import xy.reflect.ui.util.component.SimpleLayout.Kind;

import com.google.common.collect.MapMaker;

public class ReflectionUI {

	protected Map<JPanel, Object> objectByForm = new MapMaker().weakValues()
			.makeMap();
	protected Map<JPanel, ModificationStack> modificationStackByForm = new MapMaker()
			.weakKeys().makeMap();
	protected Map<JPanel, Map<String, FielControlPlaceHolder>> controlPlaceHolderByFieldNameByForm = new MapMaker()
			.weakKeys().makeMap();

	public static void main(String[] args) {
		ReflectionUI reflectionUI = new ReflectionUI();
		Object object = reflectionUI.onTypeInstanciationRequest(
				reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class)),
				null, false);
		if (object == null) {
			return;
		}
		reflectionUI.openObjectFrame(object,
				reflectionUI.getObjectKind(object),
				reflectionUI.getObjectIconImage(object));
	}

	public Map<JPanel, Object> getObjectByForm() {
		return objectByForm;
	}

	public Map<JPanel, ModificationStack> getModificationStackByForm() {
		return modificationStackByForm;
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
			throw new AssertionError("Could not copy object: " + t.toString());
		}
	}

	public void openObjectFrame(Object object, String title, Image iconImage) {
		JPanel form = createObjectForm(object);
		JFrame frame = createFrame(form, title, iconImage,
				createCommonToolbarControls(form));
		frame.setVisible(true);
	}

	public List<Component> createCommonToolbarControls(final JPanel form) {
		final ModificationStack stack = getModificationStackByForm().get(form);
		if (stack == null) {
			return null;
		}
		List<Component> result = new ArrayList<Component>();
		result.addAll(stack.createControls(this));
		return result;
	}

	public JFrame createFrame(Component content, String title, Image iconImage,
			List<Component> toolbarControls) {
		final JFrame frame = new JFrame();
		frame.setTitle(title);
		if (iconImage == null) {
			frame.setIconImage(new BufferedImage(1, 1,
					BufferedImage.TYPE_INT_ARGB));
		} else {
			frame.setIconImage(iconImage);
		}
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());

		if (content != null) {
			JScrollPane scrollPane = new JScrollPane(new ScrollPaneOptions(
					content, true, false));
			frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		}

		if (toolbarControls != null) {
			JPanel toolbar = new JPanel();
			toolbar.setLayout(new FlowLayout(FlowLayout.CENTER));
			for (Component tool : toolbarControls) {
				toolbar.add(tool);
			}
			frame.getContentPane().add(toolbar, BorderLayout.SOUTH);
		}
		frame.pack();
		frame.setLocationRelativeTo(null);
		ReflectionUIUtils.adjustWindowBounds(frame);
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
		if (fillForm(object, result, settings) == 0) {
			return null;
		}
		return result;
	}

	public int fillForm(Object object, JPanel form,
			IInfoCollectionSettings settings) {
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));

		Map<InfoCategory, List<FielControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new HashMap<InfoCategory, List<FielControlPlaceHolder>>();
		List<IFieldInfo> fields = type.getFields();
		for (IFieldInfo field : fields) {
			if (settings.excludeField(field)) {
				continue;
			}
			FielControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(
					object, field, form);
			{
				Map<String, FielControlPlaceHolder> controlPlaceHolderByFieldName = controlPlaceHolderByFieldNameByForm
						.get(form);
				if (controlPlaceHolderByFieldName == null) {
					controlPlaceHolderByFieldName = new HashMap<String, FielControlPlaceHolder>();
					controlPlaceHolderByFieldNameByForm.put(form,
							controlPlaceHolderByFieldName);
				}
				controlPlaceHolderByFieldName.put(field.getName(),
						fieldControlPlaceHolder);
			}
			{
				InfoCategory category = field.getCategory();
				if (category == null) {
					category = getNullInfoCategory();
				}
				List<FielControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
						.get(category);
				if (fieldControlPlaceHolders == null) {
					fieldControlPlaceHolders = new ArrayList<ReflectionUI.FielControlPlaceHolder>();
					fieldControlPlaceHoldersByCategory.put(category,
							fieldControlPlaceHolders);
				}
				fieldControlPlaceHolders.add(fieldControlPlaceHolder);
			}
		}

		Map<InfoCategory, List<Component>> methodControlsByCategory = new HashMap<InfoCategory, List<Component>>();
		List<IMethodInfo> methods = type.getMethods();
		for (IMethodInfo method : methods) {
			if (settings.excludeMethod(method)) {
				continue;
			}
			if (settings.allReadOnly()) {
				if (!method.isReadOnly()) {
					continue;
				}
			} else {
				if (!method.isReadOnly()) {
					method = makeMethodModificationsVisibleAndUndoable(method,
							form);
				}
			}
			Component methodControl = createMethodControl(object, method);
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

		SortedSet<InfoCategory> allCategories = new TreeSet<InfoCategory>();
		allCategories.addAll(fieldControlPlaceHoldersByCategory.keySet());
		allCategories.addAll(methodControlsByCategory.keySet());
		if (allCategories.size() == 1) {
			List<FielControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
					.get(allCategories.first());
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<Component> methodControls = methodControlsByCategory
					.get(allCategories.first());
			if (methodControls == null) {
				methodControls = Collections.emptyList();
			}
			layoutControls(fieldControlPlaceHolders, methodControls, form);
		} else if (allCategories.size() > 1) {
			form.setLayout(new BorderLayout());
			form.add(
					createMultipleInfoCategoriesComponent(allCategories,
							fieldControlPlaceHoldersByCategory,
							methodControlsByCategory), BorderLayout.CENTER);
		}
		return fields.size() + methods.size();
	}

	public Component createMultipleInfoCategoriesComponent(
			final SortedSet<InfoCategory> allCategories,
			Map<InfoCategory, List<FielControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<Component>> methodControlsByCategory) {
		final JTabbedPane tabbedPane = new JTabbedPane();
		for (final InfoCategory category : allCategories) {
			List<FielControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
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
			buttonsPanel.setBorder(BorderFactory.createRaisedBevelBorder());

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
		if (object instanceof IPrecomputedTypeInfoInstance) {
			return ((IPrecomputedTypeInfoInstance) object)
					.getPrecomputedTypeInfoSource();
		} else if (object instanceof StandardMapEntry) {
			return new PrecomputedTypeInfoSource(
					((StandardMapEntry<?, ?>) object).getTypeInfo());
		} else {
			return new JavaTypeInfoSource(object.getClass());
		}
	}

	public FielControlPlaceHolder createFieldControlPlaceHolder(Object object,
			IFieldInfo field, JPanel form) {
		return new FielControlPlaceHolder(object, field, form);
	}

	public IFieldInfo makeFieldModificationsUndoable(final IFieldInfo field,
			final JPanel form) {
		return new FieldInfoProxy(field) {
			@Override
			public void setValue(Object object, Object newValue) {
				ModificationStack stack = getModificationStackByForm()
						.get(form);
				stack.apply(new ModificationStack.SetFieldValueModification(
						ReflectionUI.this, object, field, newValue), false);
			}
		};
	}

	public IMethodInfo makeMethodModificationsVisibleAndUndoable(
			final IMethodInfo method, final JPanel form) {
		return new MethodInfoProxy(method) {

			@Override
			public Object invoke(Object object,
					Map<String, Object> valueByParameterName) {
				ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
				Map<String, Object> fieldValueCopyByFieldName = new HashMap<String, Object>();
				Object COULD_NOT_COPY = new Object();
				for (IFieldInfo field : type.getFields()) {
					Object fieldValue = field.getValue(object);
					Object fieldValueCopy;
					if (!canCopy(fieldValue)) {
						fieldValueCopy = COULD_NOT_COPY;
					} else {
						try {
							fieldValueCopy = copy(fieldValue);
						} catch (Throwable t) {
							fieldValueCopy = COULD_NOT_COPY;
						}
					}
					fieldValueCopyByFieldName.put(field.getName(),
							fieldValueCopy);

				}

				Object result = super.invoke(object, valueByParameterName);

				List<IModification> undoModifs = new ArrayList<ModificationStack.IModification>();
				for (IFieldInfo field : type.getFields()) {
					Object fieldValueCopy = fieldValueCopyByFieldName.get(field
							.getName());
					if (fieldValueCopy == COULD_NOT_COPY) {
						continue;
					}
					Object fieldValue = field.getValue(object);
					if (!ReflectionUIUtils.equalsOrBothNull(fieldValue,
							fieldValueCopy)) {
						if (!field.isReadOnly()) {
							undoModifs
									.add(new ModificationStack.SetFieldValueModification(
											ReflectionUI.this, object, field,
											fieldValueCopy));
						}
						refreshFieldControl(object, field.getName());
					}
				}
				if (undoModifs.size() > 0) {
					ModificationStack stack = getModificationStackByForm().get(
							form);
					stack.pushUndo(new ModificationStack.CompositeModification(
							ModificationStack.getUndoTitle("execution of '"
									+ method.getCaption() + "'"),
							ModificationStack.Order.FIFO, undoModifs));
				}

				return result;
			}

		};
	}

	public void refreshFieldControl(Object object, String fieldName) {
		ITypeInfo type = getTypeInfo(getTypeInfoSource(object));
		IFieldInfo field = ReflectionUIUtils.findInfoByName(type.getFields(),
				fieldName);
		if (field == null) {
			return;
		}
		for (JPanel form : ReflectionUIUtils.getKeysFromValue(
				getObjectByForm(), object)) {
			Map<String, FielControlPlaceHolder> controlPlaceHolderByFieldName = controlPlaceHolderByFieldNameByForm
					.get(form);
			FielControlPlaceHolder fieldControlPlaceHolder = controlPlaceHolderByFieldName
					.get(field.getName());
			fieldControlPlaceHolder.refreshUI();
		}
	}

	public void layoutControls(
			List<FielControlPlaceHolder> fielControlPlaceHolders,
			final List<Component> methodControls, JPanel parentForm) {
		parentForm.setLayout(new SimpleLayout(Kind.COLUMN));

		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new SimpleLayout(Kind.COLUMN));
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FielControlPlaceHolder fielControlPlaceHolder = fielControlPlaceHolders
					.get(i);
			fielControlPlaceHolder.showCaption();
			SimpleLayout.add(fieldsPanel, fielControlPlaceHolder);
		}

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

		SimpleLayout.add(parentForm, fieldsPanel);
		SimpleLayout.add(parentForm, methodsPanel);
	}

	public void setFieldControlCaption(Component fieldControl, String caption) {
		TitledBorder titledBorder;
		if (caption == null) {
			titledBorder = null;
		} else {
			titledBorder = BorderFactory
					.createTitledBorder(translateUIString(caption));
		}
		((JComponent) fieldControl).setBorder(titledBorder);
	}

	public void handleDisplayedUIExceptions(Component activatorComponent,
			final Throwable t) {
		t.printStackTrace();
		openExceptionDialog(activatorComponent,
				translateUIString("An Error Occured"), t);
	}

	public void openExceptionDialog(Component activatorComponent, String title,
			final Throwable t) {
		JTextArea textArea = new JTextArea(t.toString());
		textArea.setEditable(false);
		textArea.setMargin(new Insets(5, 5, 5, 5));
		textArea.setBorder(BorderFactory.createTitledBorder(""));
		Component errorComponent = new JOptionPane(textArea,
				JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] {});

		JDialog[] dialogArray = new JDialog[1];
		dialogArray[0] = createDialog(
				activatorComponent,
				errorComponent,
				title,
				null,
				new ArrayList<Component>(createDialogOkCancelButtons(
						dialogArray, null, null, null, false)), null);
		openDialog(dialogArray[0], true);

	}

	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		if (typeSource instanceof JavaTypeInfoSource) {
			JavaTypeInfoSource javaTypeSource = (JavaTypeInfoSource) typeSource;
			if (StandardCollectionTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				Class<?> itemType = ReflectionUIUtils.getJavaTypeParameter(
						javaTypeSource.getJavaType(),
						javaTypeSource.ofMember(), Collection.class, 0);
				return new StandardCollectionTypeInfo(this,
						javaTypeSource.getJavaType(), itemType);
			} else if (StandardMapListTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				Class<?> keyType = ReflectionUIUtils.getJavaTypeParameter(
						javaTypeSource.getJavaType(),
						javaTypeSource.ofMember(), Map.class, 0);
				Class<?> valueType = ReflectionUIUtils.getJavaTypeParameter(
						javaTypeSource.getJavaType(),
						javaTypeSource.ofMember(), Map.class, 1);
				return new StandardMapListTypeInfo(this,
						javaTypeSource.getJavaType(), keyType, valueType);
			} else if (javaTypeSource.getJavaType().isArray()) {
				Class<?> itemType = javaTypeSource.getJavaType()
						.getComponentType();
				return new ArrayTypeInfo(this, javaTypeSource.getJavaType(),
						itemType);
			} else if (javaTypeSource.getJavaType().isEnum()) {
				return new StandardEnumerationTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (DefaultBooleanTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				return new DefaultBooleanTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (DefaultTextualTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				return new DefaultTextualTypeInfo(this,
						javaTypeSource.getJavaType());
			} else if (FileTypeInfo.isCompatibleWith(javaTypeSource
					.getJavaType())) {
				return new FileTypeInfo(this);
			} else {
				return new DefaultTypeInfo(this, javaTypeSource.getJavaType());
			}
		} else if (typeSource instanceof PrecomputedTypeInfoSource) {
			return ((PrecomputedTypeInfoSource) typeSource)
					.getPrecomputedType();
		} else {
			throw new AssertionError();
		}
	}

	public Component createMethodControl(final Object object,
			final IMethodInfo method) {
		return new MethodControl(this, object, method);
	}

	public String getNullValueString() {
		return "<no result>";
	}

	public String getMethodReturnValueTitle(IMethodInfo method,
			Object returnValue) {
		return composeTitle(
				composeTitle(method.getCaption(), "Execution Result"),
				getObjectKind(returnValue));
	}

	public String composeTitle(String contextTitle, String localTitle) {
		if (contextTitle == null) {
			return localTitle;
		}
		return contextTitle + " - " + localTitle;
	}

	public void onMethodInvocationRequest(Component activatorComponent,
			final Object object, final IMethodInfo method) {
		Object returnValue;
		if (method.getParameters().size() > 0) {
			final Object[] returnValueArray = new Object[1];
			if (!openMethoExecutionSettingDialog(activatorComponent, object,
					method, returnValueArray)) {
				return;
			}
			returnValue = returnValueArray[0];
		} else {
			returnValue = method.invoke(object,
					Collections.<String, Object> emptyMap());
		}
		if (method.getReturnValueType() != null) {
			openMethodReturnValueFrame(activatorComponent, object, method,
					returnValue);
		}
	}

	public boolean openMethoExecutionSettingDialog(
			Component activatorComponent, final Object object,
			final IMethodInfo method, final Object[] returnValueArray) {
		JPanel methodPanel = new JPanel();

		List<FielControlPlaceHolder> paramControlPlaceHolders = new ArrayList<FielControlPlaceHolder>();
		final Map<String, Object> valueByParameterName = new HashMap<String, Object>();
		for (IParameterInfo param : method.getParameters()) {
			paramControlPlaceHolders.add(createFieldControlPlaceHolder(null,
					getParameterAsField(param, valueByParameterName), null));
		}

		layoutControls(paramControlPlaceHolders,
				Collections.<Component> emptyList(), methodPanel);

		final boolean[] invokedStatusArray = new boolean[] { false };
		final JDialog[] methodDialogArray = new JDialog[1];

		List<Component> toolbarControls = new ArrayList<Component>(
				createDialogOkCancelButtons(methodDialogArray,
						invokedStatusArray, method.getCaption(),
						new Runnable() {
							@Override
							public void run() {
								returnValueArray[0] = method.invoke(object,
										valueByParameterName);
							}

						}, true));

		methodDialogArray[0] = createDialog(activatorComponent, methodPanel,
				getMethodExecutionSettingTitle(object, method), null,
				toolbarControls, null);

		openDialog(methodDialogArray[0], true);
		return invokedStatusArray[0];
	}

	public String getMethodExecutionSettingTitle(Object object,
			IMethodInfo method) {
		String result = composeTitle(getObjectKind(object), method.getCaption());
		result = composeTitle(result, "Setting and Execution");
		return result;
	}

	public void openMethodReturnValueFrame(Component activatorComponent,
			Object object, IMethodInfo method, Object returnValue) {
		createMethodReturnValueFrame(activatorComponent, object, method,
				returnValue).setVisible(true);
	}

	public JFrame createMethodReturnValueFrame(Component activatorComponent,
			Object object, IMethodInfo method, final Object returnValue) {
		JPanel returnValueControl = createValueForm(
				new Object[] { returnValue }, IInfoCollectionSettings.DEFAULT);
		return createFrame(returnValueControl,
				getMethodReturnValueTitle(method, returnValue),
				getObjectIconImage(returnValue),
				createCommonToolbarControls(returnValueControl));

	}

	public IFieldInfo getParameterAsField(final IParameterInfo param,
			final Map<String, Object> valueByParameterName) {
		return new IFieldInfo() {

			@Override
			public void setValue(Object object, Object value) {
				valueByParameterName.put(param.getName(), value);
			}

			@Override
			public boolean isNullable() {
				return param.isNullable(); 
			}

			@Override
			public Object getValue(Object object) {
				Object result = valueByParameterName.get(param.getName());
				if(result == null){
					result = param.getDefaultValue();
				}
				return result;
			}

			@Override
			public ITypeInfo getType() {
				return param.getType();
			}

			@Override
			public String getCaption() {
				return param.getCaption();
			}

			@Override
			public boolean isReadOnly() {
				return false;
			}

			@Override
			public String getName() {
				return param.getName();
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}
		};
	}

	public void doCustomValidation(Object object, IFieldInfo field) {
	}

	public void openObjectDialog(Component parent, Object object, String title,
			Image iconImage, boolean modal) {
		JPanel form = createObjectForm(object);
		JDialog dialog = createDialog(parent, form, title, iconImage,
				createCommonToolbarControls(form), null);
		openDialog(dialog, modal);
	}

	public void openDialog(JDialog dialog, boolean modal) {
		if (modal) {
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.dispose();
		} else {
			dialog.setModalityType(ModalityType.MODELESS);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
	}

	public JDialog createDialog(Component ownerComponent, Component content,
			String title, Image iconImage, List<Component> toolbarControls,
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
						handleDisplayedUIExceptions(this, t);
					}
				}
				disposed = true;
			}
		};
		dialog.getContentPane().setLayout(new BorderLayout());
		if (content != null) {
			JScrollPane scrollPane = new JScrollPane(content);
			dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
		}
		if (toolbarControls != null) {
			JPanel toolbar = new JPanel();
			toolbar.setLayout(new FlowLayout(FlowLayout.CENTER));
			for (Component tool : toolbarControls) {
				toolbar.add(tool);
			}
			dialog.getContentPane().add(toolbar, BorderLayout.SOUTH);
		}
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(true);
		ReflectionUIUtils.adjustWindowBounds(dialog);
		if (iconImage == null) {
			dialog.setIconImage(new BufferedImage(1, 1,
					BufferedImage.TYPE_INT_ARGB));
		} else {
			dialog.setIconImage(iconImage);
		}
		return dialog;
	}

	public ReflectionUI getSubReflectionUI() {
		return this;
	}

	public String toString(Object object) {
		String result;
		if (object == null) {
			result = null;
		} else {
			result = object.toString();
		}
		return result;
	}

	public void markFieldControlWithError(Component c, String error) {
		if (error == null) {
			((JComponent) c).setBorder(null);
		} else {
			TitledBorder border = BorderFactory.createTitledBorder("");
			border.setTitleColor(Color.RED);
			border.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
			((JComponent) c).setToolTipText(error);
			ReflectionUIUtils.showTooltipNow(c);
			((JComponent) c).setBorder(border);
		}
	}

	public String translateUIString(String string) {
		return string;
	}

	public Object onTypeInstanciationRequest(ITypeInfo type,
			Component activatorComponent, boolean autoSelectConstructor) {
		List<ITypeInfo> polyTypes = type.getPolymorphicInstanceTypes();
		if ((polyTypes != null) && (polyTypes.size() > 0)) {
			if (polyTypes.size() == 1) {
				type = polyTypes.get(0);
			} else {
				type = openSelectionDialog(activatorComponent, polyTypes, null,
						"Choose the type of '" + type.getCaption() + "':", null);
				if (type == null) {
					return null;
				}
			}
		}
		IMethodInfo chosenConstructor = null;
		if (autoSelectConstructor) {
			chosenConstructor = ReflectionUIUtils
					.getZeroParameterConstrucor(type);
		}
		if (chosenConstructor == null) {
			List<IMethodInfo> constructors = type.getConstructors();
			if (constructors.size() > 1) {
				chosenConstructor = openSelectionDialog(activatorComponent,
						constructors, null, "Choose an option:", null);
				if (chosenConstructor == null) {
					return null;
				}
			} else {
				if (type.isConcrete()) {
					throw new AssertionError(
							"Cannot create an object of type '" + type
									+ "': No accessible constructor found");
				} else {
					type = openConcreteClassSelectionDialog(activatorComponent,
							type);
					if (type == null) {
						return null;
					} else {
						return onTypeInstanciationRequest(type,
								activatorComponent, autoSelectConstructor);
					}
				}
			}
		}
		Object newInstance;
		if (chosenConstructor.getParameters().size() > 0) {
			final Object[] returnValueArray = new Object[1];
			if (!openMethoExecutionSettingDialog(activatorComponent, null,
					chosenConstructor, returnValueArray)) {
				return null;
			}
			newInstance = returnValueArray[0];
		} else {
			newInstance = chosenConstructor.invoke(null,
					Collections.<String, Object> emptyMap());
		}
		return newInstance;
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
			throw new AssertionError(e);
		}
	}

	public String getObjectKind(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof VirtualItem) {
			return ((VirtualItem) object).toString();
		}
		if (object instanceof StandardMapEntry<?, ?>) {
			Object key = ((StandardMapEntry<?, ?>) object).getKey();
			return (key == null) ? null : key.toString();
		}
		return getTypeInfo(getTypeInfoSource(object)).getCaption();
	}

	public Image getObjectIconImage(Object item) {
		return null;
	}

	public boolean openValueDialog(Component activatorComponent,
			final Object object, Accessor<Object> valueAccessor,
			IInfoCollectionSettings settings,
			ModificationStack parentModificationStack, String title) {
		boolean[] okPressedArray = new boolean[] { false };
		JDialog dialog = createValueDialog(activatorComponent, object,
				valueAccessor, okPressedArray, settings,
				parentModificationStack, title);
		if (dialog == null) {
			return true;
		}
		openDialog(dialog, true);
		return okPressedArray[0];
	}

	public JDialog createValueDialog(final Component activatorComponent,
			final Object object, final Accessor<Object> valueAccessor,
			final boolean[] okPressedArray, IInfoCollectionSettings settings,
			final ModificationStack parentModificationStack, final String title) {

		final Object[] valueArray = new Object[] { valueAccessor.get() };
		final JPanel valueForm = createValueForm(valueArray, settings);
		if (valueForm == null) {
			return null;
		}

		final JDialog[] dialogArray = new JDialog[1];
		List<Component> toolbarControls = new ArrayList<Component>();
		Image iconImage = null;
		iconImage = getObjectIconImage(valueArray[0]);
		List<Component> commonToolbarControls = createCommonToolbarControls(valueForm);
		if (commonToolbarControls != null) {
			toolbarControls.addAll(commonToolbarControls);
		}
		Runnable whenClosingDialog = new Runnable() {
			@Override
			public void run() {
				if (okPressedArray[0]) {
					Object oldValue = valueAccessor.get();
					if (!oldValue.equals(valueArray[0])) {
						valueAccessor.set(valueArray[0]);
					} else {
						ModificationStack valueModifications = getModificationStackByForm()
								.get(valueForm);
						if (valueModifications != null) {
							List<IModification> undoModifications = new ArrayList<ModificationStack.IModification>();
							undoModifications
									.addAll(Arrays.asList(valueModifications
											.getUndoModifications(ModificationStack.Order.LIFO)));
							parentModificationStack
									.pushUndo(new ModificationStack.CompositeModification(
											ModificationStack
													.getUndoTitle(title),
											ModificationStack.Order.LIFO,
											undoModifications));
						}
					}
				} else {
					ModificationStack stack = getModificationStackByForm().get(
							valueForm);
					if (stack != null) {
						stack.undoAll(false);
					}
				}
			}
		};
		toolbarControls.addAll(createDialogOkCancelButtons(dialogArray,
				okPressedArray, "OK", null, true));

		dialogArray[0] = createDialog(activatorComponent, valueForm, title,
				iconImage, toolbarControls, whenClosingDialog);

		return dialogArray[0];
	}

	public List<JButton> createDialogOkCancelButtons(
			final JDialog[] dialogArray, final boolean[] okPressedArray,
			String okCaption, final Runnable okAction,
			boolean createCancelButton) {
		List<JButton> result = new ArrayList<JButton>();

		final JButton okButton = new JButton(
				translateUIString((okCaption != null) ? okCaption : "OK"));
		result.add(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (okPressedArray != null) {
						okPressedArray[0] = true;
					}
					if (okAction != null) {
						okAction.run();
					}
				} catch (Throwable t) {
					handleDisplayedUIExceptions(okButton, t);
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

	public JPanel createValueForm(final Object[] valueArray,
			final IInfoCollectionSettings settings) {
		final JPanel result;
		if (valueArray[0] == null) {
			result = new JPanel();
			result.setLayout(new BorderLayout());
			result.add(new JLabel(getNullValueString()), BorderLayout.CENTER);
		} else if (!getTypeInfo(getTypeInfoSource(valueArray[0]))
				.hasCustomFieldControl()) {
			result = getSubReflectionUI().createObjectForm(valueArray[0],
					settings);
		} else {
			IFieldInfo virtualField = new IFieldInfo() {

				@Override
				public void setValue(Object object, Object value) {
					valueArray[0] = value;
				}

				@Override
				public boolean isReadOnly() {
					return settings.allReadOnly();
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public Object getValue(Object object) {
					return valueArray[0];
				}

				@Override
				public ITypeInfo getType() {
					return getTypeInfo(getTypeInfoSource(valueArray[0]));
				}

				@Override
				public String getName() {
					return "";
				}

				@Override
				public String getCaption() {
					return "Value";
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}
			};
			Component fieldControl = virtualField.getType().createFieldControl(
					null, virtualField);
			result = new JPanel();
			result.setLayout(new BorderLayout());
			result.add(fieldControl, BorderLayout.CENTER);
		}
		return result;
	}

	public String getFieldTitle(Object object, IFieldInfo field) {
		return composeTitle(
				composeTitle(getObjectKind(object), field.getCaption()),
				getObjectKind(field.getValue(object)));
	}

	protected class FielControlPlaceHolder extends JPanel implements
			IRefreshableControl, ICanShowCaptionControl {

		protected static final long serialVersionUID = 1L;
		protected Object object;
		protected IFieldInfo field;
		protected Component fieldControl;
		protected boolean showCaption = true;

		public FielControlPlaceHolder(Object object, IFieldInfo field,
				JPanel form) {
			super();
			this.object = object;
			field = manageFieldValuesValidation(field);
			if ((form != null) && !field.isReadOnly()) {
				field = makeFieldModificationsUndoable(field, form);
			}
			this.field = field;
			setLayout(new BorderLayout());
			refreshUI();
		}

		public IFieldInfo getField() {
			return field;
		}

		public IFieldInfo manageFieldValuesValidation(final IFieldInfo field) {
			return new FieldInfoProxy(field) {

				@Override
				public void setValue(Object object, Object value) {
					try {
						field.setValue(object, value);
						doCustomValidation(object, field);
						markFieldControlWithError(FielControlPlaceHolder.this,
								null);
					} catch (Throwable t) {
						markFieldControlWithError(FielControlPlaceHolder.this,
								t.toString());
					}
				}
			};
		}

		@Override
		public void refreshUI() {
			if (fieldControl == null) {
				fieldControl = field.getType()
						.createFieldControl(object, field);
				add(fieldControl, BorderLayout.CENTER);
			} else {
				if (fieldControl instanceof IRefreshableControl) {
					((IRefreshableControl) fieldControl).refreshUI();
				} else {
					remove(fieldControl);
					fieldControl = null;
					refreshUI();
				}
			}
			if (showCaption) {
				if (fieldControl instanceof ICanShowCaptionControl) {
					((ICanShowCaptionControl) fieldControl).showCaption();
				} else {
					setFieldControlCaption(fieldControl, field.getCaption());
				}
			}

			ReflectionUIUtils.updateLayout(this);
		}

		@Override
		public void showCaption() {
			showCaption = true;
			refreshUI();
		}

	}

}
