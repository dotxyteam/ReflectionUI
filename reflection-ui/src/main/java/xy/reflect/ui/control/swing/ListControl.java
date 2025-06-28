
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

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
import javax.swing.border.Border;
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
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.ListControl.IItemsVisitor.VisitStatus;
import xy.reflect.ui.control.swing.builder.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.AbstractLazyTreeNode;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.ControlSplitPane;
import xy.reflect.ui.control.swing.util.ScrollPaneOptions;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.FieldAlternativeListItemConstructorsInstaller;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.ItemCreationMode;
import xy.reflect.ui.info.type.iterable.item.AbstractBufferedItemPositionFactory;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemDetailsAreaPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPositionFactory;
import xy.reflect.ui.info.type.iterable.item.EmbeddedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListFeauture.DisplayMode;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.undo.MethodControlDataModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.BetterFutureTask;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ValidationErrorWrapper;

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
	protected BufferedFieldControlData listData;

	protected JXTreeTable treeTableComponent;
	protected JScrollPane treeTableComponentScrollPane;
	protected JPanel toolbar;
	protected ItemNode rootNode;
	protected AbstractBufferedItemPositionFactory itemPositionFactory;
	protected static List<Object> clipboard = new ArrayList<Object>();
	protected Map<ItemNode, Map<Integer, String>> valuesByNode = new HashMap<ItemNode, Map<Integer, String>>();
	protected ExecutorService itemValidationErrorsCollectingExecutor = MiscUtils
			.newExecutor(ListControl.class.getName() + "ValidationErrorsCollector", 0);

	protected JPanel detailsArea;
	protected Form detailsControl;
	protected ItemUIBuilder detailsControlBuilder;
	protected IListItemDetailsAccessMode detailsMode;
	protected BufferedItemPosition detailsControlItemPosition;

	protected List<Listener<List<BufferedItemPosition>>> selectionListeners = new ArrayList<Listener<List<BufferedItemPosition>>>();
	protected boolean selectionListenersEnabled = true;
	protected IFieldControlInput input;

	protected IFieldControlData selectionTargetData;
	protected boolean selectionTargetListenerEnabled = true;

	protected static AbstractAction SEPARATOR_ACTION = new AbstractAction("") {
		protected static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};
	protected boolean initialized = false;

	public ListControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.listData = new BufferedFieldControlData(
				new ErrorHandlingFieldControlData(input.getControlData(), swingRenderer, ListControl.this));
		listData.returningValue(listData.getValue(), new Runnable() {
			@Override
			public void run() {
				initializeTreeTableModelAndControl();
				toolbar = new ControlPanel();
				detailsArea = new ControlPanel();

				displayDetailsOnItemDoubleClick();
				updatePartsOnSelectionChange();
				updateSelectionTargetOnSelectionChange();
				handleMouseRightButton();
				updateToolbar();
				initializeSelectionListening();
				refreshUI(true);
				setDefaultSelection();
			}
		});
		this.initialized = true;
	}

	protected void setDefaultSelection() {
		if (getSelection().isEmpty() && (getRootListSize() > 0)) {
			setSingleSelection(getRootListItemPosition(0));
		}
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (initialized) {
			refreshUI(true);
		}
	}

	public IFieldControlData getSelectionTargetData() {
		return selectionTargetData;
	}

	public void setSelectionTargetData(IFieldControlData selectionTargetData) {
		this.selectionTargetData = selectionTargetData;
		if (selectionTargetData != null) {
			Object selectionTargetDataValue = selectionTargetData.getValue();
			BufferedItemPosition itemPosition = (selectionTargetDataValue != null)
					? findFirstItemPositionByValue(selectionTargetDataValue)
					: null;
			withoutSelectionTargetListenerEnabled(new Runnable() {
				@Override
				public void run() {
					setSingleSelection(itemPosition);
				}
			});
		}
	}

	public Object getRootListValue() {
		return itemPositionFactory.getRootListValue();
	}

	public String getRootListTitle() {
		return listData.getCaption();
	}

	protected void updatePartsOnSelectionChange() {
		selectionListeners.add(new Listener<List<BufferedItemPosition>>() {
			@Override
			public void handle(List<BufferedItemPosition> event) {
				updateCurrentSelectionDependentParts();
			}
		});
	}

	protected void updateCurrentSelectionDependentParts() {
		updateDetailsArea(false);
		updateToolbar();
	}

	protected void layoutControls() {
		setLayout(new BorderLayout());
		if (getDetailsAccessMode().hasEmbeddedDetailsDisplayArea()) {
			JPanel listAndToolbarPanel = new ControlPanel();
			listAndToolbarPanel.setLayout(new BorderLayout());
			listAndToolbarPanel.add(BorderLayout.CENTER, treeTableComponentScrollPane);
			String toolbarConstraint = getToolbarBorderLayoutConstraint();
			if (toolbarConstraint != null) {
				listAndToolbarPanel.add(toolbar, getToolbarBorderLayoutConstraint());
			}
			ControlScrollPane listAndToolbarScrollPane = createTreeTableAndToolBarScrollPane(listAndToolbarPanel);
			SwingRendererUtils.removeScrollPaneBorder(listAndToolbarScrollPane);
			ControlScrollPane detailsAreaScrollPane = createDetailsAreaScrollPane(detailsArea);
			JSplitPane splitPane = new ControlSplitPane();
			if (listData.getBorderColor() != null) {
				splitPane.setBorder(
						BorderFactory.createLineBorder(SwingRendererUtils.getColor(listData.getBorderColor())));
			}
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
			SwingRendererUtils.ensureDividerLocation(splitPane, dividerLocation);
			splitPane.setResizeWeight(dividerLocation);
		} else {
			add(treeTableComponentScrollPane, BorderLayout.CENTER);
			add(toolbar, BorderLayout.EAST);
		}
	}

	protected String getToolbarBorderLayoutConstraint() {
		IListTypeInfo.ToolsLocation toolsLocation = getRootListType().getToolsLocation();
		if (toolsLocation == IListTypeInfo.ToolsLocation.NORTH) {
			return BorderLayout.NORTH;
		} else if (toolsLocation == IListTypeInfo.ToolsLocation.SOUTH) {
			return BorderLayout.SOUTH;
		} else if (toolsLocation == IListTypeInfo.ToolsLocation.EAST) {
			return BorderLayout.EAST;
		} else if (toolsLocation == IListTypeInfo.ToolsLocation.WEST) {
			return BorderLayout.WEST;
		} else if (toolsLocation == IListTypeInfo.ToolsLocation.HIDDEN) {
			return null;
		} else {
			throw new ReflectionUIError();
		}
	}

	protected void updateToolbar() {
		toolbar.removeAll();
		String toolbarConstraint = getToolbarBorderLayoutConstraint();
		if (toolbarConstraint == null) {
			return;
		}
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
				if ((listProperty.getDisplayMode() == DisplayMode.TOOLBAR)
						|| (listProperty.getDisplayMode() == DisplayMode.TOOLBAR_AND_CONTEXT_MENU)) {
					AbstractStandardListAction dynamicPropertyHook = createDynamicPropertyHook(listProperty);
					toolbar.add(createTool((String) dynamicPropertyHook.getActionTitle(), null, true, false,
							dynamicPropertyHook));
				}
			}
			for (IDynamicListAction listAction : dynamicActions) {
				if ((listAction.getDisplayMode() == DisplayMode.TOOLBAR)
						|| (listAction.getDisplayMode() == DisplayMode.TOOLBAR_AND_CONTEXT_MENU)) {
					AbstractStandardListAction dynamicActionHook = createDynamicActionHook(listAction);
					toolbar.add(createTool((String) dynamicActionHook.getActionTitle(),
							(Icon) dynamicActionHook.getValue(AbstractAction.LARGE_ICON_KEY), true, false,
							dynamicActionHook));
				}
			}
		}

		for (int i = 0; i < toolbar.getComponentCount(); i++) {
			Component c = toolbar.getComponent(i);
			GridBagConstraints constraints = new GridBagConstraints();
			if (toolbarConstraint.equals(BorderLayout.WEST) || toolbarConstraint.equals(BorderLayout.EAST)) {
				constraints.gridx = 0;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.insets = new Insets(1, 5, 1, 5);
			} else if (toolbarConstraint.equals(BorderLayout.NORTH) || toolbarConstraint.equals(BorderLayout.SOUTH)) {
				constraints.gridy = 0;
				constraints.fill = GridBagConstraints.VERTICAL;
				constraints.insets = new Insets(5, 1, 5, 1);
			} else {
				throw new ReflectionUIError();
			}
			layout.setConstraints(c, constraints);
		}

		JSeparator filler = new JSeparator(JSeparator.VERTICAL);
		{
			GridBagConstraints constraints = new GridBagConstraints();
			if (toolbarConstraint.equals(BorderLayout.WEST) || toolbarConstraint.equals(BorderLayout.EAST)) {
				constraints.gridx = 0;
				constraints.weighty = 1;
			} else if (toolbarConstraint.equals(BorderLayout.NORTH) || toolbarConstraint.equals(BorderLayout.SOUTH)) {
				constraints.gridy = 0;
				constraints.weightx = 1;
			} else {
				throw new ReflectionUIError();
			}
			toolbar.add(filler, constraints);
		}

		SwingRendererUtils.handleComponentSizeChange(ListControl.this);
		toolbar.validate();
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
			public Font retrieveCustomFont() {
				if (listData.getButtonCustomFontResourcePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadFontThroughCache(listData.getButtonCustomFontResourcePath(),
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

	protected void handleMouseRightButton() {
		treeTableComponent.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) {
					return;
				}
				adjustSelection(e);
				popupMenu(e);
			}

			void adjustSelection(MouseEvent e) {
				int row = treeTableComponent.rowAtPoint(e.getPoint());
				if (treeTableComponent.isRowSelected(row)) {
					return;
				}
				if ((row < 0) || (row >= treeTableComponent.getRowCount())) {
					return;
				}
				treeTableComponent.setRowSelectionInterval(row, row);
				return;
			}

			void popupMenu(MouseEvent e) {
				if (e.getComponent() == treeTableComponent) {
					JPopupMenu popup = createPopupMenu();
					popup.show(e.getComponent(), e.getX(), e.getY());
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
	public boolean displayError(Throwable error) {
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
		IListTypeInfo listType = itemPosition.getContainingListType();
		IListStructuralInfo result = listType.getStructuralInfo();
		if (result == null) {
			throw new ReflectionUIError("No " + IListStructuralInfo.class.getSimpleName() + " found on the type '"
					+ listType.getName() + "'");
		}
		return result;
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

	protected ControlScrollPane createTreeTableScrollPane(Component view) {
		return new ControlScrollPane(view) {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				IListStructuralInfo structure = getRootStructuralInfo();
				if (structure.getHeight() != -1) {
					result.height = structure.getHeight();
				}
				return result;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension result = super.getMinimumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.min(result.height, preferredSize.height);
					}
				}
				return result;

			}

			@Override
			public Dimension getMaximumSize() {
				Dimension result = super.getMaximumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.max(result.height, preferredSize.height);
					}
				}
				return result;
			}
		};
	}

	protected ControlScrollPane createTreeTableAndToolBarScrollPane(Component view) {
		return new ControlScrollPane(new ScrollPaneOptions(view, true, false)) {
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
				IListStructuralInfo structure = getRootStructuralInfo();
				if (structure.getHeight() != -1) {
					result.height = structure.getHeight();
					if (getDetailsAccessMode() instanceof EmbeddedItemDetailsAccessMode) {
						EmbeddedItemDetailsAccessMode embeddedItemDetailsAccessMode = (EmbeddedItemDetailsAccessMode) getDetailsAccessMode();
						if ((embeddedItemDetailsAccessMode
								.getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.TOP)
								|| (embeddedItemDetailsAccessMode
										.getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.BOTTOM)) {
							result.height = Math.round(result.height / 2f);
						}
					}
				}
				return result;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension result = super.getMinimumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.min(result.height, preferredSize.height);
					}
				}
				return result;

			}

			@Override
			public Dimension getMaximumSize() {
				Dimension result = super.getMaximumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.max(result.height, preferredSize.height);
					}
				}
				return result;
			}

		};
	}

	protected ControlScrollPane createDetailsAreaScrollPane(Component view) {
		return new ControlScrollPane(view) {
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
				IListStructuralInfo structure = getRootStructuralInfo();
				if (structure.getHeight() != -1) {
					result.height = structure.getHeight();
					if (getDetailsAccessMode() instanceof EmbeddedItemDetailsAccessMode) {
						EmbeddedItemDetailsAccessMode embeddedItemDetailsAccessMode = (EmbeddedItemDetailsAccessMode) getDetailsAccessMode();
						if ((embeddedItemDetailsAccessMode
								.getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.TOP)
								|| (embeddedItemDetailsAccessMode
										.getEmbeddedDetailsAreaPosition() == ItemDetailsAreaPosition.BOTTOM)) {
							result.height = Math.round(result.height / 2f);
						}
					}
				}
				return result;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension result = super.getMinimumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.min(result.height, preferredSize.height);
					}
				}
				return result;

			}

			@Override
			public Dimension getMaximumSize() {
				Dimension result = super.getMaximumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.max(result.height, preferredSize.height);
					}
				}
				return result;
			}

		};
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		IListStructuralInfo structure = getRootStructuralInfo();
		if (structure.getWidth() != -1) {
			result.width = structure.getWidth();
		}
		return result;
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		if (result != null) {
			Dimension preferredSize = getPreferredSize();
			if (preferredSize != null) {
				result.width = Math.min(result.width, preferredSize.width);
			}
		}
		return result;

	}

	@Override
	public Dimension getMaximumSize() {
		Dimension result = super.getMaximumSize();
		if (result != null) {
			Dimension preferredSize = getPreferredSize();
			if (preferredSize != null) {
				result.width = Math.max(result.width, preferredSize.width);
			}
		}
		return result;
	}

	protected AbstractBufferedItemPositionFactory createItemPositionfactory() {
		return new BufferedItemPositionFactory(listData, this);
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

	public List<BufferedItemPosition> getRootListItemPositions() {
		List<ItemPosition> result = itemPositionFactory.getRootItemPositions();
		if (result == null) {
			return null;
		}
		return MiscUtils.<ItemPosition, BufferedItemPosition>convertCollectionUnsafely(result);
	}

	public BufferedItemPosition getRootListItemPosition(int index) {
		return itemPositionFactory.getRootItemPosition(index);
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	protected void customizeCellRendererComponent(JLabel label, ItemNode node, int rowIndex, int columnIndex,
			boolean isSelected, boolean hasFocus) {
		label.putClientProperty("html.disable", Boolean.TRUE);
		if (getItemPositionByNode(node) == null) {
			return;
		}
		String text = getCellValue(node, columnIndex);
		if ((text == null) || (text.length() == 0)) {
			label.setText(" ");
			label.setToolTipText(null);
		} else {
			label.setText(text.replaceAll(MiscUtils.getNewLineRegex(), " "));
			label.setToolTipText(SwingRendererUtils.adaptToolTipTextToMultiline(text));
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
			BufferedItemPosition itemPosition = getItemPositionByNode(node);
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
		Image result = null;
		BufferedItemPosition itemPosition = getItemPositionByNode(node);
		if (columnIndex == 0) {
			result = swingRenderer.getObjectIconImage(itemPosition.getItem());
			Image overlayImage = getCellIconOverlayImage(node);
			if (overlayImage != null) {
				BufferedImage overlayedResult = new BufferedImage(
						(result != null) ? result.getWidth(null) : overlayImage.getWidth(null),
						(result != null) ? result.getHeight(null) : overlayImage.getHeight(null),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = overlayedResult.createGraphics();
				if (result != null) {
					g.drawImage(result, 0, 0, null);
				}
				int drawY = (result != null) ? (result.getHeight(null) - overlayImage.getHeight(null)) : 0;
				g.drawImage(overlayImage, 0, drawY, null);
				g.dispose();
				result = overlayedResult;
			}
		}
		return result;
	}

	protected Image getCellIconOverlayImage(ItemNode node) {
		final BufferedItemPosition itemPosition = getItemPositionByNode(node);
		final boolean[] nodeValid = new boolean[] { true };
		final boolean[] subtreeValid = new boolean[] { true };
		visitItems(new IItemsVisitor() {
			@Override
			public VisitStatus visitItem(BufferedItemPosition visitedItemPosition) {
				if (!visitedItemPosition.getContainingListType()
						.isItemNodeValidityDetectionEnabled(visitedItemPosition)) {
					return VisitStatus.SUBTREE_VISIT_INTERRUPTED;
				}
				if (swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy().getValidationError(
						swingRenderer.getLastValidationErrors(), visitedItemPosition.getItem()) != null) {
					subtreeValid[0] = false;
					if (visitedItemPosition.equals(itemPosition)) {
						nodeValid[0] = false;
					}
					return VisitStatus.TREE_VISIT_INTERRUPTED;
				}
				return VisitStatus.VISIT_NOT_INTERRUPTED;
			}
		}, node);
		if (!nodeValid[0]) {
			return SwingRendererUtils.ERROR_OVERLAY_ICON.getImage();
		}
		if (!subtreeValid[0]) {
			return SwingRendererUtils.WEAK_ERROR_OVERLAY_ICON.getImage();
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
			if ((listProperty.getDisplayMode() == DisplayMode.CONTEXT_MENU)
					|| (listProperty.getDisplayMode() == DisplayMode.TOOLBAR_AND_CONTEXT_MENU)) {
				result.add(createDynamicPropertyHook(listProperty));
			}
		}
		for (IDynamicListAction listAction : getRootListType().getDynamicActions(selection,
				modificationFactoryAccessor)) {
			if ((listAction.getDisplayMode() == DisplayMode.CONTEXT_MENU)
					|| (listAction.getDisplayMode() == DisplayMode.TOOLBAR_AND_CONTEXT_MENU)) {
				result.add(createDynamicActionHook(listAction));
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
		return new ListModificationFactory(anyListItemPosition);
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
				continue;
			}
			ItemNode selectedNode = (ItemNode) path.getLastPathComponent();
			BufferedItemPosition bufferedItemPosition = getItemPositionByNode(selectedNode);
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
		visitItemsInBreadthFirstSearchMode(new IItemsVisitor() {
			@Override
			public VisitStatus visitItem(BufferedItemPosition itemPosition) {
				if (itemPosition.getItem() == item) {
					result[0] = itemPosition;
					return VisitStatus.TREE_VISIT_INTERRUPTED;
				}
				return VisitStatus.VISIT_NOT_INTERRUPTED;
			}
		});
		return result[0];
	}

	public List<BufferedItemPosition> findItemPositionsByValue(Object item) {
		final List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
		visitItemsInBreadthFirstSearchMode(new IItemsVisitor() {
			@Override
			public VisitStatus visitItem(BufferedItemPosition itemPosition) {
				if (itemPosition.getItem().equals(item)) {
					result.add(itemPosition);
				}
				return VisitStatus.VISIT_NOT_INTERRUPTED;
			}
		});
		return result;
	}

	public BufferedItemPosition findFirstItemPositionByValue(Object item) {
		List<BufferedItemPosition> itemPositions = findItemPositionsByValue(item);
		if (itemPositions.size() == 0) {
			return null;
		}
		return itemPositions.get(0);
	}

	protected BufferedItemPosition getItemPositionByNode(ItemNode node) {
		Object userObject = node.getUserObject();
		if (!(userObject instanceof BufferedItemPosition)) {
			return null;
		}
		return (BufferedItemPosition) userObject;
	}

	public void setSelection(List<BufferedItemPosition> toSelect) {
		if (Arrays.equals(getSelection().toArray(), toSelect.toArray())) {
			return;
		}
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

	public boolean isItemPositionExpanded(BufferedItemPosition itemPosition) {
		ItemNode node = findNode(itemPosition);
		if (node == null) {
			return false;
		}
		TreePath treePath = new TreePath(node.getPath());
		return treeTableComponent.isExpanded(treePath);
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

	public void expandItemPositions(int maximumDepth) {
		visitItemsInBreadthFirstSearchMode(new IItemsVisitor() {
			@Override
			public VisitStatus visitItem(BufferedItemPosition itemPosition) {
				if (itemPosition.getDepth() >= maximumDepth) {
					return VisitStatus.TREE_VISIT_INTERRUPTED;
				}
				expandItemPosition(itemPosition);
				return VisitStatus.VISIT_NOT_INTERRUPTED;
			}
		});
	}

	public void expandAllItemPositions() {
		treeTableComponent.expandAll();
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
				BufferedItemPosition itemPosition = getItemPositionByNode(node);
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

	protected void purgePositionsAfterItemRemoval(List<BufferedItemPosition> toUpdate, BufferedItemPosition removed) {
		for (int i = 0; i < toUpdate.size(); i++) {
			BufferedItemPosition itemPosition = toUpdate.get(i);
			if (itemPosition.equals(removed) || itemPosition.getAncestors().contains(removed)) {
				toUpdate.remove(i);
				i--;
			}
		}
	}

	protected void shiftPositionsAfterItemRemoval(List<BufferedItemPosition> toUpdate, BufferedItemPosition removed) {
		for (int i = 0; i < toUpdate.size(); i++) {
			BufferedItemPosition itemPosition = toUpdate.get(i);
			if (itemPosition.getPreviousSiblings().contains(removed)) {
				toUpdate.set(i, itemPosition.getSibling(itemPosition.getIndex() - 1));
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

	protected Object onItemCreationRequest(BufferedItemPosition itemPosition, boolean provideCreationOptions) {
		IListTypeInfo listType = itemPosition.getContainingListType();
		ITypeInfo typeToInstantiate = listType.getItemType();
		if (typeToInstantiate == null) {
			typeToInstantiate = swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class, null));
		}

		BufferedItemPosition parentItemPosition = itemPosition.getParentItemPosition();
		if (parentItemPosition != null) {
			Object parentItem = parentItemPosition.getItem();
			IFieldInfo containingListField = itemPosition.getContainingListFieldIfNotRoot();
			if (containingListField.getAlternativeListItemConstructors(parentItem) != null) {
				typeToInstantiate = new FieldAlternativeListItemConstructorsInstaller(swingRenderer.getReflectionUI(),
						parentItem, containingListField).wrapTypeInfo(typeToInstantiate);
			}
		}

		if (provideCreationOptions) {
			return swingRenderer.onTypeInstantiationRequest(ListControl.this, typeToInstantiate);
		} else {
			return ReflectionUIUtils.createDefaultInstance(typeToInstantiate);
		}
	}

	protected boolean wouldDialogBeDisplayedOnItemCreation(BufferedItemPosition newItemPosition) {
		IListTypeInfo listType = newItemPosition.getContainingListType();
		ItemCreationMode itemCreationMode = listType.getItemCreationMode();
		if (itemCreationMode == ItemCreationMode.UNDEFINED) {
			itemCreationMode = getRelevantItemCreationMode(newItemPosition);
		}
		if ((itemCreationMode == ItemCreationMode.CUSTOM_UNVERIFIED_INSTANCE)
				|| (itemCreationMode == ItemCreationMode.CUSTOM_VERIFIED_INSTANCE)) {
			if (listType.isItemNullValueSupported()) {
				return true;
			}
			ITypeInfo typeToInstantiate = listType.getItemType();
			if (typeToInstantiate == null) {
				typeToInstantiate = swingRenderer.getReflectionUI()
						.getTypeInfo(new JavaTypeInfoSource(Object.class, null));
			}
			if (swingRenderer.isDecisionRequiredOnTypeInstantiationRequest(typeToInstantiate)) {
				return true;
			}
		}
		if ((itemCreationMode == ItemCreationMode.VERIFIED_NULL)
				|| (itemCreationMode == ItemCreationMode.DEFAULT_VERIFIED_INSTANCE)
				|| (itemCreationMode == ItemCreationMode.CUSTOM_VERIFIED_INSTANCE)) {
			return true;
		}
		return false;
	};

	protected ItemCreationMode getRelevantItemCreationMode(BufferedItemPosition newItemPosition) {
		if ((newItemPosition.getContainingListType().getItemType() != null) && ReflectionUIUtils
				.canCreateDefaultInstance(newItemPosition.getContainingListType().getItemType(), false)) {
			if (getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
				return ItemCreationMode.DEFAULT_VERIFIED_INSTANCE;
			} else {
				return ItemCreationMode.DEFAULT_UNVERIFIED_INSTANCE;
			}
		} else {
			if (getDetailsAccessMode().hasDetachedDetailsDisplayOption()) {
				return ItemCreationMode.CUSTOM_VERIFIED_INSTANCE;
			} else {
				return ItemCreationMode.CUSTOM_UNVERIFIED_INSTANCE;
			}
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
		}.getNewCapsule();
		ITypeInfo encapsulatedObjectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(capsule));
		return encapsulatedObjectType.getCaption();
	}

	protected AbstractStandardListAction createCopyAction() {
		return new CopyAction();
	}

	protected AbstractStandardListAction createCutAction() {
		return new CutAction();
	}

	protected BufferedItemPosition getPositionSelectedByItemRemoval(BufferedItemPosition itemPosition) {
		if (itemPosition.getIndex() > 0) {
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
		if (getRootListTitle().length() == 0) {
			return null;
		}
		return "Edit '" + getRootListTitle() + "' item";
	}

	public void addListControlSelectionListener(Listener<List<BufferedItemPosition>> listener) {
		selectionListeners.add(listener);
	}

	public void removeListControlSelectionListener(Listener<List<BufferedItemPosition>> listener) {
		selectionListeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public Listener<List<BufferedItemPosition>>[] getListControlSelectionListeners() {
		return selectionListeners.toArray(new Listener[selectionListeners.size()]);
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
					swingRenderer.handleException(ListControl.this, t);
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
			if (!getDetailsAccessMode().hasEmbeddedDetailsDisplayArea() || !listType.canViewItemDetails()) {
				singleSelection = null;
			}
		}
		if ((detailsControlItemPosition == null) && (singleSelection == null)) {
			return;
		}
		try {
			if ((detailsControlItemPosition != null) && (singleSelection != null)) {
				if (!detailsControlItemPosition.equals(singleSelection)) {
					detailsControlItemPosition = singleSelection;
					detailsControlBuilder.setPosition(detailsControlItemPosition);
				}
				detailsControlBuilder.reloadValue(detailsControl, refreshStructure);
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
						if (undoModif.isVolatile()) {
							return undoModif;
						}
						Object oldItem = detailsControlItemPosition.getItem();
						Object newItem = detailsControlBuilder.getCurrentValue();
						IModification preSelection = new SelectItemModification(detailsControlItemPosition, newItem,
								oldItem, detailsControlBuilder, detailsControl);
						return ModificationStack.createCompositeModification(undoModif.getTitle(), UndoOrder.FIFO,
								preSelection, undoModif);
					}
				});
				detailsArea.setLayout(new BorderLayout());
				Component statusBar = detailsControl.getStatusBar();
				{
					detailsArea.add(statusBar, BorderLayout.NORTH);
				}
				detailsArea.add(detailsControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(detailsArea);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (detailsControlItemPosition != null) {
							scrollTo(detailsControlItemPosition);
						}
						if (isShowing()) {
							detailsControl.validateFormInBackgroundAndReportOnStatusBar();
						}
					}
				});
				return;
			}
		} finally {
			SwingRendererUtils.updateWindowMenu(this, swingRenderer);
		}
		throw new ReflectionUIError();
	}

	protected void displayDetailsOnItemDoubleClick() {
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
					swingRenderer.handleException(treeTableComponent, t);
				}
			}
		});
	}

	protected ListControl getMasterListControl() {
		ListControl firstAncestorListControl = (ListControl) SwingUtilities.getAncestorOfClass(ListControl.class, this);
		if (firstAncestorListControl == null) {
			return null;
		}
		BufferedItemPosition itemPosition = firstAncestorListControl.getSingleSelection();
		if (itemPosition == null) {
			return null;
		}
		if (!Boolean.TRUE.equals(itemPosition.getContainingListType().getSpecificProperties()
				.get(IListTypeInfo.SUB_LIST_SLAVERY_STATUS_KEY))) {
			return null;
		}
		IFieldInfo subListField = itemPosition.getSubListField();
		if (subListField == null) {
			return null;
		}
		if (!MiscUtils.equalsOrBothNull(subListField.getValue(itemPosition.getItem()), listData.getValue())) {
			return null;
		}
		return firstAncestorListControl;
	}

	protected BufferedItemPosition toMasterListControlItemPosition(BufferedItemPosition itemPosition) {
		ListControl masterListControl = getMasterListControl();
		if (masterListControl == null) {
			return null;
		}
		BufferedItemPosition masterSingleSelection = masterListControl.getSingleSelection();
		if (masterSingleSelection == null) {
			return null;
		}
		if (itemPosition.isRoot()) {
			return masterSingleSelection.getSubItemPosition(itemPosition.getIndex());
		} else {
			return toMasterListControlItemPosition(itemPosition.getParentItemPosition())
					.getSubItemPosition(itemPosition.getIndex());
		}
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
		Runnable action = new Runnable() {
			@Override
			public void run() {
				valuesByNode.clear();
				rootNode = createRootNode();
				treeTableComponent.setTreeTableModel(createTreeTableModel());
			}
		};
		if (refreshStructure) {
			action.run();
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
		} else {
			restoringColumnWidthsAsMuchAsPossible(action);
		}
		treeTableComponent.setOpaque(listData.getValue() != null);
	}

	public void visitItems(IItemsVisitor iItemsVisitor) {
		visitItems(iItemsVisitor, rootNode);
	}

	protected VisitStatus visitItems(IItemsVisitor itemsVisitor, ItemNode currentNode) {
		BufferedItemPosition currentItemPosition = getItemPositionByNode(currentNode);
		final VisitStatus currentItemVisitStatus;
		if (currentItemPosition != null) {
			currentItemVisitStatus = itemsVisitor.visitItem(currentItemPosition);
			if (currentItemVisitStatus == VisitStatus.TREE_VISIT_INTERRUPTED) {
				return VisitStatus.TREE_VISIT_INTERRUPTED;
			}
		} else {
			currentItemVisitStatus = VisitStatus.VISIT_NOT_INTERRUPTED;
		}
		VisitStatus finalItemVisitStatus = currentItemVisitStatus;
		if (currentItemVisitStatus != VisitStatus.SUBTREE_VISIT_INTERRUPTED) {
			for (int i = 0; i < currentNode.getChildCount(); i++) {
				ItemNode childNode = (ItemNode) currentNode.getChildAt(i);
				VisitStatus childItemVisitStatus = visitItems(itemsVisitor, childNode);
				if (childItemVisitStatus == VisitStatus.TREE_VISIT_INTERRUPTED) {
					return VisitStatus.TREE_VISIT_INTERRUPTED;
				}
				if (childItemVisitStatus == VisitStatus.SUBTREE_VISIT_INTERRUPTED) {
					if (finalItemVisitStatus == VisitStatus.VISIT_NOT_INTERRUPTED) {
						finalItemVisitStatus = VisitStatus.SUBTREE_VISIT_INTERRUPTED;
					}
				}
			}
		}
		return finalItemVisitStatus;
	}

	public void visitItemsInBreadthFirstSearchMode(IItemsVisitor iItemsVisitor) {
		visitItemsInBreadthFirstSearchMode(iItemsVisitor, rootNode, new LinkedList<ListControl.ItemNode>());
	}

	protected VisitStatus visitItemsInBreadthFirstSearchMode(IItemsVisitor itemsVisitor, ItemNode currentNode,
			Queue<ItemNode> queue) {
		BufferedItemPosition currentItemPosition = getItemPositionByNode(currentNode);
		final VisitStatus currentItemVisitStatus;
		if (currentItemPosition != null) {
			currentItemVisitStatus = itemsVisitor.visitItem(currentItemPosition);
			if (currentItemVisitStatus == VisitStatus.TREE_VISIT_INTERRUPTED) {
				return VisitStatus.TREE_VISIT_INTERRUPTED;
			}
		} else {
			currentItemVisitStatus = VisitStatus.VISIT_NOT_INTERRUPTED;
		}
		if (currentItemVisitStatus != VisitStatus.SUBTREE_VISIT_INTERRUPTED) {
			for (int i = 0; i < currentNode.getChildCount(); i++) {
				ItemNode childNode = (ItemNode) currentNode.getChildAt(i);
				queue.add(childNode);
			}
		}
		VisitStatus finalItemVisitStatus = currentItemVisitStatus;
		while (!queue.isEmpty()) {
			ItemNode nextNode = queue.poll();
			VisitStatus nextItemVisitStatus = visitItemsInBreadthFirstSearchMode(itemsVisitor, nextNode, queue);
			if (nextItemVisitStatus == VisitStatus.TREE_VISIT_INTERRUPTED) {
				return VisitStatus.TREE_VISIT_INTERRUPTED;
			}
			if (nextItemVisitStatus == VisitStatus.SUBTREE_VISIT_INTERRUPTED) {
				if (finalItemVisitStatus == VisitStatus.VISIT_NOT_INTERRUPTED) {
					finalItemVisitStatus = VisitStatus.SUBTREE_VISIT_INTERRUPTED;
				}
			}
		}
		return finalItemVisitStatus;
	}

	@Override
	public boolean refreshUI(final boolean refreshStructure) {
		listData.returningValue(listData.getValue(), new Runnable() {
			@Override
			public void run() {
				if (refreshStructure) {
					removeAll();
					detailsMode = null;
					layoutControls();
					refreshTreeTableScrollPaneBorder();
					refreshTreeTableComponentStyle();
					refreshTreeTableComponentHeader();
					refreshRendrers();
				}
				restoringSelectionDespiteDataAlteration(new Runnable() {
					@Override
					public void run() {
						restoringExpandedPathsDespiteDataAlteration(new Runnable() {
							@Override
							public void run() {
								refreshItemPositionBuffers();
								refreshTreeTableModelAndControl(refreshStructure);
							}

						});
					}
				});
				if (getDetailsAccessMode().hasEmbeddedDetailsDisplayArea()) {
					updateDetailsArea(refreshStructure);
				}
				updateToolbar();
				if (refreshStructure) {
					SwingRendererUtils.handleComponentSizeChange(ListControl.this);
				}
			}
		});
		return true;
	}

	protected void withoutSelectionTargetListenerEnabled(Runnable runnable) {
		boolean oldSelectionAccessDataListenerEnabled = selectionTargetListenerEnabled;
		selectionTargetListenerEnabled = false;
		try {
			runnable.run();
		} finally {
			selectionTargetListenerEnabled = oldSelectionAccessDataListenerEnabled;
		}
	}

	protected void withoutSelectionListenersEnabled(Runnable runnable) {
		boolean oldSelectionListenersEnabled = selectionListenersEnabled;
		selectionListenersEnabled = false;
		try {
			runnable.run();
		} finally {
			selectionListenersEnabled = oldSelectionListenersEnabled;
		}
	}

	protected void updateSelectionTargetOnSelectionChange() {
		selectionListeners.add(new Listener<List<BufferedItemPosition>>() {
			@Override
			public void handle(List<BufferedItemPosition> event) {
				if (!selectionTargetListenerEnabled) {
					return;
				}
				updateSelectionTarget();
			}
		});
	}

	protected void updateSelectionTarget() {
		if (selectionTargetData != null) {
			BufferedItemPosition selectedItemPosition = getSingleSelection();
			Object value;
			if (selectedItemPosition == null) {
				value = null;
			} else {
				value = selectedItemPosition.getItem();
			}
			if (!selectionTargetData.getType().supports(value)) {
				if (selectionTargetData.getType().supports(null)) {
					value = null;
				} else {
					try {
						value = ReflectionUIUtils.createDefaultInstance(selectionTargetData.getType());
					} catch (Throwable t) {
						throw new ReflectionUIError(
								"Failed to get the value that would be used to update the selection target: "
										+ t.toString(),
								t);
					}
				}
			}
			ReflectionUIUtils.setFieldValueThroughModificationStack(selectionTargetData, value, getModificationStack(),
					ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
		}
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
		SwingRendererUtils.showFieldCaptionOnBorder(listData, treeTableComponentScrollPane, new Accessor<Border>() {
			@Override
			public Border get() {
				if (listData.getBorderColor() != null) {
					return BorderFactory.createLineBorder(SwingRendererUtils.getColor(listData.getBorderColor()));
				} else {
					return new ControlScrollPane().getBorder();
				}
			}
		}, swingRenderer);
	}

	protected void refreshRendrers() {
		treeTableComponent.setDefaultRenderer(Object.class, createTableCellRenderer());
		treeTableComponent.setTreeCellRenderer(createTreeCellRenderer());
	}

	protected void refreshTreeTableComponentStyle() {
		if (listData.getEditorBackgroundColor() != null) {
			treeTableComponent.setBackground(SwingRendererUtils.getColor(listData.getEditorBackgroundColor()));
		} else {
			treeTableComponent.setBackground(new JXTreeTable().getBackground());
		}
		if (listData.getEditorForegroundColor() != null) {
			treeTableComponent.setForeground(SwingRendererUtils.getColor(listData.getEditorForegroundColor()));
		} else {
			treeTableComponent.setForeground(new JXTreeTable().getForeground());
		}
		if (listData.getEditorCustomFontResourcePath() != null) {
			treeTableComponent
					.setFont(SwingRendererUtils
							.loadFontThroughCache(listData.getEditorCustomFontResourcePath(),
									ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
							.deriveFont(treeTableComponent.getFont().getStyle(),
									treeTableComponent.getFont().getSize()));
		} else {
			treeTableComponent.setFont(new JXTreeTable().getFont());
		}
	}

	protected void refreshTreeTableComponentHeader() {
		if (listData.getNonEditableBackgroundColor() != null) {
			treeTableComponent.getTableHeader()
					.setBackground(SwingRendererUtils.getColor(listData.getNonEditableBackgroundColor()));
		} else {
			treeTableComponent.getTableHeader().setBackground(new JXTreeTable().getTableHeader().getBackground());
		}
		if (listData.getNonEditableForegroundColor() != null) {
			treeTableComponent.getTableHeader()
					.setForeground(SwingRendererUtils.getColor(listData.getNonEditableForegroundColor()));
		} else {
			treeTableComponent.getTableHeader().setForeground(new JXTreeTable().getTableHeader().getForeground());
		}
		if (listData.getLabelCustomFontResourcePath() != null) {
			treeTableComponent.getTableHeader()
					.setFont(SwingRendererUtils
							.loadFontThroughCache(listData.getLabelCustomFontResourcePath(),
									ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
							.deriveFont(treeTableComponent.getTableHeader().getFont().getStyle(),
									treeTableComponent.getTableHeader().getFont().getSize()));
		} else {
			treeTableComponent.getTableHeader().setFont(new JTableHeader().getFont());
		}
	}

	@Override
	public boolean requestCustomFocus() {
		if (selectionTargetData == null) {
			setDefaultSelection();
		}
		if (SwingRendererUtils.requestAnyComponentFocus(treeTableComponent, swingRenderer)) {
			return true;
		}
		return false;
	}

	@Override
	public void validateControl(ValidationSession session) throws Exception {
		final Map<BufferedItemPosition, Exception> validitionErrorByItemPosition = new HashMap<BufferedItemPosition, Exception>();
		visitItems(new IItemsVisitor() {
			@Override
			public VisitStatus visitItem(BufferedItemPosition itemPosition) {
				if (Thread.currentThread().isInterrupted()) {
					return VisitStatus.TREE_VISIT_INTERRUPTED;
				}
				if (!itemPosition.getContainingListType().isItemNodeValidityDetectionEnabled(itemPosition)) {
					return VisitStatus.SUBTREE_VISIT_INTERRUPTED;
				}
				ItemUIBuilder itemUIBuilder = new ItemUIBuilder(itemPosition);
				Form[] itemForm = new Form[1];
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							itemForm[0] = itemUIBuilder.createEditorForm(false, false);
						}
					});
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return VisitStatus.TREE_VISIT_INTERRUPTED;
				}
				try {
					itemForm[0].validateForm(session);
					if (!Thread.currentThread().isInterrupted()) {
						swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy().cancelAttribution(
								swingRenderer.getLastValidationErrors(), session, itemPosition.getItem());
					}
				} catch (Exception e) {
					swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy()
							.attribute(swingRenderer.getLastValidationErrors(), session, itemPosition.getItem(), e);
					validitionErrorByItemPosition.put(itemPosition, e);
				}
				return VisitStatus.VISIT_NOT_INTERRUPTED;
			}
		});
		if (Thread.currentThread().isInterrupted()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				refreshRendrers();
				treeTableComponent.repaint();
			}
		});
		if (validitionErrorByItemPosition.size() > 0) {
			throw new ListValidationError(validitionErrorByItemPosition);
		}
	}

	protected String getDisplayPath(BufferedItemPosition itemPosition) {
		return MiscUtils
				.getReverse(MiscUtils.getAdded(Collections.singletonList(itemPosition), itemPosition.getAncestors()))
				.stream().map(eachItemPosition -> getCellValue(findNode((BufferedItemPosition) eachItemPosition), 0))
				.collect(Collectors.joining(" / "));
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
		if (detailsControl != null) {
			detailsControl.addMenuContributionTo(menuModel);
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

	protected void preventingIntermediarySelectionEvents(Runnable runnable) {
		Multiset<Object> oldSelectionBag = HashMultiset.create();
		for (BufferedItemPosition itemPosition : getSelection()) {
			if (itemPosition.isStable()) {
				oldSelectionBag.add(itemPosition);
			} else {
				List<Object> itemAndAncestors = new ArrayList<Object>();
				itemAndAncestors.add(itemPosition.getItem());
				itemAndAncestors.addAll(ReflectionUIUtils.collectItemAncestors(itemPosition));
				oldSelectionBag.add(itemAndAncestors);
			}
		}
		withoutSelectionListenersEnabled(new Runnable() {
			@Override
			public void run() {
				runnable.run();
			}
		});
		Multiset<Object> newSelectionBag = HashMultiset.create();
		for (BufferedItemPosition itemPosition : getSelection()) {
			if (itemPosition.isStable()) {
				newSelectionBag.add(itemPosition);
			} else {
				List<Object> itemAndAncestors = new ArrayList<Object>();
				itemAndAncestors.add(itemPosition.getItem());
				itemAndAncestors.addAll(ReflectionUIUtils.collectItemAncestors(itemPosition));
				newSelectionBag.add(itemAndAncestors);
			}
		}
		if (!newSelectionBag.equals(oldSelectionBag)) {
			fireSelectionEvent();
		}
	}

	protected void restoringSelectionDespiteDataAlteration(Runnable runnable) {
		final List<BufferedItemPosition> wereSelectedPositions = getSelection();
		final List<Object> wereSelected = new ArrayList<Object>();
		final List<List<Object>> wereSelectedAncestorLists = new ArrayList<List<Object>>();
		for (int i = 0; i < wereSelectedPositions.size(); i++) {
			BufferedItemPosition wasSelectedPosition = wereSelectedPositions.get(i);
			wereSelected.add(wasSelectedPosition.getItem());
			wereSelectedAncestorLists.add(ReflectionUIUtils.collectItemAncestors(wasSelectedPosition));
		}
		preventingIntermediarySelectionEvents(new Runnable() {
			public void run() {
				runnable.run();
				List<BufferedItemPosition> willBeSelectedPositions = ReflectionUIUtils
						.actualizeItemPositions(wereSelectedPositions, wereSelected, wereSelectedAncestorLists);
				setSelection(willBeSelectedPositions);
			}
		});
	}

	protected void restoringExpandedPathsDespiteDataAlteration(Runnable runnable) {
		List<BufferedItemPosition> wereExpandedPositions = getExpandedItemPositions(null);
		List<Object> wereExpanded = new ArrayList<Object>();
		List<List<Object>> wereExpandedAncestorLists = new ArrayList<List<Object>>();
		for (int i = 0; i < wereExpandedPositions.size(); i++) {
			BufferedItemPosition wasExpandedPosition = wereExpandedPositions.get(i);
			wereExpanded.add(wasExpandedPosition.getItem());
			wereExpandedAncestorLists.add(ReflectionUIUtils.collectItemAncestors(wasExpandedPosition));
		}
		runnable.run();
		collapseAllItemPositions();
		List<BufferedItemPosition> willBeExpandedPositions = ReflectionUIUtils
				.actualizeItemPositions(wereExpandedPositions, wereExpanded, wereExpandedAncestorLists);
		for (BufferedItemPosition itemPosition : willBeExpandedPositions) {
			expandItemPosition(itemPosition);
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
		protected Accessor<List<BufferedItemPosition>> newSelectionGetter;
		protected Accessor<List<BufferedItemPosition>> oldSelectionGetter;

		public RefreshStructureModification(Accessor<List<BufferedItemPosition>> newSelectionGetter,
				Accessor<List<BufferedItemPosition>> oldSelectionGetter) {
			this.newSelectionGetter = newSelectionGetter;
			this.oldSelectionGetter = oldSelectionGetter;
		}

		@Override
		public IModification applyAndGetOpposite(ModificationStack modificationStack) {
			preventingIntermediarySelectionEvents(new Runnable() {
				@Override
				public void run() {
					restoringExpandedPathsDespiteDataAlteration(new Runnable() {
						@Override
						public void run() {
							refreshItemPositionBuffers();
							refreshTreeTableModelAndControl(false);
						}
					});
					if (newSelectionGetter != null) {
						List<BufferedItemPosition> newSelection = newSelectionGetter.get();
						if (newSelection != null) {
							setSelection(newSelection);
						}
					}
				}
			});
			return new RefreshStructureModification(oldSelectionGetter, newSelectionGetter);
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
		public boolean isVolatile() {
			return false;
		}

		@Override
		public boolean isComposite() {
			return false;
		}
	}

	protected class TreeTable extends JXTreeTable {
		private static final long serialVersionUID = 1L;

		public TreeTable() {
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			try {
				return super.getToolTipText(event);
			} catch (Throwable t) {
				return null;
			}
		}
	}

	protected class SelectItemModification extends AbstractModification {

		protected BufferedItemPosition currentPosition;
		protected Object doItem;
		protected Object undoItem;
		protected ItemUIBuilder detailsControlBuilder;
		protected Form detailsControl;

		public SelectItemModification(BufferedItemPosition currentPosition, Object doItem, Object undoItem,
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

		@Override
		protected Runnable createRedoJob() {
			return createAnyJob(doItem);
		}

		protected Runnable createAnyJob(final Object currentItem) {
			return new Runnable() {
				@Override
				public void run() {
					Object[] containingListRawValue = currentPosition.retrieveContainingListRawValue();
					List<BufferedItemPosition> toSelect = ReflectionUIUtils.actualizeItemPositions(
							Collections.singletonList(currentPosition), Collections.singletonList(currentItem),
							Collections.singletonList(ReflectionUIUtils.collectItemAncestors(currentPosition)));
					setSelection(toSelect);
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
						detailsControlBuilder.reloadValue(detailsControl, false);
					}
				}
			};
		}

	}

	protected class ItemTableCellRenderer implements TableCellRenderer {

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

	protected class ItemTreeCellRenderer implements TreeCellRenderer {

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
			if (listData.getEditorCustomFontResourcePath() != null) {
				component.setFont(SwingRendererUtils
						.loadFontThroughCache(listData.getEditorCustomFontResourcePath(),
								ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
						.deriveFont(component.getFont().getStyle(), component.getFont().getSize()));
			}
			component.setOpaque(false);
			customizeCellRendererComponent(component, (ItemNode) value, row, 0, selected, focused);
			return component;
		}

	}

	protected abstract class AbstractStandardListAction extends AbstractAction {

		protected static final long serialVersionUID = 1L;

		protected abstract boolean prepare();

		protected abstract void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder);

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
				final Accessor<List<BufferedItemPosition>> defaultPostSelectionGetter = new Accessor<List<BufferedItemPosition>>() {
					List<BufferedItemPosition> oldItemPositions = getSelection();
					List<Object> oldItems = new ArrayList<Object>();
					List<List<Object>> oldItemAncestorLists = new ArrayList<List<Object>>();
					{
						for (BufferedItemPosition itemPosition : oldItemPositions) {
							oldItems.add(itemPosition.getItem());
							oldItemAncestorLists.add(ReflectionUIUtils.collectItemAncestors(itemPosition));
						}
					}

					@Override
					public List<BufferedItemPosition> get() {
						return ReflectionUIUtils.actualizeItemPositions(oldItemPositions, oldItems,
								oldItemAncestorLists);
					}
				};
				if (!prepare()) {
					return;
				}
				final String modifTitle = getCompositeModificationTitle();
				@SuppressWarnings("unchecked")
				final Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder = new Accessor[] {
						defaultPostSelectionGetter };
				if (modifTitle == null) {
					perform(postSelectionGetterHolder);
					new RefreshStructureModification(postSelectionGetterHolder[0], null)
							.applyAndGetOpposite(ModificationStack.DUMMY_MODIFICATION_STACK);
				} else {
					final ModificationStack modifStack = getModificationStack();
					modifStack.insideComposite(modifTitle, UndoOrder.FIFO, new Accessor<Boolean>() {
						@Override
						public Boolean get() {
							if (modifStack.insideComposite(modifTitle + " (without list control update)",
									UndoOrder.getNormal(), new Accessor<Boolean>() {
										@Override
										public Boolean get() {
											perform(postSelectionGetterHolder);
											return true;
										}
									}, listData.isTransient())) {
								modifStack.apply(new RefreshStructureModification(postSelectionGetterHolder[0],
										defaultPostSelectionGetter));
								return true;
							} else {
								new RefreshStructureModification(postSelectionGetterHolder[0], null)
										.applyAndGetOpposite(ModificationStack.DUMMY_MODIFICATION_STACK);
								return modifStack.wasInvalidated();
							}
						}
					}, listData.isTransient());

				}
				displayResult();
			} catch (Throwable t) {
				swingRenderer.handleException(ListControl.this, t);
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
		public Form createEditorForm(boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
			Form result = super.createEditorForm(realTimeLinkWithParent, exclusiveLinkWithParent);
			postCopyValidationErrorFromCapsuleToItem(result);
			return result;
		}

		protected void postCopyValidationErrorFromCapsuleToItem(Form form) {
			form.getRefreshListeners().add(new Form.IRefreshListener() {
				@Override
				public void onRefresh(boolean refreshStructure) {
					itemValidationErrorsCollectingExecutor.submit(new Runnable() {
						@Override
						public void run() {
							BetterFutureTask<Boolean> validationTask = form.getCurrentValidationTask();
							if (validationTask != null) {
								try {
									validationTask.get();
								} catch (CancellationException | InterruptedException | ExecutionException e) {
									return;
								}
							}
							copyValidationErrorFromCapsuleToItem(form.getObject());
						}
					});
				}
			});
		}

		protected void copyValidationErrorFromCapsuleToItem(Object capsule) {
			Exception validitionError = swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy()
					.getValidationError(swingRenderer.getLastValidationErrors(), capsule);
			if (validitionError != null) {
				swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy().attribute(
						swingRenderer.getLastValidationErrors(), null, bufferedItemPosition.getItem(), validitionError);
			} else {
				swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy().cancelAttribution(
						swingRenderer.getLastValidationErrors(), null, bufferedItemPosition.getItem());
			}
		}

		protected void copyValidationErrorFromItemToCapsule(Object capsule) {
			Exception itemValiditionError = swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy()
					.getValidationError(swingRenderer.getLastValidationErrors(), bufferedItemPosition.getItem());
			if (itemValiditionError != null) {
				swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy()
						.attribute(swingRenderer.getLastValidationErrors(), null, capsule, itemValiditionError);
			} else {
				swingRenderer.getReflectionUI().getValidationErrorAttributionStrategy()
						.cancelAttribution(swingRenderer.getLastValidationErrors(), null, capsule);
			}
		}

		@Override
		public Object getNewCapsule() {
			Object capsule = super.getNewCapsule();
			copyValidationErrorFromItemToCapsule(capsule);
			return capsule;
		}

		@Override
		public void reloadValue(Form editorForm, boolean refreshStructure) {
			Object capsule = editorForm.getObject();
			copyValidationErrorFromItemToCapsule(capsule);
			super.reloadValue(editorForm, refreshStructure);
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
		protected boolean isEncapsulatedValueValidityDetectionEnabled() {
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
						Object item = newItem;

						@Override
						public List<BufferedItemPosition> get() {
							return ReflectionUIUtils.actualizeItemPositions(
									Collections.singletonList(bufferedItemPosition), Collections.singletonList(item),
									Collections.singletonList(
											ReflectionUIUtils.collectItemAncestors(bufferedItemPosition)));
						}
					}, new Accessor<List<BufferedItemPosition>>() {
						Object item = bufferedItemPosition.getItem();

						@Override
						public List<BufferedItemPosition> get() {
							return ReflectionUIUtils.actualizeItemPositions(
									Collections.singletonList(bufferedItemPosition), Collections.singletonList(item),
									Collections.singletonList(
											ReflectionUIUtils.collectItemAncestors(bufferedItemPosition)));
						}
					});
			return ModificationStack.createCompositeModification(update.getTitle(), UndoOrder.FIFO, update,
					structureRefreshing);
		}

		@Override
		protected IModification createUndoModificationsReplacement() {
			return ReflectionUIUtils.createUndoModificationsReplacement(listData);
		}

		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			swingRenderer.handleException(ListControl.this, t);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected Runnable getParentControlRefreshJob() {
			return new Runnable() {
				@Override
				public void run() {
					ListControl.this.refreshUI(false);
				}
			};
		}

		@Override
		protected String getParentModificationTitle() {
			return getItemModificationTitle();
		}

		@Override
		protected boolean isParentModificationVolatile() {
			return listData.isTransient();
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			ITypeInfo itemType = bufferedItemPosition.getContainingListType().getItemType();
			if (itemType != null) {
				return itemType.getSource();
			}
			return new JavaTypeInfoSource(Object.class, null);
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
			return getStructuralInfo(bufferedItemPosition).getItemDetailsInfoFilter(bufferedItemPosition);
		}

		@Override
		protected boolean isValueKnownAsImmutable() {
			return false;
		}

	}

	public interface IItemsVisitor {

		VisitStatus visitItem(BufferedItemPosition itemPosition);

		public enum VisitStatus {
			VISIT_NOT_INTERRUPTED, SUBTREE_VISIT_INTERRUPTED, TREE_VISIT_INTERRUPTED
		}

	}

	protected enum InsertPosition {
		AFTER, BEFORE, UNKNOWN
	}

	protected class AddChildAction extends AbstractStandardListAction {

		private static final long serialVersionUID = 1L;

		protected BufferedItemPosition newSubItemPosition;
		protected Object newSubListItem;
		protected IListTypeInfo subListType;

		@Override
		protected boolean prepare() {
			newSubItemPosition = getNewSubItemPosition();
			subListType = newSubItemPosition.getContainingListType();
			ItemCreationMode itemCreationMode = subListType.getItemCreationMode();
			if (itemCreationMode == ItemCreationMode.UNDEFINED) {
				itemCreationMode = getRelevantItemCreationMode(newSubItemPosition);
			}
			if ((itemCreationMode == ItemCreationMode.UNVERIFIED_NULL)
					|| (itemCreationMode == ItemCreationMode.VERIFIED_NULL)) {
				newSubListItem = null;
			} else if ((itemCreationMode == ItemCreationMode.DEFAULT_UNVERIFIED_INSTANCE)
					|| (itemCreationMode == ItemCreationMode.DEFAULT_VERIFIED_INSTANCE)) {
				newSubListItem = onItemCreationRequest(newSubItemPosition, false);
			} else if ((itemCreationMode == ItemCreationMode.CUSTOM_UNVERIFIED_INSTANCE)
					|| (itemCreationMode == ItemCreationMode.CUSTOM_VERIFIED_INSTANCE)) {
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
					newSubListItem = onItemCreationRequest(newSubItemPosition, true);
					if (newSubListItem == null) {
						return false;
					}
				}
			} else {
				throw new ReflectionUIError();
			}
			if ((itemCreationMode == ItemCreationMode.VERIFIED_NULL)
					|| (itemCreationMode == ItemCreationMode.DEFAULT_VERIFIED_INSTANCE)
					|| (itemCreationMode == ItemCreationMode.CUSTOM_VERIFIED_INSTANCE)) {
				ItemUIBuilder dialogBuilder = openAnticipatedItemDialog(newSubItemPosition, newSubListItem);
				if (dialogBuilder.isCancelled()) {
					return false;
				}
				newSubListItem = dialogBuilder.getCurrentValue();
			}
			return true;
		}

		@Override
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			final List<Object> newSubListItemAncestors = ReflectionUIUtils.collectItemAncestors(newSubItemPosition);
			getModificationStack().apply(createListModificationFactory(newSubItemPosition)
					.add(newSubItemPosition.getIndex(), newSubListItem));
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return ReflectionUIUtils.actualizeItemPositions(Collections.singletonList(newSubItemPosition),
							Collections.singletonList(newSubListItem),
							Collections.singletonList(newSubListItemAncestors));
				}
			};
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
			if (wouldDialogBeDisplayedOnItemCreation(subItemPosition)) {
				title += "...";
			}
			return title;
		}

		@Override
		protected String getCompositeModificationTitle() {
			if (getRootListTitle().length() == 0) {
				return "Add item";
			}
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			getModificationStack()
					.apply(createListModificationFactory(itemPositionFactory.getRootItemPosition(-1)).clear());
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return Collections.emptyList();
				}
			};
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			List<BufferedItemPosition> selection = getSelection();
			clipboard.clear();
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			List<BufferedItemPosition> selection = getSelection();
			List<BufferedItemPosition> postSelection = new ArrayList<BufferedItemPosition>();
			for (BufferedItemPosition itemPosition : selection) {
				itemPosition = getPositionSelectedByItemRemoval(itemPosition);
				if (itemPosition != null) {
					postSelection.add(itemPosition);
				}
			}
			for (BufferedItemPosition itemPosition : selection) {
				purgePositionsAfterItemRemoval(postSelection, itemPosition);
			}
			List<Object> postSelectionItems = new ArrayList<Object>();
			List<List<Object>> postSelectionItemAncestorLists = new ArrayList<List<Object>>();
			for (BufferedItemPosition itemPosition : postSelection) {
				postSelectionItems.add(itemPosition.getItem());
				postSelectionItemAncestorLists.add(ReflectionUIUtils.collectItemAncestors(itemPosition));
			}
			for (BufferedItemPosition itemPosition : selection) {
				shiftPositionsAfterItemRemoval(postSelection, itemPosition);
			}
			selection = new ArrayList<BufferedItemPosition>(selection);
			Collections.sort(selection);
			Collections.reverse(selection);
			clipboard.clear();
			for (BufferedItemPosition itemPosition : selection) {
				clipboard.add(0, ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), itemPosition.getItem()));
				getModificationStack()
						.apply(createListModificationFactory(itemPosition).remove(itemPosition.getIndex()));
			}
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return ReflectionUIUtils.actualizeItemPositions(postSelection, postSelectionItems,
							postSelectionItemAncestorLists);
				}
			};
		}

		@Override
		protected String getActionTitle() {
			return "Cut";
		}

		@Override
		protected String getCompositeModificationTitle() {
			if (getRootListTitle().length() == 0) {
				return "Cut item(s)";
			}
			return "Cut '" + getRootListTitle() + "' item(s)";
		}

		@Override
		protected boolean isValid() {
			List<BufferedItemPosition> selection = getSelection();
			if (selection.size() > 0) {
				if (canCopyAll(selection) && canRemoveAll(selection)) {
					for (BufferedItemPosition itemPosition1 : selection) {
						for (BufferedItemPosition itemPosition2 : new ArrayList<BufferedItemPosition>(selection)) {
							if (itemPosition1.getAncestors().contains(itemPosition2)) {
								return false;
							}
						}
					}
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
		protected Object newItem;
		protected IListTypeInfo listType;

		public InsertAction(InsertPosition insertPosition) {
			this.insertPosition = insertPosition;
		}

		@Override
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			final List<Object> newItemAncestors = ReflectionUIUtils.collectItemAncestors(newItemPosition);
			getModificationStack()
					.apply(createListModificationFactory(newItemPosition).add(newItemPosition.getIndex(), newItem));
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return ReflectionUIUtils.actualizeItemPositions(Collections.singletonList(newItemPosition),
							Collections.singletonList(newItem), Collections.singletonList(newItemAncestors));
				}
			};
		}

		@Override
		protected boolean prepare() {
			newItemPosition = getNewItemPosition();
			listType = newItemPosition.getContainingListType();
			ItemCreationMode itemCreationMode = listType.getItemCreationMode();
			if (itemCreationMode == ItemCreationMode.UNDEFINED) {
				itemCreationMode = getRelevantItemCreationMode(newItemPosition);
			}
			if ((itemCreationMode == ItemCreationMode.UNVERIFIED_NULL)
					|| (itemCreationMode == ItemCreationMode.VERIFIED_NULL)) {
				newItem = null;
			} else if ((itemCreationMode == ItemCreationMode.DEFAULT_UNVERIFIED_INSTANCE)
					|| (itemCreationMode == ItemCreationMode.DEFAULT_VERIFIED_INSTANCE)) {
				newItem = onItemCreationRequest(newItemPosition, false);
			} else if ((itemCreationMode == ItemCreationMode.CUSTOM_UNVERIFIED_INSTANCE)
					|| (itemCreationMode == ItemCreationMode.CUSTOM_VERIFIED_INSTANCE)) {
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
					newItem = onItemCreationRequest(newItemPosition, true);
					if (newItem == null) {
						return false;
					}
				}
			} else {
				throw new ReflectionUIError();
			}
			if ((itemCreationMode == ItemCreationMode.VERIFIED_NULL)
					|| (itemCreationMode == ItemCreationMode.DEFAULT_VERIFIED_INSTANCE)
					|| (itemCreationMode == ItemCreationMode.CUSTOM_VERIFIED_INSTANCE)) {
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
				if (wouldDialogBeDisplayedOnItemCreation(newItemPosition)
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
							if (!newItemPosition.getContainingListType().areItemsAutomaticallyPositioned()) {
								return true;
							}
						}
						if (insertPosition == InsertPosition.AFTER) {
							if (!newItemPosition.getContainingListType().areItemsAutomaticallyPositioned()) {
								return true;
							}
						}
						if (insertPosition == InsertPosition.UNKNOWN) {
							if (newItemPosition.getContainingListType().areItemsAutomaticallyPositioned()) {
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			avoidRestoringExpandedPathOfMovedNode();
			List<BufferedItemPosition> selection = getSelection();
			if (offset > 0) {
				selection = new ArrayList<BufferedItemPosition>(selection);
				Collections.reverse(selection);
			}
			List<BufferedItemPosition> newSelection = new ArrayList<BufferedItemPosition>();
			List<Object> newSelectionItems = new ArrayList<Object>();
			List<List<Object>> newSelectionItemAncestorLists = new ArrayList<List<Object>>();
			for (BufferedItemPosition itemPosition : selection) {
				int index = itemPosition.getIndex();
				getModificationStack().apply(createListModificationFactory(itemPosition).move(index, offset));
				newSelection.add(itemPosition.getSibling(index + offset));
				newSelectionItems.add(itemPosition.getSibling(index + offset).getItem());
				newSelectionItemAncestorLists
						.add(ReflectionUIUtils.collectItemAncestors(itemPosition.getSibling(index + offset)));
			}
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return ReflectionUIUtils.actualizeItemPositions(newSelection, newSelectionItems,
							newSelectionItemAncestorLists);
				}
			};
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
			if (getRootListTitle().length() == 0) {
				return "Move item(s)";
			}
			return "Move '" + getRootListTitle() + "' item(s)";
		}

		@Override
		protected boolean isValid() {
			List<BufferedItemPosition> selection = getSelection();
			if (selection.size() > 0) {
				if (canMoveAll(selection, offset)) {
					if (allSelectionItemsInSameList()) {
						return true;
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
			if (itemPosition != null) {
				ListControl masterListControl = getMasterListControl();
				if (masterListControl != null) {
					masterListControl.setSingleSelection(toMasterListControlItemPosition(itemPosition));
					return true;
				}
			}
			dialogBuilder = new ItemUIBuilder(itemPosition);
			JDialog dialog = dialogBuilder.createDialog();
			swingRenderer.showDialog(dialog, true);
			return true;
		}

		@Override
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			BufferedItemPosition newItemPosition = getNewItemPosition();
			int index = newItemPosition.getIndex();
			final List<BufferedItemPosition> postSelection = new ArrayList<BufferedItemPosition>();
			final List<Object> postSelectionItems = new ArrayList<Object>();
			final List<List<Object>> postSelectionItemAncestors = new ArrayList<List<Object>>();
			for (Object clipboardItem : clipboard) {
				Object clipboardItemCopy = ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), clipboardItem);
				getModificationStack()
						.apply(createListModificationFactory(newItemPosition).add(index, clipboardItemCopy));
				postSelection.add(newItemPosition.getSibling(index));
				postSelectionItems.add(clipboardItemCopy);
				postSelectionItemAncestors
						.add(ReflectionUIUtils.collectItemAncestors(newItemPosition.getSibling(index)));
				index++;
			}
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return ReflectionUIUtils.actualizeItemPositions(postSelection, postSelectionItems,
							postSelectionItemAncestors);
				}
			};
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			BufferedItemPosition subItemPosition = getNewItemPosition();
			int newSubListItemIndex = subItemPosition.getContainingListSize();
			int newSubListItemInitialIndex = newSubListItemIndex;
			subItemPosition = subItemPosition.getSibling(newSubListItemIndex);
			final List<BufferedItemPosition> postSelection = new ArrayList<BufferedItemPosition>();
			final List<Object> postSelectionItems = new ArrayList<Object>();
			final List<List<Object>> postSelectionItemAncestorLists = new ArrayList<List<Object>>();
			for (Object clipboardItem : clipboard) {
				Object clipboardItemCopy = ReflectionUIUtils.copy(swingRenderer.getReflectionUI(), clipboardItem);
				getModificationStack().apply(
						createListModificationFactory(subItemPosition).add(newSubListItemIndex, clipboardItemCopy));
				postSelection.add(subItemPosition.getSibling(newSubListItemInitialIndex));
				postSelectionItems.add(clipboardItemCopy);
				postSelectionItemAncestorLists.add(
						ReflectionUIUtils.collectItemAncestors(subItemPosition.getSibling(newSubListItemInitialIndex)));
				newSubListItemIndex++;
			}
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return ReflectionUIUtils.actualizeItemPositions(postSelection, postSelectionItems,
							postSelectionItemAncestorLists);
				}
			};
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
			if (getRootListTitle().length() == 0) {
				return "Paste item(s)";
			}
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			List<BufferedItemPosition> selection = getSelection();
			List<BufferedItemPosition> postSelection = new ArrayList<BufferedItemPosition>();
			for (BufferedItemPosition itemPosition : selection) {
				itemPosition = getPositionSelectedByItemRemoval(itemPosition);
				if (itemPosition != null) {
					postSelection.add(itemPosition);
				}
			}
			for (BufferedItemPosition itemPosition : selection) {
				purgePositionsAfterItemRemoval(postSelection, itemPosition);
			}
			List<Object> postSelectionItems = new ArrayList<Object>();
			List<List<Object>> postSelectionItemAncestorLists = new ArrayList<List<Object>>();
			for (BufferedItemPosition itemPosition : postSelection) {
				postSelectionItems.add(itemPosition.getItem());
				postSelectionItemAncestorLists.add(ReflectionUIUtils.collectItemAncestors(itemPosition));
			}
			for (BufferedItemPosition itemPosition : selection) {
				shiftPositionsAfterItemRemoval(postSelection, itemPosition);
			}
			selection = new ArrayList<BufferedItemPosition>(selection);
			Collections.sort(selection);
			Collections.reverse(selection);
			for (BufferedItemPosition itemPosition : selection) {
				getModificationStack()
						.apply(createListModificationFactory(itemPosition).remove(itemPosition.getIndex()));
			}
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					return ReflectionUIUtils.actualizeItemPositions(postSelection, postSelectionItems,
							postSelectionItemAncestorLists);
				}
			};
		}

		@Override
		protected String getActionTitle() {
			return "Remove";
		}

		@Override
		protected String getCompositeModificationTitle() {
			if (getRootListTitle().length() == 0) {
				return "Remove item(s)";
			}
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
				Image iconImage = swingRenderer.getMethodIconImage(new DefaultMethodControlData(
						swingRenderer.getReflectionUI(), IDynamicListAction.NO_OWNER, dynamicAction));
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
					return new CustomContext("ListDynamicAction [name=" + dynamicAction.getName() + ", listContext="
							+ input.getContext().getIdentifier() + "]");
				}

				@Override
				public IMethodControlData getControlData() {
					return new DefaultMethodControlData(swingRenderer.getReflectionUI(), IDynamicListAction.NO_OWNER,
							dynamicAction) {
						@Override
						public Runnable getLastFormRefreshStateRestorationJob() {
							return listData.getLastFormRefreshStateRestorationJob();
						}
					};
				}
			});
			invocationData = action.prepare(ListControl.this);
			return invocationData != null;
		}

		@Override
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			final Accessor<List<BufferedItemPosition>> defaultPostSelectionGetter = postSelectionGetterHolder[0];
			action.invokeAndObtainReturnValue(invocationData, ListControl.this);
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					List<ItemPosition> result = dynamicAction.getPostSelection();
					if (result == null) {
						return defaultPostSelectionGetter.get();
					}
					return MiscUtils.<ItemPosition, BufferedItemPosition>convertCollectionUnsafely(result);
				}
			};
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
				protected boolean isEncapsulatedValueValidityDetectionEnabled() {
					return dynamicProperty.isValueValidityDetectionEnabled();
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
				protected Runnable getParentControlRefreshJob() {
					return new Runnable() {
						@Override
						public void run() {
							ListControl.this.refreshUI(false);
						}
					};
				}

				@Override
				protected String getParentModificationTitle() {
					return "Edit "
							+ ReflectionUIUtils.composeMessage(listData.getCaption(), dynamicProperty.getCaption());
				}

				@Override
				protected boolean isParentModificationVolatile() {
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
							IDynamicListProperty.NO_OWNER, dynamicProperty) {
						@Override
						public Runnable getLastFormRefreshStateRestorationJob() {
							return listData.getLastFormRefreshStateRestorationJob();
						}
					}, newObjectValue);
				}

				@Override
				protected IModification createUndoModificationsReplacement() {
					return ReflectionUIUtils.createUndoModificationsReplacement(listData);
				}

				@Override
				protected void handleRealtimeLinkCommitException(Throwable t) {
					swingRenderer.handleException(ListControl.this, t);
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
		protected void perform(Accessor<List<BufferedItemPosition>>[] postSelectionGetterHolder) {
			final Accessor<List<BufferedItemPosition>> defaultPostSelectionGetter = postSelectionGetterHolder[0];
			postSelectionGetterHolder[0] = new Accessor<List<BufferedItemPosition>>() {
				@Override
				public List<BufferedItemPosition> get() {
					List<ItemPosition> result = dynamicProperty.getPostSelection();
					if (result == null) {
						return defaultPostSelectionGetter.get();
					}
					return MiscUtils.<ItemPosition, BufferedItemPosition>convertCollectionUnsafely(result);
				}
			};
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

	public class ItemValidationError extends Exception {

		private static final long serialVersionUID = 1L;

		protected BufferedItemPosition itemPosition;

		public ItemValidationError(Entry<BufferedItemPosition, Exception> entry) {
			this(entry.getKey(), entry.getValue());
		}

		public ItemValidationError(BufferedItemPosition itemPosition, Exception cause) {
			super(cause);
			this.itemPosition = itemPosition;
		}

		@Override
		public String getMessage() {
			return "Failed to validate " + getDisplayPath(itemPosition);
		}

		@Override
		public String toString() {
			return getMessage();
		}
	}

	public class ListValidationError extends Exception {

		private static final long serialVersionUID = 1L;

		protected Map<BufferedItemPosition, Exception> validitionErrorByItemPosition;

		public ListValidationError(Map<BufferedItemPosition, Exception> validitionErrorByItemPosition) {
			super("Invalid element(s) detected");
			this.validitionErrorByItemPosition = validitionErrorByItemPosition;
		}

		public List<ItemValidationError> getEntries() {
			return validitionErrorByItemPosition.entrySet().stream()
					.map(mapEntry -> new ItemValidationError(
							((Map.Entry<BufferedItemPosition, Exception>) mapEntry).getKey(),
							ValidationErrorWrapper.unwrapValidationError(
									((Map.Entry<BufferedItemPosition, Exception>) mapEntry).getValue())))
					.collect(Collectors.toList());
		}

		@Override
		public String toString() {
			return getMessage();
		}

	}

}
