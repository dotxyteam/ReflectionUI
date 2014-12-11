package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public class SimpleTypeInfoDelegator implements ITypeInfo {
	protected ITypeInfo delegate;

	public SimpleTypeInfoDelegator(ITypeInfo delegate) {
		super();
		this.delegate = delegate;
	}

	public String getName() {
		return delegate.getName();
	}

	public String getCaption() {
		return delegate.getCaption();
	}

	public boolean isConcrete() {
		return delegate.isConcrete();
	}

	public List<IMethodInfo> getConstructors() {
		return delegate.getConstructors();
	}

	public List<IFieldInfo> getFields() {
		return delegate.getFields();
	}

	public List<IMethodInfo> getMethods() {
		return delegate.getMethods();
	}

	public Component createFieldControl(Object object, IFieldInfo field) {
		return delegate.createFieldControl(object, field);
	}

	public boolean supportsValue(Object value) {
		return delegate.supportsValue(value);
	};

	public List<ITypeInfo> getPolymorphicInstanceTypes() {
		return delegate.getPolymorphicInstanceTypes();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		if (!delegate.equals(((SimpleTypeInfoDelegator) obj).delegate)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public boolean isImmutable() {
		return delegate.isImmutable();
	}

	@Override
	public boolean hasCustomFieldControl() {
		return delegate.hasCustomFieldControl();
	}

}
