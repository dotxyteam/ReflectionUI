package xy.reflect.ui.info.type;

import java.awt.Component;
import java.util.List;

import xy.reflect.ui.info.ICommonInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;

public interface ITypeInfo extends ICommonInfo{

	boolean isConcrete();

	List<IMethodInfo> getConstructors();

	List<IFieldInfo> getFields();

	List<IMethodInfo> getMethods();
	
	Component createFieldControl(Object object, IFieldInfo field);

	boolean supportsValue(Object value);
	
	List<ITypeInfo> getPolymorphicInstanceTypes();
}
