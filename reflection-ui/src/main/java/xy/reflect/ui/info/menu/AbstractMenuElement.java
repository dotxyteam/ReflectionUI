package xy.reflect.ui.info.menu;

import java.rmi.server.UID;

public abstract class AbstractMenuElement implements IMenuElement {
	private static final long serialVersionUID = 1L;

	protected String uniqueIdentifier = new UID().toString();

	@Override
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	@Override
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

}
