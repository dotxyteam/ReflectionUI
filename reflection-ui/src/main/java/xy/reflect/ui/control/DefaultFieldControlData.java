


package xy.reflect.ui.control;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;

/**
 * Default field control data. It just uses the given object and field to
 * provide the features needed by the controls.
 * 
 * @author olitank
 *
 */
public class DefaultFieldControlData extends AbstractFieldControlData implements IFieldControlData {

	public static Object NO_OBJECT = new Object() {

		@Override
		public String toString() {
			return DefaultFieldControlData.class.getName() + ".NO_OBJECT";
		}

	};

	protected Object object;
	protected IFieldInfo field;

	public DefaultFieldControlData(ReflectionUI reflectionUI, Object object, IFieldInfo field) {
		super(reflectionUI);
		this.object = object;
		this.field = field;
	}

	public DefaultFieldControlData(ReflectionUI reflectionUI) {
		this(reflectionUI, NO_OBJECT, IFieldInfo.NULL_FIELD_INFO);
	}

	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public IFieldInfo getField() {
		return field;
	}

}
