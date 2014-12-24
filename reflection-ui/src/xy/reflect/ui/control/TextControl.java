package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITextualTypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.component.TabulatingLabel;

public class TextControl extends JPanel implements IRefreshableControl,
		ICanShowCaptionControl, ICanDisplayErrorControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected ITextualTypeInfo textType;
	protected JFormattedTextField textField;
	protected boolean textChangedByUser = true;
	private Border textFieldNormalBorder;

	public TextControl(final ReflectionUI reflectionUI, final Object object,
			final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.textType = (ITextualTypeInfo) field.getType();

		setLayout(new BorderLayout());

		DefaultFormatter formatter = new DefaultFormatter();
		formatter.setCommitsOnValidEdit(true);
		formatter.setOverwriteMode(false);
		textField = new JFormattedTextField(formatter) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void processFocusEvent(FocusEvent e) {
				textChangedByUser = false;
				super.processFocusEvent(e);
				textChangedByUser = true;
			}

		};
		textFieldNormalBorder = textField.getBorder();

		add(textField, BorderLayout.CENTER);
		textField.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				try {
					if ("value".equals(evt.getPropertyName())) {
						onTextChange((String) evt.getNewValue());
					}
				} catch (Throwable t) {
					displayError(t.toString());
				}
			}
		});
		refreshUI();
	}

	@Override
	public void displayError(String error) {
		boolean changed = (error == null) != (textField.getBorder() == textFieldNormalBorder);
		if (!changed) {
			return;
		}
		if (error == null) {
			textField.setBorder(textFieldNormalBorder);
			textField.setToolTipText("");
			ReflectionUIUtils.showTooltipNow(textField);
		} else {
			TitledBorder border = BorderFactory.createTitledBorder("");
			border.setTitleColor(Color.RED);
			border.setBorder(BorderFactory.createLineBorder(Color.RED));
			textField.setBorder(border);
			textField.setToolTipText(error);
			ReflectionUIUtils.showTooltipNow(textField);
		}
	}

	protected void onTextChange(String newValue) {
		if (!textChangedByUser) {
			return;
		}
		field.setValue(object, textType.fromText(newValue));
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
		add(new TabulatingLabel(field.getCaption() + ": "), BorderLayout.WEST);
	}

}
