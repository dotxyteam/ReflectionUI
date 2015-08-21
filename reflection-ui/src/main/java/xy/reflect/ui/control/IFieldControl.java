package xy.reflect.ui.control;

import xy.reflect.ui.util.ReflectionUIError;

public interface IFieldControl {
	boolean displayError(ReflectionUIError error);

	boolean showCaption();

	boolean refreshUI();

}
