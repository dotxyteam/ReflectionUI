package xy.reflect.ui.info.field;

import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;

public class ValueAsListField extends FieldInfoProxy {

	private ReflectionUI reflectionUI;

	public ValueAsListField(ReflectionUI reflectionUI, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(
				new StandardCollectionTypeInfo(reflectionUI, List.class, super.getType()){

					@Override
					public boolean canAdd() {
						return false;
					}

					@Override
					public boolean canRemove() {
						return false;
					}
					
				}));
	}

	@Override
	public Object getValue(Object object) {
		return Collections.singletonList(super.getValue(object));
	}

	@Override
	public void setValue(Object object, Object value) {
		value = ((List<?>)value).get(0); 
		super.setValue(object, value);
	}

	@Override
	public String toString() {
		return "ValueAsListField [base=" + base + "]";
	}

}
