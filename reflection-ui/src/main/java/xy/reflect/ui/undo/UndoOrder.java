package xy.reflect.ui.undo;
public enum UndoOrder {
	LIFO, FIFO;
	
	public static UndoOrder getNormal(){
		return LIFO;
	}
	
	public static UndoOrder getInverse(){
		return FIFO;
	}
};
