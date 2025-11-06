
// Source - https://stackoverflow.com/q
// Posted by Ansharja, modified by community. See post 'Timeline' for change history
// Retrieved 2025-11-06, License - CC BY-SA 3.0

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public class TestGridBagFilling {
	public static void main(String[] a) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("Table Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new MainPanel());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

class MainPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTable table;

	protected MainPanel() {
		super(new GridBagLayout());
		Object[][] data = new Object[100][1];
		for (int i = 0; i < 10; i++)
			data[i] = new String[] { "Some data ..." };
		table = new JTable(data, new String[] { "fvfvfvd" });
		table.setMinimumSize(new Dimension(200, 200));
		table.setShowGrid(false);
		table.setTableHeader(null);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setMinimumSize(new Dimension(200, 200));
		scrollPane.setBackground(table.getBackground());
		scrollPane.getViewport().setBackground(table.getBackground());
		// --- Adding components ---
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		add(scrollPane, c);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 400);
	}

}
