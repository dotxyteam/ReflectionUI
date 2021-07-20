


package xy.reflect.ui.control.swing.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * Panel allowing to limit its containing scroll pane view extent.
 * 
 * @author olitank
 *
 */
public class ScrollPaneOptions extends ControlPanel implements Scrollable {

	protected static final long serialVersionUID = 1L;
	protected boolean limitWidthToViewport;
	protected boolean limitHeightToViewport;

	public ScrollPaneOptions(Component content, boolean limitWidthToViewport, boolean limitHeightToViewport) {
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		this.limitWidthToViewport = limitWidthToViewport;
		this.limitHeightToViewport = limitHeightToViewport;
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
		return limitWidthToViewport;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return limitHeightToViewport;
	}

}
