/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.ModificationStackControls;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.menu.AbstractActionMenuItem;
import xy.reflect.ui.info.menu.AbstractMenuItem;
import xy.reflect.ui.info.menu.IMenuElement;
import xy.reflect.ui.info.menu.Menu;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.menu.MethodActionMenuItem;
import xy.reflect.ui.info.menu.builtin.AbstractBuiltInActionMenuItem;
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
import xy.reflect.ui.util.component.ControlPanel;
import xy.reflect.ui.util.component.ControlScrollPane;
import xy.reflect.ui.util.component.ControlTabbedPane;
import xy.reflect.ui.util.component.ImagePanel;
import xy.reflect.ui.util.component.ListTabbedPane;

public class Form extends ImagePanel {

	private static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected Object object;
	protected ModificationStack modificationStack;
	protected boolean fieldsUpdateListenerDisabled = false;
	protected IInfoFilter infoFilter;
	protected SortedMap<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory;
	protected SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory;
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
		fill();
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
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setStatusBarErrorMessage(null);
						}
					});
				} catch (Exception e) {
					swingRenderer.getReflectionUI().logDebug(e);
					final String errorMsg = ReflectionUIUtils.getPrettyErrorMessage(e);
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

	public void fill() {
		setLayout(new BorderLayout());

		JPanel membersPanel = createMembersPanel();
		{
			add(membersPanel, BorderLayout.CENTER);
			fieldControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<FieldControlPlaceHolder>>();
			methodControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<MethodControlPlaceHolder>>();
		}

		refresh(true);
	}

	public JPanel createMembersPanel() {
		return new ControlPanel();
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

	public boolean isCategoriesControlClassic() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC)
				|| (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC_VERTICAL);
	}

	public boolean isCategoriesControlModern() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN)
				|| (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN_VERTICAL);
	}

	public int getCategoriesControlPlacement() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC) {
			return JTabbedPane.TOP;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN) {
			return JTabbedPane.TOP;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC_VERTICAL) {
			return JTabbedPane.LEFT;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN_VERTICAL) {
			return JTabbedPane.LEFT;
		} else {
			throw new ReflectionUIError();
		}
	}

	public Container createCategoriesControl() {
		if (isCategoriesControlModern()) {
			return new ListTabbedPane(JTabbedPane.TOP) {

				private static final long serialVersionUID = 1L;
				private JButton nonSelectedCellRenderer;
				private JLabel selectedCellRenderer;

				{
					setOpaque(false);
				}

				@Override
				protected JList createListControl() {
					JList result = super.createListControl();
					result.setOpaque(false);
					return result;
				}

				@Override
				protected Component wrapListControl(JList listControl) {
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

				protected void refreshNonSelectedCellRenderer() {
					nonSelectedCellRenderer.setForeground(getControlsForegroundColor());
					nonSelectedCellRenderer.setBorderPainted(false);
					Color backgroundColor = getCellBackgroundColor();
					if (backgroundColor != null) {
						nonSelectedCellRenderer.setContentAreaFilled(true);
						nonSelectedCellRenderer.setBackground(backgroundColor);
						nonSelectedCellRenderer.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(),
								getLayoutSpacing(), getLayoutSpacing(), getLayoutSpacing()));
					} else {
						nonSelectedCellRenderer.setContentAreaFilled(false);
						nonSelectedCellRenderer.setBackground(null);
						nonSelectedCellRenderer.setBorder(BorderFactory.createLoweredBevelBorder());
					}
				}

				protected void refreshSelectedCellRenderer() {
					selectedCellRenderer.setForeground(getControlsForegroundColor());
					Color backgroundColor = getCellBackgroundColor();
					if (backgroundColor != null) {
						selectedCellRenderer.setOpaque(true);
						selectedCellRenderer.setBackground(swingRenderer.addColorActivationEffect(backgroundColor));
						selectedCellRenderer.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(),
								getLayoutSpacing(), getLayoutSpacing(), getLayoutSpacing()));
					} else {
						selectedCellRenderer.setOpaque(false);
						selectedCellRenderer.setBackground(null);
						selectedCellRenderer.setBorder(BorderFactory.createLoweredBevelBorder());
					}
				}

				protected Color getCellBackgroundColor() {
					Color result = getControlsBackgroundColor();
					if (result == null) {
						if (swingRenderer.getReflectionUI().getApplicationInfo().getMainBackgroundColor() != null) {
							result = SwingRendererUtils.getColor(
									swingRenderer.getReflectionUI().getApplicationInfo().getMainBackgroundColor());
						}
					}
					return result;
				}

				@Override
				public void refresh() {
					refreshSelectedCellRenderer();
					refreshNonSelectedCellRenderer();
					super.refresh();
				}

			};
		} else if (isCategoriesControlClassic()) {
			return new ControlTabbedPane() {

				private static final long serialVersionUID = 1L;

				@Override
				protected Color getTabBorderColor() {
					Color result = getControlsBorderColor();
					if (result != null) {
						return result;
					}
					return super.getTabBorderColor();
				}

			};
		} else {
			throw new ReflectionUIError();
		}
	}

	public void addCategoryTab(InfoCategory category, JPanel tab) {
		Image iconImage = SwingRendererUtils.loadImageThroughcache(category.getIconImagePath(),
				ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
		ImageIcon icon = (iconImage != null) ? new ImageIcon(iconImage) : null;
		if (categoriesControl instanceof ListTabbedPane) {
			int tabIndex = ((ListTabbedPane) categoriesControl).getTabCount();
			((ListTabbedPane) categoriesControl).addTab(swingRenderer.prepareStringToDisplay(category.getCaption()),
					tab);
			((ListTabbedPane) categoriesControl).setIconAt(tabIndex, icon);
		} else if (categoriesControl instanceof JTabbedPane) {
			int tabIndex = ((JTabbedPane) categoriesControl).getTabCount();
			((JTabbedPane) categoriesControl).addTab(swingRenderer.prepareStringToDisplay(category.getCaption()), tab);
			((JTabbedPane) categoriesControl).setIconAt(tabIndex, icon);
		} else {
			throw new ReflectionUIError();
		}
	}

	public boolean refreshCategoriesControlStructure() {
		if (categoriesControl != null) {
			if (isCategoriesControlModern()) {
				if (!(categoriesControl instanceof ListTabbedPane)) {
					return false;
				}
				((ListTabbedPane) categoriesControl).refresh();
				((ListTabbedPane) categoriesControl).setTabPlacement(getCategoriesControlPlacement());
			} else {
				if (!(categoriesControl instanceof JTabbedPane)) {
					return false;
				}
				((JTabbedPane) categoriesControl).setForeground(getControlsForegroundColor());
				((JTabbedPane) categoriesControl).updateUI();
				((JTabbedPane) categoriesControl).setTabPlacement(getCategoriesControlPlacement());
			}
		}
		return true;
	}

	public InfoCategory getDisplayedCategory() {
		if (categoriesControl != null) {
			int currentCategoryIndex;
			if (categoriesControl instanceof ListTabbedPane) {
				currentCategoryIndex = ((ListTabbedPane) categoriesControl).getSelectedIndex();
			} else if (categoriesControl instanceof JTabbedPane) {
				currentCategoryIndex = ((JTabbedPane) categoriesControl).getSelectedIndex();
			} else {
				throw new ReflectionUIError();
			}
			if (currentCategoryIndex != -1) {
				String currentCategoryCaption;
				if (categoriesControl instanceof ListTabbedPane) {
					currentCategoryCaption = ((ListTabbedPane) categoriesControl).getTitleAt(currentCategoryIndex);
				} else if (categoriesControl instanceof JTabbedPane) {
					currentCategoryCaption = ((JTabbedPane) categoriesControl).getTitleAt(currentCategoryIndex);
				} else {
					throw new ReflectionUIError();
				}
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
			if (categoriesControl instanceof ListTabbedPane) {
				tabCount = ((ListTabbedPane) categoriesControl).getTabCount();
			} else if (categoriesControl instanceof JTabbedPane) {
				tabCount = ((JTabbedPane) categoriesControl).getTabCount();
			} else {
				throw new ReflectionUIError();
			}
			for (int i = 0; i < tabCount; i++) {
				String currentCategoryCaption;
				if (categoriesControl instanceof ListTabbedPane) {
					currentCategoryCaption = ((ListTabbedPane) categoriesControl).getTitleAt(i);
				} else if (categoriesControl instanceof JTabbedPane) {
					currentCategoryCaption = ((JTabbedPane) categoriesControl).getTitleAt(i);
				} else {
					throw new ReflectionUIError();
				}
				if (categoryCaption.equals(currentCategoryCaption)) {
					if (categoryPosition != -1) {
						if (categoryPosition != i) {
							continue;
						}
					}
					if (categoriesControl instanceof ListTabbedPane) {
						((ListTabbedPane) categoriesControl).setSelectedIndex(i);
					} else if (categoriesControl instanceof JTabbedPane) {
						((JTabbedPane) categoriesControl).setSelectedIndex(i);
					} else {
						throw new ReflectionUIError();
					}
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
		if (refreshStructure && refreshMemberControlListsAndDetectModification()) {
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
			Color borderColor = getControlsBorderColor();
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

	public Color getControlsForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormForegroundColor() == null) {
			IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
			if (appInfo.getMainForegroundColor() == null) {
				return null;
			} else {
				return SwingRendererUtils.getColor(appInfo.getMainForegroundColor());
			}
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
		IApplicationInfo appInfo = reflectionUI.getApplicationInfo();
		if (appInfo.getMainBorderColor() == null) {
			return null;
		} else {
			return SwingRendererUtils.getColor(appInfo.getMainBorderColor());
		}
	}

	public Image getControlsBackgroundImage() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBackgroundImagePath() == null) {
			return null;
		} else {
			return SwingRendererUtils.loadImageThroughcache(type.getFormBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI));
		}
	}

	public boolean refreshMemberControlListsAndDetectModification() {

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
		MenuModel menuModel = new MenuModel();
		addMenuContribution(menuModel);
		menuBar.removeAll();
		for (Menu menu : menuModel.getMenus()) {
			menuBar.add(createMenu(menu));
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

	public JMenu createMenu(Menu menu) {
		JMenu result = new JMenu(menu.getName());
		for (AbstractMenuItem item : menu.getItems()) {
			result.add(createMenuItem(item));
		}
		for (int i = 0; i < menu.getItemCategories().size(); i++) {
			if (i > 0) {
				result.addSeparator();
			}
			MenuItemCategory category = menu.getItemCategories().get(i);
			for (AbstractMenuItem item : category.getItems()) {
				result.add(createMenuItem(item));
			}
		}
		return result;
	}

	public JMenuItem createMenuItem(AbstractMenuItem item) {
		if (item instanceof AbstractBuiltInActionMenuItem) {
			return createMenuActionItem((AbstractBuiltInActionMenuItem) item);
		} else if (item instanceof MethodActionMenuItem) {
			return createMenuActionItem((MethodActionMenuItem) item);
		} else if (item instanceof Menu) {
			return createMenu((Menu) item);
		} else {
			throw new ReflectionUIError("Unhandled menu item type: '" + item + "'");
		}
	}

	public JMenuItem createMenuActionItem(final MethodActionMenuItem actionMenuItem) {
		final Form form = swingRenderer.getFormByActionMenuItem().get(actionMenuItem);
		JMenuItem result = new JMenuItem(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					IMethodInfo method = actionMenuItem.getMethod();
					IMethodControlInput input = form.createMethodControlPlaceHolder(method);
					MethodAction methodAction = swingRenderer.createMethodAction(input);
					methodAction.onInvocationRequest((Form) form);
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(form, t);
				}
			}

		});
		try {
			result.setText(actionMenuItem.getName());
			ImageIcon icon = swingRenderer.getMenuItemIcon(actionMenuItem);
			if (icon != null) {
				icon = SwingRendererUtils.getSmallIcon(icon);
			}
			result.setIcon(icon);
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
			if (result.getText() == null) {
				result.setText(t.toString());
			} else {
				result.setText(result.getText() + "(" + t.toString() + ")");
			}
			result.setEnabled(false);
		}
		return result;
	}

	public JMenuItem createMenuActionItem(final AbstractBuiltInActionMenuItem actionMenuItem) {
		final Form form = swingRenderer.getFormByActionMenuItem().get(actionMenuItem);
		JMenuItem result = new JMenuItem(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					actionMenuItem.execute(form, swingRenderer);
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(form, t);
				}
			}

		});
		try {
			result.setText(actionMenuItem.getName(form, swingRenderer));
			if (!actionMenuItem.isEnabled(form, swingRenderer)) {
				result.setEnabled(false);
			}
			ImageIcon icon = swingRenderer.getMenuItemIcon(actionMenuItem);
			if (icon != null) {
				icon = SwingRendererUtils.getSmallIcon(icon);
			}
			result.setIcon(icon);
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
			if (result.getText() == null) {
				result.setText(t.toString());
			} else {
				result.setText(result.getText() + "(" + t.toString() + ")");
			}
			result.setEnabled(false);
		}
		return result;
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
			captionControlLayoutConstraints.anchor = GridBagConstraints.WEST;
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
		onlineHelpControl = createFieldOnlineHelpControl(field);
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

	public Component createFieldOnlineHelpControl(IFieldInfo field) {
		String onlineHelp = field.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Help");
		Image iconImage = swingRenderer.getObjectIconImage(title);
		return SwingRendererUtils.createOnlineHelpControl(onlineHelp, title, iconImage, swingRenderer);
	}

	public Component createButtonBarOnlineHelpControl() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		String onlineHelp = type.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Help");
		Image iconImage = swingRenderer.getObjectIconImage(title);
		return SwingRendererUtils.createOnlineHelpControl(onlineHelp, title, iconImage, swingRenderer);
	}

	public Component createSeparateFieldCaptionControl(FieldControlPlaceHolder fieldControlPlaceHolder) {
		IFieldControlData data = fieldControlPlaceHolder.getControlData();
		JLabel result = new JLabel(swingRenderer.prepareStringToDisplay(data.getCaption() + ": "));
		if (data.getForegroundColor() != null) {
			result.setForeground(SwingRendererUtils.getColor(data.getForegroundColor()));
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
				result.addAll(new ModificationStackControls(modificationStack).create(swingRenderer));
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
