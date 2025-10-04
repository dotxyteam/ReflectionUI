package xy.reflect.ui.control.swing.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import xy.reflect.ui.util.MiscUtils;

public class HyperlinkLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	protected String rawText;
	protected boolean underlined = false;
	protected Runnable linkOpener;
	protected Object customValue;

	protected boolean revalidationDisabled = false;

	public HyperlinkLabel(String rawText, Runnable linkOpener) {
		super((String) null);
		setForeground(Color.BLUE);
		setup();
		setRawTextAndLinkOpener(rawText, linkOpener);
		refresh();
	}

	public HyperlinkLabel() {
		this(null, null);
	}

	public Runnable getLinkOpener() {
		return linkOpener;
	}

	public void setLinkOpener(Runnable linkOpener) {
		this.linkOpener = linkOpener;
		refresh();
	}

	public String getRawText() {
		return rawText;
	}

	public void setRawText(String text) {
		this.rawText = text;
		refresh();
	}

	public void setRawTextAndLinkOpener(String rawText, Runnable linkOpener) {
		this.rawText = rawText;
		this.linkOpener = linkOpener;
		refresh();
	}

	@Override
	public void setText(String text) {
		if (text == null) {
			setRawText(null);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public Object getCustomValue() {
		return customValue;
	}

	public void setCustomValue(Object customValue) {
		this.customValue = customValue;
	}

	protected void refresh() {
		if (rawText == null) {
			super.setText(null);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} else {
			if (linkOpener == null) {
				super.setText(rawText);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			} else {
				String htmlText = MiscUtils.escapeHTML(rawText, true);
				htmlText = underlined ? "<u>" + htmlText + "</u>" : htmlText;
				htmlText = "<html><span style=\"color: " + getLinkColorCode() + ";\">" + htmlText + "</span></html>";
				super.setText(htmlText);
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		refresh();
	}

	protected void setup() {
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				openLink();
			}

			public void mouseEntered(MouseEvent e) {
				revalidationDisabled = true;
				try {
					HyperlinkLabel.this.underlined = true;
					refresh();
				} finally {
					revalidationDisabled = false;
				}
			}

			public void mouseExited(MouseEvent e) {
				revalidationDisabled = true;
				try {
					HyperlinkLabel.this.underlined = false;
					refresh();
				} finally {
					revalidationDisabled = false;
				}
			}
		});
	}

	@Override
	public void revalidate() {
		if (revalidationDisabled) {
			return;
		}
		super.revalidate();
	}

	protected void openLink() {
		if (linkOpener != null) {
			linkOpener.run();
		}
	}

	protected String getLinkColorCode() {
		if (getForeground() == null) {
			return System.getProperty(HyperlinkLabel.class.getName() + ".LINK_COLOR_CODE", "#000099");
		} else {
			Color color = getForeground();
			return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
		}
	}

}