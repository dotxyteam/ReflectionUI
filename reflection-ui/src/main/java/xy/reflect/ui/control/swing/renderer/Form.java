
package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.swing.menu.Menu;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.ImagePanel;
import xy.reflect.ui.control.swing.util.ListTabbedPane;
import xy.reflect.ui.control.swing.util.ModificationStackControls;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.menu.AbstractActionMenuItemInfo;
import xy.reflect.ui.info.menu.IMenuElementInfo;
import xy.reflect.ui.info.menu.MenuInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.MethodsLayout;
import xy.reflect.ui.info.type.factory.FilteredTypeFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Instances of this class are forms allowing to edit any object. The layout and
 * the controls are generated according to type information ({@link ITypeInfo})
 * extracted form the underlying object.
 * 
 * A {@link ModificationStack} is used to record and revert/replay
 * modifications. It is also used as modification event producer in order to
 * refresh and then keep the field controls up to date when a modification is
 * detected.
 * 
 * @author olitank
 *
 */
public class Form extends ImagePanel {

	private static final long serialVersionUID = 1L;

	public static final String ACTION_MENU_ITEM_CONTEXT_FORM = Form.class.getName() + ".actionMenuItemContextForm";

	protected SwingRenderer swingRenderer;
	protected Object object;
	protected ITypeInfo objectType;
	protected ModificationStack modificationStack;
	protected boolean fieldsUpdateListenerDisabled = false;
	protected IInfoFilter infoFilter;
	protected SortedMap<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<FieldControlPlaceHolder>>();
	protected SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<MethodControlPlaceHolder>>();

	protected Container categoriesControl;
	protected IModificationListener fieldsUpdateListener = createFieldsUpdateListener();
	protected boolean visibilityEventsDisabled = false;
	protected List<IRefreshListener> refreshListeners = new ArrayList<IRefreshListener>();
	protected JLabel statusBar;
	protected JMenuBar menuBar;

	/**
	 * Creates a form allowing to view/edit the given object.
	 * 
	 * @param swingRenderer The renderer used to generate this form controls.
	 * @param object        The object that will be viewed/edited through this form.
	 * @param infoFilter    A filter that will be used to exclude some
	 *                      fields/methods of the object.
	 */
	public Form(SwingRenderer swingRenderer, Object object, IInfoFilter infoFilter) {
		this.swingRenderer = swingRenderer;
		setObject(object);
		setInfoFilter(infoFilter);
		setModificationStack(new ModificationStack(null));
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				if (visibilityEventsDisabled) {
					return;
				}
				try {
					formShown();
				} catch (Throwable t) {
					Form.this.swingRenderer.handleObjectException(Form.this, t);
				}
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				if (visibilityEventsDisabled) {
					return;
				}
				try {
					formHidden();
				} catch (Throwable t) {
					Form.this.swingRenderer.handleObjectException(Form.this, t);
				}
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
		menuBar = createMenuBar();
		statusBar = createStatusBar();
		setStatusBarErrorMessage(null);
		refresh(true);
	}

	/**
	 * @return The object that is viewed/edited through this form.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Changes the object that is viewed/edited through this form. Note that this
	 * method does not refresh the form. {@link #refresh(boolean)} should be called
	 * after to actually display the new object values.
	 * 
	 * @param object The new object.
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * @return the renderer used to generate this form controls.
	 */
	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	/**
	 * @return the modification stack associated with this form.
	 */
	public ModificationStack getModificationStack() {
		return modificationStack;
	}

	/**
	 * Changes the modification stack associated with this form.
	 * 
	 * @param modificationStack The new modification stack.
	 */
	public void setModificationStack(ModificationStack modificationStack) {
		this.modificationStack = modificationStack;
	}

	/**
	 * @return whether the listener that updates the form (when a modifications is
	 *         detected) is enabled or not.
	 */
	public boolean isFieldsUpdateListenerDisabled() {
		return fieldsUpdateListenerDisabled;
	}

	/**
	 * Updates whether the listener that updates the form (when a modifications is
	 * detected) is enabled or not.
	 * 
	 * @param fieldsUpdateListenerDisabled The new listener status.
	 */
	public void setFieldsUpdateListenerDisabled(boolean fieldsUpdateListenerDisabled) {
		this.fieldsUpdateListenerDisabled = fieldsUpdateListenerDisabled;
	}

	/**
	 * @return the filter that is used to exclude some fields/methods of the object.
	 */
	public IInfoFilter getInfoFilter() {
		return infoFilter;
	}

	/**
	 * Changes the filter that is used to exclude some fields/methods of the object.
	 * Note that this method does not refresh the form. {@link #refresh(boolean)}
	 * should be called with the parameter refreshStructure=true after to actually
	 * apply the new filter.
	 * 
	 * @param infoFilter The new filter.
	 */
	public void setInfoFilter(IInfoFilter infoFilter) {
		this.infoFilter = infoFilter;
	}

	/**
	 * @return the menu bar component.
	 */
	public JMenuBar getMenuBar() {
		return menuBar;
	}

	/**
	 * @return the status bar component.
	 */
	public JLabel getStatusBar() {
		return statusBar;
	}

	/**
	 * @return the map the current field control place holders by category.
	 */
	public SortedMap<InfoCategory, List<FieldControlPlaceHolder>> getFieldControlPlaceHoldersByCategory() {
		return fieldControlPlaceHoldersByCategory;
	}

	/**
	 * @return the map the current method control place holders by category.
	 */
	public SortedMap<InfoCategory, List<MethodControlPlaceHolder>> getMethodControlPlaceHoldersByCategory() {
		return methodControlPlaceHoldersByCategory;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			result = new Dimension(100, 100);
		} else {
			int screenWidth = SwingRendererUtils.getScreenBounds(this).width;
			if (result.width > screenWidth) {
				result.width = screenWidth;
			}
		}
		if (objectType != null) {
			Dimension configuredSize = objectType.getFormPreferredSize();
			if (configuredSize != null) {
				if (configuredSize.width > 0) {
					result.width = configuredSize.width;
				}
				if (configuredSize.height > 0) {
					result.height = configuredSize.height;
				}
			}
		}
		return result;
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * Uses type information to check that the state of the underlying object is
	 * valid.
	 * 
	 * @throws Exception If the state of the underlying object is not valid.
	 */
	public void validateForm() throws Exception {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		type.validate(object);
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				Component fieldControl = fieldControlPlaceHolder.getFieldControl();
				if (fieldControl instanceof IAdvancedFieldControl) {
					try {
						((IAdvancedFieldControl) fieldControl).validateSubForms();
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

	/**
	 * Runs {@link #validate()} asynchronously and updates the status bar
	 * accordingly (displays the validation error message if the object state is not
	 * valid).
	 */
	public void validateFormInBackgroundAndReportOnStatusBar() {
		swingRenderer.getFormValidator().submit(new Runnable() {
			@Override
			public void run() {
				try {
					validateForm();
					if (getStatusBarMessage() != null) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setStatusBarErrorMessage(null);
							}
						});
					}
				} catch (Exception e) {
					final String errorMsg = MiscUtils.getPrettyErrorMessage(e);
					if (!errorMsg.equals(getStatusBarMessage())) {
						swingRenderer.getReflectionUI().logDebug(e);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setStatusBarErrorMessage(errorMsg);
							}
						});
					}
				}
			}
		});
	}

	protected void setStatusBarErrorMessage(String errorMsg) {
		if (errorMsg == null) {
			((JLabel) statusBar).setIcon(null);
			((JLabel) statusBar).setText(null);
			statusBar.setVisible(false);
		} else {
			((JLabel) statusBar).setIcon(SwingRendererUtils.ERROR_ICON);
			((JLabel) statusBar).setText(MiscUtils.multiToSingleLine(errorMsg));
			SwingRendererUtils.setMultilineToolTipText(((JLabel) statusBar), errorMsg);
			statusBar.setVisible(true);
		}
		SwingRendererUtils.handleComponentSizeChange(statusBar);
	}

	protected String getStatusBarMessage() {
		return ((JLabel) statusBar).getText();
	}

	protected void formShown() {
		swingRenderer.getAllDisplayedForms().add(this);
		if (!isModificationStackSlave()) {
			modificationStack.addListener(fieldsUpdateListener);
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		final boolean[] formUpdateNeeded = new boolean[] { false };
		swingRenderer.showBusyDialogWhile(this, new Runnable() {
			@Override
			public void run() {
				formUpdateNeeded[0] = type.onFormVisibilityChange(object, true);
			}
		}, swingRenderer.getObjectTitle(object) + " - Setting up...");
		if (formUpdateNeeded[0]) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					refresh(false);
				}
			});
		}
	}

	protected void formHidden() {
		if (!isModificationStackSlave()) {
			modificationStack.removeListener(fieldsUpdateListener);
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		final boolean[] formUpdateNeeded = new boolean[] { false };
		swingRenderer.showBusyDialogWhile(this, new Runnable() {
			@Override
			public void run() {
				formUpdateNeeded[0] = type.onFormVisibilityChange(object, false);
			}
		}, swingRenderer.getObjectTitle(object) + " - Cleaning up...");
		if (formUpdateNeeded[0]) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					refresh(false);
				}
			});
		}
		swingRenderer.getAllDisplayedForms().remove(this);
	}

	protected boolean isModificationStackSlave() {
		return modificationStack instanceof SlaveModificationStack;
	}

	protected IModificationListener createFieldsUpdateListener() {
		return new AbstractSimpleModificationListener() {
			@Override
			public void afterClearInvalidation() {
				// no event
			}

			@Override
			protected void handleAnyEvent(IModification modification) {
				if (isFieldsUpdateListenerDisabled()) {
					return;
				}
				onFieldsUpdate();
			}
		};
	}

	protected void onFieldsUpdate() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				refresh(false);
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

	protected void layoutMembersControls(
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
			categoriesControl = null;
			layoutMembersControls(fieldControlPlaceHolders, methodControlPlaceHolders, membersPanel);
		} else if (allCategories.size() > 0) {
			membersPanel.setLayout(new BorderLayout());
			categoriesControl = createCategoriesControl();
			{
				for (final InfoCategory category : allCategories) {
					List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
							.get(category);
					if (fieldControlPlaceHolders == null) {
						fieldControlPlaceHolders = Collections.emptyList();
					}
					List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
							.get(category);
					if (methodControlPlaceHolders == null) {
						methodControlPlaceHolders = Collections.emptyList();
					}

					JPanel tab = new ControlPanel();
					tab.setName("categoryContainerControl [category=" + category.getCaption() + "]");
					addCategoryTab(category, tab);
					tab.setLayout(new BorderLayout());

					JPanel tabContent = new ControlPanel();
					tabContent.setName("categoryContentControl [category=" + category.getCaption() + "]");
					tab.add(tabContent, BorderLayout.NORTH);
					layoutMembersControls(fieldControlPlaceHolders, methodControlPlaceHolders, tabContent);

				}
			}
			membersPanel.add(categoriesControl, BorderLayout.CENTER);
			Form form = SwingRendererUtils.findParentForm(categoriesControl, swingRenderer);
			if (form == null) {
				throw new ReflectionUIError();
			}
		}
	}

	protected JLabel createStatusBar() {
		JLabel result = new JLabel();
		result.setOpaque(false);
		result.setFont(new JToolTip().getFont());
		result.setName("statusBar");
		return result;
	}

	protected JMenuBar createMenuBar() {
		JMenuBar result = new JMenuBar();
		result.setOpaque(false);
		result.setName("menuBar");
		return result;
	}

	protected int getCategoriesControlPlacement() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN) {
			return JTabbedPane.TOP;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN_VERTICAL) {
			return JTabbedPane.LEFT;
		} else {
			throw new ReflectionUIError();
		}
	}

	protected Container createCategoriesControl() {
		return new ListTabbedPane(JTabbedPane.TOP) {

			private static final long serialVersionUID = 1L;
			private JButton nonSelectedCellRenderer;
			private JLabel selectedCellRenderer;

			{
				setOpaque(false);
				setName("categoriesControl [parent=" + this.getName() + "]");
			}

			@Override
			protected JScrollPane wrapListControl(@SuppressWarnings("rawtypes") JList listControl) {
				JScrollPane result = new ControlScrollPane(listControl);
				result.setBorder(null);
				return result;
			}

			@Override
			protected JPanel createCurrentComponentContainer() {
				JPanel result = super.createCurrentComponentContainer();
				result.setOpaque(false);
				return result;
			}

			@Override
			protected JButton createNonSelectedTabHeaderCellRendererComponent() {
				return nonSelectedCellRenderer = super.createNonSelectedTabHeaderCellRendererComponent();
			}

			@Override
			protected JLabel createSelectedTabHeaderCellRendererComponent() {
				return selectedCellRenderer = super.createSelectedTabHeaderCellRendererComponent();
			}

			protected void refreshCurrentComponentContainerBorder() {
				Color tabBorderColor = getControlsBorderColor();
				if (tabBorderColor != null) {
					currentComponentContainer.setBorder(BorderFactory.createLineBorder(tabBorderColor));
				} else {
					tabBorderColor = getMainBorderColor();
					if (tabBorderColor != null) {
						currentComponentContainer.setBorder(BorderFactory.createLineBorder(tabBorderColor));
					} else {
						currentComponentContainer.setBorder(BorderFactory.createTitledBorder(""));
					}
				}
			}

			protected void refreshNonSelectedCellRenderer() {
				nonSelectedCellRenderer.setForeground(getCategoriesCellForegroundColor());
				nonSelectedCellRenderer.setBorderPainted(false);
				nonSelectedCellRenderer.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(),
						getLayoutSpacing(), getLayoutSpacing(), getLayoutSpacing()));
				Color backgroundColor = getCategoriesCellBackgroundColor();
				if (backgroundColor != null) {
					nonSelectedCellRenderer.setContentAreaFilled(true);
					nonSelectedCellRenderer.setBackground(backgroundColor);
				} else {
					nonSelectedCellRenderer.setContentAreaFilled(false);
					nonSelectedCellRenderer.setBackground(null);
				}
			}

			protected void refreshSelectedCellRenderer() {
				selectedCellRenderer.setForeground(getCategoriesCellForegroundColor());
				Color backgroundColor = getCategoriesCellBackgroundColor();
				selectedCellRenderer.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(), getLayoutSpacing(),
						getLayoutSpacing(), getLayoutSpacing()));
				if (backgroundColor != null) {
					selectedCellRenderer.setOpaque(true);
					selectedCellRenderer.setBackground(SwingRendererUtils.addColorActivationEffect(backgroundColor));
				} else {
					selectedCellRenderer.setOpaque(false);
					selectedCellRenderer.setBackground(null);
					selectedCellRenderer
							.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
									BorderFactory.createEmptyBorder(getLayoutSpacing() - 2, getLayoutSpacing() - 2,
											getLayoutSpacing() - 2, getLayoutSpacing() - 2)));
				}
			}

			protected void refreshNonSelectableArea() {
				Color backgroundColor = getCategoriesCellBackgroundColor();
				if (backgroundColor != null) {
					listControl.setOpaque(true);
					listControl.setBackground(backgroundColor);
					listControlWrapper.setOpaque(true);
					listControlWrapper.setBackground(backgroundColor);
				} else {
					listControl.setOpaque(false);
					listControlWrapper.setOpaque(false);
				}
			}

			@Override
			public void refresh() {
				refreshSelectedCellRenderer();
				refreshNonSelectedCellRenderer();
				refreshNonSelectableArea();
				refreshCurrentComponentContainerBorder();
				super.refresh();
			}

		};
	}

	protected void addCategoryTab(InfoCategory category, JPanel tab) {
		Image iconImage = SwingRendererUtils.loadImageThroughCache(category.getIconImagePath(),
				ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
		ImageIcon icon = (iconImage != null) ? new ImageIcon(iconImage) : null;
		int tabIndex = ((ListTabbedPane) categoriesControl).getTabCount();
		((ListTabbedPane) categoriesControl).addTab(swingRenderer.prepareMessageToDisplay(category.getCaption()), tab);
		((ListTabbedPane) categoriesControl).setIconAt(tabIndex, icon);
	}

	protected boolean refreshCategoriesControlStructure() {
		if (categoriesControl != null) {
			((ListTabbedPane) categoriesControl).refresh();
			((ListTabbedPane) categoriesControl).setTabPlacement(getCategoriesControlPlacement());
		}
		return true;
	}

	protected Color getMainForeroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		if (reflectionUI.getApplicationInfo().getMainForegroundColor() != null) {
			return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainForegroundColor());
		} else {
			return new JPanel().getForeground();
		}
	}

	protected Color getMainBorderColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		if (reflectionUI.getApplicationInfo().getMainBorderColor() != null) {
			return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainBorderColor());
		} else {
			return null;
		}
	}

	protected Color getControlsForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormForegroundColor() == null) {
			return getMainForeroundColor();
		} else {
			return SwingRendererUtils.getColor(type.getFormForegroundColor());
		}
	}

	protected Color getControlsBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBackgroundColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(type.getFormBackgroundColor());
		}
	}

	protected Color getControlsBorderColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBorderColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(type.getFormBorderColor());
		}
	}

	protected Image getControlsBackgroundImage() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBackgroundImagePath() == null) {
			return null;
		} else {
			return SwingRendererUtils.loadImageThroughCache(type.getFormBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
		}
	}

	protected Color getCategoriesCellBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesBackgroundColor() != null) {
			return SwingRendererUtils.getColor(type.getCategoriesBackgroundColor());
		} else {
			return null;
		}
	}

	protected Color getCategoriesCellForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesForegroundColor() != null) {
			return SwingRendererUtils.getColor(type.getCategoriesForegroundColor());
		} else {
			return getControlsForegroundColor();
		}
	}

	/**
	 * @return the currently displayed category.
	 */
	public InfoCategory getDisplayedCategory() {
		if (categoriesControl != null) {
			int currentCategoryIndex;
			currentCategoryIndex = ((ListTabbedPane) categoriesControl).getSelectedIndex();
			if (currentCategoryIndex != -1) {
				String currentCategoryCaption;
				currentCategoryCaption = ((ListTabbedPane) categoriesControl).getTitleAt(currentCategoryIndex);
				return new InfoCategory(currentCategoryCaption, currentCategoryIndex, null);
			}
		}
		return null;
	}

	/**
	 * Changes the currently displayed category. If the specified category is not
	 * found then the displayed category is unchanged.
	 * 
	 * @param category A category object that will be used to find (by comparing
	 *                 attributes) the category that must be displayed. Note that if
	 *                 the given category position is -1 then this position
	 *                 attribute will not be used to match the category that must be
	 *                 displayed.
	 */
	public void setDisplayedCategory(InfoCategory category) {
		String categoryCaption = category.getCaption();
		int categoryPosition = category.getPosition();
		if (categoriesControl != null) {
			int tabCount;
			tabCount = ((ListTabbedPane) categoriesControl).getTabCount();
			for (int i = 0; i < tabCount; i++) {
				String currentCategoryCaption;
				currentCategoryCaption = ((ListTabbedPane) categoriesControl).getTitleAt(i);
				if (categoryCaption.equals(currentCategoryCaption)) {
					if (categoryPosition != -1) {
						if (categoryPosition != i) {
							continue;
						}
					}
					((ListTabbedPane) categoriesControl).setSelectedIndex(i);
					return;
				}
			}
		}
	}

	protected SortedMap<InfoCategory, List<MethodControlPlaceHolder>> createMethodControlPlaceHoldersByCategory(
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

	protected SortedMap<InfoCategory, List<FieldControlPlaceHolder>> createFieldControlPlaceHoldersByCategory(
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

	protected ITypeInfo buildFormFilteredType() {
		IInfoFilter infoFilter = this.infoFilter;
		if (infoFilter == null) {
			infoFilter = IInfoFilter.DEFAULT;
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo rawType = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
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
					swingRenderer.getReflectionUI().logError(t);
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

		}.wrapTypeInfo(rawType);
		return result;
	}

	/**
	 * @param fieldName The name of the field to search for.
	 * @return the control place holder associated with field that has the specified
	 *         name.
	 */
	public FieldControlPlaceHolder getFieldControlPlaceHolder(String fieldName) {
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				if (fieldName.equals(fieldControlPlaceHolder.getField().getName())) {
					return fieldControlPlaceHolder;
				}
			}
		}
		return null;
	}

	/**
	 * @param methodSignature The signature of the method to search for.
	 * @return the control place holder associated with method that has the
	 *         specified signature.
	 */
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

	protected void layoutMembersPanels(Container container, Container fieldsPanel, Container methodsPanel) {
		container.setLayout(new BorderLayout());
		if (fieldsPanel != null) {
			container.add(fieldsPanel, BorderLayout.CENTER);
		}
		if (methodsPanel != null) {
			container.add(methodsPanel, BorderLayout.SOUTH);
		}
	}

	protected void layoutMembersControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, JPanel membersPanel) {
		Container fieldsPanel = (fielControlPlaceHolders.size() == 0) ? null
				: createFieldsPanel(fielControlPlaceHolders);
		Container methodsPanel = (methodControlPlaceHolders.size() == 0) ? null
				: createMethodsPanel(methodControlPlaceHolders);
		layoutMembersPanels(membersPanel, fieldsPanel, methodsPanel);
	}

	/**
	 * Updates the state of the current form controls.
	 * 
	 * @param refreshStructure Whether the current form should update its structure
	 *                         to reflect the recent meta-data changes. Mainly used
	 *                         in design mode.
	 * 
	 */
	public void refresh(boolean refreshStructure) {
		if (refreshStructure && detectStructureChange()) {
			InfoCategory displayedCategory = getDisplayedCategory();
			try {
				removeAll();
				fieldControlPlaceHoldersByCategory = createFieldControlPlaceHoldersByCategory(objectType.getFields());
				methodControlPlaceHoldersByCategory = createMethodControlPlaceHoldersByCategory(
						objectType.getMethods());
				for (List<FieldControlPlaceHolder> fieldControlPlaceHolders : fieldControlPlaceHoldersByCategory
						.values()) {
					for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHolders) {
						fieldControlPlaceHolder.initializeUI();
					}
				}
				for (List<MethodControlPlaceHolder> methodControlPlaceHolders : methodControlPlaceHoldersByCategory
						.values()) {
					for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
						methodControlPlaceHolder.initializeUI();
					}
				}
				layoutMembersControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory, this);
				SwingRendererUtils.handleComponentSizeChange(this);
			} finally {
				if (displayedCategory != null) {
					setDisplayedCategory(displayedCategory);
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
			for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
				List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
						.get(category);
				for (int i = 0; i < methodControlPlaceHolders.size(); i++) {
					MethodControlPlaceHolder methodControlPlaceHolder = methodControlPlaceHolders.get(i);
					methodControlPlaceHolder.refreshUI(refreshStructure);
					if (refreshStructure) {
						updateMethodControlLayoutInContainer(methodControlPlaceHolder);
					}
				}
			}
		}
		finalizeFormUpdate();
		if (refreshStructure) {
			if (!refreshCategoriesControlStructure()) {
				fieldControlPlaceHoldersByCategory.clear();
				methodControlPlaceHoldersByCategory.clear();
				refresh(true);
				return;
			}
			setPreservingRatio(true);
			setFillingAreaWhenPreservingRatio(true);
			Color awtBackgroundColor = getControlsBackgroundColor();
			Color awtForegroundColor = getControlsForegroundColor();
			Image awtImage = getControlsBackgroundImage();
			setBackground(awtBackgroundColor);
			setImage(awtImage);
			setOpaque((awtBackgroundColor != null) && (awtImage == null));
			Color borderColor = getMainBorderColor();
			{
				menuBar.setForeground(awtForegroundColor);
				if (borderColor != null) {
					Border outsideBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor);
					Border insideBorder = BorderFactory.createEmptyBorder(getLayoutSpacing(), getLayoutSpacing(),
							getLayoutSpacing(), getLayoutSpacing());
					menuBar.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
				} else {
					menuBar.setBorder(new JMenuBar().getBorder());
				}
			}
			{
				statusBar.setForeground(awtForegroundColor);
				if (borderColor != null) {
					Border outsideBorder = BorderFactory.createLineBorder(borderColor);
					Border insideBorder = BorderFactory.createEmptyBorder(getLayoutSpacing(), getLayoutSpacing(),
							getLayoutSpacing(), getLayoutSpacing());
					statusBar.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
				} else {
					statusBar.setBorder(BorderFactory.createRaisedBevelBorder());
				}
			}
		}
		for (IRefreshListener l : refreshListeners) {
			l.onRefresh(refreshStructure);
		}
	}

	protected boolean detectStructureChange() {

		SortedMap<InfoCategory, List<FieldControlPlaceHolder>> displayedFieldControlPlaceHoldersByCategory = fieldControlPlaceHoldersByCategory;
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> displayedMethodControlPlaceHoldersByCategory = methodControlPlaceHoldersByCategory;

		boolean modificationsDetected = false;

		ITypeInfo oldObjectType = objectType;
		objectType = buildFormFilteredType();

		if (!objectType.equals(oldObjectType)) {
			modificationsDetected = true;
			setName("form [objectType=" + objectType.getName() + "]");
			modificationStack.setName(getName());
		}

		SortedMap<InfoCategory, List<FieldControlPlaceHolder>> newFieldControlPlaceHoldersByCategory = createFieldControlPlaceHoldersByCategory(
				objectType.getFields());
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> newMethodControlPlaceHoldersByCategory = createMethodControlPlaceHoldersByCategory(
				objectType.getMethods());

		if (!modificationsDetected) {
			if (!new ArrayList<InfoCategory>(newFieldControlPlaceHoldersByCategory.keySet())
					.equals(new ArrayList<InfoCategory>(displayedFieldControlPlaceHoldersByCategory.keySet()))) {
				modificationsDetected = true;
			}
		}

		if (!modificationsDetected) {
			if (!new ArrayList<InfoCategory>(newMethodControlPlaceHoldersByCategory.keySet())
					.equals(new ArrayList<InfoCategory>(displayedMethodControlPlaceHoldersByCategory.keySet()))) {
				modificationsDetected = true;
			}
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
				}
				if (modificationsDetected) {
					break;
				}
			}
		}

		return modificationsDetected;
	}

	protected void finalizeFormUpdate() {
		if (menuBar.getParent() != null) {
			updateMenuBar();
		}
		if (statusBar.getParent() != null) {
			validateFormInBackgroundAndReportOnStatusBar();
		}
	}

	/**
	 * Updates this form menu bar component.
	 */
	public void updateMenuBar() {
		MenuModel globalMenuModel = new MenuModel();
		addMenuContributionTo(globalMenuModel);
		menuBar.removeAll();
		for (MenuInfo menuInfo : globalMenuModel.getMenus()) {
			menuBar.add(creatMenu(menuInfo));
		}
		SwingRendererUtils.handleComponentSizeChange(menuBar);
		Color awtBackgroundColor = getControlsBackgroundColor();
		Color awtForegroundColor = getControlsForegroundColor();
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			JMenu menu = menuBar.getMenu(i);
			menu.setBackground(awtBackgroundColor);
			menu.setOpaque(awtBackgroundColor != null);
			menu.setForeground(awtForegroundColor);
		}
		menuBar.setVisible(menuBar.getComponentCount() > 0);
	}

	protected JMenu creatMenu(MenuInfo menuInfo) {
		return new Menu(swingRenderer, menuInfo);
	}

	/**
	 * Import this form menu contributions into the specified menu model.
	 * 
	 * @param menuModel The menu model that will receive this form menu
	 *                  contributions.
	 */
	public void addMenuContributionTo(MenuModel menuModel) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		MenuModel formMenuModel = type.getMenuModel();
		menuModel.importContributions(formMenuModel, new Filter<IMenuElementInfo>() {
			@Override
			public IMenuElementInfo get(IMenuElementInfo e) {
				if (e instanceof AbstractActionMenuItemInfo) {
					AbstractActionMenuItemInfo action = (AbstractActionMenuItemInfo) e;
					Map<String, Object> specificProperties = new HashMap<String, Object>(
							action.getSpecificProperties());
					specificProperties.put(ACTION_MENU_ITEM_CONTEXT_FORM, Form.this);
					action.setSpecificProperties(specificProperties);
				}
				return e;
			}
		});
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				Component fieldControl = fieldControlPlaceHolder.getFieldControl();
				if (fieldControl instanceof IAdvancedFieldControl) {
					((IAdvancedFieldControl) fieldControl).addMenuContributions(menuModel);
				}
			}
		}
	}

	/**
	 * Requests that this form (actually the 1st focusable control of this form) get
	 * the input focus.
	 * 
	 * @return false if the focus change request is guaranteed to fail; true if it
	 *         is likely to succeed.
	 */
	public boolean requestFormFocus() {
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				if (SwingRendererUtils.requestAnyComponentFocus(fieldControlPlaceHolder.getFieldControl(),
						swingRenderer)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param field The field for which the control place holder must be created.
	 * @return a new control place holder for the given field.
	 */
	public FieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
		return new FieldControlPlaceHolder(this, field);
	}

	/**
	 * @param method The method for which the control place holder must be created.
	 * @return a new control place holder for the given method.
	 */
	public MethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
		return new MethodControlPlaceHolder(this, method);
	}

	protected Container createFieldsPanel(List<FieldControlPlaceHolder> fielControlPlaceHolders) {
		JPanel fieldsPanel = new ControlPanel();
		fieldsPanel.setLayout(getFieldsPanelLayout());
		fieldsPanel.setBorder(getFieldsPanelBorder());
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FieldControlPlaceHolder fieldControlPlaceHolder = fielControlPlaceHolders.get(i);
			{
				fieldsPanel.add(fieldControlPlaceHolder);
				fieldControlPlaceHolder.setLayoutInContainerUpdateNeeded(true);
				fieldControlPlaceHolder.setPositionInContainer(i);
				updateFieldControlLayoutInContainer(fieldControlPlaceHolder);
			}
		}
		fieldsPanel.setName("fieldsPanel [parent=" + this.getName() + "]");
		return fieldsPanel;
	}

	protected Container createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
		JPanel methodsPanel = new ControlPanel();
		methodsPanel.setLayout(getMethodsPanelLayout());
		methodsPanel.setBorder(getMethodsPanelBorder());
		for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
			methodsPanel.add(methodControlPlaceHolder);
			updateMethodControlLayoutInContainer(methodControlPlaceHolder);
		}
		methodsPanel.setName("methodsPanel [parent=" + this.getName() + "]");
		ControlScrollPane scrollPane = new ControlScrollPane(
				SwingRendererUtils.flowInLayout(methodsPanel, GridBagConstraints.CENTER));
		SwingRendererUtils.removeScrollPaneBorder(scrollPane);
		return scrollPane;
	}

	protected LayoutManager getFieldsPanelLayout() {
		return new GridBagLayout();
	}

	protected LayoutManager getMethodsPanelLayout() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		MethodsLayout methodsOrientation = type.getMethodsLayout();
		int spacing = getLayoutSpacing();
		GridLayout newLayout;
		if (methodsOrientation == MethodsLayout.HORIZONTAL_FLOW) {
			newLayout = new GridLayout(1, 0, spacing, spacing);
		} else if (methodsOrientation == MethodsLayout.VERTICAL_FLOW) {
			newLayout = new GridLayout(0, 1, spacing, spacing);
		} else {
			throw new ReflectionUIError();
		}
		return newLayout;
	}

	protected Border getFieldsPanelBorder() {
		return null;
	}

	protected Border getMethodsPanelBorder() {
		int spacing = getLayoutSpacing();
		return new EmptyBorder(spacing, spacing, spacing, spacing);
	}

	protected void updateFieldControlLayoutInContainer(FieldControlPlaceHolder fieldControlPlaceHolder) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		IFieldInfo field = fieldControlPlaceHolder.getField();
		ITypeInfo.FieldsLayout fieldsOrientation = type.getFieldsLayout();
		JPanel fieldsPanel = (JPanel) fieldControlPlaceHolder.getParent();
		int spacing = getLayoutSpacing();

		Component captionControl = fieldControlPlaceHolder.getSiblingCaptionControl();
		if (captionControl != null) {
			fieldsPanel.remove(captionControl);
			fieldControlPlaceHolder.setSiblingCaptionControl(null);
		}
		boolean shouldHaveSeparateCaptionControl = !fieldControlPlaceHolder.showsCaption()
				&& (fieldControlPlaceHolder.getField().getCaption().length() > 0);
		if (shouldHaveSeparateCaptionControl) {
			captionControl = createSeparateFieldCaptionControl(fieldControlPlaceHolder);
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
			captionControlLayoutConstraints.anchor = GridBagConstraints.NORTHWEST;
			fieldsPanel.add(captionControl, captionControlLayoutConstraints);
			fieldControlPlaceHolder.setSiblingCaptionControl(captionControl);
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

		Component onlineHelpControl = fieldControlPlaceHolder.getSiblingOnlineHelpControl();
		if (onlineHelpControl != null) {
			fieldsPanel.remove(onlineHelpControl);
			fieldControlPlaceHolder.setSiblingOnlineHelpControl(null);
		}
		onlineHelpControl = createFieldOnlineHelpControl(fieldControlPlaceHolder);
		if (onlineHelpControl != null) {
			fieldControlPlaceHolder.setSiblingOnlineHelpControl(onlineHelpControl);
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
			fieldsPanel.add(onlineHelpControl, layoutConstraints);
		}

		SwingRendererUtils.handleComponentSizeChange(fieldsPanel);

		fieldControlPlaceHolder.setLayoutInContainerUpdateNeeded(false);

	}

	protected void updateMethodControlLayoutInContainer(MethodControlPlaceHolder methodControlPlaceHolder) {
		boolean layoutUpdated = false;
		JPanel methodsPanel = (JPanel) methodControlPlaceHolder.getParent();
		GridLayout newLayout = (GridLayout) getMethodsPanelLayout();
		GridLayout oldLayout = (GridLayout) methodsPanel.getLayout();
		if (!((oldLayout.getRows() == newLayout.getRows()) && (oldLayout.getColumns() == newLayout.getColumns())
				&& (oldLayout.getHgap() == newLayout.getHgap()) && (oldLayout.getVgap() == newLayout.getVgap()))) {
			methodsPanel.setLayout(newLayout);
			layoutUpdated = true;
		}
		EmptyBorder newBorder = (EmptyBorder) getMethodsPanelBorder();
		EmptyBorder oldBorder = (EmptyBorder) methodsPanel.getBorder();
		if (!oldBorder.getBorderInsets().equals(newBorder.getBorderInsets())) {
			layoutUpdated = true;
			methodsPanel.setBorder(newBorder);
		}
		if(layoutUpdated) {
			SwingRendererUtils.handleComponentSizeChange(methodsPanel);
		}
	}

	protected Component createFieldOnlineHelpControl(FieldControlPlaceHolder fieldControlPlaceHolder) {
		final IFieldControlData data = fieldControlPlaceHolder.getControlData();
		final String onlineHelp = data.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		final String title = ReflectionUIUtils.composeMessage(data.getCaption(), "Help");
		final Image iconImage = swingRenderer.getObjectIconImage(object);
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public Image retrieveBackgroundImage() {
				if (data.getButtonBackgroundImagePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadImageThroughCache(data.getButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
				}
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (data.getButtonBackgroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBackgroundColor());
				}
			}

			@Override
			public Color retrieveForegroundColor() {
				if (data.getButtonForegroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonForegroundColor());
				}
			}

			@Override
			public Color retrieveBorderColor() {
				if (data.getButtonBorderColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBorderColor());
				}
			}

			@Override
			public String retrieveText() {
				return "";
			}

			@Override
			public String retrieveToolTipText() {
				return swingRenderer.prepareMessageToDisplay(onlineHelp);
			}

			@Override
			public Icon retrieveIcon() {
				return SwingRendererUtils.HELP_ICON;
			}

		};
		result.setFocusable(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swingRenderer.openInformationDialog(result, onlineHelp, title, iconImage);
			}
		});
		result.setName("fieldOnlineHelpControl [field=" + fieldControlPlaceHolder.getField().getName() + ", parent="
				+ this.getName() + "]");
		return result;
	}

	protected Component createButtonBarOnlineHelpControl() {
		final ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		final String onlineHelp = type.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		final String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Help");
		final Image iconImage = swingRenderer.getObjectIconImage(object);
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public Image retrieveBackgroundImage() {
				if (type.getFormButtonBackgroundImagePath() != null) {
					return SwingRendererUtils.loadImageThroughCache(type.getFormButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI));
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath() != null) {
					return SwingRendererUtils.loadImageThroughCache(
							reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI));
				}
				return null;
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (type.getFormButtonBackgroundColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonBackgroundColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonBackgroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveForegroundColor() {
				if (type.getFormButtonForegroundColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonForegroundColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonForegroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonForegroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveBorderColor() {
				if (type.getFormButtonBorderColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonBorderColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBorderColor() != null) {
					return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainButtonBorderColor());
				}
				return null;
			}

			@Override
			public String retrieveText() {
				return "";
			}

			@Override
			public String retrieveToolTipText() {
				return swingRenderer.prepareMessageToDisplay(onlineHelp);
			}

			@Override
			public Icon retrieveIcon() {
				return SwingRendererUtils.HELP_ICON;
			}

		};
		result.setFocusable(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swingRenderer.openInformationDialog(result, onlineHelp, title, iconImage);
			}
		});
		result.setName("buttonBarOnlineHelpControl [parent=" + this.getName() + "]");
		return result;
	}

	protected Component createSeparateFieldCaptionControl(FieldControlPlaceHolder fieldControlPlaceHolder) {
		IFieldControlData data = fieldControlPlaceHolder.getControlData();
		JLabel result = new JLabel(swingRenderer.prepareMessageToDisplay(data.getCaption() + ": "));
		if (data.getLabelForegroundColor() != null) {
			result.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
		}
		result.setName("captionControl [field=" + fieldControlPlaceHolder.getField().getName() + ", parent="
				+ this.getName() + "]");
		return result;
	}

	protected int getLayoutSpacing() {
		return objectType.getFormSpacing();
	}

	/**
	 * @return the buttons that should be laid on the containing window button bar
	 *         when this form is the root one.
	 */
	public List<Component> createButtonBarControls() {
		if (object == null) {
			return null;
		}
		List<Component> result = new ArrayList<Component>();
		Component onlineHelpControl = createButtonBarOnlineHelpControl();
		if (onlineHelpControl != null) {
			result.add(onlineHelpControl);
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.isModificationStackAccessible()) {
			if (modificationStack != null) {
				result.addAll(new ModificationStackControls(this).create(swingRenderer));
			}
		}
		return result;
	}

	/**
	 * @return the modifiable list of listeners that will get the form refreshing
	 *         notifications.
	 */
	public List<IRefreshListener> getRefreshListeners() {
		return refreshListeners;
	}

	@Override
	public String toString() {
		return "Form [id=" + hashCode() + ", object=" + object + "]";
	}

	/**
	 * Listener class allowing to get notifications when a form is refreshed.
	 * 
	 * @author olitank
	 *
	 */
	public interface IRefreshListener {

		/**
		 * Called when the source form is refreshed.
		 * 
		 * @param refreshStructure The value of the parameter that was passed to
		 *                         {@link Form#refresh(boolean)}.
		 */
		void onRefresh(boolean refreshStructure);

	}

}
