


package xy.reflect.ui.info.method;

/**
 * Method proxy allowing to hide the base method.
 * 
 * @author olitank
 *
 */
public class HiddenMethodInfoProxy extends MethodInfoProxy {

	public HiddenMethodInfoProxy(IMethodInfo base) {
		super(base);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

}
