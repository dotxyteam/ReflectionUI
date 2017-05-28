package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
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
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.swing.editor.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.AbstractDelegatingInfoFilter;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPositionFactory;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemDetailsAreaPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.SubListsGroupingField.SubListGroup;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.BufferedListModificationFactory;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractLazyTreeNode;

public class ListControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;

	protected IFieldControlData listData;

	protected JXTreeTable treeTableComponent;
	protected JPanel toolbar;
	protected ItemNode rootNode;
	protected BufferedItemPositionFactory itemPositionFactory;
	protected static List<Object> clipboard = new ArrayList<Object>();
	protected Map<ItemNode, Map<Integer, String>> valuesByNode = new HashMap<ItemNode, Map<Integer, String>>();
	protected IListStructuralInfo structuralInfo;

	protected JPanel detailsArea;
	protected JPanel detailsControl;
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
		this.input = input;
		this.listData = input.getControlData();

		initializeTreeTableControl();
		toolbar = new JPanel();
		detailsArea = new JPanel();
		layoutControls();

		refreshStructure();
		if (getDetailsAccessMode().hasDetailsDisplayOption()) {
			openDetailsDialogOnItemDoubleClick();
		} else {
			updateDetailsAreaOnSelection();
		}
		updateToolbarOnSelection();
		setupContexteMenu();
		updateToolbar();
		initializeSelectionListening();
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
				updateDetailsArea();
			}
		});
	}

	protected void layoutControls() {
		setLayout(new BorderLayout());
		JScrollPane treeTableComponentScrollPane = new JScrollPane(treeTableComponent) {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				Dimension screenSize = SwingRendererUtils.getScreenSize(this);
				int characterSize = SwingRendererUtils.getStandardCharacterWidth(treeTableComponent);
				{
					result.width = Math.max(result.width, characterSize * 20);
				}
				{
					int minHeight = (int) (characterSize * 15);
					int maxHeight = (int) (screenSize.height * 0.60);
					result.height = minHeight;
					Dimension treeTableComponentPreferredSize = treeTableComponent.getPreferredSize();
					if (treeTableComponentPreferredSize != null) {
						JTableHeader header = treeTableComponent.getTableHeader();
						if (header != null) {
							treeTableComponentPreferredSize.height += header.getHeight();
						}
						treeTableComponentPreferredSize.height += (characterSize * 5);
						result.height = Math.max(result.height, treeTableComponentPreferredSize.height);
					}
					Dimension toolbarSize = toolbar.getPreferredSize();
					if (toolbarSize != null) {
						result.height = Math.max(result.height, toolbarSize.height);
					}
					result.height = Math.min(result.height, maxHeight);
				}
				return result;
			}
		};
		if (getDetailsAccessMode().hasDetailsDisplayArea()) {
			JPanel listPanel = new JPanel();
			listPanel.setLayout(new BorderLayout());
			listPanel.add(BorderLayout.CENTER, treeTableComponentScrollPane);
			listPanel.add(toolbar, BorderLayout.EAST);
			final JSplitPane splitPane = new JSplitPane();
			add(splitPane, BorderLayout.CENTER);
			final double dividerLocation;
			if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.RIGHT) {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setLeftComponent(new JScrollPane(listPanel));
				splitPane.setRightComponent(detailsArea);
				dividerLocation = 1.0 - getDetailsAccessMode().getDefaultDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.LEFT) {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setRightComponent(new JScrollPane(listPanel));
				splitPane.setLeftComponent(detailsArea);
				dividerLocation = getDetailsAccessMode().getDefaultDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.BOTTOM) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane.setTopComponent(new JScrollPane(listPanel));
				splitPane.setBottomComponent(detailsArea);
				dividerLocation = 1.0 - getDetailsAccessMode().getDefaultDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getDetailsAreaPosition() == ItemDetailsAreaPosition.TOP) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane.setBottomComponent(new JScrollPane(listPanel));
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
		setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(listData.getCaption())));
	}

	@Override
	public Dimension getMinimumSize() {
		return getMinimumSizeWhilePreventingHeightLossOnResize();
	}

	protected Dimension getMinimumSizeWhilePreventingHeightLossOnResize() {
		return new Dimension(super.getMinimumSize().width, getPreferredSize().height);
	}

	protected void updateToolbar() {
		toolbar.removeAll();

		GridBagLayout layout = new GridBagLayout();
		toolbar.setLayout(layout);

		if (getDetailsAccessMode().hasDetailsDisplayOption()) {
			if (getActiveListItemPosition().getContainingListType().canViewItemDetails()) {
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

		List<AbstractListProperty> dynamicProperties = getRootListType()
				.getDynamicProperties(itemPositionFactory.getRootItemPosition(-1), getSelection());
		List<AbstractListAction> dynamicActions = getRootListType()
				.getDynamicActions(itemPositionFactory.getRootItemPosition(-1), getSelection());
		if ((dynamicProperties.size() > 0) || (dynamicActions.size() > 0)) {
			for (AbstractListProperty listProperty : dynamicProperties) {
				AbstractAction dynamicPropertyHook = createDynamicPropertyHook(listProperty);
				toolbar.add(createTool((String) dynamicPropertyHook.getValue(AbstractAction.NAME), null, true, false,
						dynamicPropertyHook));
			}
			for (AbstractListAction listAction : dynamicActions) {
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

	protected JButton createTool(String text, Icon icon, boolean alwawsShowIcon, final boolean alwawsShowMenu,
			AbstractAction... actions) {
		final JButton result = new JButton(text, icon);
		result.setFocusable(false);
		final List<AbstractAction> actionsToPresent = new ArrayList<AbstractAction>();
		for (final AbstractAction action : actions) {
			if (action == null) {
				continue;
			}
			if (action.isEnabled()) {
				actionsToPresent.add(action);
			}
		}
		if (actionsToPresent.size() > 0) {
			if (actionsToPresent.size() == 1) {
				SwingRendererUtils.setMultilineToolTipText(result,
						(String) actionsToPresent.get(0).getValue(Action.NAME));
			} else if (actionsToPresent.size() > 1) {
				StringBuilder tooltip = new StringBuilder();
				boolean firstAction = true;
				for (AbstractAction action : actionsToPresent) {
					if (!firstAction) {
						tooltip.append("\nor\n");
					}
					tooltip.append(action.getValue(Action.NAME));
					firstAction = false;
				}
				SwingRendererUtils.setMultilineToolTipText(result, tooltip.toString());
			}
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
	public boolean handlesModificationStackUpdate() {
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

	protected void initializeTreeTableControl() {
		rootNode = createRootNode();
		treeTableComponent = new JXTreeTable(createTreeTableModel());
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
		treeTableComponent.addHighlighter(HighlighterFactory.createSimpleStriping());
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
		itemPositionFactory = new BufferedItemPositionFactory(listData);
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

		for (AbstractListProperty listProperty : getRootListType()
				.getDynamicProperties(itemPositionFactory.getRootItemPosition(-1), selection)) {
			result.add(createDynamicPropertyHook(listProperty));
		}
		for (AbstractListAction listAction : getRootListType()
				.getDynamicActions(itemPositionFactory.getRootItemPosition(-1), selection)) {
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

	protected ListModificationFactory createListModificationFactory(BufferedItemPosition itemPosition) {
		return new BufferedListModificationFactory(itemPosition, getModificationsTarget());
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
		return swingRenderer.openQuestionDialog(SwingUtilities.getWindowAncestor(treeTableComponent), question, null,
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
		if (treePaths.size() > 0) {
			try {
				treeTableComponent.scrollRowToVisible(treeTableComponent.getRowForPath(treePaths.get(0)));
			} catch (Throwable ignore) {
			}
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
			public ModificationStack getParentObjectModificationStack() {
				return dummyModificationStack;
			}

			@Override
			public boolean canCommit() {
				return true;
			}

			@Override
			public IModification createCommitModification(Object newObjectValue) {
				return IModification.NULL_MODIFICATION;
			}

			@Override
			public boolean isCancellable() {
				return true;
			}
		};
		dialogBuilder.showDialog();
		return dialogBuilder;
	}

	protected AbstractStandardListAction createAddChildAction() {
		return new AddChildAction();
	};

	protected String getItemTitle(BufferedItemPosition itemPosition) {
		Object encapsulatedObject = new ItemUIBuilder(itemPosition).getEncapsulatedObject();
		ITypeInfo encapsulatedObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(encapsulatedObject));
		return encapsulatedObjectType.getCaption();
	}

	protected Object createItem(BufferedItemPosition itemPosition) {
		IListTypeInfo subListType = itemPosition.getContainingListType();
		ITypeInfo typeToInstanciate = subListType.getItemType();
		if (typeToInstanciate == null) {
			typeToInstanciate = new DefaultTypeInfo(swingRenderer.getReflectionUI(), Object.class);
		}
		typeToInstanciate = addSpecificItemContructors(typeToInstanciate, itemPosition);
		if (subListType.isItemConstructorSelectable()) {
			return swingRenderer.onTypeInstanciationRequest(ListControl.this, typeToInstanciate);
		} else {
			return ReflectionUIUtils.createDefaultInstance(typeToInstanciate);
		}
	}

	protected ITypeInfo addSpecificItemContructors(ITypeInfo itemType, final BufferedItemPosition newItemPosition) {
		return new TypeInfoProxyFactory() {

			@Override
			protected List<IMethodInfo> getConstructors(ITypeInfo type) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(type));
				Object containingList = newItemPosition.retrieveContainingListValue();
				IListTypeInfo containingListType = newItemPosition.getContainingListType();
				List<IMethodInfo> specificItemConstructors = containingListType
						.getAdditionalItemConstructors(containingList);
				if (specificItemConstructors != null) {
					result.addAll(specificItemConstructors);
				}
				return result;
			}

			@Override
			public String toString() {
				return "addSpecificItemContructors[listType=" + newItemPosition.getContainingListType() + "]";
			}

		}.get(itemType);
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

	protected IInfo getModificationsTarget() {
		return input.getModificationsTarget();
	}

	protected AbstractAction createDynamicPropertyHook(final AbstractListProperty dynamicProperty) {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
				AbstractEditorBuilder subDialogBuilder = new AbstractEditorBuilder() {

					@Override
					public String getEncapsulationTypeCaption() {
						return ReflectionUIUtils.composeMessage(getModificationsTarget().getCaption(),
								dynamicProperty.getCaption());
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
					public boolean isObjectFormExpanded() {
						return dynamicProperty.isFormControlEmbedded();
					}

					@Override
					public boolean isObjectNullValueDistinct() {
						return dynamicProperty.isNullValueDistinct();
					}

					@Override
					public SwingRenderer getSwingRenderer() {
						return swingRenderer;
					}

					@Override
					public ValueReturnMode getObjectValueReturnMode() {
						return ValueReturnMode.combine(listData.getValueReturnMode(),
								dynamicProperty.getValueReturnMode());
					}

					@Override
					public ITypeInfo getObjectDeclaredType() {
						return dynamicProperty.getType();
					}

					@Override
					public Object getInitialObjectValue() {
						return dynamicProperty.getValue(AbstractListProperty.NO_OWNER);
					}

					@Override
					public String getCumulatedModificationsTitle() {
						return "Edit "
								+ ReflectionUIUtils.composeMessage(listData.getCaption(), dynamicProperty.getCaption());
					}

					@Override
					public IInfo getCumulatedModificationsTarget() {
						return ListControl.this.getModificationsTarget();
					}

					@Override
					public ModificationStack getParentObjectModificationStack() {
						return ListControl.this.getModificationStack();
					}

					@Override
					public Component getOwnerComponent() {
						return ListControl.this;
					}

					@Override
					public String getEditorWindowTitle() {
						return dynamicProperty.getType().getCaption();
					}

					@Override
					public boolean canCommit() {
						return !dynamicProperty.isGetOnly();
					}

					@Override
					public IModification createCommitModification(Object newObjectValue) {
						return new ControlDataValueModification(
								new DefaultFieldControlData(AbstractListProperty.NO_OWNER, dynamicProperty),
								newObjectValue, dynamicProperty);
					}

					@Override
					public IInfoFilter getObjectFormFilter() {
						return dynamicProperty.getFormControlFilter();
					}
				};
				subDialogBuilder.showDialog();
				return subDialogBuilder.isParentModificationStackImpacted();
			}

			@Override
			protected String getActionTitle() {
				return dynamicProperty.getCaption() + "...";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return ControlDataValueModification.getTitle(dynamicProperty);
			}

			@Override
			public boolean isValid() {
				return dynamicProperty.isEnabled();
			}

		};
	}

	protected AbstractAction createDynamicActionHook(final AbstractListAction dynamicAction) {
		return new AbstractAction(swingRenderer.prepareStringToDisplay(dynamicAction.getCaption())) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				MethodAction action = swingRenderer.createMethodAction(getMethodInput());
				action.execute(ListControl.this);
			}

			protected IMethodControlInput getMethodInput() {
				return new IMethodControlInput() {
					@Override
					public IInfo getModificationsTarget() {
						return ListControl.this.getModificationsTarget();
					}

					@Override
					public ModificationStack getModificationStack() {
						return ListControl.this.getModificationStack();
					}

					@Override
					public IContext getContext() {
						return new CustomContext("listDynamicAction [name=" + dynamicAction.getName() + ", listContext="
								+ input.getContext() + "]");
					}

					@Override
					public IMethodControlData getControlData() {
						final Object listRootValue = listData.getValue();
						IMethodControlData data = new DefaultMethodControlData(listRootValue, dynamicAction);
						data = new MethodControlDataProxy(data) {
							@Override
							public Object invoke(InvocationData invocationData) {
								return SwingRendererUtils.showBusyDialogWhileInvokingMethod(ListControl.this,
										swingRenderer, base, invocationData);
							}
						};
						data = new MethodControlDataProxy(data) {
							@Override
							public Object invoke(InvocationData invocationData) {
								return ReflectionUIUtils.invokeMethodThroughModificationStack(base, invocationData,
										ListControl.this.getModificationStack(),
										ListControl.this.getModificationsTarget());
							}
						};
						return data;
					}
				};
			}

			@Override
			public boolean isEnabled() {
				return dynamicAction.isEnabled();
			}

		};
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
					swingRenderer.handleExceptionsFromDisplayedUI(treeTableComponent, t);
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
	}

	protected void updateDetailsArea() {
		BufferedItemPosition singleSelection = getSingleSelection();
		if (!ReflectionUIUtils.equalsOrBothNull(singleSelection, detailsControlItemPosition)) {
			detailsControlItemPosition = singleSelection;
			if (detailsControl != null) {
				detailsArea.removeAll();
				detailsControlItem = null;
				detailsControl = null;
				SwingRendererUtils.handleComponentSizeChange(detailsArea);
			}
		}
		if (detailsControlItemPosition == null) {
			return;
		}
		if (detailsControl != null) {
			swingRenderer.refreshAllFieldControls(detailsControl, false);
		} else {
			detailsControl = new ItemUIBuilder(detailsControlItemPosition).createForm(true);
			detailsArea.removeAll();
			detailsArea.setLayout(new BorderLayout());
			detailsArea.add(swingRenderer.createWindowScrollPane(detailsControl), BorderLayout.CENTER);
			detailsArea.add(swingRenderer.createStatusBar(detailsControl), BorderLayout.NORTH);
			swingRenderer.validateFormInBackgroundAndReportOnStatusBar(detailsControl);
			SwingRendererUtils.handleComponentSizeChange(ListControl.this);
			SwingRendererUtils.requestAnyComponentFocus(detailsControl, swingRenderer);
		}
	}

	protected void openDetailsDialogOnItemDoubleClick() {
		treeTableComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
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

	protected void refreshStructure() {
		restoringColumnWidthsAsMuchAsPossible(new Runnable() {
			@Override
			public void run() {
				valuesByNode.clear();
				rootNode = createRootNode();
				treeTableComponent.setTreeTableModel(createTreeTableModel());
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
	public boolean refreshUI() {
		restoringSelectionAsMuchAsPossible(new Runnable() {
			@Override
			public void run() {
				refreshStructure();
			}
		});
		return true;
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

		selectionListenersEnabled = true;

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
		public IInfo getTarget() {
			return getModificationsTarget();
		}

		@Override
		public IModification applyAndGetOpposite() {
			List<BufferedItemPosition> oldSelection = getSelection();
			if (newSelection == null) {
				restoringSelectionAsMuchAsPossible(new Runnable() {
					@Override
					public void run() {
						refreshStructure();
					}
				});
			} else {
				refreshStructure();
				setSelection(newSelection);
			}
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

			Image imageIcon = getCellIconImage(node, columnIndex);
			if (imageIcon == null) {
				label.setIcon(null);
			} else {
				label.setIcon(new ImageIcon(imageIcon));
			}

		}
	}

	protected class ItemTableCellRenderer extends AbstractItemCellRenderer implements TableCellRenderer {

		protected TableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

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

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean isLeaf, int row, boolean focused) {
			JLabel defaultComponent = (JLabel) defaultRenderer.getTreeCellRendererComponent(tree, value, selected,
					expanded, isLeaf, row, focused);
			component.setForeground(defaultComponent.getForeground());
			customizeCellRendererComponent(component, (ItemNode) value, row, 0, selected, focused);
			return component;
		}

	}

	protected abstract class AbstractStandardListAction extends AbstractAction {

		protected static final long serialVersionUID = 1L;

		protected abstract boolean perform(List<BufferedItemPosition>[] toPostSelectHolder);

		protected abstract String getActionTitle();

		protected abstract String getCompositeModificationTitle();

		protected abstract boolean isValid();

		@Override
		public Object getValue(String key) {
			if (Action.NAME.equals(key)) {
				return swingRenderer.prepareStringToDisplay(getActionTitle());
			} else {
				return super.getValue(key);
			}
		}

		@Override
		public boolean isEnabled() {
			return isValid();
		}

		@Override
		final public void actionPerformed(ActionEvent e) {
			final String modifTitle = getCompositeModificationTitle();
			@SuppressWarnings("unchecked")
			final List<BufferedItemPosition>[] toPostSelectHolder = new List[1];
			if (modifTitle == null) {
				List<BufferedItemPosition> initialSelection = getSelection();
				if (perform(toPostSelectHolder)) {
					refreshStructure();
					if (toPostSelectHolder[0] != null) {
						setSelection(toPostSelectHolder[0]);
					} else {
						setSelection(initialSelection);
					}
				}
			} else {
				final ModificationStack modifStack = getModificationStack();
				try {
					modifStack.insideComposite(getModificationsTarget(), modifTitle, UndoOrder.FIFO,
							new Accessor<Boolean>() {
								@Override
								public Boolean get() {
									if (modifStack.insideComposite(getModificationsTarget(),
											modifTitle + " (without list control update)", UndoOrder.getNormal(),
											new Accessor<Boolean>() {
												@Override
												public Boolean get() {
													return perform(toPostSelectHolder);
												}
											})) {
										modifStack.apply(new RefreshStructureModification(toPostSelectHolder[0]));
										return true;
									} else {
										return false;
									}
								}
							});
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		}

	};

	protected class ItemUIBuilder extends AbstractEditorBuilder {
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
		public boolean isObjectFormExpanded() {
			return true;
		}

		@Override
		public boolean isObjectNullValueDistinct() {
			return bufferedItemPosition.getContainingListType().isItemNullValueDistinct();
		}

		@Override
		public boolean canCommit() {
			return canCommit;
		}

		@Override
		public IModification createCommitModification(Object newObjectValue) {
			return modificationFactory.set(bufferedItemPosition.getIndex(), newObjectValue);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		public String getCumulatedModificationsTitle() {
			return getItemModificationTitle();
		}

		@Override
		public IInfo getCumulatedModificationsTarget() {
			return getModificationsTarget();
		}

		@Override
		public ITypeInfo getObjectDeclaredType() {
			ITypeInfo result = bufferedItemPosition.getContainingListType().getItemType();
			if (result == null) {
				result = swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class));
			}
			return result;
		}

		@Override
		public ValueReturnMode getObjectValueReturnMode() {
			return objectValueReturnMode;
		}

		@Override
		public Object getInitialObjectValue() {
			if (isObjectValueInitialized()) {
				bufferedItemPosition.refreshBranch();
			}
			return bufferedItemPosition.getItem();
		}

		@Override
		public ModificationStack getParentObjectModificationStack() {
			return ListControl.this.getModificationStack();
		}

		@Override
		public Component getOwnerComponent() {
			return ListControl.this;
		}

		@Override
		public IInfoFilter getObjectFormFilter() {
			return new AbstractDelegatingInfoFilter() {
				@Override
				protected IInfoFilter getDelegate() {
					BufferedItemPosition dynamicItemPosition = bufferedItemPosition.getSibling(-1);
					dynamicItemPosition.setFakeItem(getCurrentObjectValue());
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

		@Override
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			BufferedItemPosition newSubItemPosition = getNewSubItemPosition();
			IListTypeInfo subListType = newSubItemPosition.getContainingListType();
			Object newSubListItem = createItem(getNewSubItemPosition());
			if (newSubListItem == null) {
				return false;
			}
			if (!subListType.isItemConstructorSelectable() && !getDetailsAccessMode().hasDetailsDisplayArea()) {
				ItemUIBuilder dialogBuilder = openAnticipatedItemDialog(newSubItemPosition, newSubListItem);
				if (dialogBuilder.isCancelled()) {
					return false;
				}
				newSubListItem = dialogBuilder.getCurrentObjectValue();
			}
			getModificationStack().apply(createListModificationFactory(newSubItemPosition)
					.add(newSubItemPosition.getIndex(), newSubListItem));
			if (!subListType.isOrdered()) {
				newSubItemPosition = newSubItemPosition.getSibling(
						Arrays.asList(newSubItemPosition.retrieveContainingListRawValue()).indexOf(newSubListItem));
			}
			BufferedItemPosition toSelect = newSubItemPosition.getSibling(newSubItemPosition.getIndex());
			toPostSelectHolder[0] = Collections.singletonList(toSelect);
			return true;
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
			}
			if (getSelection().size() == 0) {
				result = itemPositionFactory.getRootItemPosition(-1);
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
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			if (!userConfirms("Remove all the items?")) {
				return false;
			}
			getModificationStack()
					.apply(createListModificationFactory(itemPositionFactory.getRootItemPosition(-1)).clear());
			toPostSelectHolder[0] = Collections.emptyList();
			return true;
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
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			clipboard.clear();
			List<BufferedItemPosition> selection = getSelection();
			for (BufferedItemPosition itemPosition : selection) {
				clipboard.add(ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getItem()));
			}
			return false;
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
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			clipboard.clear();
			List<BufferedItemPosition> selection = getSelection();
			selection = new ArrayList<BufferedItemPosition>(selection);
			Collections.reverse(selection);
			List<BufferedItemPosition> toPostSelect = new ArrayList<BufferedItemPosition>();
			for (BufferedItemPosition itemPosition : selection) {
				clipboard.add(0, ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getItem()));
				int index = itemPosition.getIndex();
				getModificationStack().apply(createListModificationFactory(itemPosition).remove(index));
				updatePositionsAfterItemRemoval(toPostSelect, itemPosition);
				BufferedItemPosition affectedPosition = getPositionsAffectedByItemRemoval(itemPosition);
				if (affectedPosition != null) {
					toPostSelect.add(affectedPosition);
				}
			}
			toPostSelectHolder[0] = toPostSelect;
			return true;
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

		public InsertAction(InsertPosition insertPosition) {
			this.insertPosition = insertPosition;
		}

		@Override
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			BufferedItemPosition newItemPosition = getNewItemPosition();
			IListTypeInfo listType = newItemPosition.getContainingListType();
			Object newItem = createItem(newItemPosition);
			if (newItem == null) {
				return false;
			}
			if (!listType.isItemConstructorSelectable() && !getDetailsAccessMode().hasDetailsDisplayArea()) {
				ItemUIBuilder dialogBuilder = openAnticipatedItemDialog(newItemPosition, newItem);
				if (dialogBuilder.isCancelled()) {
					return false;
				}
				newItem = dialogBuilder.getCurrentObjectValue();
			}
			getModificationStack()
					.apply(createListModificationFactory(newItemPosition).add(newItemPosition.getIndex(), newItem));
			BufferedItemPosition toSelect = newItemPosition;
			if (!listType.isOrdered()) {
				int indexToSelect = Arrays.asList(newItemPosition.retrieveContainingListRawValue()).indexOf(newItem);
				toSelect = newItemPosition.getSibling(indexToSelect);
			}
			toPostSelectHolder[0] = Collections.singletonList(toSelect);
			return true;
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
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
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
			return true;
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

		@Override
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			BufferedItemPosition itemPosition = getSingleSelection();
			ItemUIBuilder dialogBuilder = new ItemUIBuilder(itemPosition);
			dialogBuilder.showDialog();
			return dialogBuilder.isParentModificationStackImpacted();
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
			if (getDetailsAccessMode().hasDetailsDisplayArea()) {
				return false;
			}
			BufferedItemPosition singleSelectedPosition = getSingleSelection();
			if (singleSelectedPosition != null) {
				if (!new ItemUIBuilder(singleSelectedPosition).isObjectFormEmpty()) {
					if (singleSelectedPosition.getContainingListType().canViewItemDetails()) {
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
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
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
			return true;
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
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
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
			return true;
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
		protected boolean perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			if (userConfirms("Remove the element(s)?")) {
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
				return true;
			} else {
				return false;
			}
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

}
