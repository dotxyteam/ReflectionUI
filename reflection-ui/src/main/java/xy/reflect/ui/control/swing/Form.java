package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
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
import xy.reflect.ui.info.type.ITypeInfo.MethodsLayout;
import xy.reflect.ui.info.type.factory.FilteredTypeFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.Visitor;

public class Form extends JPanel {
	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Object object;
	protected Component statusBar;

	protected ModificationStack modificationStack;
	protected boolean fieldsUpdateListenerDisabled = false;
	protected IInfoFilter infoFilter;
	protected JMenuBar menuBar;
	protected SortedMap<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory;
	protected SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory;
	protected Map<FieldControlPlaceHolder, Component> captionControlByFieldControlPlaceHolder = new MapMaker()
			.weakKeys().makeMap();
	protected Map<Component, IFieldControlPlugin> pluginByFieldControl = new MapMaker().weakKeys().makeMap();
	protected Container categoriesControl;
	protected boolean busyIndicationDisabled;
	protected IModificationListener fieldsUpdateListener = createFieldsUpdateListener();
	protected boolean visibilityEventsDisabled = false;

	public Form(SwingRenderer swingRenderer, Object object, IInfoFilter infoFilter) {
		this.swingRenderer = swingRenderer;
		setObject(object);
		setInfoFilter(infoFilter);
		setModificationStack(new ModificationStack(toString()));
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				if(visibilityEventsDisabled) {
					return;
				}				
				formShown();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				if(visibilityEventsDisabled) {
					return;
				}				
				formHidden();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
		fillForm();
		menuBar = createMenuBar();
		statusBar = createStatusBar();
		setStatusBarErrorMessage(null);
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
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

	public IInfoFilter getInfoFilter() {
		return infoFilter;
	}

	public void setInfoFilter(IInfoFilter infoFilter) {
		this.infoFilter = infoFilter;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public Component getStatusBar() {
		return statusBar;
	}

	public Map<Component, IFieldControlPlugin> getPluginByFieldControl() {
		return pluginByFieldControl;
	}

	public SortedMap<InfoCategory, List<FieldControlPlaceHolder>> getFieldControlPlaceHoldersByCategory() {
		return fieldControlPlaceHoldersByCategory;
	}

	public SortedMap<InfoCategory, List<MethodControlPlaceHolder>> getMethodControlPlaceHoldersByCategory() {
		return methodControlPlaceHoldersByCategory;
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

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		Dimension size = type.getFormPreferredSize();
		if (size != null) {
			if (size.width > 0) {
				result.width = size.width;
			}
			if (size.height > 0) {
				result.height = size.height;
			}
		}
		return result;
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
		new Thread("Validator: " + object) {
			@Override
			public void run() {
				try {
					validateForm();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setStatusBarErrorMessage(null);
						}
					});
				} catch (Exception e) {
					final String errorMsg = new ReflectionUIError(e).toString();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setStatusBarErrorMessage(errorMsg);
						}
					});
				}

			}
		}.start();
	}

	public void setStatusBarErrorMessage(String errorMsg) {
		if (errorMsg == null) {
			statusBar.setVisible(false);
		} else {
			((JLabel) statusBar).setIcon(SwingRendererUtils.ERROR_ICON);
			statusBar.setBackground(new Color(255, 245, 242));
			statusBar.setForeground(new Color(255, 0, 0));
			((JLabel) statusBar).setText(ReflectionUIUtils.multiToSingleLine(errorMsg));
			SwingRendererUtils.setMultilineToolTipText(((JLabel) statusBar), errorMsg);
			statusBar.setVisible(true);
		}
		SwingRendererUtils.handleComponentSizeChange(statusBar);
	}

	public void formShown() {
		swingRenderer.getAllDisplayedForms().add(this);
		if (!isModificationStackSlave()) {
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
		if (!isModificationStackSlave()) {
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

	public boolean isModificationStackSlave() {
		return modificationStack instanceof SlaveModificationStack;
	}

	public IModificationListener createFieldsUpdateListener() {
		return new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				if (fieldsUpdateListenerDisabled) {
					return;
				}
				onFieldsUpdate();
			}
		};
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
		setLayout(new BorderLayout());

		JPanel membersPanel = createMembersPanel();
		{
			add(membersPanel, BorderLayout.CENTER);
			ITypeInfo type = getFormFilteredType();
			fieldControlPlaceHoldersByCategory = createFieldControlPlaceHoldersByCategory(type.getFields());
			methodControlPlaceHoldersByCategory = createMethodControlPlaceHoldersByCategory(type.getMethods());
			layoutMemberControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory, membersPanel);
		}
	}

	public JPanel createMembersPanel() {
		return new JPanel();
	}

	public void layoutMemberControls(
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
			JPanel membersPanel) {
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
			layoutCategoryControls(fieldControlPlaceHolders, methodControlPlaceHolders, membersPanel);
		} else if (allCategories.size() > 0) {
			membersPanel.setLayout(new BorderLayout());
			categoriesControl = createFormCategoriesControl(allCategories, fieldControlPlaceHoldersByCategory,
					methodControlPlaceHoldersByCategory);
			membersPanel.add(categoriesControl, BorderLayout.CENTER);
			Form form = SwingRendererUtils.findParentForm(categoriesControl, swingRenderer);
			if (form == null) {
				throw new ReflectionUIError();
			}
		}
	}

	public Component createStatusBar() {
		JLabel result = new JLabel();
		result.setOpaque(true);
		result.setFont(new JToolTip().getFont());
		result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		return result;
	}

	public JMenuBar createMenuBar() {
		JMenuBar result = new JMenuBar();
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
			result.addTab(swingRenderer.prepareStringToDisplay(category.getCaption()), tab);
			tab.setLayout(new BorderLayout());

			JPanel tabContent = new JPanel();
			tab.add(tabContent, BorderLayout.NORTH);
			layoutCategoryControls(fieldControlPlaceHolders, methodControlPlaceHolders, tabContent);
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

	public void layoutCategoryControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, JPanel membersPanel) {
		Container fieldsPanel = (fielControlPlaceHolders.size() == 0) ? null
				: createFieldsPanel(fielControlPlaceHolders);
		Container methodsPanel = (methodControlPlaceHolders.size() == 0) ? null
				: createMethodsPanel(methodControlPlaceHolders);
		layoutFormCategoryPanels(membersPanel, fieldsPanel, methodsPanel);
	}

	public Container createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
		JPanel result = new JPanel();
		for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
			result.add(methodControlPlaceHolder);
			updateMethodControlLayoutInContainer(methodControlPlaceHolder);
		}
		return new JScrollPane(SwingRendererUtils.flowInLayout(result, GridBagConstraints.CENTER)) {

			private static final long serialVersionUID = 1L;

			{
				setBorder(null);
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				if (getHorizontalScrollBar() != null) {
					result.height += getHorizontalScrollBar().getHeight();
				}
				if (getVerticalScrollBar() != null) {
					result.width += getVerticalScrollBar().getWidth();
				}
				return result;
			}

			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}

		};
	}

	public void refreshForm(boolean refreshStructure) {
		if (refreshStructure && refreshMemberControlLists()) {
			InfoCategory displayedCategory = getDisplayedInfoCategory();
			try {
				removeAll();
				layoutMemberControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory, this);
				SwingRendererUtils.handleComponentSizeChange(this);
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
						updateMethodControlLayoutInContainer(methodControlPlaceHolder);
					}
				}
			}
		}
		finalizeFormUpdate();
	}

	public boolean refreshMemberControlLists() {

		boolean modificationsDetected = false;

		ITypeInfo type = getFormFilteredType();

		SortedMap<InfoCategory, List<FieldControlPlaceHolder>> displayedFieldControlPlaceHoldersByCategory = fieldControlPlaceHoldersByCategory;
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> displayedMethodControlPlaceHoldersByCategory = methodControlPlaceHoldersByCategory;

		SortedMap<InfoCategory, List<FieldControlPlaceHolder>> newFieldControlPlaceHoldersByCategory = createFieldControlPlaceHoldersByCategory(
				type.getFields());
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> newMethodControlPlaceHoldersByCategory = createMethodControlPlaceHoldersByCategory(
				type.getMethods());

		if (!newFieldControlPlaceHoldersByCategory.keySet()
				.equals(displayedFieldControlPlaceHoldersByCategory.keySet())) {
			modificationsDetected = true;
		}

		if (!newMethodControlPlaceHoldersByCategory.keySet()
				.equals(displayedMethodControlPlaceHoldersByCategory.keySet())) {
			modificationsDetected = true;
		}

		if (!modificationsDetected) {
			for (InfoCategory category : newFieldControlPlaceHoldersByCategory.keySet()) {
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = newFieldControlPlaceHoldersByCategory
						.get(category);
				List<FieldControlPlaceHolder> displayedFieldControlPlaceHolders = displayedFieldControlPlaceHoldersByCategory
						.get(category);
				if (displayedFieldControlPlaceHolders.size() != fieldControlPlaceHolders.size()) {
					modificationsDetected = true;
					break;
				}
				for (int i = 0; i < fieldControlPlaceHolders.size(); i++) {
					FieldControlPlaceHolder fieldControlPlaceHolder = fieldControlPlaceHolders.get(i);
					FieldControlPlaceHolder displayedFieldControlPlaceHolder = displayedFieldControlPlaceHolders.get(i);
					if (!fieldControlPlaceHolder.getField().equals(displayedFieldControlPlaceHolder.getField())) {
						modificationsDetected = true;
						break;
					}
					if (!fieldControlPlaceHolder.getFieldControl().getClass()
							.equals(displayedFieldControlPlaceHolder.getFieldControl().getClass())) {
						modificationsDetected = true;
						break;
					}
				}
				if (modificationsDetected) {
					break;
				}
			}
		}

		if (!modificationsDetected) {
			for (InfoCategory category : newMethodControlPlaceHoldersByCategory.keySet()) {
				List<MethodControlPlaceHolder> methodControlPlaceHolders = newMethodControlPlaceHoldersByCategory
						.get(category);
				List<MethodControlPlaceHolder> displayedMethodControlPlaceHolders = displayedMethodControlPlaceHoldersByCategory
						.get(category);
				if (displayedMethodControlPlaceHolders.size() != methodControlPlaceHolders.size()) {
					modificationsDetected = true;
					break;
				}
				for (int i = 0; i < methodControlPlaceHolders.size(); i++) {
					MethodControlPlaceHolder methodControlPlaceHolder = methodControlPlaceHolders.get(i);
					MethodControlPlaceHolder displayedMethodControlPlaceHolder = displayedMethodControlPlaceHolders
							.get(i);
					if (!methodControlPlaceHolder.getMethod().equals(displayedMethodControlPlaceHolder.getMethod())) {
						modificationsDetected = true;
						break;
					}
					if (!methodControlPlaceHolder.getMethodControl().getClass()
							.equals(displayedMethodControlPlaceHolder.getMethodControl().getClass())) {
						modificationsDetected = true;
						break;
					}
				}
				if (modificationsDetected) {
					break;
				}
			}
		}

		if (modificationsDetected) {
			fieldControlPlaceHoldersByCategory = newFieldControlPlaceHoldersByCategory;
			methodControlPlaceHoldersByCategory = newMethodControlPlaceHoldersByCategory;
		}

		return modificationsDetected;
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
		if (menuBar.getParent() != null) {
			updateMenuBar();
		}
		if (statusBar.getParent() != null) {
			validateFormInBackgroundAndReportOnStatusBar();
		}
	}

	public void updateMenuBar() {
		MenuModel menuModel = new MenuModel();
		addMenuContribution(menuModel);
		SwingRendererUtils.updateMenubar(menuBar, menuModel, swingRenderer);
	}

	public void addMenuContribution(MenuModel menuModel) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
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
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		IFieldInfo field = fieldControlPlaceHolder.getField();
		ITypeInfo.FieldsLayout fieldsOrientation = type.getFieldsLayout();
		JPanel fieldsPanel = (JPanel) fieldControlPlaceHolder.getParent();
		int spacing = getLayoutSpacing();

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
		fieldControlPlaceHolderLayoutConstraints.weightx = field.getDisplayAreaHorizontalWeight();
		fieldControlPlaceHolderLayoutConstraints.weighty = field.getDisplayAreaVerticalWeight();
		fieldControlPlaceHolderLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldControlPlaceHolderLayoutConstraints.anchor = GridBagConstraints.NORTH;
		for (Form subForm : SwingRendererUtils.findDescendantForms(fieldControlPlaceHolder, swingRenderer)) {
			subForm.visibilityEventsDisabled = true;
		}
		try {
			fieldsPanel.remove(fieldControlPlaceHolder);
			fieldsPanel.add(fieldControlPlaceHolder, fieldControlPlaceHolderLayoutConstraints);
		} finally {
			for (Form subForm : SwingRendererUtils.findDescendantForms(fieldControlPlaceHolder, swingRenderer)) {
				subForm.visibilityEventsDisabled = false;
			}
		}

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

	public void updateMethodControlLayoutInContainer(MethodControlPlaceHolder methodControlPlaceHolder) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		MethodsLayout methodsOrientation = type.getMethodsLayout();
		JPanel methodsPanel = (JPanel) methodControlPlaceHolder.getParent();
		int spacing = getLayoutSpacing();
		GridLayout newLayout;
		if (methodsOrientation == MethodsLayout.HORIZONTAL_FLOW) {
			newLayout = new GridLayout(1, 0, spacing, spacing);
		} else if (methodsOrientation == MethodsLayout.VERTICAL_FLOW) {
			newLayout = new GridLayout(0, 1, spacing, spacing);
		} else {
			throw new ReflectionUIError();
		}
		if (methodsPanel.getLayout() instanceof GridLayout) {
			GridLayout oldLayout = (GridLayout) methodsPanel.getLayout();
			if ((oldLayout.getRows() == newLayout.getRows()) && (oldLayout.getColumns() == newLayout.getColumns())
					&& (oldLayout.getHgap() == newLayout.getHgap()) && (oldLayout.getVgap() == newLayout.getVgap())) {
				return;
			}
		}
		methodsPanel.setLayout(newLayout);
		SwingRendererUtils.handleComponentSizeChange(methodsPanel);
	}

	public Component createFieldOnlineHelpControl(String onlineHelp) {
		return SwingRendererUtils.createOnlineHelpControl(onlineHelp, swingRenderer);
	}

	public Component createToolBarOnlineHelpControl(String onlineHelp) {
		return SwingRendererUtils.createOnlineHelpControl(onlineHelp, swingRenderer);
	}

	public Component createSeparateFieldCaptionControl(String caption) {
		JLabel result = new JLabel(swingRenderer.prepareStringToDisplay(caption + ": "));
		return result;
	}

	public LayoutManager getFieldsPanelLayout() {
		return new GridBagLayout();
	}

	public int getLayoutSpacing() {
		return SwingRendererUtils.getStandardCharacterWidth(this) * 1;
	}

	public List<Component> createFormToolbarControls() {
		if (object == null) {
			return null;
		}
		List<Component> result = new ArrayList<Component>();
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if ((type.getOnlineHelp() != null) && (type.getOnlineHelp().length() > 0)) {
			result.add(createToolBarOnlineHelpControl(type.getOnlineHelp()));
		}
		if (type.isModificationStackAccessible()) {
			if (modificationStack != null) {
				result.addAll(new ModificationStackControls(modificationStack).create(swingRenderer));
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "Form [id=" + hashCode() + ", object=" + object + "]";
	}
}
