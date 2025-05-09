package xy.reflect.ui.control.swing.util;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class HyperlinkTooltip {

	private static int MILLISECONDS_BEFORE_OPENING = Integer
			.valueOf(System.getProperty(HyperlinkTooltip.class.getName() + ".MILLISECONDS_BEFORE_OPENING", "1000"));
	private static int MILLISECONDS_BEFORE_CLOSING = Integer
			.valueOf(System.getProperty(HyperlinkTooltip.class.getName() + ".MILLISECONDS_BEFORE_CLOSING", "1000"));

	private static final WeakHashMap<JComponent, HyperlinkTooltip> BY_COMPONENT = new WeakHashMap<>();

	private final String message;
	private final Runnable linkOpener;
	private Object customValue;

	private final JWindow window;
	private final MouseMotionListener mouseListener;
	private Timer showingTimer;
	private Timer hidingTimer;

	private HyperlinkTooltip(JComponent component, String message, Runnable linkOpener) {
		this.window = new JWindow();
		this.message = message;
		this.linkOpener = linkOpener;
		window.setAlwaysOnTop(true);
		window.setFocusableWindowState(false);
		HyperlinkLabel label = createHyperlinkLabel();
		label.setText(message);
		label.setOpaque(true);
		label.setBackground(getBackground());
		label.setBorder(getBorder());
		label.setLinkOpener(new Runnable() {
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
			PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo != null) {
				Point pointerLocation = pointerInfo.getLocation();
				Rectangle componentBounds = component.getBounds();
				componentBounds.setLocation(component.getLocationOnScreen());
				if (componentBounds.contains(pointerLocation)) {
					window.setLocationRelativeTo(component);
					window.setLocation(window.getLocation().x, componentBounds.y + component.getHeight());
					window.setVisible(true);
					hidingTimer.restart();
				}
			}
		});
		showingTimer.setRepeats(false);
		hidingTimer = new Timer(MILLISECONDS_BEFORE_CLOSING, e -> {
			PointerInfo pointerInfo = MouseInfo.getPointerInfo();
			if (pointerInfo != null) {
				Point pointerLocation = pointerInfo.getLocation();
				Rectangle componentBounds = component.getBounds();
				componentBounds.setLocation(component.getLocationOnScreen());
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
		mouseListener = new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!window.isVisible()) {
					showingTimer.restart();
				} else {
					hidingTimer.restart();
				}
			}
		};
		component.addMouseMotionListener(mouseListener);
		label.addMouseMotionListener(mouseListener);

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

	public static void set(JComponent comp, String message, Runnable action) {
		unset(comp);
		HyperlinkTooltip tooltip = new HyperlinkTooltip(comp, message, action);
		BY_COMPONENT.put(comp, tooltip);
	}

	public static void unset(JComponent comp) {
		HyperlinkTooltip tooltip = BY_COMPONENT.remove(comp);
		if (tooltip != null) {
			comp.removeMouseMotionListener(tooltip.mouseListener);
			tooltip.window.setVisible(false);
			tooltip.window.dispose();
		}
	}

	public static HyperlinkTooltip get(JComponent comp) {
		return BY_COMPONENT.get(comp);
	}
}
