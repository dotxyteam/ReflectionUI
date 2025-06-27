
package xy.reflect.ui.control.swing.util;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

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
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		result = preventScrollBarsFromHidingContent(result);
		return result;
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		if (result == null) {
			return null;
		}
		result = preventScrollBarsFromHidingContent(result);
		return result;
	}

	@Override
	public Dimension getMaximumSize() {
		Dimension result = super.getMaximumSize();
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
			if ((hSBar != null) && hSBar.isVisible()) {
				result.height += (hSBar.getHeight() > 0) ? hSBar.getHeight() : getDefaultScrollBarThickness();
			}
		}
		JScrollBar vSBar = getVerticalScrollBar();
		{
			if ((vSBar != null) && vSBar.isVisible()) {
				result.width += (vSBar.getWidth() > 0) ? vSBar.getWidth() : getDefaultScrollBarThickness();
			}
		}
		return result;
	}

	protected int getDefaultScrollBarThickness() {
		Integer result = ((Integer) UIManager.get("ScrollBar.width"));
		if (result == null) {
			result = 10;
		}
		return result;
	}
}
