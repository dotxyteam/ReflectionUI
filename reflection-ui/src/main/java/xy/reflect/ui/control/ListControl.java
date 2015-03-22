package xy.reflect.ui.control;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ModificationStack.IModification;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCollectionSettingsProxy;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.IListTypeInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.info.type.IListTypeInfo.IListStructuralInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("rawtypes")
public class ListControl extends JSplitPane implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected JXTreeTable treeTableComponent;
	protected ItemNode rootNode;
	protected JPanel buttonsPanel;
	protected Map<ItemNode, Map<Integer, String>> valuesByNode = new HashMap<ItemNode, Map<Integer, String>>();
	protected static List<Object> clipboard = new ArrayList<Object>();

	public ListControl(final ReflectionUI reflectionUI, final Object object,
			final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;

		initializeTreeTableControl();
		setRightComponent(new JScrollPane(treeTableComponent));

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.width = size.width / 2;
		size.height = size.height / 3;
		setPreferredSize(size);
		setMinimumSize(size);

		refreshStructure();
		openDetailsDialogOnItemDoubleClick();
		updateButtonsPanelOnItemSelection();

		buttonsPanel = new JPanel();
		setLeftComponent(new JScrollPane(ReflectionUIUtils.flowInLayout(
				buttonsPanel, FlowLayout.CENTER)));
		GridLayout layout = new GridLayout(0, 1);
		buttonsPanel.setLayout(layout);

		updateButtonsPanel();
		setDividerLocation(ReflectionUIUtils
				.getStandardCharacterWidth(new JButton()) * 30);
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		return false;
	}

	public IListStructuralInfo getStructuralInfo() {
		return getRootListType().getStructuralInfo();
	}

	protected void initializeTreeTableControl() {
		rootNode = createRootNode();
		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		size.width /= 2;
		size.height /= 3;
		treeTableComponent = new JXTreeTable(createTreeTableModel());
		treeTableComponent.setExpandsSelectedPaths(true);
		treeTableComponent.getSelectionModel().setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		treeTableComponent.setRootVisible(false);
		treeTableComponent.setShowsRootHandles(true);
		treeTableComponent.setDefaultRenderer(Object.class,
				new ItemCellRenderer());
		treeTableComponent.setTreeCellRenderer(new ItemCellRenderer());
		treeTableComponent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		treeTableComponent.setHorizontalScrollEnabled(true);
		treeTableComponent.setColumnMargin(5);
		fixCustomRenderingNotAppliedOnUnselectedCells();
	}

	protected void fixCustomRenderingNotAppliedOnUnselectedCells() {
		treeTableComponent.setHighlighters();
		treeTableComponent.putClientProperty(JXTable.USE_DTCR_COLORMEMORY_HACK,
				false);
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
				return reflectionUI.translateUIString(getColumnCaption(column));
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
			ItemPosition itemPosition = (ItemPosition) node.getUserObject();
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

	protected void updateButtonsPanel() {
		buttonsPanel.removeAll();

		List<ItemPosition> selection = getSelection();
		ItemPosition singleSelectedPosition = null;
		ItemPosition singleSelectedPositionSubItemPosition = null;
		if (selection.size() == 1) {
			singleSelectedPosition = selection.get(0);
			singleSelectedPositionSubItemPosition = getSubItemPosition(singleSelectedPosition);
		}
		boolean anySelectionListReadOnly = false;
		for (ItemPosition selectionItem : selection) {
			if (selectionItem.isContainingListReadOnly()) {
				anySelectionListReadOnly = true;
				break;
			}
		}

		if (singleSelectedPosition != null) {
			if (hasItemDetails(singleSelectedPosition)) {
				createOpenItemButton(buttonsPanel);
			}
		}

		if (selection.size() == 0) {
			if (!getRootListItemPosition().isContainingListReadOnly()) {
				createAddButton(buttonsPanel);
			}
		} else if (singleSelectedPositionSubItemPosition != null) {
			if (!singleSelectedPositionSubItemPosition
					.isContainingListReadOnly()) {
				createAddButton(buttonsPanel);
			}
		}

		if (singleSelectedPosition != null) {
			if (!singleSelectedPosition.isContainingListReadOnly()) {
				if (singleSelectedPosition.getContainingListType().isOrdered()) {
					createInsertButton(buttonsPanel, InsertPosition.BEFORE);
					createInsertButton(buttonsPanel, InsertPosition.AFTER);
				} else {
					createInsertButton(buttonsPanel,
							InsertPosition.INDERTERMINATE);
				}
			}
		}

		if (selection.size() > 0) {
			boolean canCopyAllSelection = true;
			for (ItemPosition selectionItem : selection) {
				if (!reflectionUI.canCopy(selectionItem.getItem())) {
					canCopyAllSelection = false;
					break;
				}
			}
			if (canCopyAllSelection) {
				createCopyButton(buttonsPanel);
				if (!anySelectionListReadOnly) {
					createCutButton(buttonsPanel);
				}
			}
		}

		if (clipboard.size() > 0) {
			if (selection.size() == 0) {
				ItemPosition rootItemPosition = getRootListItemPosition();
				if (!rootItemPosition.isContainingListReadOnly()) {
					boolean rootItemPositionSupportsAllClipboardItems = true;
					for (Object clipboardItem : clipboard) {
						if (!rootItemPosition.supportsValue(clipboardItem)) {
							rootItemPositionSupportsAllClipboardItems = false;
							break;
						}
					}
					if (rootItemPositionSupportsAllClipboardItems) {
						if (rootItemPosition.getContainingListType()
								.isOrdered()) {
							createPasteButton(buttonsPanel,
									InsertPosition.BEFORE);
							createPasteButton(buttonsPanel,
									InsertPosition.AFTER);
						} else {
							createPasteButton(buttonsPanel,
									InsertPosition.INDERTERMINATE);
						}
					}
				}
			} else if (singleSelectedPosition != null) {
				if (!singleSelectedPosition.isContainingListReadOnly()) {
					boolean selectedItemPositionSupportsAllClipboardItems = true;
					for (Object clipboardItem : clipboard) {
						if (!singleSelectedPosition
								.supportsValue(clipboardItem)) {
							selectedItemPositionSupportsAllClipboardItems = false;
							break;
						}
					}
					if (selectedItemPositionSupportsAllClipboardItems) {
						if (singleSelectedPosition.getContainingListType()
								.isOrdered()) {
							createPasteButton(buttonsPanel,
									InsertPosition.BEFORE);
							createPasteButton(buttonsPanel,
									InsertPosition.AFTER);
						} else {
							createPasteButton(buttonsPanel,
									InsertPosition.INDERTERMINATE);
						}
					}
					if (singleSelectedPositionSubItemPosition != null) {
						if (!singleSelectedPositionSubItemPosition
								.isContainingListReadOnly()) {
							boolean singleSelectedPositionSubItemPositionSupportsAllClipboardItems = true;
							for (Object clipboardItem : clipboard) {
								if (!singleSelectedPositionSubItemPosition
										.supportsValue(clipboardItem)) {
									singleSelectedPositionSubItemPositionSupportsAllClipboardItems = false;
									break;
								}
							}
							if (singleSelectedPositionSubItemPositionSupportsAllClipboardItems) {
								createPasteInButton(buttonsPanel);
							}
						}
					}
				}
			}
		}

		if (selection.size() > 0) {
			if (!anySelectionListReadOnly) {
				createRemoveButton(buttonsPanel);
				boolean allSelectionItemsInSameList = true;
				ItemPosition firstSelectionItem = selection.get(0);
				for (ItemPosition selectionItem : selection) {
					if (!ReflectionUIUtils.equalsOrBothNull(
							firstSelectionItem.getParentItemPosition(),
							selectionItem.getParentItemPosition())) {
						allSelectionItemsInSameList = false;
						break;
					}
				}
				if (allSelectionItemsInSameList
						&& firstSelectionItem.getContainingListType()
								.isOrdered()) {
					createMoveButton(buttonsPanel, -1, "Up");
					createMoveButton(buttonsPanel, +1, "Down");
				}
			}
		}

		if (getRootList().size() > 0) {
			if (!getRootListItemPosition().isContainingListReadOnly()) {
				createClearButton(buttonsPanel);
			}
		}

		validate();
	}

	protected ItemPosition getSubItemPosition(ItemPosition itemPosition) {
		if (itemPosition.getItem() != null) {
			IListStructuralInfo treeInfo = getStructuralInfo();
			if (treeInfo != null) {
				IFieldInfo subListField = treeInfo
						.getItemSubListField(itemPosition);
				if (subListField != null) {
					return new ItemPosition(subListField, itemPosition, -1);
				}
			}
		}
		return null;
	}

	protected void createClearButton(JPanel buttonsPanel) {
		final JButton button = new JButton(
				reflectionUI.translateUIString("Clear"));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
							SwingUtilities
									.getWindowAncestor(treeTableComponent),
							reflectionUI
									.translateUIString("Remove all the items?"),
							"", JOptionPane.OK_CANCEL_OPTION)) {
						getRootList().clear();
						refreshStructure();
					}
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected void createMoveButton(JPanel buttonsPanel, final int offset,
			String label) {
		final JButton button = new JButton(
				reflectionUI.translateUIString(label));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					List<ItemPosition> selection = getSelection();
					if (offset > 0) {
						selection = new ArrayList<ItemPosition>(selection);
						Collections.reverse(selection);
					}
					for (ItemPosition itemPosition : selection) {
						FieldAutoUpdateList list = itemPosition
								.getContainingList();
						int index = itemPosition.getIndex();
						if ((index + offset) < 0) {
							return;
						}
						if ((index + offset) >= list.size()) {
							return;
						}
					}
					List<ItemPosition> newSelection = new ArrayList<ListControl.ItemPosition>();
					for (ItemPosition itemPosition : selection) {
						FieldAutoUpdateList list = itemPosition
								.getContainingList();
						int index = itemPosition.getIndex();
						list.move(index, offset);
						newSelection.add(itemPosition
								.getSibling(index + offset));
					}
					refreshStructure();
					setSelection(newSelection);
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected ItemPosition getSingleSelection() {
		List<ItemPosition> selection = getSelection();
		if ((selection.size() == 0) || (selection.size() > 1)) {
			return null;
		} else {
			return selection.get(0);
		}
	}

	protected List<ItemPosition> getSelection() {
		List<ItemPosition> result = new ArrayList<ListControl.ItemPosition>();
		for (int selectedRow : treeTableComponent.getSelectedRows()) {
			TreePath path = treeTableComponent.getPathForRow(selectedRow);
			if (path == null) {
				return null;
			}
			ItemNode selectedNode = (ItemNode) path.getLastPathComponent();
			ItemPosition itemPosition = (ItemPosition) selectedNode
					.getUserObject();
			result.add(itemPosition);
		}
		return result;
	}

	public void setSingleSelection(ItemPosition toSelect) {
		setSelection(Collections.singletonList(toSelect));
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
					ItemPosition parentItemPosition = itemPosition
							.getParentItemPosition();
					if (parentItemPosition == null) {
						treeTableComponent.clearSelection();
						return;
					}
					toSelect = new ArrayList<ListControl.ItemPosition>(toSelect);
					toSelect.set(i, parentItemPosition);
					setSelection(toSelect);
					return;
				}
				treePaths.add(new TreePath(itemNode.getPath()));
			}
		}
		treeTableComponent.getTreeSelectionModel().setSelectionPaths(
				treePaths.toArray(new TreePath[treePaths.size()]));
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
		ItemNode result = (ItemNode) parentNode.getChildAt(itemPosition
				.getIndex());
		return result;
	}

	protected void createRemoveButton(JPanel buttonsPanel) {
		final JButton button = new JButton(
				reflectionUI.translateUIString("Remove"));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
							SwingUtilities
									.getWindowAncestor(treeTableComponent),
							reflectionUI
									.translateUIString("Remove the element(s)?"),
							"", JOptionPane.OK_CANCEL_OPTION)) {
						List<ItemPosition> selection = getSelection();
						selection = new ArrayList<ItemPosition>(selection);
						Collections.reverse(selection);
						List<ItemPosition> toPostSelect = new ArrayList<ListControl.ItemPosition>();
						for (ItemPosition itemPosition : selection) {
							if (itemPosition == null) {
								return;
							}
							FieldAutoUpdateList list = itemPosition
									.getContainingList();
							int index = itemPosition.getIndex();
							list.remove(index);
							if (itemPosition.getContainingListType()
									.isOrdered() && (index > 0)) {
								toPostSelect.add(itemPosition
										.getSibling(index - 1));
							} else {
								toPostSelect.add(itemPosition
										.getParentItemPosition());
							}
						}
						refreshStructure();
						setSelection(toPostSelect);
					}
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected class GhostItemPosition extends ItemPosition {

		private Object item;

		public GhostItemPosition(ItemPosition itemPosition, Object item) {
			super(itemPosition.getContainingListField(), itemPosition
					.getParentItemPosition(), itemPosition.getIndex());
			this.item = item;
		}

		@Override
		public Object getItem() {
			return item;
		}

		@Override
		public FieldAutoUpdateList getContainingList() {
			return new FieldAutoUpdateList(this) {

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

	protected void createInsertButton(JPanel buttonsPanel,
			final InsertPosition insertPosition) {
		final ItemPosition itemPosition;
		ItemPosition singleSelection = getSingleSelection();
		final int index;
		if (singleSelection == null) {
			index = getRootList().size();
			itemPosition = new ItemPosition(field, null, index);
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
		final JButton button = new JButton(
				reflectionUI.translateUIString(buttonText));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ITypeInfo typeToInstanciate = itemType;
					if (typeToInstanciate == null) {
						typeToInstanciate = reflectionUI
								.getTypeInfo(new JavaTypeInfoSource(
										Object.class));
					}
					Object newItem = reflectionUI.onTypeInstanciationRequest(
							button, typeToInstanciate, false);
					if (newItem == null) {
						return;
					}
					GhostItemPosition futureItemPosition = new GhostItemPosition(
							itemPosition, newItem);
					if (openDetailsDialog(futureItemPosition)) {
						newItem = futureItemPosition.getItem();
						FieldAutoUpdateList list = itemPosition
								.getContainingList();
						list.add(index, newItem);
						refreshStructure();
						ItemPosition toSelect = itemPosition;
						if (!listType.isOrdered()) {
							int indexToSelect = list.indexOf(newItem);
							toSelect = itemPosition.getSibling(indexToSelect);
						}
						setSingleSelection(toSelect);
					}
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected void createAddButton(JPanel buttonsPanel) {
		final ItemPosition itemPosition = getSingleSelection();
		final ItemPosition subItemPosition;
		if (itemPosition == null) {
			subItemPosition = getRootListItemPosition();
		} else {
			subItemPosition = getSubItemPosition(itemPosition);
		}
		final IListTypeInfo subListType = subItemPosition
				.getContainingListType();
		final ITypeInfo subListItemType = subListType.getItemType();
		final JButton button = new JButton(
				reflectionUI.translateUIString((subListItemType == null) ? "Add..."
						: ("Add " + subListItemType.getCaption() + "...")));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ITypeInfo typeToInstanciate = subListItemType;
					if (typeToInstanciate == null) {
						typeToInstanciate = new DefaultTypeInfo(reflectionUI,
								Object.class);
					}
					Object newSubListItem = reflectionUI
							.onTypeInstanciationRequest(button,
									typeToInstanciate, false);
					if (newSubListItem == null) {
						return;
					}

					GhostItemPosition futureSubItemPosition = new GhostItemPosition(
							subItemPosition, newSubListItem);
					if (openDetailsDialog(futureSubItemPosition)) {
						newSubListItem = futureSubItemPosition.getItem();
						FieldAutoUpdateList subList = new ItemPosition(
								subItemPosition.getContainingListField(),
								itemPosition, -1).getContainingList();
						int newSubListItemIndex = subList.size();
						subList.add(newSubListItemIndex, newSubListItem);
						refreshStructure();
						if (!subListType.isOrdered()) {
							newSubListItemIndex = subList
									.indexOf(newSubListItem);
						}
						ItemPosition toSelect = subItemPosition
								.getSibling(newSubListItemIndex);
						setSingleSelection(toSelect);
					}
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected void createCopyButton(JPanel buttonsPanel) {
		final JButton button = new JButton(
				reflectionUI.translateUIString("Copy"));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					clipboard.clear();
					List<ItemPosition> selection = getSelection();
					for (ItemPosition itemPosition : selection) {
						if (itemPosition == null) {
							return;
						}
						clipboard.add(reflectionUI.copy(itemPosition.getItem()));
					}
					updateButtonsPanel();
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected void createCutButton(JPanel buttonsPanel) {
		final JButton button = new JButton(
				reflectionUI.translateUIString("Cut"));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					clipboard.clear();
					List<ItemPosition> selection = getSelection();
					selection = new ArrayList<ItemPosition>(selection);
					Collections.reverse(selection);
					List<ItemPosition> toPostSelect = new ArrayList<ListControl.ItemPosition>();
					for (ItemPosition itemPosition : selection) {
						if (itemPosition == null) {
							return;
						}
						clipboard.add(0,
								reflectionUI.copy(itemPosition.getItem()));
						FieldAutoUpdateList list = itemPosition
								.getContainingList();
						int index = itemPosition.getIndex();
						list.remove(index);
						if (itemPosition.getContainingListType().isOrdered()
								&& (index > 0)) {
							toPostSelect.add(itemPosition.getSibling(index - 1));
						} else {
							toPostSelect.add(itemPosition
									.getParentItemPosition());
						}
					}
					refreshStructure();
					setSelection(toPostSelect);
					updateButtonsPanel();
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected void createPasteButton(JPanel buttonsPanel,
			final InsertPosition insertPosition) {
		String buttonText;
		if (insertPosition == InsertPosition.AFTER) {
			buttonText = "Paste After";
		} else if (insertPosition == InsertPosition.BEFORE) {
			buttonText = "Paste Before";
		} else {
			buttonText = "Paste";
		}
		final JButton button = new JButton(
				reflectionUI.translateUIString(buttonText));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ItemPosition itemPosition = getSingleSelection();
					int index;
					if (itemPosition == null) {
						index = getRootList().size();
						itemPosition = new ItemPosition(field, null, index);
					} else {
						index = itemPosition.getIndex();
						if (insertPosition == InsertPosition.AFTER) {
							index++;
						}
					}
					int initialIndex = index;
					FieldAutoUpdateList list = itemPosition.getContainingList();
					for (Object clipboardItyem : clipboard) {
						list.add(index, clipboardItyem);
						index++;
					}
					refreshStructure();
					List<ItemPosition> toPostSelect = new ArrayList<ListControl.ItemPosition>();
					IListTypeInfo listType = itemPosition
							.getContainingListType();
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
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected void createPasteInButton(JPanel buttonsPanel) {
		final JButton button = new JButton(
				reflectionUI.translateUIString("Paste"));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ItemPosition itemPosition = getSingleSelection();
					if (itemPosition == null) {
						return;
					}
					ItemPosition subItemPosition = getSubItemPosition(itemPosition);
					FieldAutoUpdateList subList = subItemPosition
							.getContainingList();
					int newSubListItemIndex = subList.size();
					int newSubListItemInitialIndex = newSubListItemIndex;
					subItemPosition = subItemPosition
							.getSibling(newSubListItemIndex);
					for (Object clipboardItyem : clipboard) {
						subList.add(newSubListItemIndex, clipboardItyem);
						newSubListItemIndex++;
					}
					refreshStructure();
					List<ItemPosition> toPostSelect = new ArrayList<ListControl.ItemPosition>();
					IListTypeInfo subListType = subItemPosition
							.getContainingListType();
					newSubListItemIndex = newSubListItemInitialIndex;
					for (int i = 0; i < clipboard.size(); i++) {
						Object clipboardItem = clipboard.get(i);
						if (subListType.isOrdered()) {
							newSubListItemInitialIndex = newSubListItemInitialIndex
									+ i;
						} else {
							newSubListItemInitialIndex = subList
									.indexOf(clipboardItem);
						}
						if (newSubListItemInitialIndex != -1) {
							toPostSelect.add(subItemPosition
									.getSibling(newSubListItemInitialIndex));
						}
					}
					setSelection(toPostSelect);
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected ITypeInfo getRootListItemType() {
		return getRootListType().getItemType();
	}

	protected IListTypeInfo getRootListType() {
		return (IListTypeInfo) field.getType();
	}

	protected void createOpenItemButton(JPanel buttonsPanel) {
		final JButton button = new JButton(
				reflectionUI.translateUIString("Open"));
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ItemPosition itemPosition = getSingleSelection();
					if (itemPosition == null) {
						return;
					}
					onOpenDetaildsDialogRequest(itemPosition);
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(button, t);
				}
			}
		});
	}

	protected void updateButtonsPanelOnItemSelection() {
		treeTableComponent
				.addTreeSelectionListener(new TreeSelectionListener() {

					@Override
					public void valueChanged(TreeSelectionEvent e) {
						try {
							updateButtonsPanel();
						} catch (Throwable t) {
							reflectionUI.handleExceptionsFromDisplayedUI(
									treeTableComponent, t);
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
					TreePath path = treeTableComponent.getPathForLocation(
							me.getX(), me.getY());
					if (path == null) {
						return;
					}
					ItemNode selectedNode = (ItemNode) path
							.getLastPathComponent();
					ItemPosition itemPosition = (ItemPosition) selectedNode
							.getUserObject();
					onOpenDetaildsDialogRequest(itemPosition);
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(
							treeTableComponent, t);
				}
			}
		});
	}

	protected void onOpenDetaildsDialogRequest(ItemPosition itemPosition) {
		Object value = itemPosition.getItem();
		GhostItemPosition ghostItemPosition = new GhostItemPosition(
				itemPosition, value);
		boolean[] changeDetectedArray = new boolean[] { false };
		if (openDetailsDialog(ghostItemPosition, changeDetectedArray, true)) {
			FieldAutoUpdateList list = itemPosition.getContainingList();
			Object newValue = ghostItemPosition.getItem();
			int index = itemPosition.getIndex();
			Object newvalue = ghostItemPosition.getItem();
			if (!changeDetectedArray[0]) {
				return;
			}
			list.set(index, newvalue);
			refreshStructure();
			ItemPosition itemPositionToSelect = itemPosition;
			if (!itemPosition.getContainingListType().isOrdered()) {
				int newIndex = list.indexOf(newValue);
				itemPositionToSelect = itemPositionToSelect
						.getSibling(newIndex);
			}
			setSingleSelection(itemPositionToSelect);
		}
	}

	protected FieldAutoUpdateList getRootList() {
		return getRootListItemPosition().getContainingList();
	}

	protected ItemPosition getRootListItemPosition() {
		return new ItemPosition(field, null, -1);
	}

	protected boolean openDetailsDialog(final ItemPosition itemPosition) {
		return openDetailsDialog(itemPosition, new boolean[1], false);
	}

	protected boolean openDetailsDialog(final ItemPosition itemPosition,
			boolean[] changeDetectedArray, boolean recordModifications) {
		ItemNode itemNode = findNode(itemPosition);
		if (itemNode != null) {
			TreePath treePath = new TreePath(itemNode.getPath());
			treeTableComponent.expandPath(treePath);
		}

		if (!hasItemDetails(itemPosition)) {
			return true;
		}

		final FieldAutoUpdateList list = itemPosition.getContainingList();
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
			parentStack = ReflectionUIUtils.findModificationStack(
					ListControl.this, reflectionUI);
		} else {
			parentStack = null;
		}
		String title = reflectionUI.getFieldTitle(object, new FieldInfoProxy(
				itemPosition.getContainingListField()) {
			@Override
			public String getCaption() {
				String result = itemPosition.getContainingListField()
						.getCaption() + " Item";
				if (itemPosition.getParentItemPosition() != null) {
					ItemPosition parentItempPosition = itemPosition
							.getParentItemPosition();
					ITypeInfo prentItemType = reflectionUI
							.getTypeInfo(reflectionUI
									.getTypeInfoSource(parentItempPosition
											.getItem()));
					result = reflectionUI.composeTitle(
							prentItemType.getCaption(), result);
				}
				return result;
			}

			@Override
			public Object getValue(Object object) {
				return list.get(index);
			}
		});
		IInfoCollectionSettings settings = new InfoCollectionSettingsProxy(
				getStructuralInfo().getItemInfoSettings(itemPosition)) {
			@Override
			public boolean allReadOnly() {
				if (itemPosition.getContainingListField().isReadOnly()) {
					return true;
				} else {
					return super.allReadOnly();
				}
			}
		};
		return reflectionUI.openValueDialog(treeTableComponent, valueAccessor,
				settings, parentStack, title, changeDetectedArray);
	}

	protected boolean hasItemDetails(ItemPosition itemPosition) {
		Object item = itemPosition.getItem();
		if (item == null) {
			return false;
		}
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(item));
		if (actualItemType.hasCustomFieldControl()) {
			return true;
		}
		List<IFieldInfo> fields = actualItemType.getFields();
		List<IMethodInfo> methods = actualItemType.getMethods();

		IListStructuralInfo structuralInfo = getStructuralInfo();
		if (structuralInfo != null) {
			IInfoCollectionSettings infoSettings = structuralInfo
					.getItemInfoSettings(itemPosition);

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
	}

	public void visitItems(IItemsVisitor iItemsVisitor) {
		visitItems(iItemsVisitor, getRootListItemPosition());
	}

	private void visitItems(IItemsVisitor iItemsVisitor,
			ItemPosition currentListItemPosition) {
		FieldAutoUpdateList list = currentListItemPosition.getContainingList();
		for (int i = 0; i < list.size(); i++) {
			currentListItemPosition = currentListItemPosition.getSibling(i);
			iItemsVisitor.visitItem(currentListItemPosition);
			IListStructuralInfo treeInfo = getStructuralInfo();
			if (treeInfo != null) {
				IFieldInfo childrenField = treeInfo
						.getItemSubListField(currentListItemPosition);
				if (childrenField != null) {
					if (childrenField.getValue(currentListItemPosition
							.getItem()) != null) {
						visitItems(iItemsVisitor, new ItemPosition(
								childrenField, currentListItemPosition, -1));
					}
				}

			}
		}
	}

	public boolean refreshUI() {
		List<ItemPosition> lastlySelectedItemPositions = getSelection();
		List<Object> lastlySelectedItems = new ArrayList<Object>();
		for (int i = 0; i < lastlySelectedItemPositions.size(); i++) {
			ItemPosition lastlySelectedItemPosition = lastlySelectedItemPositions
					.get(i);
			lastlySelectedItems.add(lastlySelectedItemPosition.getItem());
		}

		refreshStructure();

		for (int i = 0; i < lastlySelectedItemPositions.size(); i++) {
			ItemPosition lastlySelectedItemPosition = lastlySelectedItemPositions
					.get(i);
			if (!lastlySelectedItemPosition.getContainingListType().isOrdered()) {
				Object lastlySelectedItem = lastlySelectedItems.get(i);
				int index = lastlySelectedItemPosition.getContainingList()
						.indexOf(lastlySelectedItem);
				lastlySelectedItemPosition = lastlySelectedItemPosition
						.getSibling(index);
				lastlySelectedItemPositions.set(i, lastlySelectedItemPosition);
			}
		}
		setSelection(lastlySelectedItemPositions);
		return true;
	}

	protected class ItemNode extends DefaultMutableTreeNode {

		protected static final long serialVersionUID = 1L;
		protected IFieldInfo childrenField;
		protected ItemPosition currentItemPosition;
		protected boolean childrenLoaded = false;;

		public ItemNode(IFieldInfo childrenField,
				ItemPosition currentItemPosition) {
			this.childrenField = childrenField;
			this.currentItemPosition = currentItemPosition;
			setUserObject(currentItemPosition);
		}

		protected void ensureChildrenAreLoaded() {
			if (childrenLoaded) {
				return;
			}
			childrenLoaded = true;
			if (childrenField == null) {
				return;
			}
			ItemPosition anyChildItemPosition = new ItemPosition(childrenField,
					currentItemPosition, -1);
			FieldAutoUpdateList list = anyChildItemPosition.getContainingList();
			for (int i = 0; i < list.size(); i++) {
				ItemPosition childItemPosition = anyChildItemPosition
						.getSibling(i);
				IFieldInfo grandChildrenField = null;
				IListStructuralInfo treeInfo = getStructuralInfo();
				if (treeInfo != null) {
					grandChildrenField = treeInfo
							.getItemSubListField(childItemPosition);
					if (grandChildrenField != null) {
						if (grandChildrenField.getValue(childItemPosition
								.getItem()) == null) {
							grandChildrenField = null;
						}
					}
				}
				ItemNode node = new ItemNode(grandChildrenField,
						childItemPosition);
				super.insert(node, i);
			}
		}

		@Override
		public int getIndex(TreeNode aChild) {
			ensureChildrenAreLoaded();
			return super.getIndex(aChild);
		}

		@Override
		public TreeNode getChildAt(int index) {
			ensureChildrenAreLoaded();
			return super.getChildAt(index);
		}

		@Override
		public int getChildCount() {
			ensureChildrenAreLoaded();
			return super.getChildCount();
		}

		@Override
		public void insert(MutableTreeNode newChild, int childIndex) {
			ensureChildrenAreLoaded();
			super.insert(newChild, childIndex);
		}

		@Override
		public void remove(int childIndex) {
			ensureChildrenAreLoaded();
			super.remove(childIndex);
		}

		@Override
		public Enumeration children() {
			ensureChildrenAreLoaded();
			return super.children();
		}

		@Override
		public void remove(MutableTreeNode aChild) {
			ensureChildrenAreLoaded();
			super.remove(aChild);
		}

		@Override
		public void add(MutableTreeNode newChild) {
			ensureChildrenAreLoaded();
			super.add(newChild);
		}

		@Override
		public boolean isNodeDescendant(DefaultMutableTreeNode anotherNode) {
			ensureChildrenAreLoaded();
			return super.isNodeDescendant(anotherNode);
		}

		@Override
		public boolean isNodeRelated(DefaultMutableTreeNode aNode) {
			ensureChildrenAreLoaded();
			return super.isNodeRelated(aNode);
		}

		@Override
		public int getDepth() {
			ensureChildrenAreLoaded();
			return super.getDepth();
		}

		@Override
		public DefaultMutableTreeNode getNextNode() {
			ensureChildrenAreLoaded();
			return super.getNextNode();
		}

		@Override
		public DefaultMutableTreeNode getPreviousNode() {
			ensureChildrenAreLoaded();
			return super.getPreviousNode();
		}

		@Override
		public Enumeration preorderEnumeration() {
			ensureChildrenAreLoaded();
			return super.preorderEnumeration();
		}

		@Override
		public Enumeration postorderEnumeration() {
			ensureChildrenAreLoaded();
			return super.postorderEnumeration();
		}

		@Override
		public Enumeration breadthFirstEnumeration() {
			ensureChildrenAreLoaded();
			return super.breadthFirstEnumeration();
		}

		@Override
		public Enumeration depthFirstEnumeration() {
			ensureChildrenAreLoaded();
			return super.depthFirstEnumeration();
		}

		@Override
		public boolean isNodeChild(TreeNode aNode) {
			ensureChildrenAreLoaded();
			return super.isNodeChild(aNode);
		}

		@Override
		public TreeNode getFirstChild() {
			ensureChildrenAreLoaded();
			return super.getFirstChild();
		}

		@Override
		public TreeNode getLastChild() {
			ensureChildrenAreLoaded();
			return super.getLastChild();
		}

		@Override
		public TreeNode getChildAfter(TreeNode aChild) {
			ensureChildrenAreLoaded();
			return super.getChildAfter(aChild);
		}

		@Override
		public TreeNode getChildBefore(TreeNode aChild) {
			ensureChildrenAreLoaded();
			return super.getChildBefore(aChild);
		}

		@Override
		public boolean isLeaf() {
			ensureChildrenAreLoaded();
			return super.isLeaf();
		}

		@Override
		public DefaultMutableTreeNode getFirstLeaf() {
			ensureChildrenAreLoaded();
			return super.getFirstLeaf();
		}

		@Override
		public DefaultMutableTreeNode getLastLeaf() {
			ensureChildrenAreLoaded();
			return super.getLastLeaf();
		}

		@Override
		public DefaultMutableTreeNode getNextLeaf() {
			ensureChildrenAreLoaded();
			return super.getNextLeaf();
		}

		@Override
		public DefaultMutableTreeNode getPreviousLeaf() {
			ensureChildrenAreLoaded();
			return super.getPreviousLeaf();
		}

		@Override
		public int getLeafCount() {
			ensureChildrenAreLoaded();
			return super.getLeafCount();
		}

		@Override
		public String toString() {
			ensureChildrenAreLoaded();
			return super.toString();
		}

		@Override
		public Object clone() {
			ensureChildrenAreLoaded();
			return super.clone();
		}

		@Override
		public int hashCode() {
			ensureChildrenAreLoaded();
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			ensureChildrenAreLoaded();
			return super.equals(obj);
		}

	}

	public class ItemPosition implements IItemPosition {
		protected IFieldInfo containingListField;
		protected ItemPosition parentItemPosition;
		protected int index;

		public ItemPosition(IFieldInfo containingListField,
				ItemPosition parentItemPosition, int index) {
			this.containingListField = containingListField;
			this.parentItemPosition = parentItemPosition;
			this.index = index;
		}

		public boolean supportsValue(Object clipboard) {
			ITypeInfo itemType = getContainingListType().getItemType();
			return (itemType == null) || (itemType.supportsValue(clipboard));
		}

		@Override
		public boolean isContainingListReadOnly() {
			if (containingListField.isReadOnly()) {
				return true;
			}
			if (parentItemPosition == null) {
				return false;
			}
			return getRootListItemPosition().isContainingListReadOnly();
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public Object getItem() {
			FieldAutoUpdateList list = getContainingList();
			if (index < 0) {
				return null;
			}
			if (index >= list.size()) {
				return null;
			}
			return list.get(index);
		}

		@Override
		public IFieldInfo getContainingListField() {
			return containingListField;
		}

		@Override
		public FieldAutoUpdateList getContainingList() {
			return new FieldAutoUpdateList(this);
		}

		@Override
		public IListTypeInfo getContainingListType() {
			return (IListTypeInfo) getContainingListField().getType();
		}

		@Override
		public ItemPosition getParentItemPosition() {
			return parentItemPosition;
		}

		@Override
		public String toString() {
			return "Item(depth=" + getDepth() + ", position=" + index
					+ ", value=" + getItem() + ")";
		}

		@Override
		public int getDepth() {
			int result = 0;
			ItemPosition current = this;
			while (current.parentItemPosition != null) {
				current = current.parentItemPosition;
				result++;
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ItemPosition)) {
				return false;
			}
			ItemPosition other = (ItemPosition) obj;
			if (!ReflectionUIUtils.equalsOrBothNull(parentItemPosition,
					other.parentItemPosition)) {
				return false;
			}
			if (index != other.index) {
				return false;
			}
			return true;
		}

		@Override
		public Object getContainingListOwner() {
			if (parentItemPosition != null) {
				return parentItemPosition.getItem();
			} else {
				return object;
			}

		}

		@Override
		public ItemPosition getSibling(int index2) {
			return new ItemPosition(containingListField, parentItemPosition,
					index2);
		}

		@Override
		public boolean isRootListItemPosition() {
			return getRootListItemPosition() == this;
		}

		@Override
		public ItemPosition getRootListItemPosition() {
			ItemPosition current = this;
			while (current.parentItemPosition != null) {
				current = current.parentItemPosition;
			}
			return current;
		}

	}

	protected class SetListValueModification implements
			ModificationStack.IModification {
		protected List standardListValue;
		private Object listOwner;
		private IFieldInfo listField;

		public SetListValueModification(List standardListValue,
				Object listOwner, IFieldInfo listField) {
			this.standardListValue = standardListValue;
			this.listOwner = listOwner;
			this.listField = listField;
		}

		@Override
		public int getNumberOfUnits() {
			return 1;
		}

		@Override
		public IModification applyAndGetOpposite(boolean refreshView) {
			IListTypeInfo listType = (IListTypeInfo) listField.getType();
			List lastStandardListValue = listType.toStandardList(listField
					.getValue(listOwner));

			Object listFieldValue = listType
					.fromStandardList(standardListValue);
			listField.setValue(listOwner, listFieldValue);

			final SetListValueModification currentModif = this;
			return new SetListValueModification(lastStandardListValue,
					listOwner, listField) {
				@Override
				public String getTitle() {
					return ModificationStack.getUndoTitle(currentModif
							.getTitle());
				}
			};
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "Edit '" + listField.getCaption() + "' of '"
					+ reflectionUI.getObjectKind(listOwner) + "'";
		}

	}

	protected class ChangeListSelectionModification implements
			ModificationStack.IModification {
		protected List<ItemPosition> toSelect;
		private List<ItemPosition> undoSelection;

		public ChangeListSelectionModification(List<ItemPosition> toSelect,
				List<ItemPosition> undoSelection) {
			this.toSelect = toSelect;
			this.undoSelection = undoSelection;
		}

		@Override
		public IModification applyAndGetOpposite(boolean refreshView) {
			List<ItemPosition> oppositeSelection;
			if (undoSelection != null) {
				oppositeSelection = undoSelection;
			} else {
				oppositeSelection = getSelection();
			}

			if (toSelect != null) {
				setSelection(toSelect);
			}

			return new ChangeListSelectionModification(oppositeSelection,
					toSelect);
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

	protected class FieldAutoUpdateList extends AbstractList {
		protected ItemPosition itemPosition;

		public FieldAutoUpdateList(ItemPosition itemPosition) {
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

		protected List getUnderlyingList() {
			Object listFieldValue = getListField().getValue(getListOwner());
			if (listFieldValue == null) {
				return null;
			}
			return getListType().toStandardList(listFieldValue);
		}

		protected void replaceUnderlyingListWith(List standardListValue) {
			ModificationStack modifStack = ReflectionUIUtils
					.findModificationStack(ListControl.this, reflectionUI);
			Object listOwner = getListOwner();
			if (!getListField().isReadOnly()) {
				SetListValueModification modif = new SetListValueModification(
						standardListValue, listOwner, getListField());
				if (itemPosition.isRootListItemPosition()) {
					modif.applyAndGetOpposite(false);
				} else {
					modifStack.apply(modif, false);
				}
			}
			if (!itemPosition.isRootListItemPosition()) {
				ItemPosition listOwnerPosition = itemPosition
						.getParentItemPosition();
				new FieldAutoUpdateList(listOwnerPosition).set(
						listOwnerPosition.getIndex(), listOwner);
			}
		}

		protected void beginModification() {
			ModificationStack modifStack = ReflectionUIUtils
					.findModificationStack(ListControl.this, reflectionUI);
			modifStack.beginComposite();
		}

		protected void endModification(String title) {
			if (itemPosition.isRootListItemPosition()) {
				restoreSelectionAfterUndoOrRedo();
			}
			ModificationStack modifStack = ReflectionUIUtils
					.findModificationStack(ListControl.this, reflectionUI);
			modifStack.endComposite(title, ModificationStack.Order.FIFO);
		}

		@Override
		public Object get(int index) {
			return getUnderlyingList().get(index);
		}

		@Override
		public int size() {
			List underlyingList = getUnderlyingList();
			if (underlyingList == null) {
				return 0;
			}
			return underlyingList.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void add(int index, Object element) {
			List tmpList = new ArrayList(getUnderlyingList());
			tmpList.add(index, element);
			beginModification();
			replaceUnderlyingListWith(tmpList);
			endModification("Add item to '" + getListField().getCaption() + "'");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object remove(int index) {
			List tmpList = new ArrayList(getUnderlyingList());
			Object result = tmpList.remove(index);
			beginModification();
			replaceUnderlyingListWith(tmpList);
			endModification("Remove item from '" + getListField().getCaption()
					+ "'");
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object set(int index, Object element) {
			List tmpList = new ArrayList(getUnderlyingList());
			Object result = tmpList.set(index, element);
			beginModification();
			replaceUnderlyingListWith(tmpList);
			endModification("Update '" + getListField().getCaption()
					+ "' item " + index);
			return result;
		}

		@SuppressWarnings("unchecked")
		public void move(int index, int offset) {
			List tmpList = new ArrayList(getUnderlyingList());
			tmpList.add(index + offset, tmpList.remove(index));
			beginModification();
			replaceUnderlyingListWith(tmpList);
			endModification("Move '" + getListField().getCaption() + "' item");
		}

		@Override
		public void clear() {
			beginModification();
			replaceUnderlyingListWith(Collections.emptyList());
			endModification("Clear '" + getListField().getCaption() + "'");
		}

		protected void restoreSelectionAfterUndoOrRedo() {
			ModificationStack modifStack = ReflectionUIUtils
					.findModificationStack(ListControl.this, reflectionUI);
			modifStack.pushUndo(new ChangeListSelectionModification(
					getSelection(), null));
		}

	}

	protected class ItemCellRenderer implements TableCellRenderer,
			TreeCellRenderer {

		DefaultTreeCellRenderer defaultTreeCellRenderer = new DefaultTreeCellRenderer();
		DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean isLeaf, int row,
				boolean focused) {
			JLabel label = (JLabel) defaultTreeCellRenderer
					.getTreeCellRendererComponent(tree, value, selected,
							expanded, isLeaf, row, focused);
			customizeComponent(label, (ItemNode) value, row, 0, selected,
					focused);
			return label;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JLabel label = (JLabel) defaultTableCellRenderer
					.getTableCellRendererComponent(table, value, isSelected,
							hasFocus, row, column);
			row = treeTableComponent.convertRowIndexToModel(row);
			TreePath path = treeTableComponent.getPathForRow(row);
			ItemNode node = (ItemNode) path.getLastPathComponent();
			customizeComponent(label, node, row, column, isSelected, hasFocus);
			return label;
		}

		protected void customizeComponent(JLabel label, ItemNode node,
				int rowIndex, int columnIndex, boolean isSelected,
				boolean hasFocus) {
			if (!(node.getUserObject() instanceof ItemPosition)) {
				return;
			}
			ItemPosition itemPosition = (ItemPosition) node.getUserObject();
			Object item = itemPosition.getItem();
			String text = getCellValue(node, columnIndex);
			if (text == null) {
				label.setText("");
			} else {
				label.setText(reflectionUI.translateUIString(text));
			}

			if (columnIndex == 0) {
				Image imageIcon = reflectionUI.getObjectIconImage(item);
				if (imageIcon == null) {
					label.setIcon(null);
				} else {
					label.setIcon(new ImageIcon(imageIcon));
				}
			} else {
				label.setIcon(null);
			}
		}
	}

	public interface IItemsVisitor {

		void visitItem(ItemPosition itemPosition);

	}

	private enum InsertPosition {
		AFTER, BEFORE, INDERTERMINATE
	}

}
