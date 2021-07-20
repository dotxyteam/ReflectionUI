


package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;

/**
 * Field proxy allowing to view/edit the base field value as a singleton list.
 * 
 * Note that the returned list is empty when the base field value is null.
 * 
 * @author olitank
 *
 */
public class ValueAsListFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo containingType;
	protected ITypeInfo type;

	public ValueAsListFieldInfo(ReflectionUI reflectionUI, IFieldInfo base, ITypeInfo containingType) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.containingType = containingType;
	}

	@Override
	public ITypeInfo getType() {
		if (type == null) {
			type = reflectionUI.buildTypeInfo(new StandardCollectionTypeInfo(
					new JavaTypeInfoSource(reflectionUI, ArrayList.class, null), super.getType()) {

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

				@Override
				public ITypeInfoSource getSource() {
					return new PrecomputedTypeInfoSource(this,
							new SpecificitiesIdentifier(containingType.getName(), ValueAsListFieldInfo.this.getName()));
				}

			}.getSource());
		}
		return type;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getValue(Object object) {
		Object singleItem = super.getValue(object);
		if (singleItem == null) {
			return Collections.emptyList();
		} else {
			return new ArrayList(Arrays.asList(singleItem));
		}
	}

	@Override
	public void setValue(Object object, Object value) {
		value = ((List<?>) value).get(0);
		super.setValue(object, value);
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
