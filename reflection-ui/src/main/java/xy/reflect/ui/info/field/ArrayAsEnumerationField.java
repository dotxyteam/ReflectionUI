package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ArrayAsEnumerationFactory;

public class ArrayAsEnumerationField extends FieldInfoProxy {

	protected ArrayAsEnumerationFactory enumFactory;
	protected ITypeInfo enumType;

	public ArrayAsEnumerationField(ReflectionUI reflectionUI, IFieldInfo base, Object[] valueOptions,
			String enumerationTypeName) {
		super(base);
		this.enumFactory = createArrayAsEnumerationFactory(reflectionUI, valueOptions, enumerationTypeName, "");
		this.enumType = reflectionUI.getTypeInfo(enumFactory.getInstanceTypeInfoSource());

	}

	protected ArrayAsEnumerationFactory createArrayAsEnumerationFactory(ReflectionUI reflectionUI,
			Object[] valueOptions, String enumerationTypeName, String string) {
		return new ArrayAsEnumerationFactory(reflectionUI, valueOptions, enumerationTypeName, "");
	}

	@Override
	public Object getValue(Object object) {
		Object value = super.getValue(object);
		return enumFactory.getInstance(value);
	}

	@Override
	public void setValue(Object object, Object value) {
		value = enumFactory.unwrapInstance(value);
		super.setValue(object, value);
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		value = enumFactory.unwrapInstance(value);
		return super.getCustomUndoUpdateJob(object, value);
	}

	@Override
	public ITypeInfo getType() {
		return enumType;
	}
}
