package xy.reflect.ui.util.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ScrollPaneOptions extends JPanel implements Scrollable {

	protected static final long serialVersionUID = 1L;
	protected boolean limitWidthToViewport;
	protected boolean limitHeightToViewport;
	
	public ScrollPaneOptions(Component content, boolean limitWidthToViewport,
			boolean limitHeightToViewport) {
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		this.limitWidthToViewport = limitWidthToViewport;
		this.limitHeightToViewport = limitHeightToViewport;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 10;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height
				: visibleRect.width) - 10;
	}

	public boolean getScrollableTracksViewportWidth() {
		return limitWidthToViewport;
	}

	public boolean getScrollableTracksViewportHeight() {
		return limitHeightToViewport;
	}
}