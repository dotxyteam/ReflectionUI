package xy.reflect.ui.undo;
public interface IModification {
	IModification applyAndGetOpposite(boolean refreshView);

	int getNumberOfUnits();

	String getTitle();
}
