package xy.reflect.ui.info;

import java.rmi.server.UID;
import java.util.HashMap;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * Each time a root form is validated, a single instance of this class is passed
 * to the {@link ITypeInfo#validate(Object)} method of the object and its
 * sub-objects that are validated.
 * 
 * @author olitank
 *
 */
public class ValidationSession extends HashMap<Object, Object> {

	private static final long serialVersionUID = 1L;

	private UID identifier = new UID();

	/**
	 * @return the unique identifier of this validation session.
	 */
	public UID getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return "ValidationSession [identifier=" + identifier + "]";
	}

}
