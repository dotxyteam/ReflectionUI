package xy.reflect.ui.info.type.util;

import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;

public class FilteredTypeFactory extends TypeCastFactory {

	protected IInfoFilter filter;

	public FilteredTypeFactory(ReflectionUI reflectionUI, ITypeInfo targetType, IInfoFilter filter) {
		super(reflectionUI, targetType);
		this.filter = filter;
	}

	@Override
	protected InstanceTypeInfoFactory createInstanceTypeInfoFactory() {
		return new InstanceTypeInfoFactory() {

			@Override
			protected String getName(ITypeInfo type) {
				return "TypeInfo of " + FilteredTypeFactory.this.toString();
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				List<IFieldInfo> result = new ArrayList<IFieldInfo>();
				for (IFieldInfo field : super.getFields(type)) {
					if (!filter.excludeField(field)) {
						result.add(field);
					}
				}
				return result;
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>();
				for (IMethodInfo method : super.getMethods(type)) {
					if (!filter.excludeMethod(method)) {
						result.add(method);
					}
				}
				return result;
			}

			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
				List<ITypeInfo> result = new ArrayList<ITypeInfo>();
				for (ITypeInfo subType : type.getPolymorphicInstanceSubTypes()) {
					result.add(new FilteredTypeFactory(reflectionUI, subType, filter).instanceTypeInfoFactory
							.get(subType));
				}
				return result;
			}

		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
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
		FilteredTypeFactory other = (FilteredTypeFactory) obj;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FilteredTypeFactory [filter=" + filter + ", targetType=" + targetType + "]";
	}
	
	

}
