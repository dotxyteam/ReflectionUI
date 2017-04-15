package xy.reflect.ui.info.filter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;

public interface IInfoFilter {

	public IInfoFilter DEFAULT = new IInfoFilter() {

		ITypeInfo typicalObjectType = new ReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class));

		@Override
		public boolean excludeField(IFieldInfo field) {
			String fieldName = field.getName();
			for (IFieldInfo typicalObjectField : typicalObjectType.getFields()) {
				String typicalObjectFieldName = typicalObjectField.getName();
				if (typicalObjectFieldName.equals(fieldName)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean excludeMethod(IMethodInfo method) {
			String methodSignature = ReflectionUIUtils.getMethodSignature(method);
			for (IMethodInfo typicalObjectMethod : typicalObjectType.getMethods()) {
				String typicalObjectMethodSignature = ReflectionUIUtils.getMethodSignature(typicalObjectMethod);
				if (typicalObjectMethodSignature.equals(methodSignature)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return IInfoFilter.class.getName() + ".DEFAULT";
		}

	};

	boolean excludeField(IFieldInfo field);

	boolean excludeMethod(IMethodInfo method);

}
