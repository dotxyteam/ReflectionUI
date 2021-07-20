


package xy.reflect.ui.control.swing.util;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;

/**
 * Base class of most scroll panes used by the {@link SwingRenderer}.
 * 
 * @author olitank
 *
 */
public class ControlScrollPane extends JScrollPane {

	private static final long serialVersionUID = 1L;

	public ControlScrollPane() {
		setOpaque(false);
		getViewport().setOpaque(false);
	}

	public ControlScrollPane(Component view) {
		super(view);
		setOpaque(false);
		getViewport().setOpaque(false);
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		result = preventScrollBarsFromHidingContent(result);
		return result;
	}

	protected Dimension preventScrollBarsFromHidingContent(Dimension baseSize) {
		Dimension result = new Dimension(baseSize);
		JScrollBar hSBar = getHorizontalScrollBar();
		{
			if (hSBar != null) {
				result.height += hSBar.getHeight();
			}
		}
		JScrollBar vSBar = getVerticalScrollBar();
		{
			if (vSBar != null) {
				result.width += vSBar.getWidth();
			}
		}
		return result;
	}
}
