package xy.reflect.ui.control.swing;

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
import xy.reflect.ui.control.IFieldControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class TextControl extends JPanel implements IFieldControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected JTextArea textComponent;
	protected boolean textChangedByUser = true;
	protected Border textFieldNormalBorder;

	public TextControl(final ReflectionUI reflectionUI, final Object object,
			final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;

		setLayout(new BorderLayout());

		textComponent = new JTextArea();
		// textComponent.setLineWrap(true);
		// textComponent.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(textComponent) {

			protected static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				result = fixScrollPaneSizeWHenVerticalBarVisible(result);
				result.height = Math.min(result.height, Toolkit
						.getDefaultToolkit().getScreenSize().height / 3);
				return result;
			}

			private Dimension fixScrollPaneSizeWHenVerticalBarVisible(
					Dimension size) {
				if (getHorizontalScrollBar().isVisible()) {
					size.height += getHorizontalScrollBar()
							.getPreferredSize().height;
				}
				return size;
			}
		};
		add(scrollPane, BorderLayout.CENTER);
		textFieldNormalBorder = textComponent.getBorder();
		if (field.isReadOnly()) {
			textComponent.setEditable(false);
			textComponent.setBackground(SwingRendererUtils
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
								reflectionUI.getSwingRenderer()
										.handleExceptionsFromDisplayedUI(
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
			SwingRendererUtils.showTooltipNow(textComponent);
		} else {
			TitledBorder border = BorderFactory.createTitledBorder("");
			border.setTitleColor(Color.RED);
			border.setBorder(BorderFactory.createLineBorder(Color.RED));
			setBorder(border);
			SwingRendererUtils.setMultilineToolTipText(textComponent,
					reflectionUI.prepareUIString(error.toString()));
			SwingRendererUtils.showTooltipNow(textComponent);
		}
		return true;
	}

	protected void onTextChange(String newStringValue) {
		if (!textChangedByUser) {
			return;
		}
		try {
			field.setValue(object, newStringValue);
		} catch (Throwable t) {
			displayError(new ReflectionUIError(t));
		}
	}

	@Override
	public void requestFocus() {
		textComponent.requestFocus();
	}

	@Override
	public boolean refreshUI() {
		textChangedByUser = false;
		String newText = (String) field.getValue(object);
		if (!ReflectionUIUtils.equalsOrBothNull(textComponent.getText(),
				newText)) {
			int lastCaretPosition = textComponent.getCaretPosition();
			textComponent.setText(newText);
			reflectionUI.getSwingRenderer().handleComponentSizeChange(this);
			textComponent.setCaretPosition(Math.min(lastCaretPosition,
					textComponent.getText().length()));
		}
		textChangedByUser = true;
		return true;
	}

	@Override
	public boolean showCaption() {
		return false;
	}

}
