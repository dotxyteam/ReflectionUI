package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.MethodControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.menu.AbstractActionMenuItem;
import xy.reflect.ui.info.menu.IMenuElement;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.FilteredTypeFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.Visitor;
import xy.reflect.ui.util.component.WrapLayout;

public class Form extends JPanel {
	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Object object;
	protected JLabel statusBar;

	protected ModificationStack modificationStack;
	protected boolean fieldsUpdateListenerDisabled = false;
	protected boolean modificationStackForwardingStatus = false;
	protected IInfoFilter infoFilter;
	protected JMenuBar menuBar;
	protected SortedMap<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory;
	protected SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory;
	protected Map<FieldControlPlaceHolder, Component> captionControlByFieldControlPlaceHolder = new MapMaker()
			.weakKeys().makeMap();
	protected Map<Component, IFieldControlPlugin> pluginByFieldControl = new MapMaker().weakKeys().makeMap();
	protected Container categoriesControl;
	protected boolean busyIndicationDisabled;
	protected IModificationListener fieldsUpdateListener;

	public Form(SwingRenderer swingRenderer, Object object, IInfoFilter infoFilter) {
		this.swingRenderer = swingRenderer;
		setObject(object);
		setInfoFilter(infoFilter);
		setModificationStack(new ModificationStack(toString()));
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				formShown();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				formHidden();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
		fillForm();
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public JLabel getStatusBar() {
		return statusBar;
	}

	public void setStatusBar(JLabel statusBar) {
		this.statusBar = statusBar;
	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public void setSwingRenderer(SwingRenderer swingRenderer) {
		this.swingRenderer = swingRenderer;
	}

	public ModificationStack getModificationStack() {
		return modificationStack;
	}

	public void setModificationStack(ModificationStack modificationStack) {
		this.modificationStack = modificationStack;
	}

	public boolean isFieldsUpdateListenerDisabled() {
		return fieldsUpdateListenerDisabled;
	}

	public void setFieldsUpdateListenerDisabled(boolean fieldsUpdateListenerDisabled) {
		this.fieldsUpdateListenerDisabled = fieldsUpdateListenerDisabled;
	}

	public boolean isModificationStackForwardingStatus() {
		return modificationStackForwardingStatus;
	}

	public void setModificationStackForwardingStatus(boolean modificationStackForwardingStatus) {
		this.modificationStackForwardingStatus = modificationStackForwardingStatus;
	}

	public IInfoFilter getInfoFilter() {
		return infoFilter;
	}

	public void setInfoFilter(IInfoFilter infoFilter) {
		this.infoFilter = infoFilter;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public void setMenuBar(JMenuBar menuBar) {
		this.menuBar = menuBar;
	}

	public Map<Component, IFieldControlPlugin> getPluginByFieldControl() {
		return pluginByFieldControl;
	}

	public SortedMap<InfoCategory, List<FieldControlPlaceHolder>> getFieldControlPlaceHoldersByCategory() {
		return fieldControlPlaceHoldersByCategory;
	}

	public void setFieldControlPlaceHoldersByCategory(
			SortedMap<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory) {
		this.fieldControlPlaceHoldersByCategory = fieldControlPlaceHoldersByCategory;
	}

	public SortedMap<InfoCategory, List<MethodControlPlaceHolder>> getMethodControlPlaceHoldersByCategory() {
		return methodControlPlaceHoldersByCategory;
	}

	public void setMethodControlPlaceHoldersByCategory(
			SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory) {
		this.methodControlPlaceHoldersByCategory = methodControlPlaceHoldersByCategory;
	}

	public Map<FieldControlPlaceHolder, Component> getCaptionControlByFieldControlPlaceHolder() {
		return captionControlByFieldControlPlaceHolder;
	}

	public void setCaptionControlByFieldControlPlaceHolder(
			Map<FieldControlPlaceHolder, Component> captionControlByFieldControlPlaceHolder) {
		this.captionControlByFieldControlPlaceHolder = captionControlByFieldControlPlaceHolder;
	}

	public Container getCategoriesControl() {
		return categoriesControl;
	}

	public void setCategoriesControl(Container categoriesControl) {
		this.categoriesControl = categoriesControl;
	}

	public boolean isBusyIndicationDisabled() {
		return busyIndicationDisabled;
	}

	public void setBusyIndicationDisabled(boolean busyIndicationDisabled) {
		this.busyIndicationDisabled = busyIndicationDisabled;
	}

	public IModificationListener getFieldsUpdateListener() {
		return fieldsUpdateListener;
	}

	public void setFieldsUpdateListener(IModificationListener fieldsUpdateListener) {
		this.fieldsUpdateListener = fieldsUpdateListener;
	}

	public void validateForm() throws Exception {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		type.validate(object);

		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
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
	}

	public void validateFormInBackgroundAndReportOnStatusBar() {
		if (statusBar == null) {
			return;
		}
		new Thread("Validator: " + object) {
			@Override
			public void run() {
				try {
					validateForm();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							swingRenderer.setStatusBarErrorMessage(statusBar, null);
						}
					});
				} catch (Exception e) {
					final String errorMsg = new ReflectionUIError(e).toString();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							swingRenderer.setStatusBarErrorMessage(statusBar, errorMsg);
						}
					});
				}

			}
		}.start();
	}

	public void formShown() {
		swingRenderer.getAllDisplayedForms().add(this);
		if (!modificationStackForwardingStatus) {
			fieldsUpdateListener = new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					if (fieldsUpdateListenerDisabled) {
						return;
					}
					onFieldsUpdate();
				}
			};
			modificationStack.addListener(fieldsUpdateListener);
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		final boolean[] formUpdateNeeded = new boolean[] { false };
		swingRenderer.showBusyDialogWhile(this, new Runnable() {
			@Override
			public void run() {
				formUpdateNeeded[0] = type.onFormVisibilityChange(object, true);
			}
		}, swingRenderer.getObjectTitle(object) + " - Setting up...");
		if (formUpdateNeeded[0]) {
			refreshForm(false);
		}
	}

	public void formHidden() {
		if (!modificationStackForwardingStatus) {
			modificationStack.removeListener(fieldsUpdateListener);
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		swingRenderer.showBusyDialogWhile(this, new Runnable() {
			@Override
			public void run() {
				type.onFormVisibilityChange(object, false);
			}
		}, swingRenderer.getObjectTitle(object) + " - Cleaning up...");
		swingRenderer.getAllDisplayedForms().remove(this);
	}

	public void onFieldsUpdate() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				refreshForm(false);
				for (Form otherForm : SwingRendererUtils.findObjectDisplayedForms(object, swingRenderer)) {
					if (otherForm != Form.this) {
						ModificationStack otherModifStack = otherForm.getModificationStack();
						if (otherForm.isDisplayable()) {
							otherForm.setFieldsUpdateListenerDisabled(true);
							otherModifStack.invalidate();
							otherForm.setFieldsUpdateListenerDisabled(false);
						}
					}
				}
			}
		});
	}

	public void fillForm() {
		ITypeInfo type = getFormFilteredType();

		fieldControlPlaceHoldersByCategory = createFieldControlPlaceHoldersByCategory(type.getFields());
		methodControlPlaceHoldersByCategory = createMethodControlPlaceHoldersByCategory(type.getMethods());

		layoutFormControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory, this);
		SwingRendererUtils.handleComponentSizeChange(this);
	}

	public void layoutFormControls(Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
			Container container) {
		SortedSet<InfoCategory> allCategories = new TreeSet<InfoCategory>();
		allCategories.addAll(fieldControlPlaceHoldersByCategory.keySet());
		allCategories.addAll(methodControlPlaceHoldersByCategory.keySet());
		if ((allCategories.size() == 1)
				&& (swingRenderer.getNullInfoCategory().equals(allCategories.iterator().next()))) {
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
			categoriesControl = createFormCategoriesControl(allCategories, fieldControlPlaceHoldersByCategory,
					methodControlPlaceHoldersByCategory);
			container.add(categoriesControl, BorderLayout.CENTER);
			Form form = SwingRendererUtils.findParentForm(categoriesControl, swingRenderer);
			if (form == null) {
				throw new ReflectionUIError();
			}
		}
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
			result.addTab(swingRenderer.prepareStringToDisplay(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutFormCategoryControls(fieldControlPlaceHolders, methodControlPlaceHolders, tabContent);
		}
		return result;
	}

	public SortedMap<InfoCategory, List<MethodControlPlaceHolder>> createMethodControlPlaceHoldersByCategory(
			List<IMethodInfo> methods) {
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> result = new TreeMap<InfoCategory, List<MethodControlPlaceHolder>>();
		for (IMethodInfo method : methods) {
			MethodControlPlaceHolder methodControlPlaceHolder = createMethodControlPlaceHolder(method);
			{
				InfoCategory category = method.getCategory();
				if (category == null) {
					category = swingRenderer.getNullInfoCategory();
				}
				List<MethodControlPlaceHolder> methodControlPlaceHolders = result.get(category);
				if (methodControlPlaceHolders == null) {
					methodControlPlaceHolders = new ArrayList<MethodControlPlaceHolder>();
					result.put(category, methodControlPlaceHolders);
				}
				methodControlPlaceHolders.add(methodControlPlaceHolder);
			}
		}
		return result;
	}

	public SortedMap<InfoCategory, List<FieldControlPlaceHolder>> createFieldControlPlaceHoldersByCategory(
			List<IFieldInfo> fields) {
		SortedMap<InfoCategory, List<FieldControlPlaceHolder>> result = new TreeMap<InfoCategory, List<FieldControlPlaceHolder>>();
		for (IFieldInfo field : fields) {
			FieldControlPlaceHolder fieldControlPlaceHolder = createFieldControlPlaceHolder(field);
			{
				InfoCategory category = field.getCategory();
				if (category == null) {
					category = swingRenderer.getNullInfoCategory();
				}
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = result.get(category);
				if (fieldControlPlaceHolders == null) {
					fieldControlPlaceHolders = new ArrayList<FieldControlPlaceHolder>();
					result.put(category, fieldControlPlaceHolders);
				}
				fieldControlPlaceHolders.add(fieldControlPlaceHolder);
			}
		}
		return result;
	}

	public ITypeInfo getFormFilteredType() {
		IInfoFilter infoFilter = this.infoFilter;
		if (infoFilter == null) {
			infoFilter = IInfoFilter.DEFAULT;
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo rawType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		infoFilter = new InfoFilterProxy(infoFilter) {

			@Override
			public boolean excludeField(IFieldInfo field) {
				if (field.isHidden()) {
					return true;
				}
				return super.excludeField(field);
			}

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				if (method.isHidden()) {
					return true;
				}
				return super.excludeMethod(method);
			}

		};
		ITypeInfo result = new FilteredTypeFactory(infoFilter) {

			List<IFieldInfo> fields;
			List<IMethodInfo> methods;
			MenuModel menuModel;
			{
				try {
					fields = super.getFields(rawType);
					methods = super.getMethods(rawType);
					menuModel = super.getMenuModel(rawType);
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
					menuModel = new MenuModel();
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

			@Override
			protected MenuModel getMenuModel(ITypeInfo type) {
				return menuModel;
			}

		}.wrapType(rawType);
		return result;
	}

	public MethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
		return new MethodControlPlaceHolder(swingRenderer, this, method);
	}

	public FieldControlPlaceHolder getFieldControlPlaceHolder(String fieldName) {
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				if (fieldName.equals(fieldControlPlaceHolder.getModificationsTarget().getName())) {
					return fieldControlPlaceHolder;
				}
			}
		}
		return null;
	}

	public MethodControlPlaceHolder getMethodControlPlaceHolder(String methodSignature) {
		for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
			for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHoldersByCategory
					.get(category)) {
				if (methodControlPlaceHolder.getMethod().getSignature().equals(methodSignature)) {
					return methodControlPlaceHolder;
				}
			}
		}
		return null;
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

	public Container createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
		JPanel result = new JPanel();
		int spacing = getLayoutSpacing(result);
		result.setLayout(new WrapLayout(WrapLayout.CENTER, spacing, spacing));
		for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
			result.add(methodControlPlaceHolder);
		}
		return result;
	}

	public void refreshForm(boolean refreshStructure) {
		boolean needsToRecreateControls = false;

		ITypeInfo type = getFormFilteredType();
		if (refreshStructure) {

			SortedMap<InfoCategory, List<FieldControlPlaceHolder>> displayedFieldControlPlaceHoldersByCategory = fieldControlPlaceHoldersByCategory;
			SortedMap<InfoCategory, List<MethodControlPlaceHolder>> displayedMethodControlPlaceHoldersByCategory = methodControlPlaceHoldersByCategory;

			SortedMap<InfoCategory, List<FieldControlPlaceHolder>> newFieldControlPlaceHoldersByCategory = createFieldControlPlaceHoldersByCategory(
					type.getFields());
			SortedMap<InfoCategory, List<MethodControlPlaceHolder>> newMethodControlPlaceHoldersByCategory = createMethodControlPlaceHoldersByCategory(
					type.getMethods());

			if (!newFieldControlPlaceHoldersByCategory.keySet()
					.equals(displayedFieldControlPlaceHoldersByCategory.keySet())) {
				needsToRecreateControls = true;
			}

			if (!newMethodControlPlaceHoldersByCategory.keySet()
					.equals(displayedMethodControlPlaceHoldersByCategory.keySet())) {
				needsToRecreateControls = true;
			}

			if (!needsToRecreateControls) {
				for (InfoCategory category : newFieldControlPlaceHoldersByCategory.keySet()) {
					List<FieldControlPlaceHolder> fieldControlPlaceHolders = newFieldControlPlaceHoldersByCategory
							.get(category);
					List<FieldControlPlaceHolder> displayedFieldControlPlaceHolders = displayedFieldControlPlaceHoldersByCategory
							.get(category);
					if (displayedFieldControlPlaceHolders.size() != fieldControlPlaceHolders.size()) {
						needsToRecreateControls = true;
						break;
					}
					for (int i = 0; i < fieldControlPlaceHolders.size(); i++) {
						FieldControlPlaceHolder fieldControlPlaceHolder = fieldControlPlaceHolders.get(i);
						FieldControlPlaceHolder displayedFieldControlPlaceHolder = displayedFieldControlPlaceHolders
								.get(i);
						if (!fieldControlPlaceHolder.getField().equals(displayedFieldControlPlaceHolder.getField())) {
							needsToRecreateControls = true;
							break;
						}
					}
					if (needsToRecreateControls) {
						break;
					}
				}
			}

			if (!needsToRecreateControls) {
				for (InfoCategory category : newMethodControlPlaceHoldersByCategory.keySet()) {
					List<MethodControlPlaceHolder> methodControlPlaceHolders = newMethodControlPlaceHoldersByCategory
							.get(category);
					List<MethodControlPlaceHolder> displayedMethodControlPlaceHolders = displayedMethodControlPlaceHoldersByCategory
							.get(category);
					if (displayedMethodControlPlaceHolders.size() != methodControlPlaceHolders.size()) {
						needsToRecreateControls = true;
						break;
					}
					for (int i = 0; i < methodControlPlaceHolders.size(); i++) {
						MethodControlPlaceHolder methodControlPlaceHolder = methodControlPlaceHolders.get(i);
						MethodControlPlaceHolder displayedMethodControlPlaceHolder = displayedMethodControlPlaceHolders
								.get(i);
						if (!methodControlPlaceHolder.getMethod()
								.equals(displayedMethodControlPlaceHolder.getMethod())) {
							needsToRecreateControls = true;
							break;
						}
					}
					if (needsToRecreateControls) {
						break;
					}
				}
			}

			if (needsToRecreateControls) {
				fieldControlPlaceHoldersByCategory = newFieldControlPlaceHoldersByCategory;
				methodControlPlaceHoldersByCategory = newMethodControlPlaceHoldersByCategory;
			}

		}

		if (needsToRecreateControls) {
			InfoCategory displayedCategory = getDisplayedInfoCategory();
			try {
				removeAll();
				layoutFormControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory, this);
				finalizeFormUpdate();
			} finally {
				if (displayedCategory != null) {
					setDisplayedInfoCategory(displayedCategory);
				}
			}
		} else {
			for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
						.get(category);
				for (int i = 0; i < fieldControlPlaceHolders.size(); i++) {
					FieldControlPlaceHolder fieldControlPlaceHolder = fieldControlPlaceHolders.get(i);
					fieldControlPlaceHolder.refreshUI(refreshStructure);
					if (refreshStructure) {
						updateFieldControlLayoutInContainer(fieldControlPlaceHolder);
					}
				}
			}
			if (refreshStructure) {
				for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
					List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
							.get(category);
					for (int i = 0; i < methodControlPlaceHolders.size(); i++) {
						MethodControlPlaceHolder methodControlPlaceHolder = methodControlPlaceHolders.get(i);
						methodControlPlaceHolder.refreshUI();
					}
				}
			}
		}

	}

	public void setDisplayedInfoCategory(String categoryCaption, int categoryPosition) {
		if (categoriesControl != null) {
			for (int i = 0; i < ((JTabbedPane) categoriesControl).getTabCount(); i++) {
				if (categoryCaption.equals(((JTabbedPane) categoriesControl).getTitleAt(i))) {
					if (categoryPosition != -1) {
						if (categoryPosition != i) {
							continue;
						}
					}
					((JTabbedPane) categoriesControl).setSelectedIndex(i);
					return;
				}
			}
		}
	}

	public void setDisplayedInfoCategory(InfoCategory category) {
		setDisplayedInfoCategory(category.getCaption(), category.getPosition());
	}

	public InfoCategory getDisplayedInfoCategory() {
		if (categoriesControl != null) {
			int currentCategoryIndex = ((JTabbedPane) categoriesControl).getSelectedIndex();
			if (currentCategoryIndex != -1) {
				String currentCategoryCaption = ((JTabbedPane) categoriesControl).getTitleAt(currentCategoryIndex);
				return new InfoCategory(currentCategoryCaption, currentCategoryIndex);
			}
		}
		return null;
	}

	public void finalizeFormUpdate() {
		updateMenuBar();
		SwingRendererUtils.handleComponentSizeChange(this);
		validateFormInBackgroundAndReportOnStatusBar();
	}

	public void updateMenuBar() {
		if (menuBar == null) {
			return;
		}
		MenuModel menuModel = new MenuModel();
		addMenuContribution(menuModel);
		SwingRendererUtils.updateMenubar(menuBar, menuModel, swingRenderer);
	}

	public void addMenuContribution(MenuModel menuModel) {
		ITypeInfo type = getFormFilteredType();
		MenuModel formMenuModel = type.getMenuModel();
		formMenuModel.visit(new Visitor<IMenuElement>() {
			@Override
			public boolean visit(IMenuElement e) {
				if (e instanceof AbstractActionMenuItem) {
					AbstractActionMenuItem action = (AbstractActionMenuItem) e;
					swingRenderer.getFormByActionMenuItem().put(action, Form.this);
				}
				return true;
			}
		});
		menuModel.importContributions(formMenuModel);
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				Component fieldControl = fieldControlPlaceHolder.getFieldControl();
				if (fieldControl instanceof IAdvancedFieldControl) {
					((IAdvancedFieldControl) fieldControl).addMenuContribution(menuModel);
				}
			}
		}
	}

	public boolean requestFormFocus() {
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				return SwingRendererUtils.requestAnyComponentFocus(fieldControlPlaceHolder.getFieldControl(),
						swingRenderer);
			}
		}
		return false;
	}

	public FieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
		return new FieldControlPlaceHolder(swingRenderer, this, field);
	}

	public Container createFieldsPanel(List<FieldControlPlaceHolder> fielControlPlaceHolders) {
		JPanel fieldsPanel = new JPanel();
		fieldsPanel.setLayout(getFieldsPanelLayout());
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FieldControlPlaceHolder fieldControlPlaceHolder = fielControlPlaceHolders.get(i);
			{
				fieldsPanel.add(fieldControlPlaceHolder);
				fieldControlPlaceHolder.setLayoutInContainerUpdateNeeded(true);
				fieldControlPlaceHolder.setPositionInContainer(i);
				updateFieldControlLayoutInContainer(fieldControlPlaceHolder);
			}
		}
		return fieldsPanel;
	}

	public void updateFieldControlLayoutInContainer(FieldControlPlaceHolder fieldControlPlaceHolder) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(fieldControlPlaceHolder.getObject()));
		ITypeInfo.FieldsLayout fieldsOrientation = type.getFieldsLayout();
		JPanel fieldsPanel = (JPanel) fieldControlPlaceHolder.getParent();
		int spacing = getLayoutSpacing(fieldsPanel);

		Component captionControl = getCaptionControlByFieldControlPlaceHolder().get(fieldControlPlaceHolder);
		if (captionControl != null) {
			fieldsPanel.remove(captionControl);
			getCaptionControlByFieldControlPlaceHolder().remove(fieldControlPlaceHolder);
		}
		boolean shouldHaveSeparateCaptionControl = !fieldControlPlaceHolder.showsCaption()
				&& (fieldControlPlaceHolder.getField().getCaption().length() > 0);
		if (shouldHaveSeparateCaptionControl) {
			captionControl = createSeparateFieldCaptionControl(fieldControlPlaceHolder.getField().getCaption());
			GridBagConstraints captionControlLayoutConstraints = new GridBagConstraints();
			captionControlLayoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
				captionControlLayoutConstraints.gridx = 0;
				captionControlLayoutConstraints.gridy = fieldControlPlaceHolder.getPositionInContainer();
			} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
				captionControlLayoutConstraints.gridy = 0;
				captionControlLayoutConstraints.gridx = fieldControlPlaceHolder.getPositionInContainer();
			} else {
				throw new ReflectionUIError();
			}
			captionControlLayoutConstraints.weightx = 0.0;
			captionControlLayoutConstraints.weighty = 1.0;
			captionControlLayoutConstraints.anchor = GridBagConstraints.WEST;
			fieldsPanel.add(captionControl, captionControlLayoutConstraints);
			getCaptionControlByFieldControlPlaceHolder().put(fieldControlPlaceHolder, captionControl);
		}

		GridBagConstraints fieldControlPlaceHolderLayoutConstraints = new GridBagConstraints();
		fieldControlPlaceHolderLayoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
		if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
			if (!shouldHaveSeparateCaptionControl) {
				fieldControlPlaceHolderLayoutConstraints.gridwidth = 2;
				fieldControlPlaceHolderLayoutConstraints.gridx = 0;
			} else {
				fieldControlPlaceHolderLayoutConstraints.gridx = 1;
			}
			fieldControlPlaceHolderLayoutConstraints.gridy = fieldControlPlaceHolder.getPositionInContainer();
		} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
			if (!shouldHaveSeparateCaptionControl) {
				fieldControlPlaceHolderLayoutConstraints.gridheight = 2;
				fieldControlPlaceHolderLayoutConstraints.gridy = 0;
			} else {
				fieldControlPlaceHolderLayoutConstraints.gridy = 1;
			}
			fieldControlPlaceHolderLayoutConstraints.gridx = fieldControlPlaceHolder.getPositionInContainer();
		} else {
			throw new ReflectionUIError();
		}
		fieldControlPlaceHolderLayoutConstraints.weightx = 1.0;
		fieldControlPlaceHolderLayoutConstraints.weighty = 1.0;
		fieldControlPlaceHolderLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldControlPlaceHolderLayoutConstraints.anchor = GridBagConstraints.NORTH;
		fieldsPanel.remove(fieldControlPlaceHolder);
		fieldsPanel.add(fieldControlPlaceHolder, fieldControlPlaceHolderLayoutConstraints);

		IFieldInfo field = fieldControlPlaceHolder.getField();
		if ((field.getOnlineHelp() != null) && (field.getOnlineHelp().length() > 0)) {
			GridBagConstraints layoutConstraints = new GridBagConstraints();
			layoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
				layoutConstraints.gridx = 2;
				layoutConstraints.gridy = fieldControlPlaceHolder.getPositionInContainer();
				layoutConstraints.weighty = 1.0;
			} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
				layoutConstraints.gridy = 2;
				layoutConstraints.gridx = fieldControlPlaceHolder.getPositionInContainer();
				layoutConstraints.weightx = 1.0;
			} else {
				throw new ReflectionUIError();
			}
			fieldsPanel.add(createFieldOnlineHelpControl(field.getOnlineHelp()), layoutConstraints);
		}

		SwingRendererUtils.handleComponentSizeChange(fieldsPanel);

		fieldControlPlaceHolder.setLayoutInContainerUpdateNeeded(false);

	}

	public Component createFieldOnlineHelpControl(String onlineHelp) {
		return SwingRendererUtils.createOnlineHelpControl(onlineHelp, swingRenderer);
	}

	public Component createSeparateFieldCaptionControl(String caption) {
		JLabel result = new JLabel(swingRenderer.prepareStringToDisplay(caption + ": "));
		return result;
	}

	public LayoutManager getFieldsPanelLayout() {
		return new GridBagLayout();
	}

	public int getLayoutSpacing(JPanel fieldsPanel) {
		return SwingRendererUtils.getStandardCharacterWidth(fieldsPanel) * 1;
	}

	public Component createFieldErrorControl(final Throwable t) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		reflectionUI.logError(t);
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		result.add(new NullControl(swingRenderer, new FieldControlInputProxy(IFieldControlInput.NULL_CONTROL_INPUT) {
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
			for (IFieldControlPlugin plugin : swingRenderer.getFieldControlPlugins()) {
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
				return new EnumerationControl(swingRenderer, input);
			}
			if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(input.getControlData().getType())) {
				return new PolymorphicControl(swingRenderer, input);
			}
			if (!input.getControlData().isNullValueDistinct()) {
				ITypeInfo fieldType = input.getControlData().getType();
				if (fieldType instanceof IListTypeInfo) {
					return new ListControl(swingRenderer, input);
				}
				final Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(fieldType.getName());
				} catch (ClassNotFoundException e) {
					return null;
				}
				if (boolean.class.equals(javaType) || Boolean.class.equals(javaType)) {
					return new CheckBoxControl(swingRenderer, input);
				}
				if (ClassUtils.isPrimitiveClassOrWrapper(javaType)) {
					return new PrimitiveValueControl(swingRenderer, input);
				}
				if (String.class.equals(javaType)) {
					return new TextControl(swingRenderer, input);
				}
			}
		}

		if (currentPlugin == null) {
			if (!IFieldControlPlugin.NONE_IDENTIFIER.equals(chosenPluginId)) {
				for (IFieldControlPlugin plugin : swingRenderer.getFieldControlPlugins()) {
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
				result = currentPlugin.createControl(swingRenderer, input);
			} catch (Throwable t) {
				result = createFieldErrorControl(t);
			}
			getPluginByFieldControl().put(result, currentPlugin);
			return result;
		}

		return null;
	}

	public Component createCustomMethodControl(IMethodControlInput input) {
		return null;
	}

	@Override
	public String toString() {
		return "Form [id=" + hashCode() + ", object=" + object + "]";
	}
}
