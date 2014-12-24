package xy.reflect.ui.info.type;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PrecomputedTypeInfoInstanceWrapper {

	private Object instance;
	private ITypeInfo precomputedType;

	
	
	public PrecomputedTypeInfoInstanceWrapper(Object instance,
			ITypeInfo precomputedType) {
		this.instance = instance;
		this.precomputedType = precomputedType;
	}
	


	public ITypeInfo getPrecomputedType() {
		return precomputedType;
	}

	public PrecomputedTypeInfoSource getPrecomputedTypeInfoSource() {
		return new PrecomputedTypeInfoSource(new TypeInfoProxy() {

			@Override
			protected Object getValue(Object object, IFieldInfo field,
					ITypeInfo containingType) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				return super.getValue(object, field, containingType);
			}

			@Override
			protected void setValue(Object object, Object value,
					IFieldInfo field, ITypeInfo containingType) {
				object = ((PrecomputedTypeInfoInstanceWrapper) object)
						.getInstance();
				super.setValue(object, value, field, containingType);
			}

		}.get(precomputedType));
	}

	public Object getInstance() {
		return instance;
	}

	@Override
	public int hashCode() {
		return instance.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PrecomputedTypeInfoInstanceWrapper)) {
			return false;
		}
		PrecomputedTypeInfoInstanceWrapper other = (PrecomputedTypeInfoInstanceWrapper) obj;
		if(! ReflectionUIUtils.equalsOrBothNull(instance, other.instance)){
			return false;
		}
		if(!precomputedType.equals(other.precomputedType)){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return instance.toString();
	}

}
