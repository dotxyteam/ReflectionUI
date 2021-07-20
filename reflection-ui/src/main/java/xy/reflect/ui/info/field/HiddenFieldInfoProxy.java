


package xy.reflect.ui.info.field;

/**
 * Field proxy allowing to hide the base field.
 * 
 * @author olitank
 *
 */
public class HiddenFieldInfoProxy extends FieldInfoProxy {

	public HiddenFieldInfoProxy(IFieldInfo base) {
		super(base);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

}
