package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;

public class ValueAsListFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IListTypeInfo listType;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public ValueAsListFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo containingType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.listType = createListType();
		this.containingType = containingType;
	}

	protected IListTypeInfo createListType() {
		return new StandardCollectionTypeInfo(reflectionUI, new JavaTypeInfoSource(ArrayList.class,
				new SpecificitiesIdentifier(containingType.getName(), getName())), super.getType()) {

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
		if (type == null) {
			type = reflectionUI.getTypeInfo(new PrecomputedTypeInfoSource(listType,
					new SpecificitiesIdentifier(containingType.getName(), getName())));
		}
		return type;
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
