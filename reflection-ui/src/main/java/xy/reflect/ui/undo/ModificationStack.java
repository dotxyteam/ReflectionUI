package xy.reflect.ui.undo;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.swing.JButton;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;

public class ModificationStack {


	public static final Object DO_EVENT = new Object();
	public static final Object UNDO_EVENT = new Object();
	public static final Object REDO_EVENT = new Object();
	public static final Object INVALIDATE_EVENT = new Object();

	protected static final String UNDO_TITLE_PREFIX = "(Revert) ";

	public static final IModification NULL_MODIFICATION = new IModification() {
		@Override
		public IModification applyAndGetOpposite(boolean refreshView) {
			return NULL_MODIFICATION;
		}

		@Override
		public int getNumberOfUnits() {
			return 0;
		}

		@Override
		public String toString() {
			return getTitle();
		}

		@Override
		public String getTitle() {
			return "NULL_MODIFICATION";
		}

	};
	public static final ModificationStack NULL_MODIFICATION_STACK = new ModificationStack(
			null) {

		@Override
		public void pushUndo(IModification undoModif) {
		}

		@Override
		public void undo(boolean refreshView) {
		}

		@Override
		public void redo(boolean refreshView) {
		}

	};

	protected Stack<IModification> undoStack = new Stack<IModification>();
	protected Stack<IModification> redoStack = new Stack<IModification>();
	protected String name;
	protected Stack<ModificationStack> compositeStack = new Stack<ModificationStack>();
	protected List<IModificationListener> listeners = new ArrayList<IModificationListener>();

	public ModificationStack(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + name + ")";
	}

	public void apply(IModification modif, boolean refreshView) {
		pushUndo(modif.applyAndGetOpposite(refreshView));
	}

	public void pushUndo(IModification undoModif) {
		if (compositeStack.size() > 0) {
			compositeStack.peek().pushUndo(undoModif);
			return;
		}
		if(undoModif.getNumberOfUnits() == 0){
			return;
		}
		undoStack.push(undoModif);
		redoStack.clear();
		notifyListeners(DO_EVENT);
	}

	public int getUndoSize() {
		return undoStack.size();
	}

	public int getRedoSize() {
		return redoStack.size();
	}

	public int getNumberOfUndoUnits() {
		int result = 0;
		for (IModification undoModif : undoStack) {
			result += undoModif.getNumberOfUnits();
		}
		return result;
	}

	public int getNumberOfRedoUnits() {
		int result = 0;
		for (IModification redoModif : redoStack) {
			result += redoModif.getNumberOfUnits();
		}
		return result;
	}

	public void undo(boolean refreshView) {
		if (compositeStack.size() > 0) {
			compositeStack.peek().undo(refreshView);
			return;
		}
		if (undoStack.size() == 0) {
			return;
		}
		IModification modif = undoStack.pop();
		redoStack.push(modif.applyAndGetOpposite(refreshView));
		notifyListeners(UNDO_EVENT);
	}

	public void redo(boolean refreshView) {
		if (compositeStack.size() > 0) {
			compositeStack.peek().redo(refreshView);
			return;
		}
		if (redoStack.size() == 0) {
			return;
		}
		IModification modif = redoStack.pop();
		undoStack.push(modif.applyAndGetOpposite(refreshView));
		notifyListeners(REDO_EVENT);
	}

	public void undoAll(boolean refreshView) {
		if (compositeStack.size() > 0) {
			compositeStack.peek().undoAll(refreshView);
			return;
		}
		while (undoStack.size() > 0) {
			undo(refreshView);
		}
	}

	public IModification[] getUndoModifications(UndoOrder order) {
		List<IModification> list = new ArrayList<IModification>(undoStack);
		if (order == UndoOrder.LIFO) {
			Collections.reverse(list);
		}
		return list.toArray(new IModification[list.size()]);
	}

	public void beginComposite() {
		compositeStack.push(new ModificationStack("(composite level "
				+ compositeStack.size() + ") " + name));
	}

	public void endComposite(String title, UndoOrder order) {
		CompositeModification compositeUndoModif = new CompositeModification(
				getUndoTitle(title), order, compositeStack.pop()
						.getUndoModifications(order));
		ModificationStack compositeParent;
		if (compositeStack.size() > 0) {
			compositeParent = compositeStack.peek();
		} else {
			compositeParent = this;
		}
		compositeParent.pushUndo(compositeUndoModif);
	}

	public void invalidate() {
		redoStack.clear();
		undoStack.clear();
		compositeStack.clear();
		notifyListeners(INVALIDATE_EVENT);
	}

	public void notifyListeners(Object event) {
		for (IModificationListener listener : new ArrayList<IModificationListener>(
				listeners)) {
			listener.handleEvent(event);
		}
	}






	public List<Component> createControls(final ReflectionUI reflectionUI) {
		List<Component> result = new ArrayList<Component>();

		result.add(createButton(reflectionUI, "Undo", new Runnable() {
			@Override
			public void run() {
				undo(true);
			}
		}, new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return undoStack.size() > 0;
			}
		}, new Accessor<String>() {
			@Override
			public String get() {
				if (undoStack.size() > 0) {
					return undoStack.peek().getTitle();
				} else {
					return null;
				}
			}
		}));

		result.add(createButton(reflectionUI, "Redo", new Runnable() {
			@Override
			public void run() {
				redo(true);
			}
		}, new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return redoStack.size() > 0;
			}
		}, new Accessor<String>() {
			@Override
			public String get() {
				if (redoStack.size() > 0) {
					return redoStack.peek().getTitle();
				} else {
					return null;
				}
			}
		}));

		result.add(createButton(reflectionUI, "Reset", new Runnable() {
			@Override
			public void run() {
				undoAll(true);
			}
		}, new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return undoStack.size() > 0;
			}
		}, new Accessor<String>() {
			@Override
			public String get() {
				return null;
			}
		}));

		return result;
	}

	public static String getUndoTitle(String title) {
		String result;
		if (title == null) {
			result = null;
		} else if (title.startsWith(UNDO_TITLE_PREFIX)) {
			result = title.substring(UNDO_TITLE_PREFIX.length());
		} else {
			result = UNDO_TITLE_PREFIX + title;
		}
		return result;
	}

	protected JButton createButton(final ReflectionUI reflectionUI,
			String label, final Runnable action,
			final Accessor<Boolean> enabled, final Accessor<String> tooltipText) {
		final JButton result = new JButton(
				reflectionUI.translateUIString(label)) {

			private static final long serialVersionUID = 1L;
			IModificationListener listener = new IModificationListener() {
				@Override
				public void handleEvent(Object event) {
					updateState();
				}
			};

			{
				updateState();
				listeners.add(listener);
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				listeners.remove(listener);
			}

			private void updateState() {
				setEnabled(enabled.get());
				ReflectionUIUtils.setMultilineToolTipText(this, reflectionUI
						.translateUIString(reflectionUI
								.translateUIString(tooltipText.get())));
			}

		};
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					action.run();
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(result, t);
				}
			}
		});
		result.setEnabled(enabled.get());
		return result;
	}

}