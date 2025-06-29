
package xy.reflect.ui.control.swing.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
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

	protected int additionalWidth = 0;
	protected int additionalHeight = 0;

	protected JScrollBar hSBar;
	protected JScrollBar vSBar;

	public ControlScrollPane() {
		this(null);
	}

	public ControlScrollPane(Component view) {
		super(view);
		setOpaque(false);
		getViewport().setOpaque(false);
		hSBar = getHorizontalScrollBar();
		vSBar = getVerticalScrollBar();
		preventScrollBarsFromHidingContent();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		result = getExtendedSize(result);
		return result;
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		if (result == null) {
			return null;
		}
		result = getExtendedSize(result);
		return result;
	}

	@Override
	public Dimension getMaximumSize() {
		Dimension result = super.getMaximumSize();
		if (result == null) {
			return null;
		}
		result = getExtendedSize(result);
		return result;
	}

	protected Dimension getExtendedSize(Dimension baseSize) {
		return new Dimension(baseSize.width + additionalWidth, baseSize.height + additionalHeight);
	}

	protected int getDefaultScrollBarThickness() {
		Integer result = ((Integer) UIManager.get("ScrollBar.width"));
		if (result == null) {
			result = 10;
		}
		return result;
	}

	protected void preventScrollBarsFromHidingContent() {
		vSBar.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED
						&& (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							updateAdditionalHeight();
							SwingRendererUtils.handleComponentSizeChange(ControlScrollPane.this);
						}

					});
				}
			}
		});
		hSBar.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED
						&& (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							updateAdditionalWidth();
							SwingRendererUtils.handleComponentSizeChange(ControlScrollPane.this);
						}

					});
				}
			}
		});
	}

	protected void updateAdditionalHeight() {
		additionalHeight = 0;
		{
			if ((hSBar != null) && hSBar.isVisible()) {
				additionalHeight = (hSBar.getHeight() > 0) ? hSBar.getHeight() : getDefaultScrollBarThickness();
			}
		}
	}

	protected void updateAdditionalWidth() {
		additionalWidth = 0;
		{
			if ((vSBar != null) && vSBar.isVisible()) {
				additionalWidth = (vSBar.getWidth() > 0) ? vSBar.getWidth() : getDefaultScrollBarThickness();
			}
		}
	}

}
