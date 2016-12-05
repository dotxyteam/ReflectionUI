package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class TextControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;

	protected JTextArea textComponent;
	protected boolean ignoreEditEvents = true;
	protected Border textFieldNormalBorder;

	public TextControl(final SwingRenderer swingRenderer, final Object object, final IFieldInfo field) {
		this.swingRenderer = swingRenderer;
		this.object = object;
		this.field = field;

		setLayout(new BorderLayout());

		textComponent = createTextComponent();
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
		add(scrollPane, BorderLayout.CENTER);
		textFieldNormalBorder = textComponent.getBorder();
		if (field.isGetOnly()) {
			textComponent.setEditable(false);
			textComponent.setBackground(SwingRendererUtils
					.fixSeveralColorRenderingIssues(ReflectionUIUtils.getDisabledTextBackgroundColor()));
			scrollPane.setBorder(BorderFactory.createTitledBorder(""));
		} else {
			textComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {

				@Override
				public void undoableEditHappened(UndoableEditEvent e) {
					if (ignoreEditEvents) {
						return;
					}
					try {
						onTextChange(textComponent.getText());
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(TextControl.this, t);
					}
				}
			});
		}
	}

	protected JTextArea createTextComponent() {
		return new JTextArea() {

			private static final long serialVersionUID = 1L;

			@Override
			public void replaceSelection(String content) {
				boolean wasIgnoringEditEvents = ignoreEditEvents;
				ignoreEditEvents = true;
				super.replaceSelection(content);
				ignoreEditEvents = wasIgnoringEditEvents;
				onTextChange(textComponent.getText());
			}

		};
	}

	protected void updateTextComponent() {
		ignoreEditEvents = true;
		String newText = (String) field.getValue(object);
		if (!ReflectionUIUtils.equalsOrBothNull(textComponent.getText(), newText)) {
			int lastCaretPosition = textComponent.getCaretPosition();
			textComponent.setText(newText);
			swingRenderer.handleComponentSizeChange(this);
			textComponent.setCaretPosition(Math.min(lastCaretPosition, textComponent.getText().length()));
		}
		ignoreEditEvents = false;
	}

	public static String toText(Object object) {
		return object.toString();
	}

	@Override
	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	@Override
	public boolean displayError(ReflectionUIError error) {
		boolean changed = !ReflectionUIUtils.equalsOrBothNull(error, textComponent.getToolTipText());
		if (!changed) {
			return true;
		}
		if (error != null) {
			swingRenderer.getReflectionUI().logError(error);
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
					swingRenderer.prepareStringToDisplay(error.toString()));
			SwingRendererUtils.showTooltipNow(textComponent);
		}
		return true;
	}

	protected void onTextChange(String newStringValue) {
		try {
			field.setValue(object, newStringValue);
		} catch (Throwable t) {
			displayError(new ReflectionUIError(t));
		}
	}

	@Override
	public boolean refreshUI() {
		updateTextComponent();
		displayError(null);
		swingRenderer.handleComponentSizeChange(this);
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
		int caretPosition = textComponent.getCaretPosition();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("caretPosition", caretPosition);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		int caretPosition = (Integer) focusDetails.get("caretPosition");
		textComponent.requestFocus();
		textComponent.setCaretPosition(Math.min(caretPosition, textComponent.getText().length()));
	}

	@Override
	public void requestFocus() {
		textComponent.requestFocus();
	}

	@Override
	public void validateSubForm() throws Exception {
	}
}
