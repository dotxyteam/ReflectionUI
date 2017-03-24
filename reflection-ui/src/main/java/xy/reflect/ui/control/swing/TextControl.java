package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class TextControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	protected Component textComponent;
	protected boolean ignoreEditEvents = true;

	public TextControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = retrieveData();

		setLayout(new BorderLayout());

		textComponent = createTextComponent();
		{
			updateTextComponent();
			JScrollPane scrollPane = new JScrollPane(textComponent) {

				protected static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension result = super.getPreferredSize();
					result = fixScrollPaneSizeWHenVerticalBarVisible(result);
					result.width = Math.min(result.width, Toolkit.getDefaultToolkit().getScreenSize().width / 3);
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
			scrollPane.setBorder(null);
			add(scrollPane, BorderLayout.CENTER);
		}
	}

	protected IFieldControlData retrieveData() {
		return input.getControlData();
	}

	protected Component createTextComponent() {
		final JTextArea result = new JTextArea() {

			private static final long serialVersionUID = 1L;

			@Override
			public void replaceSelection(String content) {
				boolean wasIgnoringEditEvents = ignoreEditEvents;
				ignoreEditEvents = true;
				super.replaceSelection(content);
				ignoreEditEvents = wasIgnoringEditEvents;
				onTextChange(getText());
			}

		};
		if (data.isGetOnly()) {
			result.setEditable(false);
			result.setBackground(ReflectionUIUtils.getDisabledTextBackgroundColor());
		} else {
			result.getDocument().addUndoableEditListener(new UndoableEditListener() {

				@Override
				public void undoableEditHappened(UndoableEditEvent e) {
					if (ignoreEditEvents) {
						return;
					}
					try {
						onTextChange(result.getText());
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(TextControl.this, t);
					}
				}
			});
		}
		result.setBorder(BorderFactory.createTitledBorder(""));
		return result;
	}

	protected void onTextChange(String newStringValue) {
		try {
			data.setValue(newStringValue);
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
			displayError(ReflectionUIUtils.getPrettyErrorMessage(t));
		}
	}

	protected void updateTextComponent() {
		ignoreEditEvents = true;
		String newText = (String) data.getValue();
		if (!ReflectionUIUtils.equalsOrBothNull(((JTextArea) textComponent).getText(), newText)) {
			int lastCaretPosition = ((JTextArea) textComponent).getCaretPosition();
			((JTextArea) textComponent).setText(newText);
			((JTextArea) textComponent)
					.setCaretPosition(Math.min(lastCaretPosition, ((JTextArea) textComponent).getText().length()));
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		ignoreEditEvents = false;
	}

	protected Component createIconTrol() {
		return new JLabel();
	}

	public static String toText(Object object) {
		return object.toString();
	}

	@Override
	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	@Override
	public boolean displayError(String msg) {
		String oldTooltipText;
		if (msg == null) {
			setBorder(null);
			oldTooltipText = ((JComponent) textComponent).getToolTipText();
			((JComponent) textComponent).setToolTipText(null);
		} else {
			SwingRendererUtils.setErrorBorder(this);
			oldTooltipText = ((JComponent) textComponent).getToolTipText();
			SwingRendererUtils.setMultilineToolTipText(((JComponent) textComponent),
					swingRenderer.prepareStringToDisplay(msg));
		}
		SwingRendererUtils.handleComponentSizeChange(this);
		if (!ReflectionUIUtils.equalsOrBothNull(oldTooltipText, ((JComponent) textComponent).getToolTipText())) {
			SwingRendererUtils.showTooltipNow(((JComponent) textComponent));
		}
		return true;
	}

	@Override
	public boolean refreshUI() {
		updateTextComponent();
		displayError(null);
		SwingRendererUtils.handleComponentSizeChange(this);
		return true;
	}

	@Override
	public boolean showCaption() {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public Object getFocusDetails() {
		int caretPosition = ((JTextArea) textComponent).getCaretPosition();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("caretPosition", caretPosition);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		int caretPosition = (Integer) focusDetails.get("caretPosition");
		textComponent.requestFocusInWindow();
		((JTextArea) textComponent)
				.setCaretPosition(Math.min(caretPosition, ((JTextArea) textComponent).getText().length()));
	}

	@Override
	public boolean requestFocusInWindow() {
		return textComponent.requestFocusInWindow();
	}

	@Override
	public void validateSubForm() throws Exception {
	}
}
