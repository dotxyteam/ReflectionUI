


package xy.reflect.ui.control.swing.util;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

/**
 * Alternative decorations (title bar, border, ...) for windows.
 * 
 * @author olitank
 *
 */
public class AlternativeWindowDecorationsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int W = 4;
	private final SideLabel left = new SideLabel(Side.W);
	private final SideLabel right = new SideLabel(Side.E);
	private final SideLabel top = new SideLabel(Side.N);
	private final SideLabel bottom = new SideLabel(Side.S);
	private final SideLabel topleft = new SideLabel(Side.NW);
	private final SideLabel topright = new SideLabel(Side.NE);
	private final SideLabel bottomleft = new SideLabel(Side.SW);
	private final SideLabel bottomright = new SideLabel(Side.SE);
	private final JPanel contentPane = new JPanel(new BorderLayout());
	private JLabel titleLabel = new JLabel("", JLabel.CENTER) {

		private static final long serialVersionUID = 1L;

		{
			Font font = getFont();
			font = new Font(font.getName(), Font.BOLD, Math.round(font.getSize() * 1.2f));
			setFont(font);
		}

		@Override
		public Color getForeground() {
			return getTitleBarForegroundColor();
		}

	};
	private final JPanel titlePanel = new JPanel(new BorderLayout());
	private final JPanel resizePanel = new JPanel(new BorderLayout()) {

		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			int w = getWidth();
			int h = getHeight();
			if (isTitleBarBackgroundPainted()) {
				g2.setPaint(getTitleBarBackgroundColor());
				g2.fillRect(0, 0, w, top.getHeight() + titlePanel.getHeight());
			}
			if (isBorderPainted()) {
				g2.setPaint(getBorderColor());
				g2.drawRect(0, 0, w - 1, h - 1);
			}

			g2.dispose();
		}
	};
	private JButton maximizeButton;
	private JButton closeButton;

	public AlternativeWindowDecorationsPanel(String windowTitle, Icon windowIcon) {
		super(new BorderLayout());
		init(windowTitle, windowIcon);
	}

	public AlternativeWindowDecorationsPanel(String windowTitle, Icon windowIcon, Window window,
			Component windowContent) {
		this(windowTitle, windowIcon);
		configureWindow(window);
		contentPane.add(windowContent);
	}

	public JButton getMaximizeButton() {
		return maximizeButton;
	}

	public JButton getCloseButton() {
		return closeButton;
	}

	public JPanel getContentPane() {
		return contentPane;
	}

	public JLabel getTitleLabel() {
		return titleLabel;
	}

	protected Color getTitleBarBackgroundColor() {
		return Color.RED;
	}

	protected Color getTitleBarForegroundColor() {
		return Color.YELLOW;
	}

	protected Color getBorderColor() {
		return Color.CYAN;
	}

	protected boolean isTitleBarBackgroundPainted() {
		return true;
	}

	protected boolean isBorderPainted() {
		return true;
	}

	public void init(String title, Icon icon) {

		setOpaque(false);
		add(resizePanel, BorderLayout.CENTER);

		titleLabel.setText(title);
		titleLabel.setIcon(icon);

		DragWindowListener dwl = new DragWindowListener();
		titlePanel.addMouseListener(dwl);
		titlePanel.addMouseMotionListener(dwl);
		titlePanel.setOpaque(false);
		titlePanel.setBorder(BorderFactory.createEmptyBorder(W, W, W, W));
		titlePanel.add(titleLabel);
		titlePanel.add(makeButtons(), BorderLayout.EAST);

		JPanel titlePanelContainer = new JPanel(new BorderLayout(0, 0));
		titlePanelContainer.add(top, BorderLayout.NORTH);
		titlePanelContainer.add(titlePanel, BorderLayout.CENTER);

		JPanel northPanel = new JPanel(new BorderLayout(0, 0));
		northPanel.add(topleft, BorderLayout.WEST);
		northPanel.add(titlePanelContainer, BorderLayout.CENTER);
		northPanel.add(topright, BorderLayout.EAST);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(bottomleft, BorderLayout.WEST);
		southPanel.add(bottom, BorderLayout.CENTER);
		southPanel.add(bottomright, BorderLayout.EAST);

		resizePanel.add(left, BorderLayout.WEST);
		resizePanel.add(right, BorderLayout.EAST);
		resizePanel.add(northPanel, BorderLayout.NORTH);
		resizePanel.add(southPanel, BorderLayout.SOUTH);
		resizePanel.add(contentPane, BorderLayout.CENTER);

		titlePanelContainer.setOpaque(false);
		northPanel.setOpaque(false);
		southPanel.setOpaque(false);

		contentPane.setOpaque(false);
		resizePanel.setOpaque(false);
	}

	private JPanel makeButtons() {
		JPanel result = new JPanel();
		result.setLayout(new FlowLayout());
		result.setOpaque(false);
		result.add(maximizeButton = makeMaximizeButton());
		result.add(closeButton = makeCloseButton());
		return result;
	}

	private JButton makeCloseButton() {
		final JButton button = new JButton(new CloseIcon());
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setOpaque(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComponent b = (JComponent) e.getSource();
				Window w = SwingUtilities.getWindowAncestor(b);
				if (w != null) {
					w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
		return button;
	}

	private JButton makeMaximizeButton() {
		final JButton button = new JButton(new MaximizeIcon());
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setOpaque(false);
		button.addActionListener(new ActionListener() {

			Rectangle minimizedBounds;

			@Override
			public void actionPerformed(ActionEvent e) {
				JComponent b = (JComponent) e.getSource();
				Window w = SwingUtilities.getWindowAncestor(b);
				Rectangle maximizedBounds = SwingRendererUtils
						.getMaximumWindowBounds(SwingRendererUtils.getWindowCurrentGraphicsDevice(w));
				if (almostEquals(w.getBounds(), maximizedBounds, maximizedBounds.width / 30)) {
					if (minimizedBounds == null) {
						minimizedBounds = new Rectangle(maximizedBounds);
						minimizedBounds.grow(-minimizedBounds.width / 6, -minimizedBounds.height / 6);
					}
					w.setBounds(minimizedBounds);
				} else {
					minimizedBounds = w.getBounds();
					w.setBounds(maximizedBounds);
				}
			}

			boolean almostEquals(Rectangle bounds1, Rectangle bounds2, int maxError) {
				if (Math.abs(bounds1.x - bounds2.x) > maxError) {
					return false;
				}
				if (Math.abs(bounds1.y - bounds2.y) > maxError) {
					return false;
				}
				if (Math.abs((bounds1.x + bounds1.width) - (bounds2.x + bounds2.width)) > maxError) {
					return false;
				}
				if (Math.abs((bounds1.y + bounds1.height) - (bounds2.y + bounds2.height)) > maxError) {
					return false;
				}
				return true;
			}
		});
		return button;
	}

	protected void configureWindow(Window window) {
		if (window instanceof JFrame) {
			((JFrame) window).setUndecorated(true);
		} else if (window instanceof JDialog) {
			((JDialog) window).setUndecorated(true);
		} else {
			throw new IllegalArgumentException();
		}
		ResizeWindowListener rwl = new ResizeWindowListener(window);
		for (SideLabel l : Arrays.asList(left, right, top, bottom, topleft, topright, bottomleft, bottomright)) {
			l.addMouseListener(rwl);
			l.addMouseMotionListener(rwl);
		}
	}

	private enum Side {
		N(Cursor.N_RESIZE_CURSOR, new Dimension(0, Side.THICKNESS)),
		W(Cursor.W_RESIZE_CURSOR, new Dimension(Side.THICKNESS, 0)),
		E(Cursor.E_RESIZE_CURSOR, new Dimension(Side.THICKNESS, 0)),
		S(Cursor.S_RESIZE_CURSOR, new Dimension(0, Side.THICKNESS)),
		NW(Cursor.NW_RESIZE_CURSOR, new Dimension(Side.THICKNESS, Side.THICKNESS)),
		NE(Cursor.NE_RESIZE_CURSOR, new Dimension(Side.THICKNESS, Side.THICKNESS)),
		SW(Cursor.SW_RESIZE_CURSOR, new Dimension(Side.THICKNESS, Side.THICKNESS)),
		SE(Cursor.SE_RESIZE_CURSOR, new Dimension(Side.THICKNESS, Side.THICKNESS));

		private static final int THICKNESS = 4;

		public final Dimension dim;
		public final int cursor;

		private Side(int cursor, Dimension dim) {
			this.cursor = cursor;
			this.dim = dim;
		}
	}

	private static class SideLabel extends JLabel {
		private static final long serialVersionUID = 1L;
		public final Side side;

		public SideLabel(Side side) {
			super();
			this.side = side;
			setCursor(Cursor.getPredefinedCursor(side.cursor));
		}

		@Override
		public Dimension getPreferredSize() {
			return side.dim;
		}

		@Override
		public Dimension getMinimumSize() {
			return side.dim;
		}

		@Override
		public Dimension getMaximumSize() {
			return side.dim;
		}
	}

	private static class ResizeWindowListener extends MouseAdapter {
		private final Window window;
		private Rectangle rect;

		public ResizeWindowListener(Window window) {
			super();
			this.window = window;
			this.rect = window.getBounds();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			rect = window.getBounds();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (rect == null) {
				return;
			}
			Side side = ((SideLabel) e.getComponent()).side;
			window.setBounds(getResizedRect(rect, side, e.getX(), e.getY()));
		}

		private static Rectangle getResizedRect(Rectangle r, Side side, int dx, int dy) {
			switch (side) {
			case NW:
				r.y += dy;
				r.height -= dy;
				r.x += dx;
				r.width -= dx;
				break;
			case N:
				r.y += dy;
				r.height -= dy;
				break;
			case NE:
				r.y += dy;
				r.height -= dy;
				r.width += dx;
				break;
			case W:
				r.x += dx;
				r.width -= dx;
				break;
			case E:
				r.width += dx;
				break;
			case SW:
				r.height += dy;
				r.x += dx;
				r.width -= dx;
				break;
			case S:
				r.height += dy;
				break;
			case SE:
				r.height += dy;
				r.width += dx;
				break;
			default:
				throw new AssertionError("Unknown SideLabel");
			}
			return r;
		}
	}

	private static class DragWindowListener extends MouseAdapter {
		private final transient Point startPt = new Point();
		private transient Window window;

		@Override
		public void mousePressed(MouseEvent me) {
			if (window == null) {
				Object o = me.getSource();
				if (o instanceof Window) {
					window = (Window) o;
				} else if (o instanceof JComponent) {
					window = SwingUtilities.windowForComponent(me.getComponent());
				}
			}
			startPt.setLocation(me.getPoint());
		}

		@Override
		public void mouseDragged(MouseEvent me) {
			if (window != null) {
				Point eventLocationOnScreen = me.getLocationOnScreen();
				window.setLocation(eventLocationOnScreen.x - startPt.x, eventLocationOnScreen.y - startPt.y);
			}
		}
	}

	private class CloseIcon implements Icon {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);
			g.setColor(getTitleBarForegroundColor());
			g.drawLine(4, 4, 11, 11);
			g.drawLine(4, 5, 10, 11);
			g.drawLine(5, 4, 11, 10);
			g.drawLine(11, 4, 4, 11);
			g.drawLine(11, 5, 5, 11);
			g.drawLine(10, 4, 4, 10);
			g.translate(-x, -y);
		}

		@Override
		public int getIconWidth() {
			return 16;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	}

	private class MaximizeIcon implements Icon {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.translate(x, y);
			g.setColor(getTitleBarForegroundColor());
			g.drawLine(4, 4, 11, 4);
			g.drawLine(11, 4, 11, 11);
			g.drawLine(11, 11, 4, 11);
			g.drawLine(4, 11, 4, 4);
			g.translate(-x, -y);
		}

		@Override
		public int getIconWidth() {
			return 16;
		}

		@Override
		public int getIconHeight() {
			return 16;
		}
	}

}
