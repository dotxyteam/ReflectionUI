


package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
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
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ReschedulableTask;
import xy.reflect.ui.util.MiscUtils;

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
	protected ReschedulableTask dataUpdateProcess = new ReschedulableTask() {
		@Override
		protected void execute() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						TextControl.this.commitChanges();
					} catch (Throwable t) {
						swingRenderer.handleObjectException(TextControl.this, t);
					}
				}
			});
		}

		@Override
		protected ExecutorService getTaskExecutor() {
			return swingRenderer.getDelayedUpdateExecutor();
		}

		@Override
		protected long getExecutionDelayMilliseconds() {
			return TextControl.this.getCommitDelayMilliseconds();
		}
	};

	public TextControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		input = adaptTextInput(input);
		this.input = input;
		this.data = input.getControlData();

		setLayout(new BorderLayout());

		textComponent = createTextComponent();
		{
			setupTextComponentEvents();
			scrollPane = createScrollPane();
			updateScrollPolicy();
			scrollPane.setViewportView(textComponent);
			add(scrollPane, BorderLayout.CENTER);
		}
		refreshUI(true);
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
					swingRenderer.handleObjectException(TextControl.this, t);
				}
			}
		});
		textComponent.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				try {
					TextControl.this.textComponentFocustLost();
				} catch (Throwable t) {
					swingRenderer.handleObjectException(TextControl.this, t);
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
		updateTextComponent(refreshStructure);
		return true;
	}

	protected void updateScrollPolicy() {
		if (scrollPane != null) {
			if (areScrollBarsEnabled()) {
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			} else {
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			}
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
				adaptSizeOnScrollBarsVisibilityChange();
			}

			@Override
			public Dimension getPreferredSize() {
				return getDynamicPreferredSize(this, super.getPreferredSize());
			}

			void adaptSizeOnScrollBarsVisibilityChange() {
				final ControlScrollPane thisScrollPane = this;
				getVerticalScrollBar().addHierarchyListener(new HierarchyListener() {
					@Override
					public void hierarchyChanged(HierarchyEvent e) {
						if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED
								&& (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									SwingRendererUtils.handleComponentSizeChange(thisScrollPane);
								}
							});
						}
					}
				});
				getHorizontalScrollBar().addHierarchyListener(new HierarchyListener() {
					@Override
					public void hierarchyChanged(HierarchyEvent e) {
						if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED
								&& (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									SwingRendererUtils.handleComponentSizeChange(thisScrollPane);
								}
							});
						}
					}
				});
			}
		};
	}

	protected Dimension getDynamicPreferredSize(ControlScrollPane scrollPane, Dimension defaultPreferredSize) {
		Dimension result = new Dimension(defaultPreferredSize);
		int characterSize = SwingRendererUtils.getStandardCharacterWidth(textComponent);
		int maxPreferredHeight = SwingRendererUtils.getScreenSize(this).height / 3;
		result.width = characterSize * 20;
		result.height = Math.min(result.height, maxPreferredHeight);
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
				textComponentEditHappened();
			}

			@Override
			public void setText(String t) {
				super.setText(t);
				updateScrollPolicy();
			}

		};
	}

	protected void updateTextComponent(boolean refreshStructure) {
		listenerDisabled = true;
		try {
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
			}
			String newText = (String) data.getValue();
			if (newText == null) {
				newText = "";
			}
			if (!MiscUtils.equalsOrBothNull(textComponent.getText(), newText)) {
				int lastCaretPosition = textComponent.getCaretPosition();
				textComponent.setText(newText);
				setCurrentTextEditPosition(Math.min(lastCaretPosition, textComponent.getText().length()));
				SwingRendererUtils.handleComponentSizeChange(textComponent);
			}
		} finally {
			listenerDisabled = false;
		}
	}

	protected void setCurrentTextEditPosition(int position) {
		textComponent.setCaretPosition(position);
	}

	protected void textComponentEditHappened() {
		updateScrollPolicy();
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

	protected long getCommitDelayMilliseconds() {
		return 1000;
	}

	protected void commitChanges() {
		data.setValue(textComponent.getText());
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
		setCurrentTextEditPosition(textComponent.getText().length());
		if (SwingRendererUtils.requestAnyComponentFocus(textComponent, swingRenderer)) {
			return true;
		}
		return false;
	}

	@Override
	public void validateSubForms() throws Exception {
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "TextControl [data=" + data + "]";
	}

}
