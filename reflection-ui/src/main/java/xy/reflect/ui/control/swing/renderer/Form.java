/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
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
	protected boolean busyIndicationDisabled;
	protected IModificationListener fieldsUpdateListener = createFieldsUpdateListener();
	protected boolean visibilityEventsDisabled = false;
	protected List<IRefreshListener> refreshListeners = new ArrayList<IRefreshListener>();
	protected JLabel statusBar;
	protected JMenuBar menuBar;

	public Form(SwingRenderer swingRenderer, Object object, IInfoFilter infoFilter) {
		this.swingRenderer = swingRenderer;
		setObject(object);
		setInfoFilter(infoFilter);
		setModificationStack(new ModificationStack(toString()));
		objectType = buildFormFilteredType();
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				if (visibilityEventsDisabled) {
					return;
				}
				try {
					formShown();
				} catch (Throwable t) {
					Form.this.swingRenderer.handleExceptionsFromDisplayedUI(Form.this, t);
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
					Form.this.swingRenderer.handleExceptionsFromDisplayedUI(Form.this, t);
				}
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
		menuBar = createMenuBar();
		statusBar = createStatusBar();
		setStatusBarErrorMessage(null);
		layoutMembersControls(fieldControlPlaceHoldersByCategory, methodControlPlaceHoldersByCategory, this);
		refresh(true);
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

	public JLabel getStatusBar() {
		return statusBar;
	}

	public SortedMap<InfoCategory, List<FieldControlPlaceHolder>> getFieldControlPlaceHoldersByCategory() {
		return fieldControlPlaceHoldersByCategory;
	}

	public SortedMap<InfoCategory, List<MethodControlPlaceHolder>> getMethodControlPlaceHoldersByCategory() {
		return methodControlPlaceHoldersByCategory;
	}

	public Container getCategoriesControl() {
		return categoriesControl;
	}

	public boolean isBusyIndicationDisabled() {
		return busyIndicationDisabled;
	}

	public void setBusyIndicationDisabled(boolean busyIndicationDisabled) {
		this.busyIndicationDisabled = busyIndicationDisabled;
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
					if (getStatusBarMessage() != null) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setStatusBarErrorMessage(null);
							}
						});
					}
				} catch (Exception e) {
					swingRenderer.getReflectionUI().logDebug(e);
					final String errorMsg = MiscUtils.getPrettyErrorMessage(e);
					if (!errorMsg.equals(getStatusBarMessage())) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								setStatusBarErrorMessage(errorMsg);
							}
						});
					}
				}

			}
		}.start();
	}

	public void setStatusBarErrorMessage(String errorMsg) {
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

	public String getStatusBarMessage() {
		return ((JLabel) statusBar).getText();
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
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					refresh(false);
				}
			});
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

	public void layoutMembersControls(
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
					addCategoryTab(category, tab);
					tab.setLayout(new BorderLayout());

					JPanel tabContent = new ControlPanel();
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

	public JLabel createStatusBar() {
		JLabel result = new JLabel();
		result.setOpaque(false);
		result.setFont(new JToolTip().getFont());
		return result;
	}

	public JMenuBar createMenuBar() {
		JMenuBar result = new JMenuBar();
		result.setOpaque(false);
		return result;
	}

	public int getCategoriesControlPlacement() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN) {
			return JTabbedPane.TOP;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN_VERTICAL) {
			return JTabbedPane.LEFT;
		} else {
			throw new ReflectionUIError();
		}
	}

	public Container createCategoriesControl() {
		return new ListTabbedPane(JTabbedPane.TOP) {

			private static final long serialVersionUID = 1L;
			private JButton nonSelectedCellRenderer;
			private JLabel selectedCellRenderer;

			{
				setOpaque(false);
			}

			@Override
			protected JScrollPane wrapListControl(@SuppressWarnings("rawtypes") JList listControl) {
				JScrollPane result = new ControlScrollPane(listControl) {
					private static final long serialVersionUID = 1L;

					@Override
					public Dimension getPreferredSize() {
						Dimension result = super.getPreferredSize();
						if (result == null) {
							return null;
						}
						result = preventScrollBarsFromHidingContent(result);
						return result;
					}

					Dimension preventScrollBarsFromHidingContent(Dimension size) {
						Dimension result = new Dimension(size);
						JScrollBar hSBar = getHorizontalScrollBar();
						{
							if (hSBar != null) {
								if (hSBar.isVisible()) {
									result.height += hSBar.getHeight();
								}
							}
						}
						JScrollBar vSBar = getVerticalScrollBar();
						{
							if (vSBar != null) {
								if (vSBar.isVisible()) {
									result.width += vSBar.getWidth();
								}
							}
						}
						return result;
					}
				};
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
					selectedCellRenderer.setBackground(swingRenderer.addColorActivationEffect(backgroundColor));
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

	public void addCategoryTab(InfoCategory category, JPanel tab) {
		Image iconImage = SwingRendererUtils.loadImageThroughCache(category.getIconImagePath(),
				ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
		ImageIcon icon = (iconImage != null) ? new ImageIcon(iconImage) : null;
		int tabIndex = ((ListTabbedPane) categoriesControl).getTabCount();
		((ListTabbedPane) categoriesControl).addTab(swingRenderer.prepareStringToDisplay(category.getCaption()), tab);
		((ListTabbedPane) categoriesControl).setIconAt(tabIndex, icon);
	}

	public boolean refreshCategoriesControlStructure() {
		if (categoriesControl != null) {
			((ListTabbedPane) categoriesControl).refresh();
			((ListTabbedPane) categoriesControl).setTabPlacement(getCategoriesControlPlacement());
		}
		return true;
	}

	public Color getMainForeroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		if (reflectionUI.getApplicationInfo().getMainForegroundColor() != null) {
			return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainForegroundColor());
		} else {
			return new JPanel().getForeground();
		}
	}

	public Color getMainBorderColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		if (reflectionUI.getApplicationInfo().getMainBorderColor() != null) {
			return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainBorderColor());
		} else {
			return null;
		}
	}

	public Color getControlsForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormForegroundColor() == null) {
			return getMainForeroundColor();
		} else {
			return SwingRendererUtils.getColor(type.getFormForegroundColor());
		}
	}

	public Color getControlsBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBackgroundColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(type.getFormBackgroundColor());
		}
	}

	public Color getControlsBorderColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBorderColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(type.getFormBorderColor());
		}
	}

	public Image getControlsBackgroundImage() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBackgroundImagePath() == null) {
			return null;
		} else {
			return SwingRendererUtils.loadImageThroughCache(type.getFormBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
		}
	}

	public Color getCategoriesCellBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesBackgroundColor() != null) {
			return SwingRendererUtils.getColor(type.getCategoriesBackgroundColor());
		} else {
			return null;
		}
	}

	public Color getCategoriesCellForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesForegroundColor() != null) {
			return SwingRendererUtils.getColor(type.getCategoriesForegroundColor());
		} else {
			return getControlsForegroundColor();
		}
	}

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

	public ITypeInfo buildFormFilteredType() {
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

	public MethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
		return new MethodControlPlaceHolder(swingRenderer, this, method);
	}

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

	public void layoutMembersPanels(Container container, Container fieldsPanel, Container methodsPanel) {
		container.setLayout(new BorderLayout());
		if (fieldsPanel != null) {
			container.add(fieldsPanel, BorderLayout.CENTER);
		}
		if (methodsPanel != null) {
			container.add(methodsPanel, BorderLayout.SOUTH);
		}
	}

	public void layoutMembersControls(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, JPanel membersPanel) {
		Container fieldsPanel = (fielControlPlaceHolders.size() == 0) ? null
				: createFieldsPanel(fielControlPlaceHolders);
		Container methodsPanel = (methodControlPlaceHolders.size() == 0) ? null
				: createMethodsPanel(methodControlPlaceHolders);
		layoutMembersPanels(membersPanel, fieldsPanel, methodsPanel);
	}

	public Container createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
		JPanel result = new ControlPanel();
		for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
			result.add(methodControlPlaceHolder);
			updateMethodControlLayoutInContainer(methodControlPlaceHolder);
		}
		return new ControlScrollPane(SwingRendererUtils.flowInLayout(result, GridBagConstraints.CENTER)) {

			private static final long serialVersionUID = 1L;

			{
				SwingRendererUtils.removeScrollPaneBorder(this);
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

	public void refresh(boolean refreshStructure) {
		if (refreshStructure && recreateControlPlaceHoldersIfNeeded()) {
			InfoCategory displayedCategory = getDisplayedCategory();
			try {
				removeAll();
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

	public boolean recreateControlPlaceHoldersIfNeeded() {

		boolean modificationsDetected = false;

		objectType = buildFormFilteredType();

		SortedMap<InfoCategory, List<FieldControlPlaceHolder>> displayedFieldControlPlaceHoldersByCategory = fieldControlPlaceHoldersByCategory;
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> displayedMethodControlPlaceHoldersByCategory = methodControlPlaceHoldersByCategory;

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

	public void finalizeFormUpdate() {
		if (menuBar.getParent() != null) {
			updateMenuBar();
		}
		if (statusBar.getParent() != null) {
			validateFormInBackgroundAndReportOnStatusBar();
		}
	}

	public void updateMenuBar() {
		MenuModel globalMenuModel = new MenuModel();
		addMenuContributionTo(globalMenuModel);
		menuBar.removeAll();
		for (MenuInfo menuInfo : globalMenuModel.getMenus()) {
			menuBar.add(new Menu(swingRenderer, menuInfo));
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

	public void addMenuContributionTo(MenuModel menuModel) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
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
		JPanel fieldsPanel = new ControlPanel();
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
		onlineHelpControl = createFieldOnlineHelpControl(fieldControlPlaceHolder.getControlData());
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

	public Component createFieldOnlineHelpControl(final IFieldControlData data) {
		final String onlineHelp = data.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		final String title = ReflectionUIUtils.composeMessage(data.getCaption(), "Help");
		final Image iconImage = swingRenderer.getObjectIconImage(object);
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

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
			public String retrieveCaption() {
				return "";
			}

			@Override
			public String retrieveToolTipText() {
				return onlineHelp;
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
		return result;
	}

	public Component createButtonBarOnlineHelpControl() {
		final ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		final String onlineHelp = type.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		final String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Help");
		final Image iconImage = swingRenderer.getObjectIconImage(object);
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

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
			public String retrieveCaption() {
				return "";
			}

			@Override
			public String retrieveToolTipText() {
				return onlineHelp;
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
		return result;
	}

	public Component createSeparateFieldCaptionControl(FieldControlPlaceHolder fieldControlPlaceHolder) {
		IFieldControlData data = fieldControlPlaceHolder.getControlData();
		JLabel result = new JLabel(swingRenderer.prepareStringToDisplay(data.getCaption() + ": "));
		if (data.getLabelForegroundColor() != null) {
			result.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
		}
		return result;
	}

	public LayoutManager getFieldsPanelLayout() {
		return new GridBagLayout();
	}

	public int getLayoutSpacing() {
		return SwingRendererUtils.getStandardCharacterWidth(this) * 1;
	}

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
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.isModificationStackAccessible()) {
			if (modificationStack != null) {
				result.addAll(new ModificationStackControls(this).create(swingRenderer));
			}
		}
		return result;
	}

	public List<IRefreshListener> getRefreshListeners() {
		return refreshListeners;
	}

	@Override
	public String toString() {
		return "Form [id=" + hashCode() + ", object=" + object + "]";
	}

	public interface IRefreshListener {

		void onRefresh(boolean refreshStructure);

	}
}
