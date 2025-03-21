
package xy.reflect.ui.control.swing.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * Panel allowing to modify the behavior of its parent scroll pane with options
 * to disable scrolling on each axis.
 * 
 * When the scrolling is disabled on an axis, the view is compressed to fit the
 * available space.
 * 
 * @author olitank
 *
 */
public class ScrollPaneOptions extends ControlPanel implements Scrollable {

	protected static final long serialVersionUID = 1L;
	protected boolean disableHorizontalScrolling;
	protected boolean disableVerticalScrolling;

	public ScrollPaneOptions(Component content, boolean disableHorizontalScrolling, boolean disableVerticalScrolling) {
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		this.disableHorizontalScrolling = disableHorizontalScrolling;
		this.disableVerticalScrolling = disableVerticalScrolling;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 10;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		if (!disableHorizontalScrolling) {
			if (getParent().getWidth() > getPreferredSize().width) {
				return true;
			}
		}
		return disableHorizontalScrolling;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		if (!disableVerticalScrolling) {
			if (getParent().getHeight() > getPreferredSize().height) {
				return true;
			}
		}
		return disableVerticalScrolling;
	}

}
