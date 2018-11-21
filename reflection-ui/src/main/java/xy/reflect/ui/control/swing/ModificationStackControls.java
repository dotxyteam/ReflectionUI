package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AbstractControlButton;

public class ModificationStackControls {

	protected ModificationStack modificationStack;

	public ModificationStackControls(ModificationStack modificationStack) {
		super();
		this.modificationStack = modificationStack;
	}

	protected JButton createButton(final SwingRenderer swingRenderer, final String label, final Runnable action,
			final Accessor<Boolean> enabled, final Accessor<String> tooltipText) {
		final JButton result = new AbstractControlButton() {

			protected static final long serialVersionUID = 1L;
			IModificationListener listener = new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							updateState();
						}
					});
				}
			};
			{
				updateState();
				modificationStack.addListener(listener);
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			protected boolean isApplicationStyleButtonSpecific() {
				return true;
			}

			@Override
			public String retrieveCaption() {
				return label;
			}

			@Override
			public String retrieveToolTipText() {
				return tooltipText.get();
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				modificationStack.removeListener(listener);
			}

			protected void updateState() {
				setEnabled(enabled.get());
				SwingRendererUtils.setMultilineToolTipText(this,
						swingRenderer.prepareStringToDisplay(tooltipText.get()));
			}

		};
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swingRenderer.getDataUpdateJobExecutor().submit(new Runnable() {
					@Override
					public void run() {
						try {
							action.run();
						} catch (final Throwable t) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									swingRenderer.handleExceptionsFromDisplayedUI(result, t);
								}
							});
						}
					}
				});
			}
		});
		result.setEnabled(enabled.get());
		return result;
	}

	public List<Component> create(SwingRenderer swingRenderer) {
		List<Component> result = new ArrayList<Component>();
		result.add(createUndoButton(swingRenderer));
		result.add(createRedoButton(swingRenderer));
		result.add(createResetButton(swingRenderer));
		return result;
	}

	protected Component createUndoButton(final SwingRenderer swingRenderer) {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				modificationStack.undo();
			}
		};
		Accessor<Boolean> enabled = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return modificationStack.canUndo();
			}
		};
		Accessor<String> tooltipText = new Accessor<String>() {
			@Override
			public String get() {
				if (modificationStack.getUndoSize() > 0) {
					return modificationStack.getUndoModifications()[modificationStack.getUndoModifications().length - 1]
							.getTitle();
				} else {
					return null;
				}
			}
		};
		JButton button = createButton(swingRenderer, "Undo", action, enabled, tooltipText);
		return button;
	}

	protected Component createRedoButton(final SwingRenderer swingRenderer) {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				modificationStack.redo();
			}
		};
		Accessor<Boolean> enabled = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return modificationStack.canRedo();
			}
		};
		Accessor<String> tooltipText = new Accessor<String>() {
			@Override
			public String get() {
				if (modificationStack.getRedoSize() > 0) {
					return modificationStack.getRedoModifications()[modificationStack.getRedoModifications().length - 1]
							.getTitle();
				} else {
					return null;
				}
			}
		};
		JButton button = createButton(swingRenderer, "Redo", action, enabled, tooltipText);
		return button;
	}

	protected Component createResetButton(final SwingRenderer swingRenderer) {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				modificationStack.undoAll();
			}
		};
		Accessor<Boolean> enabled = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return modificationStack.canReset();
			}
		};
		Accessor<String> tooltipText = new Accessor<String>() {
			@Override
			public String get() {
				return null;
			}
		};
		JButton button = createButton(swingRenderer, "Reset", action, enabled, tooltipText);
		return button;
	}

}
