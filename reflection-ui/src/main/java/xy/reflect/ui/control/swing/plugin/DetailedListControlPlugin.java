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
package xy.reflect.ui.control.swing.plugin;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemDetailsAreaPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.ListStructuralInfoProxy;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control that displays a list of values in a grid. Each cell contains a
 * sub-control allowing to edit each item.
 * 
 * @author olitank
 *
 */
public class DetailedListControlPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Detailed List Control";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		throw new ReflectionUIError();
	}

	@Override
	public boolean handles(IFieldControlInput input) {
		if (!(input.getControlData().getType() instanceof IListTypeInfo)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new DetailedListConfiguration();
	}

	@Override
	public DetailedListControl createControl(Object renderer, IFieldControlInput input) {
		return new DetailedListControl((SwingRenderer) renderer, input);
	}

	public static class DetailedListConfiguration extends AbstractConfiguration {

		private static final long serialVersionUID = 1L;

		public DetailedListLayout layout = DetailedListLayout.VERTICAL_FLOW;
		public int gridDimension = 0;
		public boolean stretchCellsVertically = true;
		public boolean stretchCellsHorizontally = true;

		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			stretchCellsVertically = true;
			stretchCellsHorizontally = true;
			in.defaultReadObject();
		}

	}

	public enum DetailedListLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

	public class DetailedListControl extends ListControl {
		private static final long serialVersionUID = 1L;

		protected JPanel detailedCellsContainer;
		protected List<DetailedCellControl> detailedCellControlList;

		public DetailedListControl(SwingRenderer swingRenderer, final IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		protected void initializeTreeTableModelAndControl() {
			itemPositionFactory = createItemPositionfactory();
			detailedCellsContainer = new ControlPanel();
			{
				detailedCellsContainer.setLayout(new GridBagLayout());
			}
			treeTableComponentScrollPane = createTreeTableScrollPane(detailedCellsContainer);
			clearSelectionWhenContainerClicked();
			setupContexteMenu(detailedCellsContainer);
		}

		protected void clearSelectionWhenContainerClicked() {
			detailedCellsContainer.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getComponent() != detailedCellsContainer) {
						return;
					}
					setSelection(Collections.emptyList());
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.getComponent() != detailedCellsContainer) {
						return;
					}
					setSelection(Collections.emptyList());
				}

			});
		}

		@Override
		protected void layoutControls() {
			setLayout(new BorderLayout());
			add(treeTableComponentScrollPane, BorderLayout.CENTER);
			add(toolbar, BorderLayout.EAST);
		}

		@Override
		public void refreshTreeTableModelAndControl(boolean refreshStructure) {
			int itemCount = itemPositionFactory.getRootItemPosition(-1).getContainingListSize();
			if (detailedCellControlList == null) {
				detailedCellControlList = new ArrayList<DetailedCellControl>();
			}
			if (itemCount == detailedCellControlList.size()) {
				for (int i = 0; i < itemCount; i++) {
					DetailedCellControl detailedCellControl = detailedCellControlList.get(i);
					detailedCellControl.refreshUI(refreshStructure);
					if (refreshStructure) {
						detailedCellsContainer.remove(detailedCellControl);
						detailedCellsContainer.add(detailedCellControl, getDetailedCellLayoutConstraints(i));
					}
				}
			} else {
				for (DetailedCellControl c : detailedCellControlList) {
					detailedCellsContainer.remove(c);
				}
				detailedCellControlList.clear();
				for (int i = 0; i < itemCount; i++) {
					BufferedItemPosition itemPosition = itemPositionFactory.getRootItemPosition(i);
					DetailedCellControl detailedCellControl = new DetailedCellControl(itemPosition);
					{
						detailedCellControlList.add(detailedCellControl);
						detailedCellsContainer.add(detailedCellControl, getDetailedCellLayoutConstraints(i));
					}
				}
			}
			if (refreshStructure) {
				SwingRendererUtils.handleComponentSizeChange(detailedCellsContainer);
			}
		}

		protected Object getDetailedCellLayoutConstraints(int i) {
			DetailedListConfiguration controlCustomization = (DetailedListConfiguration) loadControlCustomization(
					input);
			GridBagConstraints result = new GridBagConstraints();
			if (controlCustomization.layout == DetailedListLayout.VERTICAL_FLOW) {
				result.gridx = 0;
				result.gridy = i;
				if (controlCustomization.gridDimension > 0) {
					result.gridx = result.gridy / controlCustomization.gridDimension;
					result.gridy = result.gridy % controlCustomization.gridDimension;
				}
			} else if (controlCustomization.layout == DetailedListLayout.HORIZONTAL_FLOW) {
				result.gridy = 0;
				result.gridx = i;
				if (controlCustomization.gridDimension > 0) {
					result.gridy = result.gridx / controlCustomization.gridDimension;
					result.gridx = result.gridx % controlCustomization.gridDimension;
				}
			} else {
				throw new ReflectionUIError();
			}
			if (controlCustomization.stretchCellsHorizontally && controlCustomization.stretchCellsVertically) {
				result.fill = GridBagConstraints.BOTH;
			} else if (controlCustomization.stretchCellsHorizontally) {
				result.fill = GridBagConstraints.HORIZONTAL;
			} else if (controlCustomization.stretchCellsVertically) {
				result.fill = GridBagConstraints.VERTICAL;
			} else {
				result.fill = GridBagConstraints.NONE;
			}
			result.weightx = 1.0;
			result.weighty = 1.0;
			return result;
		}

		@Override
		public List<BufferedItemPosition> getSelection() {
			List<BufferedItemPosition> result = new ArrayList<BufferedItemPosition>();
			if (detailedCellsContainer != null) {
				for (Component c : detailedCellsContainer.getComponents()) {
					DetailedCellControl detailedCellControl = (DetailedCellControl) c;
					if (detailedCellControl.isSelected()) {
						result.add(detailedCellControl.getItemPosition());
					}
				}
			}
			return result;
		}

		@Override
		public void setSelection(List<BufferedItemPosition> toSelect) {
			if (getSelection().equals(toSelect)) {
				return;
			}
			if (detailedCellsContainer != null) {
				for (Component c : detailedCellsContainer.getComponents()) {
					DetailedCellControl detailedCellControl = (DetailedCellControl) c;
					if (toSelect.contains(detailedCellControl.getItemPosition())) {
						detailedCellControl.setSelected(true);
					} else {
						detailedCellControl.setSelected(false);
					}
				}
				fireSelectionEvent();
			}
		}

		@Override
		public void expandItemPosition(BufferedItemPosition itemPosition) {
		}

		@Override
		public void collapseItemPosition(BufferedItemPosition itemPosition) {
		}

		@Override
		public void collapseAllItemPositions() {
		}

		@Override
		public List<BufferedItemPosition> getExpandedItemPositions(BufferedItemPosition parentItemPosition) {
			return Collections.emptyList();
		}

		@Override
		protected void setupContexteMenu() {
		}

		@Override
		protected void initializeSelectionListening() {
		}

		@Override
		protected void updateDetailsAreaOnSelection() {
		}

		@Override
		protected void openDetailsDialogOnItemDoubleClick() {
		}

		@Override
		protected void refreshTreeTableComponentBackground() {
		}

		@Override
		protected void refreshRendrers() {
		}

		@Override
		protected void refreshTreeTableComponentHeader() {
		}

		@Override
		public boolean requestCustomFocus() {
			if (getRootListSize() > 0) {
				setSingleSelection(getRootListItemPosition(0));
			}
			if (SwingRendererUtils.requestAnyComponentFocus(detailedCellsContainer, swingRenderer)) {
				return true;
			}
			return false;
		}

		@Override
		protected void restoringExpandedPathsAsMuchAsPossible(Runnable runnable) {
			runnable.run();
		}

		@Override
		protected void restoringColumnWidthsAsMuchAsPossible(Runnable runnable) {
			throw new ReflectionUIError();
		}

		@Override
		protected void updateDetailsArea(boolean refreshStructure) {
			throw new ReflectionUIError();
		}

		@Override
		public IListItemDetailsAccessMode getDetailsAccessMode() {
			return new IListItemDetailsAccessMode() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean hasDetachedDetailsDisplayOption() {
					return false;
				}

				@Override
				public boolean hasEmbeddedDetailsDisplayArea() {
					return false;
				}

				@Override
				public ItemDetailsAreaPosition getEmbeddedDetailsAreaPosition() {
					throw new ReflectionUIError();
				}

				@Override
				public double getDefaultEmbeddedDetailsAreaOccupationRatio() {
					throw new ReflectionUIError();
				}
			};
		}

		@Override
		public void scrollTo(BufferedItemPosition itemPosition) {
			DetailedCellControl targetDetailedCellControl = null;
			for (Component c : detailedCellsContainer.getComponents()) {
				DetailedCellControl detailedCellControl = (DetailedCellControl) c;
				if (itemPosition.equals(detailedCellControl.getItemPosition())) {
					targetDetailedCellControl = detailedCellControl;
					break;
				}
			}
			if (targetDetailedCellControl == null) {
				return;
			}
			detailedCellsContainer.scrollRectToVisible(targetDetailedCellControl.getBounds());
		}

		@Override
		public IListStructuralInfo getStructuralInfo(BufferedItemPosition itemPosition) {
			return new ListStructuralInfoProxy(super.getStructuralInfo(itemPosition)) {

				@Override
				public List<IColumnInfo> getColumns() {
					return Collections.emptyList();
				}

				@Override
				public IFieldInfo getItemSubListField(ItemPosition itemPosition) {
					return null;
				}

			};
		}

		protected void setupContexteMenu(JPanel panel) {
			panel.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.getButton() != MouseEvent.BUTTON3) {
						return;
					}
					if (e.getComponent() == panel) {
						JPopupMenu popup = createPopupMenu();
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}

			});
		}

		@Override
		public String toString() {
			return "DetailedListControl [data=" + listData + "]";
		}

		public class DetailedCellControl extends ControlPanel {

			private static final long serialVersionUID = 1L;

			protected BufferedItemPosition itemPosition;
			protected boolean selected = false;

			private Form form;

			private ItemUIBuilder formBuilder;

			public DetailedCellControl(BufferedItemPosition itemPosition) {
				this.itemPosition = itemPosition;
				setLayout(new GridBagLayout());
				formBuilder = new ItemUIBuilder(itemPosition);
				form = formBuilder.createEditorForm(true, false);
				{
					GridBagConstraints formConstraints = new GridBagConstraints();
					formConstraints.fill = GridBagConstraints.BOTH;
					formConstraints.weightx = 1.0;
					formConstraints.weighty = 1.0;
					int insetsThickness = SwingRendererUtils.getStandardCharacterWidth(this) * 2;
					formConstraints.insets = new Insets(insetsThickness, insetsThickness, insetsThickness,
							insetsThickness);
					add(form, formConstraints);
				}
				enableSelection();
				setupContexteMenu(this);
			}

			protected void enableSelection() {
				addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						setSingleSelection(DetailedCellControl.this.itemPosition);
					}

					@Override
					public void mousePressed(MouseEvent e) {
						setSingleSelection(DetailedCellControl.this.itemPosition);
					}
				});
				final AWTEventListener selectionListener = new AWTEventListener() {
					@Override
					public void eventDispatched(AWTEvent event) {
						if (!(event instanceof MouseEvent)) {
							return;
						}
						MouseEvent moueseEvent = (MouseEvent) event;
						if ((moueseEvent.getID() != MouseEvent.MOUSE_PRESSED)
								&& (moueseEvent.getID() != MouseEvent.MOUSE_CLICKED)) {
							return;
						}
						Component c = moueseEvent.getComponent();
						if (c == null) {
							return;
						}
						if (SwingUtilities.getAncestorOfClass(DetailedCellControl.class,
								c) != DetailedCellControl.this) {
							return;
						}
						setSingleSelection(DetailedCellControl.this.itemPosition);
					}
				};
				addAncestorListener(new AncestorListener() {

					@Override
					public void ancestorAdded(AncestorEvent event) {
						Toolkit.getDefaultToolkit().addAWTEventListener(selectionListener, AWTEvent.MOUSE_EVENT_MASK);
					}

					@Override
					public void ancestorRemoved(AncestorEvent event) {
						Toolkit.getDefaultToolkit().removeAWTEventListener(selectionListener);
					}

					@Override
					public void ancestorMoved(AncestorEvent event) {
					}

				});
				updateSelectionState();
			}

			protected void updateSelectionState() {
				int borderThickness = 5;
				if (selected) {
					setBorder(BorderFactory.createLineBorder(SwingRendererUtils.getListSelectionBackgroundColor(),
							borderThickness));
				} else {
					setBorder(BorderFactory.createEmptyBorder(borderThickness, borderThickness, borderThickness,
							borderThickness));
				}
			}

			public BufferedItemPosition getItemPosition() {
				return itemPosition;
			}

			public boolean isSelected() {
				return selected;
			}

			public void setSelected(boolean b) {
				selected = b;
				updateSelectionState();
			}

			public void refreshUI(boolean refreshStructure) {
				formBuilder.refreshEditorForm(form, refreshStructure);
			}

			@Override
			public String toString() {
				return "DetailedCellControl [itemPosition=" + itemPosition + "]";
			}

		}

	}

}
