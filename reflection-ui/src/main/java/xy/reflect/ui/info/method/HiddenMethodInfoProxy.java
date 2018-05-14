package xy.reflect.ui.info.method;

public class HiddenMethodInfoProxy extends MethodInfoProxy {

	public HiddenMethodInfoProxy(IMethodInfo base) {
		super(base);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

}