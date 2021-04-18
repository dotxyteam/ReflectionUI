/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.DelayedUpdateProcess;
import xy.reflect.ui.util.ReflectionUIUtils;

public class TextControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	protected JTextComponent textComponent;
	protected JScrollPane scrollPane;
	protected boolean listenerDisabled = false;

	protected Border defaultTextComponentBorder;
	protected DelayedUpdateProcess dataUpdateProcess = new DelayedUpdateProcess() {
		@Override
		protected void commit() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					TextControl.this.commitChanges();
				}
			});
		}

		@Override
		protected long getCommitDelayMilliseconds() {
			return TextControl.this.getCommitDelayMilliseconds();
		}
	};

	public TextControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
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

	protected void setupTextComponentEvents() {
		textComponent.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				TextControl.this.textComponentEditHappened();
			}
		});
		textComponent.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				TextControl.this.textComponentFocustLost();
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
								putValue(Action.NAME, swingRenderer.prepareStringToDisplay("Copy"));
							}
						}));
					}
					if (textComponent.isEditable()) {
						popup.add(new JMenuItem(new DefaultEditorKit.CutAction() {
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, swingRenderer.prepareStringToDisplay("Cut"));
							}
						}));
						popup.add(new JMenuItem(new DefaultEditorKit.PasteAction() {
							private static final long serialVersionUID = 1L;

							{
								putValue(Action.NAME, swingRenderer.prepareStringToDisplay("Paste"));
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
			if (isMultiline()) {
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			} else {
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			}
		}
	}

	protected boolean isMultiline() {
		return textComponent.getText().indexOf('\n') != -1;
	}

	protected JScrollPane createScrollPane() {
		return new ControlScrollPane() {

			protected static final long serialVersionUID = 1L;

			{
				SwingRendererUtils.removeScrollPaneBorder(this);
			}

			@Override
			public Dimension getPreferredSize() {
				return getDynamicPreferredSize(this, super.getPreferredSize());
			}
		};
	}

	protected Dimension getDynamicPreferredSize(JScrollPane scrollPane, Dimension defaultPreferredSize) {
		Dimension result = defaultPreferredSize;
		if (isMultiline()) {
			result.height += scrollPane.getHorizontalScrollBar().getPreferredSize().height;
		}
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
			if (!ReflectionUIUtils.equalsOrBothNull(textComponent.getText(), newText)) {
				int lastCaretPosition = textComponent.getCaretPosition();
				textComponent.setText(newText);
				setCurrentTextEditPosition(Math.min(lastCaretPosition, textComponent.getText().length()));
				displayError(null);
				SwingRendererUtils.handleComponentSizeChange(this);
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
		dataUpdateProcess.cancelCommitSchedule();
		dataUpdateProcess.scheduleCommit();
	}

	protected void textComponentFocustLost() {
		if (dataUpdateProcess.isCommitScheduled()) {
			dataUpdateProcess.cancelCommitSchedule();
			commitChanges();
		}
	}

	protected long getCommitDelayMilliseconds() {
		return 1000;
	}

	protected void commitChanges() {
		try {
			data.setValue(textComponent.getText());
		} catch (Throwable t) {
			swingRenderer.getReflectionUI().logError(t);
			displayError(ReflectionUIUtils.getPrettyErrorMessage(t));
		}
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
		setCurrentTextEditPosition(textComponent.getText().length());
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
