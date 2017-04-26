package xy.reflect.ui.util.component;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.swing.*;

import xy.reflect.ui.util.SwingRendererUtils;

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
	private final JPanel resizePanel = new JPanel(new BorderLayout()) {

		private static final long serialVersionUID = 1L;

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			int w = getWidth();
			int h = getHeight();
			g2.setPaint(getDecorationsBackgroundColor());
			g2.fillRect(0, 0, w, h);
			g2.setPaint(getDecorationsForegroundColor());
			g2.drawRect(0, 0, w - 1, h - 1);

			g2.drawLine(0, 2, 2, 0);
			g2.drawLine(w - 3, 0, w - 1, 2);

			g2.clearRect(0, 0, 2, 1);
			g2.clearRect(0, 0, 1, 2);
			g2.clearRect(w - 2, 0, 2, 1);
			g2.clearRect(w - 1, 0, 1, 2);

			g2.dispose();
		}
	};
	private JButton maximizeButton;
	private JButton closeButton;

	public AlternativeWindowDecorationsPanel(String windowTitle) {
		super(new BorderLayout());
		add(resizePanel, BorderLayout.CENTER);
		init(windowTitle);
	}

	public AlternativeWindowDecorationsPanel(String windowTitle, Window window, Component windowContent) {
		this(windowTitle);
		configureWindow(window);
		contentPane.add(windowContent);
	}

	public JButton getMaximizeButton() {
		return maximizeButton;
	}

	public JButton getCloseButton() {
		return closeButton;
	}

	public Color getDecorationsBackgroundColor() {
		return Color.RED;
	}

	public Color getDecorationsForegroundColor() {
		return Color.YELLOW;
	}

	public void init(String titleText) {

		JPanel titlePanel = new JPanel(new BorderLayout());
		DragWindowListener dwl = new DragWindowListener();
		titlePanel.addMouseListener(dwl);
		titlePanel.addMouseMotionListener(dwl);
		titlePanel.setOpaque(false);
		titlePanel.setBorder(BorderFactory.createEmptyBorder(W, W, W, W));

		JLabel titleLabel = new JLabel(titleText, JLabel.CENTER);
		titleLabel.setForeground(getDecorationsForegroundColor());
		titleLabel.setFont(getDecorationsFont());
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

	public JPanel getContentPane() {
		return contentPane;
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
		button.setOpaque(true);
		button.setBackground(getDecorationsBackgroundColor());
		button.setFont(getDecorationsFont());
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
		button.setOpaque(true);
		button.setBackground(getDecorationsBackgroundColor());
		button.setFont(getDecorationsFont());
		button.addActionListener(new ActionListener() {

			Rectangle minimizeBounds;

			@Override
			public void actionPerformed(ActionEvent e) {
				JComponent b = (JComponent) e.getSource();
				Window w = SwingUtilities.getWindowAncestor(b);
				JFrame frame = (JFrame) w;
				if (minimizeBounds != null) {
					w.setBounds(minimizeBounds);
					minimizeBounds = null;
				} else {
					minimizeBounds = w.getBounds();
					Rectangle maxBounds = SwingRendererUtils
							.getMaximumWindowBounds(SwingRendererUtils.getWindowCurrentGraphicsDevice(frame));
					w.setBounds(maxBounds);
				}
			}
		});
		return button;
	}

	public Font getDecorationsFont() {
		return new Font("Arial", Font.BOLD, 14);
	}

	public void configureWindow(Window window) {
		if (window instanceof JFrame) {
			((JFrame) window).setUndecorated(true);
		} else if (window instanceof JDialog) {
			((JDialog) window).setUndecorated(true);
		}
		ResizeWindowListener rwl = new ResizeWindowListener(window);
		for (SideLabel l : Arrays.asList(left, right, top, bottom, topleft, topright, bottomleft, bottomright)) {
			l.addMouseListener(rwl);
			l.addMouseMotionListener(rwl);
		}
	}

	public void attachToWindow(Window window) {
		configureWindow(window);
		installOnWindow(window);
	}

	public void installOnWindow(Window window) {
		if (window instanceof JFrame) {
			((JFrame) window).setContentPane(this);
		} else if (window instanceof JDialog) {
			((JDialog) window).setContentPane(this);
		}
	}

	private enum Side {
		N(Cursor.N_RESIZE_CURSOR, new Dimension(0, 4)), W(Cursor.W_RESIZE_CURSOR, new Dimension(4, 0)), E(
				Cursor.E_RESIZE_CURSOR, new Dimension(4, 0)), S(Cursor.S_RESIZE_CURSOR, new Dimension(0, 4)), NW(
						Cursor.NW_RESIZE_CURSOR, new Dimension(4, 4)), NE(Cursor.NE_RESIZE_CURSOR,
								new Dimension(4, 4)), SW(Cursor.SW_RESIZE_CURSOR,
										new Dimension(4, 4)), SE(Cursor.SE_RESIZE_CURSOR, new Dimension(4, 4));
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
			g.setColor(getDecorationsForegroundColor());
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
			g.setColor(getDecorationsForegroundColor());
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
