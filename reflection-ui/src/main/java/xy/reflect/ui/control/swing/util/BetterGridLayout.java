package xy.reflect.ui.control.swing.util;

import java.awt.*;

/**
 * GridLayout that does not take into account invisible components.
 *
 */
public class BetterGridLayout extends GridLayout {

	private static final long serialVersionUID = 1L;

	public BetterGridLayout(int rows, int cols) {
		super(rows, cols);
	}

	public BetterGridLayout(int rows, int cols, int hgap, int vgap) {
		super(rows, cols, hgap, vgap);
	}

	@Override
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			// count only visible components
			Component[] components = parent.getComponents();
			int visibleCount = 0;
			for (Component c : components) {
				if (c.isVisible())
					visibleCount++;
			}
			if (visibleCount == 0) {
				return;
			}
			int rows = getRows();
			int cols = getColumns();

			if (rows > 0) {
			    cols = (visibleCount + rows - 1) / rows;
			} else if (cols > 0) {
			    rows = (visibleCount + cols - 1) / cols;
			} else {
			    throw new IllegalArgumentException("Both rows and columns are zero in layout");
			}
			int totalGapsWidth = (cols - 1) * getHgap();
			int totalGapsHeight = (rows - 1) * getVgap();
			int widthWOInsets = parent.getWidth() - (insets.left + insets.right);
			int heightWOInsets = parent.getHeight() - (insets.top + insets.bottom);
			int cellWidth = (widthWOInsets - totalGapsWidth) / cols;
			int cellHeight = (heightWOInsets - totalGapsHeight) / rows;
			int visibleIndex = 0;
			for (Component comp : components) {
				if (!comp.isVisible())
					continue;
				int row = visibleIndex / cols;
				int col = visibleIndex % cols;
				int x = insets.left + col * (cellWidth + getHgap());
				int y = insets.top + row * (cellHeight + getVgap());
				comp.setBounds(x, y, cellWidth, cellHeight);
				visibleIndex++;
			}
		}
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return computeLayoutSize(parent, Component::getPreferredSize);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return computeLayoutSize(parent, Component::getMinimumSize);
	}

	private Dimension computeLayoutSize(Container parent, java.util.function.Function<Component, Dimension> sizeFn) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int nComponents = 0;
			Dimension maxSize = new Dimension(0, 0);
			for (Component c : parent.getComponents()) {
				if (c.isVisible()) {
					Dimension d = sizeFn.apply(c);
					maxSize.width = Math.max(maxSize.width, d.width);
					maxSize.height = Math.max(maxSize.height, d.height);
					nComponents++;
				}
			}
			int rows = getRows();
			int cols = getColumns();
			if (rows > 0) {
				cols = (nComponents + rows - 1) / rows;
			} else {
				rows = (nComponents + cols - 1) / cols;
			}
			int width = cols * maxSize.width + (cols - 1) * getHgap();
			int height = rows * maxSize.height + (rows - 1) * getVgap();
			width += insets.left + insets.right;
			height += insets.top + insets.bottom;
			return new Dimension(width, height);
		}
	}
}
