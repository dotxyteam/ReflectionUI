package xy.reflect.ui.undo;
public interface IModificationListener {

	Object DO_EVENT = new Object();
	Object UNDO_EVENT = new Object();
	Object REDO_EVENT = new Object();
	Object INVALIDATE_EVENT = new Object();

	void handleEvent(Object event);
}
