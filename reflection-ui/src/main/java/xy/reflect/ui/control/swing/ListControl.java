
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
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
import javax.swing.JDialog;
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

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.AbstractFieldControlData;
import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.builder.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.AbstractLazyTreeNode;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.ControlSplitPane;
import xy.reflect.ui.control.swing.util.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.swing.util.ScrollPaneOptions;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.DelegatingInfoFilter;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.InitialItemValueCreationOption;
import xy.reflect.ui.info.type.iterable.item.AbstractBufferedItemPositionFactory;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemDetailsAreaPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.BufferedListModificationFactory;
import xy.reflect.ui.undo.CompositeModification;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.undo.MethodControlDataModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays a tree table. Compatible with
 * {@link IListTypeInfo}.
 * 
 * @author olitank
 *
 */
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

	protected List<Listener<List<BufferedItemPosition>>> selectionListeners = new ArrayList<Listener<List<BufferedItemPosition>>>();
	protected boolean selectionListenersEnabled = true;
	protected IFieldControlInput input;
	protected Listener<Throwable> refreshingErrorHandler;

	protected static AbstractAction SEPARATOR_ACTION = new AbstractAction("") {
		protected static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};
	protected boolean initialized = false;

	public ListControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		input = new FieldControlInputProxy(input) {
			ErrorHandlingFieldControlData errorHandlingFieldControlData = new ErrorHandlingFieldControlData(
					super.getControlData(), swingRenderer, ListControl.this) {
				{
					refreshingErrorHandler = new Listener<Throwable>() {
						@Override
						public void handle(Throwable t) {
							handleError(t);
						}
					};
				}
			};

			@Override
			public IFieldControlData getControlData() {
				return errorHandlingFieldControlData;
			}
		};
		this.input = input;
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
		if (getRootListSize() > 0) {
			setSingleSelection(getRootListItemPosition(0));
		}
		this.initialized = true;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (initialized) {
			refreshUI(true);
		}
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
			public void handle(List<BufferedItemPosition> newSelection) {
				if (!getDetailsAccessMode().hasEmbeddedDetailsDisplayArea()) {
					return;
				}
				if (newSelection.size() == 1) {
					if (newSelection.get(0).equals(detailsControlItemPosition)) {
						return;
					}
				} else {
					if (detailsControlItemPosition == null) {
						return;
					}
				}
				updateDetailsArea(false);
			}
		});
	}

	protected void layoutControls() {
		setLayout(new BorderLayout());
		if (getDetailsAccessMode().hasEmbeddedDetailsDisplayArea()) {
			JPanel listAndToolbarPanel = new ControlPanel();
			listAndToolbarPanel.setLayout(new BorderLayout());
			listAndToolbarPanel.add(BorderLayout.CENTER, treeTableComponentScrollPane);
			listAndToolbarPanel.add(toolbar, BorderLayout.EAST);
			ControlScrollPane listAndToolbarScrollPane = createTreeTableAndToolBarScrollPane(listAndToolbarPanel);
			SwingRendererUtils.removeScrollPaneBorder(listAndToolbarScrollPane);
			ControlScrollPane detailsAreaScrollPane = createDetailsAreaScrollPane(detailsArea);
			final JSplitPane splitPane = new ControlSplitPane();
			add(splitPane, BorderLayout.CENTER);
			final double dividerLocation;
			if (getDetailsAccessMode().getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.RIGHT) {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setLeftComponent(listAndToolbarScrollPane);
				splitPane.setRightComponent(detailsAreaScrollPane);
				dividerLocation = 1.0 - getDetailsAccessMode().getDefaultEmbeddedDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.LEFT) {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
				splitPane.setRightComponent(listAndToolbarScrollPane);
				splitPane.setLeftComponent(detailsAreaScrollPane);
				dividerLocation = getDetailsAccessMode().getDefaultEmbeddedDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.BOTTOM) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane.setTopComponent(listAndToolbarScrollPane);
				splitPane.setBottomComponent(detailsAreaScrollPane);
				dividerLocation = 1.0 - getDetailsAccessMode().getDefaultEmbeddedDetailsAreaOccupationRatio();
			} else if (getDetailsAccessMode().getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.TOP) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane.setBottomComponent(listAndToolbarScrollPane);
				splitPane.setTopComponent(detailsAreaScrollPane);
				dividerLocation = getDetailsAccessMode().getDefaultEmbeddedDetailsAreaOccupationRatio();
			} else {
				throw new ReflectionUIError();
			}
			SwingRendererUtils.setSafelyDividerLocation(splitPane, dividerLocation);
			splitPane.setResizeWeight(dividerLocation);
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

		if (getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
			AbstractStandardListAction openAction = createOpenItemAction();
			if (openAction.isValid()) {
				toolbar.add(createTool(null, SwingRendererUtils.DETAILS_ICON, true, false, openAction));
			}
		}

		AbstractStandardListAction addChildAction = createAddChildAction();
		AbstractStandardListAction insertAction = createInsertAction(InsertPosition.UNKNOWN);
		AbstractStandardListAction insertActionBefore = createInsertAction(InsertPosition.BEFORE);
		AbstractStandardListAction insertActionAfter = createInsertAction(InsertPosition.AFTER);
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
				AbstractStandardListAction dynamicPropertyHook = createDynamicPropertyHook(listProperty);
				toolbar.add(createTool((String) dynamicPropertyHook.getActionTitle(), null, true, false,
						dynamicPropertyHook));
			}
			for (IDynamicListAction listAction : dynamicActions) {
				AbstractStandardListAction dynamicActionHook = createDynamicActionHook(listAction);
				toolbar.add(createTool((String) dynamicActionHook.getActionTitle(),
						(Icon) dynamicActionHook.getValue(AbstractAction.LARGE_ICON_KEY), true, false,
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

	protected JButton createTool(final String caption, final Icon icon, boolean alwawsShowIcon,
			final boolean alwawsShowMenu, AbstractStandardListAction... actions) {
		final List<AbstractStandardListAction> actionsToPresent = new ArrayList<AbstractStandardListAction>();
		for (final AbstractStandardListAction action : actions) {
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
			public Icon retrieveIcon() {
				return icon;
			}

			@Override
			public String retrieveText() {
				return swingRenderer.prepareMessageToDisplay(caption);
			}

			@Override
			public String retrieveToolTipText() {
				if (actionsToPresent.size() > 0) {
					if (actionsToPresent.size() == 1) {
						String tooltipText = (String) actionsToPresent.get(0).getActionDescription();
						if (tooltipText == null) {
							tooltipText = (String) actionsToPresent.get(0).getActionTitle();
						}
						return swingRenderer.prepareMessageToDisplay(tooltipText);
					} else if (actionsToPresent.size() > 1) {
						StringBuilder tooltipTextBuilder = new StringBuilder();
						boolean firstAction = true;
						for (AbstractStandardListAction action : actionsToPresent) {
							if (!firstAction) {
								tooltipTextBuilder.append("\nor\n");
							}
							String itemTooltipText = (String) action.getActionDescription();
							if (itemTooltipText == null) {
								itemTooltipText = (String) action.getActionTitle();
							}
							tooltipTextBuilder.append(itemTooltipText);
							firstAction = false;
						}
						return swingRenderer.prepareMessageToDisplay(tooltipTextBuilder.toString());
					} else {
						return null;
					}
				} else {
					return null;
				}
			}

		};

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
				adjustSelection(e);
				popupMenu(e);
			}

			void popupMenu(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) {
					return;
				}
				if (e.getComponent() == treeTableComponent) {
					JPopupMenu popup = createPopupMenu();
					popup.show(e.getComponent(), e.getX(), e.getY());
					return;
				}
			}

			void adjustSelection(MouseEvent e) {
				int row = treeTableComponent.rowAtPoint(e.getPoint());
				if (treeTableComponent.isRowSelected(row)) {
					return;
				}
				if ((row < 0) || (row >= treeTableComponent.getRowCount())) {
					treeTableComponent.clearSelection();
					return;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					treeTableComponent.setRowSelectionInterval(row, row);
					return;
				}
			}
		});
	}

	protected JPopupMenu createPopupMenu() {
		JPopupMenu result = new JPopupMenu();
		for (Action action : getCurrentSelectionActions()) {
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

	public IListStructuralInfo getRootStructuralInfo() {
		return getStructuralInfo(getRootListItemPosition(-1));
	}

	public IListStructuralInfo getStructuralInfo(BufferedItemPosition itemPosition) {
		if (structuralInfo == null) {
			IListTypeInfo listType = itemPosition.getContainingListType();
			structuralInfo = listType.getStructuralInfo();
			if (structuralInfo == null) {
				throw new ReflectionUIError("No " + IListStructuralInfo.class.getSimpleName() + " found on the type '"
						+ listType.getName() + "'");
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
		treeTableComponent = createTreeTable();
		treeTableComponentScrollPane = createTreeTableScrollPane(treeTableComponent);
		treeTableComponent.setExpandsSelectedPaths(true);
		treeTableComponent.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		treeTableComponent.setRootVisible(false);
		treeTableComponent.setShowsRootHandles(true);
		treeTableComponent.setDefaultRenderer(Object.class, createTableCellRenderer());
		treeTableComponent.setTreeCellRenderer(createTreeCellRenderer());
		treeTableComponent.setColumnMargin(5);
		treeTableComponent.getTableHeader().setReorderingAllowed(false);
		treeTableComponent.setHorizontalScrollEnabled(true);
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

	protected JXTreeTable createTreeTable() {
		return new TreeTable();
	}

	protected TableCellRenderer createTableCellRenderer() {
		return new ItemTableCellRenderer();
	}

	protected TreeCellRenderer createTreeCellRenderer() {
		return new ItemTreeCellRenderer();
	}

	protected ControlScrollPane createScrollPane(Component view) {
		return new ControlScrollPane(view) {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				IListStructuralInfo structure = getRootStructuralInfo();
				if (structure.getLength() != -1) {
					result.height = structure.getLength();
				}
				return result;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension result = super.getMinimumSize();
				if (result == null) {
					return null;
				}
				IListStructuralInfo structure = getRootStructuralInfo();
				if (structure.getLength() != -1) {
					result.height = structure.getLength();
				}
				return result;
			}

			@Override
			public Dimension getMaximumSize() {
				Dimension result = super.getMaximumSize();
				if (result == null) {
					return null;
				}
				IListStructuralInfo structure = getRootStructuralInfo();
				if (structure.getLength() != -1) {
					result.height = structure.getLength();
				}
				return result;
			}
		};
	}

	protected ControlScrollPane createTreeTableScrollPane(Component view) {
		return createScrollPane(view);
	}

	protected ControlScrollPane createTreeTableAndToolBarScrollPane(Component view) {
		ControlScrollPane result = createScrollPane(new ScrollPaneOptions(view, true, false));
		return result;
	}

	protected ControlScrollPane createDetailsAreaScrollPane(Component view) {
		ControlScrollPane result = createScrollPane(new ScrollPaneOptions(view, true, false));
		SwingRendererUtils.removeScrollPaneBorder(result);
		return result;
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
				return swingRenderer.prepareMessageToDisplay(getColumnCaption(column));
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
		IListStructuralInfo tableInfo = getRootStructuralInfo();
		if (tableInfo == null) {
			return "";
		}
		return tableInfo.getColumns().get(columnIndex).getCaption();
	}

	public int getColumnCount() {
		IListStructuralInfo tableInfo = getRootStructuralInfo();
		if (tableInfo == null) {
			return 1;
		}
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
			BufferedItemPosition itemPosition = findItemPositionByNode(node);
			if (itemPosition == null) {
				value = "";
			} else {
				try {
					IListStructuralInfo tableInfo = getRootStructuralInfo();
					if (tableInfo == null) {
						value = ReflectionUIUtils.toString(swingRenderer.getReflectionUI(), itemPosition.getItem());
					} else {
						List<IColumnInfo> columns = tableInfo.getColumns();
						if (columnIndex < columns.size()) {
							IColumnInfo column = tableInfo.getColumns().get(columnIndex);
							if (column.hasCellValue(itemPosition)) {
								value = column.getCellValue(itemPosition);
							} else {
								if (columnIndex == 0) {
									value = ReflectionUIUtils.toString(swingRenderer.getReflectionUI(),
											itemPosition.getItem());
								} else {
									value = null;
								}
							}
						} else {
							value = null;
						}
					}
				} catch (Throwable t) {
					value = "<" + MiscUtils.getPrettyErrorMessage(t) + ">";
				}
			}
			nodeValues.put(columnIndex, value);
		}
		return value;
	}

	protected Image getCellIconImage(ItemNode node, int columnIndex) {
		BufferedItemPosition itemPosition = findItemPositionByNode(node);
		if (columnIndex == 0) {
			return swingRenderer.getObjectIconImage(itemPosition.getItem());
		}
		return null;
	}

	protected List<AbstractAction> getCurrentSelectionActions() {

		List<AbstractAction> result = new ArrayList<AbstractAction>();

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

		List<BufferedItemPosition> selection = getSelection();

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
		return new BufferedListModificationFactory(anyListItemPosition);
	}

	protected boolean allSelectionItemsInSameList() {
		boolean result = true;
		List<BufferedItemPosition> selection = getSelection();
		BufferedItemPosition firstSelectionItem = selection.get(0);
		for (BufferedItemPosition selectionItem : selection) {
			if (!MiscUtils.equalsOrBothNull(firstSelectionItem.getParentItemPosition(),
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
			BufferedItemPosition bufferedItemPosition = findItemPositionByNode(selectedNode);
			result.add(bufferedItemPosition);
		}
		return result;
	}

	public void setSingleSelection(BufferedItemPosition toSelect) {
		if (toSelect == null) {
			setSelection(Collections.emptyList());
		} else {
			setSelection(Collections.singletonList(toSelect));
		}
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

	public BufferedItemPosition findItemPositionByNode(ItemNode node) {
		Object userObject = node.getUserObject();
		if (!(userObject instanceof BufferedItemPosition)) {
			return null;
		}
		return (BufferedItemPosition) userObject;
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

	public void expandItemPosition(BufferedItemPosition itemPosition) {
		ItemNode node = findNode(itemPosition);
		if (node == null) {
			return;
		}
		TreePath treePath = new TreePath(node.getPath());
		treeTableComponent.expandPath(treePath);
	}

	public void collapseItemPosition(BufferedItemPosition itemPosition) {
		ItemNode node = findNode(itemPosition);
		if (node == null) {
			return;
		}
		TreePath treePath = new TreePath(node.getPath());
		treeTableComponent.collapsePath(treePath);
	}

	public void collapseAllItemPositions() {
		treeTableComponent.collapseAll();
	}

	public List<BufferedItemPosition> getExpandedItemPositions(BufferedItemPosition parentItemPosition) {
		List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		ItemNode parentNode = (parentItemPosition != null) ? findNode(parentItemPosition) : rootNode;
		Enumeration<?> expandedPaths = treeTableComponent.getExpandedDescendants(new TreePath(parentNode));
		if (expandedPaths != null) {
			while (expandedPaths.hasMoreElements()) {
				TreePath treePath = (TreePath) expandedPaths.nextElement();
				ItemNode node = (ItemNode) treePath.getLastPathComponent();
				if (node == rootNode) {
					continue;
				}
				BufferedItemPosition itemPosition = findItemPositionByNode(node);
				result.add(itemPosition);
			}
		}
		return result;
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
			protected ModificationStack getParentModificationStack() {
				return dummyModificationStack;
			}

			@Override
			protected boolean canCommitToParent() {
				return true;
			}

			@Override
			protected IModification createCommittingModification(Object newObjectValue) {
				return IModification.NULL_MODIFICATION;
			}

			@Override
			protected boolean isDialogCancellable() {
				return true;
			}
		};
		dialogBuilder.createAndShowDialog();
		return dialogBuilder;
	}

	protected boolean isDialogDisplayedOnItemCreation(BufferedItemPosition newItemPosition) {
		IListTypeInfo listType = newItemPosition.getContainingListType();
		ITypeInfo typeToInstanciate = listType.getItemType();
		if (typeToInstanciate == null) {
			typeToInstanciate = swingRenderer.getReflectionUI()
					.buildTypeInfo(new JavaTypeInfoSource(swingRenderer.getReflectionUI(), Object.class, null));
		}
		boolean constructorSelectable = (listType.getInitialItemValueCreationOption() == null) || (listType
				.getInitialItemValueCreationOption() == InitialItemValueCreationOption.CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES);
		return constructorSelectable && (listType.isItemNullValueSupported()
				|| swingRenderer.isDecisionRequiredOnTypeInstanciationRequest(typeToInstanciate));
	};

	protected Object onItemCreationRequest(BufferedItemPosition itemPosition) {
		IListTypeInfo listType = itemPosition.getContainingListType();
		ITypeInfo typeToInstanciate = listType.getItemType();
		if (typeToInstanciate == null) {
			typeToInstanciate = swingRenderer.getReflectionUI()
					.buildTypeInfo(new JavaTypeInfoSource(swingRenderer.getReflectionUI(), Object.class, null));
		}

		BufferedItemPosition parentItemPosition = itemPosition.getParentItemPosition();
		if (parentItemPosition != null) {
			Object parentItem = parentItemPosition.getItem();
			IFieldInfo containingListField = itemPosition.getContainingListFieldIfNotRoot();
			if (containingListField.getAlternativeListItemConstructors(parentItem) != null) {
				typeToInstanciate = new AbstractFieldControlData.FieldAlternativeListItemConstructorsInstaller(
						swingRenderer.getReflectionUI(), parentItem, containingListField)
								.wrapTypeInfo(typeToInstanciate);
			}
		}

		boolean constructorSelectable = (listType.getInitialItemValueCreationOption() == null) || (listType
				.getInitialItemValueCreationOption() == InitialItemValueCreationOption.CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES);

		if (constructorSelectable) {
			return swingRenderer.onTypeInstanciationRequest(ListControl.this, typeToInstanciate);
		} else {
			return ReflectionUIUtils.createDefaultInstance(typeToInstanciate);
		}
	}

	protected AbstractStandardListAction createAddChildAction() {
		return new AddChildAction();
	};

	protected String getItemTitle(BufferedItemPosition itemPosition) {
		Object capsule = new ItemUIBuilder(itemPosition) {
			@Override
			protected Object loadValue() {
				return null;
			}
		}.getCapsule();
		ITypeInfo encapsulatedObjectType = swingRenderer.getReflectionUI()
				.buildTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(capsule));
		return encapsulatedObjectType.getCaption();
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

	protected AbstractStandardListAction createDynamicPropertyHook(final IDynamicListProperty dynamicProperty) {
		return new DynamicPropertyHook(dynamicProperty);
	}

	protected AbstractStandardListAction createDynamicActionHook(final IDynamicListAction dynamicAction) {
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
					swingRenderer.handleObjectException(ListControl.this, t);
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

	protected void updateDetailsArea(boolean refreshStructure) {
		BufferedItemPosition singleSelection = getSingleSelection();
		if (singleSelection != null) {
			IListTypeInfo listType = singleSelection.getContainingListType();
			if (!listType.canViewItemDetails()) {
				singleSelection = null;
			}
		}
		if ((detailsControlItemPosition == null) && (singleSelection == null)) {
			return;
		}
		if ((detailsControlItemPosition != null) && (singleSelection != null)) {
			/*
			 * Here we will try to perform an optimization by reusing the same
			 * detailsControlBuilder to display another item. Therefore we must request the
			 * details control structure refreshing if we detect that the type of the item
			 * changes.
			 */
			{
				if (!MiscUtils.equalsOrBothNull(detailsControlItemPosition.getContainingListType().getItemType(),
						singleSelection.getContainingListType().getItemType())) {
					refreshStructure = true;
				} else {
					Object newItem = singleSelection.getItem();
					Object currentItem = detailsControlBuilder.getCurrentValue();
					if ((newItem != null) && (currentItem != null)) {
						ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
						ITypeInfo newItemType = reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(newItem));
						ITypeInfo currentItemType = reflectionUI
								.buildTypeInfo(reflectionUI.getTypeInfoSource(currentItem));
						if (!newItemType.equals(currentItemType)) {
							refreshStructure = true;
						}
					}
				}
			}
			detailsControlItemPosition = singleSelection;
			detailsControlBuilder.setPosition(detailsControlItemPosition);
			detailsControlBuilder.refreshEditorForm(detailsControl, refreshStructure);
			return;
		}
		if ((detailsControlItemPosition != null) && (singleSelection == null)) {
			detailsControlItemPosition = null;
			detailsArea.removeAll();
			detailsControlBuilder = null;
			detailsControl = null;
			SwingRendererUtils.handleComponentSizeChange(detailsArea);
			return;
		}

		if ((detailsControlItemPosition == null) && (singleSelection != null)) {
			detailsControlItemPosition = singleSelection;
			detailsControlBuilder = new ItemUIBuilder(detailsControlItemPosition);
			detailsControl = detailsControlBuilder.createEditorForm(true, false);
			/*
			 * A pre-selection must be done before each undo/redo modification to ensure
			 * that the replayed modification will affect the right item. Otherwise it may
			 * not be the case when the same detailsControlBuilder is reused to display
			 * multiple items (optimization): the modification would then affect the
			 * currently selected item instead of the one that was initially modified since
			 * the modification references the modified item through the current
			 * detailsControl.
			 */
			detailsControl.getModificationStack().setPushFilter(new Filter<IModification>() {
				@Override
				public IModification get(IModification undoModif) {
					Object oldItem = detailsControlItemPosition.getItem();
					Object newItem = detailsControlBuilder.getCurrentValue();
					IModification preSelection = new PreSelection(detailsControlItemPosition, newItem, oldItem,
							detailsControlBuilder, detailsControl);
					return new CompositeModification(undoModif.getTitle(), UndoOrder.FIFO, preSelection, undoModif);
				}
			});
			detailsArea.setLayout(new BorderLayout());
			Component statusBar = detailsControl.getStatusBar();
			{
				detailsArea.add(statusBar, BorderLayout.NORTH);
			}
			detailsArea.add(detailsControl, BorderLayout.CENTER);
			detailsControl.validateFormInBackgroundAndReportOnStatusBar();
			SwingRendererUtils.handleComponentSizeChange(detailsArea);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (detailsControlItemPosition != null) {
						scrollTo(detailsControlItemPosition);
					}
				}
			});
			return;
		}
		throw new ReflectionUIError();
	}

	protected void openDetailsDialogOnItemDoubleClick() {
		treeTableComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if (!getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
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
					swingRenderer.handleObjectException(treeTableComponent, t);
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
				rootNode = createRootNode();
				treeTableComponent.setTreeTableModel(createTreeTableModel());
				if (refreshStructure) {
					TableColumnModel columnModel = treeTableComponent.getColumnModel();
					{
						List<IColumnInfo> columnInfos = getRootStructuralInfo().getColumns();
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
		BufferedItemPosition currentListItemPosition = findItemPositionByNode(currentNode);
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
		if (refreshStructure) {
			removeAll();
			detailsMode = null;
			structuralInfo = null;
			layoutControls();
			refreshTreeTableScrollPaneBorder();
			refreshTreeTableComponentBackground();
			refreshTreeTableComponentHeader();
			refreshRendrers();
			restoringSelectionAsMuchAsPossible(new Runnable() {
				@Override
				public void run() {
					restoringExpandedPathsAsMuchAsPossible(new Runnable() {
						@Override
						public void run() {
							refreshItemPositionBuffers();
							refreshTreeTableModelAndControl(refreshStructure);
						}

					});
				}
			});
			if (getDetailsAccessMode().hasEmbeddedDetailsDisplayArea()) {
				updateDetailsArea(true);
			}
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			restoringSelectionAsMuchAsPossible(new Runnable() {

				@Override
				public void run() {
					restoringExpandedPathsAsMuchAsPossible(new Runnable() {
						@Override
						public void run() {
							refreshItemPositionBuffers();
							refreshTreeTableModelAndControl(refreshStructure);
						}
					});
				}

			});
			if (getDetailsAccessMode().hasEmbeddedDetailsDisplayArea()) {
				updateDetailsArea(false);
			}
		}
		return true;
	}

	protected boolean isSelectionUseful() {
		if (getDetailsAccessMode().hasEmbeddedDetailsDisplayArea()) {
			return true;
		}

		return false;
	}

	protected void refreshItemPositionBuffers() {
		itemPositionFactory.refreshAll();
	}

	protected void refreshTreeTableScrollPaneBorder() {
		if (listData.getCaption().length() > 0) {
			treeTableComponentScrollPane.setBorder(
					BorderFactory.createTitledBorder(swingRenderer.prepareMessageToDisplay(listData.getCaption())));
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
		} else {
			treeTableComponentScrollPane.setBorder(null);
		}
	}

	protected void refreshRendrers() {
		treeTableComponent.setDefaultRenderer(Object.class, createTableCellRenderer());
		treeTableComponent.setTreeCellRenderer(createTreeCellRenderer());
	}

	protected void refreshTreeTableComponentBackground() {
		if (listData.getEditorBackgroundColor() != null) {
			treeTableComponent.setBackground(SwingRendererUtils.getColor(listData.getEditorBackgroundColor()));
		} else {
			treeTableComponent.setBackground(new JXTreeTable().getBackground());
		}
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
	public void validateSubForms() throws Exception {
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
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
		List<IColumnInfo> columnInfos = getRootStructuralInfo().getColumns();
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

	protected void restoringExpandedPathsAsMuchAsPossible(Runnable runnable) {
		List<BufferedItemPosition> wereExpandedPositions = getExpandedItemPositions(null);
		List<Object> wereExpanded = new ArrayList<Object>();
		for (int i = 0; i < wereExpandedPositions.size(); i++) {
			try {
				BufferedItemPosition wasExpandedPosition = wereExpandedPositions.get(i);
				wereExpanded.add(wasExpandedPosition.getItem());
			} catch (Throwable t) {
				wereExpanded.add(null);
			}
		}

		runnable.run();

		collapseAllItemPositions();
		int i = 0;
		for (Iterator<BufferedItemPosition> it = wereExpandedPositions.iterator(); it.hasNext();) {
			BufferedItemPosition wasExpandedPosition = it.next();
			try {
				if (!wasExpandedPosition.getContainingListType().isOrdered()) {
					Object wasSelected = wereExpanded.get(i);
					int index = Arrays.asList(wasExpandedPosition.retrieveContainingListRawValue())
							.indexOf(wasSelected);
					wasExpandedPosition = wasExpandedPosition.getSibling(index);
					wereExpandedPositions.set(i, wasExpandedPosition);
				}
			} catch (Throwable t) {
				it.remove();
			}
			i++;
		}
		for (BufferedItemPosition wasExpandedPosition : wereExpandedPositions) {
			expandItemPosition(wasExpandedPosition);
		}
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
			try {
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
			} catch (Throwable t) {
				refreshingErrorHandler.handle(t);
			}
			return result;
		}

	}

	protected class RefreshStructureModification implements IModification {
		protected Accessor<List<BufferedItemPosition>> newSelectionGetter;
		protected Accessor<List<BufferedItemPosition>> oldSelectionGetter;

		public RefreshStructureModification(Accessor<List<BufferedItemPosition>> newSelectionGetter,
				Accessor<List<BufferedItemPosition>> oldSelectionGetter) {
			super();
			this.newSelectionGetter = newSelectionGetter;
			this.oldSelectionGetter = oldSelectionGetter;
		}

		public RefreshStructureModification(List<BufferedItemPosition> newSelection) {
			this(Accessor.returning(newSelection), new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return getSelection();
				}
			});
		}

		@Override
		public IModification applyAndGetOpposite() {
			List<BufferedItemPosition> newSelection = newSelectionGetter.get();
			if (newSelection == null) {
				restoringSelectionAsMuchAsPossible(new Runnable() {
					public void run() {
						restoringExpandedPathsAsMuchAsPossible(new Runnable() {
							@Override
							public void run() {
								refreshTreeTableModelAndControl(false);
							}
						});
					}
				});
			} else {
				restoringExpandedPathsAsMuchAsPossible(new Runnable() {
					@Override
					public void run() {
						refreshTreeTableModelAndControl(false);
					}
				});
				setSelection(newSelection);
				if (newSelection.size() > 0) {
					scrollTo(newSelection.get(0));
				}
			}
			return new RefreshStructureModification(oldSelectionGetter, Accessor.returning(newSelection));
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "Refresh Structure And Select Item(s)";
		}

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public boolean isFake() {
			return false;
		}

	}

	protected class SelectModification implements IModification {
		protected Accessor<List<BufferedItemPosition>> newSelectionGetter;
		protected Accessor<List<BufferedItemPosition>> oldSelectionGetter;

		public SelectModification(Accessor<List<BufferedItemPosition>> newSelectionGetter,
				Accessor<List<BufferedItemPosition>> oldSelectionGetter) {
			this.newSelectionGetter = newSelectionGetter;
			this.oldSelectionGetter = oldSelectionGetter;
		}

		public SelectModification(List<BufferedItemPosition> newSelection) {
			this(Accessor.returning(newSelection), new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return getSelection();
				}
			});
		}

		@Override
		public IModification applyAndGetOpposite() {
			List<BufferedItemPosition> newSelection = newSelectionGetter.get();
			if (newSelection != null) {
				setSelection(newSelection);
				if (newSelection.size() > 0) {
					scrollTo(newSelection.get(0));
				}
			}
			return new SelectModification(oldSelectionGetter, Accessor.returning(newSelection));
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "Select Items(s)";
		}

		@Override
		public boolean isNull() {
			return false;
		}

		@Override
		public boolean isFake() {
			return false;
		}

	}

	protected class TreeTable extends JXTreeTable {
		private static final long serialVersionUID = 1L;

		@Override
		public String getToolTipText(MouseEvent event) {
			try {
				return super.getToolTipText(event);
			} catch (Throwable t) {
				return null;
			}
		}
	}

	protected class PreSelection extends AbstractModification {

		protected BufferedItemPosition currentPosition;
		protected Object doItem;
		protected Object undoItem;
		protected ItemUIBuilder detailsControlBuilder;
		protected Form detailsControl;

		public PreSelection(BufferedItemPosition currentPosition, Object doItem, Object undoItem,
				ItemUIBuilder detailsControlBuilder, Form detailsControl) {
			this.currentPosition = currentPosition;
			this.doItem = doItem;
			this.undoItem = undoItem;
			this.detailsControlBuilder = detailsControlBuilder;
			this.detailsControl = detailsControl;
		}

		@Override
		public String getTitle() {
			return "Item Selection";
		}

		@Override
		protected Runnable createDoJob() {
			return createAnyJob(doItem);
		}

		@Override
		protected Runnable createUndoJob() {
			return createAnyJob(undoItem);
		}

		protected Runnable createAnyJob(final Object currentItem) {
			return new Runnable() {
				@Override
				public void run() {
					Object[] containingListRawValue = currentPosition.retrieveContainingListRawValue();
					if (!currentPosition.getContainingListType().isOrdered()) {
						int indexToSelect = Arrays.asList(containingListRawValue).indexOf(currentItem);
						if (indexToSelect == -1) {
							throw new ReflectionUIError("Cannot find item equals to: '" + currentItem + "'");
						}
						currentPosition = currentPosition.getSibling(indexToSelect);
					}
					setSelection(Collections.singletonList(currentPosition));
					if (!currentPosition.equals(detailsControlItemPosition)) {
						updateDetailsArea(false);
					}
					/*
					 * Now we are sure that the item that must be modified is selected. But it may
					 * be a copy that is just equal. To ensure that the item that will be committed
					 * (currentItem) is the one that is referenced by the details modification
					 * object, we must update its reference in the detailsControl. PROBLEM: the
					 * details modification object may reference a "dead" detailsControl (rebuilt at
					 * some time) preventing from updating this reference. That is why the right
					 * detailsControlBuilder and its detailsControl are stored in this class.
					 */
					if (detailsControlBuilder.getCurrentValue() != currentItem) {
						containingListRawValue[currentPosition.getIndex()] = currentItem;
						currentPosition.changeContainingListBuffer(containingListRawValue);
						detailsControlBuilder.refreshEditorForm(detailsControl, false);
					}
				}
			};
		}

	}

	protected abstract class AbstractItemCellRenderer {

		protected void customizeCellRendererComponent(JLabel label, ItemNode node, int rowIndex, int columnIndex,
				boolean isSelected, boolean hasFocus) {
			label.putClientProperty("html.disable", Boolean.TRUE);
			if (findItemPositionByNode(node) == null) {
				return;
			}
			String text = getCellValue(node, columnIndex);
			if ((text == null) || (text.length() == 0)) {
				label.setText(" ");
				label.setToolTipText(null);
			} else {
				label.setText(text.replaceAll(MiscUtils.getNewLineRegex(), " "));
				SwingRendererUtils.setMultilineToolTipText(label, text);
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
				return swingRenderer.prepareMessageToDisplay(getActionTitle());
			} else if (Action.SHORT_DESCRIPTION.equals(key)) {
				String result = getActionDescription();
				if (result != null) {
					result = swingRenderer.prepareMessageToDisplay(result);
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
		public void actionPerformed(ActionEvent e) {
			try {
				if (!prepare()) {
					return;
				}
				final String modifTitle = getCompositeModificationTitle();
				@SuppressWarnings("unchecked")
				final List<BufferedItemPosition>[] toPostSelectHolder = new List[1];
				if (modifTitle == null) {
					perform(toPostSelectHolder);
					new RefreshStructureModification(toPostSelectHolder[0]).applyAndGetOpposite();
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
									}, listData.isTransient())) {
								modifStack.apply(new RefreshStructureModification(toPostSelectHolder[0]));
								return true;
							} else {
								new RefreshStructureModification(toPostSelectHolder[0]).applyAndGetOpposite();
								return modifStack.wasInvalidated();
							}
						}
					}, listData.isTransient());

				}
				displayResult();
			} catch (Throwable t) {
				swingRenderer.handleObjectException(ListControl.this, t);
			}
		}

		protected void displayResult() {
		}

	};

	protected class ItemUIBuilder extends AbstractEditorBuilder {
		protected BufferedItemPosition bufferedItemPosition;
		protected ListModificationFactory modificationFactory;
		protected boolean canCommit;
		protected ValueReturnMode objectValueReturnMode;

		public ItemUIBuilder(BufferedItemPosition bufferedItemPosition) {
			setPosition(bufferedItemPosition);
		}

		public void setPosition(BufferedItemPosition bufferedItemPosition) {
			this.bufferedItemPosition = bufferedItemPosition;
			this.modificationFactory = createListModificationFactory(bufferedItemPosition);
			this.canCommit = modificationFactory.canSet(bufferedItemPosition.getIndex());
			this.objectValueReturnMode = bufferedItemPosition.getItemReturnMode();
		}

		@Override
		protected IContext getContext() {
			return input.getContext();
		}

		@Override
		protected IContext getSubContext() {
			return new CustomContext("ListItem");
		}

		@Override
		protected boolean isEncapsulatedFormEmbedded() {
			return true;
		}

		@Override
		protected boolean isNullValueDistinct() {
			return bufferedItemPosition.getContainingListType().isItemNullValueSupported();
		}

		@Override
		protected boolean canCommitToParent() {
			return canCommit;
		}

		@Override
		protected IModification createCommittingModification(final Object newItem) {
			IModification update = modificationFactory.set(bufferedItemPosition.getIndex(), newItem);
			IModification structureRefreshing = new RefreshStructureModification(
					new Accessor<List<BufferedItemPosition>>() {
						@Override
						public List<BufferedItemPosition> get() {
							return null;
						}
					}, new Accessor<List<BufferedItemPosition>>() {
						@Override
						public List<BufferedItemPosition> get() {
							return null;
						}
					});
			IModification postSelection = new SelectModification(
					Accessor.returning(Collections.singletonList(bufferedItemPosition)),
					Accessor.returning(Collections.singletonList(bufferedItemPosition)));
			return new CompositeModification(update.getTitle(), UndoOrder.FIFO, update, structureRefreshing,
					postSelection);
		}

		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			swingRenderer.handleObjectException(ListControl.this, t);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected String getParentModificationTitle() {
			return getItemModificationTitle();
		}

		@Override
		protected boolean isParentModificationFake() {
			return listData.isTransient();
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			ITypeInfo itemType = bufferedItemPosition.getContainingListType().getItemType();
			if (itemType != null) {
				return itemType.getSource();
			}
			return new JavaTypeInfoSource(swingRenderer.getReflectionUI(), Object.class, null);
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return objectValueReturnMode;
		}

		@Override
		protected Object loadValue() {
			return bufferedItemPosition.getItem();
		}

		@Override
		protected ModificationStack getParentModificationStack() {
			return ListControl.this.getModificationStack();
		}

		@Override
		protected Component getOwnerComponent() {
			return ListControl.this;
		}

		@Override
		protected IInfoFilter getEncapsulatedFormFilter() {
			return new DelegatingInfoFilter() {
				@Override
				protected IInfoFilter getDelegate() {
					BufferedItemPosition dynamicItemPosition = bufferedItemPosition.getSibling(-1);
					dynamicItemPosition.setFakeItem(getCurrentValue());
					return getStructuralInfo(dynamicItemPosition).getItemInfoFilter(dynamicItemPosition);
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
			if (subListType
					.getInitialItemValueCreationOption() == InitialItemValueCreationOption.CREATE_INITIAL_NULL_VALUE) {
				newSubListItem = null;
			} else {
				boolean nullValueChosen = false;
				if (subListType.isItemNullValueSupported()) {
					String choice = swingRenderer.openSelectionDialog(ListControl.this,
							Arrays.asList("Create", "<Null>"), "Create", "Choose", getItemTitle(newSubItemPosition));
					if (choice == null) {
						return false;
					}
					if ("<Null>".equals(choice)) {
						newSubListItem = null;
						nullValueChosen = true;
					}
				}
				if (!nullValueChosen) {
					newSubListItem = onItemCreationRequest(newSubItemPosition);
					if (newSubListItem == null) {
						return false;
					}
				}
			}
			boolean wasDialogDisplayed = isDialogDisplayedOnItemCreation(newSubItemPosition);
			if (!wasDialogDisplayed && getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
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
		protected boolean isValid() {
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
			if (isDialogDisplayedOnItemCreation(subItemPosition)
					|| getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
				title += "...";
			}
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
		protected boolean isValid() {
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
		protected boolean isValid() {
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
		protected boolean isValid() {
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
		protected boolean prepare() {
			newItemPosition = getNewItemPosition();
			listType = newItemPosition.getContainingListType();
			if (listType
					.getInitialItemValueCreationOption() == InitialItemValueCreationOption.CREATE_INITIAL_NULL_VALUE) {
				newItem = null;
			} else {
				boolean nullValueChosen = false;
				if (listType.isItemNullValueSupported()) {
					String choice = swingRenderer.openSelectionDialog(ListControl.this,
							Arrays.asList("Create", "<Null>"), "Create", "Choose", getItemTitle(newItemPosition));
					if (choice == null) {
						return false;
					}
					if ("<Null>".equals(choice)) {
						newItem = null;
						nullValueChosen = true;
					}
				}
				if (!nullValueChosen) {
					newItem = onItemCreationRequest(newItemPosition);
					if (newItem == null) {
						return false;
					}
				}
			}
			boolean wasDialogDisplayed = isDialogDisplayedOnItemCreation(newItemPosition);
			if (!wasDialogDisplayed && getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
				ItemUIBuilder dialogBuilder = openAnticipatedItemDialog(newItemPosition, newItem);
				if (dialogBuilder.isCancelled()) {
					return false;
				}
				newItem = dialogBuilder.getCurrentValue();
			}
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
				if (isDialogDisplayedOnItemCreation(newItemPosition)
						|| getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
					buttonText += " ...";
				}
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
		protected boolean isValid() {
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
			avoidRestoringExpandedPathOfMovedNode();
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

		protected void avoidRestoringExpandedPathOfMovedNode() {
			List<BufferedItemPosition> selection = getSelection();
			BufferedItemPosition first = selection.get(0);
			for (int i = 0; i < first.getContainingListSize(); i++) {
				BufferedItemPosition itemPosition = first.getSibling(i);
				collapseItemPosition(itemPosition);
			}
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
		protected boolean isValid() {
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
			JDialog dialog = dialogBuilder.createDialog();
			swingRenderer.showDialog(dialog, true);
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			toPostSelectHolder[0] = getSelection();
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
		protected boolean isValid() {
			if (!getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
				return false;
			}
			BufferedItemPosition singleSelectedPosition = getSingleSelection();
			if (singleSelectedPosition != null) {
				if (!new ItemUIBuilder(singleSelectedPosition).isFormEmpty()) {
					IListTypeInfo listType = singleSelectedPosition.getContainingListType();
					if (listType.canViewItemDetails()) {
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
		protected boolean isValid() {
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

	protected class PasteIntoAction extends AddChildAction {

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
		protected boolean isValid() {
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
		public Object getValue(String key) {
			if (Action.LARGE_ICON_KEY.equals(key)) {
				ResourcePath iconImagePath = dynamicAction.getIconImagePath();
				if (iconImagePath == null) {
					return null;
				}
				Image iconImage = SwingRendererUtils.loadImageThroughCache(iconImagePath,
						ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
				if (iconImage == null) {
					return null;
				}
				return SwingRendererUtils.getIcon(iconImage);
			} else {
				return super.getValue(key);
			}
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
					return new DefaultMethodControlData(swingRenderer.getReflectionUI(), IDynamicListAction.NO_OWNER,
							dynamicAction);
				}
			});
			invocationData = action.prepare(ListControl.this);
			return invocationData != null;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			action.invokeAndSetReturnValue(invocationData, ListControl.this);
			if (dynamicAction.getPostSelection() != null) {
				toPostSelectHolder[0] = MiscUtils.<ItemPosition, BufferedItemPosition>convertCollectionUnsafely(
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
		protected boolean isValid() {
			return dynamicAction.isEnabled(IDynamicListAction.NO_OWNER);
		}

		@Override
		protected String getActionDescription() {
			return dynamicAction.getOnlineHelp();
		}
	}

	protected class DynamicPropertyHook extends AbstractStandardListAction {
		protected static final long serialVersionUID = 1L;

		protected IDynamicListProperty dynamicProperty;
		protected AbstractEditorBuilder subDialogBuilder;

		public DynamicPropertyHook(IDynamicListProperty dynamicProperty) {
			this.dynamicProperty = dynamicProperty;
		}

		@Override
		protected boolean prepare() {
			subDialogBuilder = new AbstractEditorBuilder() {

				@Override
				protected String getCapsuleTypeCaption() {
					return ReflectionUIUtils.composeMessage(listData.getCaption(), dynamicProperty.getCaption());
				}

				@Override
				protected IContext getContext() {
					return input.getContext();
				}

				@Override
				protected IContext getSubContext() {
					return new CustomContext("ListDynamicProperty [name=" + dynamicProperty.getName() + "]");
				}

				@Override
				protected boolean isEncapsulatedFormEmbedded() {
					return dynamicProperty.isFormControlEmbedded();
				}

				@Override
				protected boolean isNullValueDistinct() {
					return dynamicProperty.isNullValueDistinct();
				}

				@Override
				public SwingRenderer getSwingRenderer() {
					return swingRenderer;
				}

				@Override
				protected ValueReturnMode getReturnModeFromParent() {
					return ValueReturnMode.combine(listData.getValueReturnMode(), dynamicProperty.getValueReturnMode());
				}

				@Override
				protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
					return dynamicProperty.getType().getSource();
				}

				@Override
				protected Object loadValue() {
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
				protected String getParentModificationTitle() {
					return "Edit "
							+ ReflectionUIUtils.composeMessage(listData.getCaption(), dynamicProperty.getCaption());
				}

				@Override
				protected boolean isParentModificationFake() {
					return listData.isTransient();
				}

				@Override
				protected ModificationStack getParentModificationStack() {
					return ListControl.this.getModificationStack();
				}

				@Override
				protected Component getOwnerComponent() {
					return ListControl.this;
				}

				@Override
				protected boolean canCommitToParent() {
					BufferedItemPosition anyRootItemPosition = getRootListItemPosition(-1);
					return anyRootItemPosition.isContainingListEditable();
				}

				@Override
				protected IModification createCommittingModification(Object newObjectValue) {
					return new FieldControlDataModification(new DefaultFieldControlData(swingRenderer.getReflectionUI(),
							IDynamicListProperty.NO_OWNER, dynamicProperty), newObjectValue);
				}

				@Override
				protected void handleRealtimeLinkCommitException(Throwable t) {
					swingRenderer.handleObjectException(ListControl.this, t);
				}

				@Override
				protected IInfoFilter getEncapsulatedFormFilter() {
					return dynamicProperty.getFormControlFilter();
				}
			};
			JDialog dialog = subDialogBuilder.createDialog();
			swingRenderer.showDialog(dialog, true);
			return true;
		}

		@Override
		protected void perform(List<BufferedItemPosition>[] toPostSelectHolder) {
			if (dynamicProperty.getPostSelection() != null) {
				toPostSelectHolder[0] = MiscUtils.<ItemPosition, BufferedItemPosition>convertCollectionUnsafely(
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
		protected boolean isValid() {
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
		protected void setNonBufferedRootListValue(Object rootListValue) {
			listData.setValue(rootListValue);
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

		private ListControl getEnclosingInstance() {
			return ListControl.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
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
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return true;
		}

	}

}
