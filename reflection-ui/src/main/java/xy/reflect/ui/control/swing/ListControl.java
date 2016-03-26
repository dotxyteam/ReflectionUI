package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
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
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.IListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SetListValueModification;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractLazyTreeNode;
import xy.reflect.ui.util.component.WrapLayout;

@SuppressWarnings("rawtypes")
public class ListControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
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

	public static boolean isCompatibleWith(ReflectionUI reflectionUI, Object fieldValue) {
		final ITypeInfo fieldValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(fieldValue));
		return fieldValueType instanceof IListTypeInfo;
	}

	public ListControl(final ReflectionUI reflectionUI, final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;

		setLayout(new BorderLayout());

		initializeTreeTableControl();
		add(new JScrollPane(treeTableComponent), BorderLayout.CENTER);

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.width = size.width / 2;
		size.height = size.height / 3;
		setPreferredSize(size);
		setMinimumSize(size);

		refreshStructure();
		openDetailsDialogOnItemDoubleClick();
		updateToolbarOnItemSelection();
		setupContexteMenu();

		toolbar = new JPanel();
		toolbar.setLayout(new WrapLayout(WrapLayout.LEFT));
		add(toolbar, BorderLayout.NORTH);
		updateToolbar();

	}

	protected void updateToolbar() {
		toolbar.removeAll();
		if (!getRootListItemPosition().isContainingListReadOnly()) {

			AbstractAction addChildAction = createAddChildAction();
			AbstractAction insertAction = createInsertAction(InsertPosition.UNKNOWN);
			AbstractAction insertActionBefore = createInsertAction(InsertPosition.BEFORE);
			AbstractAction insertActionAfter = createInsertAction(InsertPosition.AFTER);

			toolbar.add(createTool(null, SwingRendererUtils.DETAILS_ICON, false, false, createOpenItemAction()));

			toolbar.add(createTool(null, SwingRendererUtils.ADD_ICON, true, false, addChildAction, insertAction,
					insertActionBefore, insertActionAfter));

			toolbar.add(createTool(null, SwingRendererUtils.REMOVE_ICON, true, false, createRemoveAction()));

			toolbar.add(createTool(null, SwingRendererUtils.UP_ICON, false, false, createMoveAction(-1)));

			toolbar.add(createTool(null, SwingRendererUtils.DOWN_ICON, false, false, createMoveAction(1)));

			for (IListAction listAction : getRootListType().getSpecificActions(object, field, getSelection())) {
				toolbar.add(createTool(listAction.getTitle(), null, true, false, createSpecificAction(listAction)));
			}

		}
		reflectionUI.getSwingRenderer().handleComponentSizeChange(ListControl.this);
		toolbar.repaint();
	}

	protected JButton createTool(String text, Icon icon, boolean alwawsShowIcon, final boolean alwawsShowMenu,
			AbstractAction... actions) {
		final JButton result = new JButton(text, icon);
		result.setFocusable(false);
		List<AbstractAction> allActiveActions = createCurrentSelectionActions();
		final List<AbstractAction> actionsToPresent = new ArrayList<AbstractAction>();
		for (final AbstractAction action : actions) {
			if (action == null) {
				continue;
			}
			String actionTitle = (String) action.getValue(Action.NAME);
			if (findActionByName(allActiveActions, actionTitle)) {
				actionsToPresent.add(action);
			}
		}
		if (actionsToPresent.size() > 0) {
			if (actionsToPresent.size() == 1) {
				SwingRendererUtils.setMultilineToolTipText(result,
						(String) actionsToPresent.get(0).getValue(Action.NAME));
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

	protected static boolean findActionByName(List<AbstractAction> actions, String name) {
		for (AbstractAction action : actions) {
			if (name.equals(action.getValue(Action.NAME))) {
				return true;
			}
		}
		return false;
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

	public IListStructuralInfo getStructuralInfo() {
		if (structuralInfo == null) {
			structuralInfo = getRootListType().getStructuralInfo();
		}
		return structuralInfo;
	}

	protected void initializeTreeTableControl() {
		rootNode = createRootNode();
		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.width /= 2;
		size.height /= 3;
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
				return reflectionUI.prepareUIString(getColumnCaption(column));
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
		return tableInfo.getColumnCaption(columnIndex);
	}

	protected int getColumnCount() {
		if (getStructuralInfo() == null) {
			return 1;
		}
		IListStructuralInfo tableInfo = getStructuralInfo();
		return tableInfo.getColumnCount();
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
			AutoUpdatingFieldItemPosition itemPosition = (AutoUpdatingFieldItemPosition) node.getUserObject();
			IListStructuralInfo tableInfo = getStructuralInfo();
			if (tableInfo == null) {
				value = reflectionUI.toString(itemPosition.getItem());
			} else {
				value = tableInfo.getCellValue(itemPosition, columnIndex);
			}
			nodeValues.put(columnIndex, value);
		}
		return value;
	}

	protected Image getCellIconImage(ItemNode node, int columnIndex) {
		AutoUpdatingFieldItemPosition itemPosition = (AutoUpdatingFieldItemPosition) node.getUserObject();
		IListStructuralInfo tableInfo = getStructuralInfo();
		if (tableInfo == null) {
			Object item = itemPosition.getItem();
			return reflectionUI.getIconImage(item);
		} else {
			return tableInfo.getCellIconImage(itemPosition, columnIndex);
		}
	}

	protected List<AbstractAction> createCurrentSelectionActions() {
		List<AbstractAction> result = new ArrayList<AbstractAction>();

		List<AutoUpdatingFieldItemPosition> selection = getSelection();

		for (IListAction listAction : getRootListType().getSpecificActions(object, field, selection)) {
			result.add(createSpecificAction(listAction));
		}

		result.add(SEPARATOR_ACTION);

		AutoUpdatingFieldItemPosition singleSelectedPosition = null;
		AutoUpdatingFieldItemPosition singleSelectedPositionSubItemPosition = null;
		if (selection.size() == 1) {
			singleSelectedPosition = selection.get(0);
			singleSelectedPositionSubItemPosition = getSubItemPosition(singleSelectedPosition);
		}
		boolean anySelectionItemContainingListReadOnly = false;
		for (AutoUpdatingFieldItemPosition selectionItem : selection) {
			if (selectionItem.isContainingListReadOnly()) {
				anySelectionItemContainingListReadOnly = true;
				break;
			}
		}

		if (singleSelectedPosition != null) {
			if (hasItemDetails(singleSelectedPosition)) {
				result.add(createOpenItemAction());
			}
		}

		result.add(SEPARATOR_ACTION);

		if (selection.size() == 0) {
			if (!getRootListItemPosition().isContainingListReadOnly()) {
				result.add(createAddChildAction());
			}
		} else if (singleSelectedPositionSubItemPosition != null) {
			if (!singleSelectedPositionSubItemPosition.isContainingListReadOnly()) {
				result.add(createAddChildAction());
			}
		}

		if (singleSelectedPosition != null) {
			if (!singleSelectedPosition.isContainingListReadOnly()) {
				if (singleSelectedPosition.getContainingListType().isOrdered()) {
					result.add(createInsertAction(InsertPosition.BEFORE));
					result.add(createInsertAction(InsertPosition.AFTER));
				} else {
					result.add(createInsertAction(InsertPosition.UNKNOWN));
				}
			}
		}

		result.add(SEPARATOR_ACTION);

		if (selection.size() > 0) {
			boolean canCopyAllSelection = true;
			for (AutoUpdatingFieldItemPosition selectionItem : selection) {
				if (!reflectionUI.canCopy(selectionItem.getItem())) {
					canCopyAllSelection = false;
					break;
				}
			}
			if (canCopyAllSelection) {
				result.add(createCopyAction());
				if (!anySelectionItemContainingListReadOnly) {
					result.add(createCutAction());
				}
			}
		}

		if (clipboard.size() > 0) {
			if (selection.size() == 0) {
				AutoUpdatingFieldItemPosition rootItemPosition = getRootListItemPosition();
				if (!rootItemPosition.isContainingListReadOnly()) {
					boolean rootItemPositionSupportsAllClipboardItems = true;
					for (Object clipboardItem : clipboard) {
						if (!rootItemPosition.supportsItem(clipboardItem)) {
							rootItemPositionSupportsAllClipboardItems = false;
							break;
						}
					}
					if (rootItemPositionSupportsAllClipboardItems) {
						if (rootItemPosition.getContainingListType().isOrdered()) {
							result.add(createPasteAction(InsertPosition.BEFORE));
							result.add(createPasteAction(InsertPosition.AFTER));
						} else {
							result.add(createPasteAction(InsertPosition.UNKNOWN));
						}
					}
				}
			} else if (singleSelectedPosition != null) {
				if (!singleSelectedPosition.isContainingListReadOnly()) {
					boolean selectedItemPositionSupportsAllClipboardItems = true;
					for (Object clipboardItem : clipboard) {
						if (!singleSelectedPosition.supportsItem(clipboardItem)) {
							selectedItemPositionSupportsAllClipboardItems = false;
							break;
						}
					}
					if (selectedItemPositionSupportsAllClipboardItems) {
						if (singleSelectedPosition.getContainingListType().isOrdered()) {
							result.add(createPasteAction(InsertPosition.BEFORE));
							result.add(createPasteAction(InsertPosition.AFTER));
						} else {
							result.add(createPasteAction(InsertPosition.UNKNOWN));
						}
					}
					if (singleSelectedPositionSubItemPosition != null) {
						if (!singleSelectedPositionSubItemPosition.isContainingListReadOnly()) {
							boolean singleSelectedPositionSubItemPositionSupportsAllClipboardItems = true;
							for (Object clipboardItem : clipboard) {
								if (!singleSelectedPositionSubItemPosition.supportsItem(clipboardItem)) {
									singleSelectedPositionSubItemPositionSupportsAllClipboardItems = false;
									break;
								}
							}
							if (singleSelectedPositionSubItemPositionSupportsAllClipboardItems) {
								result.add(createPasteInAction());
							}
						}
					}
				}
			}
		}

		result.add(SEPARATOR_ACTION);

		if (selection.size() > 0) {
			if (!anySelectionItemContainingListReadOnly) {
				boolean allSelectionItemsInSameList = true;
				AutoUpdatingFieldItemPosition firstSelectionItem = selection.get(0);
				for (AutoUpdatingFieldItemPosition selectionItem : selection) {
					if (!ReflectionUIUtils.equalsOrBothNull(firstSelectionItem.getParentItemPosition(),
							selectionItem.getParentItemPosition())) {
						allSelectionItemsInSameList = false;
						break;
					}
				}
				if (allSelectionItemsInSameList && firstSelectionItem.getContainingListType().isOrdered()) {
					result.add(createMoveAction(-1));
					result.add(createMoveAction(+1));
				}
			}
		}

		result.add(SEPARATOR_ACTION);

		if (selection.size() > 0) {
			if (!anySelectionItemContainingListReadOnly) {
				result.add(createRemoveAction());
			}
		}

		if (getRootList().size() > 0) {
			if (!getRootListItemPosition().isContainingListReadOnly()) {
				result.add(createClearAction());
			}
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

	protected AutoUpdatingFieldItemPosition getSubItemPosition(AutoUpdatingFieldItemPosition itemPosition) {
		if (itemPosition.getItem() != null) {
			IListStructuralInfo treeInfo = getStructuralInfo();
			if (treeInfo != null) {
				IFieldInfo subListField = treeInfo.getItemSubListField(itemPosition);
				if (subListField != null) {
					return new AutoUpdatingFieldItemPosition(subListField, itemPosition, -1);
				}
			}
		}
		return null;
	}

	protected AbstractAction createClearAction() {
		return new AbstractAction(reflectionUI.prepareUIString("Remove All")) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (userConfirms("Remove all the items?")) {
						getRootList().clear();
						refreshStructure();
					}
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected boolean userConfirms(String question) {
		return reflectionUI.getSwingRenderer().openQuestionDialog(SwingUtilities.getWindowAncestor(treeTableComponent),
				question, null, "OK", "Cancel");
	}

	protected AbstractAction createMoveAction(final int offset) {
		String label = (offset > 0) ? "Move Down" : "Move Up";
		return new AbstractAction(reflectionUI.prepareUIString(label)) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					List<AutoUpdatingFieldItemPosition> selection = getSelection();
					if (offset > 0) {
						selection = new ArrayList<AutoUpdatingFieldItemPosition>(selection);
						Collections.reverse(selection);
					}
					for (AutoUpdatingFieldItemPosition itemPosition : selection) {
						AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
						int index = itemPosition.getIndex();
						if ((index + offset) < 0) {
							return;
						}
						if ((index + offset) >= list.size()) {
							return;
						}
					}
					List<AutoUpdatingFieldItemPosition> newSelection = new ArrayList<ListControl.AutoUpdatingFieldItemPosition>();
					for (AutoUpdatingFieldItemPosition itemPosition : selection) {
						AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
						int index = itemPosition.getIndex();
						list.move(index, offset);
						newSelection.add(itemPosition.getSibling(index + offset));
					}
					refreshStructure();
					setSelection(newSelection);
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	public AutoUpdatingFieldItemPosition getSingleSelection() {
		List<AutoUpdatingFieldItemPosition> selection = getSelection();
		if ((selection.size() == 0) || (selection.size() > 1)) {
			return null;
		} else {
			return selection.get(0);
		}
	}

	public List<AutoUpdatingFieldItemPosition> getSelection() {
		List<AutoUpdatingFieldItemPosition> result = new ArrayList<ListControl.AutoUpdatingFieldItemPosition>();
		for (int selectedRow : treeTableComponent.getSelectedRows()) {
			TreePath path = treeTableComponent.getPathForRow(selectedRow);
			if (path == null) {
				return null;
			}
			ItemNode selectedNode = (ItemNode) path.getLastPathComponent();
			AutoUpdatingFieldItemPosition itemPosition = (AutoUpdatingFieldItemPosition) selectedNode.getUserObject();
			result.add(itemPosition);
		}
		return result;
	}

	public void setSingleSelection(AutoUpdatingFieldItemPosition toSelect) {
		setSelection(Collections.singletonList(toSelect));
	}

	public AutoUpdatingFieldItemPosition findItemPosition(final Object item) {
		final AutoUpdatingFieldItemPosition[] result = new AutoUpdatingFieldItemPosition[1];
		visitItems(new IItemsVisitor() {
			@Override
			public boolean visitItem(AutoUpdatingFieldItemPosition itemPosition) {
				if (reflectionUI.equals(itemPosition.getItem(), item)) {
					result[0] = itemPosition;
					return false;
				}
				return true;
			}
		});
		return result[0];
	}

	public void setSelection(List<AutoUpdatingFieldItemPosition> toSelect) {
		List<TreePath> treePaths = new ArrayList<TreePath>();
		for (int i = 0; i < toSelect.size(); i++) {
			AutoUpdatingFieldItemPosition itemPosition = toSelect.get(i);
			if (itemPosition == null) {
				treeTableComponent.clearSelection();
			} else {
				ItemNode itemNode = findNode(itemPosition);
				if (itemNode == null) {
					AutoUpdatingFieldItemPosition parentItemPosition = itemPosition.getParentItemPosition();
					if (parentItemPosition == null) {
						treeTableComponent.clearSelection();
						return;
					}
					toSelect = new ArrayList<ListControl.AutoUpdatingFieldItemPosition>(toSelect);
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

	protected ItemNode findNode(AutoUpdatingFieldItemPosition itemPosition) {
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

	protected AbstractAction createRemoveAction() {
		return new AbstractAction(reflectionUI.prepareUIString("Remove")) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (userConfirms("Remove the element(s)?")) {
						List<AutoUpdatingFieldItemPosition> selection = getSelection();
						selection = new ArrayList<AutoUpdatingFieldItemPosition>(selection);
						Collections.reverse(selection);
						List<AutoUpdatingFieldItemPosition> toPostSelect = new ArrayList<ListControl.AutoUpdatingFieldItemPosition>();
						if (selection.size() > 1) {
							beginCompositeModification(false);
						}
						for (AutoUpdatingFieldItemPosition itemPosition : selection) {
							if (itemPosition == null) {
								return;
							}
							AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
							int index = itemPosition.getIndex();
							list.remove(index);
							updateOnItemRemoval(toPostSelect, itemPosition);
							if (itemPosition.getContainingListType().isOrdered() && (index > 0)) {
								toPostSelect.add(itemPosition.getSibling(index - 1));
							} else {
								toPostSelect.add(itemPosition.getParentItemPosition());
							}
						}
						if (selection.size() > 1) {
							endCompositeModification("Remove " + selection.size() + " elements", false, UndoOrder.LIFO);
						}
						refreshStructure();
						setSelection(toPostSelect);
					}
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected void updateOnItemRemoval(List<AutoUpdatingFieldItemPosition> toUpdate,
			AutoUpdatingFieldItemPosition itemPosition) {
		for (int i = 0; i < toUpdate.size(); i++) {
			AutoUpdatingFieldItemPosition toUpdateItem = toUpdate.get(i);
			if (toUpdateItem.equals(itemPosition) || toUpdateItem.getAncestors().contains(itemPosition)) {
				toUpdate.remove(i);
				i--;
			} else if (toUpdateItem.getPreviousSiblings().contains(itemPosition)) {
				toUpdate.set(i, new AutoUpdatingFieldItemPosition(toUpdateItem.getContainingListField(),
						toUpdateItem.getParentItemPosition(), toUpdateItem.getIndex() - 1));
			}
		}
	}

	protected void beginCompositeModification(boolean restoreSelection) {
		ModificationStack modifStack = getParentFormModificationStack();
		modifStack.beginComposite();
		if (restoreSelection) {
			modifStack.pushUndo(new ChangeListSelectionModification(getSelection(), getSelection()));
		}
	}

	protected void endCompositeModification(String title, boolean restoreSelection, UndoOrder order) {
		ModificationStack modifStack = getParentFormModificationStack();
		if (restoreSelection) {
			modifStack.pushUndo(new ChangeListSelectionModification(getSelection(), getSelection()));
		}
		modifStack.endComposite(title, order);
	}

	protected class GhostItemPosition extends AutoUpdatingFieldItemPosition {

		protected Object item;

		public GhostItemPosition(AutoUpdatingFieldItemPosition itemPosition, Object item) {
			super(itemPosition.getContainingListField(), itemPosition.getParentItemPosition(), itemPosition.getIndex());
			this.item = item;
		}

		@Override
		public Object getItem() {
			return item;
		}

		@Override
		public AutoUpdatingFieldList getContainingAutoUpdatingFieldList() {
			return new AutoUpdatingFieldList(this) {

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

	protected AbstractAction createInsertAction(final InsertPosition insertPosition) {
		final AutoUpdatingFieldItemPosition itemPosition;
		AutoUpdatingFieldItemPosition singleSelection = getSingleSelection();
		final int index;
		if (singleSelection == null) {
			index = getRootList().size();
			itemPosition = new AutoUpdatingFieldItemPosition(field, null, index);
		} else {
			if (insertPosition == InsertPosition.AFTER) {
				index = singleSelection.getIndex() + 1;
			} else {
				index = singleSelection.getIndex();
			}
			itemPosition = singleSelection.getSibling(index);
		}
		final IListTypeInfo listType = itemPosition.getContainingListType();
		final ITypeInfo itemType = listType.getItemType();

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
			}
			buttonText += " ...";
		}
		return new AbstractAction(reflectionUI.prepareUIString(buttonText)) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ITypeInfo typeToInstanciate = itemType;
					if (typeToInstanciate == null) {
						typeToInstanciate = reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class));
					}
					typeToInstanciate = addSpecificItemContructors(typeToInstanciate, itemPosition);
					Object newItem = reflectionUI.getSwingRenderer().onTypeInstanciationRequest(ListControl.this,
							typeToInstanciate, false);
					if (newItem == null) {
						return;
					}
					GhostItemPosition futureItemPosition = new GhostItemPosition(itemPosition, newItem);
					if (openDetailsDialog(futureItemPosition, new boolean[1], false)) {
						newItem = futureItemPosition.getItem();
						AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
						list.add(index, newItem);
						refreshStructure();
						AutoUpdatingFieldItemPosition toSelect = itemPosition;
						if (!listType.isOrdered()) {
							int indexToSelect = list.indexOf(newItem);
							toSelect = itemPosition.getSibling(indexToSelect);
						}
						setSingleSelection(toSelect);
					}
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected AbstractAction createAddChildAction() {
		final AutoUpdatingFieldItemPosition itemPosition = getSingleSelection();
		final AutoUpdatingFieldItemPosition subItemPosition;
		if (itemPosition == null) {
			subItemPosition = getRootListItemPosition();
		} else {
			subItemPosition = getSubItemPosition(itemPosition);
		}
		if (subItemPosition == null) {
			return null;
		}
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
		return new AbstractAction(reflectionUI.prepareUIString(title)) {

			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ITypeInfo typeToInstanciate = subListItemType;
					if (typeToInstanciate == null) {
						typeToInstanciate = new DefaultTypeInfo(reflectionUI, Object.class);
					}
					typeToInstanciate = addSpecificItemContructors(typeToInstanciate, subItemPosition);
					Object newSubListItem = reflectionUI.getSwingRenderer().onTypeInstanciationRequest(ListControl.this,
							typeToInstanciate, false);
					if (newSubListItem == null) {
						return;
					}

					GhostItemPosition futureSubItemPosition = new GhostItemPosition(subItemPosition, newSubListItem);
					if (openDetailsDialog(futureSubItemPosition, new boolean[1], false)) {
						newSubListItem = futureSubItemPosition.getItem();
						AutoUpdatingFieldList subList = new AutoUpdatingFieldItemPosition(
								subItemPosition.getContainingListField(), itemPosition, -1)
										.getContainingAutoUpdatingFieldList();
						int newSubListItemIndex = subList.size();
						subList.add(newSubListItemIndex, newSubListItem);
						refreshStructure();
						if (!subListType.isOrdered()) {
							newSubListItemIndex = subList.indexOf(newSubListItem);
						}
						AutoUpdatingFieldItemPosition toSelect = subItemPosition.getSibling(newSubListItemIndex);
						setSingleSelection(toSelect);
					}
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}

		};
	}

	protected ITypeInfo addSpecificItemContructors(ITypeInfo itemType,
			final AutoUpdatingFieldItemPosition newItemPosition) {
		return new TypeInfoProxyConfiguration() {

			@Override
			protected List<IMethodInfo> getConstructors(ITypeInfo type) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(type));
				IListTypeInfo containingListType = newItemPosition.getContainingListType();
				Object containingListOwner = newItemPosition.getContainingListOwner();
				if (containingListOwner != null) {
					IFieldInfo containingListField = newItemPosition.getContainingListField();
					List<IMethodInfo> specificItemConstructors = containingListType
							.getSpecificItemConstructors(containingListOwner, containingListField);
					if (specificItemConstructors != null) {
						result.addAll(specificItemConstructors);
					}
				}
				return result;
			}

		}.get(itemType);
	}

	protected AbstractAction createCopyAction() {
		return new AbstractAction(reflectionUI.prepareUIString("Copy")) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					clipboard.clear();
					List<AutoUpdatingFieldItemPosition> selection = getSelection();
					for (AutoUpdatingFieldItemPosition itemPosition : selection) {
						if (itemPosition == null) {
							return;
						}
						clipboard.add(reflectionUI.copy(itemPosition.getItem()));
					}
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected AbstractAction createCutAction() {
		return new AbstractAction(reflectionUI.prepareUIString("Cut")) {

			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					clipboard.clear();
					List<AutoUpdatingFieldItemPosition> selection = getSelection();
					selection = new ArrayList<AutoUpdatingFieldItemPosition>(selection);
					Collections.reverse(selection);
					List<AutoUpdatingFieldItemPosition> toPostSelect = new ArrayList<ListControl.AutoUpdatingFieldItemPosition>();
					if (selection.size() > 1) {
						beginCompositeModification(false);
					}
					for (AutoUpdatingFieldItemPosition itemPosition : selection) {
						if (itemPosition == null) {
							return;
						}
						clipboard.add(0, reflectionUI.copy(itemPosition.getItem()));
						AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
						int index = itemPosition.getIndex();
						list.remove(index);
						updateOnItemRemoval(toPostSelect, itemPosition);
						if (itemPosition.getContainingListType().isOrdered() && (index > 0)) {
							toPostSelect.add(itemPosition.getSibling(index - 1));
						} else {
							toPostSelect.add(itemPosition.getParentItemPosition());
						}
					}
					if (selection.size() > 1) {
						endCompositeModification("Cut " + selection.size() + " elements", false, UndoOrder.LIFO);
					}
					refreshStructure();
					setSelection(toPostSelect);
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected AbstractAction createPasteAction(final InsertPosition insertPosition) {
		String buttonText;
		if (insertPosition == InsertPosition.AFTER) {
			buttonText = "Paste After";
		} else if (insertPosition == InsertPosition.BEFORE) {
			buttonText = "Paste Before";
		} else {
			buttonText = "Paste";
		}
		return new AbstractAction(reflectionUI.prepareUIString(buttonText)) {

			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					AutoUpdatingFieldItemPosition itemPosition = getSingleSelection();
					int index;
					if (itemPosition == null) {
						index = getRootList().size();
						itemPosition = new AutoUpdatingFieldItemPosition(field, null, index);
					} else {
						index = itemPosition.getIndex();
						if (insertPosition == InsertPosition.AFTER) {
							index++;
						}
					}
					int initialIndex = index;
					AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
					for (Object clipboardItyem : clipboard) {
						list.add(index, clipboardItyem);
						index++;
					}
					refreshStructure();
					List<AutoUpdatingFieldItemPosition> toPostSelect = new ArrayList<ListControl.AutoUpdatingFieldItemPosition>();
					IListTypeInfo listType = itemPosition.getContainingListType();
					index = initialIndex;
					for (int i = 0; i < clipboard.size(); i++) {
						Object clipboardItem = clipboard.get(i);
						if (listType.isOrdered()) {
							index = initialIndex + i;
						} else {
							index = list.indexOf(clipboardItem);
						}
						if (index != -1) {
							toPostSelect.add(itemPosition.getSibling(index));
						}
					}
					setSelection(toPostSelect);
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected AbstractAction createPasteInAction() {
		return new AbstractAction(reflectionUI.prepareUIString("Paste")) {

			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					AutoUpdatingFieldItemPosition itemPosition = getSingleSelection();
					if (itemPosition == null) {
						return;
					}
					AutoUpdatingFieldItemPosition subItemPosition = getSubItemPosition(itemPosition);
					AutoUpdatingFieldList subList = subItemPosition.getContainingAutoUpdatingFieldList();
					int newSubListItemIndex = subList.size();
					int newSubListItemInitialIndex = newSubListItemIndex;
					subItemPosition = subItemPosition.getSibling(newSubListItemIndex);
					for (Object clipboardItem : clipboard) {
						subList.add(newSubListItemIndex, clipboardItem);
						newSubListItemIndex++;
					}
					refreshStructure();
					List<AutoUpdatingFieldItemPosition> toPostSelect = new ArrayList<ListControl.AutoUpdatingFieldItemPosition>();
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
					setSelection(toPostSelect);
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected ITypeInfo getRootListItemType() {
		return getRootListType().getItemType();
	}

	protected IListTypeInfo getRootListType() {
		return (IListTypeInfo) field.getType();
	}

	protected AbstractAction createSpecificAction(final IListAction action) {
		return new AbstractAction(reflectionUI.prepareUIString(action.getTitle())) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					reflectionUI.getSwingRenderer().showBusyDialogWhile(ListControl.this, new Runnable() {
						@Override
						public void run() {
							action.perform(ListControl.this);
						}
					}, action.getTitle());
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
			}
		};
	}

	protected AbstractAction createOpenItemAction() {
		return new AbstractAction(reflectionUI.prepareUIString("Open")) {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					AutoUpdatingFieldItemPosition itemPosition = getSingleSelection();
					if (itemPosition == null) {
						return;
					}
					onOpenDetaildsDialogRequest(itemPosition);
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(ListControl.this, t);
				}
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
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(treeTableComponent, t);
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
					TreePath path = treeTableComponent.getPathForLocation(me.getX(), me.getY());
					if (path == null) {
						return;
					}
					ItemNode selectedNode = (ItemNode) path.getLastPathComponent();
					AutoUpdatingFieldItemPosition itemPosition = (AutoUpdatingFieldItemPosition) selectedNode
							.getUserObject();
					onOpenDetaildsDialogRequest(itemPosition);
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(treeTableComponent, t);
				}
			}
		});
	}

	protected void onOpenDetaildsDialogRequest(AutoUpdatingFieldItemPosition itemPosition) {
		Object value = itemPosition.getItem();
		GhostItemPosition ghostItemPosition = new GhostItemPosition(itemPosition, value);
		boolean[] changeDetectedArray = new boolean[] { false };
		if (openDetailsDialog(ghostItemPosition, changeDetectedArray, true)) {
			AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
			Object newValue = ghostItemPosition.getItem();
			int index = itemPosition.getIndex();
			Object newvalue = ghostItemPosition.getItem();
			if (!changeDetectedArray[0]) {
				return;
			}
			list.set(index, newvalue);
			refreshStructure();
			AutoUpdatingFieldItemPosition itemPositionToSelect = itemPosition;
			if (!itemPosition.getContainingListType().isOrdered()) {
				int newIndex = list.indexOf(newValue);
				itemPositionToSelect = itemPositionToSelect.getSibling(newIndex);
			}
			setSingleSelection(itemPositionToSelect);
		}
	}

	protected AutoUpdatingFieldList getRootList() {
		return getRootListItemPosition().getContainingAutoUpdatingFieldList();
	}

	protected AutoUpdatingFieldItemPosition getRootListItemPosition() {
		return new AutoUpdatingFieldItemPosition(field, null, -1);
	}

	protected boolean openDetailsDialog(final AutoUpdatingFieldItemPosition itemPosition, boolean[] changeDetectedArray,
			boolean recordModifications) {
		ItemNode itemNode = findNode(itemPosition);
		if (itemNode != null) {
			TreePath treePath = new TreePath(itemNode.getPath());
			treeTableComponent.expandPath(treePath);
		}

		if (!hasItemDetails(itemPosition)) {
			return true;
		}

		final AutoUpdatingFieldList list = itemPosition.getContainingAutoUpdatingFieldList();
		final int index = itemPosition.getIndex();
		final Object[] valueArray = new Object[1];
		final Accessor<Object> valueAccessor = new Accessor<Object>() {

			@Override
			public Object get() {
				return (valueArray[0] = list.get(index));
			}

			@Override
			public void set(Object value) {
				if (!itemPosition.isContainingListReadOnly()) {
					list.set(index, (valueArray[0] = value));
				}
			}

		};
		ModificationStack parentStack;
		if (recordModifications) {
			parentStack = getParentFormModificationStack();
		} else {
			parentStack = null;
		}
		String title = reflectionUI.getFieldTitle(object, new FieldInfoProxy(itemPosition.getContainingListField()) {
			@Override
			public String getCaption() {
				String result = itemPosition.getContainingListField().getCaption() + " Item";
				if (itemPosition.getParentItemPosition() != null) {
					AutoUpdatingFieldItemPosition parentItempPosition = itemPosition.getParentItemPosition();
					ITypeInfo prentItemType = reflectionUI
							.getTypeInfo(reflectionUI.getTypeInfoSource(parentItempPosition.getItem()));
					result = ReflectionUIUtils.composeTitle(prentItemType.getCaption(), result);
				}
				return result;
			}

			@Override
			public Object getValue(Object object) {
				return list.get(index);
			}
		});
		IInfoCollectionSettings settings = getStructuralInfo().getItemInfoSettings(itemPosition);
		boolean isGetOnly = itemPosition.getContainingListField().isReadOnly();
		return reflectionUI.getSwingRenderer().openValueDialog(treeTableComponent, valueAccessor,
				isGetOnly, settings, parentStack, title, changeDetectedArray);
	}

	protected ModificationStack getParentFormModificationStack() {
		return SwingRendererUtils.findModificationStack(ListControl.this, reflectionUI);
	}

	protected boolean hasItemDetails(AutoUpdatingFieldItemPosition itemPosition) {
		Object item = itemPosition.getItem();
		if (item == null) {
			return false;
		}
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
		if (reflectionUI.getSwingRenderer().hasCustomFieldControl(item)) {
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

	protected void visitItems(IItemsVisitor iItemsVisitor, AutoUpdatingFieldItemPosition currentListItemPosition) {
		AutoUpdatingFieldList list = currentListItemPosition.getContainingAutoUpdatingFieldList();
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
								new AutoUpdatingFieldItemPosition(childrenField, currentListItemPosition, -1));
					}
				}

			}
		}
	}

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
		List<AutoUpdatingFieldItemPosition> lastlySelectedItemPositions = getSelection();
		List<Object> lastlySelectedItems = new ArrayList<Object>();
		for (int i = 0; i < lastlySelectedItemPositions.size(); i++) {
			try {
				AutoUpdatingFieldItemPosition lastlySelectedItemPosition = lastlySelectedItemPositions.get(i);
				lastlySelectedItems.add(lastlySelectedItemPosition.getItem());
			} catch (Throwable t) {
				lastlySelectedItems.add(null);
			}
		}

		runnable.run();

		int i = 0;
		for (Iterator<AutoUpdatingFieldItemPosition> it = lastlySelectedItemPositions.iterator(); it.hasNext();) {
			AutoUpdatingFieldItemPosition lastlySelectedItemPosition = it.next();
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
		protected AutoUpdatingFieldItemPosition currentItemPosition;
		protected boolean childrenLoaded = false;;

		public ItemNode(IFieldInfo childrenField, AutoUpdatingFieldItemPosition currentItemPosition) {
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
			AutoUpdatingFieldItemPosition anyChildItemPosition = new AutoUpdatingFieldItemPosition(childrenField,
					currentItemPosition, -1);
			AutoUpdatingFieldList list = anyChildItemPosition.getContainingAutoUpdatingFieldList();
			for (int i = 0; i < list.size(); i++) {
				AutoUpdatingFieldItemPosition childItemPosition = anyChildItemPosition.getSibling(i);
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

	public class AutoUpdatingFieldItemPosition extends ItemPosition {

		public AutoUpdatingFieldItemPosition(IFieldInfo containingListField,
				AutoUpdatingFieldItemPosition parentItemPosition, int index) {
			super(containingListField, parentItemPosition, index, object);
		}

		public AutoUpdatingFieldList getContainingAutoUpdatingFieldList() {
			return new AutoUpdatingFieldList(this);
		}

		@Override
		public AutoUpdatingFieldItemPosition getParentItemPosition() {
			return (AutoUpdatingFieldItemPosition) super.getParentItemPosition();
		}

		@Override
		public AutoUpdatingFieldItemPosition getSibling(int index2) {
			ItemPosition result = super.getSibling(index2);
			return new AutoUpdatingFieldItemPosition(result.getContainingListField(), getParentItemPosition(),
					result.getIndex());
		}

		@Override
		public AutoUpdatingFieldItemPosition getRootListItemPosition() {
			ItemPosition result = super.getRootListItemPosition();
			return new AutoUpdatingFieldItemPosition(result.getContainingListField(), null, result.getIndex());
		}

	}

	protected class ChangeListSelectionModification implements IModification {
		protected List<AutoUpdatingFieldItemPosition> toSelect;
		protected List<AutoUpdatingFieldItemPosition> undoSelection;

		public ChangeListSelectionModification(List<AutoUpdatingFieldItemPosition> toSelect,
				List<AutoUpdatingFieldItemPosition> undoSelection) {
			this.toSelect = toSelect;
			this.undoSelection = undoSelection;
		}

		@Override
		public IModification applyAndGetOpposite(boolean refreshView) {
			List<AutoUpdatingFieldItemPosition> oppositeSelection;
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

	protected class AutoUpdatingFieldList extends AbstractList {
		protected AutoUpdatingFieldItemPosition itemPosition;

		public AutoUpdatingFieldList(AutoUpdatingFieldItemPosition itemPosition) {
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
				return null;
			}
			return getListType().toArray(listFieldValue);
		}

		protected void replaceUnderlyingListValue(Object[] listValue) {
			ModificationStack modifStack = getParentFormModificationStack();
			Object listOwner = getListOwner();
			if (!getListField().isReadOnly()) {
				SetListValueModification modif = new SetListValueModification(reflectionUI, listValue, listOwner,
						getListField());
				if (itemPosition.isRootListItemPosition()) {
					modif.applyAndGetOpposite(false);
				} else {
					modifStack.apply(modif, false);
				}
			}
			if (!itemPosition.isRootListItemPosition()) {
				AutoUpdatingFieldItemPosition listOwnerPosition = itemPosition.getParentItemPosition();
				new AutoUpdatingFieldList(listOwnerPosition).set(listOwnerPosition.getIndex(), listOwner);
			}
		}

		protected void beginModification() {
			beginCompositeModification(false);
		}

		protected void endModification(String title) {
			endCompositeModification(title, false, UndoOrder.FIFO);
		}

		@Override
		public Object get(int index) {
			return getUnderlyingListValue()[index];
		}

		@Override
		public int size() {
			Object[] underlyingListValue = getUnderlyingListValue();
			if (underlyingListValue == null) {
				return 0;
			}
			return underlyingListValue.length;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void add(int index, Object element) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			tmpList.add(index, element);
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification("Add item to '" + getListField().getCaption() + "'");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object remove(int index) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			Object result = tmpList.remove(index);
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification("Remove item from '" + getListField().getCaption() + "'");
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object set(int index, Object element) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			Object result = tmpList.set(index, element);
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification("Update '" + getListField().getCaption() + "' item " + index);
			return result;
		}

		@SuppressWarnings("unchecked")
		public void move(int index, int offset) {
			List tmpList = new ArrayList(Arrays.asList(getUnderlyingListValue()));
			tmpList.add(index + offset, tmpList.remove(index));
			beginModification();
			replaceUnderlyingListValue(tmpList.toArray());
			endModification("Move '" + getListField().getCaption() + "' item");
		}

		@Override
		public void clear() {
			beginModification();
			replaceUnderlyingListValue(new Object[0]);
			endModification("Clear '" + getListField().getCaption() + "'");
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
			if (!(node.getUserObject() instanceof AutoUpdatingFieldItemPosition)) {
				return;
			}
			String text = getCellValue(node, columnIndex);
			if (text == null) {
				label.setText("");
			} else {
				label.setText(reflectionUI.prepareUIString(text));
			}

			Image imageIcon = getCellIconImage(node, columnIndex);
			if (imageIcon == null) {
				label.setIcon(null);
			} else {
				label.setIcon(new ImageIcon(imageIcon));
			}
		}
	}

	public interface IItemsVisitor {

		boolean visitItem(AutoUpdatingFieldItemPosition itemPosition);

	}

	protected enum InsertPosition {
		AFTER, BEFORE, UNKNOWN
	}
}
