package xy.reflect.ui.undo;
public enum UndoOrder {
	LIFO, FIFO;
	
	public static UndoOrder getDefault(){
		return LIFO;
	}
};
