package xy.reflect.ui.control.swing.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Tabbed pane that has a classic customizable look. The background painting can
 * be skipped and the border colors can be changed.
 * 
 * @author olitank
 *
 */
public class ClassicTabbedPane extends ControlTabbedPane {

	private static final long serialVersionUID = 1L;

	protected boolean tabBackgroundPainted = true;

	@Override
	public void setUI(TabbedPaneUI newUI) {
		if (newUI instanceof BasicTabbedPaneUI) {
			newUI = new BasicTabbedPaneUI() {

				@Override
				protected boolean shouldRotateTabRuns(int tabPlacement) {
					return false;
				}

				@Override
				protected LayoutManager createLayoutManager() {
					if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
						return super.createLayoutManager();
					} else { /* WRAP_TAB_LAYOUT */
						return new TabbedPaneLayout() {

							@Override
							protected void padSelectedTab(int tabPlacement, int selectedIndex) {
							}

						};
					}
				}

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
					if (tabBackgroundPainted) {
						super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
					}
				}

				@Override
				protected void installDefaults() {
					Color selectedColorToRestore = UIManager.getColor("TabbedPane.selected");
					if (getBackground() != null) {
						UIManager.put("TabbedPane.selected",
								SwingRendererUtils.addColorActivationEffect(getBackground()));
					}
					try {
						super.installDefaults();
					} finally {
						UIManager.put("TabbedPane.selected", selectedColorToRestore);
					}
					lightHighlight = getLightHighlightColor();
					shadow = getShadowColor();
					darkShadow = getDarkShadowColor();
				}

				protected Color getDarkShadowColor() {
					if (getTabBorderColor() != null) {
						return getTabBorderColor();
					}
					return UIManager.getColor("TabbedPane.darkShadow");
				}

				protected Color getShadowColor() {
					if (getTabBorderColor() != null) {
						return getTabBorderColor();
					}
					return UIManager.getColor("TabbedPane.shadow");
				}

				protected Color getLightHighlightColor() {
					if (getTabBorderColor() != null) {
						return getTabBorderColor();
					}
					return UIManager.getColor("TabbedPane.highlight");
				}

			};
		}
		super.setUI(newUI);
	}

	protected Color getTabBorderColor() {
		return null;
	}

	public boolean isTabBackgroundPainted() {
		return tabBackgroundPainted;
	}

	public void setTabBackgroundPainted(boolean tabBackgroundPainted) {
		this.tabBackgroundPainted = tabBackgroundPainted;
	}

}
