package xy.reflect.ui.info.field;

import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;

public class NerverNullField extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;

	protected static final Object NULL_REPLACEMENT = new Object() {
		@Override
		public String toString() {
			return "NULL_REPLACEMENT";
		}
	};

	public NerverNullField(ReflectionUI reflectionUI, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
	}

	@Override
	public boolean isValueNullable() {
		return false;
	}

	@Override
	public Object getValue(Object object) {
		Object result = super.getValue(object);
		if (result == null) {
			result = NULL_REPLACEMENT;
		}
		return result;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(Object.class));
	}

}