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
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.swing.editor.AbstractEditorWindowBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.AbstractDelegatingInfoFilter;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.AbstractBufferedItemPositionFactory;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemDetailsAreaPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.SubListsGroupingField.SubListGroup;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.BufferedListModificationFactory;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.MethodControlDataModification;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractControlButton;
import xy.reflect.ui.util.component.AbstractLazyTreeNode;
import xy.reflect.ui.util.component.ControlPanel;
import xy.reflect.ui.util.component.ControlScrollPane;
import xy.reflect.ui.util.component.ControlSplitPane;
import xy.reflect.ui.util.component.ScrollPaneOptions;

public class ListControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected IFieldControlData listData;

	protected JXTreeTable treeTableComponent;
	protected JScrollPane treeTableComponentScrollPane;
	protected JPanel toolbar;
	protected ItemNode rootNode;
	protected AbstractBufferedItemPositionFactory itemPositionFactory;
	protected static List<Object> clipboard = new ArrayList<Object>();
	protected Map<ItemNode, Map<Integer, String>> valuesByNode = new HashMap<ItemNode, Map<Integer, String>>();
	protected IListStructuralInfo structuralInfo;

	protected JPanel detailsArea;
	protected Form detailsControl;
	protected ItemUIBuilder detailsControlBuilder;
	protected IListItemDetailsAccessMode detailsMode;
	protected BufferedItemPosition detailsControlItemPosition;
	protected Object detailsControlItem;

	protected List<Listener<List<BufferedItemPosition>>> selectionListeners = new ArrayList<Listener<List<BufferedItemPosition>>>();
	protected boolean selectionListenersEnabled = true;
	protected IFieldControlInput input;

	protected static AbstractAction SEPARATOR_ACTION = new AbstractAction("") {
		protected static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};

	public ListControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				IFieldControlData result = super.getControlData();
				result = SwingRendererUtils.handleErrors(swingRenderer, result, ListControl.this);
				return result;
			}
		};
		this.listData = input.getControlData();

		initializeTreeTableModelAndControl();
		toolbar = new ControlPanel();
		detailsArea = new ControlPanel();

		openDetailsDialogOnItemDoubleClick();
		updateDetailsAreaOnSelection();
		updateToolbarOnSelection();
		setupContexteMenu();
		updateToolbar();
		initializeSelectionListening();
		refreshUI(true);
	}

	public Object getRootListValue() {
		return itemPositionFactory.getRootListValue();
	}

	public String getRootListTitle() {
		return listData.getCaption();
	}

	protected void updateToolbarOnSelection() {
		selectionListeners.add(new Listener<List<BufferedItemPosition>>() {
			@Override
			public void handle(List<BufferedItemPosition> event) {
				updateToolbar();
			}
		});

	}

	protected void updateDetailsAreaOnSelection() {
		selectionListeners.add(new Listener<List<BufferedItemPosition>>() {
			@Override
			public void handle(List<BufferedItemPosition> event) {
				if (!getDetailsAccessMode().hasDetailsDisplayArea()) {
					return;
				}
				updateDetailsArea(false);
			}
		});
	}

	protected void layoutControls() {
		setLayout(new BorderLayout());
		if (getDetailsAccessMode().hasDetailsDisplayArea()) {
			JPanel listAndToolbarPanel = new ControlPanel();
			listAndToolbarPanel.setLayout(new BorderLayout());
			listAndToolbarPanel.add(BorderLayout.CENTER, treeTableComponentScrollPane);
			listAndToolbarPanel.add(toolbar, BorderLayout.EAST);
			ControlScrollPane listAndToolbarScrollPane = new ControlScrollPane(listAndToolbarPanel);
			SwingRendererUtils.removeScrollPaneBorder(listAndToolbarScrollPane);
			final JSplitPane splitPane = new ControlSplitPane();
			add(splitPane, BorderLayout.CENTER);
			final double dividerLocation;
			if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.RIGHT) {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setLeftComponent(listAndToolbarScrollPane);
				splitPane.setRightComponent(detailsArea);
				dividerLocation = 1.0 - getDetailsAccessMode().getDefaultDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.LEFT) {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setRightComponent(listAndToolbarScrollPane);
				splitPane.setLeftComponent(detailsArea);
				dividerLocation = getDetailsAccessMode().getDefaultDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.BOTTOM) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane.setTopComponent(listAndToolbarScrollPane);
				splitPane.setBottomComponent(detailsArea);
				dividerLocation = 1.0 - getDetailsAccessMode().getDefaultDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.TOP) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane.setBottomComponent(listAndToolbarScrollPane);
				splitPane.setTopComponent(detailsArea);
				dividerLocation = getDetailsAccessMode().getDefaultDetailsAreaOccupationRatio();
			} else {
				throw new ReflectionUIError();
			}
			splitPane.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent event) {
					if (splitPane.isShowing()) {
						splitPane.setDividerLocation(dividerLocation);
						splitPane.setResizeWeight(dividerLocation);
						splitPane.removeComponentListener(this);
					}
				}
			});
		} else {
			add(treeTableComponentScrollPane, BorderLayout.CENTER);
			add(toolbar, BorderLayout.EAST);
		}
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	protected void updateToolbar() {
		toolbar.removeAll();

		GridBagLayout layout = new GridBagLayout();
		toolbar.setLayout(layout);

		if (getDetailsAccessMode().hasDetailsDisplayOption()) {
			if (getRootListType().canViewItemDetails()) {
				toolbar.add(createTool(null, SwingRendererUtils.DETAILS_ICON, true, false, createOpenItemAction()));
			}
		}

		AbstractAction addChildAction = createAddChildAction();
		AbstractAction insertAction = createInsertAction(InsertPosition.UNKNOWN);
		AbstractAction insertActionBefore = createInsertAction(InsertPosition.BEFORE);
		AbstractAction insertActionAfter = createInsertAction(InsertPosition.AFTER);
		toolbar.add(createTool(null, SwingRendererUtils.ADD_ICON, false, false, addChildAction, insertAction,
				insertActionBefore, insertActionAfter));
		toolbar.add(createTool(null, SwingRendererUtils.REMOVE_ICON, false, false, createRemoveAction()));
		AbstractStandardListAction moveUpAction = createMoveAction(-1);
		AbstractStandardListAction moveDownAction = createMoveAction(1);
		if (moveUpAction.isValid() || moveDownAction.isValid()) {
			toolbar.add(createTool(null, SwingRendererUtils.UP_ICON, true, false, moveUpAction));
			toolbar.add(createTool(null, SwingRendererUtils.DOWN_ICON, true, false, moveDownAction));
		}

		Mapper<ItemPosition, ListModificationFactory> modificationFactoryAccessor = new Mapper<ItemPosition, ListModificationFactory>() {
			@Override
			public ListModificationFactory get(ItemPosition itemPosition) {
				return createListModificationFactory((BufferedItemPosition) itemPosition);
			}
		};
		List<IDynamicListProperty> dynamicProperties = getRootListType().getDynamicProperties(getSelection(),
				modificationFactoryAccessor);
		List<IDynamicListAction> dynamicActions = getRootListType().getDynamicActions(getSelection(),
				modificationFactoryAccessor);
		if ((dynamicProperties.size() > 0) || (dynamicActions.size() > 0)) {
			for (IDynamicListProperty listProperty : dynamicProperties) {
				AbstractAction dynamicPropertyHook = createDynamicPropertyHook(listProperty);
				toolbar.add(createTool((String) dynamicPropertyHook.getValue(AbstractAction.NAME), null, true, false,
						dynamicPropertyHook));
			}
			for (IDynamicListAction listAction : dynamicActions) {
				AbstractAction dynamicActionHook = createDynamicActionHook(listAction);
				toolbar.add(createTool((String) dynamicActionHook.getValue(AbstractAction.NAME), null, true, false,
						dynamicActionHook));
			}
		}

		toolbar.add(new JSeparator(JSeparator.VERTICAL));

		for (int i = 0; i < toolbar.getComponentCount(); i++) {
			Component c = toolbar.getComponent(i);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			if (c instanceof JSeparator) {
				JSeparator separator = (JSeparator) c;
				if (separator.getOrientation() == JSeparator.HORIZONTAL) {
					constraints.insets = new Insets(10, 0, 10, 0);
				} else if (separator.getOrientation() == JSeparator.VERTICAL) {
					constraints.weighty = 1;
				} else {
					throw new ReflectionUIError();
				}
			} else {
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.insets = new Insets(1, 5, 1, 5);
			}
			layout.setConstraints(c, constraints);
		}

		SwingRendererUtils.handleComponentSizeChange(ListControl.this);
		toolbar.repaint();
	}

	protected JButton createTool(final String text, final Icon icon, boolean alwawsShowIcon,
			final boolean alwawsShowMenu, AbstractAction... actions) {
		final List<AbstractAction> actionsToPresent = new ArrayList<AbstractAction>();
		for (final AbstractAction action : actions) {
			if (action == null) {
				continue;
			}
			if (action.isEnabled()) {
				actionsToPresent.add(action);
			}
		}
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public Image retrieveBackgroundImage() {
				if (listData.getButtonBackgroundImagePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadImageThroughCache(listData.getButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
				}
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (listData.getButtonBackgroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(listData.getButtonBackgroundColor());
				}
			}

			@Override
			public Color retrieveForegroundColor() {
				if (listData.getButtonForegroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(listData.getButtonForegroundColor());
				}
			}

			@Override
			public Color retrieveBorderColor() {
				if (listData.getButtonBorderColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(listData.getButtonBorderColor());
				}
			}

			@Override
			public String retrieveCaption() {
				return text;
			}

			@Override
			public Icon retrieveIcon() {
				return icon;
			}

			@Override
			public String retrieveToolTipText() {
				if (actionsToPresent.size() > 0) {
					if (actionsToPresent.size() == 1) {
						String tooltipText = (String) actionsToPresent.get(0).getValue(Action.SHORT_DESCRIPTION);
						if (tooltipText == null) {
							tooltipText = (String) actionsToPresent.get(0).getValue(Action.NAME);
						}
						return tooltipText;
					} else if (actionsToPresent.size() > 1) {
						StringBuilder tooltipTextBuilder = new StringBuilder();
						boolean firstAction = true;
						for (AbstractAction action : actionsToPresent) {
							if (!firstAction) {
								tooltipTextBuilder.append("\nor\n");
							}
							String itemTooltipText = (String) action.getValue(Action.SHORT_DESCRIPTION);
							if (itemTooltipText == null) {
								itemTooltipText = (String) action.getValue(Action.NAME);
							}
							tooltipTextBuilder.append(itemTooltipText);
							firstAction = false;
						}
						return tooltipTextBuilder.toString();
					} else {
						return null;
					}
				} else {
					return null;
				}
			}

		};

		result.setFocusable(false);
		if (actionsToPresent.size() > 0) {
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!alwawsShowMenu && (actionsToPresent.size() == 1)) {
						actionsToPresent.get(0).actionPerformed(null);
					} else {
						final JPopupMenu popupMenu = new JPopupMenu();
						for (AbstractAction action : actionsToPresent) {
							popupMenu.add(action);
						}
						popupMenu.show(result, result.getWidth(), result.getHeight());
					}
				}
			});
		} else {
			result.setEnabled(false);
			if (!alwawsShowIcon) {
				result.setVisible(false);
			}
		}
		return result;
	}

	protected void setupContexteMenu() {
		treeTableComponent.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) {
					return;
				}
				selectOnRighClick(e);
				if (e.getComponent() == treeTableComponent) {
					JPopupMenu popup = createPopupMenu();
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			protected void selectOnRighClick(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) {
					return;
				}
				int row = treeTableComponent.rowAtPoint(e.getPoint());
				if (treeTableComponent.isRowSelected(row)) {
					return;
				}
				if (row >= 0 && row < treeTableComponent.getRowCount()) {
					treeTableComponent.setRowSelectionInterval(row, row);
				} else {
					treeTableComponent.clearSelection();
				}
			}
		});
	}

	protected JPopupMenu createPopupMenu() {
		JPopupMenu result = new JPopupMenu();
		for (Action action : createCurrentSelectionActions()) {
			if (action == SEPARATOR_ACTION) {
				result.add(new JSeparator());
				continue;
			}
			JMenuItem menuItem = new JMenuItem(action);
			result.add(menuItem);
		}
		return result;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	public IListStructuralInfo getStructuralInfo() {
		if (structuralInfo == null) {
			IListTypeInfo listType = getRootListType();
			structuralInfo = listType.getStructuralInfo();
			if (structuralInfo == null) {
				throw new ReflectionUIError("No " + IListStructuralInfo.class.getSimpleName() + " found on the type '"
						+ listType.getName());
			}
		}
		return structuralInfo;
	}

	public IListItemDetailsAccessMode getDetailsAccessMode() {
		if (detailsMode == null) {
			IListTypeInfo listType = getRootListType();
			detailsMode = listType.getDetailsAccessMode();
			if (detailsMode == null) {
				throw new ReflectionUIError("No " + IListItemDetailsAccessMode.class.getSimpleName()
						+ " found on the type '" + listType.getName());
			}
		}
		return detailsMode;
	}

	protected void initializeTreeTableModelAndControl() {
		itemPositionFactory = createItemPositionfactory();
		rootNode = createRootNode();
		treeTableComponent = new JXTreeTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent event) {
				try {
					return super.getToolTipText(event);
				} catch (Throwable t) {
					return null;
				}
			}
		};
		treeTableComponentScrollPane = createScrollPane();
		treeTableComponentScrollPane.setViewportView(treeTableComponent);
		treeTableComponent.setExpandsSelectedPaths(true);
		treeTableComponent.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		treeTableComponent.setRootVisible(false);
		treeTableComponent.setShowsRootHandles(true);
		treeTableComponent.setDefaultRenderer(Object.class, new ItemTableCellRenderer());
		treeTableComponent.setTreeCellRenderer(new ItemTreeCellRenderer());
		treeTableComponent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		treeTableComponent.setHorizontalScrollEnabled(true);
		treeTableComponent.setColumnMargin(5);
		treeTableComponent.getTableHeader().setReorderingAllowed(false);
		treeTableComponent.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingRendererUtils.handleComponentSizeChange(ListControl.this);
					}
				});
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingRendererUtils.handleComponentSizeChange(ListControl.this);
					}
				});
			}
		});
	}

	protected JScrollPane createScrollPane() {
		return new ControlScrollPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				IListStructuralInfo structure = getStructuralInfo();
				if (structure.getLength() != -1) {
					result.height = structure.getLength();
				}
				return result;
			}
		};
	}

	protected AbstractBufferedItemPositionFactory createItemPositionfactory() {
		return new ItemPositionfactory();
	}

	protected TreeTableModel createTreeTableModel() {
		return new AbstractTreeTableModel(rootNode) {

			@Override
			public int getIndexOfChild(Object parent, Object child) {
				return ((ItemNode) parent).getIndex((ItemNode) child);
			}

			@Override
			public int getChildCount(Object parent) {
				return ((ItemNode) parent).getChildCount();
			}

			@Override
			public Object getChild(Object parent, int index) {
				return ((ItemNode) parent).getChildAt(index);
			}

			@Override
			public Object getValueAt(Object arg0, int fieldIndex) {
				return getCellValue((ItemNode) arg0, fieldIndex);
			}

			@Override
			public int getColumnCount() {
				return ListControl.this.getColumnCount();
			}

			@Override
			public String getColumnName(int column) {
				return swingRenderer.prepareStringToDisplay(getColumnCaption(column));
			}

		};
	}

	protected ItemNode createRootNode() {
		return new ItemNode(null);
	}

	public BufferedItemPosition getRootListItemPosition(int index) {
		return itemPositionFactory.getRootItemPosition(index);
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	public String getColumnCaption(int columnIndex) {
		if (getStructuralInfo() == null) {
			return "";
		}
		IListStructuralInfo tableInfo = getStructuralInfo();
		return tableInfo.getColumns().get(columnIndex).getCaption();
	}

	public int getColumnCount() {
		if (getStructuralInfo() == null) {
			return 1;
		}
		IListStructuralInfo tableInfo = getStructuralInfo();
		return tableInfo.getColumns().size();
	}

	protected String getCellValue(ItemNode node, int columnIndex) {
		Map<Integer, String> nodeValues = valuesByNode.get(node);
		if (nodeValues == null) {
			nodeValues = new HashMap<Integer, String>();
			valuesByNode.put(node, nodeValues);
		}
		String value;
		if (nodeValues.containsKey(columnIndex)) {
			value = nodeValues.get(columnIndex);
		} else {
			BufferedItemPosition itemPosition = (BufferedItemPosition) node.getUserObject();
			if (itemPosition == null) {
				value = "";
			} else {
				Object item = itemPosition.getItem();
				if (item instanceof SubListGroup) {
					if (columnIndex == 0) {
						value = item.toString();
					} else {
						return null;
					}
				} else {
					IListStructuralInfo tableInfo = getStructuralInfo();
					if (tableInfo == null) {
						value = ReflectionUIUtils.toString(swingRenderer.getReflectionUI(), item);
					} else {
						List<IColumnInfo> columns = tableInfo.getColumns();
						if (columnIndex < columns.size()) {
							IColumnInfo column = tableInfo.getColumns().get(columnIndex);
							if (column.hasCellValue(itemPosition)) {
								value = column.getCellValue(itemPosition);
							} else {
								value = null;
							}
						} else {
							value = null;
						}
					}
				}
			}
			nodeValues.put(columnIndex, value);
		}
		return value;
	}

	protected Image getCellIconImage(ItemNode node, int columnIndex) {
		BufferedItemPosition itemPosition = (BufferedItemPosition) node.getUserObject();
		if (columnIndex == 0) {
			return swingRenderer.getObjectIconImage(itemPosition.getItem());
		}
		return null;
	}

	protected List<AbstractAction> createCurrentSelectionActions() {

		List<AbstractAction> result = new ArrayList<AbstractAction>();

		List<BufferedItemPosition> selection = getSelection();

		AbstractStandardListAction standardAction;

		standardAction = createOpenItemAction();
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		result.add(SEPARATOR_ACTION);

		standardAction = createAddChildAction();
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createInsertAction(InsertPosition.BEFORE);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createInsertAction(InsertPosition.AFTER);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createInsertAction(InsertPosition.UNKNOWN);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		result.add(SEPARATOR_ACTION);

		standardAction = createCopyAction();
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createCutAction();
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createPasteAction(InsertPosition.BEFORE);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createPasteAction(InsertPosition.AFTER);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createPasteAction(InsertPosition.UNKNOWN);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createPasteIntoAction();
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		result.add(SEPARATOR_ACTION);

		standardAction = createMoveAction(-1);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createMoveAction(1);
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		result.add(SEPARATOR_ACTION);

		standardAction = createRemoveAction();
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		standardAction = createClearAction();
		if (standardAction.isValid()) {
			result.add(standardAction);
		}

		result.add(SEPARATOR_ACTION);

		Mapper<ItemPosition, ListModificationFactory> modificationFactoryAccessor = new Mapper<ItemPosition, ListModificationFactory>() {
			@Override
			public ListModificationFactory get(ItemPosition itemPosition) {
				return createListModificationFactory((BufferedItemPosition) itemPosition);
			}
		};
		for (IDynamicListProperty listProperty : getRootListType().getDynamicProperties(selection,
				modificationFactoryAccessor)) {
			result.add(createDynamicPropertyHook(listProperty));
		}
		for (IDynamicListAction listAction : getRootListType().getDynamicActions(selection,
				modificationFactoryAccessor)) {
			result.add(createDynamicActionHook(listAction));
		}

		result = removeSeparatorsInExcess(result);
		return result;
	}

	protected List<AbstractAction> removeSeparatorsInExcess(List<AbstractAction> actions) {
		List<AbstractAction> result = new ArrayList<AbstractAction>();
		AbstractAction lastAction = null;
		for (AbstractAction action : actions) {
			if (action == SEPARATOR_ACTION) {
				if (lastAction == SEPARATOR_ACTION) {
					continue;
				}
			}
			result.add(action);
			lastAction = action;
		}
		if (result.size() > 0) {
			if (SEPARATOR_ACTION == result.get(0)) {
				result.remove(0);
			}
		}
		if (result.size() > 0) {
			if (SEPARATOR_ACTION == result.get(result.size() - 1)) {
				result.remove(result.size() - 1);
			}
		}
		return result;
	}

	protected boolean canCopyAll(List<BufferedItemPosition> selection) {
		boolean result = true;
		for (BufferedItemPosition selectionItem : selection) {
			if (!ReflectionUIUtils.canCopy(swingRenderer.getReflectionUI(), selectionItem.getItem())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean canRemoveAll(List<BufferedItemPosition> selection) {
		boolean result = true;
		for (BufferedItemPosition selectionItem : selection) {
			if (!createListModificationFactory(selectionItem).canRemove(selectionItem.getIndex())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean canMoveAll(List<BufferedItemPosition> selection, int offset) {
		boolean result = true;
		for (BufferedItemPosition selectionItem : selection) {
			if (!createListModificationFactory(selectionItem).canMove(selectionItem.getIndex(), offset)) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected ListModificationFactory createListModificationFactory(BufferedItemPosition anyListItemPosition) {
		Mapper<Object, IModification> commitModifAccessor = new Mapper<Object, IModification>() {
			@Override
			public IModification get(Object rootListValue) {
				if (listData.isGetOnly()) {
					return IModification.NULL_MODIFICATION;
				} else {
					return new FieldControlDataModification(listData, rootListValue);
				}
			}
		};
		return new BufferedListModificationFactory(anyListItemPosition, commitModifAccessor);
	}

	protected boolean allSelectionItemsInSameList() {
		boolean result = true;
		List<BufferedItemPosition> selection = getSelection();
		BufferedItemPosition firstSelectionItem = selection.get(0);
		for (BufferedItemPosition selectionItem : selection) {
			if (!ReflectionUIUtils.equalsOrBothNull(firstSelectionItem.getParentItemPosition(),
					selectionItem.getParentItemPosition())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected AbstractStandardListAction createClearAction() {
		return new ClearAction();
	}

	protected boolean userConfirms(String question) {
		return swingRenderer.openQuestionDialog(SwingUtilities.getWindowAncestor(ListControl.this), question, null,
				"OK", "Cancel");
	}

	protected AbstractStandardListAction createMoveAction(final int offset) {
		return new MoveAction(offset);

	}

	public BufferedItemPosition getSingleSelection() {
		List<BufferedItemPosition> selection = getSelection();
		if ((selection.size() == 0) || (selection.size() > 1)) {
			return null;
		} else {
			return selection.get(0);
		}
	}

	public List<BufferedItemPosition> getSelection() {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		for (int selectedRow : treeTableComponent.getSelectedRows()) {
			TreePath path = treeTableComponent.getPathForRow(selectedRow);
			if (path == null) {
				return null;
			}
			ItemNode selectedNode = (ItemNode) path.getLastPathComponent();
			BufferedItemPosition bufferedItemPosition = (BufferedItemPosition) selectedNode.getUserObject();
			result.add(bufferedItemPosition);
		}
		return result;
	}

	public void setSingleSelection(BufferedItemPosition toSelect) {
		setSelection(Collections.singletonList(toSelect));
	}

	public BufferedItemPosition findItemPositionByReference(final Object item) {
		final BufferedItemPosition[] result = new BufferedItemPosition[1];
		visitItems(new IItemsVisitor() {
			@Override
			public boolean visitItem(BufferedItemPosition itemPosition) {
				if (itemPosition.getItem() == item) {
					result[0] = itemPosition;
					return false;
				}
				return true;
			}
		});
		return result[0];
	}

	public void setSelection(List<BufferedItemPosition> toSelect) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (int i = 0; i < toSelect.size(); i++) {
			BufferedItemPosition itemPosition = toSelect.get(i);
			ItemNode itemNode = findNode(itemPosition);
			if (itemNode == null) {
				BufferedItemPosition parentItemPosition = itemPosition.getParentItemPosition();
				if (parentItemPosition == null) {
					treeTableComponent.clearSelection();
					return;
				}
				toSelect = new ArrayList<BufferedItemPosition>(toSelect);
				toSelect.set(i, parentItemPosition);
				setSelection(toSelect);
				return;
			}
			treePaths.add(new TreePath(itemNode.getPath()));
		}
		treeTableComponent.getTreeSelectionModel().setSelectionPaths(treePaths.toArray(new TreePath[treePaths.size()]));
	}

	public void scrollTo(BufferedItemPosition itemPosition) {
		ItemNode itemNode = findNode(itemPosition);
		if (itemNode == null) {
			return;
		}
		TreePath treePath = new TreePath(itemNode.getPath());
		try {
			treeTableComponent.scrollRowToVisible(treeTableComponent.getRowForPath(treePath));
		} catch (Throwable ignore) {
		}
	}

	protected ItemNode findNode(BufferedItemPosition itemPosition) {
		ItemNode parentNode;
		if (itemPosition.isRoot()) {
			parentNode = rootNode;
		} else {
			parentNode = findNode(itemPosition.getParentItemPosition());
		}
		if (parentNode == null) {
			return null;
		}
		if (itemPosition.getIndex() < 0) {
			return null;
		}
		if (itemPosition.getIndex() >= parentNode.getChildCount()) {
			return null;
		}
		ItemNode result = (ItemNode) parentNode.getChildAt(itemPosition.getIndex());
		return result;
	}

	protected AbstractStandardListAction createRemoveAction() {
		return new RemoveAction();
	}

	protected void updatePositionsAfterItemRemoval(List<BufferedItemPosition> toUpdate, BufferedItemPosition removed) {
		for (int i = 0; i < toUpdate.size(); i++) {
			BufferedItemPosition toUpdateItem = toUpdate.get(i);
			if (toUpdateItem.equals(removed) || toUpdateItem.getAncestors().contains(removed)) {
				toUpdate.remove(i);
				i--;
			} else if (toUpdateItem.getPreviousSiblings().contains(removed)) {
				toUpdate.set(i, toUpdateItem.getSibling(toUpdateItem.getIndex() - 1));
			}
		}
	}

	protected AbstractStandardListAction createInsertAction(final InsertPosition insertPosition) {
		return new InsertAction(insertPosition);
	}

	protected ItemUIBuilder openAnticipatedItemDialog(BufferedItemPosition anticipatedItemPosition,
			Object anticipatedItem) {
		BufferedItemPosition fakeItemPosition = anticipatedItemPosition.getSibling(-1);
		fakeItemPosition.setFakeItem(anticipatedItem);
		ItemUIBuilder dialogBuilder = new ItemUIBuilder(fakeItemPosition) {
			ModificationStack dummyModificationStack = new ModificationStack(null);

			@Override
			public ModificationStack getParentModificationStack() {
				return dummyModificationStack;
			}

			@Override
			public boolean canCommitToParent() {
				return true;
			}

			@Override
			public IModification createParentCommitModification(Object newObjectValue) {
				return IModification.NULL_MODIFICATION;
			}

			@Override
			public boolean isCancellable() {
				return true;
			}
		};
		dialogBuilder.createAndShowDialog();
		return dialogBuilder;
	}

	protected AbstractStandardListAction createAddChildAction() {
		return new AddChildAction();
	};

	protected String getItemTitle(BufferedItemPosition itemPosition) {
		Object encapsulatedObject = new ItemUIBuilder(itemPosition).getCapsule();
		ITypeInfo encapsulatedObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(encapsulatedObject));
		return encapsulatedObjectType.getCaption();
	}

	protected Object createItem(BufferedItemPosition itemPosition) {
		IListTypeInfo subListType = itemPosition.getContainingListType();
		ITypeInfo typeToInstanciate = subListType.getItemType();
		if (typeToInstanciate == null) {
			typeToInstanciate = new DefaultTypeInfo(swingRenderer.getReflectionUI(),
					new JavaTypeInfoSource(Object.class, null));
		}
		return listData.createValue(typeToInstanciate, subListType.isItemConstructorSelectable());
	}

	protected AbstractStandardListAction createCopyAction() {
		return new CopyAction();
	}

	protected AbstractStandardListAction createCutAction() {
		return new CutAction();
	}

	protected BufferedItemPosition getPositionsAffectedByItemRemoval(BufferedItemPosition itemPosition) {
		if (itemPosition.getContainingListType().isOrdered() && (itemPosition.getIndex() > 0)) {
			return itemPosition.getSibling(itemPosition.getIndex() - 1);
		} else {
			return itemPosition.getParentItemPosition();
		}
	}

	protected AbstractStandardListAction createPasteAction(final InsertPosition insertPosition) {
		return new PasteAction(insertPosition);
	}

	protected AbstractStandardListAction createPasteIntoAction() {
		return new PasteIntoAction();
	}

	public ITypeInfo getRootListItemType() {
		return getRootListType().getItemType();
	}

	public IListTypeInfo getRootListType() {
		return (IListTypeInfo) listData.getType();
	}

	protected AbstractAction createDynamicPropertyHook(final IDynamicListProperty dynamicProperty) {
		return new DynamicPropertyHook(dynamicProperty);
	}

	protected AbstractAction createDynamicActionHook(final IDynamicListAction dynamicAction) {
		return new DynamicActionHook(dynamicAction);
	}

	protected AbstractStandardListAction createOpenItemAction() {
		return new OpenItemAction();
	}

	protected String getItemModificationTitle() {
		return "Edit '" + getRootListTitle() + "' item";
	}

	public void addListControlSelectionListener(Listener<List<BufferedItemPosition>> listener) {
		selectionListeners.add(listener);
	}

	public void removeListControlSelectionListener(Listener<List<BufferedItemPosition>> listener) {
		selectionListeners.remove(listener);
	}

	protected void initializeSelectionListening() {
		treeTableComponent.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (!selectionListenersEnabled) {
					return;
				}
				try {
					fireSelectionEvent();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		});
	}

	protected void fireSelectionEvent() {
		List<BufferedItemPosition> newSelection = getSelection();
		for (Listener<List<BufferedItemPosition>> listener : selectionListeners) {
			try {
				listener.handle(newSelection);
			} catch (Throwable t) {
				swingRenderer.getReflectionUI().logError(t);
			}
		}
		try {
			getRootListType().onSelection(newSelection);
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
		}
	}

	protected void updateDetailsArea(boolean refreshStructure) {
		BufferedItemPosition singleSelection = getSingleSelection();
		if (!ReflectionUIUtils.equalsOrBothNull(singleSelection, detailsControlItemPosition)) {
			detailsControlItemPosition = singleSelection;
			if (detailsControl != null) {
				detailsArea.removeAll();
				detailsControlItem = null;
				detailsControl = null;
				if (detailsControlItemPosition == null) {
					SwingRendererUtils.handleComponentSizeChange(detailsArea);
				}
			}
		}
		if (detailsControlItemPosition == null) {
			return;
		}
		if (detailsControl != null) {
			detailsControlBuilder.refreshEditorForm(detailsControl, refreshStructure);
		} else {
			detailsArea.removeAll();
			detailsArea.setLayout(new BorderLayout());
			detailsControlBuilder = new ItemUIBuilder(detailsControlItemPosition);
			detailsControl = detailsControlBuilder.createEditorForm(true, false);
			Component statusBar = detailsControl.getStatusBar();
			{
				detailsArea.add(statusBar, BorderLayout.NORTH);
			}
			detailsArea.add(createDetailsAreaScrollPane(detailsControl), BorderLayout.CENTER);
			detailsControl.validateFormInBackgroundAndReportOnStatusBar();
			SwingRendererUtils.handleComponentSizeChange(detailsArea);
			SwingRendererUtils.requestAnyComponentFocus(detailsControl, swingRenderer);
			scrollUntilVisible(detailsControlItemPosition);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					scrollUntilVisible(detailsControlItemPosition);
				}
			});
		}
	}

	protected Component createDetailsAreaScrollPane(Form detailsControl) {
		ControlScrollPane result = new ControlScrollPane(new ScrollPaneOptions(detailsControl, true, false));
		SwingRendererUtils.removeScrollPaneBorder(result);
		return result;
	}

	public void scrollUntilVisible(BufferedItemPosition itemPosition) {
		ItemNode node = findNode(itemPosition);
		if (node == null) {
			return;
		}
		TreePath treePath = new TreePath(node.getPath());
		int row = treeTableComponent.getRowForPath(treePath);
		treeTableComponent.scrollRowToVisible(row);
	}

	protected void openDetailsDialogOnItemDoubleClick() {
		treeTableComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if (!getDetailsAccessMode().hasDetailsDisplayOption()) {
					return;
				}
				try {
					if (me.getClickCount() != 2) {
						return;
					}
					int row = treeTableComponent.rowAtPoint(me.getPoint());
					if (row == -1) {
						return;
					}
					AbstractStandardListAction action = createOpenItemAction();
					if (!action.isValid()) {
						return;
					}
					action.actionPerformed(null);
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(treeTableComponent, t);
				}
			}
		});
	}

	public Object[] getRootListRawValue() {
		return itemPositionFactory.getRootItemPosition(-1).retrieveContainingListRawValue();
	}

	public int getRootListSize() {
		return itemPositionFactory.getRootItemPosition(-1).getContainingListSize();
	}

	public BufferedItemPosition getActiveListItemPosition() {
		BufferedItemPosition result = getSingleSelection();
		if (result == null) {
			result = itemPositionFactory.getRootItemPosition(-1);
			if (result.getContainingListSize() > 0) {
				result = result.getSibling(result.getContainingListSize() - 1);
			}
		}
		return result;
	}

	protected ModificationStack getModificationStack() {
		return input.getModificationStack();
	}

	protected void refreshTreeTableModelAndControl(final boolean refreshStructure) {
		restoringColumnWidthsAsMuchAsPossible(new Runnable() {
			@Override
			public void run() {
				valuesByNode.clear();
				itemPositionFactory = createItemPositionfactory();
				rootNode = createRootNode();
				treeTableComponent.setTreeTableModel(createTreeTableModel());
				if (refreshStructure) {
					TableColumnModel columnModel = treeTableComponent.getColumnModel();
					{
						List<IColumnInfo> columnInfos = getStructuralInfo().getColumns();
						for (int i = 0; i < columnInfos.size(); i++) {
							IColumnInfo columnInfo = columnInfos.get(i);
							TableColumn column = columnModel.getColumn(i);
							column.setPreferredWidth(columnInfo.getMinimalCharacterCount()
									* SwingRendererUtils.getStandardCharacterWidth(treeTableComponent));
						}
					}
				}
			}
		});

	}

	public void visitItems(IItemsVisitor iItemsVisitor) {
		visitItems(iItemsVisitor, rootNode);
	}

	protected boolean visitItems(IItemsVisitor iItemsVisitor, ItemNode currentNode) {
		BufferedItemPosition currentListItemPosition = (BufferedItemPosition) currentNode.getUserObject();
		if (currentListItemPosition != null) {
			if (!iItemsVisitor.visitItem(currentListItemPosition)) {
				return false;
			}
		}
		for (int i = 0; i < currentNode.getChildCount(); i++) {
			ItemNode childNode = (ItemNode) currentNode.getChildAt(i);
			if (!visitItems(iItemsVisitor, childNode)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean refreshUI(final boolean refreshStructure) {
		restoringSelectionAsMuchAsPossible(new Runnable() {
			@Override
			public void run() {
				refreshTreeTableModelAndControl(refreshStructure);
			}
		});
		if (refreshStructure) {
			removeAll();
			detailsMode = null;
			layoutControls();
			if (getDetailsAccessMode().hasDetailsDisplayArea()) {
				updateDetailsArea(true);
			}
			refreshTreeTableScrollPaneBorder();
			refreshTreeTableComponentBackground();
			refreshTreeTableComponentHeader();
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		return true;
	}

	protected void refreshTreeTableScrollPaneBorder() {
		treeTableComponentScrollPane.setBorder(
				BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(listData.getCaption())));
		{
			if (listData.getLabelForegroundColor() != null) {
				((TitledBorder) treeTableComponentScrollPane.getBorder())
						.setTitleColor(SwingRendererUtils.getColor(listData.getLabelForegroundColor()));
			}
			if (listData.getBorderColor() != null) {
				((TitledBorder) treeTableComponentScrollPane.getBorder()).setBorder(
						BorderFactory.createLineBorder(SwingRendererUtils.getColor(listData.getBorderColor())));
			}
		}
	}

	protected void refreshTreeTableComponentBackground() {
		if (listData.getEditorBackgroundColor() != null) {
			treeTableComponent.setBackground(SwingRendererUtils.getColor(listData.getEditorBackgroundColor()));
		} else {
			treeTableComponent.setBackground(new JXTreeTable().getBackground());
		}
		treeTableComponent.setDefaultRenderer(Object.class, new ItemTableCellRenderer());
	}

	protected void refreshTreeTableComponentHeader() {
		if (listData.getEditorBackgroundColor() != null) {
			treeTableComponent.getTableHeader()
					.setBackground(SwingRendererUtils.getColor(listData.getEditorBackgroundColor()));
		} else {
			treeTableComponent.getTableHeader().setBackground(new JXTreeTable().getTableHeader().getBackground());
		}
		if (listData.getEditorForegroundColor() != null) {
			treeTableComponent.getTableHeader()
					.setForeground(SwingRendererUtils.getColor(listData.getEditorForegroundColor()));
		} else {
			treeTableComponent.getTableHeader().setForeground(new JXTreeTable().getTableHeader().getForeground());
		}
	}

	@Override
	public boolean requestCustomFocus() {
		if (getRootListSize() > 0) {
			setSingleSelection(getRootListItemPosition(0));
		}
		if (SwingRendererUtils.requestAnyComponentFocus(treeTableComponent, swingRenderer)) {
			return true;
		}
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	protected void restoringColumnWidthsAsMuchAsPossible(Runnable runnable) {
		Map<String, Integer> preferredWidthByColumnName = new HashMap<String, Integer>();
		TableColumnModel columnModel = treeTableComponent.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			TableColumn col = columnModel.getColumn(i);
			preferredWidthByColumnName.put(col.getHeaderValue().toString(), col.getPreferredWidth());
		}

		runnable.run();

		columnModel = treeTableComponent.getColumnModel();
		List<IColumnInfo> columnInfos = getStructuralInfo().getColumns();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			TableColumn col = columnModel.getColumn(i);
			IColumnInfo columnInfo = columnInfos.get(i);
			Integer knownPreferredWidth = preferredWidthByColumnName.get(col.getHeaderValue().toString());
			if (knownPreferredWidth != null) {
				col.setPreferredWidth(knownPreferredWidth);
			} else {
				col.setPreferredWidth(columnInfo.getMinimalCharacterCount()
						* SwingRendererUtils.getStandardCharacterWidth(treeTableComponent));
			}
		}
	}

	protected void restoringSelectionAsMuchAsPossible(Runnable runnable) {
		List<BufferedItemPosition> wereSelectedPositions = getSelection();
		List<Object> wereSelected = new ArrayList<Object>();
		for (int i = 0; i < wereSelectedPositions.size(); i++) {
			try {
				BufferedItemPosition wasSelectedPosition = wereSelectedPositions.get(i);
				wereSelected.add(wasSelectedPosition.getItem());
			} catch (Throwable t) {
				wereSelected.add(null);
			}
		}

		selectionListenersEnabled = false;
		try {
			runnable.run();
			setSelection(Collections.<BufferedItemPosition>emptyList());
			int i = 0;
			for (Iterator<BufferedItemPosition> it = wereSelectedPositions.iterator(); it.hasNext();) {
				BufferedItemPosition wasSelectedPosition = it.next();
				try {
					if (!wasSelectedPosition.getContainingListType().isOrdered()) {
						Object wasSelected = wereSelected.get(i);
						int index = Arrays.asList(wasSelectedPosition.retrieveContainingListRawValue())
								.indexOf(wasSelected);
						wasSelectedPosition = wasSelectedPosition.getSibling(index);
						wereSelectedPositions.set(i, wasSelectedPosition);
					}
				} catch (Throwable t) {
					it.remove();
				}
				i++;
			}
			try {
				setSelection(wereSelectedPositions);
			} catch (Throwable ignore) {
			}
		} finally {
			selectionListenersEnabled = true;
		}
		fireSelectionEvent();
	}

	@Override
	public String toString() {
		return "ListControl [listData=" + listData + "]";
	}

	protected class ItemNode extends AbstractLazyTreeNode {

		protected static final long serialVersionUID = 1L;
		protected BufferedItemPosition currentItemPosition;
		protected boolean childrenLoaded = false;;

		public ItemNode(BufferedItemPosition currentItemPosition) {
			this.currentItemPosition = currentItemPosition;
			setUserObject(currentItemPosition);
		}

		@Override
		protected List<AbstractLazyTreeNode> createChildrenNodes() {
			List<AbstractLazyTreeNode> result = new ArrayList<AbstractLazyTreeNode>();
			if (currentItemPosition == null) {
				for (int i = 0; i < itemPositionFactory.getRootItemPosition(-1).getContainingListSize(); i++) {
					BufferedItemPosition rootItemPosition = itemPositionFactory.getRootItemPosition(i);
					ItemNode node = new ItemNode(rootItemPosition);
					result.add(node);
				}
			} else {
				for (BufferedItemPosition childItemPosition : currentItemPosition.getSubItemPositions()) {
					ItemNode node = new ItemNode(childItemPosition);
					result.add(node);
				}
			}
			return result;
		}

	}

	protected class RefreshStructureModification implements IModification {
		protected List<BufferedItemPosition> newSelection;

		public RefreshStructureModification(List<BufferedItemPosition> newSelection) {
			this.newSelection = newSelection;
		}

		@Override
		public IModification applyAndGetOpposite() {
			List<BufferedItemPosition> oldSelection = getSelection();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (newSelection == null) {
						restoringSelectionAsMuchAsPossible(new Runnable() {
							@Override
							public void run() {
								refreshTreeTableModelAndControl(false);
							}
						});
					} else {
						refreshTreeTableModelAndControl(false);
						setSelection(newSelection);
						if (newSelection.size() > 0) {
							scrollTo(newSelection.get(0));
						}
					}
				}
			});
			return new RefreshStructureModification(oldSelection);
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "Select " + newSelection;
		}

		@Override
		public boolean isNull() {
			return false;
		}

	}

	protected abstract class AbstractItemCellRenderer {

		protected void customizeCellRendererComponent(JLabel label, ItemNode node, int rowIndex, int columnIndex,
				boolean isSelected, boolean hasFocus) {
			label.putClientProperty("html.disable", Boolean.TRUE);
			if (!(node.getUserObject() instanceof BufferedItemPosition)) {
				return;
			}
			String text = getCellValue(node, columnIndex);
			if ((text == null) || (text.length() == 0)) {
				label.setText(" ");
			} else {
				label.setText(swingRenderer.prepareStringToDisplay(text));
			}

			Image iconImage = getCellIconImage(node, columnIndex);
			if (iconImage == null) {
				label.setIcon(null);
			} else {
				label.setIcon(new ImageIcon(iconImage));
			}

			if (!isSelected) {
				if (listData.getEditorForegroundColor() != null) {
					label.setForeground(SwingRendererUtils.getColor(listData.getEditorForegroundColor()));
				}
				if (listData.getEditorBackgroundColor() != null) {
					label.setBackground(SwingRendererUtils.getColor(listData.getEditorBackgroundColor()));
				}
			}

		}
	}

	protected class ItemTableCellRenderer extends AbstractItemCellRenderer implements TableCellRenderer {

		protected TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

		public ItemTableCellRenderer() {
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			row = treeTableComponent.convertRowIndexToModel(row);
			TreePath path = treeTableComponent.getPathForRow(row);
			if (path != null) {
				ItemNode node = (ItemNode) path.getLastPathComponent();
				customizeCellRendererComponent(label, node, row, column, isSelected, hasFocus);
			}
			return label;
		}

	}

	protected class ItemTreeCellRenderer extends AbstractItemCellRenderer implements TreeCellRenderer {

		protected TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
		protected JLabel component = new JLabel();

		public ItemTreeCellRenderer() {
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean isLeaf, int row, boolean focused) {
			JLabel defaultComponent = (JLabel) defaultRenderer.getTreeCellRendererComponent(tree, value, selected,
					expanded, isLeaf, row, focused);
			component.setForeground(defaultComponent.getForeground());
			component.setBackground(defaultComponent.getBackground());
			customizeCellRendererComponent(component, (ItemNode) value, row, 0, selected, focused);
			component.setOpaque(false);
			return component;
		}

	}

	protected abstract class AbstractStandardListAction extends AbstractAction {

		protected static final long serialVersionUID = 1L;

		protected abstract boolean prepare();

		protected abstract void perform(List<BufferedItemPosition>[] toPostSelectHolder);

		protected abstract String getActionTitle();

		protected abstract String getCompositeModificationTitle();

		protected abstract boolean isValid();

		@Override
		public Object getValue(String key) {
			if (Action.NAME.equals(key)) {
				return swingRenderer.prepareStringToDisplay(getActionTitle());
			} else if (Action.SHORT_DESCRIPTION.equals(key)) {
				String result = getActionDescription();
				if (result != null) {
					result = swingRenderer.prepareStringToDisplay(result);
				}
				return result;
			} else {
				return super.getValue(key);
			}
		}

		protected String getActionDescription() {
			return null;
		}

		@Override
		public boolean isEnabled() {
			return isValid();
		}

		@Override
		final public void actionPerformed(ActionEvent e) {
			try {
				if (!prepare()) {
					return;
				}
				swingRenderer.showBusyDialogWhile(ListControl.this, new Runnable() {
					public void run() {
						final String modifTitle = getCompositeModificationTitle();
						@SuppressWarnings("unchecked")
						final List<BufferedItemPosition>[] toPostSelectHolder = new List[1];
						if (modifTitle == null) {
							List<BufferedItemPosition> initialSelection = getSelection();
							perform(toPostSelectHolder);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									refreshTreeTableModelAndControl(false);
									if (toPostSelectHolder[0] != null) {
										setSelection(toPostSelectHolder[0]);
										if (toPostSelectHolder[0].size() > 0) {
											scrollTo(toPostSelectHolder[0].get(0));
										}
									} else {
										setSelection(initialSelection);
									}
								}
							});

						} else {
							final ModificationStack modifStack = getModificationStack();
							modifStack.insideComposite(modifTitle, UndoOrder.FIFO, new Accessor<Boolean>() {
								@Override
								public Boolean get() {
									if (modifStack.insideComposite(modifTitle + " (without list control update)",
											UndoOrder.getNormal(), new Accessor<Boolean>() {
												@Override
												public Boolean get() {
													perform(toPostSelectHolder);
													return true;
												}
											})) {
										modifStack.apply(new RefreshStructureModification(toPostSelectHolder[0]));
										return true;
									} else {
										modifStack.apply(new RefreshStructureModification(toPostSelectHolder[0]));
										return false;
									}
								}
							});

						}
					}
				}, getCompositeModificationTitle());
			} catch (Throwable t) {
				swingRenderer.handleExceptionsFromDisplayedUI(ListControl.this, t);
			}
			displayResult();
		}

		protected void displayResult() {
		}

	};

	protected class ItemUIBuilder extends AbstractEditorWindowBuilder {
		protected BufferedItemPosition bufferedItemPosition;
		protected ListModificationFactory modificationFactory;
		protected boolean canCommit;
		protected ValueReturnMode objectValueReturnMode;

		public ItemUIBuilder(BufferedItemPosition bufferedItemPosition) {
			super();
			this.bufferedItemPosition = bufferedItemPosition;
			this.modificationFactory = createListModificationFactory(bufferedItemPosition);
			this.canCommit = modificationFactory.canSet(bufferedItemPosition.getIndex());
			this.objectValueReturnMode = bufferedItemPosition.getItemReturnMode();
		}

		@Override
		public IContext getContext() {
			return input.getContext();
		}

		@Override
		public IContext getSubContext() {
			return new CustomContext("ListItem");
		}

		@Override
		public boolean isEncapsulatedFormEmbedded() {
			return true;
		}

		@Override
		public boolean isNullValueDistinct() {
			return bufferedItemPosition.getContainingListType().isItemNullValueDistinct();
		}

		@Override
		public boolean canCommitToParent() {
			return canCommit;
		}

		@Override
		public IModification createParentCommitModification(Object newObjectValue) {
			return modificationFactory.set(bufferedItemPosition.getIndex(), newObjectValue);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		public String getParentModificationTitle() {
			return getItemModificationTitle();
		}

		@Override
		public ITypeInfoSource getDeclaredNonSpecificTypeInfoSource() {
			ITypeInfo itemType = bufferedItemPosition.getContainingListType().getItemType();
			if (itemType != null) {
				return itemType.getSource();
			}
			return new JavaTypeInfoSource(Object.class, null);
		}

		@Override
		public ValueReturnMode getReturnModeFromParent() {
			return objectValueReturnMode;
		}

		@Override
		public Object getInitialValue() {
			bufferedItemPosition.refreshBranch();
			return bufferedItemPosition.getItem();
		}

		@Override
		public ModificationStack getParentModificationStack() {
			return ListControl.this.getModificationStack();
		}

		@Override
		public Component getOwnerComponent() {
			return ListControl.this;
		}

		@Override
		public IInfoFilter getEncapsulatedFormFilter() {
			return new AbstractDelegatingInfoFilter() {
				@Override
				protected IInfoFilter getDelegate() {
					BufferedItemPosition dynamicItemPosition = bufferedItemPosition.getSibling(-1);
					dynamicItemPosition.setFakeItem(getCurrentValue());
					return getStructuralInfo().getItemInfoFilter(dynamicItemPosition);
				}
			};
		}

	}

	public interface IItemsVisitor {

		boolean visitItem(BufferedItemPosition itemPosition);

	}

	protected enum InsertPosition {
		AFTER, BEFORE, UNKNOWN
	}

	protected class AddChildAction extends AbstractStandardListAction {

		private static final long serialVersionUID = 1L;

		protected BufferedItemPosition newSubItemPosition;
		protected IListTypeInfo subListType;
		protected Object newSubListItem;

		@Override
		protected boolean prepare() {
			newSubItemPosition = getNewSubItemPosition();
			subListType = newSubItemPosition.getContainingListType();
			newSubListItem = createItem(getNewSubItemPosition());
			if (newSubListItem == null) {
				return false;
			}
			if (!subListType.isItemConstructorSelectable() && !getDetailsAccessMode().hasDetailsDisplayArea()) {
				ItemUIBuilder dialogBuilder = openAnticipatedItemDialog(newSubItemPosition, newSubListItem);
				if (dialogBuilder.isCancelled()) {
					return false;
				}
				newSubListItem = dialogBuilder.getCurrentValue();
			}
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			getModificationStack().apply(createListModificationFactory(newSubItemPosition)
					.add(newSubItemPosition.getIndex(), newSubListItem));

			if (!subListType.isOrdered()) {
				newSubItemPosition = newSubItemPosition.getSibling(
						Arrays.asList(newSubItemPosition.retrieveContainingListRawValue()).indexOf(newSubListItem));
			}
			BufferedItemPosition toSelect = newSubItemPosition.getSibling(newSubItemPosition.getIndex());
			toPostSelectHolder[0] = Collections.singletonList(toSelect);
		}

		@Override
		public boolean isValid() {
			BufferedItemPosition newSubItemPosition = getNewSubItemPosition();
			if (newSubItemPosition == null) {
				return false;
			}
			if (!createListModificationFactory(newSubItemPosition).canAdd(newSubItemPosition.getIndex())) {
				return false;
			}
			if (!newSubItemPosition.getContainingListType().isInsertionAllowed()) {
				return false;
			}
			return true;
		}

		protected BufferedItemPosition getNewSubItemPosition() {
			BufferedItemPosition result = null;
			BufferedItemPosition singleSelection = getSingleSelection();
			if (singleSelection != null) {
				result = singleSelection.getSubItemPosition(-1);
			} else {
				if (getSelection().size() == 0) {
					result = itemPositionFactory.getRootItemPosition(-1);
				}
			}
			if (result != null) {
				result = result.getSibling(result.getContainingListSize());
			}
			return result;
		}

		@Override
		protected String getActionTitle() {
			BufferedItemPosition subItemPosition = getNewSubItemPosition();
			final IListTypeInfo subListType = subItemPosition.getContainingListType();
			final ITypeInfo subListItemType = subListType.getItemType();
			String title = "Add";
			if (subItemPosition.getDepth() > 0) {
				title += " Child";
			}
			if (subListItemType != null) {
				title += " " + getItemTitle(subItemPosition);
			}
			title += "...";
			return title;
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Add item into '" + getRootListTitle() + "'";
		}

	}

	protected class ClearAction extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		@Override
		protected boolean prepare() {
			if (!userConfirms("Remove all the items?")) {
				return false;
			}
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			getModificationStack()
					.apply(createListModificationFactory(itemPositionFactory.getRootItemPosition(-1)).clear());
			toPostSelectHolder[0] = Collections.emptyList();
		}

		@Override
		protected String getActionTitle() {
			return "Remove All";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Clear '" + getRootListTitle() + "'";
		}

		@Override
		public boolean isValid() {
			if (getRootListRawValue().length > 0) {
				if (createListModificationFactory(itemPositionFactory.getRootItemPosition(-1)).canClear()) {
					if (getRootListType().isRemovalAllowed()) {
						return true;
					}
				}
			}
			return false;
		}
	}

	protected class CopyAction extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		@Override
		protected boolean prepare() {
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			clipboard.clear();
			List<BufferedItemPosition> selection = getSelection();
			for (BufferedItemPosition itemPosition : selection) {
				clipboard.add(ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getItem()));
			}
		}

		@Override
		protected String getActionTitle() {
			return "Copy";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return null;
		}

		@Override
		public boolean isValid() {
			List<BufferedItemPosition> selection = getSelection();
			if (selection.size() > 0) {
				if (canCopyAll(selection)) {
					return true;
				}
			}
			return false;
		}

	}

	protected class CutAction extends AbstractStandardListAction {

		protected static final long serialVersionUID = 1L;

		@Override
		protected boolean prepare() {
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			clipboard.clear();
			List<BufferedItemPosition> selection = getSelection();
			selection = new ArrayList<BufferedItemPosition>(selection);
			Collections.reverse(selection);
			List<BufferedItemPosition> toPostSelect = new ArrayList<BufferedItemPosition>();
			for (BufferedItemPosition itemPosition : selection) {
				clipboard.add(0, ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getItem()));
			}
			for (BufferedItemPosition itemPosition : selection) {
				int index = itemPosition.getIndex();
				getModificationStack().apply(createListModificationFactory(itemPosition).remove(index));
				updatePositionsAfterItemRemoval(toPostSelect, itemPosition);
				BufferedItemPosition affectedPosition = getPositionsAffectedByItemRemoval(itemPosition);
				if (affectedPosition != null) {
					toPostSelect.add(affectedPosition);
				}
			}
			toPostSelectHolder[0] = toPostSelect;
		}

		@Override
		protected String getActionTitle() {
			return "Cut";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Cut '" + getRootListTitle() + "' item(s)";
		}

		@Override
		public boolean isValid() {
			List<BufferedItemPosition> selection = getSelection();
			if (selection.size() > 0) {
				if (canCopyAll(selection) && canRemoveAll(selection)) {
					return true;
				}
			}
			return false;
		}

	}

	protected class InsertAction extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		protected InsertPosition insertPosition;

		protected BufferedItemPosition newItemPosition;
		protected IListTypeInfo listType;
		protected Object newItem;

		public InsertAction(InsertPosition insertPosition) {
			this.insertPosition = insertPosition;
		}

		@Override
		protected boolean prepare() {
			newItemPosition = getNewItemPosition();
			listType = newItemPosition.getContainingListType();
			newItem = createItem(newItemPosition);
			if (newItem == null) {
				return false;
			}
			if (!listType.isItemConstructorSelectable() && !getDetailsAccessMode().hasDetailsDisplayArea()) {
				ItemUIBuilder dialogBuilder = openAnticipatedItemDialog(newItemPosition, newItem);
				if (dialogBuilder.isCancelled()) {
					return false;
				}
				newItem = dialogBuilder.getCurrentValue();
			}
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			getModificationStack()
					.apply(createListModificationFactory(newItemPosition).add(newItemPosition.getIndex(), newItem));
			BufferedItemPosition toSelect = newItemPosition;
			if (!listType.isOrdered()) {
				int indexToSelect = Arrays.asList(newItemPosition.retrieveContainingListRawValue()).indexOf(newItem);
				toSelect = newItemPosition.getSibling(indexToSelect);
			}
			toPostSelectHolder[0] = Collections.singletonList(toSelect);
		}

		@Override
		protected String getActionTitle() {
			BufferedItemPosition newItemPosition = getNewItemPosition();
			if (newItemPosition == null) {
				return null;
			}
			IListTypeInfo listType = newItemPosition.getContainingListType();
			ITypeInfo itemType = listType.getItemType();

			String buttonText = "Insert";
			{
				if (itemType != null) {
					buttonText += " " + getItemTitle(newItemPosition);
				}
				if (insertPosition == InsertPosition.AFTER) {
					buttonText += " After";
				} else if (insertPosition == InsertPosition.BEFORE) {
					buttonText += " Before";
				}
				buttonText += " ...";
			}
			return buttonText;
		}

		protected BufferedItemPosition getNewItemPosition() {
			BufferedItemPosition singleSelection = getSingleSelection();
			final int index;
			if (singleSelection == null) {
				return null;
			} else {
				if (insertPosition == InsertPosition.AFTER) {
					index = singleSelection.getIndex();
					return singleSelection.getSibling(index + 1);
				} else {
					return singleSelection;
				}

			}
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Insert into '" + getRootListTitle() + "'";
		}

		@Override
		public boolean isValid() {
			BufferedItemPosition newItemPosition = getNewItemPosition();
			if (newItemPosition != null) {
				if (newItemPosition.getContainingListType().isInsertionAllowed()) {
					if (createListModificationFactory(newItemPosition).canAdd(newItemPosition.getIndex())) {
						if (insertPosition == InsertPosition.BEFORE) {
							if (newItemPosition.getContainingListType().isOrdered()) {
								return true;
							}
						}
						if (insertPosition == InsertPosition.AFTER) {
							if (newItemPosition.getContainingListType().isOrdered()) {
								return true;
							}
						}
						if (insertPosition == InsertPosition.UNKNOWN) {
							if (!newItemPosition.getContainingListType().isOrdered()) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}
	}

	protected class MoveAction extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;
		protected int offset;

		public MoveAction(int offset) {
			this.offset = offset;
		}

		@Override
		protected boolean prepare() {
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			List<BufferedItemPosition> selection = getSelection();
			if (offset > 0) {
				selection = new ArrayList<BufferedItemPosition>(selection);
				Collections.reverse(selection);
			}
			List<BufferedItemPosition> newSelection = new ArrayList<BufferedItemPosition>();
			for (BufferedItemPosition itemPosition : selection) {
				int index = itemPosition.getIndex();
				getModificationStack().apply(createListModificationFactory(itemPosition).move(index, offset));
				newSelection.add(itemPosition.getSibling(index + offset));
			}
			toPostSelectHolder[0] = newSelection;
		}

		@Override
		protected String getActionTitle() {
			return (offset > 0) ? "Move Down" : "Move Up";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Move '" + getRootListTitle() + "' item(s)";
		}

		@Override
		public boolean isValid() {
			List<BufferedItemPosition> selection = getSelection();
			if (selection.size() > 0) {
				if (canMoveAll(selection, offset)) {
					if (allSelectionItemsInSameList()) {
						if (selection.get(0).getContainingListType().isOrdered()) {
							return true;
						}
					}
				}
			}
			return false;
		}

	}

	protected class OpenItemAction extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		protected ItemUIBuilder dialogBuilder;

		@Override
		protected boolean prepare() {
			BufferedItemPosition itemPosition = getSingleSelection();
			dialogBuilder = new ItemUIBuilder(itemPosition);
			swingRenderer.showDialog(dialogBuilder.createDialog(), true);
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			if (dialogBuilder.canPotentiallyModifyParentObject()) {
				dialogBuilder.impactParent();
			}
		}

		@Override
		protected String getActionTitle() {
			return "Open";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return getItemModificationTitle();
		}

		@Override
		public boolean isValid() {
			if (!getDetailsAccessMode().hasDetailsDisplayOption()) {
				return false;
			}
			BufferedItemPosition singleSelectedPosition = getSingleSelection();
			if (singleSelectedPosition != null) {
				if (!new ItemUIBuilder(singleSelectedPosition).isFormEmpty()) {
					if (getRootListType().canViewItemDetails()) {
						return true;
					}
				}
			}
			return false;
		}
	}

	protected class PasteAction extends InsertAction {

		protected static final long serialVersionUID = 1L;

		public PasteAction(InsertPosition insertPosition) {
			super(insertPosition);
		}

		@Override
		protected boolean prepare() {
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			BufferedItemPosition newItemPosition = getNewItemPosition();
			int index = newItemPosition.getIndex();
			int initialIndex = index;
			for (Object clipboardItem : clipboard) {
				clipboardItem = ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), clipboardItem);
				getModificationStack().apply(createListModificationFactory(newItemPosition).add(index, clipboardItem));
				index++;
			}
			List<BufferedItemPosition> toPostSelect = new ArrayList<BufferedItemPosition>();
			IListTypeInfo listType = newItemPosition.getContainingListType();
			index = initialIndex;
			for (int i = 0; i < clipboard.size(); i++) {
				Object clipboardItem = clipboard.get(i);
				if (listType.isOrdered()) {
					index = initialIndex + i;
				} else {
					index = Arrays.asList(newItemPosition.retrieveContainingListRawValue()).indexOf(clipboardItem);
				}
				if (index != -1) {
					toPostSelect.add(newItemPosition.getSibling(index));
				}
			}
			toPostSelectHolder[0] = toPostSelect;
		}

		@Override
		protected String getActionTitle() {
			BufferedItemPosition newItemPosition = getNewItemPosition();
			if (newItemPosition == null) {
				return null;
			}
			String buttonText = "Paste";
			{
				if (insertPosition == InsertPosition.AFTER) {
					buttonText += " After";
				} else if (insertPosition == InsertPosition.BEFORE) {
					buttonText += " Before";
				}
			}
			return buttonText;
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Paste into '" + getRootListTitle() + "'";
		}

		@Override
		public boolean isValid() {
			if (!super.isValid()) {
				return false;
			}
			if (clipboard.size() == 0) {
				return false;
			}
			BufferedItemPosition newItemPosition = getNewItemPosition();
			if (!createListModificationFactory(newItemPosition).canAddAll(newItemPosition.getIndex(), clipboard)) {
				return false;
			}
			return true;
		}
	}

	protected class PasteIntoAction extends AbstractStandardListAction {

		protected static final long serialVersionUID = 1L;

		@Override
		protected boolean prepare() {
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			BufferedItemPosition subItemPosition = getNewItemPosition();
			int newSubListItemIndex = subItemPosition.getContainingListSize();
			int newSubListItemInitialIndex = newSubListItemIndex;
			subItemPosition = subItemPosition.getSibling(newSubListItemIndex);
			for (Object clipboardItem : clipboard) {
				clipboardItem = ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), clipboardItem);
				getModificationStack()
						.apply(createListModificationFactory(subItemPosition).add(newSubListItemIndex, clipboardItem));
				newSubListItemIndex++;
			}
			List<BufferedItemPosition> toPostSelect = new ArrayList<BufferedItemPosition>();
			IListTypeInfo subListType = subItemPosition.getContainingListType();
			newSubListItemIndex = newSubListItemInitialIndex;
			for (int i = 0; i < clipboard.size(); i++) {
				Object clipboardItem = clipboard.get(i);
				if (subListType.isOrdered()) {
					newSubListItemInitialIndex = newSubListItemInitialIndex + i;
				} else {
					newSubListItemInitialIndex = Arrays.asList(subItemPosition.retrieveContainingListRawValue())
							.indexOf(clipboardItem);
				}
				if (newSubListItemInitialIndex != -1) {
					toPostSelect.add(subItemPosition.getSibling(newSubListItemInitialIndex));
				}
			}
			toPostSelectHolder[0] = toPostSelect;
		}

		protected BufferedItemPosition getNewItemPosition() {
			List<BufferedItemPosition> selection = getSelection();
			if (selection.size() == 0) {
				return itemPositionFactory.getRootItemPosition(0);
			}
			if (selection.size() == 1) {
				return selection.get(0).getSubItemPosition(0);
			}
			return null;
		}

		@Override
		protected String getActionTitle() {
			return "Paste Into";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Paste item(s) into '" + getRootListTitle() + "'";
		}

		@Override
		protected boolean isValid() {
			if (clipboard.size() > 0) {
				BufferedItemPosition newItemPosition = getNewItemPosition();
				if (newItemPosition != null) {
					if (createListModificationFactory(newItemPosition).canAddAll(newItemPosition.getIndex(),
							clipboard)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	protected class RemoveAction extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		@Override
		protected boolean prepare() {
			return userConfirms("Remove the element(s)?");
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			List<BufferedItemPosition> selection = getSelection();
			selection = new ArrayList<BufferedItemPosition>(selection);
			Collections.reverse(selection);
			List<BufferedItemPosition> toPostSelect = new ArrayList<BufferedItemPosition>();
			for (BufferedItemPosition itemPosition : selection) {
				int index = itemPosition.getIndex();
				getModificationStack().apply(createListModificationFactory(itemPosition).remove(index));
				updatePositionsAfterItemRemoval(toPostSelect, itemPosition);
				BufferedItemPosition affectedPosition = getPositionsAffectedByItemRemoval(itemPosition);
				if (affectedPosition != null) {
					toPostSelect.add(affectedPosition);
				}
			}
			toPostSelectHolder[0] = toPostSelect;
		}

		@Override
		protected String getActionTitle() {
			return "Remove";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return "Remove '" + getRootListTitle() + "' item(s)";
		}

		@Override
		public boolean isValid() {
			List<BufferedItemPosition> selection = getSelection();
			if (selection.size() == 0) {
				return false;
			}
			if (selection.size() > 0) {
				for (BufferedItemPosition selectionItem : selection) {
					if (!createListModificationFactory(selectionItem).canRemove(selectionItem.getIndex())) {
						return false;
					}
					if (!selectionItem.getContainingListType().isRemovalAllowed()) {
						return false;
					}
				}
			}
			return true;
		}
	}

	protected class DynamicActionHook extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		protected IDynamicListAction dynamicAction;
		protected MethodAction action;
		protected InvocationData invocationData;

		public DynamicActionHook(IDynamicListAction dynamicAction) {
			this.dynamicAction = dynamicAction;
		}

		@Override
		protected boolean prepare() {
			action = swingRenderer.createMethodAction(new IMethodControlInput() {

				@Override
				public ModificationStack getModificationStack() {
					return ListControl.this.getModificationStack();
				}

				@Override
				public IContext getContext() {
					return new CustomContext("listDynamicAction [name=" + dynamicAction.getName() + ", listContext="
							+ input.getContext().getIdentifier() + "]");
				}

				@Override
				public IMethodControlData getControlData() {
					IMethodControlData data = new DefaultMethodControlData(swingRenderer.getReflectionUI(),
							IDynamicListAction.NO_OWNER, dynamicAction);
					data = new MethodControlDataProxy(data) {
						@Override
						public Object invoke(InvocationData invocationData) {
							return ReflectionUIUtils.invokeMethodThroughModificationStack(base, invocationData,
									ListControl.this.getModificationStack());
						}
					};
					return data;
				}
			});
			invocationData = action.prepare(ListControl.this);
			return invocationData != null;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			action.invokeAndUpdateReturnValue(invocationData);
			if (dynamicAction.getPostSelection() != null) {
				toPostSelectHolder[0] = ReflectionUIUtils.<ItemPosition, BufferedItemPosition>convertCollectionUnsafely(
						dynamicAction.getPostSelection());
			}
		}

		@Override
		protected void displayResult() {
			if (action.shouldDisplayReturnValue()) {
				action.openMethodReturnValueWindow(ListControl.this);
			}
		}

		@Override
		protected String getActionTitle() {
			return dynamicAction.getCaption();
		}

		@Override
		protected String getCompositeModificationTitle() {
			return MethodControlDataModification
					.getTitle(ReflectionUIUtils.composeMessage(getRootListTitle(), dynamicAction.getCaption()));
		}

		@Override
		public boolean isValid() {
			return dynamicAction.isEnabled();
		}

		@Override
		protected String getActionDescription() {
			return dynamicAction.getOnlineHelp();
		}
	}

	protected class DynamicPropertyHook extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		protected IDynamicListProperty dynamicProperty;
		protected AbstractEditorWindowBuilder subDialogBuilder;

		public DynamicPropertyHook(IDynamicListProperty dynamicProperty) {
			this.dynamicProperty = dynamicProperty;
		}

		@Override
		protected boolean prepare() {
			subDialogBuilder = new AbstractEditorWindowBuilder() {

				@Override
				public String getCapsuleTypeCaption() {
					return ReflectionUIUtils.composeMessage(listData.getCaption(), dynamicProperty.getCaption());
				}

				@Override
				public IContext getContext() {
					return input.getContext();
				}

				@Override
				public IContext getSubContext() {
					return new CustomContext("ListDynamicProperty [name=" + dynamicProperty.getName() + "]");
				}

				@Override
				public boolean isEncapsulatedFormEmbedded() {
					return dynamicProperty.isFormControlEmbedded();
				}

				@Override
				public boolean isNullValueDistinct() {
					return dynamicProperty.isNullValueDistinct();
				}

				@Override
				public SwingRenderer getSwingRenderer() {
					return swingRenderer;
				}

				@Override
				public ValueReturnMode getReturnModeFromParent() {
					return ValueReturnMode.combine(listData.getValueReturnMode(), dynamicProperty.getValueReturnMode());
				}

				@Override
				public ITypeInfoSource getDeclaredNonSpecificTypeInfoSource() {
					return dynamicProperty.getType().getSource();
				}

				@Override
				public Object getInitialValue() {
					return dynamicProperty.getValue(IDynamicListProperty.NO_OWNER);
				}

				@Override
				protected boolean hasEncapsulatedFieldValueOptions() {
					return dynamicProperty.hasValueOptions(IDynamicListProperty.NO_OWNER);
				}

				@Override
				protected Object[] getEncapsulatedFieldValueOptions() {
					return dynamicProperty.getValueOptions(IDynamicListProperty.NO_OWNER);
				}

				@Override
				public String getParentModificationTitle() {
					return "Edit "
							+ ReflectionUIUtils.composeMessage(listData.getCaption(), dynamicProperty.getCaption());
				}

				@Override
				public ModificationStack getParentModificationStack() {
					return ListControl.this.getModificationStack();
				}

				@Override
				public Component getOwnerComponent() {
					return ListControl.this;
				}

				@Override
				public boolean canCommitToParent() {
					BufferedItemPosition anyRootItemPosition = getRootListItemPosition(-1);
					return anyRootItemPosition.isContainingListEditable();
				}

				@Override
				public IModification createParentCommitModification(Object newObjectValue) {
					return new FieldControlDataModification(new DefaultFieldControlData(swingRenderer.getReflectionUI(),
							IDynamicListProperty.NO_OWNER, dynamicProperty), newObjectValue);
				}

				@Override
				public IInfoFilter getEncapsulatedFormFilter() {
					return dynamicProperty.getFormControlFilter();
				}
			};
			swingRenderer.showDialog(subDialogBuilder.createDialog(), true);
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			if (subDialogBuilder.canPotentiallyModifyParentObject()) {
				subDialogBuilder.impactParent();
			}
			if (dynamicProperty.getPostSelection() != null) {
				toPostSelectHolder[0] = ReflectionUIUtils.<ItemPosition, BufferedItemPosition>convertCollectionUnsafely(
						dynamicProperty.getPostSelection());
			}
		}

		@Override
		protected String getActionTitle() {
			return dynamicProperty.getCaption() + "...";
		}

		@Override
		protected String getCompositeModificationTitle() {
			return FieldControlDataModification
					.getTitle(ReflectionUIUtils.composeMessage(getRootListTitle(), dynamicProperty.getCaption()));
		}

		@Override
		public boolean isValid() {
			return dynamicProperty.isEnabled();
		}

		@Override
		protected String getActionDescription() {
			return dynamicProperty.getOnlineHelp();
		}

	}

	protected class ItemPositionfactory extends AbstractBufferedItemPositionFactory {

		@Override
		public Object getNonBufferedRootListValue() {
			return listData.getValue();
		}

		@Override
		public IListTypeInfo getRootListType() {
			return (IListTypeInfo) listData.getType();
		}

		@Override
		public ValueReturnMode getRootListValueReturnMode() {
			return listData.getValueReturnMode();
		}

		@Override
		public boolean isRootListGetOnly() {
			return listData.isGetOnly();
		}

		@Override
		public String getRootListTitle() {
			return listData.getCaption();
		}

		private ListControl getOuterType() {
			return ListControl.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			ItemPositionfactory other = (ItemPositionfactory) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			return true;
		}

	};

}
