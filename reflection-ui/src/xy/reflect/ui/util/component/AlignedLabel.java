package xy.reflect.ui.util.component;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class AlignedLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	public AlignedLabel(String text) {
		super(text, SwingConstants.LEFT);
	}

	@Override
	public Dimension getPreferredSize() {
		Component root = SwingUtilities.getRoot(this);
		if (root == null) {
			return super.getPreferredSize();
		}
		Dimension result = super.getPreferredSize();
		int left = SwingUtilities.convertPoint(this, new Point(0, 0), root).x;
		int right = SwingUtilities.convertPoint(this,
				new Point(result.width, 0), root).x;
		int charWidth = getFontMetrics(getFont()).charWidth('m');
		int widthsInterval = charWidth
				* getCharacterCountForTextBoxexAlignment();
		right = (int) Math.round(Math.ceil(((double) right) / widthsInterval)
				* widthsInterval);
		result.width = right - left;
		return result;
	}

	protected int getCharacterCountForTextBoxexAlignment() {
		return 5;
	}

}
