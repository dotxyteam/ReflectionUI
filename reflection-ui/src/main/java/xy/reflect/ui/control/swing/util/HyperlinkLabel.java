package xy.reflect.ui.control.swing.util;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import xy.reflect.ui.util.MiscUtils;

public class HyperlinkLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	private String rawText;
	private Runnable linkOpener;

	private boolean revalidationDisabled = false;

	public HyperlinkLabel() {
		setup();
	}

	public Runnable getLinkOpener() {
		return linkOpener;
	}

	public void setLinkOpener(Runnable linkOpener) {
		this.linkOpener = linkOpener;
	}

	public String getRawText() {
		return rawText;
	}

	@Override
	public void setText(String text) {
		rawText = text;
		setText(text, false);
	}

	public void setText(String text, boolean underlined) {
		if (text == null) {
			super.setText(null);
			this.rawText = null;
		} else {
			String htmlText = MiscUtils.escapeHTML(text, true);
			htmlText = underlined ? "<u>" + htmlText + "</u>" : htmlText;
			htmlText = "<html><span style=\"color: " + getLinkColorCode() + ";\">" + htmlText + "</span></html>";
			super.setText(htmlText);
			this.rawText = text;
		}
	}

	protected void setup() {
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				openLink();
			}

			public void mouseEntered(MouseEvent e) {
				revalidationDisabled = true;
				try {
					setText(rawText, true);
				} finally {
					revalidationDisabled = false;
				}
			}

			public void mouseExited(MouseEvent e) {
				revalidationDisabled = true;
				try {
					setText(rawText, false);
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
		return System.getProperty(HyperlinkLabel.class.getName() + ".LINK_COLOR_CODE", "#000099");
	}

}