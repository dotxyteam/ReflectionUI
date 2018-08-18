package xy.reflect.ui.util.component;

import java.awt.Graphics;

import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;

public class ControlTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = 1L;

	public ControlTabbedPane() {
		setOpaque(false);
	}

	@Override
	public void setUI(TabbedPaneUI newUI) {
		if (newUI instanceof javax.swing.plaf.basic.BasicTabbedPaneUI) {
			newUI = new javax.swing.plaf.basic.BasicTabbedPaneUI() {
				protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
					if (!isOpaque()) {
						return;
					}
					super.paintContentBorder(g, tabPlacement, selectedIndex);
				}
			};
		}
		super.setUI(newUI);
	}

}
