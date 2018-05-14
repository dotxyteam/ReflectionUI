package xy.reflect.ui.info.field;

public class HiddenFieldInfoProxy extends FieldInfoProxy {

	public HiddenFieldInfoProxy(IFieldInfo base) {
		super(base);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

}
