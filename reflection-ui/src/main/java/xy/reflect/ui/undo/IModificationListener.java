package xy.reflect.ui.undo;
public interface IModificationListener {

	void handlePush(IModification undoModification);
	void handleUdno(IModification undoModification);
	void handleRedo(IModification modification);
	void handleInvalidate();
	void handleInvalidationCleared();
}
