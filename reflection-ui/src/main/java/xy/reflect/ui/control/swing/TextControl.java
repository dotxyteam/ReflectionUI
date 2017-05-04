package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class TextControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	protected JTextArea textComponent;
	protected JScrollPane scrollPane;
	protected boolean listenerDisabled = false;

	public TextControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();

		setLayout(new BorderLayout());

		textComponent = createTextComponent();
		{
			updateTextComponent();
			scrollPane = createScrollPane();
			scrollPane.setViewportView(textComponent);
			scrollPane.setBorder(null);
			add(scrollPane, BorderLayout.CENTER);
		}
		SwingRendererUtils.handleComponentSizeChange(this);
	}

	protected JScrollPane createScrollPane() {
		return new JScrollPane() {

			protected static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				result = fixScrollPaneSizeWHenVerticalBarVisible(result);
				int characterSize = SwingRendererUtils.getStandardCharacterWidth(textComponent);
				result.width = Math.min(result.width, characterSize * 20);
				result.height = Math.min(result.height, Toolkit.getDefaultToolkit().getScreenSize().height / 3);
				return result;
			}

			private Dimension fixScrollPaneSizeWHenVerticalBarVisible(Dimension size) {
				if (getHorizontalScrollBar().isVisible()) {
					size.height += getHorizontalScrollBar().getPreferredSize().height;
				}
				return size;
			}
		};
	}

	protected JTextArea createTextComponent() {
		JTextArea result = new JTextArea() {

			private static final long serialVersionUID = 1L;

			@Override
			public void replaceSelection(String content) {
				boolean listenerWasDisabled = listenerDisabled;
				listenerDisabled = true;
				try {
					super.replaceSelection(content);
				} finally {
					listenerDisabled = listenerWasDisabled;
				}
				textComponentEditHappened();
			}

		};
		if (data.isGetOnly()) {
			result.setEditable(false);
			result.setBackground(ReflectionUIUtils.getDisabledTextBackgroundColor());
		} else {
			result.getDocument().addUndoableEditListener(new UndoableEditListener() {

				@Override
				public void undoableEditHappened(UndoableEditEvent e) {
					TextControl.this.textComponentEditHappened();
				}
			});
		}
		result.setBorder(new JTextField().getBorder());
		return result;
	}

	protected void textComponentEditHappened() {
		if (listenerDisabled) {
			return;
		}
		try {
			onTextChange(textComponent.getText());
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
			displayError(ReflectionUIUtils.getPrettyErrorMessage(t));
		}
	}

	protected void onTextChange(String newStringValue) {
		data.setValue(newStringValue);
	}

	protected void updateTextComponent() {
		listenerDisabled = true;
		try {
			String newText = (String) data.getValue();
			if (!ReflectionUIUtils.equalsOrBothNull(textComponent.getText(), newText)) {
				int lastCaretPosition = textComponent.getCaretPosition();
				textComponent.setText(newText);
				textComponent
						.setCaretPosition(Math.min(lastCaretPosition, ((JTextArea) textComponent).getText().length()));
				displayError(null);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
		} finally {
			listenerDisabled = false;
		}
	}

	protected Component createIconTrol() {
		return new JLabel();
	}

	@Override
	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	@Override
	public boolean displayError(String msg) {
		SwingRendererUtils.displayErrorOnBorderAndTooltip(this, textComponent, msg, swingRenderer);
		return true;
	}

	@Override
	public boolean refreshUI() {
		updateTextComponent();
		return true;
	}

	@Override
	public boolean showsCaption() {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public boolean requestCustomFocus() {
		textComponent.setCaretPosition(textComponent.getText().length());
		if (SwingRendererUtils.requestAnyComponentFocus(textComponent, swingRenderer)) {
			return true;
		}
		return false;
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "TextControl [data=" + data + "]";
	}

}
