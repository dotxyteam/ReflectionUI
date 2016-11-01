package xy.reflect.ui.undo;
public interface IModificationListener {

	void handleDo(IModification modification);
	void handleUdno(IModification undoModification);
	void handleRedo(IModification modification);
	void handleInvalidate();
	void handleInvalidationCleared();
}
