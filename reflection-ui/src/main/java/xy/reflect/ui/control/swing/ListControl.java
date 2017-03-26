package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
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

import xy.reflect.ui.control.input.DefaultFieldControlData;
import xy.reflect.ui.control.input.DefaultMethodControlData;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.control.input.IMethodControlData;
import xy.reflect.ui.control.input.IMethodControlInput;
import xy.reflect.ui.control.input.MethodControlDataProxy;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemDetailsAreaPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.DelegatingItemPosition;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.SubListsGroupingField.SubListGroup;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.CompositeModification;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractLazyTreeNode;

@SuppressWarnings("unused")
public class ListControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;

	protected IFieldControlData listData;

	protected JXTreeTable treeTableComponent;
	protected JPanel toolbar;
	protected ItemNode rootNode;
	protected static List<Object> clipboard = new ArrayList<Object>();
	protected Map<ItemNode, Map<Integer, String>> valuesByNode = new HashMap<ItemNode, Map<Integer, String>>();
	protected IListStructuralInfo structuralInfo;

	protected JPanel detailsArea;
	protected JPanel detailsControl;
	protected IListItemDetailsAccessMode detailsMode;
	protected ItemPosition detailsControlItemPosition;
	protected Object detailsControlItem;

	protected List<Runnable> selectionListeners = new ArrayList<Runnable>();
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
		selectionListeners.add(new Runnable() {
			@Override
			public void run() {
				updateToolbar();
			}
		});

	}

	protected void updateDetailsAreaOnSelection() {
		selectionListeners.add(new Runnable() {
			@Override
			public void run() {
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
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int characterSize = SwingRendererUtils.getStandardCharacterWidth(treeTableComponent);
				Dimension result = new Dimension();
				{
					result.width = characterSize * 40;
				}
				{
					int minHeight = (int) (characterSize * 10);
					int maxHeight = (int) (screenSize.height * 0.60);
					result.height = minHeight;
					Dimension treeTableComponentPreferredSize = treeTableComponent.getPreferredSize();
					if (treeTableComponentPreferredSize != null) {
						JTableHeader header = treeTableComponent.getTableHeader();
						if (header != null) {
							treeTableComponentPreferredSize.height += header.getHeight();
						}
						treeTableComponentPreferredSize.height += 10;
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
					splitPane.setDividerLocation(dividerLocation);
					splitPane.removeComponentListener(this);
				}
			});
		} else {
			add(treeTableComponentScrollPane, BorderLayout.CENTER);
			add(toolbar, BorderLayout.EAST);
		}
	}

	@Override
	public Dimension getMinimumSize() {
		return getMinimumSizePreventingHeightLossOnResize();
	}

	protected Dimension getMinimumSizePreventingHeightLossOnResize() {
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

		List<AbstractListProperty> dynamicProperties = getRootListType().getDynamicProperties(getSelection());
		List<AbstractListAction> dynamicActions = getRootListType().getDynamicActions(getSelection());
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
		return new ItemNode(null);
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(listData.getCaption()));
		return true;
	}

	protected String getColumnCaption(int columnIndex) {
		if (getStructuralInfo() == null) {
			return "";
		}
		IListStructuralInfo tableInfo = getStructuralInfo();
		return tableInfo.getColumns().get(columnIndex).getCaption();
	}

	protected int getColumnCount() {
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
			ItemPosition itemPosition = (ItemPosition) node.getUserObject();
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
		ItemPosition itemPosition = (ItemPosition) node.getUserObject();
		if (columnIndex == 0) {
			return swingRenderer.getObjectIconImage(itemPosition.getItem());
		}
		return null;
	}

	protected List<AbstractAction> createCurrentSelectionActions() {

		List<AbstractAction> result = new ArrayList<AbstractAction>();

		List<ItemPosition> selection = getSelection();

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

		for (AbstractListProperty listProperty : getRootListType().getDynamicProperties(selection)) {
			result.add(createDynamicPropertyHook(listProperty));
		}
		for (AbstractListAction listAction : getRootListType().getDynamicActions(selection)) {
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

	protected boolean canCopyAll(List<ItemPosition> selection) {
		boolean result = true;
		for (ItemPosition selectionItem : selection) {
			if (!ReflectionUIUtils.canCopy(swingRenderer.getReflectionUI(), selectionItem.getItem())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean canRemoveAll(List<ItemPosition> selection) {
		boolean result = true;
		for (ItemPosition selectionItem : selection) {
			if (!new ListModificationFactory(selectionItem, getModificationsTarget())
					.canRemove(selectionItem.getIndex())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean canMoveAll(List<ItemPosition> selection, int offset) {
		boolean result = true;
		for (ItemPosition selectionItem : selection) {
			if (!new ListModificationFactory(selectionItem, getModificationsTarget()).canMove(selectionItem.getIndex(),
					offset)) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean allSelectionItemsInSameList() {
		boolean result = true;
		List<ItemPosition> selection = getSelection();
		ItemPosition firstSelectionItem = selection.get(0);
		for (ItemPosition selectionItem : selection) {
			if (!ReflectionUIUtils.equalsOrBothNull(firstSelectionItem.getParentItemPosition(),
					selectionItem.getParentItemPosition())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean itemPositionSupportsAllClipboardItems(ItemPosition itemPosition) {
		boolean result = true;
		for (Object clipboardItem : clipboard) {
			if (!itemPosition.supportsItem(clipboardItem)) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected ItemPosition getSubItemPosition(ItemPosition itemPosition) {
		if (itemPosition == null) {
			return getAnyRootListItemPosition();
		}
		final IFieldControlData subListData = itemPosition.getSubListData();
		if (subListData == null) {
			return null;
		}
		return new ItemPosition(itemPosition, subListData, -1) {

			@Override
			public String getContainingListTitle() {
				return subListData.getCaption();
			}

		};
	}

	protected AbstractStandardListAction createClearAction() {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				if (!userConfirms("Remove all the items?")) {
					return false;
				}
				getModificationStack().apply(
						new ListModificationFactory(getAnyRootListItemPosition(), getModificationsTarget()).clear());
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
					if (new ListModificationFactory(getAnyRootListItemPosition(), getModificationsTarget())
							.canClear()) {
						if (getRootListType().isRemovalAllowed()) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}

	protected boolean userConfirms(String question) {
		return swingRenderer.openQuestionDialog(SwingUtilities.getWindowAncestor(treeTableComponent), question, null,
				"OK", "Cancel");
	}

	protected AbstractStandardListAction createMoveAction(final int offset) {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				List<ItemPosition> selection = getSelection();
				if (offset > 0) {
					selection = new ArrayList<ItemPosition>(selection);
					Collections.reverse(selection);
				}
				List<ItemPosition> newSelection = new ArrayList<ItemPosition>();
				for (ItemPosition itemPosition : selection) {
					int index = itemPosition.getIndex();
					getModificationStack().apply(
							new ListModificationFactory(itemPosition, getModificationsTarget()).move(index, offset));
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
				List<ItemPosition> selection = getSelection();
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

		};

	}

	public ItemPosition getSingleSelection() {
		List<ItemPosition> selection = getSelection();
		if ((selection.size() == 0) || (selection.size() > 1)) {
			return null;
		} else {
			return selection.get(0);
		}
	}

	public List<ItemPosition> getSelection() {
		List<ItemPosition> result = new ArrayList<ItemPosition>();
		for (int selectedRow : treeTableComponent.getSelectedRows()) {
			TreePath path = treeTableComponent.getPathForRow(selectedRow);
			if (path == null) {
				return null;
			}
			ItemNode selectedNode = (ItemNode) path.getLastPathComponent();
			BufferedItemPosition bufferedItemPosition = (BufferedItemPosition) selectedNode.getUserObject();
			result.add(bufferedItemPosition.getStandardItemPosition());
		}
		return result;
	}

	public void setSingleSelection(ItemPosition toSelect) {
		setSelection(Collections.singletonList(toSelect));
	}

	public ItemPosition findItemPositionByReference(final Object item) {
		final ItemPosition[] result = new ItemPosition[1];
		visitItems(new IItemsVisitor() {
			@Override
			public boolean visitItem(ItemPosition itemPosition) {
				if (itemPosition.getItem() == item) {
					result[0] = itemPosition;
					return false;
				}
				return true;
			}
		});
		return result[0];
	}

	public void setSelection(List<ItemPosition> toSelect) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (int i = 0; i < toSelect.size(); i++) {
			ItemPosition itemPosition = toSelect.get(i);
			if (itemPosition == null) {
				treeTableComponent.clearSelection();
			} else {
				ItemNode itemNode = findNode(itemPosition);
				if (itemNode == null) {
					ItemPosition parentItemPosition = itemPosition.getParentItemPosition();
					if (parentItemPosition == null) {
						treeTableComponent.clearSelection();
						return;
					}
					toSelect = new ArrayList<ItemPosition>(toSelect);
					toSelect.set(i, parentItemPosition);
					setSelection(toSelect);
					return;
				}
				treePaths.add(new TreePath(itemNode.getPath()));
			}
		}
		treeTableComponent.getTreeSelectionModel().setSelectionPaths(treePaths.toArray(new TreePath[treePaths.size()]));
		try {
			treeTableComponent.scrollRowToVisible(treeTableComponent.getRowForPath(treePaths.get(0)));
		} catch (Throwable ignore) {
		}
	}

	protected ItemNode findNode(ItemPosition itemPosition) {
		ItemNode parentNode;
		if (itemPosition.isRootListItemPosition()) {
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
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				if (userConfirms("Remove the element(s)?")) {
					List<ItemPosition> selection = getSelection();
					selection = new ArrayList<ItemPosition>(selection);
					Collections.reverse(selection);
					List<ItemPosition> toPostSelect = new ArrayList<ItemPosition>();
					for (ItemPosition itemPosition : selection) {
						int index = itemPosition.getIndex();
						getModificationStack().apply(
								new ListModificationFactory(itemPosition, getModificationsTarget()).remove(index));
						updateItemPositionsAfterItemRemoval(toPostSelect, itemPosition);
						if (itemPosition.getContainingListType().isOrdered() && (index > 0)) {
							toPostSelect.add(itemPosition.getSibling(index - 1));
						} else {
							toPostSelect.add(itemPosition.getParentItemPosition());
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
				List<ItemPosition> selection = getSelection();
				if (selection.size() == 0) {
					return false;
				}
				if (selection.size() > 0) {
					for (ItemPosition selectionItem : selection) {
						if (!new ListModificationFactory(selectionItem, getModificationsTarget())
								.canRemove(selectionItem.getIndex())) {
							return false;
						}
						if (!selectionItem.getContainingListType().isRemovalAllowed()) {
							return false;
						}
					}
				}
				return true;
			}
		};
	}

	protected void updateItemPositionsAfterItemRemoval(List<ItemPosition> toUpdate, ItemPosition removed) {
		for (int i = 0; i < toUpdate.size(); i++) {
			ItemPosition toUpdateItem = toUpdate.get(i);
			if (toUpdateItem.equals(removed) || toUpdateItem.getAncestors().contains(removed)) {
				toUpdate.remove(i);
				i--;
			} else if (toUpdateItem.getPreviousSiblings().contains(removed)) {
				toUpdate.set(i, toUpdateItem.getSibling(toUpdateItem.getIndex() - 1));
			}
		}
	}

	protected AbstractStandardListAction createInsertAction(final InsertPosition insertPosition) {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				ItemPosition newItemPosition = getNewItemPosition();
				IListTypeInfo listType = newItemPosition.getContainingListType();
				Object newItem = createItem(newItemPosition);
				if (newItem == null) {
					return false;
				}
				BufferedItemPosition futureItemPosition = new BufferedItemPosition(newItemPosition, newItem);
				ItemUIBuilder dialogBuilder = new ItemUIBuilder(futureItemPosition) {
					ModificationStack dummyModificationStack = new ModificationStack(null);

					@Override
					public ModificationStack getParentModificationStack() {
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
				};
				dialogBuilder.showDialog();
				if (dialogBuilder.wasOkPressed()) {
					newItem = dialogBuilder.getCurrentObjectValue();
					getModificationStack().apply(new ListModificationFactory(newItemPosition, getModificationsTarget())
							.add(newItemPosition.getIndex(), newItem));
					ItemPosition toSelect = newItemPosition;
					if (!listType.isOrdered()) {
						int indexToSelect = Arrays.asList(newItemPosition.getContainingListRawValue()).indexOf(newItem);
						toSelect = newItemPosition.getSibling(indexToSelect);
					}
					toPostSelectHolder[0] = Collections.singletonList(toSelect);
					return true;
				} else {
					return false;
				}
			}

			@Override
			protected String getActionTitle() {
				ItemPosition newItemPosition = getNewItemPosition();
				if (newItemPosition == null) {
					return null;
				}
				IListTypeInfo listType = newItemPosition.getContainingListType();
				ITypeInfo itemType = listType.getItemType();

				String buttonText = "Insert";
				{
					if (itemType != null) {
						buttonText += " " + itemType.getCaption();
					}
					if (listType.isOrdered()) {
						if (insertPosition == InsertPosition.AFTER) {
							buttonText += " After";
						} else if (insertPosition == InsertPosition.BEFORE) {
							buttonText += " Before";
						}
					} else {
						if (insertPosition == InsertPosition.AFTER) {
							return null;
						} else if (insertPosition == InsertPosition.BEFORE) {
							return null;
						}
					}
					buttonText += " ...";
				}
				return buttonText;
			}

			private ItemPosition getNewItemPosition() {
				ItemPosition singleSelection = getSingleSelection();
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
				return "Insert item into '" + getRootListTitle() + "'";
			}

			@Override
			public boolean isValid() {
				ItemPosition newItemPosition = getNewItemPosition();
				if (newItemPosition != null) {
					if (newItemPosition.getContainingListType().isInsertionAllowed()) {
						if (new ListModificationFactory(newItemPosition, getModificationsTarget())
								.canAdd(newItemPosition.getIndex())) {
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
		};
	}

	protected AbstractStandardListAction createAddChildAction() {
		return new AbstractStandardListAction() {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				ItemPosition newSubItemPosition = getNewSubItemPosition();
				IListTypeInfo subListType = newSubItemPosition.getContainingListType();
				Object newSubListItem = createItem(getNewSubItemPosition());
				if (newSubListItem == null) {
					return false;
				}
				BufferedItemPosition futureSubItemPosition = new BufferedItemPosition(newSubItemPosition,
						newSubListItem);
				ItemUIBuilder dialogBuilder = new ItemUIBuilder(futureSubItemPosition) {
					ModificationStack dummyModificationStack = new ModificationStack(null);

					@Override
					public ModificationStack getParentModificationStack() {
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

				};
				dialogBuilder.showDialog();
				if (dialogBuilder.wasOkPressed()) {
					newSubListItem = dialogBuilder.getCurrentObjectValue();
					getModificationStack()
							.apply(new ListModificationFactory(newSubItemPosition, getModificationsTarget())
									.add(newSubItemPosition.getIndex(), newSubListItem));
					if (!subListType.isOrdered()) {
						newSubItemPosition = newSubItemPosition.getSibling(
								Arrays.asList(newSubItemPosition.getContainingListRawValue()).indexOf(newSubListItem));
					}
					ItemPosition toSelect = newSubItemPosition.getSibling(newSubItemPosition.getIndex());
					toPostSelectHolder[0] = Collections.singletonList(toSelect);
					return true;
				} else {
					return false;
				}
			}

			@Override
			public boolean isValid() {
				ItemPosition newSubItemPosition = getNewSubItemPosition();
				if (newSubItemPosition == null) {
					return false;
				}
				if (!new ListModificationFactory(newSubItemPosition, getModificationsTarget())
						.canAdd(newSubItemPosition.getIndex())) {
					return false;
				}
				if (!newSubItemPosition.getContainingListType().isInsertionAllowed()) {
					return false;
				}
				return true;
			}

			protected ItemPosition getNewSubItemPosition() {
				ItemPosition result = null;
				ItemPosition singleSelection = getSingleSelection();
				if (singleSelection != null) {
					result = ListControl.this.getSubItemPosition(singleSelection);
				}
				if (getSelection().size() == 0) {
					result = getAnyRootListItemPosition();
				}
				if (result != null) {
					result = result.getSibling(result.getContainingListRawValue().length);
				}
				return result;
			}

			@Override
			protected String getActionTitle() {
				ItemPosition subItemPosition = getNewSubItemPosition();
				final IListTypeInfo subListType = subItemPosition.getContainingListType();
				final ITypeInfo subListItemType = subListType.getItemType();
				String title = "Add";
				if (subItemPosition.getDepth() > 0) {
					title += " Child";
				}
				if (subListItemType != null) {
					title += " " + subListItemType.getCaption();
				}
				title += "...";
				return title;
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Add item into '" + getRootListTitle() + "'";
			}

		};
	}

	protected Object createItem(ItemPosition itemPosition) {
		IListTypeInfo subListType = itemPosition.getContainingListType();
		ITypeInfo typeToInstanciate = subListType.getItemType();
		if (typeToInstanciate == null) {
			typeToInstanciate = new DefaultTypeInfo(swingRenderer.getReflectionUI(), Object.class);
		}
		typeToInstanciate = addSpecificItemContructors(typeToInstanciate, itemPosition);
		try {
			return swingRenderer.onTypeInstanciationRequest(ListControl.this, typeToInstanciate, true);
		} catch (Throwable ignore) {
			return swingRenderer.onTypeInstanciationRequest(ListControl.this, typeToInstanciate, false);
		}
	}

	protected ITypeInfo addSpecificItemContructors(ITypeInfo itemType, final ItemPosition newItemPosition) {
		return new TypeInfoProxyFactory() {

			@Override
			protected List<IMethodInfo> getConstructors(ITypeInfo type) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(type));
				IFieldControlData containingListData = newItemPosition.getContainingListData();
				IListTypeInfo containingListType = newItemPosition.getContainingListType();
				List<IMethodInfo> specificItemConstructors = containingListType
						.getAdditionalItemConstructors(containingListData.getValue());
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
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				clipboard.clear();
				List<ItemPosition> selection = getSelection();
				for (ItemPosition itemPosition : selection) {
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
				List<ItemPosition> selection = getSelection();
				if (selection.size() > 0) {
					if (canCopyAll(selection)) {
						return true;
					}
				}
				return false;
			}

		};
	}

	protected AbstractStandardListAction createCutAction() {
		return new AbstractStandardListAction() {

			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				clipboard.clear();
				List<ItemPosition> selection = getSelection();
				selection = new ArrayList<ItemPosition>(selection);
				Collections.reverse(selection);
				List<ItemPosition> toPostSelect = new ArrayList<ItemPosition>();
				for (ItemPosition itemPosition : selection) {
					clipboard.add(0, ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getItem()));
					int index = itemPosition.getIndex();
					getModificationStack()
							.apply(new ListModificationFactory(itemPosition, getModificationsTarget()).remove(index));
					updateItemPositionsAfterItemRemoval(toPostSelect, itemPosition);
					if (itemPosition.getContainingListType().isOrdered() && (index > 0)) {
						toPostSelect.add(itemPosition.getSibling(index - 1));
					} else {
						toPostSelect.add(itemPosition.getParentItemPosition());
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
				List<ItemPosition> selection = getSelection();
				if (selection.size() > 0) {
					if (canCopyAll(selection) && canRemoveAll(selection)) {
						return true;
					}
				}
				return false;
			}

		};
	}

	protected AbstractStandardListAction createPasteAction(final InsertPosition insertPosition) {
		return new AbstractStandardListAction() {

			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				ItemPosition newItemPosition = getNewItemPosition();
				int index = newItemPosition.getIndex();
				int initialIndex = index;
				for (Object clipboardItem : clipboard) {
					getModificationStack().apply(new ListModificationFactory(newItemPosition, getModificationsTarget())
							.add(index, clipboardItem));
					index++;
				}
				List<ItemPosition> toPostSelect = new ArrayList<ItemPosition>();
				IListTypeInfo listType = newItemPosition.getContainingListType();
				index = initialIndex;
				for (int i = 0; i < clipboard.size(); i++) {
					Object clipboardItem = clipboard.get(i);
					if (listType.isOrdered()) {
						index = initialIndex + i;
					} else {
						index = Arrays.asList(newItemPosition.getContainingListRawValue()).indexOf(clipboardItem);
					}
					if (index != -1) {
						toPostSelect.add(newItemPosition.getSibling(index));
					}
				}
				toPostSelectHolder[0] = toPostSelect;
				return true;
			}

			protected ItemPosition getNewItemPosition() {
				List<ItemPosition> selection = getSelection();
				if (selection.size() == 1) {
					ItemPosition singleSelection = selection.get(0);
					int index = singleSelection.getIndex();
					return singleSelection.getSibling(index + 1);
				} else {
					return null;
				}
			}

			@Override
			protected String getActionTitle() {
				String result;
				if (insertPosition == InsertPosition.AFTER) {
					result = "Paste After";
				} else if (insertPosition == InsertPosition.BEFORE) {
					result = "Paste Before";
				} else {
					result = "Paste";
				}
				return result;
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Paste item(s) into '" + getRootListTitle() + "'";
			}

			@Override
			public boolean isValid() {
				if (clipboard.size() > 0) {
					ItemPosition newItemPosition = getNewItemPosition();
					if (newItemPosition != null) {
						if (new ListModificationFactory(newItemPosition, getModificationsTarget())
								.canAdd(newItemPosition.getIndex())) {
							if (itemPositionSupportsAllClipboardItems(newItemPosition)) {
								if (newItemPosition.getContainingListType().isOrdered()) {
									if (insertPosition == InsertPosition.BEFORE) {
										return true;
									}
									if (insertPosition == InsertPosition.AFTER) {
										return true;
									}
								} else {
									if (insertPosition == InsertPosition.UNKNOWN) {
										return true;
									}
								}
							}
						}
					}
				}
				return false;
			}
		};
	}

	protected AbstractStandardListAction createPasteIntoAction() {
		return new AbstractStandardListAction() {

			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				ItemPosition subItemPosition = getNewItemPosition();
				int newSubListItemIndex = subItemPosition.getContainingListRawValue().length;
				int newSubListItemInitialIndex = newSubListItemIndex;
				subItemPosition = subItemPosition.getSibling(newSubListItemIndex);
				for (Object clipboardItem : clipboard) {
					getModificationStack().apply(new ListModificationFactory(subItemPosition, getModificationsTarget())
							.add(newSubListItemIndex, clipboardItem));
					newSubListItemIndex++;
				}
				List<ItemPosition> toPostSelect = new ArrayList<ItemPosition>();
				IListTypeInfo subListType = subItemPosition.getContainingListType();
				newSubListItemIndex = newSubListItemInitialIndex;
				for (int i = 0; i < clipboard.size(); i++) {
					Object clipboardItem = clipboard.get(i);
					if (subListType.isOrdered()) {
						newSubListItemInitialIndex = newSubListItemInitialIndex + i;
					} else {
						newSubListItemInitialIndex = Arrays.asList(subItemPosition.getContainingListRawValue())
								.indexOf(clipboardItem);
					}
					if (newSubListItemInitialIndex != -1) {
						toPostSelect.add(subItemPosition.getSibling(newSubListItemInitialIndex));
					}
				}
				toPostSelectHolder[0] = toPostSelect;
				return true;
			}

			private ItemPosition getNewItemPosition() {
				List<ItemPosition> selection = getSelection();
				if (selection.size() == 0) {
					return getAnyRootListItemPosition().getSibling(0);
				}
				if (selection.size() == 1) {
					return getSubItemPosition(selection.get(0));
				}
				return null;
			}

			@Override
			protected String getActionTitle() {
				return "Paste";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Paste item(s) into '" + getRootListTitle() + "'";
			}

			@Override
			protected boolean isValid() {
				if (clipboard.size() > 0) {
					ItemPosition newItemPosition = getNewItemPosition();
					if (newItemPosition != null) {
						if (new ListModificationFactory(newItemPosition, getModificationsTarget())
								.canAdd(newItemPosition.getIndex())) {
							if (itemPositionSupportsAllClipboardItems(newItemPosition)) {
								return true;
							}
						}
					}
				}
				return false;
			}
		};
	}

	protected ITypeInfo getRootListItemType() {
		return getRootListType().getItemType();
	}

	protected IListTypeInfo getRootListType() {
		return (IListTypeInfo) listData.getType();
	}

	protected IInfo getModificationsTarget() {
		return input.getModificationsTarget();
	}

	protected AbstractAction createDynamicPropertyHook(final AbstractListProperty dynamicProperty) {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				AbstractEditorDialogBuilder subDialogBuilder = new AbstractEditorDialogBuilder() {

					@Override
					public boolean isObjectFormExpanded() {
						return true;
					}

					@Override
					public boolean isObjectNullable() {
						return dynamicProperty.isNullable();
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
						Object listRootValue = listData.getValue();
						if (listRootValue == null) {
							return null;
						}
						return dynamicProperty.getValue(listRootValue);
					}

					@Override
					public String getCumulatedModificationsTitle() {
						return "Edit "
								+ ReflectionUIUtils.composeMessage(listData.getCaption(), dynamicProperty.getCaption());
					}

					@Override
					public IInfo getCumulatedModificationsTarget() {
						return dynamicProperty;
					}

					@Override
					public ModificationStack getParentModificationStack() {
						return input.getModificationStack();
					}

					@Override
					public Component getOwnerComponent() {
						return ListControl.this;
					}

					@Override
					public String getEditorTitle() {
						return dynamicProperty.getType().getCaption();
					}

					@Override
					public boolean canCommit() {
						return !dynamicProperty.isGetOnly() && !listData.isGetOnly();
					}

					@Override
					public IModification createCommitModification(Object newObjectValue) {
						Object listRootValue = listData.getValue();
						List<IModification> modifications = new ArrayList<IModification>();
						if (!dynamicProperty.isGetOnly()) {
							return null;
						}
						modifications.add(new ControlDataValueModification(
								new DefaultFieldControlData(listRootValue, dynamicProperty), newObjectValue, null));
						if (!listData.isGetOnly()) {
							return null;
						}
						modifications.add(new ControlDataValueModification(listData, listRootValue, null));
						return new CompositeModification(getCumulatedModificationsTarget(),
								getCumulatedModificationsTitle(), UndoOrder.getInverse(), modifications);
					}

					@Override
					public IInfoFilter getObjectFormFilter() {
						return IInfoFilter.NO_FILTER;
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
				action.setShouldDisplayReturnValueIfAny(true);
				action.setModificationStack(getModificationStack());
				action.execute(ListControl.this);
			}

			protected IMethodControlInput getMethodInput() {
				final ModificationStack childModifStack = new ModificationStack(null);
				final IInfo compositeModifTarget = dynamicAction;
				return new IMethodControlInput() {
					@Override
					public IInfo getModificationsTarget() {
						return compositeModifTarget;
					}

					@Override
					public ModificationStack getModificationStack() {
						return childModifStack;
					}

					@Override
					public IMethodControlData getControlData() {
						final Object listRootValue = listData.getValue();
						ITypeInfo listRootValueType = swingRenderer.getReflectionUI()
								.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(listRootValue));
						IMethodControlData data = new DefaultMethodControlData(listRootValueType, listRootValue,
								dynamicAction);
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
								Object result = SwingRendererUtils.invokeMethodThroughModificationStack(base,
										invocationData, childModifStack, compositeModifTarget);
								String compositeModifTitle = InvokeMethodModification.getTitle(base);
								IModification commitModif;
								if (listData.isGetOnly()) {
									commitModif = null;
								} else {
									commitModif = new ControlDataValueModification(listData, listRootValue,
											getModificationsTarget());
								}
								boolean childModifAccepted = true;
								ValueReturnMode childValueReturnMode = ValueReturnMode
										.combine(listData.getValueReturnMode(), base.getValueReturnMode());
								boolean childValueReplaced = false;
								ReflectionUIUtils.integrateSubModifications(swingRenderer.getReflectionUI(),
										getModificationStack(), childModifStack, childModifAccepted,
										childValueReturnMode, childValueReplaced, commitModif, compositeModifTarget,
										compositeModifTitle);
								return result;
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
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<ItemPosition>[] toPostSelectHolder) {
				ItemPosition itemPosition = getSingleSelection();
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
				ItemPosition singleSelectedPosition = getSingleSelection();
				if (singleSelectedPosition != null) {
					if (!new ItemUIBuilder(singleSelectedPosition).isObjectFormEmpty()) {
						if (singleSelectedPosition.getContainingListType().canViewItemDetails()) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}

	protected String getItemModificationTitle() {
		return "Edit '" + getRootListTitle() + "' item";
	}

	public List<Runnable> getSelectionListeners() {
		return selectionListeners;
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
		for (Runnable listener : selectionListeners) {
			listener.run();
		}
	}

	protected void updateDetailsArea() {
		ItemPosition singleSelection = getSingleSelection();
		if (!ReflectionUIUtils.equalsOrBothNull(singleSelection, detailsControlItemPosition)) {
			detailsControlItemPosition = singleSelection;
			if (detailsControl != null) {
				detailsArea.removeAll();
				detailsControlItem = null;
				detailsControl = null;
				SwingRendererUtils.handleComponentSizeChange(ListControl.this);
			}
		}
		if (detailsControlItemPosition == null) {
			return;
		}
		if (detailsControl != null) {
			swingRenderer.refreshAllFieldControls(detailsControl, false);
		} else {
			detailsControl = new ItemUIBuilder(detailsControlItemPosition).createEditorPanel();
			detailsArea.removeAll();
			detailsArea.setLayout(new BorderLayout());
			detailsArea.add(new JScrollPane(detailsControl), BorderLayout.CENTER);
			detailsArea.add(swingRenderer.createStatusBar(detailsControl), BorderLayout.NORTH);
			swingRenderer.updateFormStatusBarInBackground(detailsControl);
			SwingRendererUtils.handleComponentSizeChange(ListControl.this);
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

	protected Object[] getRootListRawValue() {
		return getAnyRootListItemPosition().getContainingListRawValue();
	}

	protected ItemPosition getAnyRootListItemPosition() {
		return new ItemPosition(listData, -1);
	}

	protected ItemPosition getActiveListItemPosition() {
		ItemPosition result = getSingleSelection();
		if (result == null) {
			result = getAnyRootListItemPosition();
			Object[] listRawValue = result.getContainingListRawValue();
			if (listRawValue != null) {
				result = result.getSibling(listRawValue.length);
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
		ItemPosition currentListItemPosition = (ItemPosition) currentNode.getUserObject();
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
	public Object getFocusDetails() {
		boolean treeTableComponentFocused = SwingRendererUtils.hasOrContainsFocus(treeTableComponent);
		;
		boolean detailsControlFocused = false;
		Object detailsControlFocusDetails = null;
		if (detailsControl != null) {
			detailsControlFocused = SwingRendererUtils.hasOrContainsFocus(detailsControl);
			if (detailsControlFocused) {
				detailsControlFocusDetails = swingRenderer.getFormFocusDetails(detailsControl);
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("treeTableComponentFocused", treeTableComponentFocused);
		result.put("detailsControlFocused", detailsControlFocused);
		result.put("detailsControlFocusDetails", detailsControlFocusDetails);
		return result;
	}

	@Override
	public boolean requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		Boolean treeTableComponentFocused = (Boolean) focusDetails.get("treeTableComponentFocused");
		Boolean detailsControlFocused = (Boolean) focusDetails.get("detailsControlFocused");
		Object detailsControlFocusDetails = focusDetails.get("detailsControlFocusDetails");
		if (Boolean.TRUE.equals(treeTableComponentFocused)) {
			return treeTableComponent.requestFocusInWindow();
		}
		if (Boolean.TRUE.equals(detailsControlFocused)) {
			if (detailsControlFocusDetails != null) {
				return swingRenderer.requestFormDetailedFocus(detailsControl, detailsControlFocusDetails);
			}
		}
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public boolean requestFocusInWindow() {
		return treeTableComponent.requestFocusInWindow();
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
		List<ItemPosition> wereSelectedPositions = getSelection();
		List<Object> wereSelected = new ArrayList<Object>();
		for (int i = 0; i < wereSelectedPositions.size(); i++) {
			try {
				ItemPosition wasSelectedPosition = wereSelectedPositions.get(i);
				wereSelected.add(wasSelectedPosition.getItem());
			} catch (Throwable t) {
				wereSelected.add(null);
			}
		}

		selectionListenersEnabled = false;

		runnable.run();

		setSelection(Collections.<ItemPosition>emptyList());
		int i = 0;
		for (Iterator<ItemPosition> it = wereSelectedPositions.iterator(); it.hasNext();) {
			ItemPosition wasSelectedPosition = it.next();
			try {
				if (!wasSelectedPosition.getContainingListType().isOrdered()) {
					Object wasSelected = wereSelected.get(i);
					int index = Arrays.asList(wasSelectedPosition.getContainingListRawValue()).indexOf(wasSelected);
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
				ItemPosition anyRootListItemPosition = getAnyRootListItemPosition();
				for (int i = 0; i < anyRootListItemPosition.getContainingListRawValue().length; i++) {
					ItemPosition rootItemPosition = anyRootListItemPosition.getSibling(i);
					ItemNode node = new ItemNode(
							new BufferedItemPosition(rootItemPosition, rootItemPosition.getItem()));
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
		protected List<ItemPosition> newSelection;

		public RefreshStructureModification(List<ItemPosition> newSelection) {
			this.newSelection = newSelection;
		}

		@Override
		public IInfo getTarget() {
			return getModificationsTarget();
		}

		@Override
		public IModification applyAndGetOpposite() {
			List<ItemPosition> oldSelection = getSelection();
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
			if (!(node.getUserObject() instanceof ItemPosition)) {
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

		protected abstract boolean perform(List<ItemPosition>[] toPostSelectHolder);

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
			final List<ItemPosition>[] toPostSelectHolder = new List[1];
			if (modifTitle == null) {
				List<ItemPosition> initialSelection = getSelection();
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

	protected class ItemUIBuilder extends AbstractEditorDialogBuilder {
		protected ItemPosition itemPosition;

		public ItemUIBuilder(ItemPosition itemPosition) {
			super();
			this.itemPosition = itemPosition;
		}

		@Override
		public boolean isObjectFormExpanded() {
			return true;
		}

		@Override
		public boolean isObjectNullable() {
			return itemPosition.getContainingListType().isItemNullable();
		}

		@Override
		public boolean canCommit() {
			return new ListModificationFactory(itemPosition, getModificationsTarget()).canSet(itemPosition.getIndex());
		}

		@Override
		public IModification createCommitModification(Object newObjectValue) {
			return new ListModificationFactory(itemPosition, getModificationsTarget()).set(itemPosition.getIndex(),
					newObjectValue);
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
			ITypeInfo result = itemPosition.getContainingListType().getItemType();
			if (result == null) {
				result = swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class));
			}
			return result;
		}

		@Override
		public ValueReturnMode getObjectValueReturnMode() {
			return itemPosition.getItemReturnMode();
		}

		@Override
		public Object getInitialObjectValue() {
			return itemPosition.getItem();
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
		public IInfoFilter getObjectFormFilter() {
			return getStructuralInfo().getItemInfoFilter(itemPosition);
		}

	}

	public interface IItemsVisitor {

		boolean visitItem(ItemPosition itemPosition);

	}

	protected enum InsertPosition {
		AFTER, BEFORE, UNKNOWN
	}
}
