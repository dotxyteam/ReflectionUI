
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ReschedulableTask;

/**
 * Field control that displays String values in a text box.
 * 
 * @author olitank
 *
 */
public class TextControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	protected JTextComponent textComponent;
	protected JScrollPane scrollPane;
	protected boolean listenerDisabled = false;

	protected Border defaultTextComponentBorder;
	protected ReschedulableTask dataUpdateProcess;

	public TextControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		input = adaptTextInput(input);
		this.input = input;
		this.data = input.getControlData();
		this.dataUpdateProcess = createDelayedUpdateProcess();

		setLayout(new BorderLayout());

		textComponent = createTextComponent();
		{
			setupTextComponentEvents();
			scrollPane = createScrollPane();
			scrollPane.setViewportView(textComponent);
			add(scrollPane, BorderLayout.CENTER);
		}
		refreshUI(true);
	}

	protected ReschedulableTask createDelayedUpdateProcess() {
		return swingRenderer.createDelayedUpdateProcess(this, new Runnable() {
			@Override
			public void run() {
				TextControl.this.commitChanges();
			}
		}, 1000);
	}

	public JTextComponent getTextComponent() {
		return textComponent;
	}

	protected IFieldControlInput adaptTextInput(IFieldControlInput input) {
		return input;
	}

	protected void setupTextComponentEvents() {
		textComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				try {
					TextControl.this.textComponentEditHappened();
				} catch (Throwable t) {
					swingRenderer.handleException(TextControl.this, t);
				}
			}
		});
		textComponent.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				try {
					TextControl.this.textComponentFocustLost();
				} catch (Throwable t) {
					swingRenderer.handleException(TextControl.this, t);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		textComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JPopupMenu popup = new JPopupMenu();
					if (textComponent.getSelectedText() != null) {
						popup.add(new JMenuItem(new DefaultEditorKit.CopyAction() {
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, swingRenderer.prepareMessageToDisplay("Copy"));
							}
						}));
					}
					if (textComponent.isEditable()) {
						popup.add(new JMenuItem(new DefaultEditorKit.CutAction() {
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, swingRenderer.prepareMessageToDisplay("Cut"));
							}
						}));
						popup.add(new JMenuItem(new DefaultEditorKit.PasteAction() {
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, swingRenderer.prepareMessageToDisplay("Paste"));
							}
						}));
					}
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			updateScrollPolicy();
		}
		if (dataUpdateProcess.isScheduled()) {
			dataUpdateProcess.cancelSchedule();
			commitChanges();
		}
		refreshTextComponent(refreshStructure);
		return true;
	}

	protected void updateScrollPolicy() {
		if (areScrollBarsEnabled()) {
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		} else {
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		}
	}

	protected boolean areScrollBarsEnabled() {
		return true;
	}

	protected JScrollPane createScrollPane() {
		return new ControlScrollPane() {

			protected static final long serialVersionUID = 1L;

			{
				SwingRendererUtils.removeScrollPaneBorder(this);
			}

			@Override
			public Dimension getPreferredSize() {
				return getScrollPanePreferredSize(super.getPreferredSize());
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension result = super.getMinimumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.min(result.height, preferredSize.height);
					}
				}
				return result;

			}

			@Override
			public Dimension getMaximumSize() {
				Dimension result = super.getMaximumSize();
				if (result != null) {
					Dimension preferredSize = getPreferredSize();
					if (preferredSize != null) {
						result.height = Math.max(result.height, preferredSize.height);
					}
				}
				return result;
			}

		};
	}

	protected Dimension getScrollPanePreferredSize(Dimension defaultPreferredSize) {
		Dimension result = new Dimension(defaultPreferredSize);
		Dimension characterSize = new Dimension(SwingRendererUtils.getStandardCharacterWidth(textComponent),
				SwingRendererUtils.getStandardCharacterHeight(textComponent));
		result.width = Math.min(result.width, characterSize.width * 20);
		result.height = Math.min(result.height, characterSize.height * 10);
		return result;

	}

	protected JTextComponent createTextComponent() {
		return new JTextArea() {

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
				try {
					TextControl.this.textComponentEditHappened();
				} catch (Throwable t) {
					swingRenderer.handleException(TextControl.this, t);
				}
			}

		};
	}

	protected void refreshTextComponent(boolean refreshStructure) {
		if (refreshStructure) {
			if (data.isGetOnly()) {
				textComponent.setEditable(false);
				textComponent.setOpaque(false);
				textComponent.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
				textComponent.setBorder(BorderFactory.createEmptyBorder());
			} else {
				textComponent.setEditable(true);
				textComponent.setOpaque(true);
				if (data.getEditorForegroundColor() != null) {
					textComponent.setForeground(SwingRendererUtils.getColor(data.getEditorForegroundColor()));
					textComponent.setCaretColor(SwingRendererUtils.getColor(data.getEditorForegroundColor()));
				} else {
					textComponent.setForeground(new JTextField().getForeground());
					textComponent.setCaretColor(new JTextField().getForeground());
				}
				if (data.getEditorBackgroundColor() != null) {
					textComponent.setBackground(SwingRendererUtils.getColor(data.getEditorBackgroundColor()));
				} else {
					textComponent.setBackground(new JTextField().getBackground());
				}
				if (data.getBorderColor() != null) {
					textComponent.setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				} else {
					textComponent.setBorder(new JTextField().getBorder());
				}
			}
			if (data.getEditorCustomFontResourcePath() != null) {
				textComponent.setFont(SwingRendererUtils
						.loadFontThroughCache(data.getEditorCustomFontResourcePath(),
								ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
						.deriveFont(textComponent.getFont().getStyle(), textComponent.getFont().getSize()));
			} else {
				textComponent.setFont(new JTextField().getFont());
			}

		}
		listenerDisabled = true;
		try {
			updateTextComponentValue();
		} finally {
			listenerDisabled = false;
		}
	}

	protected void updateTextComponentValue() {
		String newText = (String) data.getValue();
		if (newText == null) {
			newText = "";
		}
		if (!MiscUtils.equalsOrBothNull(textComponent.getText(), newText)) {
			int lastCaretPosition = textComponent.getCaretPosition();
			textComponent.setText(newText);
			setCurrentTextEditPosition(Math.min(lastCaretPosition, textComponent.getDocument().getLength()));
			SwingRendererUtils.handleComponentSizeChange(textComponent);
		}
	}

	protected void commitChanges() {
		data.setValue(textComponent.getText());
	}

	protected void setCurrentTextEditPosition(int position) {
		textComponent.setCaretPosition(position);
	}

	protected void textComponentEditHappened() {
		if (listenerDisabled) {
			return;
		}
		dataUpdateProcess.reschedule();
	}

	protected void textComponentFocustLost() {
		if (dataUpdateProcess.isScheduled()) {
			dataUpdateProcess.cancelSchedule();
			commitChanges();
		}
	}

	@Override
	public boolean displayError(Throwable error) {
		SwingRendererUtils.displayErrorOnBorderAndTooltip(this, textComponent, error, swingRenderer);
		return true;
	}

	@Override
	public boolean showsCaption() {
		return false;
	}

	@Override
	public boolean isAutoManaged() {
		return false;
	}

	@Override
	public boolean requestCustomFocus() {
		if (data.isGetOnly()) {
			return false;
		}
		setCurrentTextEditPosition(textComponent.getDocument().getLength());
		if (SwingRendererUtils.requestAnyComponentFocus(textComponent, swingRenderer)) {
			return true;
		}
		return false;
	}

	@Override
	public void validateControlData(ValidationSession session) throws Exception {
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
	}

}
