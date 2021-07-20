


package xy.reflect.ui.info.field;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;

/**
 * Field proxy allowing to view/edit the base field value as an enumeration
 * item.
 * 
 * @author olitank
 *
 */
public abstract class ValueOptionsAsEnumerationFieldInfo extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;

	protected GenericEnumerationFactory enumFactory;
	protected ITypeInfo enumType;

	protected abstract Object getObject();

	public ValueOptionsAsEnumerationFieldInfo(ReflectionUI reflectionUI, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.enumFactory = createEnumerationFactory();
		this.enumType = reflectionUI.buildTypeInfo(enumFactory
				.getInstanceTypeInfoSource(new SpecificitiesIdentifier(getObjectType().getName(), getName())));
	}

	protected GenericEnumerationFactory createEnumerationFactory() {
		String enumTypeName = "ValueOptions [ownerType=" + getObjectType().getName() + ", field=" + base.getName()
				+ "]";
		Iterable<Object> iterable = new Iterable<Object>() {
			@Override
			public Iterator<Object> iterator() {
				Object[] valueOptions = base.getValueOptions(getObject());
				return Arrays.asList(valueOptions).iterator();
			}
		};
		return new GenericEnumerationFactory(reflectionUI, iterable, enumTypeName, "", false,
				base.getType().supports(null)) {
			@Override
			protected String getItemName(Object item) {
				return "Option [index=" + getOrLoadItems().indexOf(item) + "]";
			}
		};
	}

	protected ITypeInfo getObjectType() {
		return reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(getObject()));
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
	public Object getValue(Object object) {
		Object value = super.getValue(object);
		return enumFactory.getItemInstance(value);
	}

	@Override
	public void setValue(Object object, Object value) {
		value = enumFactory.getInstanceItem(value);
		super.setValue(object, value);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		value = enumFactory.getInstanceItem(value);
		return super.getNextUpdateCustomUndoJob(object, value);
	}

	@Override
	public ITypeInfo getType() {
		return enumType;
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getObject() == null) ? 0 : getObject().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueOptionsAsEnumerationFieldInfo other = (ValueOptionsAsEnumerationFieldInfo) obj;
		if (getObject() == null) {
			if (other.getObject() != null)
				return false;
		} else if (!getObject().equals(other.getObject()))
			return false;
		return true;
	}

}
