package xy.reflect.ui.control.swing.util;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Window.Type;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class HyperlinkTooltip {

	protected static int MILLISECONDS_BEFORE_OPENING = Integer
			.valueOf(System.getProperty(HyperlinkTooltip.class.getName() + ".MILLISECONDS_BEFORE_OPENING", "1000"));
	protected static int MILLISECONDS_BEFORE_CLOSING = Integer
			.valueOf(System.getProperty(HyperlinkTooltip.class.getName() + ".MILLISECONDS_BEFORE_CLOSING", "1000"));

	protected static final WeakHashMap<JComponent, HyperlinkTooltip> BY_COMPONENT = new WeakHashMap<>();

	protected final String message;
	protected final Runnable linkOpener;
	protected Object customValue;
	protected Function<JComponent, Rectangle> customComponentResponsiveBoundsMapper;

	protected final JWindow window;
	protected final MouseMotionListener mouseMotionListener;
	protected Timer showingTimer;
	protected Timer hidingTimer;

	protected HyperlinkTooltip(JComponent component, String message, Runnable linkOpener) {
		this.message = message;
		this.linkOpener = linkOpener;
		this.window = new JWindow(SwingUtilities.getWindowAncestor(component));
		window.setType(Type.UTILITY);
		window.setAlwaysOnTop(true);
		window.setFocusableWindowState(false);
		HyperlinkLabel label = createHyperlinkLabel();
		label.setOpaque(true);
		label.setBackground(getBackground());
		label.setBorder(getBorder());
		label.setRawTextAndLinkOpener(message, new Runnable() {
			@Override
			public void run() {
				window.setVisible(false);
				if (HyperlinkTooltip.this.linkOpener != null) {
					HyperlinkTooltip.this.linkOpener.run();
				}
			}
		});
		window.add(label);
		window.pack();
		showingTimer = new Timer(MILLISECONDS_BEFORE_OPENING, e -> {
			if (!isConnectedTo(component)) {
				return;
			}
			PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo != null) {
				Point pointerLocation = pointerInfo.getLocation();
				Rectangle componentBounds = getComponentResponsiveBoundsOnScreen(component);
				if (componentBounds.contains(pointerLocation)) {
					window.setLocation(componentBounds.x + ((componentBounds.width - window.getWidth()) / 2),
							componentBounds.y + componentBounds.height);
					window.setVisible(true);
					hidingTimer.restart();
				}
			}
		});
		showingTimer.setRepeats(false);
		hidingTimer = new Timer(MILLISECONDS_BEFORE_CLOSING, e -> {
			if (!isConnectedTo(component)) {
				return;
			}
			PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo != null) {
				Point pointerLocation = pointerInfo.getLocation();
				Rectangle componentBounds = getComponentResponsiveBoundsOnScreen(component);
				if (!componentBounds.contains(pointerLocation) && !window.getBounds().contains(pointerLocation)) {
					window.setVisible(false);
				} else {
					hidingTimer.restart();
				}
			} else {
				window.setVisible(false);
			}
		});
		hidingTimer.setRepeats(false);
		mouseMotionListener = new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!window.isVisible()) {
					showingTimer.restart();
				} else {
					hidingTimer.restart();
				}
			}
		};
		component.addMouseMotionListener(mouseMotionListener);
		label.addMouseMotionListener(mouseMotionListener);

	}

	protected Rectangle getComponentResponsiveBoundsOnScreen(JComponent component) {
		Rectangle result = component.getBounds();
		result.setLocation(component.getLocationOnScreen());
		if (customComponentResponsiveBoundsMapper != null) {
			Rectangle responsiveBounds = customComponentResponsiveBoundsMapper.apply(component);
			if (responsiveBounds == null) {
				return null;
			}
			result.x += responsiveBounds.x;
			result.y += responsiveBounds.y;
			result.width = responsiveBounds.width;
			result.height = responsiveBounds.height;
		}
		return result;
	}

	protected boolean isConnectedTo(JComponent component) {
		return Arrays.asList(component.getMouseMotionListeners()).contains(mouseMotionListener);
	}

	protected HyperlinkLabel createHyperlinkLabel() {
		return new HyperlinkLabel();
	}

	protected Border getBorder() {
		return UIManager.getBorder("ToolTip.border");
	}

	protected Color getBackground() {
		return UIManager.getColor("ToolTip.background");
	}

	public String getMessage() {
		return message;
	}

	public Runnable getLinkOpener() {
		return linkOpener;
	}

	public Object getCustomValue() {
		return customValue;
	}

	public void setCustomValue(Object customValue) {
		this.customValue = customValue;
	}

	public Function<JComponent, Rectangle> getCustomComponentResponsiveBoundsMapper() {
		return customComponentResponsiveBoundsMapper;
	}

	public void setCustomComponentResponsiveBoundsMapper(
			Function<JComponent, Rectangle> customComponentResponsiveBoundsMapper) {
		this.customComponentResponsiveBoundsMapper = customComponentResponsiveBoundsMapper;
	}

	public MouseMotionListener getMouseMotionListener() {
		return mouseMotionListener;
	}

	public Timer getShowingTimer() {
		return showingTimer;
	}

	public Timer getHidingTimer() {
		return hidingTimer;
	}

	public static void set(JComponent component, String message, Runnable linkOpener) {
		unset(component);
		BY_COMPONENT.put(component, new HyperlinkTooltip(component, message, linkOpener));
	}

	public static void unset(JComponent component) {
		HyperlinkTooltip tooltip = BY_COMPONENT.remove(component);
		if (tooltip != null) {
			component.removeMouseMotionListener(tooltip.mouseMotionListener);
			tooltip.window.setVisible(false);
			tooltip.window.dispose();
		}
	}

	public static HyperlinkTooltip get(JComponent comp) {
		return BY_COMPONENT.get(comp);
	}
}
