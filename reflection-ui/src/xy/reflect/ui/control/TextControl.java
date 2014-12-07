package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITextualTypeInfo;

public class TextControl extends JPanel implements IRefreshableControl,
		ICanShowCaptionControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected ITextualTypeInfo textType;
	protected JFormattedTextField textField;
	protected boolean textChangedByUser = true;

	public TextControl(final ReflectionUI reflectionUI, final Object object,
			final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.textType = (ITextualTypeInfo) field.getType();

		setLayout(new BorderLayout());

		DefaultFormatter formatter = new DefaultFormatter();
		formatter.setCommitsOnValidEdit(true);
		textField = new JFormattedTextField(formatter);

		add(textField, BorderLayout.CENTER);
		textField.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("value".equals(evt.getPropertyName())) {
					onTextChange();
				}
			}
		});
		refreshUI();
	}

	protected void onTextChange() {
		if (!textChangedByUser) {
			return;
		}
		field.setValue(object, textType.fromText(textField.getText()));
	}

	@Override
	public void requestFocus() {
		textField.requestFocus();
	}

	@Override
	public void refreshUI() {
		if (field.isReadOnly()) {
			textField.setEditable(false);
		}
		textChangedByUser = false;
		textField.setText(textType.toText(field.getValue(object)));
		textChangedByUser = true;
	}

	@Override
	public void showCaption() {
		add(new JLabel(field.getCaption() + ": ", SwingConstants.LEFT) {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Component root = SwingUtilities.getRoot(this);
				if (root == null) {
					return super.getPreferredSize();
				}
				Dimension result = super.getPreferredSize();
				int left = SwingUtilities.convertPoint(this, new Point(0, 0),
						root).x;
				int right = SwingUtilities.convertPoint(this, new Point(
						result.width, 0), root).x;
				int charWidth = getFontMetrics(getFont()).charWidth('m');
				int widthsInterval = charWidth * getCharacterCountForTextBoxexAlignment();
				right = (int) Math.round(Math.ceil(((double) right)
						/ widthsInterval)
						* widthsInterval);
				result.width = right - left;
				return result;
			}

		}, BorderLayout.WEST);
	}

	protected int getCharacterCountForTextBoxexAlignment() {
		return 5;
	}

}
