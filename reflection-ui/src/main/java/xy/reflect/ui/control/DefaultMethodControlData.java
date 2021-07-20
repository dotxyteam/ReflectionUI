


package xy.reflect.ui.control;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;

/**
 * Default method control input. It just uses the given object and method to
 * provide the features needed by the controls.
 * 
 * @author olitank
 *
 */
public class DefaultMethodControlData extends AbstractMethodControlData {

	public static Object NO_OBJECT = new Object() {

		@Override
		public String toString() {
			return DefaultMethodControlData.class.getName() + ".NO_OBJECT";
		}

	};

	protected Object object;
	protected IMethodInfo method;

	public DefaultMethodControlData(ReflectionUI reflectionUI, Object object, IMethodInfo method) {
		super(reflectionUI);
		this.object = object;
		this.method = method;
	}

	public DefaultMethodControlData(ReflectionUI reflectionUI) {
		this(reflectionUI, NO_OBJECT, IMethodInfo.NULL_METHOD_INFO);
	}

	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public IMethodInfo getMethod() {
		return method;
	}

}
