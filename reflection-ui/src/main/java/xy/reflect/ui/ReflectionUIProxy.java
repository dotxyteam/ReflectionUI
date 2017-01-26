package xy.reflect.ui;

import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

public class ReflectionUIProxy implements IReflectionUI {

	protected IReflectionUI base;

	public ReflectionUIProxy(IReflectionUI base) {
		super();
		this.base = base;
	}

	public void registerPrecomputedTypeInfoObject(Object object, ITypeInfo type) {
		base.registerPrecomputedTypeInfoObject(object, type);
	}

	public void unregisterPrecomputedTypeInfoObject(Object object) {
		base.unregisterPrecomputedTypeInfoObject(object);
	}

	public ITypeInfoSource getTypeInfoSource(Object object) {
		return base.getTypeInfoSource(object);
	}

	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		return base.getTypeInfo(typeSource);
	}

	public void logInformation(String msg) {
		base.logInformation(msg);
	}

	public void logError(String msg) {
		base.logError(msg);
	}

	public void logError(Throwable t) {
		base.logError(t);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReflectionUIProxy other = (ReflectionUIProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReflectionUIProxy [base=" + base + "]";
	}
	
	

}
