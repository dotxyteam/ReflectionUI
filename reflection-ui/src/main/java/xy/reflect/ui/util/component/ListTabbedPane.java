package xy.reflect.ui.util.component;

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

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
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
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListTabbedPane extends JPanel {

	public static void main(String[] args) {
		ListTabbedPane tabbedPane = new ListTabbedPane(JTabbedPane.TOP);
		for (int i = 0; i < 20; i++) {
			tabbedPane.addTab("tab" + i, new JTextArea("tab" + i + " OK"));
		}
		tabbedPane.setPreferredSize(new Dimension(800, 600));
		JOptionPane.showMessageDialog(null, tabbedPane);

	}

	private static final long serialVersionUID = 1L;

	private static final String NULL_CARD_NAME = ListTabbedPane.class.getName() + ".nullCard";

	private JList listControl;
	private int lastListSelection = -1;
	private boolean listSelectionHandlingEnabled = true;
	private JPanel currentComponentContainer;
	private CardLayout cardLayout;
	private DefaultListModel listModel;
	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	private List<Object> disabledListElements = new ArrayList<Object>();
	private Map<String, Component> componentByCardName = new HashMap<String, Component>();

	public ListTabbedPane(int placement) {
		listControl = createListControl();

		currentComponentContainer = createCurrentComponentContainer();
		currentComponentContainer.add(createNullTabComponent(), getCardName(null));

		layoutComponents(listControl, currentComponentContainer, placement);

		listModel = new DefaultListModel();
		refresh();
	}

	protected void layoutComponents(JList listControl, JPanel currentComponentContainer, int placement) {
		setLayout(new BorderLayout());

		add(currentComponentContainer, BorderLayout.CENTER);

		Component listFinalComponent = wrapListControl(listControl, placement);
		if (placement == JTabbedPane.LEFT) {
			add(listFinalComponent, BorderLayout.WEST);
			listControl.setLayoutOrientation(JList.VERTICAL_WRAP);
			listControl.setVisibleRowCount(-1);
		} else if (placement == JTabbedPane.TOP) {
			add(listFinalComponent, BorderLayout.NORTH);
			listControl.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			listControl.setVisibleRowCount(1);
		} else {
			throw getInvalidpalcementError();
		}
	}

	protected IllegalArgumentException getInvalidpalcementError() {
		return new IllegalArgumentException("Invalid placement. Expected: JTabbedPane.LEFT or JTabbedPane.TOP");
	}

	protected JList createListControl() {
		JList result = new JList() {
			private static final long serialVersionUID = 1L;

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
		result.setBackground(new Color(UIManager.getColor("control").getRGB()));
		result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		result.setCellRenderer(new ListCellRenderer() {

			JButton button = createNonSelectedTabHeaderCellRendererComponent();
			JLabel label = createSelectedTabHeaderCellRendererComponent();

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String text = ListTabbedPane.this.getCardName(value);
				Icon icon = ListTabbedPane.this.getIcon(value);
				if (isSelected) {
					label.setText(text);
					label.setIcon(icon);
					return label;
				} else {
					button.setText(text);
					button.setIcon(icon);
					return button;
				}

			}
		});
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

	protected Component wrapListControl(JList listControl, int placement) {
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
			throw getInvalidpalcementError();
		}
		return panel;
	}

	protected JLabel createSelectedTabHeaderCellRendererComponent() {
		JLabel result = new JLabel();
		result.setHorizontalAlignment(SwingConstants.CENTER);
		result.setOpaque(true);
		result.setBorder(BorderFactory.createTitledBorder(""));
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

	protected AbstractListModel createFilteredListModel() {
		return new AbstractListModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getSize() {
				return getFilteredElementList().size();
			}

			@Override
			public Object getElementAt(int index) {
				return getFilteredElementList().get(index);
			}

			private List<Object> getFilteredElementList() {
				List<Object> result = new ArrayList<Object>();
				for (int i = 0; i < listModel.size(); i++) {
					Object element = listModel.getElementAt(i);
					if (disabledListElements.contains(element)) {
						continue;
					}
					result.add(element);
				}
				return result;
			}

		};
	}

	protected Component createNullTabComponent() {
		return new JPanel();
	}

	public int getSelectedIndex() {
		return listControl.getSelectedIndex();
	}

	public void setSelectedIndex(int index) {
		if (index == -1) {
			listControl.clearSelection();
		} else {
			listControl.setSelectedIndex(index);
		}
	}

	protected void onSelectionChange() {
		if (!listSelectionHandlingEnabled) {
			return;
		}
		if (listControl.getSelectedIndex() == lastListSelection) {
			return;
		}
		final int currentIndex = listControl.getSelectedIndex();
		lastListSelection = currentIndex;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (currentIndex == -1) {
							cardLayout.show(currentComponentContainer, getCardName(null));
						} else {
							cardLayout.show(currentComponentContainer,
									getCardName(listControl.getModel().getElementAt(currentIndex)));
						}
						notifyChangeListeners();
					}
				});
			}
		});
	}

	protected void notifyChangeListeners() {
		for (final ChangeListener l : changeListeners) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					l.stateChanged(new ChangeEvent(ListTabbedPane.this));
				}
			});
		}
	}

	protected String getCardName(Object listElement) {
		if (listElement == null) {
			return NULL_CARD_NAME;
		} else {
			return listElement.toString();
		}
	}

	protected Icon getIcon(Object value) {
		return null;
	}

	public void addTab(Object element, Component component) {
		String cardName = getCardName(element);
		currentComponentContainer.add(component, cardName);
		listModel.addElement(element);
		componentByCardName.put(cardName, component);
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
		Object currentElement = listModel.getElementAt(index);
		return getCardName(currentElement);
	}

	public Component getSelectedComponent() {
		int currentIndex = listControl.getSelectedIndex();
		if (currentIndex == -1) {
			return null;
		}
		return getComponentAt(currentIndex);
	}

	public int getTabCount() {
		return listModel.size();
	}

	public Component getComponentAt(int index) {
		Object currentElement = listModel.getElementAt(index);
		String currentCardName = getCardName(currentElement);
		return componentByCardName.get(currentCardName);
	}

	public void setEnabledAt(int tabIndex, boolean b) {
		if (b) {
			disabledListElements.remove(listModel.getElementAt(tabIndex));
		} else {
			disabledListElements.add(listModel.getElementAt(tabIndex));
		}
		refresh();
	}

	public void refresh() {
		listSelectionHandlingEnabled = false;
		try {
			int initialSelectedIndex = getSelectedIndex();
			listControl.setModel(createFilteredListModel());
			initialSelectedIndex = Math.min(initialSelectedIndex, listControl.getModel().getSize() - 1);
			setSelectedIndex(initialSelectedIndex);
		} finally {
			listSelectionHandlingEnabled = true;
		}
	}

	public boolean isEnabledAt(int tabIndex) {
		return !disabledListElements.contains(listModel.getElementAt(tabIndex));
	}

}