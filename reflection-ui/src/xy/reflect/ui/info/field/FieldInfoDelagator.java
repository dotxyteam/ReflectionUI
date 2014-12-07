package xy.reflect.ui.info.field;

import xy.reflect.ui.info.type.ITypeInfo;

public class FieldInfoDelagator implements IFieldInfo {

	protected IFieldInfo delegate;

	public FieldInfoDelagator(IFieldInfo delegate) {
		this.delegate = delegate;
		if(delegate == null){
			System.out.println("debug");
		}
	}

	@Override
	public Object getValue(Object object) {
		return delegate.getValue(object);
	}

	@Override
	public ITypeInfo getType() {
		return delegate.getType();
	}

	@Override
	public String getCaption() {
		return delegate.getCaption();
	}

	@Override
	public void setValue(Object object, Object value) {
		delegate.setValue(object, value);
	}

	@Override
	public boolean isNullable() {
		return delegate.isNullable();
	}

	@Override
	public boolean isReadOnly() {
		return delegate.isReadOnly();
	}
	
	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(obj == this){
			return true;
		}
		if(!getClass().equals(obj.getClass())){
			return false;
		}
		return delegate.equals(((FieldInfoDelagator) obj).delegate);
	}
	
	

}
