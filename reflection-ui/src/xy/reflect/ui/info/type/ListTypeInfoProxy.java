package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public class ListTypeInfoProxy implements IListTypeInfo {
	protected IListTypeInfo base;

	public ListTypeInfoProxy(IListTypeInfo base) {
		super();
		this.base = base;
	}

	public String getName() {
		return base.getName();
	}

	public String getCaption() {
		return base.getCaption();
	}

	public ITypeInfo getItemType() {
		return base.getItemType();
	}

	public List<?> toStandardList(Object value) {
		return base.toStandardList(value);
	}

	public Object fromStandardList(List<?> list) {
		return base.fromStandardList(list);
	}

	public boolean isConcrete() {
		return base.isConcrete();
	}

	public List<IMethodInfo> getConstructors() {
		return base.getConstructors();
	}

	public IListStructuralInfo getStructuralInfo() {
		return base.getStructuralInfo();
	}

	public List<IFieldInfo> getFields() {
		return base.getFields();
	}

	public List<IMethodInfo> getMethods() {
		return base.getMethods();
	}

	public Component createFieldControl(Object object, IFieldInfo field) {
		return base.createFieldControl(object, field);
	}

	public boolean supportsValue(Object value) {
		return base.supportsValue(value);
	}

	public List<ITypeInfo> getPolymorphicInstanceTypes() {
		return base.getPolymorphicInstanceTypes();
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!base.equals(((ListTypeInfoProxy) obj).base)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return base.toString();
	}

	@Override
	public boolean isOrdered() {
		return base.isOrdered();
	}
	

	@Override
	public boolean isImmutable() {
		return base.isImmutable();
	}

	@Override
	public boolean hasCustomFieldControl() {
		return base.hasCustomFieldControl();
	}


}
