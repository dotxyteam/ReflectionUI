package xy.reflect.ui.util.component;

import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;
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
						int width = tabPane.getWidth();
						int height = tabPane.getHeight();
						Insets insets = tabPane.getInsets();
						Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

						int x = insets.left;
						int y = insets.top;
						int w = width - insets.right - insets.left;
						int h = height - insets.top - insets.bottom;

						boolean tabsOverlapBorder = UIManager.getBoolean("TabbedPane.tabsOverlapBorder");
						switch (tabPlacement) {
						case LEFT:
							x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
							if (tabsOverlapBorder) {
								x -= tabAreaInsets.right;
							}
							w -= (x - insets.left);
							break;
						case RIGHT:
							w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
							if (tabsOverlapBorder) {
								w += tabAreaInsets.left;
							}
							break;
						case BOTTOM:
							h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
							if (tabsOverlapBorder) {
								h += tabAreaInsets.top;
							}
							break;
						case TOP:
						default:
							y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
							if (tabsOverlapBorder) {
								y -= tabAreaInsets.bottom;
							}
							h -= (y - insets.top);
						}

						paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
						paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
						paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
						paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);

					} else {
						super.paintContentBorder(g, tabPlacement, selectedIndex);
					}
				}

				@Override
				protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w,
						int h, boolean isSelected) {
					if (isOpaque()) {
						super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
					}						
				}
				
			};
		}
		super.setUI(newUI);
	}

}
