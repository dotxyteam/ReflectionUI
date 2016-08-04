package xy.reflect.ui.undo;
public interface IModification {
	IModification applyAndGetOpposite();

	int getNumberOfUnits();

	String getTitle();
}
