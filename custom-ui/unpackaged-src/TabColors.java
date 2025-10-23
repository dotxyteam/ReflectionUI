import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/** @see http://stackoverflow.com/questions/8752037 */
public class TabColors extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int MAX = 5;
	private final JTabbedPane pane = new JTabbedPane();

	public TabColors() {
		pane.setOpaque(false);
		pane.setBackground(new Color(0, 0, 0, 0));  
		for (int i = 0; i < MAX; i++) {
			Color color = Color.getHSBColor((float) i / MAX, 1, 1);
			pane.add("Tab " + String.valueOf(i), new TabContent(i, color));
			pane.setBackgroundAt(i, color);
		}
		this.add(pane);
	}

	private static class TabContent extends JPanel {

		private static final long serialVersionUID = 1L;

		private TabContent(int i, Color color) {
			add(new JLabel("Tab content " + String.valueOf(i) + " with " + color));
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(320, 240);
		}
	}

	private void display() {
		JFrame f = new JFrame("TabColors");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(this);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public static void main(String[] args) {
		UIManager.put("TabbedPane.selected", new JTabbedPane().getBackground());
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new TabColors().display();
			}
		});
	}
}