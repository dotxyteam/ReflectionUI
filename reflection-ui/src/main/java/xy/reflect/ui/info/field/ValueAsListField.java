package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;

public class ValueAsListField extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IListTypeInfo listType;

	public ValueAsListField(ReflectionUI reflectionUI, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.listType = createListType();
	}

	protected IListTypeInfo createListType() {
		return new StandardCollectionTypeInfo(reflectionUI, ArrayList.class, super.getType()) {

			@Override
			public boolean isInsertionAllowed() {
				return false;
			}

			@Override
			public boolean isRemovalAllowed() {
				return false;
			}

			@Override
			public boolean canReplaceContent() {
				return false;
			}

			@Override
			public boolean canInstanciateFromArray() {
				return true;
			}
			
			

		};
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(listType));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getValue(Object object) {
		return new ArrayList(Arrays.asList(super.getValue(object)));
	}

	@Override
	public void setValue(Object object, Object value) {
		value = ((List<?>) value).get(0);
		super.setValue(object, value);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public String toString() {
		return "ValueAsListField [base=" + base + "]";
	}

}
