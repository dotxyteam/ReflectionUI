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
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.InfoProxyGenerator;
import xy.reflect.ui.undo.CompositeModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractLazyTreeNode;

@SuppressWarnings("rawtypes")
public class ListControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;
	protected JXTreeTable treeTableComponent;
	protected ItemNode rootNode;
	protected JPanel toolbar;
	protected Map<ItemNode, Map<Integer, String>> valuesByNode = new HashMap<ItemNode, Map<Integer, String>>();
	protected IListStructuralInfo structuralInfo;
	protected static List<Object> clipboard = new ArrayList<Object>();
	protected static AbstractAction SEPARATOR_ACTION = new AbstractAction("") {
		protected static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};

	public ListControl(final SwingRenderer swingRenderer, final Object object, final IFieldInfo field) {
		this.swingRenderer = swingRenderer;
		this.object = object;
		this.field = field;

		setLayout(new BorderLayout());

		initializeTreeTableControl();
		add(new JScrollPane(treeTableComponent), BorderLayout.CENTER);

		refreshStructure();
		openDetailsDialogOnItemDoubleClick();
		updateToolbarOnItemSelection();
		setupContexteMenu();

		toolbar = new JPanel();
		add(toolbar, BorderLayout.EAST);
		updateToolbar();

	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = Toolkit.getDefaultToolkit().getScreenSize();
		result.width = result.width / 2;
		result.height = result.height / 3;
		Dimension toolbarSize = toolbar.getPreferredSize();
		if (toolbarSize != null) {
			result.height = Math.max(result.height, toolbarSize.height);
			Border border = getBorder();
			if (border != null) {
				Insets borderInsets = border.getBorderInsets(this);
				if (borderInsets != null) {
					result.height += borderInsets.bottom + borderInsets.top;
				}
			}
		}
		return result;
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	protected void updateToolbar() {
		toolbar.removeAll();

		GridBagLayout layout = new GridBagLayout();
		toolbar.setLayout(layout);

		toolbar.add(createTool(null, SwingRendererUtils.DETAILS_ICON, true, false, createOpenItemAction()));

		if (!getRootListItemPosition().isContainingListReadOnly()) {
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
		for (AbstractListAction listAction : getRootListType().getSpecificActions(object, field, getSelection())) {
			toolbar.add(createTool(listAction.getCaption(), null, true, false, createSpecificActionHook(listAction)));
		}

		for (int i = 0; i < toolbar.getComponentCount(); i++) {
			Component c = toolbar.getComponent(i);
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(0, 5, 2, 5);
			layout.setConstraints(c, constraints);
		}
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.weighty = 1;
		toolbar.add(new JSeparator(), constraints);

		swingRenderer.handleComponentSizeChange(ListControl.this);
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
				if (e.isPopupTrigger() && e.getComponent() == treeTableComponent) {
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
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	public IListStructuralInfo getStructuralInfo() {
		if (structuralInfo == null) {
			structuralInfo = getRootListType().getStructuralInfo();
		}
		return structuralInfo;
	}

	protected void initializeTreeTableControl() {
		rootNode = createRootNode();
		treeTableComponent = new JXTreeTable(createTreeTableModel());
		treeTableComponent.setExpandsSelectedPaths(true);
		treeTableComponent.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		treeTableComponent.setRootVisible(false);
		treeTableComponent.setShowsRootHandles(true);
		treeTableComponent.setDefaultRenderer(Object.class, new ItemCellRenderer());
		treeTableComponent.setTreeCellRenderer(new ItemCellRenderer());
		treeTableComponent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		treeTableComponent.setHorizontalScrollEnabled(true);
		treeTableComponent.setColumnMargin(5);
		treeTableComponent.getTableHeader().setReorderingAllowed(false);
		fixCustomRenderingNotAppliedOnUnselectedCells();
	}

	protected void fixCustomRenderingNotAppliedOnUnselectedCells() {
		treeTableComponent.setHighlighters();
		treeTableComponent.putClientProperty(JXTable.USE_DTCR_COLORMEMORY_HACK, false);
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
		return new ItemNode(field, null);
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(field.getCaption()));
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
			IListStructuralInfo tableInfo = getStructuralInfo();
			if (tableInfo == null) {
				value = swingRenderer.getReflectionUI().toString(itemPosition.getItem());
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
			nodeValues.put(columnIndex, value);
		}
		return value;
	}

	protected Image getCellIconImage(ItemNode node, int columnIndex) {
		AutoFieldValueUpdatingItemPosition itemPosition = (AutoFieldValueUpdatingItemPosition) node.getUserObject();
		if (columnIndex == 0) {
			return swingRenderer.getObjectIconImage(itemPosition.getItem());
		}
		return null;
	}

	protected List<AbstractAction> createCurrentSelectionActions() {

		List<AbstractAction> result = new ArrayList<AbstractAction>();

		List<AutoFieldValueUpdatingItemPosition> selection = getSelection();

		for (AbstractListAction listAction : getRootListType().getSpecificActions(object, field, selection)) {
			result.add(createSpecificActionHook(listAction));
		}

		result.add(SEPARATOR_ACTION);

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

	protected boolean anySelectionItemContainingListReadOnly() {
		boolean result = false;
		List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
		for (AutoFieldValueUpdatingItemPosition selectionItem : selection) {
			if (selectionItem.isContainingListReadOnly()) {
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
			if (!swingRenderer.getReflectionUI().canCopy(selectionItem.getItem())) {
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
		if (itemPosition.getItem() != null) {
			IListStructuralInfo treeInfo = getStructuralInfo();
			if (treeInfo != null) {
				IFieldInfo subListField = treeInfo.getItemSubListField(itemPosition);
				if (subListField != null) {
					return new AutoFieldValueUpdatingItemPosition(subListField, itemPosition, -1);
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
				refreshStructure();
				toPostSelectHolder[0] = Collections.emptyList();
				return true;
			}

			@Override
			protected String getTitle() {
				return "Remove All";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Clear '" + field.getCaption() + "'";
			}

			@Override
			public boolean isValid() {
				if (getRootList().size() > 0) {
					if (!getRootListItemPosition().isContainingListReadOnly()) {
						if (getRootListType().canRemove()) {
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
				return "Move '" + field.getCaption() + "' item(s)";
			}

			@Override
			public boolean isValid() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() > 0) {
					if (!anySelectionItemContainingListReadOnly()) {
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

	public AutoFieldValueUpdatingItemPosition findItemPosition(final Object item) {
		final AutoFieldValueUpdatingItemPosition[] result = new AutoFieldValueUpdatingItemPosition[1];
		visitItems(new IItemsVisitor() {
			@Override
			public boolean visitItem(AutoFieldValueUpdatingItemPosition itemPosition) {
				if (swingRenderer.getReflectionUI().equals(itemPosition.getItem(), item)) {
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
		updateToolbar();
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
						updateOnItemRemoval(toPostSelect, itemPosition);
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
				return "Remove '" + field.getCaption() + "' item(s)";
			}

			@Override
			public boolean isValid() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() == 0) {
					return false;
				}
				if (selection.size() > 0) {
					for (AutoFieldValueUpdatingItemPosition selectionItem : selection) {
						if (selectionItem.isContainingListReadOnly()) {
							return false;
						}
						if (!selectionItem.getContainingListType().canRemove()) {
							return false;
						}
					}
				}
				return true;
			}
		};
	}

	protected void updateOnItemRemoval(List<AutoFieldValueUpdatingItemPosition> toUpdate,
			AutoFieldValueUpdatingItemPosition itemPosition) {
		for (int i = 0; i < toUpdate.size(); i++) {
			AutoFieldValueUpdatingItemPosition toUpdateItem = toUpdate.get(i);
			if (toUpdateItem.equals(itemPosition) || toUpdateItem.getAncestors().contains(itemPosition)) {
				toUpdate.remove(i);
				i--;
			} else if (toUpdateItem.getPreviousSiblings().contains(itemPosition)) {
				toUpdate.set(i, new AutoFieldValueUpdatingItemPosition(toUpdateItem.getContainingListField(),
						toUpdateItem.getParentItemPosition(), toUpdateItem.getIndex() - 1));
			}
		}
	}

	protected class GhostItemPosition extends AutoFieldValueUpdatingItemPosition {

		protected Object item;

		public GhostItemPosition(AutoFieldValueUpdatingItemPosition itemPosition, Object item) {
			super(itemPosition.getContainingListField(), itemPosition.getParentItemPosition(), itemPosition.getIndex());
			this.item = item;
		}

		@Override
		public Object getItem() {
			return item;
		}

		@Override
		public AutoFieldValueUpdatingList getContainingAutoUpdatingFieldList() {
			return new AutoFieldValueUpdatingList(this) {

				@Override
				public Object get(int index) {
					if (index == itemPosition.getIndex()) {
						return item;
					} else {
						return super.get(index);
					}
				}

				@Override
				public Object set(int index, Object element) {
					if (index == itemPosition.getIndex()) {
						Object old = item;
						item = element;
						return old;
					} else {
						return super.set(index, element);
					}
				}

			};
		}

	}

	protected AbstractStandardListAction createInsertAction(final InsertPosition insertPosition) {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
				IListTypeInfo listType = newItemPosition.getContainingListType();
				ITypeInfo typeToInstanciate = getTypeToInstanciate();
				Object newItem = swingRenderer.onTypeInstanciationRequest(ListControl.this, typeToInstanciate, false);
				if (newItem == null) {
					return false;
				}
				GhostItemPosition futureItemPosition = new GhostItemPosition(newItemPosition, newItem);
				if (openDetailsDialog(futureItemPosition, new boolean[1], null)) {
					newItem = futureItemPosition.getItem();
					AutoFieldValueUpdatingList list = newItemPosition.getContainingAutoUpdatingFieldList();
					list.add(newItemPosition.getIndex(), newItem);
					refreshStructure();
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

			private ITypeInfo getTypeToInstanciate() {
				AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
				if (newItemPosition == null) {
					return null;
				}
				IListTypeInfo listType = newItemPosition.getContainingListType();
				ITypeInfo itemType = listType.getItemType();
				ITypeInfo result = itemType;
				if (result == null) {
					result = swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class));
				}
				result = addSpecificItemContructors(result, newItemPosition);
				return result;
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
				return "Insert item into '" + field.getCaption() + "'";
			}

			@Override
			public boolean isValid() {
				AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
				if (newItemPosition != null) {
					if (newItemPosition.getContainingListType().canAdd()) {
						if (!newItemPosition.isContainingListReadOnly()) {
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
				AutoFieldValueUpdatingItemPosition itemPosition = getSingleSelection();
				AutoFieldValueUpdatingItemPosition subItemPosition = getSubItemPosition();
				IListTypeInfo subListType = subItemPosition.getContainingListType();
				ITypeInfo typeToInstanciate = getTypeToInstanciate();
				Object newSubListItem = swingRenderer.onTypeInstanciationRequest(ListControl.this, typeToInstanciate,
						false);
				if (newSubListItem == null) {
					return false;
				}
				GhostItemPosition futureSubItemPosition = new GhostItemPosition(subItemPosition, newSubListItem);
				if (openDetailsDialog(futureSubItemPosition, new boolean[1], null)) {
					newSubListItem = futureSubItemPosition.getItem();
					AutoFieldValueUpdatingList subList = new AutoFieldValueUpdatingItemPosition(
							subItemPosition.getContainingListField(), itemPosition, -1)
									.getContainingAutoUpdatingFieldList();
					int newSubListItemIndex = subList.size();
					subList.add(newSubListItemIndex, newSubListItem);
					refreshStructure();
					if (!subListType.isOrdered()) {
						newSubListItemIndex = subList.indexOf(newSubListItem);
					}
					AutoFieldValueUpdatingItemPosition toSelect = subItemPosition.getSibling(newSubListItemIndex);
					toPostSelectHolder[0] = Collections.singletonList(toSelect);
					return true;
				} else {
					return false;
				}
			}

			private ITypeInfo getTypeToInstanciate() {
				AutoFieldValueUpdatingItemPosition subItemPosition = getSubItemPosition();
				IListTypeInfo subListType = subItemPosition.getContainingListType();
				ITypeInfo subListItemType = subListType.getItemType();
				ITypeInfo result = subListItemType;
				if (result == null) {
					result = new DefaultTypeInfo(swingRenderer.getReflectionUI(), Object.class);
				}
				result = addSpecificItemContructors(result, subItemPosition);
				return result;
			}

			@Override
			public boolean isValid() {
				AutoFieldValueUpdatingItemPosition subItemPosition = getSubItemPosition();
				if (subItemPosition == null) {
					return false;
				}
				if (subItemPosition.isContainingListReadOnly()) {
					return false;
				}
				if (!subItemPosition.getContainingListType().canAdd()) {
					return false;
				}
				return true;
			}

			private AutoFieldValueUpdatingItemPosition getSubItemPosition() {
				AutoFieldValueUpdatingItemPosition itemPosition = getSingleSelection();
				AutoFieldValueUpdatingItemPosition result;
				if (itemPosition == null) {
					result = getRootListItemPosition();
				} else {
					result = ListControl.this.getSubItemPosition(itemPosition);
				}
				return result;
			}

			@Override
			protected String getTitle() {
				AutoFieldValueUpdatingItemPosition subItemPosition = getSubItemPosition();
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
				return "Add item into '" + field.getCaption() + "'";
			}

		};
	}

	protected ITypeInfo addSpecificItemContructors(ITypeInfo itemType,
			final AutoFieldValueUpdatingItemPosition newItemPosition) {
		return new InfoProxyGenerator() {

			@Override
			protected List<IMethodInfo> getConstructors(ITypeInfo type) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(type));
				IListTypeInfo containingListType = newItemPosition.getContainingListType();
				Object containingListOwner = newItemPosition.getContainingListOwner();
				if (containingListOwner != null) {
					IFieldInfo containingListField = newItemPosition.getContainingListField();
					List<IMethodInfo> specificItemConstructors = containingListType
							.getObjectSpecificItemConstructors(containingListOwner, containingListField);
					if (specificItemConstructors != null) {
						result.addAll(specificItemConstructors);
					}
				}
				return result;
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
					clipboard.add(swingRenderer.getReflectionUI().copy(itemPosition.getItem()));
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
					clipboard.add(0, swingRenderer.getReflectionUI().copy(itemPosition.getItem()));
					AutoFieldValueUpdatingList list = itemPosition.getContainingAutoUpdatingFieldList();
					int index = itemPosition.getIndex();
					list.remove(index);
					updateOnItemRemoval(toPostSelect, itemPosition);
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
				return "Cut '" + field.getCaption() + "' item(s)";
			}

			@Override
			public boolean isValid() {
				List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
				if (selection.size() > 0) {
					if (canCopyAllSelection()) {
						if (!anySelectionItemContainingListReadOnly()) {
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
				for (Object clipboardItyem : clipboard) {
					list.add(index, clipboardItyem);
					index++;
				}
				refreshStructure();
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
				return "Paste item(s) into '" + field.getCaption() + "'";
			}

			@Override
			public boolean isValid() {
				if (clipboard.size() > 0) {
					AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
					if (newItemPosition != null) {
						if (!newItemPosition.isContainingListReadOnly()) {
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
				refreshStructure();
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
				return "Paste item(s) into '" + field.getCaption() + "'";
			}

			@Override
			protected boolean isValid() {
				if (clipboard.size() > 0) {
					AutoFieldValueUpdatingItemPosition newItemPosition = getNewItemPosition();
					if (newItemPosition != null) {
						if (!newItemPosition.isContainingListReadOnly()) {
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
		return (IListTypeInfo) field.getType();
	}

	protected AbstractAction createSpecificActionHook(final AbstractListAction listAction) {
		return new AbstractAction(swingRenderer.prepareStringToDisplay(listAction.getCaption())) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				listAction.setListControl(ListControl.this);
				final JPanel form = SwingRendererUtils.findForm(ListControl.this, swingRenderer);
				IMethodInfo method = new MethodInfoProxy(listAction) {
					@Override
					public Object invoke(Object object, InvocationData invocationData) {
						return SwingRendererUtils.invokeMethodAndAllowToUndo(object, listAction, invocationData, form,
								swingRenderer);
					}
				};
				swingRenderer.onMethodInvocationRequest(ListControl.this, ListControl.this.object, method, null);
			}

			@Override
			public boolean isEnabled() {
				return listAction.isEnabled();
			}

		};
	}

	protected AbstractStandardListAction createOpenItemAction() {
		return new AbstractStandardListAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				AutoFieldValueUpdatingItemPosition itemPosition = getSingleSelection();
				return onOpenDetaildsDialogRequest(itemPosition);
			}

			@Override
			protected String getTitle() {
				return "Open";
			}

			@Override
			protected String getCompositeModificationTitle() {
				return null;
			}

			@Override
			public boolean isValid() {
				AutoFieldValueUpdatingItemPosition singleSelectedPosition = getSingleSelection();
				if (singleSelectedPosition != null) {
					if (hasItemDetails(singleSelectedPosition)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	protected void updateToolbarOnItemSelection() {
		treeTableComponent.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				try {
					updateToolbar();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(treeTableComponent, t);
				}
			}
		});
	}

	protected void openDetailsDialogOnItemDoubleClick() {
		treeTableComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				try {
					if (me.getClickCount() != 2) {
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

	protected boolean onOpenDetaildsDialogRequest(final AutoFieldValueUpdatingItemPosition itemPosition) {
		Object value = itemPosition.getItem();
		final GhostItemPosition ghostItemPosition = new GhostItemPosition(itemPosition, value);
		final ModificationStack ghostParentModifStack = new ModificationStack(null);
		boolean[] changeDetectedHolder = new boolean[] { false };
		openDetailsDialog(ghostItemPosition, changeDetectedHolder, ghostParentModifStack);
		if (!changeDetectedHolder[0]) {
			return false;
		}
		if (itemPosition.isContainingListReadOnly()) {
			return false;
		}
		new AbstractStandardListAction() {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean perform(List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder) {
				ModificationStack parentModifStack = getParentFormModificationStack();
				parentModifStack.pushUndo(new CompositeModification(null, UndoOrder.getDefault(),
						ghostParentModifStack.getUndoModifications(UndoOrder.getDefault())));
				final AutoFieldValueUpdatingList list = itemPosition.getContainingAutoUpdatingFieldList();
				final Object newValue = ghostItemPosition.getItem();
				final int index = itemPosition.getIndex();
				list.set(index, newValue);
				return false;
			}

			@Override
			protected boolean isValid() {
				return true;
			}

			@Override
			protected String getTitle() {
				return null;
			}

			@Override
			protected String getCompositeModificationTitle() {
				return "Edit '" + field.getCaption() + "' item";
			}
		}.actionPerformed(null);
		return true;
	}

	protected AutoFieldValueUpdatingList getRootList() {
		return getRootListItemPosition().getContainingAutoUpdatingFieldList();
	}

	protected AutoFieldValueUpdatingItemPosition getRootListItemPosition() {
		return new AutoFieldValueUpdatingItemPosition(field, null, -1);
	}

	protected boolean openDetailsDialog(final AutoFieldValueUpdatingItemPosition itemPosition,
			boolean[] changeDetectedHolder, ModificationStack parentStack) {
		ItemNode itemNode = findNode(itemPosition);
		if (itemNode != null) {
			TreePath treePath = new TreePath(itemNode.getPath());
			treeTableComponent.expandPath(treePath);
		}

		if (!hasItemDetails(itemPosition)) {
			return true;
		}

		final AutoFieldValueUpdatingList list = itemPosition.getContainingAutoUpdatingFieldList();
		final int index = itemPosition.getIndex();
		final Object[] valueHolder = new Object[] { list.get(index) };
		final Accessor<Object> valueAccessor = new Accessor<Object>() {

			@Override
			public Object get() {
				return valueHolder[0];
			}

			@Override
			public void set(Object value) {
				if (!itemPosition.isContainingListReadOnly()) {
					valueHolder[0] = value;
					list.set(index, value);
				}
			}

		};
		String title = swingRenderer.getObjectTitle(valueHolder[0]);
		Image iconImage = swingRenderer.getObjectIconImage(valueHolder[0]);
		IInfoCollectionSettings settings = getStructuralInfo().getItemInfoSettings(itemPosition);
		boolean isGetOnly = itemPosition.getContainingListField().isGetOnly();
		boolean[] okPressedHolder = isGetOnly ? null : new boolean[1];
		boolean cancellable = true;
		swingRenderer.openObjectDialog(treeTableComponent, valueAccessor, isGetOnly, title, iconImage, true, settings,
				parentStack, cancellable, okPressedHolder, changeDetectedHolder, null);
		return isGetOnly ? false : okPressedHolder[0];
	}

	protected ModificationStack getParentFormModificationStack() {
		return SwingRendererUtils.findModificationStack(ListControl.this, swingRenderer);
	}

	protected boolean hasItemDetails(AutoFieldValueUpdatingItemPosition itemPosition) {
		Object item = itemPosition.getItem();
		if (item == null) {
			return false;
		}
		ITypeInfo actualItemType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(item));
		if (swingRenderer.hasCustomFieldControl(item)) {
			return true;
		}
		List<IFieldInfo> fields = actualItemType.getFields();
		List<IMethodInfo> methods = actualItemType.getMethods();

		IListStructuralInfo structuralInfo = getStructuralInfo();
		if (structuralInfo != null) {
			IInfoCollectionSettings infoSettings = structuralInfo.getItemInfoSettings(itemPosition);

			fields = new ArrayList<IFieldInfo>(fields);
			for (Iterator<IFieldInfo> it = fields.iterator(); it.hasNext();) {
				IFieldInfo field = it.next();
				if (infoSettings.excludeField(field)) {
					it.remove();
				}
			}

			methods = new ArrayList<IMethodInfo>(methods);
			for (Iterator<IMethodInfo> it = methods.iterator(); it.hasNext();) {
				IMethodInfo method = it.next();
				if (infoSettings.excludeMethod(method)) {
					it.remove();
				}
			}

		}
		return (fields.size() + methods.size()) > 0;
	}

	protected void refreshStructure() {
		valuesByNode.clear();
		rootNode = createRootNode();
		treeTableComponent.setTreeTableModel(createTreeTableModel());
		TableColumnModel columnModel = treeTableComponent.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			TableColumn col = columnModel.getColumn(i);
			col.setPreferredWidth(
					col.getPreferredWidth() + 5 * SwingRendererUtils.getStandardCharacterWidth(treeTableComponent));
		}
	}

	public void visitItems(IItemsVisitor iItemsVisitor) {
		visitItems(iItemsVisitor, getRootListItemPosition());
	}

	protected void visitItems(IItemsVisitor iItemsVisitor, AutoFieldValueUpdatingItemPosition currentListItemPosition) {
		AutoFieldValueUpdatingList list = currentListItemPosition.getContainingAutoUpdatingFieldList();
		for (int i = 0; i < list.size(); i++) {
			currentListItemPosition = currentListItemPosition.getSibling(i);
			if (!iItemsVisitor.visitItem(currentListItemPosition)) {
				return;
			}
			IListStructuralInfo treeInfo = getStructuralInfo();
			if (treeInfo != null) {
				IFieldInfo childrenField = treeInfo.getItemSubListField(currentListItemPosition);
				if (childrenField != null) {
					if (childrenField.getValue(currentListItemPosition.getItem()) != null) {
						visitItems(iItemsVisitor,
								new AutoFieldValueUpdatingItemPosition(childrenField, currentListItemPosition, -1));
					}
				}

			}
		}
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

	protected void restoringSelectionAsMuchAsPossible(Runnable runnable) {
		List<AutoFieldValueUpdatingItemPosition> lastlySelectedItemPositions = getSelection();
		List<Object> lastlySelectedItems = new ArrayList<Object>();
		for (int i = 0; i < lastlySelectedItemPositions.size(); i++) {
			try {
				AutoFieldValueUpdatingItemPosition lastlySelectedItemPosition = lastlySelectedItemPositions.get(i);
				lastlySelectedItems.add(lastlySelectedItemPosition.getItem());
			} catch (Throwable t) {
				lastlySelectedItems.add(null);
			}
		}

		runnable.run();

		int i = 0;
		for (Iterator<AutoFieldValueUpdatingItemPosition> it = lastlySelectedItemPositions.iterator(); it.hasNext();) {
			AutoFieldValueUpdatingItemPosition lastlySelectedItemPosition = it.next();
			try {
				if (!lastlySelectedItemPosition.getContainingListType().isOrdered()) {
					Object lastlySelectedItem = lastlySelectedItems.get(i);
					int index = lastlySelectedItemPosition.getContainingAutoUpdatingFieldList()
							.indexOf(lastlySelectedItem);
					lastlySelectedItemPosition = lastlySelectedItemPosition.getSibling(index);
					lastlySelectedItemPositions.set(i, lastlySelectedItemPosition);
				}
			} catch (Throwable t) {
				it.remove();
			}
			i++;
		}
		try {
			setSelection(lastlySelectedItemPositions);
		} catch (Throwable ignore) {
		}
	}

	protected class ItemNode extends AbstractLazyTreeNode {

		protected static final long serialVersionUID = 1L;
		protected IFieldInfo childrenField;
		protected AutoFieldValueUpdatingItemPosition currentItemPosition;
		protected boolean childrenLoaded = false;;

		public ItemNode(IFieldInfo childrenField, AutoFieldValueUpdatingItemPosition currentItemPosition) {
			this.childrenField = childrenField;
			this.currentItemPosition = currentItemPosition;
			setUserObject(currentItemPosition);
		}

		@Override
		protected List<AbstractLazyTreeNode> createChildrenNodes() {
			if (childrenField == null) {
				return Collections.emptyList();
			}
			List<AbstractLazyTreeNode> result = new ArrayList<AbstractLazyTreeNode>();
			AutoFieldValueUpdatingItemPosition anyChildItemPosition = new AutoFieldValueUpdatingItemPosition(
					childrenField, currentItemPosition, -1);
			AutoFieldValueUpdatingList list = anyChildItemPosition.getContainingAutoUpdatingFieldList();
			for (int i = 0; i < list.size(); i++) {
				AutoFieldValueUpdatingItemPosition childItemPosition = anyChildItemPosition.getSibling(i);
				IFieldInfo grandChildrenField = null;
				IListStructuralInfo treeInfo = getStructuralInfo();
				if (treeInfo != null) {
					grandChildrenField = treeInfo.getItemSubListField(childItemPosition);
					if (grandChildrenField != null) {
						if (grandChildrenField.getValue(childItemPosition.getItem()) == null) {
							grandChildrenField = null;
						}
					}
				}
				ItemNode node = new ItemNode(grandChildrenField, childItemPosition);
				result.add(node);
			}
			return result;
		}

	}

	public class AutoFieldValueUpdatingItemPosition extends ItemPosition {

		public AutoFieldValueUpdatingItemPosition(IFieldInfo containingListField,
				AutoFieldValueUpdatingItemPosition parentItemPosition, int index) {
			super(containingListField, parentItemPosition, index, object);
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
			ItemPosition result = super.getSibling(index2);
			return new AutoFieldValueUpdatingItemPosition(result.getContainingListField(), getParentItemPosition(),
					result.getIndex());
		}

		@Override
		public AutoFieldValueUpdatingItemPosition getRootListItemPosition() {
			ItemPosition result = super.getRootListItemPosition();
			return new AutoFieldValueUpdatingItemPosition(result.getContainingListField(), null, result.getIndex());
		}

	}

	protected class ReplaceListValueContentModification implements IModification {
		protected Object[] listRawValue;
		protected Object listOwner;
		protected IFieldInfo listField;
		protected ReflectionUI reflectionUI;
		private boolean discardFieldValueSetting;
		private Object listValue;

		public ReplaceListValueContentModification(ReflectionUI reflectionUI, Object[] listRawValue, Object listOwner,
				IFieldInfo listField, boolean discardFieldValueSetting) {
			this.reflectionUI = reflectionUI;
			this.listRawValue = listRawValue;
			this.listOwner = listOwner;
			this.listField = listField;
			this.discardFieldValueSetting = discardFieldValueSetting;
		}

		@Override
		public int getNumberOfUnits() {
			return 1;
		}

		@Override
		public IModification applyAndGetOpposite() {
			IListTypeInfo listType = (IListTypeInfo) listField.getType();
			listValue = listField.getValue(listOwner);
			Object[] lastListRawValue = listType.toArray(listValue);

			listType.replaceContent(listValue, listRawValue);
			if (!discardFieldValueSetting) {
				if (!listField.isGetOnly()) {
					listField.setValue(listOwner, listValue);
				}
			}

			final ReplaceListValueContentModification currentModif = this;
			return new ReplaceListValueContentModification(reflectionUI, lastListRawValue, listOwner, listField,
					discardFieldValueSetting) {
				@Override
				public String getTitle() {
					return ModificationStack.getUndoTitle(currentModif.getTitle());
				}
			};
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "Edit '" + listField.getCaption() + "' of '" + swingRenderer.getObjectTitle(listOwner) + "'";
		}

		public Object getLastListValue() {
			return listValue;
		}

	}

	protected class SetListValueModification implements IModification {
		protected Object[] listRawValue;
		protected Object listOwner;
		protected IFieldInfo listField;
		protected ReflectionUI reflectionUI;

		public SetListValueModification(ReflectionUI reflectionUI, Object[] listRawValue, Object listOwner,
				IFieldInfo listField) {
			this.reflectionUI = reflectionUI;
			this.listRawValue = listRawValue;
			this.listOwner = listOwner;
			this.listField = listField;
		}

		@Override
		public int getNumberOfUnits() {
			return 1;
		}

		@Override
		public IModification applyAndGetOpposite() {
			IListTypeInfo listType = (IListTypeInfo) listField.getType();
			Object listValue = listField.getValue(listOwner);
			Object[] lastListRawValue = listType.toArray(listValue);

			listValue = listType.fromArray(listRawValue);
			listField.setValue(listOwner, listValue);

			final SetListValueModification currentModif = this;
			return new SetListValueModification(reflectionUI, lastListRawValue, listOwner, listField) {
				@Override
				public String getTitle() {
					return ModificationStack.getUndoTitle(currentModif.getTitle());
				}
			};
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "Edit '" + listField.getCaption() + "' of '" + swingRenderer.getObjectTitle(listOwner) + "'";
		}

	}

	protected class ChangeListSelectionModification implements IModification {
		protected List<AutoFieldValueUpdatingItemPosition> toSelect;
		protected List<AutoFieldValueUpdatingItemPosition> undoSelection;

		public ChangeListSelectionModification(List<AutoFieldValueUpdatingItemPosition> toSelect,
				List<AutoFieldValueUpdatingItemPosition> undoSelection) {
			this.toSelect = toSelect;
			this.undoSelection = undoSelection;
		}

		@Override
		public IModification applyAndGetOpposite() {
			List<AutoFieldValueUpdatingItemPosition> oppositeSelection;
			if (undoSelection != null) {
				oppositeSelection = undoSelection;
			} else {
				oppositeSelection = getSelection();
			}

			if (toSelect != null) {
				setSelection(toSelect);
			}

			return new ChangeListSelectionModification(oppositeSelection, toSelect);
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "Select " + toSelect;
		}

		@Override
		public int getNumberOfUnits() {
			return 1;
		}

	}

	protected class AutoFieldValueUpdatingList extends AbstractList {
		protected AutoFieldValueUpdatingItemPosition itemPosition;

		public AutoFieldValueUpdatingList(AutoFieldValueUpdatingItemPosition itemPosition) {
			this.itemPosition = itemPosition;
		}

		public IFieldInfo getListField() {
			return itemPosition.getContainingListField();
		}

		public Object getListOwner() {
			return itemPosition.getContainingListOwner();
		}

		public IListTypeInfo getListType() {
			return (IListTypeInfo) getListField().getType();
		}

		protected Object[] getUnderlyingListValue() {
			Object listFieldValue = getListField().getValue(getListOwner());
			if (listFieldValue == null) {
				return new Object[0];
			}
			return getListType().toArray(listFieldValue);
		}

		protected void replaceUnderlyingListValue(Object[] listRawValue) {
			ModificationStack modifStack = getParentFormModificationStack();
			Object listOwner = getListOwner();
			if (itemPosition.getContainingListType().canReplaceContent()) {
				ReplaceListValueContentModification modif = new ReplaceListValueContentModification(
						swingRenderer.getReflectionUI(), listRawValue, listOwner, getListField(), false);
				modifStack.apply(modif);
			} else {
				SetListValueModification modif = new SetListValueModification(swingRenderer.getReflectionUI(),
						listRawValue, listOwner, getListField());
				modifStack.apply(modif);
			}

			if (!itemPosition.isRootListItemPosition()) {
				AutoFieldValueUpdatingItemPosition listOwnerPosition = itemPosition.getParentItemPosition();
				new AutoFieldValueUpdatingList(listOwnerPosition).set(listOwnerPosition.getIndex(), listOwner);
			}
		}

		protected void beginModification() {
			ModificationStack modifStack = getParentFormModificationStack();
			modifStack.beginComposite();
		}

		protected void endModification() {
			ModificationStack modifStack = getParentFormModificationStack();
			modifStack.endComposite(null, UndoOrder.FIFO);
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

		@SuppressWarnings("unchecked")
		@Override
		public void add(int index, Object element) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			tmpList.add(index, element);
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object remove(int index) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			Object result = tmpList.remove(index);
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification();
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object set(int index, Object element) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			Object result = tmpList.set(index, element);
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification();
			return result;
		}

		@SuppressWarnings("unchecked")
		public void move(int index, int offset) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			tmpList.add(index + offset, tmpList.remove(index));
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification();
		}

		@Override
		public void clear() {
			beginModification();
			replaceUnderlyingListValue(new Object[0]);
			endModification();
		}

	}

	protected class ItemCellRenderer implements TableCellRenderer, TreeCellRenderer {

		DefaultTreeCellRenderer defaultTreeCellRenderer = new DefaultTreeCellRenderer();
		DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean isLeaf, int row, boolean focused) {
			JLabel label = (JLabel) defaultTreeCellRenderer.getTreeCellRendererComponent(tree, value, selected,
					expanded, isLeaf, row, focused);
			if (!selected) {
				label.setOpaque(false);
				label.setBackground(null);
			}
			customizeComponent(label, (ItemNode) value, row, 0, selected, focused);
			return label;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			JLabel label = (JLabel) defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			row = treeTableComponent.convertRowIndexToModel(row);
			TreePath path = treeTableComponent.getPathForRow(row);
			if (path != null) {
				ItemNode node = (ItemNode) path.getLastPathComponent();
				customizeComponent(label, node, row, column, isSelected, hasFocus);
			}
			return label;
		}

		protected void customizeComponent(JLabel label, ItemNode node, int rowIndex, int columnIndex,
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
			String modifTitle = getCompositeModificationTitle();
			if (modifTitle == null) {
				List<AutoFieldValueUpdatingItemPosition> initialSelection = getSelection();
				@SuppressWarnings("unchecked")
				List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder = new List[1];
				if (perform(toPostSelectHolder)) {
					refreshStructure();
					if (toPostSelectHolder[0] != null) {
						setSelection(toPostSelectHolder[0]);
					} else {
						setSelection(initialSelection);
					}
				}
			} else {
				ModificationStack modifStack = getParentFormModificationStack();
				modifStack.beginComposite();
				try {
					List<AutoFieldValueUpdatingItemPosition> selection = getSelection();
					modifStack.pushUndo(new ChangeListSelectionModification(selection, getSelection()));
					@SuppressWarnings("unchecked")
					List<AutoFieldValueUpdatingItemPosition>[] toPostSelectHolder = new List[1];
					if (perform(toPostSelectHolder)) {
						refreshStructure();
						if (toPostSelectHolder[0] != null) {
							modifStack
									.apply(new ChangeListSelectionModification(toPostSelectHolder[0], getSelection()));
						}
						modifStack.endComposite(modifTitle, UndoOrder.LIFO);
					} else {
						modifStack.cancelComposite();
					}
				} catch (Throwable t) {
					modifStack.cancelComposite();
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
