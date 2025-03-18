
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
 * Above the view's preferred dimension, the scroll pane normally stretches it
 * until it fills the available space. Using {@link ScrollPaneOptions} with the
 * false option allows the view to maintain its preferred dimension on the axis.
 * The true option disables the axis scroll bar and stretches the view until it
 * fills the available space.
 * 
 * Below the view's preferred dimension, the scroll pane displays the axis
 * scroll bar. Using {@link ScrollPaneOptions} with the false option does not
 * change this behavior. The true option disables the axis scroll bar and
 * compresses the view to fit the available space.
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
		return disableHorizontalScrolling;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return disableVerticalScrolling;
	}

}
