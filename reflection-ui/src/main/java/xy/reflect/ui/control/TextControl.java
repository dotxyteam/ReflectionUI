package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITextualTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TextControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected ITextualTypeInfo textType;
	protected JTextArea textComponent;
	protected boolean textChangedByUser = true;
	protected Border textFieldNormalBorder;

	public TextControl(final ReflectionUI reflectionUI, final Object object,
			final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.textType = (ITextualTypeInfo) field.getType();

		setLayout(new BorderLayout());

		textComponent = new JTextArea();
		textComponent.setLineWrap(true);
		textComponent.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(textComponent) {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				result.height = Math.min(result.height, Toolkit
						.getDefaultToolkit().getScreenSize().height / 3);
				return result;
			}
		};
		add(scrollPane, BorderLayout.CENTER);
		textFieldNormalBorder = textComponent.getBorder();
		if (field.isReadOnly()) {
			textComponent.setEditable(false);
			textComponent.setBackground(ReflectionUIUtils
					.fixSeveralColorRenderingIssues(ReflectionUIUtils
							.getDisabledTextBackgroundColor()));
			scrollPane.setBorder(BorderFactory.createTitledBorder(""));
		} else {
			textComponent.getDocument().addUndoableEditListener(
					new UndoableEditListener() {

						@Override
						public void undoableEditHappened(UndoableEditEvent e) {
							try {
								onTextChange(textComponent.getText());
							} catch (Throwable t) {
								reflectionUI.handleExceptionsFromDisplayedUI(
										TextControl.this, t);
							}
						}
					});
		}
		refreshUI();
	}

	@Override
	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		boolean changed = !ReflectionUIUtils.equalsOrBothNull(error,
				textComponent.getToolTipText());
		if (!changed) {
			return true;
		}
		if (error != null) {
			reflectionUI.logError(error);
		}
		if (error == null) {
			setBorder(textFieldNormalBorder);
			textComponent.setToolTipText("");
			ReflectionUIUtils.showTooltipNow(textComponent);
		} else {
			TitledBorder border = BorderFactory.createTitledBorder("");
			border.setTitleColor(Color.RED);
			border.setBorder(BorderFactory.createLineBorder(Color.RED));
			setBorder(border);
			ReflectionUIUtils.setMultilineToolTipText(textComponent,
					reflectionUI.translateUIString(error.toString()));
			ReflectionUIUtils.showTooltipNow(textComponent);
		}
		return true;
	}

	protected void onTextChange(String newSTringValue) {
		if (!textChangedByUser) {
			return;
		}
		Object convertedvalue;
		try {
			convertedvalue = textType.fromText(newSTringValue);
		} catch (Throwable t) {
			displayError(new ReflectionUIError(t));
			reflectionUI.handleComponentSizeChange(this);
			return;
		}
		field.setValue(object, convertedvalue);
		reflectionUI.handleComponentSizeChange(this);
	}

	@Override
	public void requestFocus() {
		textComponent.requestFocus();
	}

	@Override
	public boolean refreshUI() {
		textChangedByUser = false;
		int lastCaretPosition = textComponent.getCaretPosition();
		textComponent.setText(textType.toText(field.getValue(object)));
		textComponent.setCaretPosition(Math.min(lastCaretPosition,
				textComponent.getText().length()));
		textChangedByUser = true;
		return true;
	}

	@Override
	public boolean showCaption() {
		return false;
	}

}
