package xy.reflect.ui.util.component;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
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
		for (int i = 0; i < 10; i++) {
			tabbedPane.addTab("tab" + i, new JTextArea("tab" + i + " OK"));
		}
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
		listControl.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				onSelectionChange();
			}
		});

		currentComponentContainer = createCurrentComponentContainer();
		currentComponentContainer.add(createNullTabComponent(), getCardName(null));

		layoutComponents(listControl, currentComponentContainer, placement);

		listModel = new DefaultListModel();
		refresh();
	}

	protected void layoutComponents(JList listControl, JPanel currentComponentContainer, int placement) {
		setLayout(new BorderLayout());

		add(currentComponentContainer, BorderLayout.CENTER);

		Component listFinalComponent = wrapListControl(listControl);
		if (placement == JTabbedPane.LEFT) {
			add(listFinalComponent, BorderLayout.WEST);
			listControl.setLayoutOrientation(JList.VERTICAL);
			listControl.setVisibleRowCount(-1);
		} else if (placement == JTabbedPane.TOP) {
			add(listFinalComponent, BorderLayout.NORTH);
			listControl.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			listControl.setVisibleRowCount(1);
		} else {
			throw new IllegalArgumentException("Invalid placement. Expected: SwingConstants.LEFT, SwingConstants.TOP");
		}
	}

	protected Component wrapListControl(JList listControl) {
		JScrollPane listControlScrolledContainer = new JScrollPane(listControl);
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listPanel.add(listControlScrolledContainer, BorderLayout.CENTER);
		return listPanel;
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

	protected JList createListControl() {
		JList result = new JList();
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
		return result;
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