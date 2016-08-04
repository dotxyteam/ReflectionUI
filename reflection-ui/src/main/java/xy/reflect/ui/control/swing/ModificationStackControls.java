package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.SwingRendererUtils;

public class ModificationStackControls {

	protected ModificationStack modificationStack;

	public ModificationStackControls(ModificationStack modificationStack) {
		super();
		this.modificationStack = modificationStack;
	}

	protected JButton createButton(final ReflectionUI reflectionUI, String label, final Runnable action,
			final Accessor<Boolean> enabled, final Accessor<String> tooltipText) {
		final JButton result = new JButton(reflectionUI.prepareStringToDisplay(label)) {

			protected static final long serialVersionUID = 1L;
			IModificationListener listener = new IModificationListener() {
				@Override
				public void handleEvent(Object event) {
					updateState();
				}
			};

			{
				updateState();
				modificationStack.getListeners().add(listener);
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				modificationStack.getListeners().remove(listener);
			}

			protected void updateState() {
				setEnabled(enabled.get());
				SwingRendererUtils.setMultilineToolTipText(this,
						reflectionUI.prepareStringToDisplay(reflectionUI.prepareStringToDisplay(tooltipText.get())));
			}

		};
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					action.run();
				} catch (Throwable t) {
					new SwingRenderer(reflectionUI).handleExceptionsFromDisplayedUI(result, t);
				}
			}
		});
		result.setEnabled(enabled.get());
		return result;
	}

	public List<Component> createControls(ReflectionUI reflectionUI) {
		List<Component> result = new ArrayList<Component>();
		result.add(createUndoButton(reflectionUI));
		result.add(createRedoButton(reflectionUI));
		result.add(createResetButton(reflectionUI));
		return result;
	}

	protected Component createRedoButton(final ReflectionUI reflectionUI) {
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
					return modificationStack.getRedoModifications(UndoOrder.LIFO)[0].getTitle();
				} else {
					return null;
				}
			}
		};
		JButton button = createButton(reflectionUI, "Redo", action, enabled, tooltipText);
		return button;
	}

	protected Component createResetButton(final ReflectionUI reflectionUI) {
		Runnable action = new Runnable() {
			@Override
			public void run() {
				modificationStack.undoAll();
			}
		};
		Accessor<Boolean> enabled = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return modificationStack.canUndo() && !modificationStack.isInvalidated();
			}
		};
		Accessor<String> tooltipText = new Accessor<String>() {
			@Override
			public String get() {
				return null;
			}
		};
		JButton button = createButton(reflectionUI, "Reset", action, enabled, tooltipText);
		return button;
	}

	protected Component createUndoButton(final ReflectionUI reflectionUI) {
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
					return modificationStack.getUndoModifications(UndoOrder.LIFO)[0].getTitle();
				} else {
					return null;
				}
			}
		};
		JButton button = createButton(reflectionUI, "Undo", action, enabled, tooltipText);
		return button;
	}

}
