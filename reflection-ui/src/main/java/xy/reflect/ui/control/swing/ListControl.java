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
import java.util.AbstractList;
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

import xy.reflect.ui.control.input.FieldControlData;
import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.control.input.IControlInput;
import xy.reflect.ui.info.DesktopSpecificProperty;
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
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.SubListsGroupingField.SubListGroup;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.undo.UpdateListValueModification;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractLazyTreeNode;

public class ListControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;

	protected IControlData listData;

	protected JXTreeTable treeTableComponent;
	protected JPanel toolbar;
	protected ItemNode rootNode;
	protected static List<Object> clipboard = new ArrayList<Object>();
	protected Map<ItemNode, Map<Integer, String>> valuesByNode = new HashMap<ItemNode, Map<Integer, String>>();
	protected IListStructuralInfo structuralInfo;

	protected JPanel detailsArea;
	protected JPanel detailsControl;
	protected IListItemDetailsAccessMode detailsMode;
	protected GhostItemPosition detailsControlItemPosition;
	protected Object detailsControlItem;

	protected List<Runnable> selectionListeners = new ArrayList<Runnable>();
	protected boolean selectionListenersEnabled = true;
	protected IFieldInfo modifiedField;
	protected IControlInput input;

	protected static AbstractAction SEPARATOR_ACTION = new AbstractAction("") {
		protected static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};

	public ListControl(final SwingRenderer swingRenderer, IControlInput input) {
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
				Dimension result = new Dimension();
				{
					result.width = 0;
				}
				{
					int minHeight = (int) (screenSize.height * 0.15);
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

		if (UpdateListValueModification.isCompatibleWith(getRootListItemPosition())) {
			AbstractAction addChildAction = createAddChildAction();
			AbstractAction insertAction = createInsertAction(InsertPosition.UNKNOWN);
			AbstractAction insertActionBefore = createInsertAction(InsertPosition.BEFORE);
			AbstractAction insertActionAfter = createInsertAction(InsertPosition.AFTER);
			toolbar.add(createTool(null, SwingRendererUtils.ADD_ICON, false, false, addChildAction, insertAction,
					insertActionBefore, insertActionAfter));
			toolbar.add(createTool(null, SwingRendererUtils.REMOVE_ICON, false, false, createRemoveAction()));
			AbstractStandardListAction moveUpAction = createMoveAction(-1);
			AbstractStandardListAction moveDownAction = createMoveAction(1);
			if (moveUpAction.isEnabled() || moveDownAction.isEnabled()) {
				toolbar.add(createTool(null, SwingRendererUtils.UP_ICON, true, false, moveUpAction));
				toolbar.add(createTool(null, SwingRendererUtils.DOWN_ICON, true, false, moveDownAction));
			}
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
			AutoFieldValueUpdatingItemPosition itemPosition = (AutoFieldValueUpdatingItemPosition) node.getUserObject();
			if (itemPosition == null) {
				value = "";
			} else {
				Object item = itemPosition.getLastKnownItem();
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
		AutoFieldValueUpdatingItemPosition itemPosition = (AutoFieldValueUpdatingItemPosition) node.getUserObject();
		if (columnIndex == 0) {
			return swingRenderer.getObjectIconImage(itemPosition.getLastKnownItem());
		}
		return null;
	}

	protected List<AbstractAction> createCurrentSelectionActions() {

		List<AbstractAction> result = new ArrayList<AbstractAction>();

		List<AutoFieldValueUpdatingItemPosition> selection = getSelection();

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

	protected boolean anySelectionItemContainingListGetOnly() {
		boolean result = false;
		List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
		for (AutoFieldValueUpdatingItemPosition selectionItem : selection) {
			if (!UpdateListValueModification.isCompatibleWith(selectionItem)) {
				result = true;
				break;
			}
		}
		return result;
	}

	protected boolean canCopyAllSelection() {
		boolean result = true;
		List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
		for (AutoFieldValueUpdatingItemPosition selectionItem : selection) {
			if (!ReflectionUIUtils.canCopy(swingRenderer.getReflectionUI(), selectionItem.getLastKnownItem())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean allSelectionItemsInSameList() {
		boolean result = true;
		List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
		AutoFieldValueUpdatingItemPosition firstSelectionItem = selection.get(0);
		for (AutoFieldValueUpdatingItemPosition selectionItem : selection) {
			if (!ReflectionUIUtils.equalsOrBothNull(firstSelectionItem.getParentItemPosition(),
					selectionItem.getParentItemPosition())) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected boolean itemPositionSupportsAllClipboardItems(AutoFieldValueUpdatingItemPosition itemPosition) {
		boolean result = true;
		for (Object clipboardItem : clipboard) {
			if (!itemPosition.supportsItem(clipboardItem)) {
				result = false;
				break;
			}
		}
		return result;
	}

	protected AutoFieldValueUpdatingItemPosition getSubItemPosition(AutoFieldValueUpdatingItemPosition itemPosition) {
		if (itemPosition == null) {
			return getRootListItemPosition();
		}
		if (itemPosition.getLastKnownItem() != null) {
			IListStructuralInfo treeInfo = getStructuralInfo();
			if (treeInfo != null) {
				final IFieldInfo subListField = treeInfo.getItemSubListField(itemPosition);
				if (subListField != null) {
					FieldControlData subListData = new FieldControlData(itemPosition.getLastKnownItem(), subListField);
					return new AutoFieldValueUpdatingItemPosition(itemPosition, subListData, -1) {

						@Override
						public String getContainingListTitle() {
							return subListField.getCaption();
						}

					};
				}
			}
		}
		return null;
	}

	protected AbstractStandardListAction createClearAction() {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				if (!userConfirms("Remove all the items?")) {
					return false;
				}
				getRootList().clear();
				toPostSelectHolder[0] = Collections.emptyList();
				return true;
			}

			@Override
			protected String getTitle() {
				return "Remove All";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Clear '" + getRootListTitle() + "'";
			}

			@Override
			public boolean isValid() {
				if (getRootList().size() > 0) {
					if (UpdateListValueModification.isCompatibleWith(getRootListItemPosition())) {
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (offset > 0) {
					selection = new ArrayList<AutoFieldValueUpdatingItemPosition>(selection);
					Collections.reverse(selection);
				}
				List<AutoFieldValueUpdatingItemPosition> newSelection = new ArrayList<ListControl.AutoFieldValueUpdatingItemPosition>();
				for (AutoFieldValueUpdatingItemPosition itemPosition : selection) {
					AutoFieldValueUpdatingList list = itemPosition.getContainingAutoUpdatingFieldList();
					int index = itemPosition.getIndex();
					list.move(index, offset);
					newSelection.add(itemPosition.getSibling(index + offset));
				}
				toPostSelectHolder[0] = newSelection;
				return true;
			}

			@Override
			protected String getTitle() {
				return (offset > 0) ? "Move Down" : "Move Up";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Move '" + getRootListTitle() + "' item(s)";
			}

			@Override
			public boolean isValid() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() > 0) {
					if (!anySelectionItemContainingListGetOnly()) {
						if (allSelectionItemsInSameList()) {
							if (selection.get(0).getContainingListType().isOrdered()) {
								boolean canMoveAllItems = true;
								for (AutoFieldValueUpdatingItemPosition itemPosition : selection) {
									AutoFieldValueUpdatingList list = itemPosition.getContainingAutoUpdatingFieldList();
									int index = itemPosition.getIndex();
									if ((index + offset) < 0) {
										canMoveAllItems = false;
										break;
									}
									if ((index + offset) >= list.size()) {
										canMoveAllItems = false;
										break;
									}
								}
								if (canMoveAllItems) {
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

	public AutoFieldValueUpdatingItemPosition getSingleSelection() {
		List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
		if ((selection.size() == 0) || (selection.size() > 1)) {
			return null;
		} else {
			return selection.get(0);
		}
	}

	public List<AutoFieldValueUpdatingItemPosition> getSelection() {
		List<AutoFieldValueUpdatingItemPosition> result = new ArrayList<ListControl.AutoFieldValueUpdatingItemPosition>();
		for (int selectedRow : treeTableComponent.getSelectedRows()) {
			TreePath path = treeTableComponent.getPathForRow(selectedRow);
			if (path == null) {
				return null;
			}
			ItemNode selectedNode = (ItemNode) path.getLastPathComponent();
			AutoFieldValueUpdatingItemPosition itemPosition = (AutoFieldValueUpdatingItemPosition) selectedNode
					.getUserObject();
			result.add(itemPosition);
		}
		return result;
	}

	public void setSingleSelection(AutoFieldValueUpdatingItemPosition toSelect) {
		setSelection(Collections.singletonList(toSelect));
	}

	public AutoFieldValueUpdatingItemPosition findItemPositionByReference(final Object item) {
		final AutoFieldValueUpdatingItemPosition[] result = new AutoFieldValueUpdatingItemPosition[1];
		visitItems(new IItemsVisitor() {
			@Override
			public boolean visitItem(AutoFieldValueUpdatingItemPosition itemPosition) {
				if (itemPosition.getLastKnownItem() == item) {
					result[0] = itemPosition;
					return false;
				}
				return true;
			}
		});
		return result[0];
	}

	public void setSelection(List<AutoFieldValueUpdatingItemPosition> toSelect) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (int i = 0; i < toSelect.size(); i++) {
			AutoFieldValueUpdatingItemPosition itemPosition = toSelect.get(i);
			if (itemPosition == null) {
				treeTableComponent.clearSelection();
			} else {
				ItemNode itemNode = findNode(itemPosition);
				if (itemNode == null) {
					AutoFieldValueUpdatingItemPosition parentItemPosition = itemPosition.getParentItemPosition();
					if (parentItemPosition == null) {
						treeTableComponent.clearSelection();
						return;
					}
					toSelect = new ArrayList<ListControl.AutoFieldValueUpdatingItemPosition>(toSelect);
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

	protected ItemNode findNode(AutoFieldValueUpdatingItemPosition itemPosition) {
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				if (userConfirms("Remove the element(s)?")) {
					List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
					selection = new ArrayList<AutoFieldValueUpdatingItemPosition>(selection);
					Collections.reverse(selection);
					List<AutoFieldValueUpdatingItemPosition> toPostSelect = new ArrayList<ListControl.AutoFieldValueUpdatingItemPosition>();
					for (AutoFieldValueUpdatingItemPosition itemPosition : selection) {
						AutoFieldValueUpdatingList list = itemPosition.getContainingAutoUpdatingFieldList();
						int index = itemPosition.getIndex();
						list.remove(index);
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
			protected String getTitle() {
				return "Remove";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Remove '" + getRootListTitle() + "' item(s)";
			}

			@Override
			public boolean isValid() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() == 0) {
					return false;
				}
				if (selection.size() > 0) {
					for (AutoFieldValueUpdatingItemPosition selectionItem : selection) {
						if (!UpdateListValueModification.isCompatibleWith(selectionItem)) {
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

	protected void updateItemPositionsAfterItemRemoval(List<AutoFieldValueUpdatingItemPosition> toUpdate,
			AutoFieldValueUpdatingItemPosition removed) {
		for (int i = 0; i < toUpdate.size(); i++) {
			AutoFieldValueUpdatingItemPosition toUpdateItem = toUpdate.get(i);
			if (toUpdateItem.equals(removed) || toUpdateItem.getAncestors().contains(removed)) {
				toUpdate.remove(i);
				i--;
			} else if (toUpdateItem.getPreviousSiblings().contains(removed)) {
				toUpdate.set(i, toUpdateItem.getSibling(toUpdateItem.getIndex() - 1));
			}
		}
	}

	protected class GhostItemPosition extends AutoFieldValueUpdatingItemPosition {

		protected AutoFieldValueUpdatingItemPosition itemPosition;

		public GhostItemPosition(AutoFieldValueUpdatingItemPosition itemPosition, Object item) {
			super(itemPosition.getParentItemPosition(), itemPosition.getContainingListData(), itemPosition.getIndex());
			this.itemPosition = itemPosition;
			this.lastKnowItem = item; 
		}

		public boolean isInReadOnlyMode() {
			return !UpdateListValueModification.isCompatibleWith(itemPosition);
		}

		@Override
		public Object getLastKnownItem() {
			if (isInReadOnlyMode()) {
				super.updateLastKnownItem();
			}
			return lastKnowItem;
		}

		public void setLastKnownItem(Object item) {
			if (isInReadOnlyMode()) {
				throw new ReflectionUIError();
			}
			this.lastKnowItem = item;
		}

		@Override
		protected void updateLastKnownItem() {
		}

		public String getContainingListTitle() {
			return itemPosition.getContainingListTitle();
		}

		@Override
		public AutoFieldValueUpdatingList getContainingAutoUpdatingFieldList() {
			throw new ReflectionUIError();
		}

		private ListControl getOuterType() {
			return ListControl.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((itemPosition == null) ? 0 : itemPosition.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			GhostItemPosition other = (GhostItemPosition) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (itemPosition == null) {
				if (other.itemPosition != null)
					return false;
			} else if (!itemPosition.equals(other.itemPosition))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Ghost" + super.toString();
		}

	}

	protected AbstractStandardListAction createInsertAction(final InsertPosition insertPosition) {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
				IListTypeInfo listType = newItemPosition.getContainingListType();
				Object newItem = createItem(newItemPosition);
				if (newItem == null) {
					return false;
				}
				GhostItemPosition futureItemPosition = new GhostItemPosition(newItemPosition, newItem);

				ObjectDialogBuilder dialogStatus = openDetailsDialog(futureItemPosition);

				if (dialogStatus == null) {
					return false;
				}
				if (dialogStatus.isOkPressed()) {
					newItem = futureItemPosition.getLastKnownItem();
					AutoFieldValueUpdatingList list = newItemPosition.getContainingAutoUpdatingFieldList();
					list.add(newItemPosition.getIndex(), newItem);
					AutoFieldValueUpdatingItemPosition toSelect = newItemPosition;
					if (!listType.isOrdered()) {
						int indexToSelect = list.indexOf(newItem);
						toSelect = newItemPosition.getSibling(indexToSelect);
					}
					toPostSelectHolder[0] = Collections.singletonList(toSelect);
					return true;
				} else {
					return false;
				}
			}

			@Override
			protected String getTitle() {
				AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
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

			private AutoFieldValueUpdatingItemPosition getNewItemPosition() {
				AutoFieldValueUpdatingItemPosition singleSelection = getSingleSelection();
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
				AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
				if (newItemPosition != null) {
					if (newItemPosition.getContainingListType().isInsertionAllowed()) {
						if (UpdateListValueModification.isCompatibleWith(newItemPosition)) {
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				AutoFieldValueUpdatingItemPosition newSubItemPosition = getNewSubItemPosition();
				IListTypeInfo subListType = newSubItemPosition.getContainingListType();
				Object newSubListItem = createItem(getNewSubItemPosition());
				if (newSubListItem == null) {
					return false;
				}
				GhostItemPosition futureSubItemPosition = new GhostItemPosition(newSubItemPosition, newSubListItem);
				ObjectDialogBuilder dialogStatus = openDetailsDialog(futureSubItemPosition);
				if (dialogStatus == null) {
					return false;
				}
				if (dialogStatus.isOkPressed()) {
					newSubListItem = futureSubItemPosition.getLastKnownItem();
					AutoFieldValueUpdatingList subList = newSubItemPosition.getContainingAutoUpdatingFieldList();
					subList.add(newSubItemPosition.getIndex(), newSubListItem);
					if (!subListType.isOrdered()) {
						newSubItemPosition = newSubItemPosition.getSibling(subList.indexOf(newSubListItem));
					}
					AutoFieldValueUpdatingItemPosition toSelect = newSubItemPosition
							.getSibling(newSubItemPosition.getIndex());
					toPostSelectHolder[0] = Collections.singletonList(toSelect);
					return true;
				} else {
					return false;
				}
			}

			@Override
			public boolean isValid() {
				AutoFieldValueUpdatingItemPosition subItemPosition = getNewSubItemPosition();
				if (subItemPosition == null) {
					return false;
				}
				if (!UpdateListValueModification.isCompatibleWith(subItemPosition)) {
					return false;
				}
				if (!subItemPosition.getContainingListType().isInsertionAllowed()) {
					return false;
				}
				return true;
			}

			private AutoFieldValueUpdatingItemPosition getNewSubItemPosition() {
				AutoFieldValueUpdatingItemPosition result = null;
				AutoFieldValueUpdatingItemPosition singleSelection = getSingleSelection();
				if (singleSelection != null) {
					result = ListControl.this.getSubItemPosition(singleSelection);
				}
				if (getSelection().size() == 0) {
					result = getRootListItemPosition();
				}
				if (result != null) {
					result = result.getSibling(result.getContainingListRawValue().length);
				}
				return result;
			}

			@Override
			protected String getTitle() {
				AutoFieldValueUpdatingItemPosition subItemPosition = getNewSubItemPosition();
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

	protected Object createItem(AutoFieldValueUpdatingItemPosition itemPosition) {
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

	protected ITypeInfo addSpecificItemContructors(ITypeInfo itemType,
			final AutoFieldValueUpdatingItemPosition newItemPosition) {
		return new TypeInfoProxyFactory() {

			@Override
			protected List<IMethodInfo> getConstructors(ITypeInfo type) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(type));
				IControlData containingListData = newItemPosition.getContainingListData();
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				clipboard.clear();
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				for (AutoFieldValueUpdatingItemPosition itemPosition : selection) {
					clipboard.add(
							ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getLastKnownItem()));
				}
				return false;
			}

			@Override
			protected String getTitle() {
				return "Copy";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return null;
			}

			@Override
			public boolean isValid() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() > 0) {
					if (canCopyAllSelection()) {
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				clipboard.clear();
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				selection = new ArrayList<AutoFieldValueUpdatingItemPosition>(selection);
				Collections.reverse(selection);
				List<AutoFieldValueUpdatingItemPosition> toPostSelect = new ArrayList<ListControl.AutoFieldValueUpdatingItemPosition>();
				for (AutoFieldValueUpdatingItemPosition itemPosition : selection) {
					clipboard.add(0,
							ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getLastKnownItem()));
					AutoFieldValueUpdatingList list = itemPosition.getContainingAutoUpdatingFieldList();
					int index = itemPosition.getIndex();
					list.remove(index);
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
			protected String getTitle() {
				return "Cut";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Cut '" + getRootListTitle() + "' item(s)";
			}

			@Override
			public boolean isValid() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() > 0) {
					if (canCopyAllSelection()) {
						if (!anySelectionItemContainingListGetOnly()) {
							return true;
						}
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
				int index = newItemPosition.getIndex();
				int initialIndex = index;
				AutoFieldValueUpdatingList list = newItemPosition.getContainingAutoUpdatingFieldList();
				for (Object clipboardItem : clipboard) {
					list.add(index, clipboardItem);
					index++;
				}
				List<AutoFieldValueUpdatingItemPosition> toPostSelect = new ArrayList<ListControl.AutoFieldValueUpdatingItemPosition>();
				IListTypeInfo listType = newItemPosition.getContainingListType();
				index = initialIndex;
				for (int i = 0; i < clipboard.size(); i++) {
					Object clipboardItem = clipboard.get(i);
					if (listType.isOrdered()) {
						index = initialIndex + i;
					} else {
						index = list.indexOf(clipboardItem);
					}
					if (index != -1) {
						toPostSelect.add(newItemPosition.getSibling(index));
					}
				}
				toPostSelectHolder[0] = toPostSelect;
				return true;
			}

			private AutoFieldValueUpdatingItemPosition getNewItemPosition() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() == 0) {
					int index = getRootList().size();
					return getRootListItemPosition().getSibling(index);
				} else if (selection.size() == 1) {
					AutoFieldValueUpdatingItemPosition singleSelection = selection.get(0);
					int index = singleSelection.getIndex();
					return singleSelection.getSibling(index + 1);
				} else {
					return null;
				}
			}

			@Override
			protected String getTitle() {
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
					AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
					if (newItemPosition != null) {
						if (UpdateListValueModification.isCompatibleWith(newItemPosition)) {
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				AutoFieldValueUpdatingItemPosition subItemPosition = getNewItemPosition();
				AutoFieldValueUpdatingList subList = subItemPosition.getContainingAutoUpdatingFieldList();
				int newSubListItemIndex = subList.size();
				int newSubListItemInitialIndex = newSubListItemIndex;
				subItemPosition = subItemPosition.getSibling(newSubListItemIndex);
				for (Object clipboardItem : clipboard) {
					subList.add(newSubListItemIndex, clipboardItem);
					newSubListItemIndex++;
				}
				List<AutoFieldValueUpdatingItemPosition> toPostSelect = new ArrayList<ListControl.AutoFieldValueUpdatingItemPosition>();
				IListTypeInfo subListType = subItemPosition.getContainingListType();
				newSubListItemIndex = newSubListItemInitialIndex;
				for (int i = 0; i < clipboard.size(); i++) {
					Object clipboardItem = clipboard.get(i);
					if (subListType.isOrdered()) {
						newSubListItemInitialIndex = newSubListItemInitialIndex + i;
					} else {
						newSubListItemInitialIndex = subList.indexOf(clipboardItem);
					}
					if (newSubListItemInitialIndex != -1) {
						toPostSelect.add(subItemPosition.getSibling(newSubListItemInitialIndex));
					}
				}
				toPostSelectHolder[0] = toPostSelect;
				return true;
			}

			private AutoFieldValueUpdatingItemPosition getNewItemPosition() {
				AutoFieldValueUpdatingItemPosition itemPosition = getSingleSelection();
				if (itemPosition == null) {
					return null;
				}
				return getSubItemPosition(itemPosition);
			}

			@Override
			protected String getTitle() {
				return "Paste";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Paste item(s) into '" + getRootListTitle() + "'";
			}

			@Override
			protected boolean isValid() {
				if (clipboard.size() > 0) {
					AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
					if (newItemPosition != null) {
						if (UpdateListValueModification.isCompatibleWith(newItemPosition)) {
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

	protected IInfo getModifiedField() {
		return input.getField();
	}

	protected AbstractAction createDynamicPropertyHook(final AbstractListProperty dynamicProperty) {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				final Object listRootValue = listData.getValue();
				EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
						dynamicProperty.getType());
				encapsulation.setTypeCaption(
						ReflectionUIUtils.composeMessage(ListControl.this.getRootListTitle(), "Dynamic Property"));
				encapsulation.setFieldCaption(dynamicProperty.getCaption());
				encapsulation.setFieldGetOnly(dynamicProperty.isGetOnly());
				encapsulation.setFieldNullable(dynamicProperty.isNullable());
				encapsulation.setTypeModificationStackAccessible(
						UpdateListValueModification.isCompatibleWith(getRootListItemPosition()));
				Object encapsulatedPropertyValue = encapsulation.getInstance(new Accessor<Object>() {

					@Override
					public Object get() {
						return dynamicProperty.getValue(listRootValue);
					}

					@Override
					public void set(Object t) {
						dynamicProperty.setValue(listRootValue, t);
					}

				});

				ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(swingRenderer, ListControl.this,
						encapsulatedPropertyValue);
				swingRenderer.showDialog(dialogBuilder.build(), true);

				ModificationStack parentModifStack = getParentFormModificationStack();
				ModificationStack childModifStack = dialogBuilder.getModificationStack();
				IInfo childModifTarget = dynamicProperty;
				IModification commitModif;
				if (dynamicProperty.isGetOnly()) {
					commitModif = null;
				} else {
					commitModif = new UpdateListValueModification(getRootListItemPosition(),
							getRootListType().toArray(listRootValue), dynamicProperty);
				}
				boolean childModifAccepted = (!dialogBuilder.isCancellable()) || dialogBuilder.isOkPressed();
				ValueReturnMode childValueReturnMode = dynamicProperty.getValueReturnMode();
				boolean childValueNew = dialogBuilder.isValueNew();
				return ReflectionUIUtils.integrateSubModifications(swingRenderer.getReflectionUI(), parentModifStack,
						childModifStack, childModifAccepted, childValueReturnMode, childValueNew, commitModif,
						childModifTarget, null);
			}

			@Override
			protected String getTitle() {
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
				IMethodInfo method = dynamicAction;
				method = new MethodInfoProxy(method) {
					@Override
					public Object invoke(Object listRootValue, InvocationData invocationData) {
						return SwingRendererUtils.showBusyDialogWhileInvokingMethod(ListControl.this, swingRenderer,
								listRootValue, base, invocationData);
					}
				};
				method = new MethodInfoProxy(method) {
					@Override
					public Object invoke(Object listRootValue, InvocationData invocationData) {
						ModificationStack childModifStack = new ModificationStack(null);
						Object result = SwingRendererUtils.invokeMethodThroughModificationStack(listRootValue, base,
								invocationData, childModifStack);
						String childModifTitle = InvokeMethodModification.getTitle(base);
						IInfo childModifTarget = base;
						IModification commitModif;
						if (base.isReadOnly()) {
							commitModif = null;
						} else {
							commitModif = new UpdateListValueModification(getRootListItemPosition(),
									getRootListType().toArray(listRootValue), base);
						}
						boolean childModifAccepted = true;
						ValueReturnMode childValueReturnMode = base.getValueReturnMode();
						boolean childValueNew = false;
						ReflectionUIUtils.integrateSubModifications(swingRenderer.getReflectionUI(),
								getParentFormModificationStack(), childModifStack, childModifAccepted,
								childValueReturnMode, childValueNew, commitModif, childModifTarget, childModifTitle);
						return result;
					}
				};
				MethodAction action = swingRenderer.createMethodAction(listData.getValue(), method);
				action.setShouldDisplayReturnValueIfAny(true);
				action.setRetunValueWindowDetached(method.getValueReturnMode() == ValueReturnMode.COPY);
				action.setModificationStack(getParentFormModificationStack());
				action.execute(ListControl.this);
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
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				AutoFieldValueUpdatingItemPosition itemPosition = getSingleSelection();
				GhostItemPosition futureItemPosition = new GhostItemPosition(itemPosition,
						itemPosition.getLastKnownItem());

				final ObjectDialogBuilder dialogStatus = openDetailsDialog(futureItemPosition);

				ModificationStack parentModifStack = getParentFormModificationStack();
				ModificationStack childModifStack = dialogStatus.getModificationStack();
				ValueReturnMode childValueReturnMode = itemPosition.getContainingListData().getValueReturnMode();
				IInfo childModifTarget = getModifiedField();
				IModification commitModif;
				if (!UpdateListValueModification.isCompatibleWith(itemPosition)) {
					commitModif = null;
				} else {
					Object[] listRawValue = itemPosition.getContainingListRawValue();
					listRawValue[itemPosition.getIndex()] = futureItemPosition.getLastKnownItem();
					commitModif = new UpdateListValueModification(itemPosition, listRawValue, childModifTarget);
				}

				toPostSelectHolder[0] = Collections.singletonList(itemPosition);
				boolean childModifAccepted = (!dialogStatus.isCancellable()) || dialogStatus.isOkPressed();
				boolean childValueNew = (itemPosition.getLastKnownItem() != futureItemPosition.getLastKnownItem());
				return ReflectionUIUtils.integrateSubModifications(swingRenderer.getReflectionUI(), parentModifStack,
						childModifStack, childModifAccepted, childValueReturnMode, childValueNew, commitModif,
						childModifTarget, null);

			}

			@Override
			protected String getTitle() {
				return "Open";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return getItemModificationTitle();
			}

			@Override
			public boolean isValid() {
				AutoFieldValueUpdatingItemPosition singleSelectedPosition = getSingleSelection();
				if (singleSelectedPosition != null) {
					if (hasItemDetails(singleSelectedPosition)) {
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
		AutoFieldValueUpdatingItemPosition singleSelection = getSingleSelection();
		if (singleSelection == null) {
			detailsControlItemPosition = null;
		} else {
			detailsControlItemPosition = new GhostItemPosition(singleSelection, singleSelection.getLastKnownItem());
		}
		if ((detailsControlItemPosition == null)
				|| (!detailsControlItemPosition.getContainingListType().canViewItemDetails())) {
			detailsArea.removeAll();
			detailsControlItemPosition = null;
			detailsControlItem = null;
			detailsControl = null;
			SwingRendererUtils.handleComponentSizeChange(ListControl.this);
			return;

		}
		if (detailsControl != null) {
			swingRenderer.refreshAllFieldControls(detailsControl, false);
			return;
		}
		Object encapsulated = getEncapsulatedItem(detailsControlItemPosition);
		detailsControl = swingRenderer.createForm(encapsulated);
		swingRenderer.getBusyIndicationDisabledByForm().put(detailsControl, true);
		detailsArea.removeAll();
		detailsArea.setLayout(new BorderLayout());
		detailsArea.add(new JScrollPane(detailsControl), BorderLayout.CENTER);
		detailsArea.add(swingRenderer.createStatusBar(detailsControl), BorderLayout.NORTH);
		swingRenderer.updateStatusBarInBackground(detailsControl);
		SwingRendererUtils.handleComponentSizeChange(ListControl.this);
		forwardDetailsModifications();
	}

	protected Object getEncapsulatedItem(final GhostItemPosition ghostItemPosition) {
		ITypeInfo itemType = ghostItemPosition.getContainingListType().getItemType();
		if (itemType == null) {
			itemType = swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class));
		}
		EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
				itemType);
		encapsulation.setFieldGetOnly(ghostItemPosition.isInReadOnlyMode());
		encapsulation.setFieldNullable(ghostItemPosition.isNullable());
		encapsulation.setFieldValueReturnMode(ghostItemPosition.getContainingListData().getValueReturnMode());
		encapsulation.setFieldCaption("");
		encapsulation.setTypeCaption(itemType.getCaption());
		encapsulation.setTypeModificationStackAccessible(!ghostItemPosition.isInReadOnlyMode());
		Map<String, Object> properties = new HashMap<String, Object>();
		{
			DesktopSpecificProperty.setSubFormExpanded(properties, true);
			DesktopSpecificProperty.setFilter(properties, getStructuralInfo().getItemInfoFilter(ghostItemPosition));
			encapsulation.setFieldSpecificProperties(properties);
		}
		Accessor<Object> filteredItemAccessor = new Accessor<Object>() {

			@Override
			public Object get() {
				Object result = ghostItemPosition.getLastKnownItem();
				if (result == null) {
					return null;
				}
				return result;
			}

			@Override
			public void set(Object value) {
				ghostItemPosition.setLastKnownItem(value);
			}

		};
		return encapsulation.getInstance(filteredItemAccessor);
	}

	protected void forwardDetailsModifications() {
		listData = detailsControlItemPosition.getContainingListData();
		if (listData.isGetOnly() && (listData.getValueReturnMode() == ValueReturnMode.COPY)) {
			ModificationStack childModifStack = swingRenderer.getModificationStackByForm().get(detailsControl);
			childModifStack.addListener(new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					updateDetailsArea();
				}
			});
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = Accessor.returning(listData.getValueReturnMode());
			Accessor<Boolean> childValueNewGetter = new Accessor<Boolean>() {
				@Override
				public Boolean get() {
					return detailsControlItemPosition.getLastKnownItem() != detailsControlItem;
				}
			};
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (!UpdateListValueModification.isCompatibleWith(detailsControlItemPosition)) {
						return null;
					}
					IListTypeInfo listType = (IListTypeInfo) listData.getType();
					Object[] listRawValue = listType.toArray(listData.getValue());
					listRawValue[detailsControlItemPosition.getIndex()] = detailsControlItemPosition.getLastKnownItem();
					return new UpdateListValueModification(detailsControlItemPosition, listRawValue,
							getModifiedField());
				}
			};
			Accessor<IInfo> childModifTargetGetter = new Accessor<IInfo>() {
				@Override
				public IInfo get() {
					return getModifiedField();
				}
			};
			Accessor<String> childModifTitleGetter = new Accessor<String>() {
				@Override
				public String get() {
					return getItemModificationTitle();
				}
			};
			Accessor<ModificationStack> parentModifStackGetter = new Accessor<ModificationStack>() {
				@Override
				public ModificationStack get() {
					return getParentFormModificationStack();
				}
			};
			SwingRendererUtils.forwardSubModifications(swingRenderer.getReflectionUI(), detailsControl,
					childModifAcceptedGetter, childValueReturnModeGetter, childValueNewGetter, commitModifGetter,
					childModifTargetGetter, childModifTitleGetter, parentModifStackGetter, swingRenderer);
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

	protected AutoFieldValueUpdatingList getRootList() {
		return getRootListItemPosition().getContainingAutoUpdatingFieldList();
	}

	protected AutoFieldValueUpdatingItemPosition getRootListItemPosition() {
		return new AutoFieldValueUpdatingItemPosition(null, listData, -1) {

			@Override
			public String getContainingListTitle() {
				return ListControl.this.getRootListTitle();
			}

		};
	}

	protected AutoFieldValueUpdatingItemPosition getActiveListItemPosition() {
		AutoFieldValueUpdatingItemPosition result = getSingleSelection();
		if (result == null) {
			result = getRootListItemPosition();
			Object[] listRawValue = result.getContainingListRawValue();
			if (listRawValue != null) {
				result = result.getSibling(listRawValue.length);
			}
		}
		return result;
	}

	protected ObjectDialogBuilder openDetailsDialog(GhostItemPosition ghostItemPosition) {
		ItemNode itemNode = findNode(ghostItemPosition);
		if (itemNode != null) {
			TreePath treePath = new TreePath(itemNode.getPath());
			treeTableComponent.expandPath(treePath);
		}
		if (!hasItemDetails(ghostItemPosition)) {
			return null;
		}

		Object encapsulated = getEncapsulatedItem(ghostItemPosition);

		ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(swingRenderer, ListControl.this, encapsulated);
		dialogBuilder.setCancellable(!ghostItemPosition.isInReadOnlyMode());
		swingRenderer.showDialog(dialogBuilder.build(), true);
		return dialogBuilder;
	}

	protected ModificationStack getParentFormModificationStack() {
		return input.getModificationStack();
	}

	protected boolean hasItemDetails(AutoFieldValueUpdatingItemPosition itemPosition) {
		if (itemPosition.isNullable()) {
			return true;
		}
		Object item = itemPosition.getLastKnownItem();
		ITypeInfo valueType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(item));
		IInfoFilter infoFilter = getStructuralInfo().getItemInfoFilter(itemPosition);
		if (!SwingRendererUtils.isFormEmpty(valueType, infoFilter, swingRenderer)) {
			return true;
		}
		return false;
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
		AutoFieldValueUpdatingItemPosition currentListItemPosition = (AutoFieldValueUpdatingItemPosition) currentNode
				.getUserObject();
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
		if (detailsControl == null) {
			return null;
		}
		Object detailsControlFocusDetails = swingRenderer.getFormFocusDetails(detailsControl);
		if (detailsControlFocusDetails == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("detailsControlFocusDetails", detailsControlFocusDetails);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		Object detailsControlFocusDetails = focusDetails.get("detailsControlFocusDetails");
		detailsControl.requestFocus();
		swingRenderer.setFormFocusDetails(detailsControl, detailsControlFocusDetails);
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void requestFocus() {
		if (detailsControl != null) {
			detailsControl.requestFocus();
		}
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
		List<AutoFieldValueUpdatingItemPosition> wereSelectedPositions = getSelection();
		List<Object> wereSelected = new ArrayList<Object>();
		for (int i = 0; i < wereSelectedPositions.size(); i++) {
			try {
				AutoFieldValueUpdatingItemPosition wasSelectedPosition = wereSelectedPositions.get(i);
				wereSelected.add(wasSelectedPosition.getLastKnownItem());
			} catch (Throwable t) {
				wereSelected.add(null);
			}
		}

		selectionListenersEnabled = false;

		runnable.run();

		setSelection(Collections.<AutoFieldValueUpdatingItemPosition>emptyList());
		int i = 0;
		for (Iterator<AutoFieldValueUpdatingItemPosition> it = wereSelectedPositions.iterator(); it.hasNext();) {
			AutoFieldValueUpdatingItemPosition wasSelectedPosition = it.next();
			try {
				if (!wasSelectedPosition.getContainingListType().isOrdered()) {
					Object wasSelected = wereSelected.get(i);
					int index = wasSelectedPosition.getContainingAutoUpdatingFieldList().indexOf(wasSelected);
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

	protected class ItemNode extends AbstractLazyTreeNode {

		protected static final long serialVersionUID = 1L;
		protected AutoFieldValueUpdatingItemPosition currentItemPosition;
		protected boolean childrenLoaded = false;;

		public ItemNode(AutoFieldValueUpdatingItemPosition currentItemPosition) {
			this.currentItemPosition = currentItemPosition;
			setUserObject(currentItemPosition);
		}

		@Override
		protected List<AbstractLazyTreeNode> createChildrenNodes() {
			AutoFieldValueUpdatingItemPosition anyChildItemPosition = getSubItemPosition(currentItemPosition);
			if (anyChildItemPosition == null) {
				return Collections.emptyList();
			}
			List<AbstractLazyTreeNode> result = new ArrayList<AbstractLazyTreeNode>();
			AutoFieldValueUpdatingList list = anyChildItemPosition.getContainingAutoUpdatingFieldList();
			for (int i = 0; i < list.size(); i++) {
				AutoFieldValueUpdatingItemPosition childItemPosition = anyChildItemPosition.getSibling(i);
				ItemNode node = new ItemNode(childItemPosition);
				result.add(node);
			}
			return result;
		}

	}

	public abstract class AutoFieldValueUpdatingItemPosition extends ItemPosition {

		public abstract String getContainingListTitle();

		public AutoFieldValueUpdatingItemPosition(AutoFieldValueUpdatingItemPosition parentItemPosition,
				IControlData containingListData, int index) {
			super(parentItemPosition, containingListData, index);
		}

		public AutoFieldValueUpdatingList getContainingAutoUpdatingFieldList() {
			return new AutoFieldValueUpdatingList(this);
		}

		@Override
		public AutoFieldValueUpdatingItemPosition getParentItemPosition() {
			return (AutoFieldValueUpdatingItemPosition) super.getParentItemPosition();
		}

		@Override
		public AutoFieldValueUpdatingItemPosition getSibling(int index2) {
			return (AutoFieldValueUpdatingItemPosition) super.getSibling(index2);
		}

	}

	protected class RefreshStructureModification implements IModification {
		protected List<AutoFieldValueUpdatingItemPosition> newSelection;

		public RefreshStructureModification(List<AutoFieldValueUpdatingItemPosition> newSelection) {
			this.newSelection = newSelection;
		}

		@Override
		public IInfo getTarget() {
			return getModifiedField();
		}

		@Override
		public IModification applyAndGetOpposite() {
			List<AutoFieldValueUpdatingItemPosition> oldSelection = getSelection();
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

	protected class AutoFieldValueUpdatingList extends AbstractList<Object> {
		protected AutoFieldValueUpdatingItemPosition itemPosition;

		public AutoFieldValueUpdatingList(AutoFieldValueUpdatingItemPosition itemPosition) {
			this.itemPosition = itemPosition;
		}

		public IControlData getListData() {
			return itemPosition.getContainingListData();
		}

		public IListTypeInfo getListType() {
			return (IListTypeInfo) getListData().getType();
		}

		protected Object[] getUnderlyingListValue() {
			Object listFieldValue = getListData().getValue();
			if (listFieldValue == null) {
				return new Object[0];
			}
			return getListType().toArray(listFieldValue);
		}

		protected void updateUnderlyingListValue(Object[] listRawValue) {
			ModificationStack modifStack = getParentFormModificationStack();
			UpdateListValueModification modif = new UpdateListValueModification(itemPosition, listRawValue,
					getModifiedField());
			modifStack.apply(modif);
		}

		@Override
		public Object get(int index) {
			return getUnderlyingListValue()[index];
		}

		@Override
		public int size() {
			Object[] underlyingListValue = getUnderlyingListValue();
			return underlyingListValue.length;
		}

		@Override
		public void add(int index, Object element) {
			List<Object> tmpList = new ArrayList<Object>(Arrays.asList(getUnderlyingListValue()));
			tmpList.add(index, element);
			updateUnderlyingListValue(tmpList.toArray());
		}

		@Override
		public Object remove(int index) {
			List<Object> tmpList = new ArrayList<Object>(Arrays.asList(getUnderlyingListValue()));
			Object result = tmpList.remove(index);
			updateUnderlyingListValue(tmpList.toArray());
			return result;
		}

		@Override
		public Object set(int index, Object element) {
			List<Object> tmpList = new ArrayList<Object>(Arrays.asList(getUnderlyingListValue()));
			Object result = tmpList.set(index, element);
			updateUnderlyingListValue(tmpList.toArray());
			return result;
		}

		public void move(int index, int offset) {
			List<Object> tmpList = new ArrayList<Object>(Arrays.asList(getUnderlyingListValue()));
			tmpList.add(index + offset, tmpList.remove(index));
			updateUnderlyingListValue(tmpList.toArray());
		}

		@Override
		public void clear() {
			updateUnderlyingListValue(new Object[0]);
		}

	}

	protected abstract class AbstractItemCellRenderer {

		protected void customizeCellRendererComponent(JLabel label, ItemNode node, int rowIndex, int columnIndex,
				boolean isSelected, boolean hasFocus) {
			label.putClientProperty("html.disable", Boolean.TRUE);
			if (!(node.getUserObject() instanceof AutoFieldValueUpdatingItemPosition)) {
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

		protected abstract boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder);

		protected abstract String getTitle();

		protected abstract String getCompositeModificationTitle();

		protected abstract boolean isValid();

		@Override
		public Object getValue(String key) {
			if (Action.NAME.equals(key)) {
				return swingRenderer.prepareStringToDisplay(getTitle());
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
			final List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder = new List[1];
			if (modifTitle == null) {
				List<AutoFieldValueUpdatingItemPosition> initialSelection = getSelection();
				if (perform(toPostSelectHolder)) {
					refreshStructure();
					if (toPostSelectHolder[0] != null) {
						setSelection(toPostSelectHolder[0]);
					} else {
						setSelection(initialSelection);
					}
				}
			} else {
				final ModificationStack modifStack = getParentFormModificationStack();
				try {
					modifStack.insideComposite(getModifiedField(), modifTitle, UndoOrder.FIFO, new Accessor<Boolean>() {
						@Override
						public Boolean get() {
							if (modifStack.insideComposite(getModifiedField(),
									modifTitle + " (without list control update)", UndoOrder.getDefault(),
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

	public interface IItemsVisitor {

		boolean visitItem(AutoFieldValueUpdatingItemPosition itemPosition);

	}

	protected enum InsertPosition {
		AFTER, BEFORE, UNKNOWN
	}
}
