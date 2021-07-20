


package xy.reflect.ui.info;

/**
 * Base class of abstract UI model elements allowing to ensure that they
 * override important methods.
 * 
 * @author olitank
 *
 */
public abstract class AbstractInfo implements IInfo {

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

}
