
package xy.reflect.ui.control.swing.util;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Alternate tabbed pane implementation that uses a list control to render tab
 * headers.
 * 
 * @author olitank
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListTabbedPane extends JPanel {

	public static void main(String[] args) {
		final ListTabbedPane tabbedPane = new ListTabbedPane(JTabbedPane.TOP);
		for (int i = 0; i < 20; i++) {
			tabbedPane.addTab("tab" + i, new JTextArea("tab" + i + " OK"));
			tabbedPane.setEnabledAt(i, (i % 2) == 1);
			tabbedPane.setIconAt(i, ((i % 2) == 1) ? SwingRendererUtils.UP_ICON : SwingRendererUtils.DOWN_ICON);
		}
		tabbedPane.setPreferredSize(new Dimension(800, 600));
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						tabbedPane.setTabPlacement(JTabbedPane.LEFT);
					}
				});
			}
		}.start();
		JOptionPane.showMessageDialog(null, tabbedPane);

	}

	private static final long serialVersionUID = 1L;

	protected DefaultListModel titleListModel;
	protected JList titleListControl;
	protected JScrollPane titleListControlWrapper;
	protected int lastTitleListSelectionIndex = -1;
	protected boolean titleListSelectionHandlingEnabled = true;
	protected JPanel currentComponentContainer;
	protected CardLayout cardLayout;
	protected List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	protected List<String> disabledCardNames = new ArrayList<String>();
	protected Map<Integer, Component> componentByIndex = new HashMap<Integer, Component>();
	protected Map<Integer, Icon> iconByIndex = new HashMap<Integer, Icon>();
	protected int placement;

	public ListTabbedPane(int placement) {
		this.placement = placement;

		titleListControl = createListControl();
		titleListControl.setModel(titleListModel = createListModel());

		currentComponentContainer = createCurrentComponentContainer();
		currentComponentContainer.add(createNullTabComponent(), getCardName(-1));

		layoutComponents(currentComponentContainer);

		refresh();
	}

	public int getTabPlacement() {
		return placement;
	}

	public void setTabPlacement(int placement) {
		this.placement = placement;
		remove(titleListControlWrapper);
		layoutListControl();
		titleListControl.setCellRenderer(createListCellRenderer());
		validate();
	}

	protected void layoutComponents(JPanel currentComponentContainer) {
		setLayout(new BorderLayout());
		add(currentComponentContainer, BorderLayout.CENTER);
		titleListControlWrapper = wrapListControl(titleListControl);
		layoutListControl();
	}

	protected void layoutListControl() {
		if (placement == JTabbedPane.LEFT) {
			add(titleListControlWrapper, BorderLayout.WEST);
			titleListControl.setLayoutOrientation(JList.VERTICAL);
			titleListControl.setVisibleRowCount(-1);
		} else if (placement == JTabbedPane.TOP) {
			add(titleListControlWrapper, BorderLayout.NORTH);
			titleListControl.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			titleListControl.setVisibleRowCount(1);
		} else {
			throw getInvalidPlacementError();
		}
	}

	protected IllegalArgumentException getInvalidPlacementError() {
		return new IllegalArgumentException("Invalid placement. Expected: JTabbedPane.LEFT or JTabbedPane.TOP");
	}

	protected JList createListControl() {
		JList result = new JList() {
			private static final long serialVersionUID = 1L;

			{
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}

			@Override
			public void setSelectionInterval(int anchor, int lead) {
				for (int index = anchor; index <= lead; index++) {
					if (disabledCardNames.contains(getCardName(index))) {
						return;
					}
				}
				super.setSelectionInterval(anchor, lead);
			}

			private boolean isNotOnEmptySpaceAfterItems(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				return index > -1 && getCellBounds(index, index).contains(e.getPoint());
			}

			@Override
			protected void processMouseEvent(MouseEvent e) {
				if (isNotOnEmptySpaceAfterItems(e)) {
					super.processMouseEvent(e);
				}
			}

			@Override
			protected void processMouseMotionEvent(MouseEvent e) {
				if (isNotOnEmptySpaceAfterItems(e)) {
					super.processMouseMotionEvent(e);
				}
			}

		};
		result.setCellRenderer(createListCellRenderer());
		result.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				onSelectionChange();
			}
		});
		return result;
	}

	protected ListCellRenderer createListCellRenderer() {
		return new ListCellRenderer() {

			JButton button = createNonSelectedTabHeaderCellRendererComponent();
			{
				button.setHorizontalAlignment(
						(placement == JTabbedPane.LEFT) ? SwingConstants.LEFT : SwingConstants.CENTER);
				button.setHorizontalTextPosition(SwingConstants.RIGHT);
				button.setVerticalTextPosition(SwingConstants.CENTER);
			}
			JLabel label = createSelectedTabHeaderCellRendererComponent();
			{
				label.setHorizontalAlignment(
						(placement == JTabbedPane.LEFT) ? SwingConstants.LEFT : SwingConstants.CENTER);
				label.setHorizontalTextPosition(SwingConstants.RIGHT);
				label.setVerticalTextPosition(SwingConstants.CENTER);
			}

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String text = (String) value;
				Icon icon = ListTabbedPane.this.getIconAt(index);
				if (isSelected) {
					label.setText(text);
					label.setIcon(icon);
					label.setEnabled(!disabledCardNames.contains(getCardName(index)));
					return label;
				} else {
					button.setText(text);
					button.setIcon(icon);
					button.setEnabled(!disabledCardNames.contains(getCardName(index)));
					return button;
				}

			}
		};
	}

	protected JScrollPane wrapListControl(JList listControl) {
		return new JScrollPane(listControl) {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result == null) {
					return null;
				}
				result = preventScrollBarsFromHidingContent(result);
				return result;
			}

			Dimension preventScrollBarsFromHidingContent(Dimension size) {
				Dimension result = new Dimension(size);
				JScrollBar hSBar = getHorizontalScrollBar();
				{
					if (hSBar != null) {
						if (hSBar.isVisible()) {
							result.height += hSBar.getHeight();
						}
					}
				}
				JScrollBar vSBar = getVerticalScrollBar();
				{
					if (vSBar != null) {
						if (vSBar.isVisible()) {
							result.width += vSBar.getWidth();
						}
					}
				}
				return result;
			}
		};
	}

	protected Component preventItemSelectionWhenClickingEmptySpace(Component listComponent, int placement) {
		JPanel panel = new JPanel(new GridBagLayout());
		if (placement == JTabbedPane.LEFT) {
			{
				GridBagConstraints c = new GridBagConstraints();
				c.gridy = 0;
				c.weighty = 1.0;
				c.fill = GridBagConstraints.VERTICAL;
				c.anchor = GridBagConstraints.NORTH;
				panel.add(listComponent, c);
			}
			{
				GridBagConstraints c = new GridBagConstraints();
				c.gridy = 1;
				c.weighty = 1.0;
				panel.add(new JTextArea("hello"), c);
			}
		} else if (placement == JTabbedPane.TOP) {
			{
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0;
				c.weightx = 1.0;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.anchor = GridBagConstraints.WEST;
				panel.add(listComponent, c);
			}
			{
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 1;
				c.weightx = 1.0;
				panel.add(new JTextArea("hello"), c);
			}
		} else {
			throw getInvalidPlacementError();
		}
		return panel;
	}

	protected JLabel createSelectedTabHeaderCellRendererComponent() {
		JLabel result = new JLabel() {

			private static final long serialVersionUID = 1L;

			@Override
			public Color getForeground() {
				if (!isEnabled()) {
					JButton colorSource = createNonSelectedTabHeaderCellRendererComponent();
					colorSource.setEnabled(false);
					return colorSource.getForeground();
				}
				return super.getForeground();
			}

		};
		result.setOpaque(true);
		return result;
	}

	protected JButton createNonSelectedTabHeaderCellRendererComponent() {
		return new JButton();
	}

	protected CardLayout createCardLayout() {
		return new CardLayout();
	}

	protected JPanel createCurrentComponentContainer() {
		JPanel result = new JPanel();
		result.setLayout(cardLayout = createCardLayout());
		return result;
	}

	protected DefaultListModel createListModel() {
		return new DefaultListModel();
	}

	protected Component createNullTabComponent() {
		return new JPanel();
	}

	public int getSelectedIndex() {
		return titleListControl.getSelectedIndex();
	}

	public void setSelectedIndex(int index) {
		if (index == -1) {
			titleListControl.clearSelection();
		} else {
			titleListControl.setSelectedIndex(index);
		}
	}

	protected void onSelectionChange() {
		if (!titleListSelectionHandlingEnabled) {
			return;
		}
		if (titleListControl.getSelectedIndex() == lastTitleListSelectionIndex) {
			return;
		}
		final int currentIndex = titleListControl.getSelectedIndex();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				preventMultipleVisibleCardsIssue();
				cardLayout.show(currentComponentContainer, getCardName(currentIndex));
				notifyChangeListeners();
			}

			void preventMultipleVisibleCardsIssue() {
				for (Component comp : currentComponentContainer.getComponents()) {
					if (comp.isVisible()) {
						comp.setVisible(false);
					}
				}
			}
		});
		lastTitleListSelectionIndex = currentIndex;
	}

	protected void notifyChangeListeners() {
		for (ChangeListener l : changeListeners) {
			l.stateChanged(new ChangeEvent(ListTabbedPane.this));
		}
	}

	protected String getCardName(int index) {
		return Integer.toString(index);
	}

	public void addTab(String title, Component component) {
		insertTab(title, null, component, getTabCount());
	}

	public void insertTab(String title, Icon icon, Component component, int index) {
		String cardName = getCardName(index);
		currentComponentContainer.add(component, cardName);
		titleListModel.insertElementAt(title, index);
		componentByIndex.put(index, component);
		setIconAt(index, icon);
		if (getSelectedIndex() == -1) {
			setSelectedIndex(0);
		}
	}

	public void removeTabAt(int index) {
		Component component = componentByIndex.remove(index);
		titleListModel.removeElementAt(index);
		currentComponentContainer.remove(component);
		if (getSelectedIndex() == -1) {
			setSelectedIndex(0);
		}
	}

	public void addChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	public String getTitleAt(int index) {
		return (String) titleListModel.getElementAt(index);
	}

	public Component getSelectedComponent() {
		int currentIndex = titleListControl.getSelectedIndex();
		if (currentIndex == -1) {
			return null;
		}
		return getComponentAt(currentIndex);
	}

	public int getTabCount() {
		return titleListModel.size();
	}

	public Component getComponentAt(int index) {
		return componentByIndex.get(index);
	}

	public void setEnabledAt(int tabIndex, boolean b) {
		if (b) {
			disabledCardNames.remove(getCardName(tabIndex));
		} else {
			disabledCardNames.add(getCardName(tabIndex));
		}
		titleListControl.repaint();
	}

	public boolean isEnabledAt(int tabIndex) {
		return !disabledCardNames.contains(getCardName(tabIndex));
	}

	public void refresh() {
		titleListSelectionHandlingEnabled = false;
		try {
			int initialSelectedIndex = getSelectedIndex();
			titleListControl.setModel(titleListModel);
			initialSelectedIndex = Math.min(initialSelectedIndex, titleListControl.getModel().getSize() - 1);
			setSelectedIndex(initialSelectedIndex);
		} finally {
			titleListSelectionHandlingEnabled = true;
		}
	}

	public Icon getIconAt(int index) {
		return iconByIndex.get(index);
	}

	public void setIconAt(int tabIndex, Icon icon) {
		iconByIndex.put(tabIndex, icon);
	}

}
