package xy.reflect.ui.info.field;

import java.util.Arrays;
import java.util.Iterator;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.GenericEnumerationFactory;

public class ValueOptionsAsEnumerationField extends FieldInfoProxy {

	protected Object object;
	
	protected GenericEnumerationFactory enumFactory;
	protected ITypeInfo enumType;
	private ReflectionUI reflectionUI;
	
	public ValueOptionsAsEnumerationField(ReflectionUI reflectionUI, Object object, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.enumFactory = createArrayAsEnumerationFactory();
		this.enumType = reflectionUI.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
	}

	protected GenericEnumerationFactory createArrayAsEnumerationFactory() {
		ITypeInfo ownerType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		String enumTypeName = "ValueOptions [ownerType=" + ownerType.getName() + ", field=" + base.getName() + "]";
		Iterable<Object> iterable =  new Iterable<Object>(){
			@Override
			public Iterator<Object> iterator() {
				Object[] valueOptions = base.getValueOptions(object);
				return Arrays.asList(valueOptions).iterator();
			}			
		};
		return new GenericEnumerationFactory(reflectionUI, iterable, enumTypeName, "", false);
	}
	
	@Override
	public Object[] getValueOptions(Object object) {
		return null;
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
