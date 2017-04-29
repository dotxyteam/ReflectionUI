package xy.reflect.ui.menu;

import java.io.Serializable;

public interface IMenuElement extends Serializable {

	String getName();

	void setUniqueIdentifier(String uniqueIdentifier);

	String getUniqueIdentifier();
}
