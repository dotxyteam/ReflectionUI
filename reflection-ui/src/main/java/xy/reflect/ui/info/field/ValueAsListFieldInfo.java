
package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field proxy allowing to view/edit the base field value as a singleton list.
 * 
 * @author olitank
 *
 */
public class ValueAsListFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo objectType;
	protected ITypeInfo type;

	public ValueAsListFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo objectType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.objectType = objectType;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.getTypeInfo(new StandardCollectionTypeInfo(reflectionUI, 
					new JavaTypeInfoSource(ArrayList.class, null), super.getType()) {

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
				public boolean canInstantiateFromArray() {
					return true;
				}

				@Override
				public boolean isItemNullValueSupported() {
					return base.getType().supports(null);
				}

				@Override
				public ITypeInfoSource getSource() {
					return new PrecomputedTypeInfoSource(this,
							new SpecificitiesIdentifier(objectType.getName(), ValueAsListFieldInfo.this.getName()));
				}

			}.getSource());
		}
		return type;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getValue(Object object) {
		Object singleItem = super.getValue(object);
		return new ArrayList(Arrays.asList(singleItem));
	}

	@Override
	public void setValue(Object object, Object value) {
		if (value == null) {
			throw new ReflectionUIError("<null> list value not supported");
		}
		ArrayList<?> list = (ArrayList<?>) value;
		if (list.size() != 1) {
			throw new ReflectionUIError("The list size must be equal to 1");
		}
		value = list.get(0);
		super.setValue(object, value);
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public String toString() {
		return "ValueAsListField [base=" + base + "]";
	}

}
